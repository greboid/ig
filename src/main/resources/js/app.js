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
        window.location.hash = 'close'
    }
});

$(document).keyup(function(e) {
     if (e.key === "ArrowLeft") {
     var hash = $(location).attr('hash')
         if (hash == '#close' || hash == '#') {
             return
         }
         var box = $(hash).closest('.post').prev('.post').find('.perfundo__overlay').attr('id')
         if (box !==  void 0) {
            window.location.hash = box
         } else {
            window.location.hash = 'close'
         }
    }
});

$(document).keyup(function(e) {
     if (e.key === "ArrowRight") {
        var hash = $(location).attr('hash')
        if (hash == '#close' || hash == '#') {
            return
        }
        var box = $(hash).closest('.post').next('.post').find('.perfundo__overlay').attr('id')
        if (box !== void 0) {
            window.location.hash = box
        } else {
            window.location.hash = 'close'
        }
    }
});
