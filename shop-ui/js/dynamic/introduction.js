/**
 * Created by shwang on 2018/3/29.
 */
$(function(){

    var id = getQueryVariable("commodity_id");
    if(undefined != id && null != id && "" != id){
        /**
         * 查询商品详情
         */
        $.ajax({
            type: 'GET',
            contentType: "application/json;",
            url: domain + "api/search/findCommodityFromESById/" + id,
            success: function(result){
                if(result.status == 0){
                    var items = result.items;
                    $(".tb-detail-hd").attr("commodity_id", items.commodity_id);
                    $(".collection").attr("id", items.commodity_id);
                    $(".tb-detail-hd h1").html(items.commodity_name);
                    //设置浏览图片
                    $.each(items.image_url, function(index, image_urls) {
                        if (0 == index) {
                            $(".tb-thumb").append("<li class=\"tb-selected\"><div class=\"tb-pic tb-s40\"><a href=\"javascript:void(0)\"><img src=\"" + image_urls + "\" mid=\"" + image_urls + "\" big=\"" + image_urls + "\"></a></div></li>");
                            $("#thumblist li a").click();
                        } else {
                            $(".tb-thumb").append("<li><div class=\"tb-pic tb-s40\"><a href=\"javascript:void(0)\"><img src=\"" + image_urls + "\" mid=\"" + image_urls + "\" big=\"" + image_urls + "\"></a></div></li>");
                        }
                        $(".slides").append("<li><img src=\"" + image_urls + "\" title=\"pic\"/></li>");
                    });
                    $(".tm-ind-sellCount .tm-count").html(items.month_sales_volume);
                    $(".tm-ind-sumCount .tm-count").html(items.total_evaluation_num);
                    $(".tm-ind-reviewCount .tm-count").html(items.total_sales_volume);
                    $(".sys_item_price").html(items.price);
                    $(".sys_item_mktprice").html(items.original_price);
                    $(".sys_item_freprice").html(items.freight);
                    $(".stock").html(items.num);
                    $("#J_AttrUL").html("");
                    $.each(items.commodity_params, function (index, value) {
                        $("#J_AttrUL").append("<li title=\"\">" + value + "</li>");
                    });
                    //设置产品参数
                    var params = items.commodity_params;
                    console.log(items)
                    $.each(items.detail_image_url, function(index, detail_image_url) {
                        $(".twlistNews").append("<img src=\"" + detail_image_url + "\">");
                    });

                    var data = {
                        keyword: items.brand_name + " " + items.category_name,
                        pageSize: 5,
                        page: 1
                    }
                    $.ajax({
                        type: 'POST',
                        contentType: "application/json;",
                        url: domain + "api/search/search",
                        data: JSON.stringify(data),
                        success: function(result){
                            if(result.status == 0){
                                var commoditys = result.items.items;
                                for (var index in commoditys) {
                                    commodity = commoditys[index];
                                    $(".mc ul").append("<li>\n" +
                                        "                    <div class=\"p-img\">\n" +
                                        "                        <a href=\"/introduction.html?commodity_id=" + commodity.commodity_id + "\"> <img class=\"\" src=\"" + commodity.image_url[0] + "\"> </a>\n" +
                                        "                    </div>\n" +
                                        "                    <div class=\"p-name\"><a href=\"introduction.html?commodity_id=" + commodity.commodity_id + "\">\n" + commodity.commodity_name +
                                        "                    </a>\n" +
                                        "                    </div>\n" +
                                        "                    <div class=\"p-price\"><strong>￥" + commodity.price + "</strong></div>\n" +
                                        "                </li>");
                                    $(".mc ul").find("li:first").attr("class", "first");
                                }
                            }
                        },
                        error: function () {
                            console.log("网络异常");
                        }
                    });
                }
            },
            error: function () {
                console.log("网络异常");
            }
        });
    } else {
        xw.alert("获取商品信息失败")
    }

    $("#LikBuy").click(function () {
        var cart_ids = new Array();
        var cart_json = {};
        cart_json["order_id"] = id;
        cart_json["source"] = "buy";
        cart_json["order_num"] = isNaN(parseInt($("#text_box").val())) ? 0 : parseInt($("#text_box").val());
        cart_ids.push(cart_json);
        $.ajax({
            type: 'POST',
            contentType: "application/json;",
            url: domain + "api/order/preparedInsertOrder",
            data: JSON.stringify(cart_ids),
            success: function(result){
                if(result.status == 0){
                    window.location.href = "/pay.html?order_id=" + result.items;
                } else {
                    xw.alert(result.message)
                }
            },
            error: function () {
                console.log("网络异常");
            }
        });
    });

    $("#LikBasket").click(function (event) {
        var data ={
            commodity_id: parseInt($(".tb-detail-hd").attr("commodity_id")),
            commodity_name: $(".tb-detail-hd h1").html(),
            price: $(".sys_item_price").html(),
            original_price: $(".sys_item_mktprice").html(),
            order_num: parseInt($("#text_box").val()),
            image_url: $("#thumblist").find("li:first-child").find("img").attr("src")
        }
        $.ajax({
            type: 'POST',
            contentType: "application/json;",
            url: domain + "api/cart/insertCart",
            data: JSON.stringify(data),
            success: function(result){
                if(result.status == 0){
                    console.log(result);
                    var offset = $("#shopCart_1").offset();
                    var img = $("#thumblist").find("li").eq(0).find("img").attr("src"); //获取当前点击图片链接
                    var flyer = $('<img class="flyer-img" src="' + img + '">'); //抛物体对象
                    flyer.fly({
                        start: {
                            left: event.clientX,//抛物体起点横坐标
                            top: event.clientY //抛物体起点纵坐标
                        },
                        end: {
                            left: offset.left,//抛物体终点横坐标
                            top: 250, //抛物体终点纵坐标
                        },
                        onEnd: function() {
                            $("#tip").show().animate({width: '200px'},300).fadeOut(500);////成功加入购物车动画效果
                            this.destory(); //销毁抛物体
                        }
                    });
                }
            },
            error: function () {
                console.log("网络异常");
            }
        });
    });

    /**
     * 收藏
     */
    $(".collection").click(function(event) {
        var data ={
            commodity_id: parseInt($(".tb-detail-hd").attr("commodity_id")),
            commodity_name: $(".tb-detail-hd h1").html(),
            price: $(".sys_item_price").html(),
            original_price: $(".sys_item_mktprice").html(),
            image_url: $("#thumblist").find("li:first-child").find("img").attr("src")
        }
        $.ajax({
            type: 'POST',
            contentType: "application/json;",
            url: domain + "api/collection/insertCollection",
            data: JSON.stringify(data),
            success: function(result){
                if(result.status == 0){
                    xw.alert(result.message);
                }
            },
            error: function () {
                console.log("网络异常");
            }
        });
    });
});