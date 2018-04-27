/**
 * Created by za-wangshenhua on 2018/4/6.
 */
var source;
$(function () {
    var id = getQueryVariable("order_id");
    if(undefined != id && null != id && "" != id){
        /**
         * 查询商品详情
         */
        $.ajax({
            type: 'GET',
            contentType: "application/json;",
            url: domain + "api/order/findPreparedOrder/" + id,
            success: function(result){
                if(result.status == 0){
                    var items = result.items;
                    $(".bundle-main").html("");
                    var total_price = 0.00;
                    source = items[0].source;
                    $.each(items, function (index, value) {
                        total_price += parseInt(value.price) * value.order_num;
                        $(".bundle-main").append("<ul class=\"item-content clearfix\"><div class=\"pay-phone\">" +
                            "<li class=\"td td-item\"><div class=\"item-pic\"><a href=\"javascript:void(0)\" class=\"J_MakePoint\">" +
                            "<img src=\"" + value.image_url + "\" class=\"itempic J_ItemImg\" width=\"80\" ></a>" +
                            "</div><div class=\"item-info\"><div class=\"item-basic-info\">" +
                            "<a href=\"javascript:void(0)\" class=\"item-title J_MakePoint\" commodity_id = \"" + value.commodity_id + "\" data-point=\"tbcart.8.11\">" + value.commodity_name + "</a>" +
                            "</div></div></li><li class=\"td td-info\"><div class=\"item-props\">" +
                            /*"<span class=\"sku-line\">颜色：12#川南玛瑙</span><span class=\"sku-line\">包装：裸装</span>" +*/
                            "</div></li><li class=\"td td-price\"><div class=\"item-price price-promo-promo\"><div class=\"price-content\">" +
                            "￥<em class=\"J_Price price-now\">" + value.price + "</em>" +
                            "</div></div></li></div><li class=\"td td-amount\"><div class=\"amount-wrapper \"><div class=\"item-amount \">" +
                            "<span class=\"phone-title\">购买数量</span><div class=\"sl\">" + value.order_num +
                            "</div></div></div></li><li class=\"td td-sum\"><div class=\"td-inner\">" +
                            "<em tabindex=\"0\" class=\"J_ItemSum number\">￥" + (parseInt(value.price) * value.order_num).toFixed(2) + "</em>" +
                            "</div></li><li class=\"td td-oplist\"><div class=\"td-inner\">" +
                            "<span class=\"phone-title\">配送方式</span>" +
                            "<div class=\"pay-logis\">快递￥<b class=\"sys_item_freprice\">" + value.freight + "</b>元" +
                            "</div></div></li></ul>");
                    });
                }
                $(".pay-sum").html(parseInt(total_price).toFixed(2));
                $("#J_ActualFee").html(total_price.toFixed(2));
            },
            error: function () {
                console.log("网络异常");
            }
        });
    } else {
        $.Pop("获取商品信息失败！", "alert", function(){});
    }

    $(".btn-go").click(function () {
        var order_details = new Array();
        $(".item-content").each(function (index, content) {
            var content = {
                "id": parseInt($(this).find(".item-title").attr("commodity_id")),
                "order_num": parseInt($(this).find(".sl").text())
            }
            order_details.push(content);
        })
        var data = {
            "shipping_information_id" : 1,
            "order_details" : order_details,
            "source": source,
            "leave_message": "尽快发货"
        }
        $.ajax({
            type: 'POST',
            contentType: "application/json;",
            url: domain + "api/order/insertOrder",
            data: JSON.stringify(data),
            success: function(result){
                if(result.status == 0) {
                    console.log(result);
                    $.Pop("下单成功！</br>查看订单列表", "confirm", function(){
                        window.location.href = "/success.html"
                    });
                } else {
                    $.Pop("下单失败，请重试！", "alert", function(){});
                }
            },
            error: function () {
                console.log("网络异常");
            }
        });
    });
});