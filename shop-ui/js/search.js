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
                        $li.append("<li><div class=\"i-pic limit\"><img src=\"http://111.231.132.168:8080/images/59df2e7fN86c99a27.jpg\"/><p class=\"title fl\">" +
                        value.commodity_name +
                            "</p><p class=\"price fl\"> <b>¥</b><strong>" +
                            value.price +
                            "</strong></p><p class=\"number fl\">销量<span>" +
                            "1110" +
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
