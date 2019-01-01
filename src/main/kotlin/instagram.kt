package com.greboid.scraper

import com.google.gson.*
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.IOException
import java.math.BigInteger
import java.net.URL
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import kotlin.math.min
import kotlin.streams.toList

const val ig: String = "https://www.instagram.com"

class Instagram {

    fun getUserProfile(username: String): Profile? {
        return getProfile(username)
    }
}

internal fun getProfile(username: String): Profile? {
    val doc = try {
        Jsoup.connect("$ig/$username").get()
    } catch (e: IOException) {
        return null
    }
    val jsonData = doc.select("script:containsData(window._sharedData)").find { element ->
        element.data().startsWith("window._sharedData")
    }?.data()?.substringBeforeLast(';')?.substringAfter("=")?.trim()
    val data = Gson().fromJson(jsonData, InstagramSharedData::class.java)
    val userData = data.entry_data.ProfilePage.first().graphql.user
    return Profile(
            userData.username,
            userData.id,
            userData.biography,
            userData.external_url,
            userData.profile_pic_url,
            userData.profile_pic_url_hd,
            userData.edge_owner_to_timeline_media.edges.stream().map {
                getShortcodePost(it.node.shortcode)
            }.toList().filterNotNull().toMutableList(),
            userData.edge_owner_to_timeline_media.page_info.end_cursor,
            userData.edge_owner_to_timeline_media.page_info.has_next_page,
            userData.edge_owner_to_timeline_media.count,
            data.rhx_gis
    )
}

internal fun getShortcodePost(shortcode: String?): Post? {
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
            when (data.getPostType()) {
                PostType.SIDECAR -> data.edge_sidecar_to_children?.edges?.map {
                    it.node.display_url
                }?.toList() ?: emptyList()
                PostType.VIDEO -> listOf(data.video_url ?: URL("http://instagram.com"))
                else -> listOf(data.display_url)
            },
            data.edge_media_to_caption.edges.firstOrNull()?.node?.text ?: "",
            data.owner.id,
            data.owner.username,
            data.taken_at_timestamp
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
        private var end_cursor: String?,
        private var hasMore: Boolean,
        private val count: Int,
        private val rhx_gis: String
) {
    fun backfill(desiredCapacity: Int) {
        val targetCapacity = min(desiredCapacity, count)
        val count = 12
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
                getMD5("$rhx_gis:{\"id\":\"$id\",\"first\":$count,\"after\":\"$end_cursor\"}")
        )
        soup.data("query_hash", "5b0222df65d7f6659c9b82246780caa7")
        soup.data("variables", "{\"id\":\"$id\",\"first\":$count,\"after\":\"$end_cursor\"}")
        val json = soup.ignoreContentType(true).execute().body()
        val data = Gson().fromJson(json, InstagramData::class.java)
        end_cursor = data.data.user.edge_owner_to_timeline_media.page_info.end_cursor
        hasMore = data.data.user.edge_owner_to_timeline_media.page_info.has_next_page
        posts.addAll(data.data.user.edge_owner_to_timeline_media.edges.stream().map {
            getShortcodePost(it.node.shortcode)
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
        val thumbnailURL: URL,
        val displayURL: List<URL>,
        val caption: String,
        val ownerID: String,
        val ownerUsername: String,
        val timestamp: Int
)

enum class PostType {
    IMAGE,
    VIDEO,
    SIDECAR,
    UNKNOWN
}

internal fun ShortcodeMedia.getPostType(): PostType {
    return when (__typename) {
        "GraphImage" -> PostType.IMAGE
        "GraphSidecar" -> PostType.SIDECAR
        "GraphVideo" -> PostType.VIDEO
        else -> PostType.UNKNOWN
    }
}

internal data class InstagramData(val data: Data)

internal data class Data(val user: User)

internal data class InstagramSharedData(
        val rhx_gis: String,
        val entry_data: EntryData
)

internal data class EntryData(
        val ProfilePage: List<ProfilePage>,
        val PostPage: List<PostPage>
)

internal data class PostPage(
        val graphql: PostgraphQL
)

internal data class PostgraphQL(
        val shortcode_media: ShortcodeMedia
)

internal data class ShortcodeMedia(
        val id: String,
        val __typename: String,
        val shortcode: String,
        val display_url: URL,
        val video_url: URL?,
        val edge_media_to_caption: EdgeMediaToCaption,
        val edge_sidecar_to_children: EdgeSidecarToChildren?,
        val owner: Owner,
        val taken_at_timestamp: Int
)

internal data class EdgeSidecarToChildren(
        val edges: List<SideCarNodeHolder>
)

internal data class SideCarNodeHolder(
        val node: Node
)

internal data class Owner(
        val id: String,
        val username: String
)

internal data class ProfilePage(
        val graphql: GraphQL
)

internal data class GraphQL(
        val user: User
)

internal data class User(
        val edge_owner_to_timeline_media: EdgeOwnerToTimelineMedia,
        val username: String,
        val id: String,
        val biography: String,
        val external_url: URL?,
        val profile_pic_url: URL,
        val profile_pic_url_hd: URL?
)

internal data class EdgeOwnerToTimelineMedia(
        val edges: List<NodeHolder>,
        val page_info: PageInfo,
        val count: Int
)

internal data class NodeHolder(
        val node: Node
)

internal data class Node(
        val id: String,
        val __typename: String,
        val edge_media_to_caption: EdgeMediaToCaption,
        val shortcode: String,
        val display_url: URL,
        val video_url: URL?,
        val thumbnail_src: URL
)

internal data class EdgeMediaToCaption(
        val edges: List<CaptionNodeHolder>
)

internal data class PageInfo(
        val count: Int,
        val has_next_page: Boolean,
        val end_cursor: String?
)

internal data class CaptionNodeHolder(
        val node: CaptionNode
)

internal data class CaptionNode(
        val text: String
)