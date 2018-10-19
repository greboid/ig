package com.greboid.scraper

import com.nhaarman.mockitokotlin2.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.mockito.internal.matchers.Null
import java.io.File
import java.sql.Connection
import java.sql.Statement

internal class DbTest {

    @Test
    fun `test connect gets file path`() {
        val file = mock<File>()
        val db = Database(file)
        db.connect()
        verify(file).absoluteFile
        verifyNoMoreInteractions(file)
    }

    @Test
    fun `test connect returns a connection`() {
        assertTrue(Database(mock()).connect() is Connection)
    }

    @Test
    fun `test using mocked connection`() {
        val connection = mock<Connection>()
        val statement = mock<Statement>()
        whenever(connection.createStatement()).thenReturn(statement)
        val db = Database(mock())
        db.setConnection(connection)
        db.initTables()
        verify(statement).executeUpdate(Schema.createAllTables)
    }
}