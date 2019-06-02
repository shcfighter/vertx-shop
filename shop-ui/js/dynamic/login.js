/**
 * Created by shwang on 2018/2/27.
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
                    //alert(result.message);
                    xw.alert(result.message)
                }
            },
            error: function () {
                console.log("网络错误！");
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
                    xw.confirm("注册成功!",function(){
                        window.location.href = "/index.html";
                    },function(){});
                } else {
                    xw.alert(result.message)
                }
            },
            error: function () {
                console.log("网络错误！");
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
     * enter键自动提交
     */
    $("input[name='passwordConfirm'], input[name=\"pwd\"]").bind('keypress', function(event){
        if(event.keyCode == 13){
            if(window.location.pathname.indexOf("login") > 0){
                $(".login").click();
            } else if (window.location.pathname.indexOf("register") > 0) {
                $(event.currentTarget).closest(".am-tab-panel").find(".am-btn-primary").click();
            }
        }
    });

})
