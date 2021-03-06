<%@ page language="java" contentType="text/html; charset=utf-8"
    pageEncoding="utf-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
    <meta name="renderer" content="webkit">
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
    <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1">
    <title>微众</title>
    <link rel="stylesheet" href="../wzBac/lib/layui/css/layui.css">
    <style>
        .wzListH1{
            padding:10px 0 20px 0;
            font-size: 26px;
            color: #333;
        }
    </style>
</head>
<body class="layui-layout-body">
<div class="layui-layout layui-layout-admin">
    <div class="layui-header">

    </div>

    <div class="layui-side layui-bg-black">
        <div class="layui-side-scroll">
            <!-- 左侧导航区域（可配合layui已有的垂直导航） -->

        </div>
    </div>

    <div class="layui-body">
        <!-- 内容主体区域 -->
        <div style="padding: 15px;">
            <h1 class="wzListH1">微众财富信息导出</h1>
            <div class="layui-form-item">
               
                <div class="layui-inline">
                    <label class="layui-form-label">选择时间</label>
                    <div class="layui-input-inline">
                        <input type="text" class="layui-input" id="st" placeholder="开始时间">
                    </div>
                </div>
                <div class="layui-inline">
                    <button class="layui-btn layui-btn-normal" onclick="exports()">导出</button>
                </div>

                </div>
            <table class="layui-hide" id="wzTable"></table>
        </div>
    </div>

    <div class="layui-footer">
        <!-- 底部固定区域 -->
        © layui.com - 底部固定区域
    </div>
</div>

<script src="../wzBac/js/jquery-3.2.1.min.js"></script>
<script src="../wzBac/js/common.js"></script>
<script src="../wzBac/lib/layui/layui.js"></script>
<script type="text/javascript">
function exports(){
	
	   var stime = $("#st").val();
	   window.location.href='../manageWB/exportWB?time='+stime;
}
layui.use(['laydate'], function(){
	var laydate=layui.laydate;
	laydate.render({
	    elem: '#st'
	});
});


</script>
</body>
</html>