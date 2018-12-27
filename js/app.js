initial = true;
scrolling = false;
finished = false;

$('#app').before('<div id="menu"></div>')

$.getJSON($(location).attr('protocol') + '//' + $(location).attr('host') + '/profiles', {}, function(data) {
  $.each(data, function(index, profile) {
    if ($(location).attr('pathname').substr(8) == profile) {
      $('#menu').append('<li><a href="/'+profile+'" class="active">'+profile+'</a></li>')
    } else {
      $('#menu').append('<li><a href="/'+profile+'">'+profile+'</a></li>')
    }
  })
})

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
  if (initial) {
    count = count * Math.floor($(window).height() / imgwidth)
  }
  $.getJSON($(location).attr('protocol') + '//' + $(location).attr('host') + '/feed?start='+offset+'&count='+count+'&profile='+$(location).attr('pathname').substr(1), {}, function(data) {
    var images = [];
    $.each(data, function(index, image) {
      images.push(image);
    })
    if (images.length > 0) {
      finished = true;
    }
    $.each(images, function(index, image) {
      $('#app').append($('<a data-fancybox="images" class="item" title="'+image.source+'" data-shortcode="'+image.shortcode+'" data-source="'+image.source+'" data-caption="<a href=\'https://instagram.com/'+image.source+'\'>'+image.source+'</a> - <a href=\'https://instagram.com/p/'+image.shortcode+'\'>'+image.shortcode+'</a><br>'+image.caption+'" href="' + image.url + '"><img class="itemimage" src="' + image.thumb + '"/></a>'));
    })
  }).done(function() {
    initial = false;
    scrolling = false;
    if ($('#app').height() < $(window).height() || !finished) {
      getImages()
    }
  })
}


