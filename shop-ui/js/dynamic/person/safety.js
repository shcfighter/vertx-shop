$(function () {
    $.ajax({
        type: 'GET',
        contentType: "application/json;",
        url: domain + "api/user/findUserCertified",
        success: function(result){
            if(result.status == 0) {
                $.each(result.items, function (index, value) {

                })
            }
        },
        error: function () {
            console.log("网络异常");
        }
    });
});