$(document).ready(function() {
    var teacher = $("#selectTeacher").val();
    $("#teacher").val(teacher);
});

$("#teacher").change(function() {
    var sd = $("#sd").val();
    var ed = $("#ed").val();
    if((Date.parse(sd)) && (Date.parse(ed))) {
        window.location = "/managers/attendance/byDate/"+this.value+"?startDate="+sd+"&endDate="+ed;
    } else {
        alert("Please enter valid To and From dates to filter by teacher.");
    }
});