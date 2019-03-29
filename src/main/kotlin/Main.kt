package com.greboid.scraper

import com.greboid.scraper.retrievers.IGRetriever
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.time.delay
import java.nio.file.Files
import java.nio.file.Paths
import java.time.Duration

@KtorExperimentalAPI
fun main() = runBlocking {
    val configFile = Paths.get("config/config.yml")
    if (!Files.exists(configFile)) {
        println("No config exists, creating default.")
        println("Please edit config/config.yml as needed.")
        createDefault(configFile)
        return@runBlocking
    }
    val config = getConfig(configFile) ?: run {
        println("Unable to load config.")
        return@runBlocking
    }
    val database = Database(config)
    val retriever = IGRetriever(database, config)
    database.connect()
    val web = launch {
        Web(database, config).start()
    }
    delay(Duration.ofSeconds(2))
    if (!web.isActive) {
        return@runBlocking
    }
    retriever.start()
    web.join()
}
