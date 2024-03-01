var oldText = "";
var selectActive = 0;
var selectType = -1;

$(document).ready(function() {
    var alertShown = $('#alertShown').val();
    if(alertShown<1) {
        alert("WARNING: Any changes made to Start Date, End Date or Day of Week will cause all associated attendance data to be reset!\nProceed with caution!");
    }

    selectActive = $('#selectActive').val();
    selectType = $('#selectType').val();

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
    var loc = "/managers/classes/";
    if(selectActive>0) {
        if(selectType == 1) {
            loc = "/managers/classes/future";
        } else if(selectType == 2) {
            loc = "/managers/classes/past";
        } else if(selectType == 3) {
            loc = "/managers/classes/all";
        }
    }
    window.location = loc;
};
