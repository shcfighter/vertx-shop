$(function () {

    /**
     * 初始化收货地址列表
     */
    $.ajax({
        type: 'GET',
        contentType: "application/json;",
        url: domain + "api/collection/findCollection?pageNum=1",
        success: function(result){
            if(result.status == 0) {
                $(".s-content").html("");
                $.each(result.items, function (index, value) {
                    $(".s-content").append("<div class=\"s-item-wrap\">\n" +
                        "                            <div class=\"s-item\">\n" +
                        "                                <div class=\"s-pic\">\n" +
                        "                                    <a href=\"#\" class=\"s-pic-link\">\n" +
                        "                                        <img src=\"" + value.image_url + "\"\n" +
                        "                                             alt=\"" + value.commodity_name + "\"\n" +
                        "                                             title=\"" + value.commodity_name + "\" class=\"s-pic-img s-guess-item-img\">\n" +
                        "                                    </a>\n" +
                        "                                </div>\n" +
                        "                                <div class=\"s-info\">\n" +
                        "                                    <div class=\"s-title\"><a href=\"#\" title=\"" + value.commodity_name + "\">" + value.commodity_name + "</a>\n" +
                        "                                    </div>\n" +
                        "                                    <div class=\"s-price-box\">\n" +
                        "                                        <span class=\"s-price\"><em class=\"s-price-sign\">¥</em><em\n" +
                        "                                                class=\"s-value\">" + value.price + "</em></span>\n" +
                        "                                        <span class=\"s-history-price\"><em class=\"s-price-sign\">¥</em><em\n" +
                        "                                                class=\"s-value\">" + value.original_price + "</em></span>\n" +
                        "                                    </div>\n" +
                        "                                    <div class=\"s-extra-box\">\n" +
                        "                                        <span class=\"s-comment\">好评: 98.03%</span>\n" +
                        "                                        <span class=\"s-sales\">月销: 219</span>\n" +
                        "                                    </div>\n" +
                        "                                </div>\n" +
                        "                                <div class=\"s-tp\">\n" +
                        "                                    <span class=\"ui-btn-loading-before\">找相似</span>\n" +
                        "                                    <i class=\"am-icon-shopping-cart\"></i>\n" +
                        "                                    <span class=\"ui-btn-loading-before buy\">加入购物车</span>\n" +
                        "                                    <p>\n" +
                        "                                        <a href=\"javascript:;\" class=\"c-nodo J_delFav_btn\">取消收藏</a>\n" +
                        "                                    </p>\n" +
                        "                                </div>\n" +
                        "                            </div>\n" +
                        "                        </div>");
                })
            } else {
                $.Pop(result.message, "alert", function(){});
            }
        },
        error: function () {
            console.log("网络异常");
        }
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

    /**
     * 设置默认地址
     */
    $(".am-thumbnails").on("click", ".new-option-r", function () {
        var $default_address = $(this).parent('.user-addresslist');
        $.ajax({
            type: 'PUT',
            contentType: "application/json;",
            url: domain + "api/user/updateDefaultAddress/" + $(this).attr("id"),
            success: function(result){
                if(result.status == 0) {
                    $($default_address).addClass("defaultAddr").siblings().removeClass("defaultAddr");
                } else {
                    $.Pop(result.message, "alert", function(){});
                }
            },
            error: function () {
                console.log("网络异常");
            }
        });
    });
    var $ww = $(window).width();
    if ($ww > 640) {
        $("#doc-modal-1").removeClass("am-modal am-modal-no-btn")
    }

    $(".am-thumbnails").on("click", "#am-icon-edit", function () {
        var id = $(this).closest(".user-addresslist ").find(".new-option-r").attr("id");
        $.ajax({
            type: 'GET',
            contentType: "application/json;",
            url: domain + "api/user/getAddressById/" + id,
            success: function(result){
                if(result.status == 0) {
                    var items = result.items;
                    $("#user-name").val(items.receiver);
                    $("#user-phone").val(items.mobile);
                    //$("#user-address").after("<input type=\"hidden\" name=\"harea\" data-id=\"420704\" id=\"harea\" value=\"鄂城区\"><input type=\"hidden\" name=\"hproper\" data-id=\"420700\" id=\"hproper\" value=\"鄂州市\"><input type=\"hidden\" name=\"hcity\" data-id=\"420000\" id=\"hcity\" value=\"湖北省\">");
                    $("#user-intro").val(items.address);
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
