/**
 * Created by shwang on 2018/2/27.
 */
var domain = "http://localhost/";
//var domain = "http://111.231.132.168/";

var token = null;
var sessionLoginUser = sessionStorage.getItem("loginUser");
if(undefined != sessionLoginUser && null != sessionLoginUser && "" != sessionLoginUser){
    var loginUser = jQuery.parseJSON(sessionLoginUser);
    token = loginUser.token;
}
console.log("token: " + token);

//全局ajax拦截处理
$.ajaxSetup({
    contentType:"application/json;charset=utf-8",
    headers: {
        'token': token
    },
    statusCode: {
        401: function() {
            if(!$(".xw-pop-modalbox").html()){
                xw.confirm("未登录，请先登录",function(){
                    window.location.href = "/login.html"
                },function(){
                    //alert('你点击了取消')
                });
            }
        },
        404: function() {
            xw.confirm("数据获取/输入失败，没有此服务。404",function(){
                window.location.href = "/login.html"
            },function(){
                //alert('你点击了取消')
            });
        },
        504: function() {
            xw.confirm("数据获取/输入失败，服务器没有响应。504",function(){
                window.location.href = "/login.html"
            },function(){
                //alert('你点击了取消')
            });
        },
        500: function() {
            xw.confirm("服务器有误。500",function(){
                window.location.href = "/login.html"
            },function(){
                //alert('你点击了取消')
            });
        },
        502: function() {
            xw.confirm("网关超时。502",function(){
                window.location.href = "/login.html"
            },function(){
                //alert('你点击了取消')
            });
        }
    }
});

$(function(){
    var sessionLoginUser = sessionStorage.getItem("loginUser");
    if(undefined == sessionLoginUser ||
        null == sessionLoginUser ||
        "" == sessionLoginUser){
        return ;
    }
    if($("#J_MiniCartNum") && $(".cart_num ")){
        loadCartNum();
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