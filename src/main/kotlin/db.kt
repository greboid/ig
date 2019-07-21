package com.greboid.scraper

import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.TimeZone

class Database(private val config: Config) {
    private lateinit var internalConnection: Connection
    internal val connection: Connection
        get() {
            if (!internalConnection.isValid(1)) {
                connect()
            }
            return internalConnection
        }

    fun connect(): Connection {
        val startTime = System.currentTimeMillis()
        var tempConnect: Connection? = null
        var lastError: Throwable? = null
        do {
            logger.debug("Trying DB connection.")
            if (startTime + 30000 < System.currentTimeMillis()) {
                logger.debug("Breaking")
                break
            }
            try {
                tempConnect = DriverManager.getConnection(
                    "jdbc:mysql://${config.dbhost}:${config.dbport}/${config.db}?useUnicode=yes&characterEncoding=UTF-8&serverTimezone=${TimeZone.getDefault().id}",
                    config.dbuser, config.dbpassword)
            } catch (e: SQLException) {
                lastError = e
            }
            Thread.sleep(1000)
        } while (tempConnect == null)
        if (tempConnect != null) {
            internalConnection = tempConnect
        } else {
            throw IllegalStateException("Unable to connect to database: ${lastError?.localizedMessage ?: "Unknown error"}")
        }
        init()
        return connection
    }

    private fun init() {
        Schema.init(connection)
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
        result.first()
        val returnValue = result.getInt(1)
        result.close()
        statement.close()
        return returnValue
    }

    fun getProfileID(name: String): Int? {
        val statement = connection.prepareStatement(Schema.getProfileID) ?: return null
        statement.setString(1, name)
        val result = statement.executeQuery()
        val check = result.first()
        if (!check) {
            return null
        }
        val returnValue = result.getInt(1)
        result.close()
        statement.close()
        return returnValue
    }

    fun getUsers(): List<String> =
            connection.getAllString(Schema.getUsers, "username")

    private fun checkIGPost(shortcode: String, ord: Int): Boolean {
        val statement = connection.prepareStatement(Schema.checkIGPost) ?: return false
        statement.setString(1, shortcode)
        statement.setInt(2, ord)
        val result = statement.executeQuery()
        result.first()
        val returnValue = result.getInt(1)
        result.close()
        statement.close()
        return returnValue < 1
    }

    fun addIGPost(shortcode: String, ord: Int, userID: Int, thumbnailURL: String,
                  imageURL: String, caption: String, timestamp: Int): Boolean {
        return connection.setAndUpdate(Schema.addIGPost,
                listOf(shortcode, ord, userID, thumbnailURL, imageURL, caption, timestamp, imageURL)) == 1
    }

    fun getIGPost(shortcode: String, ord: Int = 0): IGPost {
        val s = connection.prepareStatement(Schema.selectIGPost)
        s.setString(1, shortcode)
        s.setInt(2, ord)
        val results = s.executeQuery()
        results.next()
        val returnValue = IGPost(
                shortcode = results.getString(1),
                source = results.getString(2),
                thumb = results.getString(3),
                url = results.getString(4),
                caption = results.getString(5),
                timestamp = results.getInt(6),
                ord = results.getInt(7),
                date = Instant.ofEpochMilli(results.getInt(6).toLong() * 1000)
                        .atZone(ZoneId.of("UTC")).format(DateTimeFormatter.RFC_1123_DATE_TIME)
        )
        results.close()
        s.close()
        return returnValue
    }
    fun getAllIgPost(start: Int = 0, count: Int = 5) = getIntIGPost(null, start, count)
    fun getIGPost(profile: String, start: Int = 0, count: Int = 5) = getIntIGPost(profile, start, count)

    private fun getIntIGPost(profile: String? = null, start: Int = 0, count: Int = 5): List<IGPost> {
        val s = (if (profile == null) {
            val s = connection.prepareStatement(Schema.selectAllIGPosts)
            s.setInt(1, count)
            s.setInt(2, start)
            s
        } else {
            val s = connection.prepareStatement(Schema.selectIGPosts)
            s.setString(1, profile)
            s.setInt(2, count)
            s.setInt(3, start)
            s
        })
        val results = s.executeQuery()
        val returnValue = resultSetToIgPosts(results)
        s.close()
        return returnValue
    }

    fun getUserIGPost(user: String, start: Int = 0, count: Int = 5): List<IGPost> {
        val s = connection.prepareStatement(Schema.selectUserIGPosts)
        s.setString(1, user)
        s.setInt(2, count)
        s.setInt(3, start)
        val results = s.executeQuery()
        val returnValue = resultSetToIgPosts(results)
        s.close()
        return returnValue
    }

