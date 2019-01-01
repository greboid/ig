package com.greboid.scraper

interface Retriever {
    suspend fun start(database: Database, config: Config)
}
