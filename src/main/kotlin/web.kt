package com.greboid.scraper

import com.google.gson.Gson
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.application.log
import io.ktor.features.Compression
import io.ktor.features.DefaultHeaders
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.content.*
import io.ktor.response.respondFile
import io.ktor.response.respondRedirect
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import java.io.File

class Web(val database: Database) {
    fun start() {
        val server = embeddedServer(Netty, port = 8080) {
            install(DefaultHeaders)
            install(Compression)
            routing {
                static("/js") {
                    files("js")
                }
                static("/css") {
                    files("css")
                }
                static("/feed") {

                }
                get("/profiles") {
                    call.respondText(Gson().toJson(database.getProfiles()), ContentType.Application.Json)
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