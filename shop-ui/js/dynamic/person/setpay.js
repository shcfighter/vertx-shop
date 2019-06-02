$(function () {
    $.ajax({
        type: 'GET',
        contentType: "application/json;",
        url: domain + "api/user/getUserInfo",
        success: function(result){
            if(result.status == 0) {
                var user = result.items;
                if(null == user.mobile || "" == user.mobile){
                    xw.confirm("未绑定手机号码，请先绑定手机号!",function(){
                        window.location.href = "bindphone.html";
                    },function(){});
                }
                $("#user-phone").html(result.items.mobile);
            } else {
                xw.alert(result.message)
            }
        },
        error: function () {
            console.log("网络异常");
        }
    });
    /**
     * 发送手机验证码
     */
    $("#sendMobileCode").click(function() {
        var data = {
            destination: $("#user-phone").html()
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

    $("#setPayPwd").click(function() {
        var data = {
            mobile: $("#user-phone").html(),
            code: $("#user-code").val(),
            pay_pwd: $("#user-password").val(),
            confirm_pwd: $("#user-confirm-password").val(),
        }
        $.ajax({
            type: "PUT",
            contentType: "application/json;",
            url: domain + "api/account/setPayPwd",
            data: JSON.stringify(data),
            success: function(result){
                if (result.status == 0) {
                    xw.alert("设置支付密码成功")
                } else {
                    xw.alert(result.message)
                }
            },
            error: function () {
                console.log("网络错误！");
            }
        });
    });
});