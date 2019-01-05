package com.greboid.scraper

import com.wix.mysql.EmbeddedMysql
import com.wix.mysql.EmbeddedMysql.anEmbeddedMysql
import com.wix.mysql.ScriptResolver
import com.wix.mysql.config.Charset.UTF8MB4
import com.wix.mysql.config.MysqldConfig.aMysqldConfig
import com.wix.mysql.config.SchemaConfig
import com.wix.mysql.config.SchemaConfig.aSchemaConfig
import com.wix.mysql.distribution.Version
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class DbTest {

    companion object {

        private lateinit var mysqld: EmbeddedMysql
        internal val config = Config(dbhost = "127.0.0.1", dbport = 2215)
        val initial: SchemaConfig = aSchemaConfig("ig")
                .withScripts(ScriptResolver.classPathScript("/db/initial.sql"))
                .withCharset(UTF8MB4)
                .build()

        @BeforeAll
        @JvmStatic
        private fun setup() {
            val config = aMysqldConfig(Version.v5_7_latest)
                    .withCharset(UTF8MB4)
                    .withPort(2215)
                    //.withServerVariable("bind-address", "127.0.0.1")
                    .withUser("ig", "ig")
                    .withTimeZone("Europe/London")
                    .withTimeout(5, TimeUnit.MINUTES)
                    .build()
            mysqld = anEmbeddedMysql(config)
                    .addSchema(initial)
                    .start()
        }

        @AfterAll
        @JvmStatic
        fun teardown() {
            mysqld.stop()
        }
    }

    @BeforeEach
    private fun resetSchema() {
        mysqld.reloadSchema(initial)
    }

    @Test
    fun `test connect`() {
        val db = Database(config)
        db.connect()
    }

    @Test
    fun `test invalid host`() {
        val db = Database(Config(dbhost = "junk"))
        assertThrows<IllegalStateException> { db.connect() }
    }

    @Test
    fun `test use before connect`() {
        val db = Database(config)
        assertThrows<UninitializedPropertyAccessException> { db.getUsers() }
    }

    @Test
    fun `test get profiles`() {
        val db = Database(config)
        db.connect()
        assertEquals(emptyList(), db.getProfiles())
        mysqld.executeScripts("ig", ScriptResolver.classPathScript("/db/profiles.sql"))
        assertEquals(listOf("testprofile1", "testprofile2"), db.getProfiles())
    }

    @Test
    fun `test add profile`() {
        val db = Database(config)
        db.connect()
        val name = "test"
        db.addProfile(name)
        assertEquals(listOf("test"), db.getProfiles())
    }

    @Test
    fun `del profile`() {
        val db = Database(config)
        db.connect()
        val name = "test"
        assertEquals(emptyList(), db.getProfiles())
        db.addProfile(name)
        assertEquals(listOf("test"), db.getProfiles())
        db.delProfile(name)
        assertEquals(emptyList(), db.getProfiles())
    }

    @Test
    fun `test get users`() {
        val db = Database(config)
        db.connect()
        assertEquals(emptyList(), db.getUsers())
        mysqld.executeScripts("ig", ScriptResolver.classPathScript("/db/users.sql"))
        assertEquals(listOf("testuser1", "testuser2"), db.getUsers())
    }

    @Test
    fun `test add user`() {
        val db = Database(config)
        db.connect()
        val name = "test"
        assertEquals(emptyList(), db.getUsers())
        db.addUser(name)
        assertEquals(listOf("test"), db.getUsers())
    }

    @Test
    fun `del user`() {
        val db = Database(config)
        db.connect()
        val name = "test"
        assertEquals(emptyList(), db.getUsers())
        db.addUser(name)
        assertEquals(listOf("test"), db.getUsers())
        db.delUser(name)
        assertEquals(emptyList(), db.getUsers())
    }

    @Test
    fun `check user profile entry is removed when deleting user`() {
        val db = Database(config)
        db.connect()
        assertEquals(emptyList(), db.getUserProfiles("testuser1"))
        assertEquals(emptyList(), db.getUserProfiles("testuser2"))
        mysqld.executeScripts("ig", ScriptResolver.classPathScript("/db/userprofiles.sql"))
        assertEquals(listOf("testprofile1", "testprofile2"), db.getUserProfiles("testuser1"))
        db.delUser("testuser1")
        val result = checkProfiles(db, 1)
        assertEquals(0, result)
    }

    private fun checkProfiles(db: Database, userID: Int): Int? {
        val statement = db.connection.prepareStatement(
                "select COUNT(*) FROM profile_users where userid=?") ?: return null
        statement.setInt(1, userID)
        val result = statement.executeQuery()
        result.first()
        val returnValue = result.getInt(1)
        result.close()
        statement.close()
        return returnValue
    }

    @Test
    fun `test get profile users`() {
        val db = Database(config)
        db.connect()
        assertEquals(emptyList(), db.getProfileUsers("testprofile1"))
        assertEquals(emptyList(), db.getProfileUsers("testprofile2"))
        mysqld.executeScripts("ig", ScriptResolver.classPathScript("/db/profileusers.sql"))
        assertEquals(listOf("testuser1", "testuser2"), db.getProfileUsers("testprofile1"))
        assertEquals(listOf("testuser1"), db.getProfileUsers("testprofile2"))
    }

    @Test
    fun `test get user profiles`() {
        val db = Database(config)
        db.connect()
        assertEquals(emptyList(), db.getUserProfiles("testuser1"))
        assertEquals(emptyList(), db.getUserProfiles("testuser2"))
        mysqld.executeScripts("ig", ScriptResolver.classPathScript("/db/userprofiles.sql"))
        assertEquals(listOf("testprofile1", "testprofile2"), db.getUserProfiles("testuser1"))
        assertEquals(listOf("testprofile1"), db.getUserProfiles("testuser2"))
    }

    @Test
    fun `test add user to profile`() {
        val db = Database(config)
        db.connect()
        mysqld.executeScripts("ig", ScriptResolver.classPathScript("/db/users.sql"))
        mysqld.executeScripts("ig", ScriptResolver.classPathScript("/db/profiles.sql"))
        assertEquals(emptyList(), db.getUserProfiles("testuser1"))
        db.addUserProfile("testuser1", "testprofile1")
        assertEquals(listOf("testprofile1"), db.getUserProfiles("testuser1"))
        db.addUserProfile("testuser1", "testprofile2")
        assertEquals(listOf("testprofile1", "testprofile2"), db.getUserProfiles("testuser1"))
    }

    @Test
    fun `test del user from profile`() {
        val db = Database(config)
        db.connect()
        assertEquals(emptyList(), db.getUserProfiles("testuser1"))
        assertEquals(emptyList(), db.getUserProfiles("testuser2"))
        mysqld.executeScripts("ig", ScriptResolver.classPathScript("/db/userprofiles.sql"))
        assertEquals(listOf("testprofile1", "testprofile2"), db.getUserProfiles("testuser1"))
        db.delUserProfile("testuser1", "testprofile1")
        assertEquals(listOf("testprofile2"), db.getUserProfiles("testuser1"))
        db.delUserProfile("testuser1", "testprofile2")
        assertEquals(emptyList(), db.getUserProfiles("testuser1"))
    }

    @Test
    fun `test getting posts`() {
        val db = Database(config)
        db.connect()
        mysqld.executeScripts("ig", ScriptResolver.classPathScript("/db/posts.sql"))
        val results = db.getAllIgPost()
        assertEquals(5, results.size)
    }

    @Test
    fun `test getting profile posts`() {
        val db = Database(config)
        db.connect()
        mysqld.executeScripts("ig", ScriptResolver.classPathScript("/db/posts.sql"))
        val results = db.getIGPost("testprofile2")
        assertEquals(4, results.size)
    }

    @Test
    fun `test getting limited number posts`() {
        val db = Database(config)
        db.connect()
        mysqld.executeScripts("ig", ScriptResolver.classPathScript("/db/posts.sql"))
        val results = db.getIGPost("testprofile1", 0, 3)
        assertEquals(3, results.size)
        assertTrue(results.map { it.shortcode }.toList().containsAll(listOf("Uv6J1JGURH2", "RSLBmnFzrba", "YsE1tF0WXcV")))
    }

    @Test
    fun `test getting offset number posts`() {
        val db = Database(config)
        db.connect()
        mysqld.executeScripts("ig", ScriptResolver.classPathScript("/db/posts.sql"))
        val results = db.getIGPost("testprofile1", 2, 3)
        assertEquals(3, results.size)
        assertTrue(results.map { it.shortcode }.toList().containsAll(listOf("YsE1tF0WXcV", "Z5Z8TJG7X9p", "IIpRRMXowSZ")))
    }

    @Test
    fun `test adding post`() {
        val db = Database(config)
        db.connect()
        mysqld.executeScripts("ig", ScriptResolver.classPathScript("/db/users.sql"))
        val igpost1 = IGPost("a", "testuser1", "thumb1", "url1", "caption1", 1, 1)
        val igpost2 = IGPost("b", "testuser2", "thumb2", "url2", "caption2", 2, 2)
        assertEquals(emptyList(), db.getAllIgPost())
        db.addIGPost(igpost1.shortcode, igpost1.ord, 1, igpost1.thumb, igpost1.url, igpost1.caption, igpost1.timestamp)
        val results1 = db.getAllIgPost()
        assertEquals(1, results1.size)
        assertTrue(results1.contains(igpost1))
        db.addIGPost(igpost2.shortcode, igpost2.ord, 2, igpost2.thumb, igpost2.url, igpost2.caption, igpost2.timestamp)
        val results2 = db.getAllIgPost()
        assertEquals(2, results2.size)
        assertTrue(results2.contains(igpost1))
        assertTrue(results2.contains(igpost2))
    }

    @Test
    fun `test adding duplicate post`() {
        val db = Database(config)
        db.connect()
        mysqld.executeScripts("ig", ScriptResolver.classPathScript("/db/users.sql"))
        val igpost1 = IGPost("a", "testuser1", "thumb1", "url1", "caption1", 1, 0)
        val igpost2 = IGPost("b", "testuser2", "thumb2", "url2", "caption2", 2, 0)
        val igpost3 = IGPost("b", "testuser2", "thumb2", "url2", "caption2", 2, 0)
        assertEquals(emptyList(), db.getAllIgPost())
        db.addIGPost(igpost1.shortcode, igpost1.ord, 1, igpost1.thumb, igpost1.url, igpost1.caption, igpost1.timestamp)
        val results1 = db.getAllIgPost()
        assertEquals(1, results1.size)
        assertTrue(results1.contains(igpost1))
        db.addIGPost(igpost2.shortcode, igpost2.ord, 2, igpost2.thumb, igpost2.url, igpost2.caption, igpost2.timestamp)
        val results2 = db.getAllIgPost()
        assertEquals(2, results2.size)
        assertTrue(results2.contains(igpost1))
        assertTrue(results2.contains(igpost2))
        db.addIGPost(igpost3.shortcode, igpost3.ord, 2, igpost3.thumb, igpost3.url, igpost3.caption, igpost3.timestamp)
        val results3 = db.getAllIgPost()
        assertEquals(2, results3.size)
        assertTrue(results3.contains(igpost1))
        assertTrue(results3.contains(igpost2))
    }

    @Test
    fun `test adding sidecar`() {
        val db = Database(config)
        db.connect()
        mysqld.executeScripts("ig", ScriptResolver.classPathScript("/db/users.sql"))
        val igpost1 = IGPost("a", "testuser1", "thumb1", "url1", "caption1", 1, 0)
        val igpost2 = IGPost("a", "testuser1", "thumb2", "url2", "caption1", 1, 1)
        val igpost3 = IGPost("a", "testuser1", "thumb3", "url3", "caption1", 1, 2)
        assertEquals(emptyList(), db.getAllIgPost())
        db.addIGPost(igpost1.shortcode, igpost1.ord, 1, igpost1.thumb, igpost1.url, igpost1.caption, igpost1.timestamp)
        db.addIGPost(igpost2.shortcode, igpost2.ord, 1, igpost2.thumb, igpost2.url, igpost2.caption, igpost2.timestamp)
        db.addIGPost(igpost3.shortcode, igpost3.ord, 1, igpost3.thumb, igpost3.url, igpost3.caption, igpost3.timestamp)
        val results1 = db.getAllIgPost()
        assertEquals(3, results1.size)
        assertTrue(results1.contains(igpost1))
        assertTrue(results1.contains(igpost2))
        assertTrue(results1.contains(igpost3))
    }
}