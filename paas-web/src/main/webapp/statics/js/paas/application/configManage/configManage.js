(function () {
	"use strict";

	app.controller('configManageCtrl', ['$scope', 'httpLoad', '$rootScope', '$modal', '$state', '$timeout','$location','$anchorScroll',
		function ($scope, httpLoad, $rootScope, $modal, $state, $timeout,$location,$anchorScroll) {
			$rootScope.moduleTitle = '应用服务 > 配置管理';//定义当前页
	    $rootScope.link = '/statics/css/image.css';//引入页面样式
		  $scope.isListView = true;
			$scope.param = {
                page:1,
                rows: 10
			};

			//获取云主机列表
			$scope.getData = function (name) {
				
				httpLoad.loadData({
					url: '/config/manage/config',
					method: 'GET',
					data: $scope.param,
					noParam: true,
					success: function (data) {
						if (data.success) {
							$scope.listData = data.data.rows;

                            $scope.totalCount = data.data.total;

						}
					}
				});
			};

			$scope.getData();
			//详情
			$scope.detail = function(row){
				$state.go('paas.application.configManageDetail', {id: row.id})

			}

			$scope.goBack = function(){
				 $scope.isActive = false;
				 $timeout(function() {
						 $scope.showHistory = false;
				 }, 200);
		 };
		 $scope.gohistory = function($event){
			 //历史记录
				$event.stopPropagation();
			 $scope.isActive = true ;
			 $scope.showHistory = true;
				};
			//搜索
				
				$scope.search = function () {
					//对参数进行处理，去除空参数
					var toObjFormat = function (obj) {
						for (var a in obj) {
							if (obj[a] == "") delete obj[a];
						}
						return obj;
					}
					var params = [];
					var param1 = toObjFormat({
						"name": $scope.searchByName
					});
					if (angular.toJson(param1).length > 2) params.push({param: param1, sign: 'LK'});
					$scope.param = {
						page: 1,
						rows: 10,
						params: angular.toJson(params)
					}
					$scope.getData();
				}

			//全选
			$scope.operation = {};
			$scope.operation.isBatch1 = true;$scope.operation.isALl = false;
			$scope.selectALl = function(){
				$scope.operation.isBatch1 = !$scope.operation.isALl;
						$scope.listData.forEach(function(item){
							item.select = $scope.operation.isALl;
						});
			}
			$scope.choose = function(){
				var a = 0,b=0;
				$scope.listData.forEach(function(item){
					if(item.select==true) a++;
					else b=1;
				});
				if(a>=1) $scope.operation.isBatch1 = false;
				else $scope.operation.isBatch1 = true;
				if(b==1) $scope.operation.isALl = false;
				else $scope.operation.isALl = true;
			};
			//返回
			$scope.goAction = function (flag, row, $event) {
				switch (flag / 1) {
					case 1:
					//新增
                        var modalInstance = $modal.open({
                            templateUrl: '/statics/tpl/application/configManage/add.html',
                            controller: 'configManageUpgradeModalCtrl',// 初始化模态范围
                            backdrop: 'static',
                            resolve: {
                                row: function () {
                                    return row;
                                }

                            }
                        });
                        modalInstance.result.then(function () {
                            $scope.getData();
                        }, function () {
                        });

						break;
					case 2:
					//编辑
					$state.go('paas.application.addmodule',{modelid: row.id,name:row.name,type:row.deployType});
						break;
					case 3:
					    var ids = [];
					    ids.push(row.id)
                        var modalInstance = $modal.open({
                            templateUrl : '/statics/tpl/application/store/remove.html',
                            controller : 'removeconfigManageModalCtrl',
                            backdrop: 'static',
                            resolve : {
                                id : function(){
                                    return ids;
                                },

                            }
                        });
                        modalInstance.result.then(function(){
                            $scope.getData(1);
                            $scope.operation.isALl = false;
                            $scope.operation.isBatch1 = true;
                            angular.forEach($scope.listData, function (data, index) {
                                data.select = false;

                            })
                        },function(){});
						break;
					case 4:
					//部署
					// $state.go('paas.application.storegridin', {id: id})

						break;
				}
			};
			$scope.deteleAll = function ($event) {
				var ids = [];
				for(var i=0;i<$scope.listData.length;i++){
					var item = $scope.listData[i]
					if(item.select){
						ids.push(item.id);
					}
				}
				var modalInstance = $modal.open({
					templateUrl : '/statics/tpl/application/store/remove.html',
					controller : 'removeconfigManageModalCtrl',
						backdrop: 'static',
					resolve : {
						id : function(){
							return ids;
						},

					}
				});
				modalInstance.result.then(function(){
					$scope.getData(1);
					$scope.operation.isALl = false;
					$scope.operation.isBatch1 = true;
					angular.forEach($scope.listData, function (data, index) {
						data.select = false;

				})
				},function(){});
			};
		}
	]);

//upgrade
    angular.module('app').controller('configManageUpgradeModalCtrl', ['$scope', '$stateParams', '$modalInstance', '$timeout', 'httpLoad', 'row',
        function ($scope, $stateParams, $modalInstance, $timeout, httpLoad, row) { //依赖于modalInstance
            $scope.modalName = '创建';
            $scope.addData = {};
             $scope.addData.type = 0;
            var url = '/config/manage/config';
            $scope.uplistData = [];
            $scope.addData.fileDir = ''
            $scope.labelList = [{keys:'',values:''}];
            $scope.param = {
                page:1,
                rows: 100000,
                params:angular.toJson([{"param":{"status":"2,4"},"sign":"IN"}])
            };
            $scope.getApp = function(app){


                httpLoad.loadData({
                    url: '/application/list',
                    method: 'POST',
                    data: {params:angular.toJson([{"param":{"envId":app},"sign":"EQ"}])},
                    noParam: true,
                    success: function (data) {
                        if (data.success) {
                            $scope.dataAppType = data.data.rows;

                        }
                    }
                });
            }

            if (row) {
                url = '/config/manage/update/config';
                var urlupdata = '/config/manage/'+row.id+'/config'
                $scope.modalName = '编辑';
                httpLoad.loadData({
                    url:urlupdata,
                    method: 'GET',
                    data: {},
                    success: function (data) {
                        if (data.success) {
                            var data = data.data;
                            $scope.addData.name = data.configManage.name;
                            $scope.addData.type = data.configManage.type;
                            $scope.addData.fileDir = data.configManage.fileDir;
                            $scope.addData.remark = data.configManage.remark;
                            $scope.labelList = [];
                           for(var i in data.configMap.data){
                               $scope.labelList.push({keys:i,values:data.configMap.data[i]});
                           }
							data.files.forEach(function(item){
								var a={file:{name:item},progress:100,updataBtn:true}
								$scope.uplistData.push(a);   $scope.fileShow = true

							})

                        }
                    }
                });
            }else{
                httpLoad.loadData({
                    url:'/environment/list' ,
                    method:'POST',
                    data:$scope.param,
                    noParam: true,
                    success:function(data){
                        if(data.success){
                            $scope.masterList = data.data.rows;
                        }
                    }
                });
              //  $scope.getApp();
            }

            //标签
            $scope.addLabels = function(){
                //做验证-》只有上面用户组有用户下面才可以继续添加用户组
                $scope.labelList.push({keys:'',values:''})
                $timeout(function () {
                    $(".labellista").hide().last().show()
                },100)
            }
            $scope.removeLabel = function(key){
                if($scope.labelList.length == 1) return $scope.pop('请至少添加一组','error');
                $scope.labelList.splice(key,1);
                $timeout(function () {
                    $(".labellista").last().show()
                },100)
            }

            $scope.ok = function () {
                var updata = false;
                if($scope.addData.fileDir){
                    $scope.uplistData.forEach(function (item) {
                        if(item.updataBtn){
                            updata = true;
                        }
                    })
                    if(!updata){  $scope.pop('请上传文件','error');
                        return;}
                }
                var labelList = {};
                for(var j=0;j<$scope.labelList.length;j++){
                    var items = $scope.labelList[j];
                    if((!items.keys&&items.values)||(items.keys&&!items.values)){
                        $scope.pop('请添加完整的标签','error');
                        return;
                    }else if(!items.keys&&!items.values){
                        continue
                    }else{
                        labelList[items.keys] = items.values;
                    }
                }

                $scope.param = {};
               // $scope.param.image = $scope.imageurl;
                if(row){
                	if($scope.addData.type==0){
                		$scope.param.id = row.id;
                        $scope.param.remark = $scope.addData.remark;
                        $scope.param.dataMap = labelList;
                	}else{
                		$scope.param.id = row.id;
                        $scope.param.remark = $scope.addData.remark;
                        $scope.param.dataMap = {};
                	}
                	
                }else{
                	if($scope.addData.type==0){
                		 $scope.param.configManage = $scope.addData;
                         $scope.param.dataMap = labelList;
                	}else{
                		 $scope.param.configManage = $scope.addData;
                		 $scope.param.dataMap = {};
                	}
                	
                }
             
            

                httpLoad.loadData({
                    // url: '/application/rolling',
                    url: url,
                    method: 'post',
                    data: $scope.param,
                    //  noParam: true,
                    success: function (data) {
                        if (data.success) {
                            $scope.pop(data.message);
                            $modalInstance.close();
                        }
                    }
                });
            }

            $scope.cancel = function () {
                $modalInstance.dismiss('cancel');
            };
        }]);
    //删除ctrl
    angular.module('app').controller('removeconfigManageModalCtrl',['$scope','$modalInstance','httpLoad','LANGUAGE','id',
        function($scope,$modalInstance,httpLoad,LANGUAGE,id){ //依赖于modalInstance
            $scope.messageApply = '本应用包含的部署、服务和运行实例都将彻底删除，确认删除？';

            $scope.ok = function(){
                $scope.isCheck = true;
                httpLoad.loadData({
                    url:'/config/manage/config',
                    method:'DELETE',
                    data: {ids:id},
                    success:function(data){
                        if(data.success){
                            //console.log(removeData);
                            $scope.pop(data.message);
                            $modalInstance.close();
                        }else{
                            $modalInstance.dismiss('cancel');
                            $scope.operation.isBatch1 = false;
                        }
                    }
                });
            };
            $scope.cancel = function(){
                $modalInstance.dismiss('cancel');
                $scope.operation.isBatch1 = true;
            }
        }]);
    app.directive('ngFilesUpload', ['$rootScope', 'httpLoad', function ($rootScope, httpLoad) {
        return {
            restrict: 'EA',
            scope:true,
            link: function (scope, element, attrs) {
                scope.item = {};
              
                $('#btnFileUpload').on('change', function (event) {
                    var files = event.target.files, list = [];

                   for(var i=0;i<files.length;i++){
                       if(/[\u4e00-\u9fa5]/.test(files[i].name)){
                           $rootScope.pop('文件名不允许存在中文','error');
                           return
                       };
                       //对文件大小和类型进行过滤
                       var arr = files[i].name.split('.');
                       // if ((['properties'].indexOf(arr[arr.length - 1]) == -1) && ['properties'].indexOf(arr[arr.length - 1]) == -1) {
                       //     scope.$apply(function () {
                       //         $rootScope.pop('仅支持上传【properties】类型的文件', 'error');
                       //     });
                       //     return;
                       // }


                       scope.$apply(function () {
                           scope.uplistData.push({file:files[i]});
                           scope.fileShow = true
                       });

                   }

                    $('#btnFileUpload').html($('#btnFileUpload').html());
                    if(!scope.addData.fileDir){
                    	var item ={file:{name:"",size:0}}
                    	
                    	 socket = new WebSocket('ws://' + location.host +'/uploadPicture');
                         var i = 0;var startSize = 0,endSize = 0;
                         var paragraph = 4 * 1024 * 1024;    //以4MB为一个分片
                         var count = parseInt(item.file.size / paragraph) + 1;
                         socket.onopen = function () {
                             item.isUploading = true;
                             socket.send(JSON.stringify({
                                 'name': item.file.name,
                                 'dirPath':scope.addData.fileDir,
                                 'upload': 'picture',
                                 'type':1
                             }));


                         };
                         socket.onmessage = function (event) {
                      
                             var obj = JSON.parse(event.data);
                           
                             if (obj.category == "UPLOAD_ACK") {
                             	scope.addData.fileDir = obj.content;
                                
                             } 
                         };
                    
                    }else{
                    	
                    }
                   
                });
                var socket ;
                scope.cancelFile = function (item,index) {
                	if(scope.modalName == '编辑'){
                		 socket = new WebSocket('ws://' + location.host +'/uploadPicture');
                		if(socket.readyState != 1){
                			
                		       socket.onopen = function () {
                		    	   socket.send(JSON.stringify({
                                       'UPLOAD_CANCEL': 'UPLOAD_CANCEL',
                                       'name': item.file.name,
                                       'dirPath':scope.addData.fileDir
                                   }));
                                   scope.updataBtn = false
                                   scope.$apply(function () {
                                	   scope.uplistData.splice(index,1)
                                   });
                                  
                               };
                		}
                		
                	}else{
                		 if(socket) {
                             socket.send(JSON.stringify({
                                 'UPLOAD_CANCEL': 'UPLOAD_CANCEL',
                                 'name': item.file.name,
                                 'dirPath':scope.addData.fileDir
                             }));
                             scope.$apply(function () {
                          	   scope.uplistData.splice(index,1)
                             });
                            
                         }else {
                             scope.pop('没有文件不能取消上传','error');
                         }
                	}
                   

                }

                scope.upload = function (item) {
                	if(!socket){ socket = new WebSocket('ws://' + location.host +'/uploadPicture');}
                   
                    var i = 0;var startSize = 0,endSize = 0;
                    var paragraph = 4 * 1024 * 1024;    //以4MB为一个分片
                    var count = parseInt(item.file.size / paragraph) + 1;
                    socket.send(JSON.stringify({
                        'name': item.file.name,
                        'dirPath':scope.addData.fileDir,
                       
                        'ready': 'ready',
                        'type':1
                    }));
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
                   // sendFile();
                    socket.onmessage = function (event) {
                        
                        item.updataBtn = true
                        item.isUploading = true;
                        item.isCancel = false;
                        var obj = JSON.parse(event.data);
                       
                        if (obj.category == "UPLOAD_READY") {
                        
                            sendFile();
                        } else if (obj.category == 'UPLOAD') {
                            if (obj.content == 'SAVE_FAILURE') {
                            	 scope.updataBtn = false;
                                 scope.pop('文件上传失败','error');
                               
                            } else if (obj.content == 'SAVE_SUCCESS') {
                                sendFile();
                            }else if (obj.content.indexOf('TRUE') >= 0) {
                            	scope.pop("文件上传成功");
                                item.isReady = true; item.isSuccess = true;item.isUploading = false;
                                
                              
                            }
                        } else if (obj.category == 'UPLOAD_CANCEL') {
                            if (obj.content == 'CANCEL_SUCCESS') {
                                item.progress = 0;
                            }
                            scope.pop('已删除文件','info');
                            item.progress = 0;
                            item.updataBtn = false
                      
                        }
                        scope.$apply(scope.progress);
                    };
                };
            }
        }
    }]);

    app.controller('configManagehistoryModalCtrl', ['$rootScope', '$scope','$state','httpLoad','$stateParams',function($rootScope, $scope,$state,httpLoad,$stateParams) {
		 $rootScope.link = '/statics/css/alarm.css';//引入页面样式

		 $scope.param = {
		 		page:1,
			        rows: 10,
			        params:angular.toJson([{"param":{"object":"config"},"sign":"LK"}])
			    };
			$scope.getHistory = function (page) {
				$scope.param.page = page||$scope.param.page
		       httpLoad.loadData({
		          url: '/app/log/list',
		           method:'POST',
		           data: $scope.param,
		           noParam:true,
		           success:function(data){
		               if(data.success&&data.data){
		                   $scope.userList = data.data.rows;
		                   $scope.totalCount = data.data.total;
		                   if(data.data.rows==[]){
		                       $scope.pop("返回数据为空");
		                   }
		               }

		           }
		       });
		      }
								$scope.getHistory(1)
		}]);
})();
