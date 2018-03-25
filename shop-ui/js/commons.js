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