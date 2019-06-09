$(function () {
    $.ajax({
        type: 'GET',
        contentType: "application/json;",
        url: domain + "api/uaa",
        success: function(result){
            if(result.status == 2001){
                localStorage.removeItem("loginUser");
                $(".login_user").html("<a href=\"/login.html\" target=\"_top\" class=\"h\">亲，请登录</a>  <a href=\"/register.html\" target=\"_top\">免费注册</a>");
            }
        },
        error: function () {
            console.log("网络异常");
        }
    });
});