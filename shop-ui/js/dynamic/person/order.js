$(function () {
    var pageSize = 10;
    $(".am-nav-tabs a").click(function() {
        var tab = $(this).attr("href");
        var page = parseInt($(".am-pagination .am-active").attr("page"));
        if(undefined == page || null == page){
            page = 1;
        }
        var data = {
            status: parseInt($(this).attr("status")),
            pageSize: pageSize,
            page: page
        }
        var tab_index = $(this).closest("ul").find("a").index(this);
        $.ajax({
            type: 'POST',
            contentType: "application/json;",
            url: domain + "api/order/findOrder",
            data: JSON.stringify(data),
            success: function(result){
                if(result.status == 0){
                    pageTurning(result, pageSize);
                    var items = result.items;
                    var $order_list = $(".am-tabs-bd").find(".order-list").get(tab_index);
                    $($order_list).html("");
                    $.each(items, function (index, value) {
                        var orderDetails = JSON.parse(value.order_details);
                        var orderDetailsHtml = "";
                        var price = 0.00;
                        $.each(orderDetails, function (orderIndex, orderValue) {
                            price += parseFloat(orderValue.price.substr(1).replace(",", "")) * parseFloat(orderValue.order_num);
                            orderDetailsHtml += "<ul class=\"item-list\">\n" +
                            "    <li class=\"td td-item\">\n" +
                            "        <div class=\"item-pic\">\n" +
                            "            <a href=\"#\" class=\"J_MakePoint\">\n" +
                            "                <img src=\"" + orderValue.image_url + "\"\n" +
                            "                     class=\"itempic J_ItemImg\">\n" +
                            "            </a>\n" +
                            "        </div>\n" +
                            "        <div class=\"item-info\">\n" +
                            "            <div class=\"item-basic-info\">\n" +
                            "                <a href=\"#\">\n" +
                            "                    <p>" + orderValue.commodity_name + "</p>\n" +
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
                            "            <span>×</span>" + orderValue.order_num + "\n" +
                            "        </div>\n" +
                            "    </li>\n" +
                            "    <li class=\"td td-operation\">\n" +
                            "        <div class=\"item-operation\">\n" +
                            "            <a href=\"refund.html\">退款/退货</a>\n" +
                            "        </div>\n" +
                            "    </li>\n" +
                            "</ul>";
                        });
                        $($order_list).append("<div class=\"order-status3\">\n" +
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
                            "                    合计：" + price + "\n" +
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
                    $.Pop(result.message, "alert", function(){});
                }
            },
            error: function () {
            }
        });
    });
    $(".am-nav-tabs").find("a").get(0).click();

    $(".am-pagination").on("click", "li", function () {
        if($(this).hasClass("am-disabled")) {
            return ;
        }
        $(".am-pagination li").removeClass();
        $(this).addClass("am-active");
        var status = $(".am-nav-tabs").find(".am-active").children("a").attr("status");
        console.log(status);
        if (undefined == status || null ==status) {
            status = 0;
        }
        $(".am-nav-tabs").find("a").get(status).click();
    });
});