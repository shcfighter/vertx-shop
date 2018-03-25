/**
 * Created by za-wangshenhua on 2018/3/22.
 */
$(function() {
    /**
     * 搜索
     */
    $("#ai-topsearch").click(function() {
        var data = {
            keyword: $("#searchInput").val()
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
                    var items = result.items;
                    var $li = $(".boxes");
                    $li.html("");
                    $.each(items, function(index, value) {
                        $li.append("<li><div class=\"i-pic limit\"><img src=\"" +
                            value.image_url +
                            "\"/><p class=\"title fl\">" +
                            value.commodity_name +
                            "</p><p class=\"price fl\"> <b>¥</b><strong>" +
                            value.price +
                            "</strong></p><p class=\"number fl\">销量<span>" +
                            value.sales_volume +
                            "</span></p></div></li>");
                    });
                }
            },
            error: function () {
                console.log("errror");
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
                        $li.after("<li><div class=\"i-pic check\">" +
                            "<img src=\"" + value.image_url + "\"/>" +
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
                console.log("errror");
            }
        });
    });
});
