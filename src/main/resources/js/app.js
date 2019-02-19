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
  $.getJSON($(location).attr('protocol') + '//' + $(location).attr('host') + '/igposts?start='+offset+'&count='+count+'&profile='+$(location).attr('pathname').substr(1), {}, function(data) {
    var images = [];
    $.each(data, function(index, image) {
      images.push(image);
    })
    if (images.length == 0) {
      finished = true;
    }
    var promises = images.map(function (image, index) {
        return $.get(
            $(location).attr('protocol') + '//' + $(location).attr('host') + '/template/image/'+image.shortcode
        );
    });
    $.when.apply($, promises).done(function() {
        for (var i = 0; i < arguments.length; i++) {
        console.log($($(arguments[i][0]).find('.perfundo__link')[0]).attr("href"))
            $('#app').append(arguments[i][0])
        }
    });
  }).done(function() {
    initial = false;
    scrolling = false;
    if ($('#app').height() < $(window).height() && !finished) {
      getImages()
    }
  })
}