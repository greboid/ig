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
        assertThrows<UninitializedPropertyAccessException> { db.getAccounts() }
    }

    @Test
    fun `test get accounts`() {
        val db = Database(config)
        db.connect()
        mysqld.executeScripts("ig", ScriptResolver.classPathScript("/db/accounts.sql"))
        assertEquals(listOf("testuser1", "testuser2", "testuser3"), db.getAccounts())
    }

    @Test
    fun `test get account ID`() {
        val db = Database(config)
        db.connect()
        mysqld.executeScripts("ig", ScriptResolver.classPathScript("/db/accounts.sql"))
        assertEquals(1, db.getAccountID("testuser1"))
        assertEquals(2, db.getAccountID("testuser2"))
    }

    @Test
    fun `test add account`() {
        val db = Database(config)
        db.connect()
        assertEquals(emptyList(), db.getAccounts())
        db.addAccount("testuser4", "password4")
        assertEquals(listOf("testuser4"), db.getAccounts())
    }

    @Test
    fun `test del account`() {
        val db = Database(config)
        db.connect()
        mysqld.executeScripts("ig", ScriptResolver.classPathScript("/db/accounts.sql"))
        assertEquals(listOf("testuser1", "testuser2", "testuser3"), db.getAccounts())
        db.delAccount("testuser1")
        assertEquals(listOf("testuser2", "testuser3"), db.getAccounts())
    }

    @Test
    fun `test is admin`() {
        val db = Database(config)
        db.connect()
        mysqld.executeScripts("ig", ScriptResolver.classPathScript("/db/accounts.sql"))
        assertEquals(true, db.getAccountAdmin("testuser1"))
        assertEquals(false, db.getAccountAdmin("testuser2"))
    }

    @Test
    fun `test set admin`() {
        val db = Database(config)
        db.connect()
        mysqld.executeScripts("ig", ScriptResolver.classPathScript("/db/accounts.sql"))
        assertEquals(true, db.getAccountAdmin("testuser1"))
        db.setAccountAdmin("testuser1", false)
        assertEquals(false, db.getAccountAdmin("testuser1"))
        db.setAccountAdmin("testuser1", true)
        assertEquals(true, db.getAccountAdmin("testuser1"))
    }

    @Test
    fun `test get categories`() {
        val db = Database(config)
        db.connect()
        mysqld.executeScripts("ig", ScriptResolver.classPathScript("/db/accounts.sql"))
        assertEquals(emptyList(), db.getCategories("testuser1"))
        mysqld.executeScripts("ig", ScriptResolver.classPathScript("/db/categories.sql"))
        assertEquals(listOf("testcategories1", "testcategories2"), db.getCategories("testuser1"))
    }

    @Test
    fun `test add category`() {
        val db = Database(config)
        db.connect()
        mysqld.executeScripts("ig", ScriptResolver.classPathScript("/db/accounts.sql"))
        val name = "test"
        assertEquals(emptyList(), db.getCategories("testuser1"))
        assertTrue(db.addCategory("testuser1", name))
        assertEquals(listOf("test"), db.getCategories("testuser1"))
    }

    @Test
    fun `del category`() {
        val db = Database(config)
        db.connect()
        val name = "test"
        mysqld.executeScripts("ig", ScriptResolver.classPathScript("/db/accounts.sql"))
        assertEquals(emptyList(), db.getCategories("testuser1"))
        db.addCategory("testuser1", name)
        assertEquals(listOf("test"), db.getCategories("testuser1"))
        db.delCategory("testuser1", name)
        assertEquals(emptyList(), db.getCategories("testuser1"))
    }

    @Test
    fun `test get sources`() {
        val db = Database(config)
        db.connect()
        mysqld.executeScripts("ig", ScriptResolver.classPathScript("/db/accounts.sql"))
        assertEquals(emptyList(), db.getSources("testuser1"))
        mysqld.executeScripts("ig", ScriptResolver.classPathScript("/db/sources.sql"))
        assertEquals(listOf("testsources1", "testsources2"), db.getSources("testuser1"))
    }

    @Test
    fun `test add source`() {
        val db = Database(config)
        db.connect()
        mysqld.executeScripts("ig", ScriptResolver.classPathScript("/db/accounts.sql"))
        mysqld.executeScripts("ig", ScriptResolver.classPathScript("/db/sourcetypes.sql"))
        val name = "test"
        assertEquals(emptyList(), db.getSources("testuser1"))
        db.addSource("testuser1", name, "TestType")
        assertEquals(listOf("test"), db.getSources("testuser1"))
    }

    @Test
    fun `del user`() {
        val db = Database(config)
        db.connect()
        mysqld.executeScripts("ig", ScriptResolver.classPathScript("/db/accounts.sql"))
        mysqld.executeScripts("ig", ScriptResolver.classPathScript("/db/sourcetypes.sql"))
        val name = "test"
        assertEquals(emptyList(), db.getSources("testuser1"))
        db.addSource("testuser1", name, "TestType")
        assertEquals(listOf("test"), db.getSources("testuser1"))
        db.delSource("testuser1", name)
        assertEquals(emptyList(), db.getSources("testuser1"))
    }

    @Test
    fun `check user profile entry is removed when deleting user`() {
        val db = Database(config)
        db.connect()
        assertEquals(emptyList(), db.getSourcesCategories("testuser1", "testsource1"))
        assertEquals(emptyList(), db.getSourcesCategories("testuser2", "testsource1"))
        mysqld.executeScripts("ig", ScriptResolver.classPathScript("/db/userprofiles.sql"))
        assertEquals(listOf("testcategories1", "testcategories2"), db.getSourcesCategories("testuser1", "testsource1"))
        db.delSource("testuser1", "testsource1")
        val result = checkProfiles(db, 1)
        assertEquals(0, result)
    }

    private fun checkProfiles(db: Database, userID: Int): Int? {
        val statement = db.connection.prepareStatement(
                "select COUNT(*) FROM categoryMap where source_ID=?") ?: return null
        statement.setInt(1, userID)
        val result = statement.executeQuery()
        result.first()
        val returnValue = result.getInt(1)
        result.close()
        statement.close()
        return returnValue
    }

    @Test
    fun `test get categories sources`() {
        val db = Database(config)
        db.connect()
        assertEquals(emptyList(), db.getProfileUsers("testuser1","testcategories1"))
        assertEquals(emptyList(), db.getProfileUsers("testuser1","testcategories2"))
        mysqld.executeScripts("ig", ScriptResolver.classPathScript("/db/profileusers.sql"))
        assertEquals(listOf("testsource1", "testsource2"), db.getProfileUsers("testuser1","testcategories1"))
        assertEquals(listOf("testsource1"), db.getProfileUsers("testuser1","testcategories2"))
    }

    @Test
    fun `test get sources categories`() {
        val db = Database(config)
        db.connect()
        assertEquals(emptyList(), db.getSourcesCategories("testuser1", "testsource1"))
        assertEquals(emptyList(), db.getSourcesCategories("testuser2", "testsource1"))
        mysqld.executeScripts("ig", ScriptResolver.classPathScript("/db/userprofiles.sql"))
        assertEquals(listOf("testcategories1", "testcategories2"), db.getSourcesCategories("testuser1", "testsource1"))
        assertEquals(listOf("testcategories1"), db.getSourcesCategories("testuser1", "testsource2"))
    }

    @Test
    fun `test add user to profile`() {
        val db = Database(config)
        db.connect()
        mysqld.executeScripts("ig", ScriptResolver.classPathScript("/db/accounts.sql"))
        mysqld.executeScripts("ig", ScriptResolver.classPathScript("/db/sources.sql"))
        mysqld.executeScripts("ig", ScriptResolver.classPathScript("/db/categories.sql"))
        assertEquals(emptyList(), db.getSourcesCategories("testuser1", "testsources1"))
        db.addSourceCategory("testuser1", "testsources1", "testcategories1")
        assertEquals(listOf("testcategories1"), db.getSourcesCategories("testuser1", "testsources1"))
        db.addSourceCategory("testuser1", "testsources1", "testcategories2")
        assertEquals(listOf("testcategories1", "testcategories2"), db.getSourcesCategories("testuser1", "testsources1"))
    }

    @Test
    fun `test del user from profile`() {
        val db = Database(config)
        db.connect()
        assertEquals(emptyList(), db.getSourcesCategories("testuser1", "testsource1"))
        assertEquals(emptyList(), db.getSourcesCategories("testuser2", "testsource1"))
        mysqld.executeScripts("ig", ScriptResolver.classPathScript("/db/userprofiles.sql"))
        assertEquals(listOf("testcategories1", "testcategories2"), db.getSourcesCategories("testuser1", "testsource1"))
        db.delSourceCategory("testuser1","testsource1", "testcategories1")
        assertEquals(listOf("testcategories2"), db.getSourcesCategories("testuser1", "testsource1"))
        db.delSourceCategory("testuser1", "testsource1", "testcategories2")
        assertEquals(emptyList(), db.getSourcesCategories("testuser1", "testsource1"))
    }

    @Test
    fun `test getting posts`() {
        val db = Database(config)
        db.connect()
        mysqld.executeScripts("ig", ScriptResolver.classPathScript("/db/accounts.sql"))
        mysqld.executeScripts("ig", ScriptResolver.classPathScript("/db/categories.sql"))
        mysqld.executeScripts("ig", ScriptResolver.classPathScript("/db/sources.sql"))
        mysqld.executeScripts("ig", ScriptResolver.classPathScript("/db/posts.sql"))
        val results = db.getAllIgPost("testuser1")
        assertEquals(5, results.size)
    }

    @Test
    fun `test getting profile posts`() {
        val db = Database(config)
        db.connect()
        mysqld.executeScripts("ig", ScriptResolver.classPathScript("/db/accounts.sql"))
        mysqld.executeScripts("ig", ScriptResolver.classPathScript("/db/categories.sql"))
        mysqld.executeScripts("ig", ScriptResolver.classPathScript("/db/sources.sql"))
        mysqld.executeScripts("ig", ScriptResolver.classPathScript("/db/categorymap.sql"))
        mysqld.executeScripts("ig", ScriptResolver.classPathScript("/db/posts.sql"))
        val results = db.getIGPost("testuser1", "testcategories1")
        assertEquals(4, results.size)
    }

    @Test
    fun `test getting limited number posts`() {
        val db = Database(config)
        db.connect()
        mysqld.executeScripts("ig", ScriptResolver.classPathScript("/db/accounts.sql"))
        mysqld.executeScripts("ig", ScriptResolver.classPathScript("/db/categories.sql"))
        mysqld.executeScripts("ig", ScriptResolver.classPathScript("/db/sources.sql"))
        mysqld.executeScripts("ig", ScriptResolver.classPathScript("/db/categorymap.sql"))
        mysqld.executeScripts("ig", ScriptResolver.classPathScript("/db/posts.sql"))
        val results = db.getIGPost("testuser1", "testprofile1", 0, 3)
        assertEquals(3, results.size)
        assertTrue(results.map { it.shortcode }.toList().containsAll(listOf("Uv6J1JGURH2", "RSLBmnFzrba", "YsE1tF0WXcV")))
    }

    @Test
    fun `test getting offset number posts`() {
        val db = Database(config)
        db.connect()
        mysqld.executeScripts("ig", ScriptResolver.classPathScript("/db/accounts.sql"))
        mysqld.executeScripts("ig", ScriptResolver.classPathScript("/db/categories.sql"))
        mysqld.executeScripts("ig", ScriptResolver.classPathScript("/db/sources.sql"))
        mysqld.executeScripts("ig", ScriptResolver.classPathScript("/db/categorymap.sql"))
        mysqld.executeScripts("ig", ScriptResolver.classPathScript("/db/posts.sql"))
        val results = db.getIGPost("testuser1", "testprofile1", 2, 3)
        assertEquals(3, results.size)
        assertTrue(results.map { it.shortcode }.toList().containsAll(listOf("YsE1tF0WXcV", "Z5Z8TJG7X9p", "IIpRRMXowSZ")))
    }

    @Test
    fun `test adding post`() {
        val db = Database(config)
        db.connect()
        mysqld.executeScripts("ig", ScriptResolver.classPathScript("/db/sources.sql"))
        val igpost1 = IGPost("a", "testsources1", "thumb1", "url1", "caption1", 1, 1)
        val igpost2 = IGPost("b", "testsources2", "thumb2", "url2", "caption2", 2, 2)
        assertEquals(emptyList(), db.getAllIgPost("testuser1"))
        db.addIGPost(igpost1.shortcode, igpost1.ord, 1, igpost1.thumb, igpost1.url, igpost1.caption, igpost1.timestamp)
        val results1 = db.getAllIgPost("testuser1")
        assertEquals(1, results1.size)
        assertTrue(results1.contains(igpost1))
        db.addIGPost(igpost2.shortcode, igpost2.ord, 2, igpost2.thumb, igpost2.url, igpost2.caption, igpost2.timestamp)
        val results2 = db.getAllIgPost("testuser1")
        assertEquals(2, results2.size)
        assertTrue(results2.contains(igpost1))
        assertTrue(results2.contains(igpost2))
    }

    @Test
    fun `test adding duplicate post`() {
        val db = Database(config)
        db.connect()
        mysqld.executeScripts("ig", ScriptResolver.classPathScript("/db/sources.sql"))
        val igpost1 = IGPost("a", "testsources1", "thumb1", "url1", "caption1", 1, 0)
        val igpost2 = IGPost("b", "testsources2", "thumb2", "url2", "caption2", 2, 0)
        val igpost3 = IGPost("b", "testsources2", "thumb2", "url2", "caption2", 2, 0)
        assertEquals(emptyList(), db.getAllIgPost("testuser1"))
        db.addIGPost(igpost1.shortcode, igpost1.ord, 1, igpost1.thumb, igpost1.url, igpost1.caption, igpost1.timestamp)
        val results1 = db.getAllIgPost("testuser1")
        assertEquals(1, results1.size)
        assertTrue(results1.contains(igpost1))
        db.addIGPost(igpost2.shortcode, igpost2.ord, 2, igpost2.thumb, igpost2.url, igpost2.caption, igpost2.timestamp)
        val results2 = db.getAllIgPost("testuser1")
        assertEquals(2, results2.size)
        assertTrue(results2.contains(igpost1))
        assertTrue(results2.contains(igpost2))
        db.addIGPost(igpost3.shortcode, igpost3.ord, 2, igpost3.thumb, igpost3.url, igpost3.caption, igpost3.timestamp)
        val results3 = db.getAllIgPost("testuser1")
        assertEquals(2, results3.size)
        assertTrue(results3.contains(igpost1))
        assertTrue(results3.contains(igpost2))
    }

    @Test
    fun `test adding sidecar`() {
        val db = Database(config)
        db.connect()
        mysqld.executeScripts("ig", ScriptResolver.classPathScript("/db/sources.sql"))
        val igpost1 = IGPost("a", "testsources1", "thumb1", "url1", "caption1", 1, 0)
        val igpost2 = IGPost("a", "testsources1", "thumb2", "url2", "caption1", 1, 1)
        val igpost3 = IGPost("a", "testsources1", "thumb3", "url3", "caption1", 1, 2)
        assertEquals(emptyList(), db.getAllIgPost("testuser1"))
        db.addIGPost(igpost1.shortcode, igpost1.ord, 1, igpost1.thumb, igpost1.url, igpost1.caption, igpost1.timestamp)
        db.addIGPost(igpost2.shortcode, igpost2.ord, 1, igpost2.thumb, igpost2.url, igpost2.caption, igpost2.timestamp)
        db.addIGPost(igpost3.shortcode, igpost3.ord, 1, igpost3.thumb, igpost3.url, igpost3.caption, igpost3.timestamp)
        val results1 = db.getAllIgPost("testuser1")
        assertEquals(3, results1.size)
        assertTrue(results1.contains(igpost1))
        assertTrue(results1.contains(igpost2))
        assertTrue(results1.contains(igpost3))
    }
}