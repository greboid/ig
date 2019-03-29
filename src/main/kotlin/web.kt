package com.greboid.scraper

import com.google.gson.Gson
import freemarker.cache.ClassTemplateLoader
import freemarker.template.Configuration
import freemarker.template.Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS
import io.ktor.application.ApplicationCallPipeline
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.auth.FormAuthChallenge
import io.ktor.auth.UserIdPrincipal
import io.ktor.auth.authenticate
import io.ktor.auth.authentication
import io.ktor.auth.form
import io.ktor.features.CORS
import io.ktor.features.Compression
import io.ktor.features.ConditionalHeaders
import io.ktor.features.DefaultHeaders
import io.ktor.features.PartialContent
import io.ktor.features.StatusPages
import io.ktor.features.XForwardedHeaderSupport
import io.ktor.freemarker.FreeMarker
import io.ktor.freemarker.FreeMarkerContent
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
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
import java.io.StringWriter

data class IGSession(val user: String, val admin: Boolean, var previousPage: String = "/")

class Web(private val database: Database, private val config: Config) {
    @KtorExperimentalAPI
    suspend fun start() {
        val server = embeddedServer(CIO, port = config.webPort) {
            install(DefaultHeaders)
            install(PartialContent)
            install(Compression)
            install(ConditionalHeaders)
            install(CORS)
            install(XForwardedHeaderSupport)
            install(Sessions) {
                cookie<IGSession>("session", SessionStorageMemory()) {
                    transform(SessionTransportTransformerMessageAuthentication(hex(config.sessionKey), "HmacSHA256"))
                }
            }
            install(StatusPages) {
                exception<Throwable> { cause ->
                    call.respond(HttpStatusCode.InternalServerError, "Internal Server Error")
                    cause.printStackTrace()
                }
            }
            install(FreeMarker) {
                templateLoader = ClassTemplateLoader(this::class.java.classLoader, "templates")
            }
            install(Authentication) {
                form(name = "auth") {
                    userParamName = "username"
                    passwordParamName = "password"
                    challenge = FormAuthChallenge.Redirect { "/login" }
                    validate { credentials ->
                        if (credentials.name == config.adminUsername && credentials.password == config.adminPassword) {
                            UserIdPrincipal(credentials.name)
                        } else {
                            null
                        }
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
                get("/login") {
                    call.respond(call.resolveResource("/html/login.html", "") ?: HttpStatusCode.InternalServerError)
                }
                authenticate("auth") {
                    post("/login") {
                        val principal = call.authentication.principal<UserIdPrincipal>()
                        if (principal == null) {
                            call.respond(call.resolveResource("/html/index.html", "")
                                    ?: HttpStatusCode.InternalServerError)
                        } else {
                            call.sessions.set("session", IGSession(principal.name, true))
                            call.respondRedirect("/admin", false)
                        }
                    }
                }
                get("/logout") {
                    call.sessions.clear<IGSession>()
                    call.respondRedirect("/", false)
                }
                get("/igposts") {
                    val start: Int = call.request.queryParameters["start"]?.toInt() ?: 0
                    val count: Int = call.request.queryParameters["count"]?.toInt() ?: 5
                    val profile: String = call.request.queryParameters["profile"] ?: ""
                    if (profile.isNotEmpty()) {
                        call.respondText(Gson().toJson(database.getIGPost(profile, start, count)), ContentType.Application.Json)
                    } else {
                        call.respondText("")
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
                    intercept(ApplicationCallPipeline.Features) {
                        call.sessions.get<IGSession>()?.user ?: call.respondRedirect("/login", false)
                    }
                    get("/") {
                        call.respond(call.resolveResource("/html/admin.html", "") ?: HttpStatusCode.InternalServerError)
                    }
                    post("/") {
                        call.respondRedirect("/admin")
                    }
                    post("/ProfileUsers") {
                        val profileUsers = Gson().fromJson(call.receive<String>(), ProfileUsers::class.java)
                        val currentProfiles = database.getUserProfiles(profileUsers.selected)
                        val newProfiles = profileUsers.profiles
                        val profilesToRemove = currentProfiles.minus(newProfiles)
                        val profilesToAdd = newProfiles.subtract(currentProfiles)
                        profilesToRemove.forEach { profile -> database.delUserProfile(profileUsers.selected, profile) }
                        profilesToAdd.forEach { profile -> database.addUserProfile(profileUsers.selected, profile) }
                        call.respond(HttpStatusCode.OK, "{}")
                    }
                    post("/users") {
                        val newUsers = Gson().fromJson(call.receive<String>(), Array<String>::class.java).toList()
                        val currentUsers = database.getUsers()
                        val usersToRemove = currentUsers.minus(newUsers)
                        val usersToAdd = newUsers.subtract(currentUsers)
                        usersToRemove.forEach { user -> database.delUser(user) }
                        usersToAdd.forEach { user -> database.addUser(user) }
                        call.respond(HttpStatusCode.OK, "{}")
                    }
                    post("/profiles") {
                        val newProfiles = Gson().fromJson(call.receive<String>(), Array<String>::class.java).toList()
                        val currentProfiles = database.getProfiles()
                        val profilesToRemove = currentProfiles.minus(newProfiles)
                        val profilesToAdd = newProfiles.subtract(currentProfiles)
                        profilesToRemove.forEach { profile -> database.delProfile(profile) }
                        profilesToAdd.forEach { profile -> database.addProfile(profile) }
                        call.respond(HttpStatusCode.OK, "{}")
                    }
                }
                get ("/rss/{profile}") {
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
                    val location = call.request.local.scheme + "://" + call.request.local.host + ":" + call.request.local.port + call.request.local.uri
                    call.respond(FreeMarkerContent("rss.ftl", mapOf("feedItems" to feedItems, "rss" to rss, "url" to location)))
                }
                get ("/template/image/{shortcode}/{ord?}") {
                    val shortcode = call.parameters["shortcode"] ?: ""
                    val ord = call.parameters["ord"]?.toInt() ?: 0
                    if (shortcode.isBlank()) {
                        call.respond(HttpStatusCode.NotFound, "Shortcode not found")
                    }
                    call.respond(getImageLightbox(database.getIGPost(shortcode, ord)))
                }
                get("/{...}") {
                    call.respond(FreeMarkerContent("index.ftl", mapOf("profiles" to database.getProfiles())))
                }
                get("/") {
                    val profiles = database.getProfiles()
                    if (profiles.isNotEmpty()) {
                        call.respondRedirect(database.getProfiles().first()+'#', false)
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

internal class ProfileUsers(val selected: String = "", val profiles: List<String> = emptyList()) {
    override fun toString(): String {
        return "[user=$selected, profiles=$profiles]"
    }
}
