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
                    $(".pay-sum").html(parseInt(total_price).toFixed(2));
                    $("#J_ActualFee").html(total_price.toFixed(2));
                }
            },
            error: function () {
                console.log("网络异常");
            }
        });
    } else {
        $.Pop("获取商品信息失败！", "alert", function(){});
    }

    loadAddress();

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
                    window.location.href = "/success.html";
                    /*$.Pop("下单成功！</br>查看订单列表", "confirm", function(){
                        window.location.href = "/success.html"
                    });*/
                } else {
                    $.Pop("下单失败，请重试！", "alert", function(){});
                }
            },
            error: function () {
                console.log("网络异常");
            }
        });
    });

    /**
     * 地区控件
     */
    $("#user-address").click(function (e) {
        SelCity(this,e);
    });

    /**
     *  保存收货地址
     */
    $(".am-save").click(function () {
        var data = {
            receiver: $("#user-name").val(),
            mobile: $("#user-phone").val(),
            province_code: $("#hcity").attr("data-id"),
            city_code: $("#hproper").attr("data-id"),
            county_code: $("#harea").attr("data-id"),
            address: $("#user-intro").val(),
            address_details: $("#hcity").val() + " " + $("#hproper").val() + " " + $("#harea").val() + " " + $("#user-intro").val()
        }
        $.ajax({
            type: 'POST',
            contentType: "application/json;",
            url: domain + "api/user/insertAddress",
            data: JSON.stringify(data),
            success: function(result){
                if(result.status == 0) {
                    loadAddress();
                    $('.theme-poptit .close,.btn-op .close').click();
                    $.Pop(result.message, "alert", function(){});
                } else {
                    $.Pop(result.message, "alert", function(){});
                }
            },
            error: function () {
                console.log("网络异常");
            }
        });
    });

});

function loadAddress() {
    /**
     * 初始化收货地址列表
     */
    $.ajax({
        type: 'GET',
        contentType: "application/json;",
        url: domain + "api/user/findAddress",
        success: function(result){
            if(result.status == 0) {
                $(".address ul").html("");
                $.each(result.items, function (index, value) {
                    var defaultAddr = "";
                    var hidden = "hidden";
                    if (value.is_default == 1) {
                        defaultAddr = "defaultAddr";
                        hidden = "";
                    }
                    $(".address ul").append("<div class=\"per-border\"></div>\n" +
                        "                <li class=\"user-addresslist " + defaultAddr + "\">\n" +
                        "                    <div class=\"address-left\">\n" +
                        "                        <div class=\"user DefaultAddr\">\n" +
                        "                            <span class=\"buy-address-detail\">\n" +
                        "                                <span class=\"buy-user new-txt\">" + value.receiver + " </span>\n" +
                        "                                <span class=\"buy-phone new-txt-rd2\">" + value.mobile + "</span>\n" +
                        "                            </span>\n" +
                        "                        </div>\n" +
                        "                        <div class=\"default-address DefaultAddr new-mu_l2a\">\n" +
                        "                            <span class=\"buy-line-title buy-line-title-type\">收货地址：</span>\n" +
                        "                            <span class=\"buy--address-detail\">\n" +
                        "                               <span class=\"street\">" + value.address_details + "</span>\n" +
                        "                            </span>\n" +
                        "                        </div>\n" +
                        "                        <ins class=\"deftip " + hidden + "\">默认地址</ins>\n" +
                        "                    </div>\n" +
                        "                    <div class=\"address-right\">\n" +
                        "                        <a href=\"person/address.html\"><span class=\"am-icon-angle-right am-icon-lg\"></span></a>\n" +
                        "                    </div>\n" +
                        "                    <div class=\"clear\"></div>\n" +
                        "                </li>");
                })
            } else {
                $.Pop(result.message, "alert", function(){});
            }
        },
        error: function () {
            console.log("网络异常");
        }
    });
}