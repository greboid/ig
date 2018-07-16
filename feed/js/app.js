scrolling = false;

$('#app').before('<div id="menu"></div>')

$.getJSON($(location).attr('protocol') + '//' + $(location).attr('host') + '/profiles', {}, function(data) {
  $.each(data, function(index, profile) {
    active = $(location).attr('pathname').substr(1) == profile.name
    console.log(active)
    if ($(location).attr('pathname').substr(1) == profile.name) {
      $('#menu').append('<li><a href="/'+profile.name+'" class="active">'+profile.name+'</a></li>')
    } else {
      $('#menu').append('<li><a href="/'+profile.name+'">'+profile.name+'</a></li>')
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
  $.getJSON($(location).attr('protocol') + '//' + $(location).attr('host') + '/feed?start='+offset+'&count='+count+'&profile='+$(location).attr('pathname').substr(1), {}, function(data) {
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


