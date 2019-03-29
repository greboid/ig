package com.greboid.scraper

interface Retriever {
    suspend fun start()
    suspend fun stop()
    fun retrieveAll()
    fun retrieve(identifier: String)
}
