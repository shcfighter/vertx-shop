/**
 * Created by shwang on 2018/4/17.
 */
$(function () {
    var data = {
        page_size: 10,
        page: 1
    }
    loadCart(1);

    $(".deleteAll").live("click", function () {
        var arr = new Array();
        $(".cart_check:checkbox[checked]").each(function(i){
            arr[i] = $(this).val();
        });
        var id = arr.join(",");
        var data = {
            ids: id
        }
        $.ajax({
            type: 'POST',
            contentType: "application/json;",
            url: domain + "api/cart/removeCart",
            data: JSON.stringify(data),
            success: function(result){
                if(result.status == 0){
                    loadCart(1);
                    $(".check-all").prop("checked",false);
                    calculatePrice();
                }
            },
            error: function () {
                console.log("网络异常");
            }
        });
    });

    $(".delete").live("click", function () {
        var id = $(this).attr("id");
        var data = {
            ids: id
        }
        $.ajax({
            type: 'POST',
            contentType: "application/json;",
            url: domain + "api/cart/removeCart",
            data: JSON.stringify(data),
            success: function(result){
                if(result.status == 0){
                    $("#" + id).closest("ul").remove();
                    calculatePrice();
                }
            },
            error: function () {
                console.log("网络异常");
            }
        });
    });

    //全选或全不选
    $(".check-all").live("click", function(){
        if(this.checked){
            $(".check").each(function(){
                $(this).prop("checked",true);
            });
        }else{
            $(".check").each(function(){
                $(this).prop("checked",false);
            });
        }
        calculatePrice();
    });

    $(".cart_check").live("click", function () {
        calculatePrice();
    });

    $(".btn-area").click(function () {
        var cart_ids = new Array();
        $(".cart_check").each(function(){
          if(this.checked){
              var cart_json = {};
              cart_json["order_id"] = $(this).attr("commodity_id");
              cart_json["source"] = "cart";
              cart_json["order_num"] = isNaN(parseInt($(this).closest(".item-content").find(".text_box").val())) ? 0 : parseInt($(this).closest(".item-content").find(".text_box").val());
              cart_ids.push(cart_json);
          }
        });
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
});

/**
 * 计算勾选的价格及数量
 */
function calculatePrice(){
    var orderNum = 0;
    var price = 0.00;
    $(".check").each(function(){
        if(this.checked){
            orderNum += isNaN(parseInt($(this).closest(".item-content").find(".text_box").val())) ? 0 : parseInt($(this).closest(".item-content").find(".text_box").val());
            price += isNaN(parseFloat($(this).closest(".item-content").find(".J_ItemSum").text())) ? 0 : parseInt($(this).closest(".item-content").find(".J_ItemSum").text());
        }
    });
    $("#J_SelectedItemsCount").html(orderNum);
    $("#J_Total").html(price.toFixed(2));
}

function loadCart(page){
    $.ajax({
        type: 'GET',
        contentType: "application/json;",
        url: domain + "api/cart/findCartPage?pageSize=10&page=" + page,
        //data: JSON.stringify(data),
        success: function(result){
            if (0 == result.status) {
                $("#page").val(result.page);
                $.each(result.items, function (index, value) {
                    $(".bundle-main").append("<ul class=\"item-content clearfix\">" +
                        "\t<li class=\"td td-chk\">\n" +
                        "\t    <div class=\"cart-checkbox \">\n" +
                        "\t\t<input class=\"check cart_check\" id=\"J_CheckBox_170769542747\" name=\"items[]\" commodity_id=" +value.commodity_id + " value=\"" + value._id + "\" type=\"checkbox\">\n" +
                        "\t\t<label for=\"J_CheckBox_170769542747\"></label>\n" +
                        "\t    </div>\n" +
                        "\t</li>\n" +
                        "\t<li class=\"td td-item\">\n" +
                        "\t    <div class=\"item-pic\">\n" +
                        "\t\t<a href=\"/introduction.html?commodity_id=" + value.commodity_id + "\" target=\"_blank\" data-title=\"" + value.commodity_name + "\"\n" +
                        "\t\t   class=\"J_MakePoint\" data-point=\"tbcart.8.12\">\n" +
                        "\t\t    <img src=\"" + value.image_url + "\" class=\"itempic J_ItemImg\"></a>\n" +
                        "\t    </div>\n" +
                        "\t    <div class=\"item-info\">\n" +
                        "\t\t<div class=\"item-basic-info\">\n" +
                        "\t\t    <a href=\"/introduction.html?commodity_id=" + value.commodity_id + "\" target=\"_blank\" title=\"" + value.commodity_name + "\n" +
                        "\t\t       class=\"item-title J_MakePoint\" data-point=\"tbcart.8.11\">" + value.commodity_name + "</a>\n" +
                        "\t\t</div>\n" +
                        "\t    </div>\n" +
                        "\t</li>\n" +
                        // "\t<li class=\"td td-info\">\n" +
                        // "\t    <div class=\"item-props item-props-can\">\n" +
                        // "\t\t<span class=\"sku-line\"><!--颜色：10#蜜橘色--></span>\n" +
                        // "\t\t<span class=\"sku-line\"><!--包装：两支手袋装（送彩带）--></span>\n" +
                        // "\t\t<span tabindex=\"0\" class=\"btn-edit-sku theme-login\"><!--修改--></span>\n" +
                        // "\t\t<i class=\"theme-login am-icon-sort-desc\"></i>\n" +
                        // "\t    </div>\n" +
                        // "\t</li>\n" +
                        "\t<li class=\"td td-price\">\n" +
                        "\t    <div class=\"item-price price-promo-promo\">\n" +
                        "\t\t<div class=\"price-content\">\n" +
                        "\t\t    <div class=\"price-line\">\n" +
                        "\t\t\t<em class=\"price-original\">￥" + value.original_price + "</em>\n" +
                        "\t\t    </div>\n" +
                        "\t\t    <div class=\"price-line\">\n" +
                        "\t\t\t￥<em class=\"J_Price price-now\" tabindex=\"0\">" + value.price + "</em>\n" +
                        "\t\t    </div>\n" +
                        "\t\t</div>\n" +
                        "\t    </div>\n" +
                        "\t</li>\n" +
                        "\t<li class=\"td td-amount\">\n" +
                        "\t    <div class=\"amount-wrapper \">\n" +
                        "\t\t<div class=\"item-amount \">\n" +
                        "\t\t    <div class=\"sl\">\n" +
                        "\t\t\t<input class=\"min am-btn\" name=\"\" type=\"button\" value=\"-\"/>\n" +
                        "\t\t\t<input class=\"text_box\" name=\"\" type=\"text\" value=\"" + value.order_num + "\" style=\"width:30px;\"/>\n" +
                        "\t\t\t<input class=\"add am-btn\" name=\"\" type=\"button\" value=\"+\"/>\n" +
                        "\t\t    </div>\n" +
                        "\t\t</div>\n" +
                        "\t    </div>\n" +
                        "\t</li>\n" +
                        "\t<li class=\"td td-sum\">\n" +
                        "\t    <div class=\"td-inner\">\n" +
                        "\t\t￥<em tabindex=\"0\" class=\"J_ItemSum number\">" + (value.order_num * value.price).toFixed(2) + "</em>\n" +
                        "\t    </div>\n" +
                        "\t</li>\n" +
                        "\t<li class=\"td td-op\">\n" +
                        "\t    <div class=\"td-inner\">\n" +
                        "\t\t<a title=\"移入收藏夹\" class=\"btn-fav\" href=\"#\">\n" +
                        "\t\t    移入收藏夹</a>\n" +
                        "\t\t<a href=\"javascript:void(0);\" data-point-url=\"#\" id = \"" + value._id + "\"class=\"delete\">\n" +
                        "\t\t    删除</a>\n" +
                        "\t    </div>\n" +
                        "\t</li>\n" +
                        "</ul>");
                });
            } else {
                xw.alert(result.message)
            }
        },
        error: function () {
            console.log("网络错误！");
        }
    });

    $(".add").live("click", function () {
        var t = $(this).parent().find('input[class*=text_box]');
        var price = $(this).closest(".item-content").find(".price-now").text();
        $(this).closest(".item-content").find(".J_ItemSum").text(((parseInt(t.val()) + 1) * price).toFixed(2));
        calculatePrice();
    })
    $(".min").live("click", function () {
        var t = parseInt($(this).parent().find('input[class*=text_box]').val());
        var price = $(this).closest(".item-content").find(".price-now").text();
        if (t <= 1) {
            $(this).closest(".item-content").find(".J_ItemSum").text(price);
        } else {
            $(this).closest(".item-content").find(".J_ItemSum").text(((t - 1) * price).toFixed(2));
        }
        calculatePrice();
    })
}

$(window).scroll(
    function () {
        var scrollTop = $(this).scrollTop();
        var scrollHeight = $(document).height();
        var windowHeight = $(this).height();
        if (scrollTop + windowHeight == scrollHeight) {
            var page = $("#page").val();
            if(undefined == page || null == page){
                page = 1;
            }
            this.loadCart(parseInt(page) + 1);
        }
    });