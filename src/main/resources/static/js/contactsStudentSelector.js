$(document).ready(function() {
    var selected = $("#selectType").val();
    $("#show").val(selected);
});

$("#show").change(function() {
    if(this.value == 0) {
        window.location = "/admins/studentContacts/0";
    } else if(this.value == 1) {
        window.location = "/admins/studentContacts/1";
    }
});