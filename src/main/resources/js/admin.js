$.getJSON($(location).attr('protocol') + '//' + $(location).attr('host') + '/users', {}, function(data) {
    $.each(data, function(index, user) {
        $("#userList").append("<li class=\"list-group-item\" data-name=\""+user+"\">"+user+" <a class=\"remove\" href=\"\">Remove</a></li>");
        $("#userSelect").append("<option value=\""+user+"\">"+user+"</option>");
        $($("#userSelect").find("option")[0]).attr('selected','selected');
        $($("#userSelect").find("option")[0]).change();
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
$('#saveUsers').on('click', function(event) {
    var users = [];
    $('#userList').find('li').each(function(index) {
        users.push($(this).data('name'));
    });
    $.postJSON( "/admin/users", JSON.stringify(users), function() {
        $($("#userSelect").find("option")[0]).change();
    });
});
$('#saveProfiles').on('click', function(event) {
    var profiles = [];
    $('#profileList').find('li').each(function(index) {
        profiles.push($(this).data('name'));
    });
    $.postJSON( "/admin/profiles", JSON.stringify(profiles), function() {
        $($("#userSelect").find("option")[0]).change();
    });
});
$('#saveProfileUsers').on('click', function(event) {
    var profiles = new Object()
    profiles.selected = $($(userSelect).find(":selected")[0]).val()
    var selectedProfiles = [];
    $(profileSelect).find(":selected").each(function(index) {
        selectedProfiles.push($(this).text());
    });
    profiles.profiles = selectedProfiles
    $.postJSON( "/admin/ProfileUsers", JSON.stringify(profiles))
});
$('#userSelect').change(function() {
    $.getJSON($(location).attr('protocol') + '//' + $(location).attr('host') + '/profiles', {}, function(data) {
        $('#profileSelect option').remove();
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
    var parentDiv = $(event.target).closest('div[class="col"]')[0];
    var input = $(parentDiv).find("input[type=text]");
    var list = $(parentDiv).find("ul");
    var select = $('#userSelect');
    var text = $(input).val().trim().toLowerCase();
    var known = false;
    $(list).find("li").each(function() {
        if ($(this).data('name') == text) {
            known = true;
        }
    });
    if (text != "" && !known) {
        $(list).append("<li class=\"list-group-item\" data-name=\""+text+"\">"+text+" <a class=\"remove\" href=\"\">Remove</a></li>");
        $(select).append("<option value=\""+text+"\" data-name=\""+text+"\">"+text+"</option>")
        $(input).val('')
    }
    var listElems = $(list).find('li').detach().sort(function (a, b) {
        return ($(a).data("name") < $(b).data("name") ? -1 : $(a).data("name") > $(b).data("name") ? 1 : 0);
    });
    $(list).append(listElems);
    var selectOptions = $(select).find('option');
    var selected = $(select).find(":selected").text();
    selectOptions.sort(function(a,b) {
        if (a.text > b.text) return 1;
        if (a.text < b.text) return -1;
        return 0
    });
    $(select).empty().append(selectOptions);
    $(select).filter(function () { return $(this).html() == selected; }).attr('selected', 'selected');
}
$.postJSON = function(url, data, callback) {
    return jQuery.ajax({
        'type': 'POST',
        'url': url,
        'contentType': 'application/json',
        'data': data,
        'dataType': 'json',
        'success': callback
    });
};
