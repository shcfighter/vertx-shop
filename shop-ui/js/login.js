/**
 * Created by za-wangshenhua on 2018/2/27.
 */
$(function() {
    /**
     * 登录
     */
    $(".am-btn").click(function() {
        var data = {
            loginName: $("input[name='loginName']").val(),
            pwd: $("input[name='pwd']").val()
        }
        $.ajax({
            type: 'POST',
            contentType: "application/json;",
            url: domain + "api/user/login",
            data: JSON.stringify(data),
            success: function(result){
                window.location.href = "index.html";
            },
            error: function () {
                
            }
        });
    });

    /**
     * 注册
     */
    $("input[type='submit']").click(function() {
        var registerDiv = $(this).closest(".am-tab-panel");

        var data = {
            type: $(this).attr("name"),
            loginName: registerDiv.find("input[name='loginName']").val(),
            password: registerDiv.find("input[name='password']").val(),
            passwordConfirm: registerDiv.find("input[name='passwordConfirm']").val(),
        }
        $.ajax({
            type: 'POST',
            contentType: "application/json;",
            url: domain + "api/user/register",
            data: JSON.stringify(data),
            success: function(result){
                window.location.href = "index.html";
            },
            error: function () {

            }
        });
    });
});