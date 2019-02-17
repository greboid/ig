package com.greboid.scraper

import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException

class Database(private val config: Config) {
    internal lateinit var connection: Connection

    fun connect(): Connection {
        connection = try {
            DriverManager.getConnection(
                    "jdbc:mysql://${config.dbhost}:${config.dbport}/${config.db}?useUnicode=yes&characterEncoding=UTF-8",
                    config.dbuser, config.dbpassword)
        } catch (e: SQLException) {
            throw IllegalStateException("Unable to connect: ${e.localizedMessage}")
        }
        init()
        return connection
    }

    private fun init() {
        Schema.createAllTables.forEach {
            connection.createStatement().executeUpdate(it)
        }
    }

    fun getAccounts(): List<String> {
        return connection.getAllString(Schema.getAccounts, "username")
    }

    fun getAccountID(name: String): Int? {
        val statement = connection.prepareStatement(Schema.getAccountID)
        statement.setString(1, name)
        val results = statement.executeQuery()
        results.first()
        val returnValue = results.getInt(1)
        results.close()
        statement.close()
        return returnValue
    }

    fun addAccount(name: String, password: String) {
        connection.setAndUpdate(Schema.addAccount, listOf(name, password))
    }

    fun delAccount(name: String) =
            connection.setAndUpdate(Schema.delAccount, listOf(name)) == 1

    fun getAccountAdmin(name: String): Boolean {
        val statement = connection.prepareStatement(Schema.getAccountAdmin) ?: return false
        statement.setString(1, name)
        val result = statement.executeQuery()
        result.first()
        val returnValue = result.getBoolean(1)
        result.close()
        statement.close()
        return returnValue
    }

    fun setAccountAdmin(name: String, admin: Boolean): Boolean {
        val statement = connection.prepareStatement(Schema.setAccountAdmin) ?: return false
        statement.setBoolean(1, admin)
        statement.setString(2, name)
        return statement.executeUpdate() == 1
    }

    fun addCategory(account: String, name: String): Boolean {
        val accountID = getAccountID(account) ?: return false
        return connection.setAndUpdate(Schema.addCategory, listOf(accountID, name)) == 1
    }


    fun delCategory(account: String, name: String): Boolean {
        val accountID = getAccountID(account) ?: return false
        return connection.setAndUpdate(Schema.delCategory, listOf(accountID, name)) == 1
    }

    fun getCategories(account: String): List<String> {
        val accountID = getAccountID(account) ?: return emptyList()
        val statement = connection.prepareStatement(Schema.getProfiles) ?: return emptyList()
        statement.setInt(1, accountID)
        val results: ResultSet = statement.executeQuery()
        val returnValue = sequence {
            while (results.next()) {
                yield(results.getString(1))
            }
        }.toList()
        results.close()
        statement.close()
        return returnValue
    }

