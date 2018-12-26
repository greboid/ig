package com.greboid.scraper

import java.lang.IllegalStateException
import java.sql.*

class Database(val url: String, val username: String = "", val password: String = "") {
    private lateinit var connection: Connection

    fun connect(): Connection {
        connection = try {
            DriverManager.getConnection(url, username, password)
        } catch (e: SQLException) {
            throw IllegalStateException("Unable to connect again")
        }
        return connection
    }

    internal fun setConnection(conn: Connection) {
        connection = conn
    }

    fun init() {
        connection.createStatement()?.executeUpdate(Schema.createAllTables)
                ?: throw IllegalStateException("Must be connected to initialise.")
        connection.createStatement()?.execute("PRAGMA busy_timeout=30000")
    }

    fun addProfile(name: String) =
            connection.setAndUpdate(Schema.addProfile, mapOf(Pair(1, name))) == 1

    fun delProfile(name: String) =
            connection.setAndUpdate(Schema.delProfile, mapOf(Pair(1, name))) == 1

    fun getProfiles() =
            connection.getAllString(Schema.getProfiles, "name")

    fun addUser(name: String) =
            connection.setAndUpdate(Schema.addUser, mapOf(Pair(1, name))) == 1

    fun delUser(name: String) =
            connection.setAndUpdate(Schema.delUser, mapOf(Pair(1, name))) == 1

    fun getUserID(name: String): Int? {
        val statement = connection.prepareStatement(Schema.getUserID) ?: return null
        statement.setString(1, name)
        val result = statement.executeQuery()
        return result.getInt(1)
    }

    fun getUsers(): List<String> =
            connection.getAllString(Schema.getUsers, "username")

    fun addMedia(shortcode: String, userID: Int, thumbnailURL: String,
                 imageURL: String, caption: String, timestamp: Int) =
            connection.setAndUpdate(Schema.addMedia, mapOf(
                    Pair(1, shortcode),
                    Pair(2, userID),
                    Pair(3, thumbnailURL),
                    Pair(4, imageURL),
                    Pair(5, caption),
                    Pair(6, timestamp)
            )) == 1

    fun delMedia(shortcode: String) =
            connection.setAndUpdate(Schema.deleteMedia, mapOf(Pair(1, shortcode))) == 1

    fun getMedia(profile: String, start: Int = 0, end: Int = 5): List<MediaObject> {
        val results = connection.setAndQuery(Schema.selectMedias,
                mapOf(Pair(1, profile), Pair(2, end), Pair(3, start))) ?: return emptyList()
        return sequence {
            while (results.next()) {
                yield(MediaObject(results.getString(1), results.getInt(2),
                        results.getString(3), results.getString(4),
                        results.getString(5), results.getInt(6)))
            }
        }.toList()
    }

    internal object Schema {
        internal val getUserID = """
            select id from users where username=?
        """.trimIndent().replace("[\n\r]".toRegex(), "")
        internal val addProfile = """
            insert or ignore into profiles(name) values ?
        """.trimIndent().replace("[\n\r]".toRegex(), "")
        internal val delProfile = """
            delete from profiles where name=?
        """.trimIndent().replace("[\n\r]".toRegex(), "")
        internal val getProfiles = """
            select name from profiles
        """.trimIndent().replace("[\n\r]".toRegex(), "")
        internal val addUser = """
            insert or ignore into users(username) values ?
        """.trimIndent().replace("[\n\r]".toRegex(), "")
        internal val delUser = """
            delete from users where name=?
        """.trimIndent().replace("[\n\r]".toRegex(), "")
        internal val getUsers = """
            select username from users
        """.trimIndent().replace("[\n\r]".toRegex(), "")
        internal val addMedia = """
            insert or replace into medias
            (shortcode,userID,thumbnailURL,imageURL,caption,timestamp)
            values (?,?,?,?,?,?)
        """.trimIndent().replace("[\n\r]".toRegex(), "")
        internal val deleteMedia = """
            delete from medias where shortcode=?
        """.trimIndent().replace("[\n\r]".toRegex(), "")
        internal val selectMedias = """
            SELECT
            shortcode, medias.username as source, thumbnailURL as thumb, imageURL as url, caption as caption, timestamp
            FROM medias
            LEFT JOIN users on users.username=medias.username
            LEFT JOIN profile_users on profile_users.userid=users.id
            LEFT JOIN profiles on profile_users.profileid=profiles.id
            WHERE profiles.name=?
            ORDER BY timestamp DESC
            LIMIT ?
            OFFSET ?
        """.trimIndent().replace("[\n\r]".toRegex(), "")
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
        private val createMedias = """
            CREATE TABLE IF NOT EXISTS medias (
            id INTEGER PRIMARY KEY,
            shortcode TEXT,
            userID INTEGER,
            thumbnailURL TEXT,
            imageURL TEXT,
            caption TEXT,
            timestamp INTEGER
            )
        """.trimIndent().replace("[\n\r]".toRegex(), "")
        val createAllTables = "$createProfiles;\r\n$createProfileUsers;\r\n$createUsers;\r\n$createMedias;"
    }
}

data class MediaObject(val shortcode: String, val userID: Int,
                       val thumbnailURL: String, val imageURL: String,
                       val caption: String, val timestamp: Int)

fun ResultSet.getAllString(fieldName: String) = sequence {
    use {
        while (next()) {
            yield(getString(fieldName))
        }
    }
}.toList()

fun Connection.getAllString(sql: String, fieldName: String) =
        prepareStatement(sql)?.executeQuery()?.getAllString(fieldName) ?: emptyList()

fun PreparedStatement.setAndUpdate(values: Map<Int, Any>) = use {
    for ((index, value) in values) {
        when (value) {
            is String -> setString(index, value)
            is Int -> setInt(index, value)
            else -> setObject(index, value)
        }
    }
    executeUpdate()
}

fun Connection.setAndUpdate(sql: String, values: Map<Int, Any>) =
        prepareStatement(sql)?.setAndUpdate(values)


fun PreparedStatement.setAndQuery(values: Map<Int, Any>): ResultSet = use {
    for ((index, value) in values) {
        when (value) {
            is String -> setString(index, value)
            is Int -> setInt(index, value)
            else -> setObject(index, value)
        }
    }
    executeQuery()
}

fun Connection.setAndQuery(sql: String, values: Map<Int, Any>) =
        prepareStatement(sql)?.setAndQuery(values)