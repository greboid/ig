package com.greboid.scraper

import java.io.File

fun main(args: Array<String>) {
    val config = getConfig(File("defaults.yml").reader()) ?: run { println("Unable to load config. Exiting."); return }
    val database = Database(config.database ?: run { println("Unable to load database. Exiting."); return })
    database.connect() ?: run { println("Unable to connect to the database. Exiting."); return }
    database.initTables()
//    database.syncWithConfig(config)
    //getProfile("hrvy")
    //getPost("BoxdzSWHe9P")
    //getPost("BoxdzSWHe9P")
}