    private fun resultSetToIgPosts(results: ResultSet): List<IGPost> {
        val returnValue = sequence {
            while (results.next()) {
                yield(IGPost(
                    shortcode = results.getString(1),
                    source = results.getString(2),
                    thumb = results.getString(3),
                    url = results.getString(4),
                    caption = results.getString(5),
                    timestamp = results.getInt(6),
                    ord = results.getInt(7),
                    date = Instant.ofEpochMilli(results.getInt(6).toLong() * 1000)
                        .atZone(ZoneId.of("UTC")).format(DateTimeFormatter.RFC_1123_DATE_TIME)
                ))
            }
        }.toList()
        results.close()
        return returnValue
    }

    fun getStringSetting(name: String, default: String? = null): String? {
        val s = connection.prepareStatement(Schema.selectSetting)
        s.setString(1, name)
        val results = s.executeQuery()
        if (results.isMyResultSetEmpty()) {
            return default
        }
        results.next()
        val returnValue = results.getString(1)
        results.close()
        s.close()
        return returnValue
    }

    fun getIntSetting(name: String, default: Int? = null): Int? {
        val s = connection.prepareStatement(Schema.selectSetting)
        s.setString(1, name)
        val results = s.executeQuery()
        if (results.isMyResultSetEmpty()) {
            return default
        }
        results.next()
        val returnValue = try {
            results.getInt(1)
        } catch (e: NumberFormatException) {
            default
        }
        results.close()
        s.close()
        return returnValue
    }

    fun getBoolSetting(name: String, default: Boolean? = null): Boolean? {
        val s = connection.prepareStatement(Schema.selectSetting)
        s.setString(1, name)
        val results = s.executeQuery()
        if (results.isMyResultSetEmpty()) {
            return default
        }
        results.next()
        val returnValue = results.getBoolean(1)
        results.close()
        s.close()
        return returnValue
    }

    fun setStringSetting(name: String, value: String) {
        connection.setAndUpdate(Schema.updateSetting, listOf(name, value, value))
    }

    fun setIntSetting(name: String, value: Int) {
        connection.setAndUpdate(Schema.updateSetting, listOf(name, value, value))
    }

    fun setBoolSetting(name: String, value: Boolean) {
        connection.setAndUpdate(Schema.updateSetting, listOf(name, value, value))
    }

