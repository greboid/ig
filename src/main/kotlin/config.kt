package com.greboid.scraper

import com.google.gson.*
import java.io.Reader
import java.io.Writer


fun getConfig(stream: Reader) : Config? {
    return try {
        Gson().fromJson(stream, Config::class.java)
    } catch (e: JsonParseException) {
        System.err.println("Parse error: ${e.localizedMessage}")
        null
    }
}

fun createDefault(stream: Writer) {
    stream.write(GsonBuilder().setPrettyPrinting().create().toJson(Config(
            "jdbc:sqlite:database/database.sqlite",
            "admin",
            "admin",
            "9e424e10e3dcd2f4fdd8d811c54aa36c"
    )))
    stream.flush()
}
class Config(
        val database: String,
        val adminUsername: String,
        val adminPassword: String,
        val sessionKey: String
)