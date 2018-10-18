package com.greboid.scraper

import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet

fun addProfiles(profiles: Map<String, List<String>>, conn: Connection) {
    profiles.forEach { profileName, usernames ->

        conn.prepareStatement("insert or ignore into profiles(name) values (?)").use { ps ->
            ps.setString(1, profileName)
            ps.executeUpdate()
        }
        conn.prepareStatement("insert or ignore into profiles(name) values (?)")
                .setStringAndExecute(1, profileName)

        usernames.forEach { username ->
            conn.prepareStatement("insert or ignore into users(username) values (?)")
                    .setStringAndExecute(1, username)

            val profileid = conn.prepareStatement("select id from profiles where name=?").use { ps ->
                ps.setString(1, profileName)
                ps.executeQuery().getInt("id")
            }

            val userid = conn.prepareStatement("select id from users where username=?").use { ps ->
                ps.setString(1, username)
                ps.executeQuery().getInt("id")
            }

            conn.prepareStatement("insert or ignore into profile_users (profileid, userid) values (?, ?)").use { ps ->
                ps.setInt(1, profileid)
                ps.setInt(2, userid)
                ps.executeUpdate()
            }
        }
    }
    conn.commit()
}

fun pruneProfiles(profiles: Map<String, List<String>>, conn: Connection) {
    getDatabaseProfiles(conn).filter { s -> !profiles.keys.contains(s) }.forEach {
        conn.prepareStatement("delete from profiles where name=?")
                .setStringAndExecute(1, it)
    }
    getDatabaseUsers(conn).filter {s -> !profiles.values.flatten().contains(s) }.forEach {
        conn.prepareStatement("delete from users where username=?")
                .setStringAndExecute(1, it)
    }
    conn.prepareStatement("delete from profile_users where profileid not in (select profileid from profiles)")
            .executeAndClose()
    conn.prepareStatement("delete from medias where username not in (select username from users)")
            .executeAndClose()
}

fun ResultSet.getAllString(fieldName: String) = sequence {
    use {
        while (next()) {
            yield(getString(fieldName))
        }
    }
}.toList()

fun PreparedStatement.setStringAndExecute(index: Int, value: String) = use {
    setString(index, value)
    executeUpdate()
}

fun PreparedStatement.executeAndClose() = use {
    executeUpdate()
}

fun getDatabaseProfiles(conn: Connection): List<String> {
    return conn.prepareStatement("select name from profiles").use { ps ->
        ps.executeQuery().getAllString("name")
    }
}

fun getDatabaseUsers(conn: Connection): List<String> {
    return conn.prepareStatement("select username from users").use { ps ->
        ps.executeQuery().getAllString("username")
    }
}

object Schema {
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
    val createAllTables = "PRAGMA foreign_keys=OFF;\r\nBEGIN TRANSACTION;\r\n$createProfiles;\r\n$createProfileUsers;\r\n$createUsers;\r\n$createCategories;\r\nCOMMIT;"
}