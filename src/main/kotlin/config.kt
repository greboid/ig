package com.greboid.scraper

import com.google.gson.Gson
import com.google.gson.JsonIOException
import com.google.gson.JsonSyntaxException
import io.ktor.util.KtorExperimentalAPI
import io.ktor.util.hex
import java.io.Reader

fun getConfig(stream: Reader) : Config? {
    return try {
        Gson().fromJson(stream, Config::class.java)
    } catch (e: JsonSyntaxException) {
        null
    } catch (e: JsonIOException) {
        null
    }
}
class Config(
        val database: String,
        val adminUsername: String,
        val adminPassword: String,
        val sessionKey: String
)