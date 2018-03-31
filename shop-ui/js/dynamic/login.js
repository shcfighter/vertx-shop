/**
 * Created by za-wangshenhua on 2018/2/27.
 */
$(function() {
    /**
     * 登录
     */
    $(".login").click(function() {
        var data = {
            loginName: $("input[name='loginName']").val(),
            pwd: $("input[name='pwd']").val()
        }
        $.ajax({
            type: 'POST',
            contentType: "application/json;",
            url: domain + "api/user/login",
            data: JSON.stringify(data),
            success: function(result){
                if(result.status == 0){
                    sessionStorage.setItem("loginUser", JSON.stringify(result.items));
                    window.location.href = "/index.html";
                } else {
                    alert(result.message);
                }
            },
            error: function () {
                alert("网路异常");
            }
        });
    });

    /**
     * 注册
     */
    $(".register").click(function() {
        var registerDiv = $(this).closest(".am-tab-panel");
        var type = $(this).attr("name");
        var data = {
            type: type,
            loginName: registerDiv.find("input[name='loginName']").val(),
            password: registerDiv.find("input[name='password']").val(),
            passwordConfirm: registerDiv.find("input[name='passwordConfirm']").val()
        }
        if(type == "mobile"){
            data["code"] = registerDiv.find("input[name='code']").val();
        }
        $.ajax({
            type: 'POST',
            contentType: "application/json;",
            url: domain + "api/user/register",
            data: JSON.stringify(data),
            success: function(result){
                if(result.status == 0){
                    alert("注册成功！");
                    window.location.href = "/index.html";
                } else {
                    alert(result.message);
                }
            },
            error: function () {
                alert("网路异常");
            }
        });
    });

    //手机注册验证码
    $("#sendMobileCode").click(function() {
        var registerDiv = $(this).closest(".am-tab-panel");
        var data = {
            destination: registerDiv.find("input[name='loginName']").val()
        }
        $.ajax({
            type: 'POST',
            contentType: "application/json;",
            url: domain + "api/message/insertMessage",
            data: JSON.stringify(data),
            success: function(result){
                if (result.status == 0) {
                    alert("发送成功");
                } else {
                    alert("发送失败");
                }
            },
            error: function () {

            }
        });
    });
});