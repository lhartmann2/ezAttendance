$(document).ready(function() {
    var selected = $("#selectType").val();
    $("#show").val(selected);
});

$("#show").change(function() {
    if(this.value == 0) {
        window.location = "/managers/teachers/";
    } else if(this.value == 1) {
        window.location = "/managers/teachers/sortLast";
    }
});