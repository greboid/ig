package com.greboid.scraper

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.io.File

internal class ConfigTest {

    @Test
    fun `test loading valid config file`() {
        val reader = File(this.javaClass.getResource("example.yml").toURI()).reader()
        assertNotNull(reader)
        val config = getConfig(reader)
        assertNotNull(config)
    }

    @Test
    fun `test loading invalid config file`() {
        val config = getConfig(File(this.javaClass.getResource("broken.yml").toURI()).reader())
        assertNull(config)
    }

    @Test
    fun `test loading database from valid file`() {
        val config = getConfig(File(this.javaClass.getResource("example.yml").toURI()).reader()) as Config
        assertEquals("jdbc:sqlite:database.sqlite", config.database)
    }

    @Test
    fun `test loading database from valid file without database`() {
        val config = getConfig(File(this.javaClass.getResource("nodb.yml").toURI()).reader()) as Config
        assertNull(config.database)
    }
}