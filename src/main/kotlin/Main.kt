package com.greboid.scraper

import com.greboid.scraper.retrievers.IGRetriever
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.*
import kotlinx.coroutines.time.delay
import java.io.File
import java.time.Duration

@KtorExperimentalAPI
fun main(args: Array<String>) = runBlocking {
    val configFile = File("config/config.yml")
    if (!configFile.exists()) {
        println("No config exists, creating default.")
        println("Please edit config/config.yml as needed.")
        createDefault(configFile.writer())
        return@runBlocking
    }
    val config = getConfig(configFile.reader()) ?: run {
        println("Unable to load config.")
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
    launch {
        while (isActive) {
            IGRetriever().start(database, config)
            delay(Duration.ofMinutes(15))
        }
    }
}