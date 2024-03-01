var oldText = "";

$("#cancelBtn").on("click", function() {
    var fName = $("#fn").val().length;
    var lName = $("#ln").val().length;
    var phone = $("#ph").val().length;
    var email = $("#email").val().length;
    var street = $("#st").val().length;
    var city = $("#cty").val().length;
    var zip = $("#zip").val().length;
    var other = $("#other").val().length;
    if((fName + lName + phone + email + street + city + zip + other) > 0) {
        getConfirmation();
    } else {
        cancel();
    }
});

$("#submitBtn").on("click", function() {
    if(($("#fn").val().length > 1) && ($("#ln").val().length > 1) && ($("#ph").val().length > 1)) {
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
    window.location = "/managers/parents/";
};
