package com.greboid.scraper

import com.google.gson.Gson
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.auth.*
import io.ktor.features.Compression
import io.ktor.features.DefaultHeaders
import io.ktor.features.StatusPages
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.*
import io.ktor.response.respond
import io.ktor.response.respondFile
import io.ktor.response.respondRedirect
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import java.io.File

class Web(val database: Database, val config: Config) {
    fun start() {
        val server = embeddedServer(Netty, port = 8080) {
            install(DefaultHeaders)
            install(Compression)
            install(StatusPages) {
                exception<Throwable> { cause ->
                    call.respond(HttpStatusCode.InternalServerError, "Internal Server Error")
                    println(cause)
                }
            }
            install(Authentication) {
                basic(name = "admin") {
                    realm = "IG Admin"
                    validate { credentials ->
                        if (credentials.name == config.adminUsername && credentials.password == config.adminPassword) {
                            UserIdPrincipal(credentials.name)
                        } else {
                            null
                        }}
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
                get("/feed") {
                    val start: Int = call.request.queryParameters["start"]?.toInt() ?: 0
                    val count: Int = call.request.queryParameters["count"]?.toInt() ?: 5
                    val profile: String = call.request.queryParameters["profile"] ?: ""
                    if (profile.isNotEmpty()) {
                        call.respondText(Gson().toJson(database.getMedia(profile, start, count)), ContentType.Application.Json)
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
                get("/profileusers/{profile?}") {
                    val profile = call.parameters.get("profile") ?: ""
                    if (profile.isEmpty()) {
                        call.respond(HttpStatusCode.NotFound, "Page not found.")
                    } else {
                        call.respondText(Gson().toJson(database.getProfileUsers(profile)), ContentType.Application.Json)
                    }
                }
                get("/userprofiles/{user?}") {
                    val user = call.parameters.get("user") ?: ""
                    if (user.isEmpty()) {
                        call.respond(HttpStatusCode.NotFound, "Page not found.")
                    } else {
                        call.respondText(Gson().toJson(database.getUserProfiles(user)), ContentType.Application.Json)
                    }
                }
                authenticate("admin") {
                    get("/admin") {
                        call.respondFile(File("html/admin.html"))
                    }
                    post("/admin") {
                        call.respondRedirect("/admin")
                    }
                }
                get("/{...}") {
                    call.respondFile(File("html/index.html"))
                }
                get("/") {
                    call.respondRedirect(database.getProfiles().first(), false)
                }
            }
        }
        server.start(wait = true)
    }
}