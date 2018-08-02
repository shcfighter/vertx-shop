/**
 * Created by shwang on 2018/3/22.
 */
$(function() {

    var condition = {
        f1: "家用电器",
        f2: "手机/数码/电视/笔记本电脑",
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
                    var temp = document.getElementById(index);
                    aTpl.template(temp).render(result, function(html){
                        $("." + index).html(html);
                    });
                }
            },
            error: function () {
                console.log("网络异常");
            }
        });
    });

    $(".dl-sort").find("dd").find("a").click(function () {
        window.location.href = "/search.html?keyword=" + $(this).attr("title");
    });
});
