package com.greboid.scraper

import java.lang.IllegalStateException
import java.sql.*

class Database(private val url: String, private val username: String = "", private val password: String = "") {
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

    fun getProfileUsers(profile: String): List<String> {
        val s = connection.prepareStatement(Schema.getProfileUsers)
        s.setString(1, profile)
        val results: ResultSet = s.executeQuery()
        val returnValue = sequence {
            while (results.next()) {
                yield(results.getString(1))
            }
        }.toList()
        results.close()
        s.close()
        return returnValue
    }

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

    fun addMedia(shortcode: String, ord: Int, userID: Int, thumbnailURL: String,
                 imageURL: String, caption: String, timestamp: Int) =
            connection.setAndUpdate(Schema.addMedia, mapOf(
                    Pair(1, shortcode),
                    Pair(2, ord),
                    Pair(3, userID),
                    Pair(4, thumbnailURL),
                    Pair(5, imageURL),
                    Pair(6, caption),
                    Pair(7, timestamp)
            )) == 1

    fun delMedia(shortcode: String) =
            connection.setAndUpdate(Schema.deleteMedia, mapOf(Pair(1, shortcode))) == 1

    fun getMedia(profile: String, start: Int = 0, count: Int = 5): List<MediaObject> {
        val s = connection.prepareStatement(Schema.selectMedias)
        s.setString(1, profile)
        s.setInt(2, count)
        s.setInt(3, start)
        val results: ResultSet = s.executeQuery()
        val returnValue = sequence {
            while (results.next()) {
                yield(MediaObject(results.getString(1), results.getString(2),
                        results.getString(3), results.getString(4),
                        results.getString(5), results.getInt(6)))
            }
        }.toList()
        results.close()
        s.close()
        return returnValue
    }

    internal object Schema {
        internal val getProfileUsers = """
            select users.username
            from profile_users
            left join profiles on profile_users.profileID=profiles.id
            left join users on profile_users.userID=users.id
            where profiles.name=?
        """.trimIndent()
        internal val getUserID = """
            select id from users where username=?
        """.trimIndent()
        internal val addProfile = """
            insert or ignore into profiles(name) values ?
        """.trimIndent()
        internal val delProfile = """
            delete from profiles where name=?
        """.trimIndent()
        internal val getProfiles = """
            select name from profiles
        """.trimIndent()
        internal val addUser = """
            insert or ignore into users(username) values ?
        """.trimIndent()
        internal val delUser = """
            delete from users where name=?
        """.trimIndent()
        internal val getUsers = """
            select username from users
        """.trimIndent()
        internal val addMedia = """
            insert or replace into medias
            (shortcode,ord,userID,thumbnailURL,imageURL,caption,timestamp)
            values (?,?,?,?,?,?,?)
        """.trimIndent()
        internal val deleteMedia = """
            delete from medias where shortcode=?
        """.trimIndent()
        internal val selectMedias = """
            SELECT shortcode, users.username, thumbnailURL, imageURL, caption, timestamp
            FROM medias
            LEFT JOIN users on users.id=medias.userID
            LEFT JOIN profile_users on profile_users.userid=users.id
            LEFT JOIN profiles on profile_users.profileid=profiles.id
            WHERE profiles.name=?
            ORDER BY timestamp DESC
            LIMIT ?
            OFFSET ?
        """.trimIndent()
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
            id INTEGER,
            shortcode TEXT,
            ord INTEGER,
            userID INTEGER,
            thumbnailURL TEXT,
            imageURL TEXT,
            caption TEXT,
            timestamp INTEGER,
            PRIMARY KEY (shortcode, ord)
            )
        """.trimIndent().replace("[\n\r]".toRegex(), "")
        val createAllTables = "$createProfiles;\r\n$createProfileUsers;\r\n$createUsers;\r\n$createMedias;"
    }
}

data class MediaObject(val shortcode: String, val source: String,
                       val thumb: String, val url: String,
                       val caption: String, val timestamp: Int)

fun ResultSet.getAllString(fieldName: String) = sequence {
    use {
        while (next()) {
            yield(getString(fieldName))
        }
    }
}.toList().filterNotNull()

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