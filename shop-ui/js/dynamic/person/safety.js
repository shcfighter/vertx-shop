$(function () {
    $.ajax({
        type: 'GET',
        contentType: "application/json;",
        url: domain + "api/user/findUserCertified",
        success: function(result){
            if(result.status == 0) {
                $.each(result.items, function (index, value) {
                    switch (value.certified_type) {
                        case 1: {
                            break;
                        }
                        case 2: {
                            //支付密码
                            $("#pay_password").html("修改");
                            break;
                        }
                        case 3: {
                            $(".i-safety-iphone").parent("li").find(".fore2").find("small").html("您验证的手机：" + value.remarks + " 若已丢失或停用，请立即更换")
                            break;
                        }
                        case 4: {
                            $(".i-safety-mail").parent("li").find(".fore2").find("small").html("您验证的邮箱：" + value.remarks + " 可用于快速找回登录密码")
                            break;
                        }
                        case 5: {
                            break;
                        }
                        case 6: {
                            break;
                        }
                        default : {
                            break;
                        }
                    }
                })
            }
        },
        error: function () {
            console.log("网络异常");
        }
    });
});