    fun getProfileUsers(account: String, profile: String): List<String> {
        val s = connection.prepareStatement(Schema.getProfileUsers)
        s.setString(1, profile)
        s.setString(2, account)
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

    fun getSourcesCategories(account: String, source: String): List<String> {
        val s = connection.prepareStatement(Schema.getUserProfiles)
        s.setString(1, source)
        s.setString(2, account)
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

    fun addSourceCategory(account: String, user: String, profile: String): Boolean {
        val accountID = getAccountID(account) ?: return false
        val userID = getSourcesID(user) ?: return false
        val profileID = getCategoryID(accountID, profile) ?: return false
        return connection.setAndUpdate(Schema.addUserToProfile, listOf(userID, profileID)) == 1
    }

    fun delSourceCategory(account: String, user: String, profile: String): Boolean {
        val accountID = getAccountID(account) ?: return false
        val userID = getSourcesID(user) ?: return false
        val profileID = getCategoryID(accountID, profile) ?: return false
        return connection.setAndUpdate(Schema.deleteProfileFromUser, listOf(userID, profileID)) == 1
    }

    fun addSource(account: String, name: String, sourceType: String): Boolean {
        val sourceTypeID = getSourceTypeID(sourceType) ?: return false
        return connection.setAndUpdate(Schema.addUser, listOf(name, sourceTypeID)) == 1
    }

    fun delSource(account: String, name: String): Boolean {
        return connection.setAndUpdate(Schema.delUser, listOf(name)) == 1
    }

    fun getSourceTypeID(name: String): Int? {
        val statement = connection.prepareStatement(Schema.getSourceTypeID) ?: return null
        statement.setString(1, name)
        val result = statement.executeQuery()
        result.first()
        val returnValue = result.getInt(1)
        result.close()
        statement.close()
        return returnValue
    }

    fun getSourcesID(name: String): Int? {
        val statement = connection.prepareStatement(Schema.getUserID) ?: return null
        statement.setString(1, name)
        val result = statement.executeQuery()
        result.first()
        val returnValue = result.getInt(1)
        result.close()
        statement.close()
        return returnValue
    }

    fun getCategoryID(account: Int, name: String): Int? {
        val statement = connection.prepareStatement(Schema.getProfileID) ?: return null
        statement.setString(1, name)
        statement.setInt(2, account)
        val result = statement.executeQuery()
        result.first()
        val returnValue = result.getInt(1)
        result.close()
        statement.close()
        return returnValue
    }

    fun getSources(account: String): List<String> =
            connection.getAllString(Schema.getUsers, "name")

    fun getAllSources(): List<String> =
            connection.getAllString(Schema.getUsers, "name")

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
        return checkIGPost(shortcode, ord) && connection.setAndUpdate(Schema.addIGPost,
                listOf(shortcode, ord, thumbnailURL, imageURL, caption, timestamp, userID)) == 1
    }

    fun getAllIgPost(account: String, start: Int = 0, count: Int = 5) = getIntIGPost(account,null, start, count)
    fun getIGPost(account: String, profile: String, start: Int = 0, count: Int = 5) = getIntIGPost(account, profile, start, count)

    private fun getIntIGPost(account: String, profile: String? = null, start: Int = 0, count: Int = 5): List<IGPost> {
        val accountID = getAccountID(account) ?: return emptyList()
        println("Get Posts: $account $profile $start $count")
        val s = (if (profile == null) {
            val s = connection.prepareStatement(Schema.selectAllIGPosts)
            s.setInt(1, count)
            s.setInt(2, start)
            s.setInt(3, accountID)
            s
        } else {
            val s = connection.prepareStatement(Schema.selectIGPosts)
            s.setString(1, profile)
            s.setInt(2, count)
            s.setInt(3, start)
            s.setInt(3, accountID)
            s
        })
        val results = s.executeQuery()
        val returnValue = sequence {
            while (results.next()) {
                yield(IGPost(
                        shortcode = results.getString(1),
                        source = results.getString(2),
                        thumb = results.getString(3),
                        url = results.getString(4),
                        caption = results.getString(5),
                        timestamp = results.getInt(6),
                        ord = results.getInt(7)
                ))
            }
        }.toList()
        results.close()
        s.close()
        return returnValue
    }

    internal object Schema {
        internal val addSourceType = """
            insert into sourcetype (name) values (?)
        """.trimIndent()
        internal val getSourceTypeID = """
            select id from sourceTypes where name=?
        """.trimIndent()
        internal val getAccountAdmin = """
            select isAdmin from accounts where username=?
        """.trimIndent()
        internal val setAccountAdmin = """
            update accounts set isAdmin = ? where username=?
        """.trimIndent()
        internal val delAccount = """
            delete from accounts where username=?
        """.trimIndent()
        internal val getAccountID = """
            select id from accounts where username=?
        """.trimIndent()
        internal val addAccount = """
            insert into accounts (username, password, isAdmin) values (?, ?, false)
        """.trimIndent()
        internal val getAccounts = """
            select username from accounts
        """.trimIndent()
        internal val deleteProfileFromUser = """
            delete from categoryMap where source_ID=? AND categories_id=?
        """.trimIndent()
        internal val addUserToProfile = """
            insert into categoryMap (source_ID,categories_id) values (?,?)
        """.trimIndent()
        internal val getProfileID = """
            select id from categories where name=? and account_id=?
        """.trimIndent()
        internal val getProfileUsers = """
            select sources.name
            from categories
            left join accounts on accounts.id=categories.Account_ID
            left join categorymap on categorymap.Categories_ID=categories.ID
            left join sources on sources.ID=categorymap.Source_ID
            where categories.name=?
            and accounts.username=?
        """.trimIndent()
        internal val getUserProfiles = """
            select categories.name
            from categorymap
            left join categories on categorymap.categories_ID=categories.id
            left join sources on categorymap.source_ID=sources.id
            left join accounts on accounts.id=categories.Account_ID
            where sources.name=?
            and accounts.username=?
        """.trimIndent()
        internal val getUserID = """
            select id from sources where name=?
        """.trimIndent()
        internal val addCategory = """
            insert into categories(account_id, name) values (?, ?)
        """.trimIndent()
        internal val delCategory = """
            delete from categories where account_id=? AND name=?
        """.trimIndent()
        internal val getProfiles = """
            select name from categories where Account_ID=?
        """.trimIndent()
        internal val addUser = """
            insert into sources (name, sourceType_ID) values (?, ?)
        """.trimIndent()
        internal val delUser = """
            delete from sources where name=?
        """.trimIndent()
        internal val getUsers = """
            select name from sources
        """.trimIndent()
        internal val checkIGPost = """
            select count(*) from sourceitems WHERE identifier=? AND `index`=?
        """.trimIndent()
        internal val addIGPost = """
            insert into sourceitems
            (identifier,`index`,thumbnail,url,caption,`timestamp`,Source_ID)
            values (?,?,?,?,?,?,?)
        """.trimIndent()
        internal val selectAllIGPosts = """
            SELECT identifier, sources.name, thumbnail, url, caption, `timestamp`, `index`
            FROM sourceitems
            LEFT JOIN sources on sources.id=sourceitems.Source_ID
            LEFT JOIN CategoryMap on CategoryMap.Source_ID=sources.id
            LEFT JOIN categories on CategoryMap.Categories_ID=categories.id
            ORDER BY timestamp DESC
            LIMIT ?
            OFFSET ?
        """.trimIndent()
        internal val selectIGPosts = """
            SELECT identifier, sources.name, thumbnail, url, caption, `timestamp`, `index`
            FROM sourceitems
            LEFT JOIN sources on sources.id=sourceitems.Source_ID
            LEFT JOIN CategoryMap on CategoryMap.Source_ID=sources.id
            LEFT JOIN categories on CategoryMap.Categories_ID=categories.id
            WHERE categories.name=?
            ORDER BY timestamp DESC
            LIMIT ?
            OFFSET ?
        """.trimIndent()
        val createAllTables = ClassLoader.getSystemResource("sql/schema.sql").readText().split(";").filter { it.isNotEmpty() }
    }
}

data class IGPost(val shortcode: String, val source: String,
                  val thumb: String, val url: String,
                  val caption: String, val timestamp: Int, internal val ord: Int)

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
            is Boolean -> setBoolean(index + 1, value)
            else -> setObject(index + 1, value)
        }
    }
    executeUpdate()
}

fun Connection.setAndUpdate(sql: String, values: List<Any>) =
        prepareStatement(sql)?.setAndUpdate(values)
