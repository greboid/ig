package com.greboid.scraper

fun main(args: Array<String>) {
    val config = getConfig("defaults.yml")
    val conn = getDatabase(config.db)
    addProfiles(config.profiles, conn)
    pruneProfiles(config.profiles, conn)
}