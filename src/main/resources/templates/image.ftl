<div class="post">
  <a class="perfundo__link item" href="#${image.shortcode}${image.ord}">
    <img class="itemimage" src="/${image.thumb}" alt="${image.caption?html}">
  </a>
  <div id="${image.shortcode}${image.ord}" class="perfundo__overlay fadeInLeft">
    <figure class="perfundo__content perfundo__figure">
        <#if image.url?contains(".mp4")>
            <video controls>
                <source src="${image.url}" type="video/mp4">
            </video>
        <#else>
            <img src="/${image.thumb}">
            <div class="perfundo__image" style="background-image: url(${image.url});"></div>
        </#if>
        <figcaption class="perfundo__figcaption">
        <a href="/user/${image.source}">${image.source}</a> - <a href="https://instagram.com/p/${image.shortcode}">${image.shortcode}</a>
        <br>
        ${image.caption?html}
      </figcaption>
    </figure>
    <a href="#close" class="perfundo__close perfundo__control">Close</a>
  </div>
</div>
