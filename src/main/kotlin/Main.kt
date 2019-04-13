package com.greboid.scraper

import com.greboid.scraper.retrievers.IGRetriever
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.time.delay
import mu.KotlinLogging
import java.nio.file.Files
import java.nio.file.Paths
import java.time.Duration
import java.util.logging.LogManager

@KtorExperimentalAPI
fun main() {
    val logProperties = Paths.get("config/log.properties")
    LogManager.getLogManager().readConfiguration(
        if (Files.exists(logProperties.toAbsolutePath())) {
            Files.newInputStream(logProperties)
        } else {
            object{}::class.java.getResourceAsStream("/logs.properties")
        }
    )
    val logger = KotlinLogging.logger {}
    runBlocking {
        logger.info("Loading config")
        val configFile = Paths.get("config/config.json")
        migrateConfig(configFile)
        if (!Files.exists(configFile)) {
            logger.warn("No config exists, creating default")
            createDefault(configFile)
        }
        val config = getConfig(configFile) ?: run {
            logger.error("Unable to load config")
            return@runBlocking
        }
        val database = Database(config)
        val retriever = IGRetriever(database, config)
        logger.info("Connecting to database")
        database.connect()
        val web = launch {
            logger.info("Launching web server")
            Web(database, config).start()
        }
        delay(Duration.ofSeconds(2))
        if (!web.isActive) {
            logger.error("Web server didn't load exiting")
            return@runBlocking
        }
        retriever.start()
        web.join()
    }
}
