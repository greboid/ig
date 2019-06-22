package com.greboid.scraper

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.google.gson.Gson
import com.google.gson.JsonParseException
import com.greboid.scraper.retrievers.IGRetriever
import freemarker.cache.ClassTemplateLoader
import freemarker.template.Configuration
import freemarker.template.Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.auth.UserIdPrincipal
import io.ktor.auth.authenticate
import io.ktor.auth.jwt.jwt
import io.ktor.features.CORS
import io.ktor.features.Compression
import io.ktor.features.ConditionalHeaders
import io.ktor.features.ContentNegotiation
import io.ktor.features.ContentTransformationException
import io.ktor.features.DefaultHeaders
import io.ktor.features.PartialContent
import io.ktor.features.StatusPages
import io.ktor.features.XForwardedHeaderSupport
import io.ktor.features.statusFile
import io.ktor.freemarker.FreeMarker
import io.ktor.freemarker.FreeMarkerContent
import io.ktor.gson.gson
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.defaultResource
import io.ktor.http.content.files
import io.ktor.http.content.resolveResource
import io.ktor.http.content.resources
import io.ktor.http.content.static
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.response.respondRedirect
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import io.ktor.routing.routing
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import io.ktor.sessions.SessionStorageMemory
import io.ktor.sessions.SessionTransportTransformerMessageAuthentication
import io.ktor.sessions.Sessions
import io.ktor.sessions.clear
import io.ktor.sessions.cookie
import io.ktor.sessions.get
import io.ktor.sessions.sessions
import io.ktor.util.KtorExperimentalAPI
import io.ktor.util.hex
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.StringWriter
import java.security.Security
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.Date

data class IGSession(val user: String, val admin: Boolean, var previousPage: String = "/")

open class SimpleJWT(secret: String) {
    private val algorithm = Algorithm.HMAC256(secret)
    val verifier: JWTVerifier = JWT.require(algorithm).build()
    fun sign(name: String): AuthToken {
        val expires = Date.from(
            LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC)
            .plusHours(1)
            .toInstant(ZoneOffset.UTC)
        )
        val token = JWT.create().withClaim("name", name)
            .withExpiresAt(expires)
            .sign(algorithm)
        return AuthToken(token, expires)
    }
}
data class AuthToken(val token: String, val expires: Date)

class LoginRegister(val user: String, val password: String)

