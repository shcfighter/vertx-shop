$(function () {
    $.ajax({
        type: 'GET',
        contentType: "application/json;",
        url: domain + "api/user/getUserInfo",
        success: function(result){
            if(result.status == 0) {
                console.log(result);
                var items = result.items;
                $("#login_name").val(items.login_name);
                $("#user_name").val(items.user_name);
                $("#user_phone").val(items.mobile);
                $("#user_email").val(items.email);
            }
        },
        error: function () {
            console.log("网络异常");
        }
    });

    $(".info-btn").click(function () {
        var data = {
            login_name: "张三",
            user_name: "lisi",
            mobile: "15868157542",
            email: "1234@qq.com",
            sex: 1
        }
        $.ajax({
            type: 'POST',
            contentType: "application/json;",
            url: domain + "api/user/saveUserInfo",
            data: JSON.stringify(data),
            success: function(result){
                if(result.status == 0) {
                    console.log(result);
                }
            },
            error: function () {
                console.log("网络异常");
            }
        });
    });
});