package com.greboid.scraper

interface Retriever {
    suspend fun start()
    suspend fun retrieveAll()
    suspend fun retrieve(identifier: String)
}
