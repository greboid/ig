package com.greboid.scraper

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
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
    fun `test using mocked connection`() {
        val (db, _, statement) = getStatement()
        db.init()
        Database.Schema.createAllTables.forEach {
            verify(statement).executeUpdate(it)
        }
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
        val (db, connection, statement) = getPreparedStatement()
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