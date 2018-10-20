package com.greboid.scraper

import com.nhaarman.mockitokotlin2.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.function.Executable
import java.lang.IllegalStateException
import java.sql.Connection
import java.sql.Statement

internal class DbTest {

    @Test
    fun `test connect with invalid url`() {
        assertNull(Database("rar").connect())
    }

    @Test
    fun `test connect without username or password returns a connection`() {
        assertTrue(Database("jdbc:sqlite:foo").connect() is Connection)
    }

    @Test
    fun `test connect without password returns a connection`() {
        assertTrue(Database("jdbc:sqlite:foo", "bar").connect() is Connection)
    }

    @Test
    fun `test connect returns a connection`() {
        assertTrue(Database("jdbc:sqlite:foo", "bar", "baz").connect() is Connection)
    }

    @Test
    fun `test using mocked connection`() {
        val connection = mock<Connection>()
        val statement = mock<Statement>()
        whenever(connection.createStatement()).thenReturn(statement)
        val db = Database("")
        db.setConnection(connection)
        db.initTables()
        verify(statement).executeUpdate(Schema.createAllTables)
    }
    @Test
    fun `test connect when already connected`() {
        val connection = mock<Connection>()
        val statement = mock<Statement>()
        whenever(connection.createStatement()).thenReturn(statement)
        val db = Database("")
        db.setConnection(connection)
        assertThrows(IllegalStateException::class.java) { db.connect() }
    }

    @Test
    fun `test init without connection`() {
        val connection = mock<Connection>()
        val statement = mock<Statement>()
        whenever(connection.createStatement()).thenReturn(statement)
        val db = Database("")
        assertThrows(IllegalStateException::class.java) { db.initTables() }
    }
}