package com.greboid.scraper

import com.google.gson.*
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.IOException
import java.lang.reflect.Type
import java.math.BigInteger
import java.net.URL
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import kotlin.math.min
import kotlin.streams.toList

const val ig: String = "https://www.instagram.com"

public class Instagram(db: Database) {

}

fun addUsertoDatabase() {
}

fun getProfile(username: String): Profile? {
    val doc = try {
        Jsoup.connect("$ig/$username").get()
    } catch (e: IOException) {
        return null
    }
    val jsonData = doc.select("script:containsData(window._sharedData)").find { element ->
        element.data().startsWith("window._sharedData")
    }?.data()?.substringBeforeLast(';')?.substringAfter("=")?.trim()
    val gson = GsonBuilder()
            .registerTypeAdapter(User::class.java, AnnotatedDeserializer<User>())
            .create()
    val data = gson.fromJson(jsonData, InstagramSharedData::class.java)
    val userData = data.entry_data.ProfilePage.first().graphql.user
    return Profile(
            userData.username,
            userData.id,
            userData.biography,
            userData.external_url,
            userData.profile_pic_url,
            userData.profile_pic_url_hd,
            userData.edge_owner_to_timeline_media.edges.stream().map {
                getPost(it.node.shortcode)
            }.toList().filterNotNull().toMutableList(),
            data.rhx_gis,
            userData.edge_owner_to_timeline_media.page_info.end_cursor,
            userData.edge_owner_to_timeline_media.page_info.has_next_page,
            userData.edge_owner_to_timeline_media.count
    )
}

fun getPost(shortcode: String?): Post? {
    val json = try {
        Jsoup.connect("$ig/p/$shortcode").get()
    } catch (e: IOException) {
        return null
    }.toData()
    val data = Gson().fromJson(json, InstagramSharedData::class.java)
            .entry_data.PostPage.first().graphql.shortcode_media
    return Post(
            data.id,
            data.getPostType(),
            data.shortcode,
            data.display_url,
            data.edge_media_to_caption.edges.first().node.text,
            data.owner.id,
            data.owner.username
    )
}

class Profile(
        val username: String,
        val id: String,
        val biography: String,
        val external_url: URL?,
        val profile_pic_url: URL,
        val profile_pic_url_hd: URL?,
        val posts: MutableList<Post>,
        val rhx_gis: String,
        var end_cursor: String?,
        var hasMore: Boolean,
        val count: Int
) {
    fun backfill(desiredCapacity: Int) {
        val targetCapacity = min(desiredCapacity, count)
        val count = 12;
        val fullruns: Int = (targetCapacity - posts.size) / count
        val partRuns: Int = (targetCapacity - posts.size) % count
        if (fullruns <= 0 && partRuns <= 0) {
            return
        }
        for (i in fullruns downTo 1) {
            getOlderData(count)
        }
        getOlderData(partRuns)
    }

    private fun getMD5(value: String): String {
        return String.format("%032x", BigInteger(1, MessageDigest.getInstance("MD5")
                        .apply { update(StandardCharsets.UTF_8.encode(value)) }
                        .digest()))
    }

    private fun getOlderData(count: Int) {
        val soup = Jsoup.connect("$ig/graphql/query/")
        soup.header("X-Instagram-GIS",
                getMD5("$rhx_gis:{\"id\":\"$id\",\"first\":${count},\"after\":\"$end_cursor\"}")
        )
        soup.data("query_hash", "5b0222df65d7f6659c9b82246780caa7")
        soup.data("variables", "{\"id\":\"$id\",\"first\":${count},\"after\":\"$end_cursor\"}")
        val json = soup.ignoreContentType(true).execute().body()
        val data = Gson().fromJson(json, InstagramData::class.java)
        end_cursor = data.data.user.edge_owner_to_timeline_media.page_info.end_cursor
        hasMore = data.data.user.edge_owner_to_timeline_media.page_info.has_next_page
        posts.addAll(data.data.user.edge_owner_to_timeline_media.edges.stream().map {
            getPost(it.node.shortcode)
        }.toList().filterNotNull())
    }
}

fun Document.toData(): String? {
    return select("script:containsData(window._sharedData)").find { element ->
        element.data().startsWith("window._sharedData")
    }?.data()?.substringBeforeLast(';')?.substringAfter("=")?.trim()
}

