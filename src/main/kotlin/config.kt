package com.greboid.scraper

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import java.io.Reader

fun getConfig(stream: Reader) : Config? {
    return try {
        Gson().fromJson(stream, Config::class.java)
    } catch (e: JsonSyntaxException) {
        null
    }
}
class Config(val database: String? = null)