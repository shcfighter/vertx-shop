/**
 * Created by shwang on 2018/2/27.
 */
//var domain = "http://localhost/";
var domain = "http://127.0.0.1:880/";

var token = null;
var loginUser = {};
var sessionLoginUser = localStorage.getItem("loginUser");
var tokenExpire = getCookie("token_expire");
if(undefined != sessionLoginUser && null != sessionLoginUser && "" != sessionLoginUser && tokenExpire){
    loginUser = jQuery.parseJSON(sessionLoginUser);
    token = loginUser.token;
}
if (!tokenExpire) {
    localStorage.removeItem("loginUser");
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
            xw.alert("数据获取/输入失败，没有此服务。404");
        },
        504: function() {
            xw.alert("数据获取/输入失败，服务器没有响应。504")
        },
        500: function() {
            xw.alert("服务器有误。500");
        },
        502: function() {
            xw.alert("网关超时。502");
        }
    }
});

$(function(){
    var sessionLoginUser = localStorage.getItem("loginUser");
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
                    localStorage.removeItem("loginUser");
                    delCookie("token_expire");
                    //$(".login_user").html("<a href=\"/login.html\" target=\"_top\" class=\"h\">亲，请登录</a>  <a href=\"/register.html\" target=\"_top\">免费注册</a>");
                    window.location.href = "/login.html";
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

//写cookies
function setCookie(name,value)
{
    var Days = 30;
    var exp = new Date();
    exp.setTime(exp.getTime() + Days*24*60*60*1000);
    document.cookie = name + "="+ escape (value) + ";expires=" + exp.toGMTString();
}
//读取cookies
function getCookie(name)
{
    var arr,reg=new RegExp("(^| )"+name+"=([^;]*)(;|$)");

    if(arr=document.cookie.match(reg))

        return unescape(arr[2]);
    else
        return null;
}
//删除cookies
function delCookie(name)
{
    var exp = new Date();
    exp.setTime(exp.getTime() - 1);
    var cval=getCookie(name);
    if(cval!=null)
        document.cookie= name + "="+cval+";expires="+exp.toGMTString();
}
function setCookie(name,value,time)
{
    var strsec = getsec(time);
    var exp = new Date();
    exp.setTime(exp.getTime() + strsec*1);
    document.cookie = name + "="+ escape (value) + ";expires=" + exp.toGMTString();
}
/**
 * s20是代表20秒
 * h是指小时，如12小时则是：h12
 * d是天数，30天则：d30
 * @param str
 * @returns {number}
 */
function getsec(str)
{
    var str1=str.substring(1,str.length)*1;
    var str2=str.substring(0,1);
    if (str2=="s")
    {
        return str1*1000;
    }
    else if (str2=="h")
    {
        return str1*60*60*1000;
    }
    else if (str2=="d")
    {
        return str1*24*60*60*1000;
    }
}