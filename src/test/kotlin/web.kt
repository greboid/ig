package com.greboid.scraper


import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class WebTest {

    @Test
    fun `test get url`() {
        val result = getUrl("https://httpbin.org/base64/dGVzdA==")
        assertEquals("test", result.get())
    }

    @Test
    fun `test get url with headers`() {
        val result = getUrl("https://icanhazdadjoke.com/j/GlGBIY0wAAd", mapOf("Accept" to "text/plain"))
        assertEquals("How much does a hipster weigh? An instagram.", result.get())
    }
}