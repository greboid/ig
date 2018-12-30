package com.greboid.scraper

import java.io.File
import java.net.URL
import javax.imageio.ImageIO
import com.mortennobel.imagescaling.AdvancedResizeOp
import com.mortennobel.imagescaling.ResampleOp

class Retriever {

    suspend fun start(database: Database, instagram: Instagram) {
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
                    post.displayURL.forEachIndexed { index, url ->
                        val out = File("thumbs/${post.shortcode}$index.jpg")
                        thumbnail(url, out)
                        database.addMedia(post.shortcode, index, userID, out.toString(),
                                url.toString(), post.caption, post.timestamp)
                    }
                } else {
                    val out = File("thumbs/${post.shortcode}.jpg")
                    thumbnail(post.thumbnailURL, out)
                    database.addMedia(post.shortcode, 0, userID, out.toString(),
                            post.displayURL.first().toString(), post.caption, post.timestamp)
                }
            }
        }
    }
}

private fun thumbnail(input: URL, output: File) {
    output.parentFile.mkdirs()
    val source = ImageIO.read(input)
    val widthRatio = 200.toDouble() / source.width
    val heightRatio = 200.toDouble() / source.height
    val ratio = Math.min(widthRatio, heightRatio)
    val resampleOp = ResampleOp((source.width * ratio).toInt(), (source.height * ratio).toInt())
    resampleOp.unsharpenMask = AdvancedResizeOp.UnsharpenMask.Normal
    val scaled = resampleOp.filter(source, null)
    ImageIO.write(scaled, "jpg", output)
}