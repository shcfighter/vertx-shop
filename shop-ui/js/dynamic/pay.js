/**
 * Created by za-wangshenhua on 2018/4/6.
 */
$(function () {
    var id = getQueryVariable("commodity_id");
    var order_num = getQueryVariable("order_num");
    if(undefined != id && null != id && "" != id){
        /**
         * 查询商品详情
         */
        $.ajax({
            type: 'GET',
            contentType: "application/json;",
            url: domain + "api/search/findCommodityById/" + id,
            success: function(result){
                if(result.status == 0){
                    var items = result.items;
                    console.log(items);
                    $(".bundle-main").html("<ul class=\"item-content clearfix\"><div class=\"pay-phone\">" +
                        "<li class=\"td td-item\"><div class=\"item-pic\"><a href=\"javascript:void(0)\" class=\"J_MakePoint\">" +
                        "<img src=\"" + items.image_url + "\" class=\"itempic J_ItemImg\" width=\"80\" ></a>" +
                        "</div><div class=\"item-info\"><div class=\"item-basic-info\">" +
                        "<a href=\"javascript:void(0)\" class=\"item-title J_MakePoint\" commodity_id = \"" + items.commodity_id + "\" data-point=\"tbcart.8.11\">" + items.commodity_name + "</a>" +
                        "</div></div></li><li class=\"td td-info\"><div class=\"item-props\">" +
                        /*"<span class=\"sku-line\">颜色：12#川南玛瑙</span><span class=\"sku-line\">包装：裸装</span>" +*/
                        "</div></li><li class=\"td td-price\"><div class=\"item-price price-promo-promo\"><div class=\"price-content\">" +
                        "<em class=\"J_Price price-now\">" + items.price + "</em>" +
                        "</div></div></li></div><li class=\"td td-amount\"><div class=\"amount-wrapper \"><div class=\"item-amount \">" +
                        "<span class=\"phone-title\">购买数量</span><div class=\"sl\">" +
                        "<input class=\"min am-btn\" name=\"\" type=\"button\" value=\"-\"/>" +
                        "<input class=\"text_box\" name=\"\" type=\"text\" value=\"" + order_num + "\"style=\"width:30px;\"/>" +
                        "<input class=\"add am-btn\" name=\"\" type=\"button\" value=\"+\"/>" +
                        "</div></div></div></li><li class=\"td td-sum\"><div class=\"td-inner\">" +
                        "<em tabindex=\"0\" class=\"J_ItemSum number\">￥" + (parseInt(items.price.substr(1).replace(",", "")) * order_num).toFixed(2) + "</em>" +
                        "</div></li><li class=\"td td-oplist\"><div class=\"td-inner\">" +
                        "<span class=\"phone-title\">配送方式</span>" +
                        "<div class=\"pay-logis\">快递<b class=\"sys_item_freprice\">" + items.freight + "</b>元" +
                        "</div></div></li></ul>");
                }
            },
            error: function () {
                console.log("网络异常");
            }
        });
    } else {
        alert("获取商品信息失败！");
    }

    $(".btn-go").click(function () {
        var data = {
            "shippingInformationId" : 1,
            "orderDetails" : [{
                "id": parseInt($(".item-title").attr("commodity_id")),
                "orderNum": parseInt($(".text_box").val())
            },{
                "id": 2,
                "orderNum": parseInt($(".text_box").val())
            }],
            "leaveMessage": "尽快发货"
        }
        $.ajax({
            type: 'POST',
            contentType: "application/json;",
            url: domain + "api/order/insertOrder",
            data: JSON.stringify(data),
            success: function(result){
                if(result.status == 0) {
                    console.log(result);
                    var items = result.items;
                }
            },
            error: function () {
                console.log("网络异常");
            }
        });
    });
});