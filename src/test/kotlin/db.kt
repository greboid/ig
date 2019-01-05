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

internal class DbTest {

    companion object {

        private lateinit var mysqld: EmbeddedMysql
        internal val config = Config(dbhost="localhost", dbport = 2215)
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
}