class Web(
    private val database: Database,
    private val config: Config,
    private val retriever: IGRetriever
) {
    @KtorExperimentalAPI
    suspend fun start() {
        System.setProperty("io.ktor.random.secure.random.provider", "DRBG")
        Security.setProperty("securerandom.drbg.config", "HMAC_DRBG,SHA-512,256,pr_and_reseed")
        val simpleJwt = SimpleJWT(config.jwtKey)
        val server = embeddedServer(CIO, port = config.webPort) {
            install(CORS) {
                method(HttpMethod.Options)
                method(HttpMethod.Get)
                method(HttpMethod.Post)
                method(HttpMethod.Put)
                method(HttpMethod.Delete)
                method(HttpMethod.Patch)
                header(HttpHeaders.Authorization)
                allowCredentials = true
                anyHost()
            }
            install(DefaultHeaders)
            install(PartialContent)
            install(Compression)
            install(ConditionalHeaders)
            install(XForwardedHeaderSupport)
            install(ContentNegotiation) {
                gson {
                    setPrettyPrinting()
                }
            }
            install(Sessions) {
                cookie<IGSession>("session", SessionStorageMemory()) {
                    transform(SessionTransportTransformerMessageAuthentication(hex(config.sessionKey), "HmacSHA256"))
                }
            }
            install(StatusPages) {
                statusFile(HttpStatusCode.NotFound, HttpStatusCode.Unauthorized, filePattern = "statusFiles/#.html")
                exception<Throwable> { cause ->
                    call.respond(HttpStatusCode.InternalServerError, "Internal Server Error")
                    cause.printStackTrace()
                }
            }
            install(FreeMarker) {
                templateLoader = ClassTemplateLoader(this::class.java.classLoader, "templates")
            }
            install(Authentication) {
                jwt(name="auth") {
                    verifier(simpleJwt.verifier)
                    validate {
                        UserIdPrincipal(it.payload.getClaim("name").asString())
                    }
                }
            }
            routing {
                static("/js") {
                    resources("js")
                }
                static("/css") {
                    resources("css")
                }
                static("/thumbs") {
                    files("thumbs")
                }
                post("/login") {
                    try {
                        val credentials = call.receive<LoginRegister>()
                        if (credentials.user == config.adminUsername && credentials.password == config.adminPassword) {
                            val token = simpleJwt.sign(credentials.user)
                            call.respond(
                                mapOf(
                                    "token" to token.token, "expires" to token.expires.toInstant().epochSecond
                                )
                            )
                        } else {
                            call.respond(
                                HttpStatusCode.Unauthorized, mapOf("message" to "Invalid Credentials")
                            )
                        }
                    } catch (e: JsonParseException) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Bad payload"))
                    } catch (e: ContentTransformationException) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Bad payload"))
                    }
                }
                authenticate("auth") {
                    get("/refreshtoken") {
                        val token = simpleJwt.sign(call.sessions.get<IGSession>()?.user ?: "")
                        call.respond(
                            mapOf(
                                "token" to token.token,
                                "expires" to token.expires.toInstant().epochSecond
                            )
                        )
                    }
                }
                get("/favicon.ico") {
                    call.respond(call.resolveResource("/favicon.ico", "") ?: HttpStatusCode.NotFound)
                }
                get("/logout") {
                    call.sessions.clear<IGSession>()
                    call.respondRedirect("/", false)
                }
                get("/igposts") {
                    val start: Int = call.request.queryParameters["start"]?.toInt() ?: 0
                    val count: Int = call.request.queryParameters["count"]?.toInt() ?: 5
                    val profile: String = call.request.queryParameters["profile"] ?: ""
                    val user: String = call.request.queryParameters["user"] ?: ""
                    if (profile.isNotEmpty()) {
                        call.respondText(Gson().toJson(database.getIGPost(profile, start, count)), ContentType.Application.Json)
                    } else if (user.isNotEmpty()) {
                        call.respondText(Gson().toJson(database.getUserIGPost(user, start, count)), ContentType.Application.Json)
                    } else {
                        call.respondText("", ContentType.Application.Json)
                    }
                }
                get("/profiles") {
                    call.respondText(Gson().toJson(database.getProfiles()), ContentType.Application.Json)
                }
                get("/users") {
                    call.respondText(Gson().toJson(database.getUsers()), ContentType.Application.Json)
                }
                get("/ProfileUsers/{profile?}") {
                    val profile = call.parameters["profile"] ?: ""
                    if (profile.isEmpty()) {
                        call.respond(HttpStatusCode.NotFound, "Page not found.")
                    } else {
                        call.respondText(Gson().toJson(database.getProfileUsers(profile)), ContentType.Application.Json)
                    }
                }
                get("/userprofiles/{user?}") {
                    val user = call.parameters["user"] ?: ""
                    if (user.isEmpty()) {
                        call.respond(HttpStatusCode.NotFound, "Page not found.")
                    } else {
                        call.respondText(Gson().toJson(database.getUserProfiles(user)), ContentType.Application.Json)
                    }
                }
                route("/admin") {
                    static("/") {
                        resources("/admin")
                        defaultResource("index.html", "/admin")
                    }
                    authenticate("auth") {
                        post("/ProfileUsers") {
                            val categoryUsers =
                                Gson().fromJson(call.receive<String>(), ProfileUsers::class.java).profiles
                            for (categoryUser in categoryUsers) {
                                val currentCategory = categoryUser.key
                                val newUsers = categoryUser.value
                                val currentUsers = database.getProfileUsers(currentCategory)
                                val usersToRemove = currentUsers.minus(newUsers)
                                val usersToAdd = newUsers.minus(currentUsers)
                                usersToRemove.forEach { user -> database.delUserProfile(user, currentCategory) }
                                usersToAdd.forEach { user -> database.addUserProfile(user, currentCategory) }
                            }
                            call.respond(HttpStatusCode.Accepted)
                        }
                        post("/users") {
                            val newUsers = Gson().fromJson(call.receive<String>(), Array<String>::class.java).toList()
                            val currentUsers = database.getUsers()
                            val usersToRemove = currentUsers.minus(newUsers)
                            val usersToAdd = newUsers.subtract(currentUsers)
                            usersToRemove.forEach { user -> database.delUser(user) }
                            usersToAdd.forEach { user ->
                                database.addUser(user)
                                retriever.retrieve(user)
                            }
                            call.respond(HttpStatusCode.Accepted)
                        }
                        post("/profiles") {
                            val newProfiles =
                                Gson().fromJson(call.receive<String>(), Array<String>::class.java).toList()
                            val currentProfiles = database.getProfiles()
                            val profilesToRemove = currentProfiles.minus(newProfiles)
                            val profilesToAdd = newProfiles.subtract(currentProfiles)
                            profilesToRemove.forEach { profile -> database.delProfile(profile) }
                            profilesToAdd.forEach { profile -> database.addProfile(profile) }
                            call.respond(HttpStatusCode.Accepted)
                        }
                        get("/backfill/{user}/{number}") {
                            val user = call.parameters["user"] ?: ""
                            val number = call.parameters["number"]?.toInt() ?: 0
                            GlobalScope.launch {
                                retriever.backfill(user, number)
                            }
                            call.respondRedirect("/admin", false)
                        }
                    }
                }
                get ("/rss/category/{profile}") {
                    val profile = call.parameters["profile"] ?: ""
                    if (profile.isEmpty()) {
                        call.respondRedirect("/", false)
                    } else {

                    }
                    val rss = emptyMap<String, String>().toMutableMap()
                    rss["title"] = profile
                    rss["description"] = "Items from Instagram: $profile"
                    rss["link"] = "$ig/$profile"
                    val feedItems = database.getIGPost(profile, 0, 100)
                    val location = call.request.local.scheme + "://" + call.request.local.host +
                        ":" + call.request.local.port + call.request.local.uri
                    call.respond(FreeMarkerContent(
                        "rss.ftl",
                        mapOf("feedItems" to feedItems, "rss" to rss, "url" to location),
                        null,
                        ContentType.Text.Xml)
                    )
                }
                get ("/template/image/{shortcode}/{ord?}") {
                    val shortcode = call.parameters["shortcode"] ?: ""
                    val ord = call.parameters["ord"]?.toInt() ?: 0
                    if (shortcode.isBlank()) {
                        call.respond(HttpStatusCode.NotFound, "Shortcode not found")
                    }
                    call.respond(getImageLightbox(database.getIGPost(shortcode, ord)))
                }
                get("/category/{profile}") {
                    val profile = call.parameters["profile"] ?: ""
                    call.respond(FreeMarkerContent("index.ftl",
                        mapOf("profiles" to database.getProfiles(),
                            "images" to database.getIGPost(profile=profile, start = 0, count=50),
                            "feedURL" to "/rss/category/${profile}",
                            "feedTitle" to "RSS - Category: ${profile}",
                            "username" to call.sessions.get<IGSession>()?.user
                        )))
                }
                get("/user/{user}") {
                    val user = call.parameters["user"] ?: ""
                    call.respond(FreeMarkerContent("index.ftl",
                        mapOf("profiles" to database.getProfiles(),
                            "images" to database.getUserIGPost(user=user, start = 0, count=50),
                            "feedURL" to "/rss/category/${user}",
                            "feedTitle" to "RSS - User: ${user}",
                            "username" to call.sessions.get<IGSession>()?.user
                        )))
                }
                get("/video/{shortcode}/{ord?}") {
                    val shortcode = call.parameters["shortcode"] ?: ""
                    val ord = call.parameters["ord"]?.toInt() ?: 0
                    val post = database.getIGPost(shortcode, ord)
                    call.respond(FreeMarkerContent("video.ftl",
                        mapOf("profiles" to database.getProfiles(),
                            "source" to post.url,
                            "username" to call.sessions.get<IGSession>()?.user
                        )))
                }
                get("/") {
                    val profiles = database.getProfiles()
                    if (profiles.isNotEmpty()) {
                        call.respondRedirect("/category/${database.getProfiles().first()}#", false)
                    } else {
                        call.respondRedirect("/admin", false)
                    }
                }
            }
        }
        server.start(wait = true)
    }

    fun getImageLightbox(image: IGPost): String {
        val cfg = Configuration(DEFAULT_INCOMPATIBLE_IMPROVEMENTS)
        cfg.setClassForTemplateLoading(this::class.java, "/templates/")
        cfg.defaultEncoding = "UTF-8"
        val template = cfg.getTemplate("image.ftl")
        val out = StringWriter()
        template.process(mapOf("image" to image), out)
        return out.buffer.toString()
    }
}

internal class ProfileUsers(val profiles: Map<String, List<String>>) {
    override fun toString(): String {
        return "[profiles=$profiles]"
    }
}
