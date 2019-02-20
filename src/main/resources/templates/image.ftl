  <a class="perfundo__link item" href="#${image.shortcode}${image.ord}">
    <img class="itemimage" src="${image.thumb}" alt="${image.caption}">
  </a>
  <div id="${image.shortcode}${image.ord}" class="perfundo__overlay fadeInLeft">
    <figure class="perfundo__content perfundo__figure">
        <#if image.url?contains(".mp4")>
            <video controls>
                <source src="${image.url}" type="video/mp4">
            </video>
        <#else>
            <img src="${image.url}">
        </#if>
        <figcaption class="perfundo__figcaption">
        <a href="https://instagram.com/${image.source}">${image.source}</a> - <a href="https://instagram.com/p/${image.shortcode}">${image.shortcode}</a>
        <br>${image.caption}
      </figcaption>
    </figure>
    <a href="#" class="perfundo__close perfundo__control">Close</a>
  </div>