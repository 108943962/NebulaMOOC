function showreplylist(arr) {
    $(".comment-list").addCommentList({data: arr, add: ""});
    $("#comment").click(function () {
        var obj = {};
        obj.img = "res/img.jpg";
        obj.replyName = "匿名";
        obj.content = $("#content").val();
        obj.replyBody = "";
        $("#content").val("");
        if (obj.content != "") {
            var json = {postId: 3, fatherId: -1, content: obj.content};
            postReply(json, function (data) {
                if (data.code == 100) {
                    toastr.success('评论成功');
                } else toastr.warning(data.msg);
            });
        }
    });
}


function getReply(postId) {
    var testJSON = {id: postId};
    showReply(testJSON, function (data) {
            var p = data.data;
            if (data.code == 100) {
                console.log(p);
                showreplylist(doReply(p));
            } else {
                toastr.warning("获取回复失败");
            }
        }
    );
}

getReply(3);