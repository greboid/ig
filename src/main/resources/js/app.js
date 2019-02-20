getImages()

function getImages() {
  offset = $("#app img").length
  var images = [];
  $.getJSON($(location).attr('protocol') + '//' + $(location).attr('host') + '/igposts?start='+0+'&count='+50+'&profile='+$(location).attr('pathname').substr(1), {}, function(data) {
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