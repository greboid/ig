package com.greboid.scraper

import com.google.common.jimfs.Configuration
import com.google.common.jimfs.Jimfs
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Paths


internal class ConfigTest {

    @Test
    fun `test loading valid config file`() {
        val config = getConfig(Paths.get(this.javaClass.getResource("example.yml").toURI())) as Config
        assertNotNull(config)
    }

    @Test
    fun `test loading database from valid file`() {
        val config = getConfig(Paths.get(this.javaClass.getResource("example.yml").toURI())) as Config
        assertEquals("ig", config.db)
    }

    @Test
    fun `test loading database from valid file without database`() {
        val config = getConfig(Paths.get(this.javaClass.getResource("nodb.yml").toURI())) as Config
        assertEquals("jdbc:sqlite:database.sqlite", config.db)
    }

    @Test
    fun `test default values`() {
        val path = Jimfs.newFileSystem(Configuration.unix()).getPath("/tmp")
        Files.createDirectories(path)
        val file = path.resolve("test.yml")
        createDefault(file)
        val config = getConfig(file) as Config
        assertEquals("ig", config.db)
        assertEquals("database", config.dbhost)
        assertEquals("ig", config.dbpassword)
        assertEquals(3306, config.dbport)
        assertEquals("ig", config.dbuser)
        assertEquals("admin", config.adminPassword)
        assertEquals("admin", config.adminUsername)
        assertEquals("9e424e10e3dcd2f4fdd8d811c54aa36c", config.sessionKey)
        assertEquals(80, config.webPort)

    }
}