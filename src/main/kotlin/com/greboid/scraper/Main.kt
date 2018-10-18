package com.greboid.scraper

import org.sqlite.SQLiteConfig

fun main(args: Array<String>) {
    val config = getConfig("defaults.yml")
    val conn = getDatabase(config.db)
    addProfiles(config.profiles, conn)
    pruneProfiles(config.profiles, conn)
}