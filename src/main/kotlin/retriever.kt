package com.greboid.scraper

interface Retriever {
    suspend fun start()
    suspend fun stop()
    suspend fun retrieveAll()
    suspend fun retrieve(identifier: String)
    suspend fun backfill(identifier: String, capacity: Int)
}
