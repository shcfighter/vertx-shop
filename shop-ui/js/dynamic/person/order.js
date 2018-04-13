$(function () {
    $(".am-nav-tabs a").click(function() {
        var tab = $(this).attr("href");
        console.log(tab);
        var data = {
            status: parseInt($(this).attr("status")),
            size: 10,
            page: 1
        }
        $.ajax({
            type: 'POST',
            contentType: "application/json;",
            url: domain + "api/order/findOrder",
            data: JSON.stringify(data),
            success: function(result){
                if(result.status == 0){
                    var items = result.items;
                    $(tab + " .order-list").append("");
                    $.each(items, function (index, value) {
                        var orderDetails = JSON.parse(value.order_details);
                        var orderDetailsHtml = "";
                        $.each(orderDetails, function (orderIndex, orderValue) {
                            orderDetailsHtml += "<ul class=\"item-list\">\n" +
                            "    <li class=\"td td-item\">\n" +
                            "        <div class=\"item-pic\">\n" +
                            "            <a href=\"#\" class=\"J_MakePoint\">\n" +
                            "                <img src=\"" + orderValue.imageUrl + "\"\n" +
                            "                     class=\"itempic J_ItemImg\">\n" +
                            "            </a>\n" +
                            "        </div>\n" +
                            "        <div class=\"item-info\">\n" +
                            "            <div class=\"item-basic-info\">\n" +
                            "                <a href=\"#\">\n" +
                            "                    <p>" + orderValue.commodityName + "</p>\n" +
                            "                    <p class=\"info-little\">颜色：12#川南玛瑙\n" +
                            "                        <br/>包装：裸装 </p>\n" +
                            "                </a>\n" +
                            "            </div>\n" +
                            "        </div>\n" +
                            "    </li>\n" +
                            "    <li class=\"td td-price\">\n" +
                            "        <div class=\"item-price\">\n" +
                            "            " + orderValue.price + "\n" +
                            "        </div>\n" +
                            "    </li>\n" +
                            "    <li class=\"td td-number\">\n" +
                            "        <div class=\"item-number\">\n" +
                            "            <span>×</span>" + orderValue.orderNum + "\n" +
                            "        </div>\n" +
                            "    </li>\n" +
                            "    <li class=\"td td-operation\">\n" +
                            "        <div class=\"item-operation\">\n" +
                            "            <a href=\"refund.html\">退款/退货</a>\n" +
                            "        </div>\n" +
                            "    </li>\n" +
                            "</ul>";
                        });
                        $("#tab1 .order-list").append("<div class=\"order-status3\">\n" +
                            "    <div class=\"order-title\">\n" +
                            "        <div class=\"dd-num\">订单编号：<a href=\"javascript:void(0);\">" + value.order_id + "</a></div>\n" +
                            "        <span>成交时间：" + value.create_time + "</span>\n" +
                            "    </div>\n" +
                            "    <div class=\"order-content\">\n" +
                            "        <div class=\"order-left\">\n" +
                            orderDetailsHtml +
                            "        </div>\n" +
                            "        <div class=\"order-right\">\n" +
                            "            <li class=\"td td-amount\">\n" +
                            "                <div class=\"item-amount\">\n" +
                            "                    合计：676.00\n" +
                            "                    <p>含运费：<span>10.00</span></p>\n" +
                            "                </div>\n" +
                            "            </li>\n" +
                            "            <div class=\"move-right\">\n" +
                            "                <li class=\"td td-status\">\n" +
                            "                    <div class=\"item-status\">\n" +
                            "                        <p class=\"Mystatus\">卖家已发货</p>\n" +
                            "                        <p class=\"order-info\"><a href=\"orderinfo.html\">订单详情</a></p>\n" +
                            "                        <p class=\"order-info\"><a href=\"logistics.html\">查看物流</a></p>\n" +
                            "                        <p class=\"order-info\"><a href=\"#\">延长收货</a></p>\n" +
                            "                    </div>\n" +
                            "                </li>\n" +
                            "                <li class=\"td td-change\">\n" +
                            "                    <div class=\"am-btn am-btn-danger anniu\">\n" +
                            "                        确认收货\n" +
                            "                    </div>\n" +
                            "                </li>\n" +
                            "            </div>\n" +
                            "        </div>\n" +
                            "    </div>\n" +
                            "</div>");
                    });
                } else {
                    alert(result.message);
                }
            },
            error: function () {
                alert("网路异常");
            }
        });
    });
    $(".am-nav-tabs").find("a").get(0).click();
});