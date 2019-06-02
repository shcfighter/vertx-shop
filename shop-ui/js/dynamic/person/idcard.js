$(function () {

    /**
     * 初始化加载
     */
    $.ajax({
        type: 'GET',
        contentType: "application/json;",
        url: domain + "api/user/getIdcardCertified",
        success: function(result){
            if(result.status == 0) {
                var item = result.items;
                $("#user-name").val(item.real_name);
                $("#user-IDcard").val(item.id_card);
                $("#id_card_positive_url").attr("src", item.id_card_positive_url);
                $("#id_card_negative_url").attr("src", item.id_card_negative_url);
            } else {
                xw.alert(result.message)
            }
        },
        error: function () {
            console.log("网络异常");
        }
    });

    /**
     * 上传图片
     */
    $(".inputPic").change(function() {
        var formData = new FormData();
        formData.append("file", $(this)[0].files[0]);
        var _this = this;
        $('div.loading').show();
        $.ajax({
            url: domain + "api/uploadImageAvatar",
            type: "POST",
            data: formData,
            processData: false,
            contentType: false,
            success: function (result) {
                $('div.loading').hide();
                if(result.status == 0) {
                    $(_this).closest(".cardPic").find("img").attr("src", result.items);
                    $(_this).closest("li").find(".cardExample").find("img").attr("src", result.items);
                } else {
                    xw.alert(result.message)
                }
            },
            error: function () {
                $('div.loading').hide();
                console.log("网络异常");
            }
        });
    });

    /**
     *  保存认证信息
     */
    $(".am-btn-danger").click(function () {
        var data = {
            real_name: $("#user-name").val(),
            id_card: $("#user-IDcard").val(),
            id_card_positive_url: $("#id_card_positive_url").attr("src"),
            id_card_negative_url: $("#id_card_negative_url").attr("src")
        }
        $.ajax({
            type: 'PUT',
            contentType: "application/json;",
            url: domain + "api/user/idcardCertified",
            data: JSON.stringify(data),
            success: function(result){
                if(result.status == 0) {
                    xw.alert(result.message)
                } else {
                    xw.alert(result.message)
                }
            },
            error: function () {
                console.log("网络异常");
            }
        });
    });

});
