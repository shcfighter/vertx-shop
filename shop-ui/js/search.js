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
                console.log(result);
            },
            error: function () {

            }
        });
    });
});
