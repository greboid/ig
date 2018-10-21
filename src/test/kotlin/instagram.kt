package com.greboid.scraper

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class InstagramTest {

    @Test
    fun `test get profile`() {
        val profile = getProfile("instagram")
        assertNotNull(profile)
    }

}