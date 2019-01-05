package com.greboid.scraper

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.io.File

internal class ConfigTest {

    @Test
    fun `test loading valid config file`() {
        val reader = File(this.javaClass.getResource("example.yml").toURI())
        assertNotNull(reader)
        val config = getConfig(reader)
        assertNotNull(config)
    }

    @Test
    fun `test loading invalid config file`() {
        val config = getConfig(File(this.javaClass.getResource("broken.yml").toURI()))
        assertNull(config)
    }

    @Test
    fun `test loading database from valid file`() {
        val config = getConfig(File(this.javaClass.getResource("example.yml").toURI())) as Config
        assertEquals("jdbc:sqlite:database.sqlite", config.db)
    }

    @Test
    fun `test loading database from valid file without database`() {
        val config = getConfig(File(this.javaClass.getResource("nodb.yml").toURI())) as Config
        assertNull(config.db)
    }
}