package com.greboid.scraper

import com.google.gson.Gson
import io.ktor.application.ApplicationCallPipeline
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.auth.*
import io.ktor.features.*
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.*
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.response.respondFile
import io.ktor.response.respondRedirect
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import io.ktor.routing.routing
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import io.ktor.sessions.*
import io.ktor.util.KtorExperimentalAPI
import io.ktor.util.hex
import java.io.File

data class IGSession(val user: String, val admin: Boolean, var previousPage: String = "/")

class Web(private val database: Database, private val config: Config) {
    @KtorExperimentalAPI
    suspend fun start() {
        val server = embeddedServer(CIO, port = 8080) {
            install(DefaultHeaders)
            install(PartialContent)
            install(Compression)
            install(ConditionalHeaders)
            install(CORS)
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
                    files("js")
                }
                static("/css") {
                    files("css")
                }
                static("/thumbs") {
                    files("thumbs")
                }
                get("/login") {
                    call.respondFile(File("html/login.html"))
                }
                authenticate ("auth") {
                    post("/login") {
                        val principal = call.authentication.principal<UserIdPrincipal>()
                        if (principal == null) {
                            call.respondFile(File("html/index.html"))
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
                        call.respondFile(File("html/admin.html"))
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
                get("/{...}") {
                    call.respondFile(File("html/index.html"))
                }
                get("/") {
                    val profiles = database.getProfiles()
                    if (profiles.isNotEmpty()) {
                        call.respondRedirect(database.getProfiles().first(), false)
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