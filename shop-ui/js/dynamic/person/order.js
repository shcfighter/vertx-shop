$(function () {
    $(".am-nav-tabs a").click(function() {

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
                    console.log(result.items);
                    var items = result.items;
                    $.each(items, function (index, value) {
                        $("#tab1 .order-list").html("<div class=\"order-status3\">\n" +
                            "    <div class=\"order-title\">\n" +
                            "        <div class=\"dd-num\">订单编号：<a href=\"javascript:void(0);\">value.order_id</a></div>\n" +
                            "        <span>成交时间：2015-12-20</span>\n" +
                            "    </div>\n" +
                            "    <div class=\"order-content\">\n" +
                            "        <div class=\"order-left\">\n" +
                            "            <ul class=\"item-list\">\n" +
                            "                <li class=\"td td-item\">\n" +
                            "                    <div class=\"item-pic\">\n" +
                            "                        <a href=\"#\" class=\"J_MakePoint\">\n" +
                            "                            <img src=\"../images/kouhong.jpg_80x80.jpg\"\n" +
                            "                                 class=\"itempic J_ItemImg\">\n" +
                            "                        </a>\n" +
                            "                    </div>\n" +
                            "                    <div class=\"item-info\">\n" +
                            "                        <div class=\"item-basic-info\">\n" +
                            "                            <a href=\"#\">\n" +
                            "                                <p>美康粉黛醉美唇膏 持久保湿滋润防水不掉色</p>\n" +
                            "                                <p class=\"info-little\">颜色：12#川南玛瑙\n" +
                            "                                    <br/>包装：裸装 </p>\n" +
                            "                            </a>\n" +
                            "                        </div>\n" +
                            "                    </div>\n" +
                            "                </li>\n" +
                            "                <li class=\"td td-price\">\n" +
                            "                    <div class=\"item-price\">\n" +
                            "                        333.00\n" +
                            "                    </div>\n" +
                            "                </li>\n" +
                            "                <li class=\"td td-number\">\n" +
                            "                    <div class=\"item-number\">\n" +
                            "                        <span>×</span>2\n" +
                            "                    </div>\n" +
                            "                </li>\n" +
                            "                <li class=\"td td-operation\">\n" +
                            "                    <div class=\"item-operation\">\n" +
                            "                        <a href=\"refund.html\">退款/退货</a>\n" +
                            "                    </div>\n" +
                            "                </li>\n" +
                            "            </ul>\n" +
                            "            <ul class=\"item-list\">\n" +
                            "                <li class=\"td td-item\">\n" +
                            "                    <div class=\"item-pic\">\n" +
                            "                        <a href=\"#\" class=\"J_MakePoint\">\n" +
                            "                            <img src=\"../images/62988.jpg_80x80.jpg\" class=\"itempic J_ItemImg\">\n" +
                            "                        </a>\n" +
                            "                    </div>\n" +
                            "                    <div class=\"item-info\">\n" +
                            "                        <div class=\"item-basic-info\">\n" +
                            "                            <a href=\"#\">\n" +
                            "                                <p>礼盒袜子女秋冬 纯棉袜加厚 韩国可爱 </p>\n" +
                            "                                <p class=\"info-little\">颜色分类：李清照\n" +
                            "                                    <br/>尺码：均码</p>\n" +
                            "                            </a>\n" +
                            "                        </div>\n" +
                            "                    </div>\n" +
                            "                </li>\n" +
                            "                <li class=\"td td-price\">\n" +
                            "                    <div class=\"item-price\">\n" +
                            "                        333.00\n" +
                            "                    </div>\n" +
                            "                </li>\n" +
                            "                <li class=\"td td-number\">\n" +
                            "                    <div class=\"item-number\">\n" +
                            "                        <span>×</span>2\n" +
                            "                    </div>\n" +
                            "                </li>\n" +
                            "                <li class=\"td td-operation\">\n" +
                            "                    <div class=\"item-operation\">\n" +
                            "                        <a href=\"refund.html\">退款/退货</a>\n" +
                            "                    </div>\n" +
                            "                </li>\n" +
                            "            </ul>\n" +
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
});