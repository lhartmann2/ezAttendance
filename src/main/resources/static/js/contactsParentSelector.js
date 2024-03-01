var selected;
var classSelected;

$(document).ready(function() {
    selected = $("#selectType").val();
    classSelected = $("#classSelected").val();
    $("#show").val(selected);
    $("#showClass").val(classSelected);
});

$("#show").change(function() {
    if(this.value == 0) {
        window.location = "/admins/parentContacts/0/"+classSelected;
    } else if(this.value == 1) {
        window.location = "/admins/parentContacts/1/"+classSelected;
    }
});

$("#showClass").change(function() {
    if($("#selectType").val() == 0) {
        window.location = "/admins/parentContacts/0/"+this.value;
    } else if($("#selectType").val() == 1) {
        window.location = "/admins/parentContacts/1/"+this.value;
    }
});