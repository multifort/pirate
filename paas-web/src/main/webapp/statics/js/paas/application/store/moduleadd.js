(function () {
	"use strict";

	app.controller('moduleUploadCtrl', ['$scope', 'httpLoad', '$rootScope', '$modal', '$state', '$timeout','$location','$anchorScroll',
		function ($scope, httpLoad, $rootScope, $modal, $state, $timeout,$location,$anchorScroll) {
			$rootScope.moduleTitle = '应用服务 > 应用商店 > 组件上传';//定义当前页
            $scope.addData = {};
            $scope.deployType ="单节点"
            $scope.addData.image = '';
            $scope.warehoseData = ["应用中间件","应用服务器","开发工具","数据库","负载均衡器"]
			$scope.goBack = function(){
                $state.go('paas.application.store');
				 };
            //应用部署镜像名称接口
            httpLoad.loadData({
                url: '/registry/list',
                method: 'POST',
                data: {simple:true},
                noParam: true,
                success: function (data) {
                    if (data.success) {
                        $scope.dataTypeRepository = data.data.rows;

                    }
                }
            });
            //项目的列表
            $scope.getRepository = function(obj){
                var params = {
                    simple: true,
                    params: angular.toJson([{"param": {"repository.id": obj}, "sign": "EQ"}])
                }
                httpLoad.loadData({
                    url: '/image/list',
                    method: 'POST',
                    data: params,
                    noParam: true,
                    success: function (data) {
                        if (data.success && data.data.rows) {
                            var rows = data.data.rows;
                            var arry = new Set()

                            var arryList=[];
                            rows.forEach(function(item){
                                arry.add(item.namespace)
                            })
                            arry.forEach(function(item){arryList.push(item)})
                      
                            $scope.dataTypeSpace = arryList

                        }
                    }
                });

            }
            //镜像的列表
            $scope.getSpaces = function(obj){
                var params = {
                    simple: true,
                    params: angular.toJson([{"param": {"repository.id": $scope.repository,"repository_image_info.namespace":obj}, "sign": "EQ"}])
                }
                httpLoad.loadData({
                    url: '/image/list',
                    method: 'POST',
                    data: params,
                    noParam: true,
                    success: function (data) {
                        if (data.success && data.data.rows) {
                            var rows = data.data.rows;
                            var arry = new Set();
                            var arryList=[];
                            rows.forEach(function(item){
                                arry.add(item.name)
                            })
                            arry.forEach(function(item){arryList.push(item)});
                         
                            $scope.dataTypeImage = arryList;

                        }
                    }
                });

            }
            //版本的列表
            $scope.getImages = function(obj){
                var params = {
                    simple: true,
                    params: angular.toJson([{"param": {"repository.id": $scope.repository,"repository_image_info.namespace":$scope.space,"image.name":obj}, "sign": "EQ"}])
                }
                httpLoad.loadData({
                    url: '/image/list',
                    method: 'POST',
                    data: params,
                    noParam: true,
                    success: function (data) {
                        if (data.success && data.data.rows) {
                            $scope.vision = '';
                            $scope.dataTypeVision = data.data.rows;

                        }
                    }
                });

            }
            //点击镜像 获取环境信息
            $scope.getEnvDetail = function(obj){
            	obj =  angular.fromJson(obj);
                if(obj.id){
                    obj.imageurl = (obj.repositoryType !=1) ?":"+obj.repositoryPort:'';
                    if(obj.namespace==""){
                        $scope.addData.image  =obj.repositoryAddress+obj.imageurl+  '/'+obj.name+':'+ obj.tag
                    }else{
                        $scope.addData.image  =obj.repositoryAddress+obj.imageurl+ '/'+obj.namespace+ '/'+obj.name+':'+ obj.tag
                    }
                }
                $scope.imageId = obj.id
                httpLoad.loadData({
                    url: '/application/store/template',
                    method: 'POST',
                    data: {"imageId":$scope.imageId,"jsonObject":{}},
                   // noParam: true,
                    success: function (data) {
                        if (data.success) {
                            $scope.codeMirror.setValue(data.data);


                        }
                    }
                });
            }
            $scope.array = {}
            $scope.template = function(){
                var array = {}
                var editObj = ['VOLUME_TYPE','RESOURCE_TYPE','NODE_TYPE'];
                editObj.forEach(function(attr){
                    if($scope.array[attr]){
                        array[attr] = attr;
                    }
                })
                httpLoad.loadData({
                    url: '/application/store/template',
                    method: 'POST',
                    data: {"imageId":$scope.imageId,"jsonObject":array},
                    // noParam: true,
                    success: function (data) {
                        if (data.success) {
                            $scope.codeMirror.setValue(data.data);


                        }
                    }
                });
            }

            $scope.ok = function () {
                var param = {};
                param  = $scope.addData;
                
                if(param.icon&&!param.picturePath){
                	 $scope.pop("请上传图片","error");
                	 return
                }
                param.icon = param.icon||'default.png';
                param.picturePath = param.picturePath||'';
                param.template = $('#getNameFile').val()
                param.context = $scope.codeMirror.getValue();
                param.deployType = $scope.deployType;
                if(param.context==''){
                    $scope.pop("脚本内容不能为空","error");
                    return false
                }
              
                httpLoad.loadData({
                    url:"/application/store/upload",
                    method:'POST',
                    data: param,
                    success:function(data){
                        if(data.success){
                        
                            $scope.pop(data.message);
                            $state.go('paas.application.store');
                        }
                    }
                });
            }

        }
	]);
    //websocket文件上传指令
    app.directive('ngPictureUpload', ['$rootScope', 'httpLoad', function ($rootScope, httpLoad) {
        return {
            restrict: 'EA',
            scope:true,
            link: function (scope, element, attrs) {
                scope.item = {};
                $('#btnPictureUpload').on('change', function (event) {

                    var files = event.target.files, list = [];
                    scope.updataBtn = false
                    scope.progressbarShow=false;
                    if(/[\u4e00-\u9fa5]/.test(files[0].name)){
                        $rootScope.pop('文件名不允许存在中文','error');
                    }
                    //对文件大小和类型进行过滤
                    var arr = files[0].name.split('.');
                    if (['jpg','png'].indexOf(arr[arr.length - 1]) == -1) {
                        scope.$apply(function () {
                            $rootScope.pop('请上传照片格式正确的图片，【png，JPG】', 'error');
                        });
                        return;
                    }else if(files[0]){}

                    //预览功能
                    var prevDiv = document.getElementById('preview');
                    if (files && files[0])
                    {
                        var reader = new FileReader();
                        reader.onload = function(evt){
                            var image = new Image();
                            image.onload = function() {
                                var width = image.width;
                                var height = image.height;
                                if(width!=height){
                                    scope.$apply(function () {
                                        $rootScope.pop('请上传长宽相等的图片,规格要求：100*100', 'warning');
                                    });

                                    return
                                }else{
                                    prevDiv.innerHTML = '<img src="' + evt.target.result + '" />';
                                    $('#btnPictureUpload').html($('#btnPictureUpload').html());
                                    scope.$apply(function () {
                                        scope.Pictionicon = files[0].name;
                                    });
                                }
                            }
                            image.src = evt.target.result;
                        }
                        reader.readAsDataURL(files[0]);
                    }
                    else
                    {
                        prevDiv.innerHTML = '<div class="img" style="filter:progid:DXImageTransform.Microsoft.AlphaImageLoader(sizingMethod=scale,src=\'' + file.value + '\'"></div>';
                        $('#btnPictureUpload').html($('#btnPictureUpload').html());
                        scope.$apply(function () {
                            scope.Pictionicon = files[0].name;
                        });
                    }


                    scope.item  = {file: files[0]};
                   
                });
                var socket ;
                scope.cancelFile = function (item) {
                    if(socket) {
                        socket.send(JSON.stringify({
                            'UPLOAD_CANCEL': 'UPLOAD_CANCEL'
                        }));
                        scope.updataBtn = false
                        var img = '  <em style="line-height: 200px;">请上传png，JPG格式的图片</em>';
                        prevDiv.innerHTML = img;
                    }else {
                        scope.pop('没有图片不能取消上传','error');
                    }

                }

                scope.upload = function (item) {
                    scope.progressbarShow=true;
                    socket = new WebSocket('ws://' + location.host +'/uploadPicture');
                    var i = 0;var startSize = 0,endSize = 0;
                    var paragraph = 4 * 1024 * 1024;    //以4MB为一个分片
                    var count = parseInt(item.file.size / paragraph) + 1;
                    socket.onopen = function () {
                        item.isUploading = true;
                        socket.send(JSON.stringify({
                            'name': scope.Pictionicon,
                            'upload': 'picture',
                            'type':0
                         
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
                                scope.pop('图片上传失败','error');
                            } else if (obj.content == 'SAVE_SUCCESS') {
                                sendFile();
                            }else if (obj.content.indexOf('TRUE') >= 0) {
                                scope.pop("图片上传成功");
                                scope.addData.picturePath =  obj.content.split(',')[1];
                                scope.addData.icon = scope.Pictionicon
                                item.isReady = true; item.isSuccess = true;item.isUploading = false;

                            }
                        } else if (obj.category == 'UPLOAD_CANCEL') {
                            if (obj.content == 'CANCEL_SUCCESS') {
                                item.progress = 0;
                            }
                            scope.pop('已取消图片上传','info');
                            item.progress = 0;
                            scope.updataBtn = false

                        }
                        scope.$apply(scope.progress);
                    };
                };
            }
        }
    }]);

})();
