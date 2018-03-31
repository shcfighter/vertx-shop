/**
 * Created by za-wangshenhua on 2018/3/28.
 */
$(function () {
    $(".submit").click(function(){
        var keyword = encodeURI($("input[name='index_none_header_sysc']").val());
        console.log(keyword);
        window.location.href = "/search.html?keyword=" + keyword;
    });
})