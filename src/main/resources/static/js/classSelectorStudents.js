var studentId;
var sType;

$(document).ready(function() {
    var selected = $("#selectType").val();
    $("#show").val(selected);
    studentId = $("#sId").val();
    sType = $("#origSelectType").val();
});

$("#show").change(function() {
    if(this.value == 0) {
        window.location = "/managers/students/classes/" + studentId + "/" +sType;
    } else if(this.value == 1) {
        window.location = "/managers/students/classes/past/" + studentId + "/" + sType;
    } else if(this.value == 2) {
        window.location = "/managers/students/classes/future/" + studentId + "/" + sType;
    } else if(this.value == 3) {
        window.location = "/managers/students/classes/all/" + studentId + "/" + sType;
    }
});