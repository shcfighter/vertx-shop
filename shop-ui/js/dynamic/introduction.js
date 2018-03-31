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
            url: domain + "api/search/findCommodityById/" + id,
            success: function(result){
                if(result.status == 0){
                    var items = result.items;
                    $(".tb-detail-hd h1").html(items.commodity_name);
                    $(".tb-thumb").html("<li class=\"tb-selected\"><div class=\"tb-pic tb-s40\"><a href=\"javascript:void(0)\"><img src=\"" + items.image_url + "\" mid=\"" + items.image_url + "\" big=\"" + items.image_url + "\"></a></div></li>"
                    );
                }
            },
            error: function () {
                console.log("网络异常");
            }
        });
    } else {
        alert("获取商品信息失败！");
    }
});