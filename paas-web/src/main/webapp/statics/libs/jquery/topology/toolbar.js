// 页面工具栏
function showJTopoToobar(stage){
	var toobarDiv = $('<div class="jtopo_toolbar">').html(''
	  +'<label class="i-checks i-checks-sm m-r-xs"> <input type="radio" name="modeRadio" value="normal" checked id="r1"><i></i>默认</label>'
		+'<label class="i-checks i-checks-sm m-r-xs"> <input type="radio" name="modeRadio" value="select" id="r2"><i></i>框选</label>'
		+'<label class="i-checks i-checks-sm m-r-xs"> <input type="radio" name="modeRadio" value="drag" id="r3"><i></i>平移</label>'
		+'<label class="i-checks i-checks-sm m-r-md"> <input type="radio" name="modeRadio" value="edit" id="r4"><i></i>编辑</label>'
		+'<button class="btn m-b-xs btn-xs btn-primary m-r-xs"  id="centerButton"><i class="fa fa-align-center"></i> 居 中</button>'
		+'<button class="btn m-b-xs btn-xs btn-info m-r-xs"  id="fullScreenButton"><i class="icon-size-fullscreen"></i> 全 屏</button>'
		+'<button class="btn m-b-xs btn-xs btn-success m-r-xs"  id="zoomOutButton"><i class="icon-magnifier-add"></i>  放 大</button>'
		+'<button class="btn m-b-xs btn-xs btn-danger m-r-md"  id="zoomInButton"><i class="icon-magnifier-remove"></i>  缩 小</button>'
		+'<label class="i-checks i-checks-sm m-r" ><input type="checkbox" id="zoomCheckbox" ><i></i>鼠标缩放</label>'
		+'<input type="text" id="findText" class="form-control" value="" onkeydown="findButton.click()" style="display: inline-block;width: 200px;height: 26px">'
		+'<button class="btn m-b-xs btn-xs btn-primary m-l-xs m-t-xs" id="findButton"><i class="icon-magnifier"></i> 查 询</button>'
		+'<button class="btn btn-xs btn-info pull-right m-t-xs" id="exportButton"><i class="fa fa-floppy-o"></i> 导出PNG</button>');
	$('#content').prepend(toobarDiv);

	// 工具栏按钮处理
	$("input[name='modeRadio']").click(function(){			
		stage.mode = $("input[name='modeRadio']:checked").val();
	});
	$('#centerButton').click(function(){
		stage.centerAndZoom(); //缩放并居中显示
	});
	$('#zoomOutButton').click(function(){
		stage.zoomOut();
	});
	$('#zoomInButton').click(function(){
		stage.zoomIn();
	});
	$('#exportButton').click(function(){
		stage.saveImageInfo();
	});
	$('#zoomCheckbox').click(function(){
		$(this).toggleClass('checked');
		if($('#zoomCheckbox').hasClass('checked')){
			stage.wheelZoom = 1.15; // 设置鼠标缩放比例
		}else{
			stage.wheelZoom = null; // 取消鼠标缩放比例
		}
	});
	$('#fullScreenButton').click(function(){
		runPrefixMethod(stage.canvas, "RequestFullScreen")
	});

	// 查询
	$('#findButton').click(function(){
		var text = $('#findText').val().trim();
		var nodes = stage.find('node[text="'+text+'"]');
		if(nodes.length > 0){
			var node = nodes[0];
			node.selected = true;
			var location = node.getCenterLocation();
			// 查询到的节点居中显示
			stage.setCenter(location.x, location.y);
			
			function nodeFlash(node, n){
				if(n == 0) {
					node.selected = false;
					return;
				};
				node.selected = !node.selected;
				setTimeout(function(){
					nodeFlash(node, n-1);
				}, 300);
			}
			
			// 闪烁几下
			nodeFlash(node, 6);
		}
	});
}

var runPrefixMethod = function(element, method) {
	var usablePrefixMethod;
	["webkit", "moz", "ms", "o", ""].forEach(function(prefix) {
		if (usablePrefixMethod) return;
		if (prefix === "") {
			// 无前缀，方法首字母小写
			method = method.slice(0,1).toLowerCase() + method.slice(1);
		}
		var typePrefixMethod = typeof element[prefix + method];
		if (typePrefixMethod + "" !== "undefined") {
			if (typePrefixMethod === "function") {
				usablePrefixMethod = element[prefix + method]();
			} else {
				usablePrefixMethod = element[prefix + method];
			}
		}
	}
);

return usablePrefixMethod;
};

// runPrefixMethod(this, "RequestFullScreen");
// if (typeof window.screenX === "number") {
// var eleFull = canvas;
// eleFull.addEventListener("click", function() {
// 	if (runPrefixMethod(document, "FullScreen") || runPrefixMethod(document, "IsFullScreen")) {
// 		runPrefixMethod(document, "CancelFullScreen");
// 		this.title = this.title.replace("退出", "");
// 	} else if (runPrefixMethod(this, "RequestFullScreen")) {
// 		this.title = this.title.replace("点击", "点击退出");
// 	}
// });
// } else {
// alert("浏览器不支持");
// }
