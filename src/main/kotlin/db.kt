package com.greboid.scraper

import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException

class Database(private val url: String, private val username: String = "", private val password: String = "") {
    private lateinit var connection: Connection

    fun connect(): Connection {
        connection = try {
            DriverManager.getConnection(url, username, password)
        } catch (e: SQLException) {
            throw IllegalStateException("Unable to connect: ${e.localizedMessage}")
        }
        return connection
    }

    internal fun setConnection(conn: Connection) {
        connection = conn
    }

    fun init() {
        Schema.createAllTables.forEach {
            connection.createStatement()?.executeUpdate(it)
                    ?: throw IllegalStateException("Must be connected to initialise.")
        }
        if (url.startsWith("jdbc:sqlite")) {
            connection.createStatement()?.execute("PRAGMA busy_timeout=30000")
        }
    }

    fun addProfile(name: String) =
            connection.setAndUpdate(Schema.addProfile, listOf(name)) == 1

    fun delProfile(name: String) {
        val profileID = getProfileID(name) ?: return
        connection.setAndUpdate(Schema.deleteProfileFromProfileUsers, listOf(profileID))
        connection.setAndUpdate(Schema.delProfile, listOf(name))
    }

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

    fun getUserProfiles(user: String): List<String> {
        val s = connection.prepareStatement(Schema.getUserProfiles)
        s.setString(1, user)
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

    fun addUserProfile(user: String, profile: String): Boolean {
        val userID = getUserID(user) ?: return false
        val profileID = getProfileID(profile) ?: return false
        return connection.setAndUpdate(Schema.addUserToProfile, listOf(userID, profileID)) == 1
    }

    fun delUserProfile(user: String, profile: String): Boolean {
        val userID = getUserID(user) ?: return false
        val profileID = getProfileID(profile) ?: return false
        return connection.setAndUpdate(Schema.deleteProfileFromUser, listOf(userID, profileID)) == 1
    }

    fun addUser(name: String) =
            connection.setAndUpdate(Schema.addUser, listOf(name)) == 1

    fun delUser(name: String): Boolean {
        val userID = getUserID(name) ?: return false
        connection.setAndUpdate(Schema.deleteUserFromProfileUsers, listOf(userID))
        return connection.setAndUpdate(Schema.delUser, listOf(name)) == 1
    }

    fun getUserID(name: String): Int? {
        val statement = connection.prepareStatement(Schema.getUserID) ?: return null
        statement.setString(1, name)
        val result = statement.executeQuery()
        val returnValue = result.getInt(1)
        result.close()
        statement.close()
        return returnValue
    }

    fun getProfileID(name: String): Int? {
        val statement = connection.prepareStatement(Schema.getProfileID) ?: return null
        statement.setString(1, name)
        val result = statement.executeQuery()
        val returnValue = result.getInt(1)
        result.close()
        statement.close()
        return returnValue
    }

    fun getUsers(): List<String> =
            connection.getAllString(Schema.getUsers, "username")

    fun addIGPost(shortcode: String, ord: Int, userID: Int, thumbnailURL: String,
                  imageURL: String, caption: String, timestamp: Int) =
            connection.setAndUpdate(Schema.addIGPost,
                    listOf(shortcode, ord, userID, thumbnailURL, imageURL, caption, timestamp)) == 1

    fun getIGPost(profile: String, start: Int = 0, count: Int = 5): List<IGPost> {
        val s = connection.prepareStatement(Schema.selectIGPosts)
        s.setString(1, profile)
        s.setInt(2, count)
        s.setInt(3, start)
        val results: ResultSet = s.executeQuery()
        val returnValue = sequence {
            while (results.next()) {
                yield(IGPost(results.getString(1), results.getString(2),
                        results.getString(3), results.getString(4),
                        results.getString(5), results.getInt(6)))
            }
        }.toList()
        results.close()
        s.close()
        return returnValue
    }

    internal object Schema {
        internal val deleteProfileFromUser = """
            delete from profile_users where userID=? AND profileID=?
        """.trimIndent()
        internal val addUserToProfile = """
            insert or ignore into profile_users (userID,profileID) values (?,?)
        """.trimIndent()
        internal val deleteProfileFromProfileUsers = """
            delete from profile_users where profileID=?
        """.trimIndent()
        internal val getProfileID = """
            select id from profiles where name=?
        """.trimIndent()
        internal val deleteUserFromProfileUsers = """
            delete from profile_users where userID=?
        """.trimIndent()
        internal val getUserProfiles = """
            select profiles.name
            from profile_users
            left join profiles on profile_users.profileID=profiles.id
            left join users on profile_users.userID=users.id
            where users.username=?
        """.trimIndent()
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
            insert or ignore into profiles(name) values (?)
        """.trimIndent()
        internal val delProfile = """
            delete from profiles where name=?
        """.trimIndent()
        internal val getProfiles = """
            select name from profiles
        """.trimIndent()
        internal val addUser = """
            insert or ignore into users(username) values (?)
        """.trimIndent()
        internal val delUser = """
            delete from users where username=?
        """.trimIndent()
        internal val getUsers = """
            select username from users
        """.trimIndent()
        internal val addIGPost = """
            insert or replace into igposts
            (shortcode,ord,userID,thumbnailURL,imageURL,caption,timestamp)
            values (?,?,?,?,?,?,?)
        """.trimIndent()
        internal val selectIGPosts = """
            SELECT shortcode, users.username, thumbnailURL, imageURL, caption, timestamp
            FROM igposts
            LEFT JOIN users on users.id=igposts.userID
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
            name VARCHAR(255) UNIQUE
            );
        """.trimIndent().replace("[\n\r]".toRegex(), "")
        private val createProfileUsers = """
            CREATE TABLE IF NOT EXISTS profile_users (
            id INTEGER PRIMARY KEY,
            userID INT,
            profileID INT,
            UNIQUE(userID, profileID)
            );
        """.trimIndent().replace("[\n\r]".toRegex(), "")
        private val createUsers = """
            CREATE TABLE IF NOT EXISTS users (
            id INTEGER PRIMARY KEY,
            username VARCHAR(255) UNIQUE,
            lastpoll INTEGER
            );
        """.trimIndent().replace("[\n\r]".toRegex(), "")
        private val createIGPosts = """
            CREATE TABLE IF NOT EXISTS igposts (
            id INTEGER,
            shortcode varchar(16),
            ord INTEGER,
            userID INTEGER,
            thumbnailURL TEXT,
            imageURL TEXT,
            caption TEXT,
            timestamp INTEGER,
            PRIMARY KEY (shortcode, ord)
            );
        """.trimIndent().replace("[\n\r]".toRegex(), "")
        val createAllTables: List<String> = listOf(createProfiles, createProfileUsers, createUsers, createIGPosts)
    }
}

data class IGPost(val shortcode: String, val source: String,
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

fun PreparedStatement.setAndUpdate(values: List<Any>) = use {
    values.forEachIndexed { index, value ->
        when (value) {
            is String -> setString(index + 1, value)
            is Int -> setInt(index + 1, value)
            else -> setObject(index + 1, value)
        }
    }
    executeUpdate()
}

fun Connection.setAndUpdate(sql: String, values: List<Any>) =
        prepareStatement(sql)?.setAndUpdate(values)