class Post(
        val id: String,
        val type: PostType,
        val shortcode: String,
        val displayURL: URL,
        val caption: String,
        val ownerID: String,
        val ownerUsername: String
)

enum class PostType {
    IMAGE,
    VIDEO,
    SIDECAR,
    UNKNOWN
}

internal fun shortcode_media.getPostType(): PostType {
    return when (__typename) {
        "GraphImage" -> PostType.IMAGE
        "GraphSidecar" -> PostType.SIDECAR
        "GraphVideo" -> PostType.VIDEO
        else -> PostType.UNKNOWN
    }
}

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
internal annotation class JsonRequired

internal class AnnotatedDeserializer<T> : JsonDeserializer<T> {

    @Throws(JsonParseException::class)
    override fun deserialize(je: JsonElement, type: Type, jdc: JsonDeserializationContext): T {
        val pojo = Gson().fromJson<T>(je, type) ?: throw IllegalArgumentException("")
        val fields = (pojo as Any)::class.java.declaredFields
        for (f in fields) {
            if (f.getAnnotation(JsonRequired::class.java) != null) {
                try {
                    f.isAccessible = true
                    if (f.get(pojo) == null) {
                        throw JsonParseException("Missing field in JSON: " + f.name)
                    }
                } catch (ex: IllegalArgumentException) {
                    return pojo
                } catch (ex: IllegalAccessException) {
                    return pojo
                }

            }
        }
        return pojo
    }
}

internal class InstagramData {
    @JsonRequired
    lateinit var data: data
}

internal class data {
    @JsonRequired
    lateinit var user: User
}

internal class InstagramSharedData {
    @JsonRequired
    lateinit var rhx_gis: String
    @JsonRequired
    lateinit var entry_data: EntryData
}

internal class EntryData {
    @JsonRequired
    lateinit var ProfilePage: List<ProfilePage>
    @JsonRequired
    lateinit var PostPage: List<PostPage>
}

internal class PostPage {
    @JsonRequired
    lateinit var graphql: postgraphql
}

internal class postgraphql {
    @JsonRequired
    lateinit var shortcode_media: shortcode_media
}

internal class shortcode_media {
    @JsonRequired
    lateinit var id: String
    @JsonRequired
    lateinit var __typename: String
    @JsonRequired
    lateinit var shortcode: String
    @JsonRequired
    lateinit var display_url: URL
    @JsonRequired
    lateinit var edge_media_to_caption: edge_media_to_caption
    @JsonRequired
    lateinit var owner: owner
}

internal class owner {
    @JsonRequired
    lateinit var id: String
    @JsonRequired
    lateinit var username: String
}

internal class ProfilePage {
    @JsonRequired
    lateinit var graphql: graphql
}

internal class graphql {
    @JsonRequired
    lateinit var user: User
}

internal class User {
    @JsonRequired
    lateinit var edge_owner_to_timeline_media: edge_owner_to_timeline_media
    @JsonRequired
    lateinit var username: String
    @JsonRequired
    lateinit var id: String
    @JsonRequired
    lateinit var biography: String
    var external_url: URL? = null
    @JsonRequired
    lateinit var profile_pic_url: URL
    var profile_pic_url_hd: URL? = null
}

internal class edge_owner_to_timeline_media {
    @JsonRequired
    lateinit var edges: List<nodeHolder>
    @JsonRequired
    lateinit var page_info: page_info
    @JsonRequired
    var count: Int = 0

}

internal class nodeHolder {
    @JsonRequired
    lateinit var node: node
}

internal class node {
    @JsonRequired
    lateinit var id: String
    @JsonRequired
    lateinit var __typename: String
    @JsonRequired
    lateinit var edge_media_to_caption: edge_media_to_caption
    @JsonRequired
    lateinit var shortcode: String
    @JsonRequired
    lateinit var display_url: URL
    @JsonRequired
    lateinit var thumbnail_src: URL
}

internal class edge_media_to_caption {
    @JsonRequired
    lateinit var edges: List<captionnodeHolder>
}

internal class page_info {
    var count: Int = 0
    @JsonRequired
    var has_next_page: Boolean = false
    var end_cursor: String? = null
}

internal class captionnodeHolder {
    @JsonRequired
    lateinit var node: captionnode
}

internal class captionnode {
    @JsonRequired
    lateinit var text: String
}