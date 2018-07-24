$(function () {

    var id = getQueryVariable("order_id");
    if(undefined != id && null != id && "" != id){
        /**
         * 查询订单详情
         */
        $.ajax({
            type: 'GET',
            contentType: "application/json;",
            url: domain + "api/order/getOrder/" + id,
            success: function(result){
                if(result.status == 0) {
                    var order = result.items;
                    $("#order_id").html(order.order_id);
                    $(".amountall").html(order.total_price);
                    $("#refund-money").val(order.total_price);
                    $(".freight_price").html(order.freight);
                    $(".time").html(order.create_time);
                    var order_details = JSON.parse(order.order_details);
                    var details = "";
                    $.each(order_details, function (index, detail) {
                        details += "<div class=\"item-pic\">\n" +
                            "                        <a href=\"#\" class=\"J_MakePoint\">\n" +
                            "                            <img src=\"" + detail.image_url + "\" class=\"itempic\">\n" +
                            "                        </a>\n" +
                            "                    </div>\n" +
                            "                    <div class=\"item-title\">\n" +
                            "                        <div class=\"item-name\">\n" +
                            "                            <a href=\"#\">\n" +
                            "                                <p class=\"item-basic-info\">" + detail.commodity_name + "</p>\n" +
                            "                            </a>\n" +
                            "                        </div>\n" +
                            "                    </div>";
                    })
                    $(".item-info").before(details);
                } else {
                    $.Pop(result.message, "alert", function(){});
                }
            },
            error: function () {
                console.log("网络异常");
            }
        });
    } else {
        $.Pop("获取订单信息失败！", "alert", function(){});
    }


    $(".am-btn-danger").click(function() {
        var data = {
            refund_type: parseInt($("#refund_type").val()),
            refund_reason: $("#refund_reason").val(),
            refund_money: $("#refund-money").val(),
            refund_description: $("#refund_description").val()
        }
        $.ajax({
            type: "PUT",
            contentType: "application/json;",
            url: domain + "api/order/refund/" + id,
            data: JSON.stringify(data),
            success: function(result){
                if (result.status == 0) {
                    $.Pop(result.message, "alert", function(){});
                } else {
                    $.Pop(result.message, "alert", function(){});
                }
            },
            error: function () {
                console.log("网络错误！");
            }
        });
    });
});