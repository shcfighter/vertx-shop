$(function(){

});

/**
 * 翻页
 * @param result
 */
function pageTurning(result, pageSize) {
    $(".am-pagination").html("");
    var pageTotal = result.total % pageSize == 0 ? parseInt(result.total / pageSize) : parseInt(result.total / pageSize) + 1;
    if(result.page == 1) {
        $(".am-pagination").append("<li class=\"am-disabled\" page = " + (result.page - 1) + "><a href=\"javascript:void(0);\">&laquo;</a></li>");
    } else {
        $(".am-pagination").append("<li class=\"\" page = " + (result.page - 1) + "><a href=\"javascript:void(0);\">&laquo;</a></li>");
    }
    var m = (pageTotal - result.page) >= 5 ? 5 : (9 - (pageTotal - result.page))
    var start = result.page - m >= 0 ? result.page - m : 1;
    for (var i = start; i <= pageTotal; i++){
        if (i == result.page){
            $(".am-pagination").append("<li class=\"am-active\" page = " + i + "><a href=\"javascript:void(0);\">" + i + "</a></li>");
        } else {
            $(".am-pagination").append("<li page = " + i + "><a href=\"javascript:void(0);\">" + i + "</a></li>");
        }
        if(i == start + 9){
            break;
        }
    }
    if(result.page == pageTotal) {
        $(".am-pagination").append("<li class=\"am-disabled\"  page = " + (result.page + 1) + "><a href=\"javascript:void(0);\">&raquo;</a></li>");
    } else {
        $(".am-pagination").append("<li class=\"\"  page = " + (result.page + 1) + "><a href=\"javascript:void(0);\">&raquo;</a></li>");
    }
}