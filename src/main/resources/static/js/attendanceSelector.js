var selected = 0;

$(document).ready(function() {
    selected = $("#selectType").val();
    var teacher = $("#selectTeacher").val();
    $("#show").val(selected);
    $("#teacher").val(teacher);
});

$("#show").change(function() {
    var tId = $("#teacher").val();

    if(this.value == 0) {
        window.location = "/managers/attendance/" + tId;
    } else if(this.value == 1) {
        window.location = "/managers/attendance/thisWeek/" + tId;
    } else if(this.value == 2) {
        window.location = "/managers/attendance/thisMonth/" + tId;
    } else if(this.value == 3) {
        window.location = "/managers/attendance/byDate/" + tId + "/" + selected;
    }
});

$("#teacher").change(function() {
    var tId = this.value;
    var sVal = $("#selectType").val();

    if(sVal == 0) {
            window.location = "/managers/attendance/" + tId;
        } else if(sVal == 1) {
            window.location = "/managers/attendance/thisWeek/" + tId;
        } else if(sVal == 2) {
            window.location = "/managers/attendance/thisMonth/" + tId;
        } else if(sVal == 3) {
            window.location = "/managers/attendance/byDate/" + tId + "/" + sVal;
        }
});