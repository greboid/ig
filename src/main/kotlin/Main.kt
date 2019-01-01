package com.greboid.scraper

import com.greboid.scraper.retrievers.IGRetriever
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.*
import kotlinx.coroutines.time.delay
import java.io.File
import java.time.Duration

@KtorExperimentalAPI
fun main(args: Array<String>) = runBlocking {
    val config = getConfig(File("config/defaults.yml").reader()) ?: run {
        println("Unable to load config. Exiting.")
        return@runBlocking
    }
    val database = Database(config.database)
    database.connect()
    database.init()
    val web = launch {
        Web(database, config).start()
    }
    delay(Duration.ofSeconds(2))
    if (!web.isActive) {
        return@runBlocking
    }
    val retriever = launch {
        while (isActive) {
            IGRetriever().start(database, config)
            delay(Duration.ofMinutes(15))
        }
    }
}