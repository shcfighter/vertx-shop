/**
 * Created by shwang on 2018/3/28.
 */
$(function () {
    $(".submit").click(function(){
        var keyword = encodeURI($("input[name='index_none_header_sysc']").val());
        window.location.href = "/search.html?keyword=" + keyword;
    });
    /**
     * enter键自动提交
     */
    $("#searchInput").bind('keypress',function(event){
        if(event.keyCode == "13") {
            $("#ai-topsearch").click();
        }
    });

    $("#shopCart").click(function () {
        window.location.href = "/shopcart.html";
    });

    if (!token) {
        $(".ibar_login_box").hide();
    } else {
        $(".user_info").html("<li>用户名" + loginUser.loginName + "</li><li>级&nbsp;别普通会员</li>");
    }

    $("#prof").click(function () {
        if (token) {
            $(".ibar_login_box").show();

        } else {
            window.location.href = "/login.html";
        }
    });
})