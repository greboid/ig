package com.greboid.scraper

import com.google.gson.Gson
import org.jsoup.Jsoup
import java.io.IOException
import java.net.URL
import kotlin.streams.toList

const val ig: String = "https://www.instagram.com"

fun getProfile(username: String): Profile? {
    val doc = try {
        Jsoup.connect("$ig/$username").get()
    } catch (e: IOException) {
        return null
    }
    val jsonData = doc.select("script:containsData(window._sharedData)").find { element ->
        element.data().startsWith("window._sharedData")
    }?.data()?.substringBeforeLast(';')?.substringAfter("=")?.trim()
    val data = Gson().fromJson(jsonData, InstagramSharedData::class.java)
            .checkProfileData()?.entry_data?.ProfilePage?.firstOrNull()?.graphql?.user ?: return null
    return Profile(
            data.username as String,
            data.id as String,
            data.biography as String,
            data.external_url,
            data.profile_pic_url,
            data.profile_pic_url_hd,
            data.edge_owner_to_timeline_media!!.edges!!.stream().map {
                getPost(it.node!!.shortcode)
            }.toList().filterNotNull()
    )
}

fun getPost(shortcode: String?): Post? {
    val doc = try {
        Jsoup.connect("$ig/p/$shortcode").get()
    } catch (e: IOException) {
        return null
    }
    val jsonData = doc.select("script:containsData(window._sharedData)").find { element ->
        element.data().startsWith("window._sharedData")
    }?.data()?.substringBeforeLast(';')?.substringAfter("=")?.trim() ?: return null
    val data = Gson().fromJson(jsonData, InstagramSharedData::class.java)
            .checkPostData()?.entry_data?.PostPage?.firstOrNull()?.graphql?.shortcode_media ?: return null
    return Post(
            data.id as String,
            when (data.__typename as String) {
                "GraphImage" -> PostType.IMAGE
                "GraphSidecar" -> PostType.SIDECAR
                "GraphVideo" -> PostType.VIDEO
                else -> PostType.UNKNOWN
            },
            data.shortcode as String,
            data.display_url as URL,
            data.edge_media_to_caption?.edges?.firstOrNull()?.node?.text as String,
            data.owner?.id as String,
            data.owner?.username as String
    )
}

internal fun InstagramSharedData.checkProfileData(): InstagramSharedData? {
    val entryData: EntryData = this.entry_data ?: return null
    val user: User = entryData.ProfilePage.firstOrNull()?.graphql?.user ?: return null
    user.username ?: return null
    user.id ?: return null
    user.profile_pic_url_hd = user.profile_pic_url_hd ?: user.profile_pic_url
    user.edge_owner_to_timeline_media?.edges ?: return null
    user.edge_owner_to_timeline_media?.edges = user.edge_owner_to_timeline_media?.edges?.stream()
            ?.filter{it.node?.shortcode != null}?.toList() ?: emptyList()
    return this
}

internal fun InstagramSharedData.checkPostData(): InstagramSharedData? {
    val entryData: EntryData = this.entry_data ?: return null
    val media: shortcode_media = entryData.PostPage.firstOrNull()?.graphql?.shortcode_media ?: return null
    media.__typename ?: return null
    media.shortcode ?: return null
    media.display_url ?: return null
    media.edge_media_to_caption?.edges?.firstOrNull()?.node?.text ?: return null
    media.owner?.id ?: return null
    media.owner?.username ?: return null
    return this
}

class Profile(
        val username: String,
        val id: String,
        val biography: String,
        val external_url: URL?,
        val profile_pic_url: URL?,
        val profile_pic_url_hd: URL?,
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
    var rhx_gis: String? = null
    var entry_data: EntryData? = null
}

internal class EntryData {
    var ProfilePage: List<ProfilePage> = emptyList()
    var PostPage: List<PostPage> = emptyList()
}

internal class PostPage {
    var graphql: postgraphql? = null
}

internal class postgraphql {
    var shortcode_media: shortcode_media? = null
}

internal class shortcode_media {
    var id: String? = null
    var __typename: String? = null
    var shortcode: String? = null
    var display_url: URL? = null
    var edge_media_to_caption: edge_media_to_caption? = null
    var owner: owner? = null
}

internal class owner {
    var id: String? = null
    var username: String? = null
}

internal class ProfilePage {
    var graphql: graphql? = null
}

internal class graphql {
    var user: User? = null
}

internal class User {
    var edge_owner_to_timeline_media: edge_owner_to_timeline_media? = null
    var username: String? = null
    var id: String? = null
    var biography: String? = null
    var external_url: URL? = null
    var profile_pic_url: URL? = null
    var profile_pic_url_hd: URL? = null
}

internal class edge_owner_to_timeline_media {
    var edges: List<nodeHolder>? = null

}

internal class nodeHolder {
    var node: node? = null
}

internal class node {
    var id: String? = null
    var __typename: String? = null
    var edge_media_to_caption: edge_media_to_caption? = null
    var shortcode: String? = null
    var display_url: URL? = null
    var thumbnail_src: URL? = null
}

internal class edge_media_to_caption {
    var page_info: page_info? = null
    var edges: List<captionnodeHolder> = emptyList()
}

internal class page_info {
    var count: Int = 0
    var has_next_page: Boolean = false
    var end_cursor: String? = null
}

internal class captionnodeHolder {
    var node: captionnode? = null
}

internal class captionnode {
    var text: String? = null
}