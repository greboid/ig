package com.greboid.scraper

import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty


class Web {
    fun start() {
        val server = embeddedServer(Netty, port = 8080) {
            routing {
                get("/") {
                    call.respondText("'sup", ContentType.Text.Plain)
                }
            }
        }
        server.start(wait = true)
    }
}