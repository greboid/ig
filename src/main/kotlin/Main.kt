package com.greboid.scraper

import kotlinx.coroutines.*
import kotlinx.coroutines.time.delay
import java.io.File
import java.time.Duration
import kotlin.concurrent.thread

fun main(args: Array<String>) = runBlocking {
    val config = getConfig(File("defaults.yml").reader()) ?: run {
        println("Unable to load config. Exiting.")
        return@runBlocking
    }
    val database = Database(config.database)
    database.connect()
    database.init()
    val instagram = Instagram()
    launch {
        repeat(5) {
            while (isActive) {
                //Retriever().start(database, instagram)
                delay(Duration.ofSeconds(30))
            }
        }
    }
    thread {
        Web(database).start()
    }
}