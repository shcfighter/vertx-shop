/**
 * Created by za-wangshenhua on 2018/3/22.
 */
$(function() {
    var pageSize = 12;

    /**
     * 搜索
     */
    $("#ai-topsearch").click(function() {
        var brand = $(".select-result #selectA a").html();
        var category = $(".select-result #selectB a").html();
        var keyword = $("#searchInput").val();
        if (undefined != brand && null != brand && "" != brand) {
            keyword += (" " + brand);
        }
        if (undefined != category && null != category && "" != category) {
            keyword += (" " + category);
        }
        var page = parseInt($(".am-pagination .am-active").attr("page"));
        if(undefined == page || null == page){
            page = 1;
        }
        var data = {
            keyword: keyword,
            pageSize: pageSize,
            page: page
        }
        /**
         * 搜索
         */
        $.ajax({
            type: 'POST',
            contentType: "application/json;",
            url: domain + "api/search/search",
            data: JSON.stringify(data),
            success: function(result){
                if(result.status == 0){
                    $(".am-pagination").html("");
                    var pageTotal = result.total % pageSize == 0 ? parseInt(result.total / pageSize) : parseInt(result.total / pageSize) + 1;
                    if(result.page == 1) {
                        $(".am-pagination").append("<li class=\"am-disabled\" page = " + (result.page - 1) + "><a href=\"javascript:void(0);\">&laquo;</a></li>");
                    } else {
                        $(".am-pagination").append("<li class=\"\" page = " + (result.page - 1) + "><a href=\"javascript:void(0);\">&laquo;</a></li>");
                    }
                    var m = (pageTotal - result.page) >= 5 ? 5 : (9 - (pageTotal - result.page))
                    var start = result.page - m >= 0 ? result.page - m : 1;
                    for (var i = start; i <= pageTotal; i++){
                        if (i == result.page){
                            $(".am-pagination").append("<li class=\"am-active\" page = " + i + "><a href=\"javascript:void(0);\">" + i + "</a></li>");
                        } else {
                            $(".am-pagination").append("<li page = " + i + "><a href=\"javascript:void(0);\">" + i + "</a></li>");
                        }
                        if(i == start + 9){
                            break;
                        }
                    }
                    if(result.page == pageTotal) {
                        $(".am-pagination").append("<li class=\"am-disabled\"  page = " + (result.page + 1) + "><a href=\"javascript:void(0);\">&raquo;</a></li>");
                    } else {
                        $(".am-pagination").append("<li class=\"\"  page = " + (result.page + 1) + "><a href=\"javascript:void(0);\">&raquo;</a></li>");
                    }
                    var items = result.items.items;
                    var $li = $(".boxes");
                    $li.html("");
                    $.each(items, function(index, value) {
                        $li.append("<li><div class=\"i-pic limit\" commodity_id=\"" + value.commodity_id + "\">" +
                            "<img src=\"" + value.image_url[0] + "\"/><p class=\"title fl\">" + value.commodity_name + "</p><p class=\"price fl\"> <b>¥</b><strong>" +
                            value.price +
                            "</strong></p><p class=\"number fl\">销量<span>" +
                            value.sales_volume +
                            "</span></p></div></li>");
                    });
                    var brand = result.items.brand;
                    var $brand_dd = $(".brand");
                    $(".brand dd:gt(0)").remove();
                    $.each(brand, function(index, value) {
                        $brand_dd.append("<dd><a href=\"javascript:void(0)\">" + value + "</a></dd>");
                    });
                    var category = result.items.category;
                    var $category_dd = $(".category");
                    $(".category dd:gt(0)").remove();
                    $.each(category, function(index, value) {
                        $category_dd.append("<dd><a href=\"javascript:void(0)\">" + value + "</a></dd>");
                    });
                    //清空已选
                    $(".eliminateCriteria").click();
                }
            },
            error: function () {
                console.log("网络异常");
            }
        });

        /**
         * 猜你喜欢
         */
        $.ajax({
            type: 'GET',
            contentType: "application/json;",
            url: domain + "api/search/findFavoriteCommodity",
            success: function(result){
                if(result.status == 0){
                    var items = result.items;
                    var $li = $(".side-title");
                    $li.siblings().html("");
                    $.each(items, function(index, value) {
                        $li.after("<li><div class=\"i-pic check\" commodity_id=\"" + value.commodity_id + "\">" +
                            "<img src=\"" + value.image_url[0] + "\"/>" +
                            "<p class=\"title fl\">" + value.commodity_name + "</p>" +
                            "<p class=\"price fl\">" +
                            "<b>¥</b><strong>" + value.price + "</strong>" +
                            "</p><p class=\"number fl\">销量<span>" +
                            value.sales_volume +
                            "</span></p></div></li>");
                    });
                }
            },
            error: function () {
                console.log("网络异常");
            }
        });
    });

    /**
     * 点击具体商品
     */
    $(".i-pic").live("click", function () {
        var commodity_id = $(this).attr("commodity_id");
        window.location.href = "/introduction.html?commodity_id=" + commodity_id;
    });

    var key = getQueryVariable("keyword");
    if(undefined != key && null != key && "" != key){
        $("input[name='index_none_header_sysc']").val(decodeURI(key));
    }
    $("#ai-topsearch").click();

    /**
     * enter键自动提交
     */
    $("#searchInput").bind('keypress',function(event){
        if(event.keyCode == "13")
        {
            $("#ai-topsearch").click();
        }
    });

    $(".am-pagination li").live("click", function () {
        if($(this).hasClass("am-disabled")) {
            return ;
        }
        $(".am-pagination li").removeClass();
        $(this).addClass("am-active");
        $("#ai-topsearch").click();
    });
});
