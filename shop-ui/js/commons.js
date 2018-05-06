/**
 * Created by za-wangshenhua on 2018/2/27.
 */
var domain = "http://localhost:8787/";
//var domain = "http://111.231.132.168/";

//全局ajax拦截处理
$.ajaxSetup({
    contentType:"application/json;charset=utf-8",
    statusCode: {
        401: function() {
            $.Pop('未登录，请先登录', 'confirm', function(){window.location.href = "/login.html";});
        },
        404: function() {
            $.Pop("数据获取/输入失败，没有此服务。404", "alert", function(){});
        },
        504: function() {
            $.Pop("数据获取/输入失败，服务器没有响应。504", "alert", function(){});
        },
        500: function() {
            $.Pop("服务器有误。500", "alert", function(){});
        },
        502: function() {
            $.Pop("网关超时。502", "alert", function(){});
        }
    }
});

$(function(){
    loadCartNum();
    var sessionLoginUser = sessionStorage.getItem("loginUser");
    if(undefined == sessionLoginUser ||
        null == sessionLoginUser ||
        "" == sessionLoginUser){
        return ;
    }
    var loginUser = jQuery.parseJSON(sessionLoginUser);
    $(".login_user").html("亲，<a href=\"/person/index.html\" target=\"_top\" class=\"h\">" + loginUser.loginName + "</a> " +
        " <a href=\"javascript:void(0);\" target=\"_top\" class=\"h\" id=\"logout\">退出</a>");

    $(".login_user").on("click", "#logout", function () {
        $.ajax({
            type: 'GET',
            contentType: "application/json;",
            url: domain + "api/user/logout",
            success: function(result){
                if(result.status == 0){
                    sessionStorage.removeItem("loginUser");
                    $(".login_user").html("<a href=\"/login.html\" target=\"_top\" class=\"h\">亲，请登录</a>  <a href=\"/register.html\" target=\"_top\">免费注册</a>");
                }
            },
            error: function () {
                console.log("网络异常");
            }
        });
    });

});

/**
 * url获取参数值
 * @param variable
 * @returns {*}
 */
function getQueryVariable(variable)
{
    var query = window.location.search.substring(1);
    var vars = query.split("&");
    for (var i=0;i<vars.length;i++) {
        var pair = vars[i].split("=");
        if(pair[0] == variable){return pair[1];}
    }
    return(false);
}

/**
 * 获取购物车数量
 */
function loadCartNum() {
    $.ajax({
        type: "GET",
        contentType: "application/json;",
        url: domain + "api/cart/findCartRowNum",
        success: function(result){
            if(result.status == 0){
                $("#J_MiniCartNum").html("(" + result.items + ")");
                $(".cart_num ").html(result.items);
            }
        },
        error: function () {
            console.log("网络异常");
        }
    });
}