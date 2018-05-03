$(function () {

    loadAddress();
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

function delClick(id){
    $.ajax({
        type: 'DELETE',
        contentType: "application/json;",
        url: domain + "api/user/deleteAddress/" + id,
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
}

function loadAddress(){
    /**
     * 初始化收货地址列表
     */
    $.ajax({
        type: 'GET',
        contentType: "application/json;",
        url: domain + "api/user/findAddress",
        success: function(result){
            if(result.status == 0) {
                $(".am-thumbnails").html("");
                $.each(result.items, function (index, value) {
                    var defaultAddr = "";
                    if (value.is_default == 1) {
                        defaultAddr = "defaultAddr";
                    }
                    $(".am-thumbnails").append("<li class=\"user-addresslist " + defaultAddr + "\">\n" +
                        "                        <span class=\"new-option-r\" id=\"" + value.address_id + "\"><i class=\"am-icon-check-circle\"></i>默认地址</span>\n" +
                        "                        <p class=\"new-tit new-p-re\">\n" +
                        "                            <span class=\"new-txt\">" + value.receiver + "</span>\n" +
                        "                            <span class=\"new-txt-rd2\">" + value.mobile + "</span>\n" +
                        "                        </p>\n" +
                        "                        <div class=\"new-mu_l2a new-p-re\">\n" +
                        "                            <p class=\"new-mu_l2cw\">\n" +
                        "                                <span class=\"title\">地址：</span>\n" +
                        "                                <span class=\"street\">" + value.address_details + "</span></p>\n" +
                        "                        </div>\n" +
                        "                        <div class=\"new-addr-btn\">\n" +
                        /*"                            <a href=\"javascript:void(0);\" id=\"am-icon-edit\"><i class=\"am-icon-edit\"></i>编辑</a>\n" +
                        "                            <span class=\"new-addr-bar\">|</span>\n" +*/
                        "                            <a href=\"javascript:void(0);\" onclick=\"delClick('" + value.address_id + "');\"><i class=\"am-icon-trash\"></i>删除</a>\n" +
                        "                        </div>\n" +
                        "                    </li>");
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