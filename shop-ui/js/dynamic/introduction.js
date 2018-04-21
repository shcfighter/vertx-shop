/**
 * Created by za-wangshenhua on 2018/3/29.
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
                }
            },
            error: function () {
                console.log("网络异常");
            }
        });
    } else {
        alert("获取商品信息失败！");
    }

    $("#LikBuy").click(function () {
        window.location.href = "/pay.html?commodity_id=" + id + "&order_num=" + $("#text_box").val();
    });

    $("#LikBasket").click(function () {
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

                }
            },
            error: function () {
                console.log("网络异常");
            }
        });
    });

    /**
     * 购物车
     */
    $(".addcart").click(function(event) {
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
                top: offset.top, //抛物体终点纵坐标
            },
            onEnd: function() {
                $("#tip").show().animate({width: '200px'},300).fadeOut(500);////成功加入购物车动画效果
                this.destory(); //销毁抛物体
            }
        });
    });
});