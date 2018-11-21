/**
 * Created by Zhang Haijun on 2016/9/2.
 */
(function(){
	//websocket部署
	//websocket文件上传指令
	app.directive('ngFileUploadOnly', ['$rootScope', 'httpLoad', function ($rootScope, httpLoad) {
		return {
			restrict: 'EA',
			scope:false,
			link: function (scope, element, attrs) {
				scope.item = {};
				$('#btnFileUpload').on('change', function (event) {
					var files = event.target.files, list = [];
					if(/[\u4e00-\u9fa5]/.test(files[0].name)){
						$rootScope.pop('文件名不允许存在中文','error');
					};
					//对文件大小和类型进行过滤
					var arr = files[0].name.split('.');
//					if (['tar'].indexOf(arr[arr.length - 1]) == -1) {
//						scope.$apply(function () {
//							$rootScope.pop('请上传脚本类型的文件，【.tar】', 'error');
//						});
//						return;
//					}
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
					socket = new WebSocket('ws://' + location.host +scope.websocketUrl);
					var i = 0;var startSize = 0,endSize = 0;
					var paragraph = 4 * 1024 * 1024;    //以4MB为一个分片
					var count = parseInt(item.file.size / paragraph) + 1;
					socket.onopen = function () {
						item.isUploading = true;
						socket.send(JSON.stringify({
								'filename': scope.gridinPullName,
//								registryId:scope.warehoseItem,
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
								 scope.filePath = obj.content.substr(obj.content.indexOf(',') + 1);
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

})();
