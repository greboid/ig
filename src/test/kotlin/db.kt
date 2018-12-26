package com.greboid.scraper

import com.nhaarman.mockitokotlin2.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.lang.IllegalStateException
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.Statement

internal class DbTest {

    @Test
    fun `test connect with invalid url`() {
        assertThrows(IllegalStateException::class.java) {
            Database("foo").connect()
        }
    }

    @Test
    fun `test connect without username or password returns a connection`() {
        assertTrue(Database("jdbc:sqlite::memory:").connect() is Connection)
    }

    @Test
    fun `test connect without password returns a connection`() {
        assertTrue(Database("jdbc:sqlite::memory:", "bar").connect() is Connection)
    }

    @Test
    fun `test connect returns a connection`() {
        assertTrue(Database("jdbc:sqlite::memory:", "bar", "baz").connect() is Connection)
    }

    @Test
    fun `test using mocked connection`() {
        val (db, _,statement) = getStatement()
        db.init()
        verify(statement).executeUpdate(Database.Schema.createAllTables)
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
    fun `test add profile`() {
        val (db, connection,statement) = getPreparedStatement()
        val name = "test"
        db.addProfile(name)
        verify(connection).prepareStatement(Database.Schema.addProfile)
        verify(statement).setString(1, name)
        verify(statement).executeUpdate()
        verify(statement).close()
        verifyNoMoreInteractions(statement)
    }

    @Test
    fun `del profile`() {
        val (db, connection, statement) = getPreparedStatement()
        val name = "test"
        db.delProfile(name)
        verify(connection).prepareStatement(Database.Schema.delProfile)
        verify(statement).setString(1, name)
        verify(statement).executeUpdate()
        verify(statement).close()
        verifyNoMoreInteractions(statement)
    }

    @Test
    fun `get profiles`() {
        val (db, _, _) = getPreparedStatement()
        assertTrue(db.getProfiles().isEmpty())
    }

    private fun getPreparedStatement(): Triple<Database, Connection, PreparedStatement> {
        val connection = mock<Connection>()
        val statement = mock<PreparedStatement>()
        whenever(connection.createStatement()).thenReturn(statement)
        whenever(connection.prepareStatement(any())).thenReturn(statement)
        val db = Database("")
        db.setConnection(connection)
        return Triple(db, connection, statement)
    }

    private fun getStatement(): Triple<Database, Connection, Statement> {
        val connection = mock<Connection>()
        val statement = mock<Statement>()
        whenever(connection.createStatement()).thenReturn(statement)
        val db = Database("")
        db.setConnection(connection)
        return Triple(db, connection, statement)
    }
}