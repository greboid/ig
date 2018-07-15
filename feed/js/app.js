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
  if ($(window).width() / parseFloat($("body").css("font-size")) > 120) {
    imgwidth = 200;
  } else {
    imgwidth = 100;
  }
  count = Math.floor($(window).width() / imgwidth)
  console.log($(location).attr('protocol'))
  $.getJSON($(location).attr('protocol') + '//' + $(location).attr('host') + '/feed?start='+offset+'&count='+count, {}, function(data) {
    $.each(data, function(index, image) {
      $('#app').append($('<a data-fancybox="images" class="item" title="'+image.source+'" data-source="'+image.source+'" data-caption="'+image.source+' - '+image.shortcode+'<br>'+image.caption+'" href="' + image.url + '"><img class="itemimage" src="' + image.thumb + '"/></a>'));
    })
  }).done(function() {
    scrolling = false;
    if ($('#app').height() < $(window).height()) {
      getImages()
    }
  })
}


