$(function () {

    /**
     * 发送邮箱
     */
    $("#sendMobileCode").click(function() {
        var data = {
            destination: $("#user-email").val()
        }
        $.ajax({
            type: "POST",
            contentType: "application/json;",
            url: domain + "api/message/sendEmailMessage",
            data: JSON.stringify(data),
            success: function(result){
                if (result.status == 0) {
                    xw.alert("发送成功")
                } else {
                    xw.alert("发送失败")
                }
            },
            error: function () {
                console.log("网络错误！");
            }
        });
    });

    /**
     *  修改邮箱
     */
    $(".am-btn-save").click(function () {
        var data = {
            email: $("#user-email").val(),
            code: $("#user-code").val()
        }
        $.ajax({
            type: 'PUT',
            contentType: "application/json;",
            url: domain + "api/user/changeEmail",
            data: JSON.stringify(data),
            success: function(result){
                if(result.status == 0) {
                    $(".m-progress-list").find("em").removeClass("bg").toggleClass("bg2");
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
