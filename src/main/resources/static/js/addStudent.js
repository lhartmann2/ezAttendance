var oldText = "";

$("#cancelBtn").on("click", function() {
    var fName = $("#fn").val().length;
    var lName = $("#ln").val().length;
    var nName = $("#nn").val().length;
    var phone = $("#ph").val().length;
    var allergies = $("#allergies").val().length;
    var notes = $("#notes").val().length;
    if((fName + lName + nName + phone + allergies + notes) > 0) {
        getConfirmation();
    } else {
        cancel();
    }
});

$("#submitBtn").on("click", function() {
    if(($("#fn").val().length > 1) && ($("#ln").val().length > 1)) {
        $(this).removeClass('btn').removeClass('btn-primary').addClass('spinner-border').addClass('text-primary');
        oldText = $(this).text();
        $(this).text("");
        setTimeout(revert, 2000);
    }
});

function revert() {
    $("#submitBtn").removeClass('spinner-border').removeClass('text-primary').addClass('btn').addClass('btn-primary');
    $("#submitBtn").text(oldText);
};

function getConfirmation() {
    if(confirm("Warning: All changes will be lost.")) {
        cancel();
    }
};

function cancel() {
    window.location = "/managers/students/";
};
