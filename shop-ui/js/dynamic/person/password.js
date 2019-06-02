$(function () {
    /**
     *  修改密码
     */
    $(".am-btn-danger").click(function () {
        var data = {
            original_pwd: $("#user-old-password").val(),
            pwd: $("#user-new-password").val(),
            confirm_pwd: $("#user-confirm-password").val()
        }
        $.ajax({
            type: 'PUT',
            contentType: "application/json;",
            url: domain + "api/user/changepwd",
            data: JSON.stringify(data),
            success: function(result){
                if(result.status == 0) {
                    $(".u-stage-icon-inner").find("em").removeClass("bg").toggleClass("bg2");
                    xw.alert(result.message)
                } else {
                    xw.alert(result.message)
                }
            },
            error: function () {
                console.log("网络异常");
            }
        });
    });

});
