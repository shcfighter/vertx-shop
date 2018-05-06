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
                            break;
                        }
                        case 4: {
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