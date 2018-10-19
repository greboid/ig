package com.greboid.scraper

import java.io.File

fun main(args: Array<String>) {
    val config = getConfig("example.yml") ?: run { println("Unable to load config. Exiting."); return }
    val database = Database(File(config.db))
    database.connect() ?: run { println("Unable to connect to the database. Exiting."); return }
    database.initTables()
    database.syncWithConfig(config)
}