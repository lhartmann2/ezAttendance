var oldText = "";

$(document).ready(function() {
    $('#sP').multiselect({
        disableIfEmpty: true,
        disabledText: 'No Students Found',
        enableFiltering: true,
        enableResetButton: true,
        enableCaseInsensitiveFiltering: true,
        includeFilterClearBtn: true,
        widthSynchronizationMode: 'ifPopupIsSmaller',
        maxHeight: 300,
        buttonWidth: '250px'
    });
});

$("#cancelBtn").on("click", function() {
    getConfirmation();
});

$("#submitBtn").on("click", function() {
    if(($("#fn").val().length > 1)) {
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
    window.location = "/managers/classes/";
};