    internal object Schema {
        val version = 3
        internal val deleteProfileFromUser = """
            delete from profile_users where userID=? AND profileID=?
        """
        internal val addUserToProfile = """
            insert into profile_users (userID,profileID) values (?,?)
        """
        internal val deleteProfileFromProfileUsers = """
            delete from profile_users where profileID=?
        """
        internal val getProfileID = """
            select id from profiles where name=?
        """
        internal val deleteUserFromProfileUsers = """
            delete from profile_users where userID=?
        """
        internal val getUserProfiles = """
            select profiles.name
            from profile_users
            left join profiles on profile_users.profileID=profiles.id
            left join users on profile_users.userID=users.id
            where users.username=?
        """
        internal val getProfileUsers = """
            select users.username
            from profile_users
            left join profiles on profile_users.profileID=profiles.id
            left join users on profile_users.userID=users.id
            where profiles.name=?
        """
        internal val getUserID = """
            select id from users where username=?
        """
        internal val addProfile = """
            insert into profiles(name) values (?)
        """
        internal val delProfile = """
            delete from profiles where name=?
        """
        internal val getProfiles = """
            select name from profiles order by isNull(priority), priority
        """
        internal val addUser = """
            insert into users(username) values (?)
        """
        internal val delUser = """
            delete from users where username=?
        """
        internal val getUsers = """
            select username from users
        """
        internal val checkIGPost = """
            select count(*) from igposts WHERE shortcode=? AND ord=?
        """
        internal val addIGPost = """
            insert into igposts
            (shortcode,ord,userID,thumbnailURL,imageURL,caption,timestamp)
            values (?,?,?,?,?,?,?) 
            ON DUPLICATE KEY UPDATE `imageURL` = ?
        """
        internal val selectIGPost = """
            SELECT shortcode, users.username, thumbnailURL, imageURL, caption, timestamp, ord
            FROM igposts
            LEFT JOIN users on users.id=igposts.userID
            LEFT JOIN profile_users on profile_users.userid=users.id
            LEFT JOIN profiles on profile_users.profileid=profiles.id
            WHERE igposts.shortcode=?
            AND igposts.ord=?
        """
        internal val selectAllIGPosts = """
            SELECT shortcode, users.username, thumbnailURL, imageURL, caption, timestamp, ord
            FROM igposts
            LEFT JOIN users on users.id=igposts.userID
            LEFT JOIN profile_users on profile_users.userid=users.id
            LEFT JOIN profiles on profile_users.profileid=profiles.id
            ORDER BY timestamp DESC
            LIMIT ?
            OFFSET ?
        """
        internal val selectUserIGPosts = """
            SELECT shortcode, users.username, thumbnailURL, imageURL, caption, timestamp, ord
            FROM igposts
            LEFT JOIN users on users.id=igposts.userID
            WHERE users.username=?
            ORDER BY timestamp DESC
            LIMIT ?
            OFFSET ?
        """
        internal val selectIGPosts = """
            SELECT shortcode, users.username, thumbnailURL, imageURL, caption, timestamp, ord
            FROM igposts
            LEFT JOIN users on users.id=igposts.userID
            LEFT JOIN profile_users on profile_users.userid=users.id
            LEFT JOIN profiles on profile_users.profileid=profiles.id
            WHERE profiles.name=?
            ORDER BY timestamp DESC
            LIMIT ?
            OFFSET ?
        """
        internal val selectSetting = """
            SELECT value from settings where name = ?
        """
        internal val updateSetting = """
            INSERT INTO settings VALUES (?, ?) ON DUPLICATE KEY UPDATE `value` = ?;
        """
        private val createProfiles = """
            CREATE TABLE IF NOT EXISTS profiles (
            id INTEGER PRIMARY KEY AUTO_INCREMENT,
            name VARCHAR(255) UNIQUE
            ) DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
        """
        private val createProfileUsers = """
            CREATE TABLE IF NOT EXISTS profile_users (
            id INTEGER PRIMARY KEY AUTO_INCREMENT,
            userID INT,
            profileID INT,
            UNIQUE(userID, profileID)
            ) DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
        """
        private val createUsers = """
            CREATE TABLE IF NOT EXISTS users (
            id INTEGER PRIMARY KEY AUTO_INCREMENT,
            username VARCHAR(255) UNIQUE,
            lastpoll INTEGER
            ) DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
        """
        private val createIGPosts = """
            CREATE TABLE IF NOT EXISTS igposts (
            shortcode varchar(16),
            ord INTEGER,
            userID INTEGER,
            thumbnailURL TEXT,
            imageURL TEXT,
            caption TEXT,
            timestamp INTEGER,
            PRIMARY KEY (shortcode, ord)
            ) DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
        """
        private val createVersion = """
            CREATE TABLE IF NOT EXISTS version (
            id INTEGER,
            version INTEGER
            ) DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
        """
        private val getVersion = """
            select IFNULL((SELECT version from version),0) as version
        """
        private val zeroToOneChange = """
            ALTER TABLE `profiles` ADD COLUMN priority INT NULL DEFAULT NULL AFTER `name`;
        """
        private val zeroToOneVersion = """
            insert into version values (1, 1);
        """
        private val oneToTwoChange = """
            ALTER TABLE `igposts` CHANGE COLUMN `shortcode` `shortcode` VARCHAR(64) NOT NULL;
        """
        private val oneToTwoVersion = """
            update version SET version=2 where id=1;
        """
        private val twoToThreeChange = """
            CREATE TABLE IF NOT EXISTS settings (
            `name` VARCHAR(255) NOT NULL,
            `value` VARCHAR(255) NOT NULL,
            UNIQUE INDEX `uniqueName` (`name`)
            ) DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
        """
        private val twoToThreeVersion = """
            update version SET version=3 where id=1;
        """
        private val createSettings = twoToThreeChange
        private val createAllTables: List<String> = listOf(createVersion, createProfiles, createProfileUsers, createUsers, createIGPosts, createSettings)
        fun init(connection: Connection) {
            createAllTables.forEach {
                connection.createStatement().executeUpdate(it)
            }
            var currentVersion = connection.getAllInt(getVersion, "version").first()
            if (currentVersion == 0) {
                connection.createStatement().executeUpdate(zeroToOneChange)
                connection.createStatement().executeUpdate(zeroToOneVersion)
                currentVersion = 1
            }
            if (currentVersion == 1) {
                connection.createStatement().executeUpdate(oneToTwoChange)
                connection.createStatement().executeUpdate(oneToTwoVersion)
            }
            if (currentVersion == 2) {
                connection.createStatement().executeUpdate(twoToThreeChange)
                connection.createStatement().executeUpdate(twoToThreeVersion)
            }
        }
    }
}

data class IGPost(val shortcode: String, val source: String,
                  val thumb: String, val url: String,
                  val caption: String, val timestamp: Int, val ord: Int, val date: String)

fun ResultSet.isMyResultSetEmpty(): Boolean {
    return !isBeforeFirst && row == 0
}

fun ResultSet.getAllString(fieldName: String) = sequence {
    use {
        while (next()) {
            yield(getString(fieldName))
        }
    }
}.toList().filterNotNull()

fun Connection.getAllString(sql: String, fieldName: String) =
    prepareStatement(sql)?.executeQuery()?.getAllString(fieldName) ?: emptyList()

fun ResultSet.getAllInt(fieldName: String) = sequence {
    use {
        while (next()) {
            yield(getInt(fieldName))
        }
    }
}.toList().filterNotNull()

fun Connection.getAllInt(sql: String, fieldName: String) =
    prepareStatement(sql)?.executeQuery()?.getAllInt(fieldName) ?: emptyList()

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
