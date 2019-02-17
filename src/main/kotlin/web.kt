package com.greboid.scraper

import com.google.gson.Gson
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
                    val user = if (call.sessions.get<IGSession>()?.user == null) {
                        call.sessions.get<IGSession>()!!.user
                    } else {
                        call.respond(HttpStatusCode.NotAcceptable, "{}")
                        ""
                    }
                    val start: Int = call.request.queryParameters["start"]?.toInt() ?: 0
                    val count: Int = call.request.queryParameters["count"]?.toInt() ?: 5
                    val profile: String = call.request.queryParameters["profile"] ?: ""
                    if (profile.isNotEmpty()) {
                        call.respondText(Gson().toJson(database.getIGPost(user, profile, start, count)), ContentType.Application.Json)
                    } else {
                        call.respondText("")
                    }
                }
                get("/profiles") {
                    val user = if (call.sessions.get<IGSession>()?.user == null) {
                        call.sessions.get<IGSession>()!!.user
                    } else {
                        call.respond(HttpStatusCode.NotAcceptable, "{}")
                        ""
                    }
                    call.respondText(Gson().toJson(database.getCategories(user)), ContentType.Application.Json)
                }
                get("/users") {
                    val user = if (call.sessions.get<IGSession>()?.user == null) {
                        call.sessions.get<IGSession>()!!.user
                    } else {
                        call.respond(HttpStatusCode.NotAcceptable, "{}")
                        ""
                    }
                    call.respondText(Gson().toJson(database.getSources(user)), ContentType.Application.Json)
                }
                get("/ProfileUsers/{profile?}") {
                    val user = if (call.sessions.get<IGSession>()?.user == null) {
                        call.sessions.get<IGSession>()!!.user
                    } else {
                        call.respond(HttpStatusCode.NotAcceptable, "{}")
                        ""
                    }
                    val profile = call.parameters["profile"] ?: ""
                    if (profile.isEmpty()) {
                        call.respond(HttpStatusCode.NotFound, "Page not found.")
                    } else {
                        call.respondText(Gson().toJson(database.getProfileUsers(user, profile)), ContentType.Application.Json)
                    }
                }
                get("/userprofiles/{source?}") {
                    val user = if (call.sessions.get<IGSession>()?.user == null) {
                        call.sessions.get<IGSession>()!!.user
                    } else {
                        call.respond(HttpStatusCode.NotAcceptable, "{}")
                        ""
                    }
                    val source = call.parameters["user"] ?: ""
                    if (source.isEmpty()) {
                        call.respond(HttpStatusCode.NotFound, "Page not found.")
                    } else {
                        call.respondText(Gson().toJson(database.getSourcesCategories(user, source)), ContentType.Application.Json)
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
                        val user = if (call.sessions.get<IGSession>()?.user == null) {
                            call.sessions.get<IGSession>()!!.user
                        } else {
                            call.respond(HttpStatusCode.NotAcceptable, "{}")
                            ""
                        }
                        val profileUsers = Gson().fromJson(call.receive<String>(), ProfileUsers::class.java)
                        val currentProfiles = database.getSourcesCategories(user, profileUsers.selected)
                        val newProfiles = profileUsers.profiles
                        val profilesToRemove = currentProfiles.minus(newProfiles)
                        val profilesToAdd = newProfiles.subtract(currentProfiles)
                        profilesToRemove.forEach { profile -> database.delSourceCategory(user, profileUsers.selected, profile) }
                        profilesToAdd.forEach { profile -> database.addSourceCategory(user, profileUsers.selected, profile) }
                        call.respond(HttpStatusCode.OK, "{}")
                    }
                    post("/users") {
                        val user = if (call.sessions.get<IGSession>()?.user == null) {
                            call.sessions.get<IGSession>()!!.user
                        } else {
                            call.respond(HttpStatusCode.NotAcceptable, "{}")
                            ""
                        }
                        val newUsers = Gson().fromJson(call.receive<String>(), Array<String>::class.java).toList()
                        val currentUsers = database.getSources(user)
                        val usersToRemove = currentUsers.minus(newUsers)
                        val usersToAdd = newUsers.subtract(currentUsers)
                        usersToRemove.forEach { source -> database.delSource(user, source) }
                        //TODO: Need to deal with source types
                        usersToAdd.forEach { source -> database.addSource(user, source, "Instagram") }
                        call.respond(HttpStatusCode.OK, "{}")
                    }
                    post("/profiles") {
                        val user = if (call.sessions.get<IGSession>()?.user == null) {
                            call.sessions.get<IGSession>()!!.user
                        } else {
                            call.respond(HttpStatusCode.NotAcceptable, "{}")
                            ""
                        }
                        val newProfiles = Gson().fromJson(call.receive<String>(), Array<String>::class.java).toList()
                        val currentProfiles = database.getCategories(user)
                        val profilesToRemove = currentProfiles.minus(newProfiles)
                        val profilesToAdd = newProfiles.subtract(currentProfiles)
                        profilesToRemove.forEach { profile -> database.delCategory(user, profile) }
                        profilesToAdd.forEach { profile -> database.addCategory(user, profile) }
                        call.respond(HttpStatusCode.OK, "{}")
                    }
                }
                get("/{...}") {
                    call.respond(call.resolveResource("/html/index.html", "") ?: HttpStatusCode.InternalServerError)
                }
                get("/") {
                    val user = if (call.sessions.get<IGSession>()?.user == null) {
                        call.sessions.get<IGSession>()!!.user
                    } else {
                        call.respond(HttpStatusCode.NotAcceptable, "{}")
                        ""
                    }
                    val profiles = database.getCategories(user)
                    if (profiles.isNotEmpty()) {
                        call.respondRedirect(database.getCategories(user).first(), false)
                    } else {
                        call.respondRedirect("/admin", false)
                    }
                }
            }
        }
        server.start(wait = true)
    }
}

internal class ProfileUsers(val selected: String = "", val profiles: List<String> = emptyList()) {
    override fun toString(): String {
        return "[user=$selected, profiles=$profiles]"
    }
}
