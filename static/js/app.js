scrolling = false;

getImages()
$(window).scroll(function() {
  if (!scrolling && ($(window).scrollTop() + $(window).height() == $(document).height())) {
    getImages()
  }
});
$( window ).resize(function() {
  if (!scrolling && $('#app').height() < $(window).height()) {
    getImages()
  }
});

function getImages() {
  scrolling = true;
  offset = $("#app img").length
  $.getJSON('/feed?start='+offset+'&count=19', {}, function(data) {
    $.each(data, function(index, image) {
      $('#app').append($('<a data-fancybox="images" class="item" title="'+image.source+'" data-source="'+image.source+'" data-caption="'+image.source+' - '+image.shortcode+'<br>'+image.caption+'" href="' + image.url + '"><img class="itemimage" alt="'+image.source+'" src="' + image.thumb + '"/></a>'));
    })
  }).done(function() {
    scrolling = false;
    if ($('#app').height() < $(window).height()) {
      getImages()
    }
  })
}


