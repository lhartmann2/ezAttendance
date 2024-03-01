$(document).ready(function() {

    setup();

    $(".presentCheck").change(function() {
        var thisID = this.id.match(/\d/g).join("");
        var checkValue = $(this).is(':checked');
        var presentLabel = $("#checkLabel" + thisID);
        var exDiv = $("#exDiv" + thisID);
        checkValue ? presentLabel.text("Present") : presentLabel.text("Not Present");
        if($(this).is(':checked')) {
            exDiv.hide(250);
        } else {
            exDiv.show(250);
        }
    });

    $(".excusedCheck").change(function() {
        var thisID = this.id.match(/\d/g).join("");
        var checkValue = $("#exCheck" + thisID).is(':checked');
        var exLabel = $("#exLabel" + thisID);
        checkValue ? exLabel.text("Excused Absence") : exLabel.text("Not Excused");
    });
});

function setup() {
    var length = $("#length").val();

    if(length > 0) {
        for(var i = 0; i < length; i++) {

            var presentLabel = $("#checkLabel" + i);
            var exDiv = $("#exDiv" + i);
            var exLabel = $("#exLabel" + i);
            var checkValue = $("#check"+i).is(':checked');
            var exCheckValue = $("#exCheck"+i).is(':checked');

            if(checkValue) {
                presentLabel.text("Present");
                exDiv.hide();
            } else {
                presentLabel.text("Not Present");
                exDiv.show();
            }

            if(exCheckValue) {
                exLabel.text("Excused Absence");
            } else {
                exLabel.text("Not Excused");
            }
        }
    }
};