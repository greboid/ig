<a class="item" data-toggle="lightbox" data-gallery="gallery" data-footer="&lt;p&gt;&lt;a href=&quot;/user/${image.source}&quot;&gt;${image.source}&lt;/a&gt; - &lt;a href=&quot;https://instagram.com/p/${image.shortcode}&quot;&gt;${image.shortcode}&lt;/a&gt;&lt;p&gt;&lt;p&gt;${image.caption?html}&lt;/p&gt;"
    <#if image.url?contains(".mp4")>
        href="/video/${image.shortcode}"
    <#else>
        href="${image.url}"
    </#if>
>
    <img class="itemimage" src="/${image.thumb}" alt="${image.caption?html}">
</a>
