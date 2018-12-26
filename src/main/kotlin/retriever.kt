package com.greboid.scraper

class Retriever {

    fun start(database: Database, instagram: Instagram) {
        val users = sequence {
            for (user in database.getUsers()) {
                println("Getting user: $user")
                yield(instagram.getUserProfile(user))
            }
        }.filterNotNull()
        users.filterNotNull().forEach {
            println("Getting posts for: ${it.username}")
            val userID = database.getUserID(it.username)
                    ?: run { println("Unable to get id for user: ${it.username}"); return }
            it.posts.forEach {
                println("Getting post: ${it.shortcode}")
                database.addMedia(it.shortcode, userID, it.displayURL.toString(), it.displayURL.toString(), it.caption, it.timestamp)
            }
        }
    }
}