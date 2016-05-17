/**
 * Created by Administrator on 2016/4/25.
 */
$(function(){
    function select1(elm,evt){
        $(elm).click(function(e){
            $(e.target).addClass("hover").siblings().removeClass("hover");
            var id=$(e.target).data("id");
            $(evt+id).show().siblings().hide();
        });
    }
    var elm1=select1(".service_us .div_top>div",".service_us .div_bot .service_");
    var elm2=select1("#rank .title>div","#rank .main_view .service_");
    ////党员排名
    //var arr=[];
    //for(var i=0;i<8;i++){
    //    var html='<li class="content"><span class="'+(i==0?"rankBgColor1":i==1?"rankBgColor2":i==2?"rankBgColor3":"")+'">'+(i+1)+'</span><span>【酒仙桥】老王</span><span class="'+(i==0?"rankColor1":i==1?"rankColor2":i==2?"rankColor3":"")+'">30823分</span></li>';
    //    arr.push(html);
    //}
    //$("#rank .service_1 ul").append(arr.join(""));
    //$("#rank .service_2 ul").append(arr.join(""));

    ////党员活动页：文化活动
    //function page(elm,evt){
    //    $(elm).jqPaginator({
    //        totalPages: 4,
    //        visiblePages: 4,
    //        currentPage: 1,
    //        //first: '<li class="first"><a href="javascript:void(0);">首页<\/a><\/li>',
    //        //prev: '<li class="prev"><a href="javascript:void(0);">&laquo;<\/a><\/li>',
    //        next: '<li class="next"><a href="javascript:void(0);">&gt;</a><\/li>',
    //        //last: '<li class="last"><a href="javascript:void(0);">末页<\/a><\/li>',
    //        page: '<li class="page"><a href="javascript:void(0);">{{page}}<\/a><\/li>',
    //        onPageChange: function (n) {
    //            //$(evt+n).show().siblings().hide();
    //            //switch (evt){
    //            //    case "#ser_list_text .list_":
    //            //        //组织服务
    //            //        for(var i= 0,html2=[];i<5;i++){
    //            //            var com1='<li><h4>1、留学人员来京创业、工作，不受出国前户籍所在地影响，可长期居留或短期工作，来去自由。</h4><p>Lorem ipsum dolor sit amet, consectetur adipisicing elit. Ab explicabo facere harum laborum, minus nostrum  ipsum, obcaecati optio reprehenderit velit.</p></li>';
    //            //            html2.push(com1);
    //            //        }
    //            //        $(evt+n+" ul").html(html2.join(""));
    //            //        break;
    //            //}
    //
    //        }
    //    });
    //}
    //var ser_list=page("#ser_list","#ser_list_text .list_");
//单选框调整
    $('input[name="radio-btn"]').wrap('<div class="radio-btn"><i></i></div>');
    $(".radio-btn").on('click', function () {
        var _this = $(this),
            block = _this.parent().parent();
        block.find('input:radio').attr('checked', false);
        block.find(".radio-btn").removeClass('checkedRadio');
        _this.addClass('checkedRadio');
        _this.find('input:radio').attr('checked', true);
    });
});
