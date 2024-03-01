$(document).ready(function() {
    var selected = $("#selectType").val();
    $("#show").val(selected);
});

$("#show").change(function() {
    if(this.value == 0) {
        window.location = "/managers/classes/";
    } else if(this.value == 1) {
        window.location = "/managers/classes/past";
    } else if(this.value == 2) {
        window.location = "/managers/classes/future";
    } else if(this.value == 3) {
        window.location = "/managers/classes/all";
    }
});