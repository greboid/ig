package com.greboid.scraper

import java.io.File
import java.lang.IllegalStateException
import java.sql.*
import javax.sql.DataSource

class Database(val url: String, val username: String = "", val password: String = "") {
    private var connection: Connection? = null

    fun connect(): Connection? {
        if (connection == null) {
            connection = try {
                DriverManager.getConnection(url, username, password)
            } catch (e: SQLException) {
                println(e)
                null
            }
        } else {
            throw IllegalStateException("Unable to connect again")
        }
        return connection
    }

    internal fun setConnection(conn: Connection) {
        connection = conn
    }

    fun initTables() {
        connection?.createStatement()?.executeUpdate(Schema.createAllTables)
                ?: throw IllegalStateException("Must be connected to initialise.")
    }
}

internal object Schema {
    private val createProfiles = """
        CREATE TABLE IF NOT EXISTS profiles (
        id INTEGER PRIMARY KEY,
        name TEXT UNIQUE
        )
    """.trimIndent().replace("[\n\r]".toRegex(), "")
    private val createProfileUsers = """
        CREATE TABLE IF NOT EXISTS profile_users (
        id INTEGER PRIMARY KEY,
        userID INT,
        profileID INT,
        UNIQUE(userID, profileID)
        )
    """.trimIndent().replace("[\n\r]".toRegex(), "")
    private val createUsers = """
        CREATE TABLE IF NOT EXISTS users (
        id INTEGER PRIMARY KEY,
        username TEXT UNIQUE,
        lastpoll INTEGER
        )
    """.trimIndent().replace("[\n\r]".toRegex(), "")
    private val createCategories = """
        CREATE TABLE IF NOT EXISTS medias (
        shortcode TEXT PRIMARY KEY,
        username TEXT,
        thumbnailURL TEXT,
        imageURL TEXT,
        caption TEXT,
        timestamp INTEGER
        )
    """.trimIndent().replace("[\n\r]".toRegex(), "")
    val createAllTables = "$createProfiles;\r\n$createProfileUsers;\r\n$createUsers;\r\n$createCategories;"
}