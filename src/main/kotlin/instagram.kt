package com.greboid.scraper

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import org.jsoup.Jsoup
import java.net.URL
import java.util.stream.Collectors
import kotlin.streams.toList

const val ig: String = "https://www.instagram.com"

fun getProfile(username: String): Profile {
    val doc = Jsoup.connect("${ig}/${username}").get()
    val jsonData = doc.select("script:containsData(window._sharedData)").find { element ->
        element.data().startsWith("window._sharedData")
    }?.data()?.substringBeforeLast(';')?.substringAfter("=")?.trim()
    val data = Gson().fromJson(jsonData, InstagramSharedData::class.java)
    return Profile(
            data.entry_data.ProfilePage[0].graphql.user.username,
            data.entry_data.ProfilePage[0].graphql.user.id,
            data.entry_data.ProfilePage[0].graphql.user.biography,
            data.entry_data.ProfilePage[0].graphql.user.external_url,
            data.entry_data.ProfilePage[0].graphql.user.profile_pic_url,
            data.entry_data.ProfilePage[0].graphql.user.profile_pic_url_hd,
            data.entry_data.ProfilePage[0].graphql.user.edge_owner_to_timeline_media.edges.stream()
                    .map { getPost(it.node.shortcode) }.toList()
    )
}

fun getPost(shortcode: String): Post {
    val doc = Jsoup.connect("${ig}/p/${shortcode}").get()
    val jsonData = doc.select("script:containsData(window._sharedData)").find { element ->
        element.data().startsWith("window._sharedData")
    }?.data()?.substringBeforeLast(';')?.substringAfter("=")?.trim()
    val data = Gson().fromJson(jsonData, InstagramSharedData::class.java)
    return Post(
            data.entry_data.PostPage[0].graphql.shortcode_media.id,
            when (data.entry_data.PostPage[0].graphql.shortcode_media.__typename) {
                "GraphImage" -> PostType.IMAGE
                "GraphSidecar" -> PostType.SIDECAR
                "GraphVideo" -> PostType.VIDEO
                else -> PostType.UNKNOWN
            },
            data.entry_data.PostPage[0].graphql.shortcode_media.shortcode,
            data.entry_data.PostPage[0].graphql.shortcode_media.display_url,
            data.entry_data.PostPage[0].graphql.shortcode_media.edge_media_to_caption.edges[0].node.text,
            data.entry_data.PostPage[0].graphql.shortcode_media.owner.id,
            data.entry_data.PostPage[0].graphql.shortcode_media.owner.username
    )
}

class Profile(
        val username: String,
        val id: String,
        val biography: String,
        val external_url: URL,
        val profile_pic_url: URL,
        val profile_pic_url_hd: URL,
        val posts: List<Post>
)

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

internal class InstagramSharedData {
    lateinit var rhx_gis: String
    lateinit var entry_data: EntryData
}

internal class EntryData {
    var ProfilePage: List<ProfilePage> = emptyList()
    var PostPage: List<PostPage> = emptyList()
}

internal class PostPage {
    lateinit var graphql: postgraphql
}

internal class postgraphql {
    lateinit var shortcode_media: shortcode_media
}

internal class shortcode_media {
    lateinit var id: String
    lateinit var __typename: String
    lateinit var shortcode: String
    lateinit var display_url: URL
    lateinit var edge_media_to_caption: edge_media_to_caption
    lateinit var owner: owner
}

internal class owner {
    lateinit var id: String
    lateinit var username: String
}

internal class ProfilePage {
    lateinit var graphql: graphql
}

internal class graphql {
    lateinit var user: User
}

internal class User {
    lateinit var edge_owner_to_timeline_media: edge_owner_to_timeline_media
    lateinit var username: String
    lateinit var id: String
    lateinit var biography: String
    lateinit var external_url: URL
    lateinit var profile_pic_url: URL
    lateinit var profile_pic_url_hd: URL
}

internal class edge_owner_to_timeline_media {
    lateinit var edges: List<nodeHolder>

}

internal class nodeHolder {
    lateinit var node: node
}

internal class node {
    lateinit var id: String
    lateinit var __typename: String
    lateinit var edge_media_to_caption: edge_media_to_caption
    lateinit var shortcode: String
    lateinit var display_url: URL
    lateinit var thumbnail_src: URL
}

internal class edge_media_to_caption {
    lateinit var page_info: page_info
    lateinit var edges: List<captionnodeHolder>
}

internal class page_info {
    var count: Int = 0
    var has_next_page: Boolean = false
    lateinit var end_cursor: String
}

internal class captionnodeHolder {
    lateinit var node: captionnode
}

internal class captionnode {
    @SerializedName("text")
    lateinit var text: String
}