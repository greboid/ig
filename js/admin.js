$(function() {
    $('#addUser').on('click', function(event) {
        addItem(event);
        event.preventDefault();
    });
    $('#addProfile').on('click', function(event) {
            addItem(event);
            event.preventDefault();
        });
});

function addItem(event) {
    var parentDiv = $(event.target).closest("div")[0]
    var input = $(parentDiv).find("input[type=text]")
    var list = $(parentDiv).find("ul")
    var text = $(input).val().trim().toLowerCase();
    var known = false
    $(list).find("li").each(function() {
        if ($(this).text() == text) {
            known = true;
        }
    });
    if (text != "" && !known) {
        $(list).append("<li>"+text+"</li>");
    }
    var elems = $(list).find('li').detach().sort(function (a, b) {
      return ($(a).text() < $(b).text() ? -1
            : $(a).text() > $(b).text() ? 1 : 0);
    });
    $(list).append(elems);
}