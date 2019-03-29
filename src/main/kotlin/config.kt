package com.greboid.scraper

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonParseException
import java.nio.file.Files
import java.nio.file.Path


fun getConfig(configFile: Path): Config? {
    return try {
        Gson().fromJson(Files.newBufferedReader(configFile), Config::class.java)
    } catch (e: JsonParseException) {
        System.err.println("Parse error: ${e.localizedMessage}")
        null
    }
}

fun createDefault(configFile: Path) {
    Files.createDirectories(configFile.parent)
    val writer = Files.newBufferedWriter(configFile)
    writer.write(GsonBuilder().setPrettyPrinting().create().toJson(Config()))
    writer.flush()
}

class Config(
        val db: String = "ig",
        val dbhost: String = "database",
        val dbuser: String = "ig",
        val dbpassword: String = "ig",
        val dbport: Int = 3306,
        val adminUsername: String = "admin",
        val adminPassword: String = "admin",
        val sessionKey: String = "9e424e10e3dcd2f4fdd8d811c54aa36c",
        val webPort: Int = 80,
        val refreshDelay: Int = 15
)
