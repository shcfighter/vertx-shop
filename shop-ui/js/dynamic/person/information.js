$(function () {
    $.ajax({
        type: 'GET',
        contentType: "application/json;",
        url: domain + "api/user/getUserInfo",
        success: function(result){
            if(result.status == 0) {
                console.log(result);
                var items = result.items;
                $(".info-m").find("i").text(items.login_name);
                $("#login_name").val(items.login_name);
                $("#user_name").val(items.user_name);
                $("#user_phone").val(items.mobile);
                $("#user_email").val(items.email);
                $(":radio[name='sex'][value='" + items.sex + "']").prop("checked", "checked");
                if(items.photo_url){
                    $(".am-img-thumbnail").attr("src", items.photo_url);
                }
                $("#birthday").val(items.birthday);
            }
        },
        error: function () {
            console.log("网络异常");
        }
    });

    $(".info-btn").click(function () {
        var data = {
            login_name: $("#login_name").val(),
            user_name: $("#user_name").val(),
            mobile: $("#user_phone").val(),
            email: $("#user_email").val(),
            sex: parseInt($("input[name='sex']:checked").val()),
            photo_url: $(".am-img-thumbnail").attr("src"),
            birthday: Date.parse(new Date($("#birthday").val()))
        }
        $.ajax({
            type: 'POST',
            contentType: "application/json;",
            url: domain + "api/user/saveUserInfo",
            data: JSON.stringify(data),
            success: function(result){
                if(result.status == 0) {
                    $.Pop(result.message, "alert", function(){});
                } else {
                    $.Pop(result.message, "alert", function(){});
                }
            },
            error: function () {
                console.log("网络异常");
            }
        });
    });
});

function submitform(){
    var formData = new FormData();
    formData.append("file", $(".inputPic")[0].files[0]);
    $.ajax({
        url: domain + "api/uploadImageAvatar",
        type: "POST",
        data: formData,
        processData: false,
        contentType: false,
        success: function (result) {
            console.log(result);
            if(result.status == 0) {
                $(".am-img-thumbnail").attr("src", result.items);
            } else {
                $.Pop(result.message, "alert", function(){});
            }
        },
        error: function () {
            console.log("网络异常");
        }
    });
}