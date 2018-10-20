package com.greboid.scraper

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import org.jsoup.Jsoup
import java.net.URL

val ig: String = "https://www.instagram.com"

fun getProfile(username: String) {
    val doc = Jsoup.connect("${ig}/${username}").get()
    val jsonData = doc.select("script:containsData(window._sharedData)").find {
        element ->  element.data().startsWith("window._sharedData")
    }?.data()?.substringBeforeLast(';')?.substringAfter("=")?.trim()
    val data = Gson().fromJson(jsonData, InstagramSharedData::class.java)
    println(data.entry_data.ProfilePage[0]
            .graphql.user.edge_owner_to_timeline_media.edges[0]
            .node.__typename)
    println(data.entry_data.ProfilePage[0]
            .graphql.user.edge_owner_to_timeline_media.edges[0]
            .node.edge_media_to_caption.edges[0].node.text)
}

fun getPost(shortcode: String) {
    val doc = Jsoup.connect("${ig}/p/${shortcode}").get()
    val jsonData = doc.select("script:containsData(window._sharedData)").find {
        element ->  element.data().startsWith("window._sharedData")
    }?.data()?.substringBeforeLast(';')?.substringAfter("=")?.trim()
    val data = Gson().fromJson(jsonData, InstagramSharedData::class.java)
    println(data.entry_data.PostPage[0].graphql.shortcode_media.owner.username)
    println(data.entry_data.PostPage[0].graphql.shortcode_media.display_url)
    println(data.entry_data.PostPage[0].graphql.shortcode_media.edge_media_to_caption.edges[0].node.text)
}

class Profile(
        val username: String,
        val id: String
)

class Post() {

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
    var id: Int = 0
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
    lateinit var edges: List<captionnodeHolder>
}

internal class captionnodeHolder {
    lateinit var node: captionnode
}

internal class captionnode {
    @SerializedName("text")
    lateinit var text: String
}