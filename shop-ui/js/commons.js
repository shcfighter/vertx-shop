/**
 * Created by za-wangshenhua on 2018/2/27.
 */
var domain = "http://localhost:8787/";
//var domain = "http://111.231.132.168/";

$(function(){
    var sessionLoginUser = sessionStorage.getItem("loginUser");
    if(undefined == sessionLoginUser ||
        null == sessionLoginUser ||
        "" == sessionLoginUser){
        return ;
    }
    var loginUser = jQuery.parseJSON(sessionLoginUser);
    $(".login_user").html("亲，<a href=\"/person/index.html\" target=\"_top\" class=\"h\">" + loginUser.loginName + "</a>");
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