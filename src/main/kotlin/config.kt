package com.greboid.scraper

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonParseException
import java.io.File


fun getConfig(configFile: File): Config? {
    return try {
        Gson().fromJson(configFile.reader(), Config::class.java)
    } catch (e: JsonParseException) {
        System.err.println("Parse error: ${e.localizedMessage}")
        null
    }
}

fun createDefault(configFile: File) {
    configFile.parentFile.mkdirs()
    val writer = configFile.writer()
    writer.write(GsonBuilder().setPrettyPrinting().create().toJson(Config(
            "ig",
            "database",
            "ig",
            "ig",
            "admin",
            "admin",
            "9e424e10e3dcd2f4fdd8d811c54aa36c",
            80
    )))
    writer.flush()
}

class Config(
        val db: String,
        val dbhost: String,
        val dbuser: String,
        val dbpassword: String,
        val adminUsername: String,
        val adminPassword: String,
        val sessionKey: String,
        val webPort: Int
)
