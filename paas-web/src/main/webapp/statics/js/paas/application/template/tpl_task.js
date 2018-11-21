/**
 * Created by Zhang Haijun on 2016/9/2.
 */
(function(){
	//选择服务器文件CTRL
	app.controller('selectServerFileModalCtrl', ['$scope', 'httpLoad', '$modalInstance',
		function($scope, httpLoad, $modalInstance) {
			$scope.itemsByPage = 5;//定义每页的条数
			//加载服务器列表
			httpLoad.loadData({
				url:'/image/listAll',
				noParam:true,
				success:function(data){
					$scope.serverListData= data.data;
					$scope.total = data.data.length;
					$scope.isDataLoad = true
				}
			});
			//全选
			$scope.selectAll = function(){
				for(var a in $scope.serverListData){
					$scope.serverListData[a].isSelected = $scope.isSelectAll;
				}
			}
			$scope.ok = function () {
				var data = [];
				for(var a in $scope.serverListData){
					var item = $scope.serverListData[a];
					if(item.isSelected){
						data.push(item);
					}
				}
				$modalInstance.close(data);
			}
			$scope.cancle = function () {
				$modalInstance.dismiss('cancel');
			};
		}
	]);
	//websocket文件上传指令
	app.directive('ngFileUpload', ['$rootScope', 'httpLoad', function ($rootScope, httpLoad) {
		return {
			restrict: 'EA',
			scope:true,
			link: function (scope, element, attrs) {
				scope.item = {};
				$('#btnFileUpload').on('change', function (event) {
					var files = event.target.files, list = [];
					if(/[\u4e00-\u9fa5]/.test(files[0].name)){
						$rootScope.pop('文件名不允许存在中文','error');
					};
					//对文件大小和类型进行过滤
					var arr = files[0].name.split('.');
					if ((['tar','json'].indexOf(arr[arr.length - 1]) == -1) && ['img','json'].indexOf(arr[arr.length - 1]) == -1) {
						scope.$apply(function () {
							$rootScope.pop('仅支持上传【tar】或者【img】类型的文件', 'error');
						});
						return;
					}
					$('#btnFileUpload').html($('#btnFileUpload').html());
						scope.$apply(function () {
							scope.gridinPullName = files[0].name;
						});
						scope.item  = {file: files[0]};;
				});
				var socket ;
				scope.cancelFile = function (item) {
					if(socket) {
						socket.send(JSON.stringify({
							'UPLOAD_CANCEL': 'UPLOAD_CANCEL'
						}));
						scope.updataBtn = false
					}else {
						scope.pop('没有文件不能取消上传','error');
					}

				}

				scope.upload = function (item) {
					var project = scope.scriptItemSpace.selected.name;
					  if(scope.imageshow){
				        	if(project){
								
				        	}else{
				        		scope.pop("请填写项目名称","error");return
				        	}
				        	
				        }else {
										
						}
					socket = new WebSocket('ws://' + location.host +scope.websocketUrl);
					var i = 0;var startSize = 0,endSize = 0;
					var paragraph = 4 * 1024 * 1024;    //以4MB为一个分片
					var count = parseInt(item.file.size / paragraph) + 1;
					socket.onopen = function () {
						item.isUploading = true;
						socket.send(JSON.stringify({
								'filename': scope.gridinPullName,
								registryId:scope.warehoseItem,
								'upload': 'file'
							}));


					};
					socket.onmessage = function (event) {
						var sendFile = function(){
							if(startSize < item.file.size) {
								var blob;
								startSize = endSize;
								endSize += paragraph;

								if (item.file.webkitSlice) {
									blob = item.file.webkitSlice(startSize, endSize);
								} else if (item.file.mozSlice) {
									blob = item.file.mozSlice(startSize, endSize);
								} else {
									blob = item.file.slice(startSize, endSize);
								}
								var reader = new FileReader();
								reader.readAsArrayBuffer(blob);

								reader.onload = function loaded(evt) {
									if(socket.readyState == 3){
										return
									}
									var result = evt.target.result;
									i++;
									var isok = (i / count) * 100;
									item.progress = parseInt(isok);

									socket.send(result);
								};
							}else{
								item.progress = 100;
								socket.send(JSON.stringify({
									'sendover': 'sendover'
								}));
							}
							}
						scope.updataBtn = true
						item.isUploading = true;
						item.isCancel = false;
						var obj = JSON.parse(event.data);
						console.log(obj)
						if (obj.category == "UPLOAD_ACK") {
							item.filePath = obj.content;
							sendFile();
						} else if (obj.category == 'UPLOAD') {
							if (obj.content == 'SAVE_FAILURE') {
							 	scope.updataBtn = false;
								scope.pop('文件上传失败','error');
							} else if (obj.content == 'SAVE_SUCCESS') {
								sendFile();
							}else if (obj.content.indexOf('TRUE') >= 0) {
								 scope.pop("文件上传成功");
								var filePath = obj.content.substr(obj.content.indexOf(',') + 1);
								var project = scope.scriptItemSpace.selected.name;
								var params = {};
								params.registryId =scope.warehoseItem;
								params.filePath = filePath;
								if(project){
									params.project = project;
					        	}else{
					        		params.project = null;		
					        	}
								  
							
								httpLoad.loadData({
									url: '/image/load',
									data:params,
									noParam: true,
									success: function (data) {
										if(data.success){
											 scope.pop(data.message);
											 scope.cancel();
											 scope.updataBtn = false;
										}else{
											 scope.pop(data.message);
											 scope.updataBtn = false;
										}
									}
								});
								item.isReady = true; item.isSuccess = true;item.isUploading = false;
								socket.close();
							}
						} else if (obj.category == 'UPLOAD_CANCEL') {
							  if (obj.content == 'CANCEL_SUCCESS') {
									item.progress = 0;
								}
							scope.pop('已取消文件上传','info');
							item.progress = 0;
							scope.updataBtn = false
							socket.close();
						}
						scope.$apply(scope.progress);
					};
				};
			}
		}
	}]);

	//websocket部署
	app.directive('ngDeployCourse', ['$rootScope', '$modal', 'httpLoad', function ($rootScope, $modal, httpLoad) {
		return {
			restrict: 'EA',
			scope:{
				websocketUrl:'=',
			},
		link: function (scope, element, attrs) {
				var upload = function (item) {
					var socket = new WebSocket('ws://' + location.host +websocketUrl);
					var i = 0;var startSize = 0,endSize = 0;
					var paragraph = 4 * 1024 * 1024;    //以4MB为一个分片
					var count = parseInt(item.file.size / paragraph) + 1;
					socket.onopen = function () {
						item.isUploading = true;
						socket.send(JSON.stringify({
								'filename': scope.gridinName,
								'upload': 'file'
							}));
						//取消上传
						item.cancel = function () {
							item.progress = 0;
							socket.send(JSON.stringify({
								'UPLOAD_CANCEL': 'UPLOAD_CANCEL'
							}));
							item.isUploading = false;
						};
					};
					socket.onmessage = function (event) {
						var sendFile = function(){
							if(startSize < item.file.size) {
								var blob;
								startSize = endSize;
								endSize += paragraph;

								if (item.file.webkitSlice) {
									blob = item.file.webkitSlice(startSize, endSize);
								} else if (item.file.mozSlice) {
									blob = item.file.mozSlice(startSize, endSize);
								} else {
									blob = item.file.slice(startSize, endSize);
								}
								var reader = new FileReader();
								reader.readAsArrayBuffer(blob);

								reader.onload = function loaded(evt) {
									var result = evt.target.result;
									i++;
									var isok = (i / count) * 100;
									item.progress = parseInt(isok);
									socket.send(result);
								};
							}else{
								item.progress = 100;
								socket.send(JSON.stringify({
									'sendover': 'sendover'
								}));
							}
							}
						item.isUploading = true;
						item.isCancel = false;
						var obj = JSON.parse(event.data);
						if (obj.category == "UPLOAD_ACK") {
							item.filePath = obj.content;
							sendFile();
						} else if (obj.category == 'UPLOAD') {
							if (obj.content == 'SAVE_FAILURE') {
								item.isUploading = false;
								scope.pop('文件上传失败','error');
							} else if (obj.content == 'SAVE_SUCCESS') {
								sendFile();
							} else if (obj.content == 'TRUE') {
								if(item.fileId){
									httpLoad.loadData({
										url: '/image/remove',
										data:{
											id:item.fileId
										},
										success: function (data) {
											if(data.success){
												createFile(item);
											}
										}
									});
								}else{
									createFile(item);
								}
								item.isReady = true; item.isSuccess = true;item.isUploading = false;
								socket.close();
							}
						} else if (obj.category == 'UPLOAD_CANCEL') {
							scope.pop('已取消文件上传','info');
							item.progress = 0;
							item.isCancel = true;
							socket.close();
						}
						scope.$apply(scope.progress);
					};
				};
				}
		}
	}]);

	(function($){
		$.fn.scrollGotoHere = function(){
			var heightPx = $(this).offset().top;
			if(heightPx){
				heightPx=heightPx-150
			}
			$('html,body').animate({scrollTop: heightPx+'px'}, 200);
		};
	})(jQuery);

	//在线编辑指令
	app.directive('ngCodeMirror', ['$timeout','$rootScope', function ($timeout,$rootScope) {
		return {
			restrict: 'EA',
			scope:{
				codeMirror: '='
			},
			link: function (scope, element, attrs) {
				var editor = $(element).find('.textarea')[0];
				//解决异步加载bug
				$timeout(function () {
					scope.codeMirror = CodeMirror.fromTextArea(editor, {
						theme: 'erlang-dark',
						mode: 'shell',
						lineNumbers: true,
						readOnly: false,
						extraKeys: {
							"F11": function(cm) {
								cm.setOption("fullScreen", !cm.getOption("fullScreen"));
							},
							"Esc": function(cm) {
								if (cm.getOption("fullScreen")) cm.setOption("fullScreen", false);
							}
						}
					});
					$(element).find('.code-type').data('codeMirror',scope.codeMirror);
				});
				//全屏展示
				$(element).find('.icon-size-fullscreen').on('click',function(){
					var escTip =  $('<div style="z-index:10000;font-size:16px;color:#f05050;position: fixed;top: 10px;left:50%;text-align: center;opacity: 1;font-weigth:bold;background-color: #e7fff8;padding:5px;width:400px;margin-left:-200px;">您现在处于全屏模式，按ESC键可以退出全屏！</div>');
					$(document.body).append(escTip);
					escTip.animate({
						opacity : '0'
					},5000,function(){
						escTip.remove();
					});
					scope.codeMirror.setOption("fullScreen", true);
				});
				//选择本地脚本
			$('.file').on('change', function () {
				var data = $(this)[0].files[0];
				$('#getNameFile').val(data.name)
				//对文件大小和类型进行过滤
				var arr = data.name.split('.');
				if (['txt', 'sh', 'py', 'bat', 'pl','json','yaml'].indexOf(arr[arr.length - 1]) == -1) {
					scope.$apply(function () {
						$rootScope.pop('请上传脚本类型的文件，【.txt，.sh，.py，.bat，.pl,.json,.yaml】', 'error');
					});
					return;
				}
				if (data.size > 1024 * 1024) {
					scope.$apply(function () {
						$rootScope.pop('文件大小超过1M', 'error');
					});
					return;
				}
				if (data) {
					//将文件进行转码，转换为text
					var reader = new FileReader();
					reader.readAsText(data);
					reader.onload = function (f) {
						scope.codeMirror.setValue(this.result);
					}
				}
				});
			}
		}
	}]);
	app.directive('ngCodeMirror2', ['$timeout','$rootScope', function ($timeout,$rootScope) {
		return {
			restrict: 'EA',
			scope:{
				codeMirror: '='
			},
			link: function (scope, element, attrs) {
				var editor = $(element).find('.textarea')[0];
				//解决异步加载bug
				$timeout(function () {
					scope.codeMirror = CodeMirror.fromTextArea(editor, {
						theme: 'erlang-dark',
						mode: 'shell',
						lineNumbers: true,
						readOnly: true,
						extraKeys: {
							"F11": function(cm) {
								cm.setOption("fullScreen", !cm.getOption("fullScreen"));
							},
							"Esc": function(cm) {
								if (cm.getOption("fullScreen")) cm.setOption("fullScreen", false);
							}
						}
					});
					$(element).find('.code-type').data('codeMirror',scope.codeMirror);
				});
				//全屏展示
				$(element).find('.icon-size-fullscreen').on('click',function(){
					var escTip =  $('<div style="z-index:10000;font-size:16px;color:#f05050;position: fixed;top: 10px;left:50%;text-align: center;opacity: 1;font-weigth:bold;background-color: #e7fff8;padding:5px;width:400px;margin-left:-200px;">您现在处于全屏模式，按ESC键可以退出全屏！</div>');
					$(document.body).append(escTip);
					escTip.animate({
						opacity : '0'
					},5000,function(){
						escTip.remove();
					});
					scope.codeMirror.setOption("fullScreen", true);
				});
				//选择本地脚本
			$('.file').on('change', function () {
				var data = $(this)[0].files[0];
				$('#getNameFile').val(data.name)
				//对文件大小和类型进行过滤
				var arr = data.name.split('.');
				if (['txt', 'sh', 'py', 'bat', 'pl','json','yaml'].indexOf(arr[arr.length - 1]) == -1) {
					scope.$apply(function () {
						$rootScope.pop('请上传脚本类型的文件，【.txt，.sh，.py，.bat，.pl,.json,.yaml】', 'error');
					});
					return;
				}
				if (data.size > 1024 * 1024) {
					scope.$apply(function () {
						$rootScope.pop('文件大小超过1M', 'error');
					});
					return;
				}
				if (data) {
					//将文件进行转码，转换为text
					var reader = new FileReader();
					reader.readAsText(data);
					reader.onload = function (f) {
						scope.codeMirror.setValue(this.result);
					}
				}
				});
			}
		}
	}]);

})();
