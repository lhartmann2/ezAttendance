$("#deleteBtn").on("click", function() {
    if(confirm("WARNING: Really delete this parent?")) {
        var tId = $("#tId").val();
        $(this).removeClass("btn").removeClass("btn-danger").addClass("spinner-border").addClass("text-danger");
        $(this).text("");
        window.location = "/managers/parents/delete/" + tId;
    }
});