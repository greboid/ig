<#ftl output_format="XML">
<?xml version="1.0" encoding="UTF-8" ?>
<rss version="2.0" xmlns:atom="http://www.w3.org/2005/Atom">

<channel>
<atom:link href="${url}" rel="self" type="application/rss+xml" />
<title>${rss.title}</title>
<description>${rss.description}</description>
<link>${rss.link}</link>

    <#list feedItems as feedItem>
        <item>
        <title>${feedItem.caption}</title>
        <description>${feedItem.caption}</description>
        <link>${feedItem.url}</link>
        <guid isPermaLink="false">${feedItem.shortcode}${feedItem.ord}</guid>
        <pubDate>${feedItem.date}</pubDate>
        </item>
    </#list>

</channel>
</rss>