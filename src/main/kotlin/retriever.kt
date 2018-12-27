package com.greboid.scraper

class Retriever {

    fun start(database: Database, instagram: Instagram) {
        val users = sequence {
            for (user in database.getUsers()) {
                yield(instagram.getUserProfile(user))
            }
        }.filterNotNull()
        users.filterNotNull().forEach {
            val userID = database.getUserID(it.username)
                    ?: run { println("Unable to get id for user: ${it.username}"); return }
            it.posts.forEach { post ->
                if (post.type == PostType.SIDECAR) {
                    post.displayURL.forEachIndexed{ index, url ->
                        database.addMedia(post.shortcode, index, userID, url.toString(),
                                url.toString(), post.caption, post.timestamp)
                    }
                } else {
                    database.addMedia(post.shortcode, 0, userID, post.displayURL.first().toString(),
                            post.displayURL.first().toString(), post.caption, post.timestamp)
                }
            }
        }
    }
}