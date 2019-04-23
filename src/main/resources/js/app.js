$(document).on('click', '[data-toggle="lightbox"]', function(event) {
                event.preventDefault();
                $(this).ekkoLightbox({
                    wrapping: false
                });
            });

getImages()

function getImages() {
  scrolling = true;
  offset = $("#app .item img").length
  var images = [];
  if (location.pathname.split('/')[1] == 'user') {
    var url = $(location).attr('protocol') + '//' + $(location).attr('host') + '/igposts?start='+offset+'&count='+(offset+150)+'&user='+location.pathname.split('/')[2]
  } else {
    var url = $(location).attr('protocol') + '//' + $(location).attr('host') + '/igposts?start='+offset+'&count='+(offset+150)+'&profile='+location.pathname.split('/')[2]
  }
  $.getJSON(url, {}, function(data) {
    $.each(data, function(index, image) {
      images.push(image);
     })
  }).done(function() {
        var promises = images.map(function (image, index) {
            return $.get(
                $(location).attr('protocol') + '//' + $(location).attr('host') + '/template/image/'+image.shortcode+'/'+image.ord
            );
        });
        $.when.apply($, promises).done(function() {
            for (var i = 0; i < arguments.length; i++) {
                $('#app').append(arguments[i][0])
            }
        });
        scrolling = false;
  })
}

scrolling = false;

$( window ).resize(function() {
  if (!scrolling && $('#app').height() < $(window).height()) {
    getImages()
  }
});
$(window).scroll(function() {
  if (!scrolling && ($(window).scrollTop() + $(window).height() == $(document).height())) {
    getImages()
  }
});
