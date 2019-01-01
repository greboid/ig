package com.greboid.scraper

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonParseException
import java.io.Reader
import java.io.Writer


fun getConfig(stream: Reader): Config? {
    return try {
        Gson().fromJson(stream, Config::class.java)
    } catch (e: JsonParseException) {
        System.err.println("Parse error: ${e.localizedMessage}")
        null
    }
}

fun createDefault(stream: Writer) {
    stream.write(GsonBuilder().setPrettyPrinting().create().toJson(Config(
            "jdbc:mysql://ig:ig@database/ig",
            "admin",
            "admin",
            "9e424e10e3dcd2f4fdd8d811c54aa36c",
            80
    )))
    stream.flush()
}

class Config(
        val database: String,
        val adminUsername: String,
        val adminPassword: String,
        val sessionKey: String,
        val webPort: Int
)
