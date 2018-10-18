package com.greboid.scraper

import org.sqlite.SQLiteConfig

fun main(args: Array<String>) {
    val config = getConfig("defaults.yml")
    val conn = SQLiteConfig().createConnection("jdbc:sqlite:" + config.db)
    conn.createStatement().executeUpdate(Schema.createAllTables)
    addProfiles(config.profiles, conn)
    pruneProfiles(config.profiles, conn)
}