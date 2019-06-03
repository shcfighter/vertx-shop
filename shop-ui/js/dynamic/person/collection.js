$(function () {

    loadCollection(1);

    function loadCollection(page){
        /**
         * 初始化收货地址列表
         */
        $.ajax({
            type: 'GET',
            contentType: "application/json;",
            url: domain + "api/collection/findCollection?pageNum=" + page,
            success: function(result){
                if(result.status == 0) {
                    $(".s-more-btn").attr("data-screen", page);
                    $.each(result.items, function (index, value) {
                        $(".s-content").append("<div class=\"s-item-wrap\">\n" +
                            "                            <div class=\"s-item\">\n" +
                            "                                <div class=\"s-pic\">\n" +
                            "                                    <a href=\"/introduction.html?commodity_id=" + value.commodity_id + "\" class=\"s-pic-link\">\n" +
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
                            "                                    <span class=\"ui-btn-loading-before buy\" commodity_id=\"" + value.commodity_id + "\">加入购物车</span>\n" +
                            "                                    <i class=\"am-icon-shopping-cart\"></i>\n" +
                            "                                    <span class=\"ui-btn-loading-before collection\" collection_id=\"" + value._id + "\">取消收藏</span>\n" +
                            "                                </div>\n" +
                            "                            </div>\n" +
                            "                        </div>");
                    })
                } else {
                    xw.alert(result.message)
                }
            },
            error: function () {
                console.log("网络异常");
            }
        });
    }

    /**
     * 加入购物车
     */
    $(".s-content").on("click", ".buy", function () {
        var data ={
            commodity_id: parseInt($(this).attr("commodity_id")),
        }
        console.log("commodity", data)
        $.ajax({
            type: 'POST',
            contentType: "application/json;",
            url: domain + "api/cart/insertCart",
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

    /**
     * 取消收藏
     */
    $(".s-content").on("click", ".collection", function () {
        $.ajax({
            type: 'DELETE',
            contentType: "application/json;",
            url: domain + "api/collection/removeCollection/" + $(this).attr("collection_id"),
            data: {},
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

    $(".s-more-btn").click(function () {
        var page = $(this).attr("data-screen");
        loadCollection(parseInt(page) + 1);
    });
});
