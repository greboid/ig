package com.greboid.scraper

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class InstagramTest {

    @Test
    fun `test get profile`() {
        val profile = getProfile("instagram")
        assertNotNull(profile)
        assertTrue(profile?.posts.isNullOrEmpty())
    }

    @Test
    fun `test get profile with posts`() {
        val profile = getProfile("instagram")
        assertNotNull(profile)
        assertFalse(profile?.posts.isNullOrEmpty())
        profile?.backfill(49)
        println(profile?.posts?.size)
    }
}