/**
 * Created by shwang on 2018/3/22.
 */
$(function() {

    var condition = {
        f1: "家用电器",
        f2: "手机/数码",
        f3: "家具/厨具/家居",
        f4: "男装/女装",
        f5: "美妆/护肤品",
        f6: "户外",
        f7: "酒/饮料",
        f8: "母婴",
    }

    $.each(condition, function (index, value) {
        console.log(index +"-->" + value)
        /**
         * 搜索
         */
        var data = {
            keyword: value
        }
        $.ajax({
            type: 'POST',
            contentType: "application/json;",
            url: domain + "api/search/searchLargeClass",
            data: JSON.stringify(data),
            success: function(result){
                if(result.status == 0){
                    var items = result.items;
                    var $li = $("." + index);
                    var $floodSix = $($li).find(".am-g-fixed");
                    $floodSix.html("<div class=\"am-u-sm-5 am-u-md-3 text-one list\">\n" +
                        "                    <div class=\"word\">\n" +
                        "                        <a class=\"outer\" href=\"javascript:void(0);\"><span class=\"inner\"><b\n" +
                        "                                class=\"text\">核桃</b></span></a>\n" +
                        "                        <a class=\"outer\" href=\"javascript:void(0);\"><span class=\"inner\"><b\n" +
                        "                                class=\"text\">核桃</b></span></a>\n" +
                        "                        <a class=\"outer\" href=\"javascript:void(0);\"><span class=\"inner\"><b\n" +
                        "                                class=\"text\">核桃</b></span></a>\n" +
                        "                        <a class=\"outer\" href=\"javascript:void(0);\"><span class=\"inner\"><b\n" +
                        "                                class=\"text\">核桃</b></span></a>\n" +
                        "                        <a class=\"outer\" href=\"javascript:void(0);\"><span class=\"inner\"><b\n" +
                        "                                class=\"text\">核桃</b></span></a>\n" +
                        "                        <a class=\"outer\" href=\"javascript:void(0);\"><span class=\"inner\"><b\n" +
                        "                                class=\"text\">核桃</b></span></a>\n" +
                        "                        <a class=\"outer\" href=\"javascript:void(0);\"><span class=\"inner\"><b\n" +
                        "                                class=\"text\">核桃</b></span></a>\n" +
                        "                        <a class=\"outer\" href=\"javascript:void(0);\"><span class=\"inner\"><b\n" +
                        "                                class=\"text\">核桃</b></span></a>\n" +
                        "                        <a class=\"outer\" href=\"javascript:void(0);\"><span class=\"inner\"><b\n" +
                        "                                class=\"text\">核桃</b></span></a>\n" +
                        "                    </div>\n" +
                        "                    <a href=\"/introduction.html?commodity_id=" + items[6].commodity_id + "\">\n" +
                        "                        <img src=\"" + items[6].image_url[0] + "\"/>\n" +
                        "                        <div class=\"outer-con \">\n" +
                        "                            <div class=\"title \">\n" +
                        "                                零食大礼包开抢啦\n" +
                        "                            </div>\n" +
                        "                            <div class=\"sub-title \">\n" +
                        "                                当小鱼儿恋上软豆腐\n" +
                        "                            </div>\n" +
                        "                        </div>\n" +
                        "                    </a>\n" +
                        "                    <div class=\"triangle-topright\"></div>\n" +
                        "                </div>" +
                        "                <div class=\"am-u-sm-7 am-u-md-5 am-u-lg-2 text-two big\">\n" +
                        "                    <div class=\"outer-con \">\n" +
                        "                        <div class=\"title \">\n" +
                        "                            " + items[0].brand_name + "\n" +
                        "                        </div>\n" +
                        "                        <div class=\"sub-title \">\n" +
                        "                            ¥" + items[0].price + "\n" +
                        "                        </div>\n" +
                        "                    </div>\n" +
                        "                    <a href=\"/introduction.html?commodity_id=" + items[0].commodity_id + "\"><img src=\"" + items[0].image_url[0] + "\"/></a>\n" +
                        "                </div>\n" +
                        "                <li>\n" +
                        "                    <div class=\"am-u-md-2 am-u-lg-2 text-three\">\n" +
                        "                        <div class=\"boxLi\"></div>\n" +
                        "                        <div class=\"outer-con \">\n" +
                        "                            <div class=\"title \">\n" +
                        "                                " + items[1].brand_name + "\n" +
                        "                            </div>\n" +
                        "                            <div class=\"sub-title \">\n" +
                        "                                ¥" + items[1].price + "\n" +
                        "                            </div>\n" +
                        "                        </div>\n" +
                        "                        <a href=\"/introduction.html?commodity_id=" + items[1].commodity_id + "\"><img src=\"" + items[1].image_url[0] + "\"/></a>\n" +
                        "                    </div>\n" +
                        "                </li>\n" +
                        "                <li>\n" +
                        "                    <div class=\"am-u-md-2 am-u-lg-2 text-three sug\">\n" +
                        "                        <div class=\"boxLi\"></div>\n" +
                        "                        <div class=\"outer-con \">\n" +
                        "                            <div class=\"title \">\n" +
                        "                                " + items[2].brand_name + "\n" +
                        "                            </div>\n" +
                        "                            <div class=\"sub-title \">\n" +
                        "                                ¥" + items[2].price + "\n" +
                        "                            </div>\n" +
                        "                        </div>\n" +
                        "                        <a href=\"/introduction.html?commodity_id=" + items[2].commodity_id + "\"><img src=\"" + items[2].image_url[0] + "\"/></a>\n" +
                        "                    </div>\n" +
                        "                </li>\n" +
                        "                <li>\n" +
                        "                    <div class=\"am-u-sm-4 am-u-md-5 am-u-lg-4 text-five\">\n" +
                        "                        <div class=\"boxLi\"></div>\n" +
                        "                        <div class=\"outer-con \">\n" +
                        "                            <div class=\"title \">\n" +
                        "                                " + items[3].brand_name + "\n" +
                        "                            </div>\n" +
                        "                            <div class=\"sub-title \">\n" +
                        "                                ¥" + items[3].price + "\n" +
                        "                            </div>\n" +
                        "                        </div>\n" +
                        "                        <a href=\"/introduction.html?commodity_id=" + items[3].commodity_id + "\"><img src=\"" + items[3].image_url[0] + "\"/></a>\n" +
                        "                    </div>\n" +
                        "                </li>\n" +
                        "                <li>\n" +
                        "                    <div class=\"am-u-sm-4 am-u-md-2 am-u-lg-2 text-six\">\n" +
                        "                        <div class=\"boxLi\"></div>\n" +
                        "                        <div class=\"outer-con \">\n" +
                        "                            <div class=\"title \">\n" +
                        "                                " + items[4].brand_name + "\n" +
                        "                            </div>\n" +
                        "                            <div class=\"sub-title \">\n" +
                        "                                ¥" + items[4].price + "\n" +
                        "                            </div>\n" +
                        "                        </div>\n" +
                        "                        <a href=\"/introduction.html?commodity_id=" + items[4].commodity_id + "\"><img src=\"" + items[4].image_url[0] + "\"/></a>\n" +
                        "                    </div>\n" +
                        "                </li>\n" +
                        "                <li>\n" +
                        "                    <div class=\"am-u-sm-4 am-u-md-2 am-u-lg-4 text-six\">\n" +
                        "                        <div class=\"boxLi\"></div>\n" +
                        "                        <div class=\"outer-con \">\n" +
                        "                            <div class=\"title \">\n" +
                        "                                " + items[5].brand_name + "\n" +
                        "                            </div>\n" +
                        "                            <div class=\"sub-title \">\n" +
                        "                                ¥" + items[5].price + "\n" +
                        "                            </div>\n" +
                        "                        </div>\n" +
                        "                        <a href=\"/introduction.html?commodity_id=" + items[5].commodity_id + "\"><img src=\"" + items[5].image_url[0] + "\"/></a>\n" +
                        "                    </div>\n" +
                        "                </li>");
                }
            },
            error: function () {
                console.log("网络异常");
            }
        });
    });


});
