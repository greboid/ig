getImages()

function getImages() {
  offset = $("#app .item img").length
  var images = [];
  $.getJSON($(location).attr('protocol') + '//' + $(location).attr('host') + '/igposts?start='+offset+'&count='+(offset+150)+'&profile='+$(location).attr('pathname').substr(10), {}, function(data) {
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
  })
}

$(document).keyup(function(e) {
     if (e.key === "Escape") {
        window.location.href = '#close'
    }
});
