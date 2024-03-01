$("#deleteBtn").on("click", function() {
    if(confirm("WARNING: Really delete this student?\nAll associated attendance records will also be deleted.")) {
        var tId = $("#tId").val();
        $(this).removeClass("btn").removeClass("btn-danger").addClass("spinner-border").addClass("text-danger");
        $(this).text("");
        window.location = "/managers/students/delete/" + tId;
    }
});