$("#deleteBtn").on("click", function() {
    if(confirm("WARNING: Really delete this teacher?\nAll associated classes and attendance records will also be deleted!")) {
        var tId = $("#tId").val();
        $(this).removeClass("btn").removeClass("btn-danger").addClass("spinner-border").addClass("text-danger");
        $(this).text("");
        window.location = "/managers/teachers/delete/" + tId;
    }
});