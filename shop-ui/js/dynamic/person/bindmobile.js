$(function () {

    /**
     * 发送邮箱
     */
    $("#sendMobileCode").click(function() {
        var data = {
            destination: $("#user-new-phone").val()
        }
        $.ajax({
            type: "POST",
            contentType: "application/json;",
            url: domain + "api/message/sendMobileMessage",
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
     *  修改手机号
     */
    $(".am-btn-save").click(function () {
        var data = {
            mobile: $("#user-new-phone").val(),
            code: $("#user-new-code").val()
        }
        $.ajax({
            type: 'PUT',
            contentType: "application/json;",
            url: domain + "api/user/bindMobile",
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
