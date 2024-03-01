var teacherId;

$(document).ready(function() {
    var selected = $("#selectType").val();
    $("#show").val(selected);
    teacherId = $("#tId").val();
});

$("#show").change(function() {
    if(this.value == 0) {
        window.location = "/managers/teachers/classes/" + teacherId;
    } else if(this.value == 1) {
        window.location = "/managers/teachers/classes/past/" + teacherId;
    } else if(this.value == 2) {
        window.location = "/managers/teachers/classes/future/" + teacherId;
    } else if(this.value == 3) {
        window.location = "/managers/teachers/classes/all/" + teacherId;
    }
});