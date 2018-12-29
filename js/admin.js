$.getJSON($(location).attr('protocol') + '//' + $(location).attr('host') + '/users', {}, function(data) {
    $.each(data, function(index, user) {
        $("#userList").append("<li class=\"list-group-item\" data-name=\""+user+"\">"+user+" <a class=\"remove\" href=\"\">Remove</a></li>");
        $("#userSelect").append("<option value=\""+user+"\">"+user+"</option>");
    })
})
$.getJSON($(location).attr('protocol') + '//' + $(location).attr('host') + '/profiles', {}, function(data) {
    $.each(data, function(index, profile) {
        $("#profileList").append("<li class=\"list-group-item\" data-name=\""+profile+"\">"+profile+" <a class=\"remove\" href=\"\">Remove</a></li>");
    })
})
$('#addUser').on('click', function(event) {
    addItem(event);
    event.preventDefault();
});
$('#addProfile').on('click', function(event) {
    addItem(event);
    event.preventDefault();
});
$(document).on('click','a.remove',function(event){
    $(event.target).parent().remove();
    event.preventDefault();
});
$('#userSelect').change(function() {
    $.getJSON($(location).attr('protocol') + '//' + $(location).attr('host') + '/profiles', {}, function(data) {
        $.each(data, function(index, profile) {
            $("#profileSelect").append("<option value=\""+profile+"\">"+profile+"</option>");
        })
    })
    $.getJSON($(location).attr('protocol') + '//' + $(location).attr('host') + '/userprofiles/'+$(this).val(), {}, function(data) {
        $.each(data, function(index, profile) {
            $("#profileSelect").find("option[value=\""+profile+"\"]").attr('selected','selected');
        })
    })
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
        $(list).append("<li class=\"list-group-item\" data-name=\""+text+"\">"+text+" <a class=\"remove\" href=\"\">Remove</a></li>");
    }
    var elems = $(list).find('li').detach().sort(function (a, b) {
        return ($(a).data("name") < $(b).data("name") ? -1 : $(a).data("name") > $(b).data("name") ? 1 : 0);
    });
    $(list).append(elems);
    $(input).val('')
}