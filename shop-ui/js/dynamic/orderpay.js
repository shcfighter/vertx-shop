$(function () {
    var id = getQueryVariable("order_id");
    if(undefined == id || null == id || "" == id){
        $.Pop("支付异常！", "alert", function(){
            //window.location.href = "/index.html";
        });
    }
    $("#order-id").html(id);
    $.ajax({
        type: "GET",
        contentType: "application/json;",
        url: domain + "api/order/getAddress/" + id,
        success: function(result){
            if(result.status == 0){
                var items = result.items;
                var information = items.information;
                $(".pay-blance").find("em").html("￥" + items.total_price);
                $(".user-info").find("p").eq(0).find("span").html(information.receiver);
                $(".user-info").find("p").eq(1).find("span").html(information.mobile);
                $(".user-info").find("p").eq(2).find("span").html(information.address_details);
            } else {
                $.Pop(result.message,"alert",function(){});
            }
        },
        error: function () {
            console.log("网络错误！");
        }
    });

    $("#pay").click(function () {
        var data = {
            pay_pwd: $("input[name='pay_pwd']").val()
        }

        $.ajax({
            type: 'POST',
            contentType: "application/json;",
            url: domain + "api/account/payOrder/" + id,
            data: JSON.stringify(data),
            success: function(result){
                if(result.status == 0){
                    window.location.href = "/person/order.html";
                } else {
                    if ("not_pay_pwd" == result.message) {
                        $.Pop("为设置支付密码，前往设置","alert",function(){
                            window.location.href = "/person/setpay.html";
                        });
                    } else {
                        $.Pop(result.message,"alert",function(){});
                    }
                }
            },
            error: function () {
                console.log("网络错误！");
            }
        });
    });
});