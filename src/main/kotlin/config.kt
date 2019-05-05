package com.greboid.scraper

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonParseException
import mu.KotlinLogging
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

val logger = KotlinLogging.logger {}

fun migrateConfig(configFile: Path) {
    val yamlPath = Paths.get(configFile.toAbsolutePath().toString().substringBeforeLast(".").plus(".yml"))
    if (Files.exists(yamlPath)) {
        logger.info("Migrating config to json")
        Files.move(yamlPath, configFile)
    }
}

fun getConfig(configFile: Path): Config? {
    return try {
        logger.trace("Starting to parse config file")
        val config = Gson().fromJson(Files.newBufferedReader(configFile), Config::class.java)
        val writer = Files.newBufferedWriter(configFile)
        writer.write(GsonBuilder().setPrettyPrinting().create().toJson(config))
        writer.flush()
        return config
    } catch (e: JsonParseException) {
        logger.error("Error parsing config", e)
        null
    }
}

fun createDefault(configFile: Path) {
    logger.trace("Creating config directories")
    Files.createDirectories(configFile.parent)
    val writer = Files.newBufferedWriter(configFile)
    logger.trace("Writing to config file")
    val config = Config()
    writer.write(GsonBuilder().setPrettyPrinting().create().toJson(config))
    writer.flush()
    logger.info("First run credentials:")
    logger.info("\tUsername: ${config.adminUsername}")
    logger.info("\tPassword: ${config.adminPassword}")
}

class Config(
    val db: String = "ig",
    val dbhost: String = "database",
    val dbuser: String = "ig",
    val dbpassword: String = "ig",
    val dbport: Int = 3306,
    val adminUsername: String = "admin",
    val adminPassword: String = randomString(('0'..'9') + ('a'..'f'), 16),
    val sessionKey: String = randomString(('0'..'9') + ('a'..'f'), 32),
    val webPort: Int = 80,
    val refreshDelay: Int = 15,
    val igLogin: Boolean = false,
    val igUsername: String = "",
    val igPassword: String = "",
    val testMode: Boolean = false
)

fun randomString(charPool: List<Char>, length: Int) = CharArray(length) { charPool.random() }.joinToString("")
