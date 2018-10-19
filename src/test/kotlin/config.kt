package com.greboid.scraper

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.io.File

internal class ConfigTest {

    @Test
    fun `test loading database from valid file`() {
        val config = getConfig(File(this.javaClass.getResource("example.yml").toURI()).reader()) as Config
        assertEquals("database.sqlite", config.db)
    }

    @Test
    fun `test loading profiles from valid file`() {
        val config: Config = getConfig(File(this.javaClass.getResource("example.yml").toURI()).reader()) as Config
        assertEquals(2, config.profiles.size)
        assertNotNull(config.profiles.get("animals"))
        assertNotNull(config.profiles.get("stuff"))
        assertNull(config.profiles.get("test"))
        assertEquals(3, config.profiles.get("animals")!!.size)
        assertEquals(4, config.profiles.get("stuff")!!.size)
    }

    @Test
    fun `test loading database from invalid file`() {
        val config = getConfig(File(this.javaClass.getResource("broken.yml").toURI()).reader())
        assertNull(config)
    }

    @Test
    fun `test no profiles in file`() {
        val config = getConfig(File(this.javaClass.getResource("noprofiles.yml").toURI()).reader())
        assertEquals(2, config)
    }
}