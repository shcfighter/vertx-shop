/**
 * Created by za-wangshenhua on 2018/4/17.
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
        console.log(id);
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
                    loadCart();
                    $(".check-all").prop("checked",false);
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
    });
});

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
                        "\t\t<input class=\"check cart_check\" id=\"J_CheckBox_170769542747\" name=\"items[]\" value=\"" + value._id + "\" type=\"checkbox\">\n" +
                        "\t\t<label for=\"J_CheckBox_170769542747\"></label>\n" +
                        "\t    </div>\n" +
                        "\t</li>\n" +
                        "\t<li class=\"td td-item\">\n" +
                        "\t    <div class=\"item-pic\">\n" +
                        "\t\t<a href=\"#\" target=\"_blank\" data-title=\"" + value.commodity_name + "\"\n" +
                        "\t\t   class=\"J_MakePoint\" data-point=\"tbcart.8.12\">\n" +
                        "\t\t    <img src=\"" + value.image_url + "\" class=\"itempic J_ItemImg\"></a>\n" +
                        "\t    </div>\n" +
                        "\t    <div class=\"item-info\">\n" +
                        "\t\t<div class=\"item-basic-info\">\n" +
                        "\t\t    <a href=\"#\" target=\"_blank\" title=\"" + value.commodity_name + "\n" +
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
                        "\t\t\t<em class=\"price-original\">" + value.original_price + "</em>\n" +
                        "\t\t    </div>\n" +
                        "\t\t    <div class=\"price-line\">\n" +
                        "\t\t\t<em class=\"J_Price price-now\" tabindex=\"0\">" + value.price + "</em>\n" +
                        "\t\t    </div>\n" +
                        "\t\t</div>\n" +
                        "\t    </div>\n" +
                        "\t</li>\n" +
                        "\t<li class=\"td td-amount\">\n" +
                        "\t    <div class=\"amount-wrapper \">\n" +
                        "\t\t<div class=\"item-amount \">\n" +
                        "\t\t    <div class=\"sl\">\n" +
                        "\t\t\t<input class=\"min am-btn\" name=\"\" type=\"button\" value=\"-\"/>\n" +
                        "\t\t\t<input class=\"text_box\" name=\"\" type=\"text\" value=\"3\" style=\"width:30px;\"/>\n" +
                        "\t\t\t<input class=\"add am-btn\" name=\"\" type=\"button\" value=\"+\"/>\n" +
                        "\t\t    </div>\n" +
                        "\t\t</div>\n" +
                        "\t    </div>\n" +
                        "\t</li>\n" +
                        "\t<li class=\"td td-sum\">\n" +
                        "\t    <div class=\"td-inner\">\n" +
                        "\t\t<em tabindex=\"0\" class=\"J_ItemSum number\">117.00</em>\n" +
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
                $.Pop(result.message, "alert", function(){});
            }
        },
        error: function () {
            console.log("网络错误！");
        }
    });
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