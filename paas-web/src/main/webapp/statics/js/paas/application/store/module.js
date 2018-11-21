(function () {
	"use strict";

	app.controller('modulestackCtrl', ['$scope', 'httpLoad', '$rootScope', '$modal', '$state', '$timeout','$location','$anchorScroll',
		function ($scope, httpLoad, $rootScope, $modal, $state, $timeout,$location,$anchorScroll) {
			$rootScope.moduleTitle = '应用服务 > 应用商店';//定义当前页
	    $rootScope.link = '/statics/css/image.css';//引入页面样式
		  $scope.isListView = true;
			$scope.param = {
			name:""
			};

			//获取云主机列表
			$scope.getData = function (name) {
				$scope.param.name = name||""
				httpLoad.loadData({
					url: '/application/store/list',
					method: 'GET',
					data: $scope.param,
					//noParam: true,
					success: function (data) {
						if (data.success) {
							$scope.countData = data.data;
							for(var obj in $scope.countData){
                                $scope.countData[obj].forEach(function (item) {
                                    if(!item.picturePath){item.picturPath="/statics/img/image/"+item.icon}else{
                                        item.picturPath =window.location.protocol+'//'+window.location.host+'/picture'+item.picturePath.split('/tmp/application_store/picture')[1]+'/'+item.icon
									}
                                })
							}
                            $scope.totalCount = data.data.total;

						}
					}
				});
			};

			$scope.getData();
			//详情
			$scope.detail = function(id){
				$state.go('paas.application.storedetail', {id: id})

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
				
				$scope.getData($scope.searchByName);
				}
			//上传
			$scope.add = function () {
                $state.go('paas.application.storeupload');
			}
			//全选
			$scope.operation = {};
			$scope.operation.isBatch1 = true;$scope.operation.isALl = false;
			$scope.selectALl = function(){
				$scope.operation.isBatch1 = !$scope.operation.isALl;
						$scope.countData.forEach(function(item){
							item.select = $scope.operation.isALl;
						});
			}
			$scope.choose = function(){
				var a = 0,b=0;
				$scope.countData.forEach(function(item){
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
                            templateUrl: '/statics/tpl/application/store/updeta.html',
                            controller: 'storeUpgradeModalCtrl',// 初始化模态范围
                            backdrop: 'static',
                            resolve: {
                                row: function () {
                                    return row;
                                }

                            }
                        });
                        modalInstance.result.then(function () {

                        }, function () {
                        });

						break;
					case 2:
					//编辑
					$state.go('paas.application.addmodule',{modelid: row.id,name:row.name,type:row.deployType});
						break;
					case 3:
					if(!row.filePath){
                        $scope.pop("该商品为平台初始化数据不允许删除","error");
                        return
					}else{
						 //删除弹性
                        var modalInstance = $modal.open({
                            templateUrl: '/statics/tpl/template/delModal.html',
                            controller: 'delModalCtrl',
                            backdrop: 'static',
                            resolve: {
                                tip: function () {
                                    return '是否下架该服务组件';
                                },
                                btnList: function () {
                                    return [{name: '确定', type: 'btn-info'},{name: '取消', type: 'btn-cancel'}];
                                }
                            }
                        });
                        modalInstance.result.then(function () {
                            httpLoad.loadData({
                                url: '/application/store/down',
                                method: 'POST',
                                data: {storeId:row.id},
                                success: function (data) {
                                    if (data.success) {
                                        $scope.pop(data.message);
                                        $scope.getData();
                                    }
                                }
                            });
                        });
					}
                       
						break;
					case 4:
					//部署
					// $state.go('paas.application.storegridin', {id: id})

						break;
				}
			};
			$scope.deteleAll = function ($event) {
				var ids = [];
				for(var i=0;i<$scope.countData.length;i++){
					var item = $scope.countData[i]
					if(item.select){
						ids.push(item.id);
					}
				}
				var modalInstance = $modal.open({
					templateUrl : '/statics/tpl/application/store/remove.html',
					controller : 'removemoduleModalCtrl',
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
					angular.forEach($scope.countData, function (data, index) {
						data.select = false;

				})
				},function(){});
			};
		}
	]);

//upgrade
    angular.module('app').controller('storeUpgradeModalCtrl', ['$scope', '$stateParams', '$modalInstance', 'LANGUAGE', 'httpLoad', 'row',
        function ($scope, $stateParams, $modalInstance, LANGUAGE, httpLoad, row) { //依赖于modalInstance
            $scope.scriptItem = {}
            $scope.scriptItem.selected= {};
            if( row.deployType == 0 ){
                $scope.deployType='单节点';
            }else if( row.deployType==1){
                $scope.deployType='集群';
            }else{
                $scope.deployType='单节点';
            }
            $scope.deploy = row.deployType
            $scope.getname = function(deployType){
            	$scope.deployType = deployType;
            	 httpLoad.loadData({
                     url: '/application/store/image',
                     method: 'GET',
                     data: {storeId:row.id,deployType:deployType},
                   //  noParam: true,
                     success: function (data) {
                         if (data.success) {
                         	if(data.data.length < 1){
                                 $scope.pop("没有需要变更的版本信息","error");
     						}else{
                                 var param = {
                                     images:	data.data
                                 }

                                 httpLoad.loadData({
                                     url: '/service/list',
                                     method: 'GET',
                                     data: {images:data.data},
                                     // noParam:true,
                                     success: function (data) {
                                         if (data.success) {
                                             if(data.data[0]){
                                                 $scope.serviceName = data.data[0].name;
                                             }
                                             var params = {
                                                     simple: true,
                                                     params: angular.toJson([{"param": {"image.name":$scope.serviceName, "repository.status":0}, "sign": "EQ"}])
                                                 }
                                             httpLoad.loadData({
                                                 url: '/image/list',
                                                 method: 'POST',
                                                 data: params,
                                                 noParam: true,
                                                 success: function (data) {
                                                     if (data.success) {
                                                         $scope.dataList = data.data.rows;
                                                         var list = new Set();
                                                         $scope.dataList.forEach(function(item){
                                                             list.add(item.repositoryName)
                                                         })
                                                         $scope.warehoseData = [];
                                                         list.forEach(function(item){
                                                             $scope.warehoseData.push(item)
                                                         })


                                                     }
                                                 }
                                             })
                                         } else {

                                         }
                                     }
                                 });
     						}
                         }
                     }
                 });
            }
           
            $scope.getname($scope.deployType)
           
            $scope.getnameSpace = function () {
                $scope.namespaceItem = "namespve";
                $scope.scriptItem.selected = ""
                var list = new Set();
                $scope.dataList.forEach(function(item){
                    if(item.repositoryName==$scope.warehoseItem){
                        list.add(item.namespace)
                    }

                })
                $scope.namespaceData = [];
                list.forEach(function(item){
                    $scope.namespaceData.push(item)
                })
            }
            $scope.namespaceItem = "namespve"
            $scope.getVision = function () {
                $scope.scriptItem.selected = ""
                if($scope.namespaceItem == "namespve"){
                    $scope.pop("请选择项目","error");
                    return false;
                }

                $scope.dataType = [];
                $scope.dataList.forEach(function(item){
                    if(item.repositoryName==$scope.warehoseItem&&item.namespace==$scope.namespaceItem){
                        $scope.dataType.push(item)
                    }
                })

            }
            $scope.ok = function () {

                $scope.scriptItem.selected.imageurl = ($scope.scriptItem.selected.repositoryType !=1) ?":"+$scope.scriptItem.selected.repositoryPort:'';
                if($scope.scriptItem.selected.namespace==""){
                    $scope.imageurl  =$scope.scriptItem.selected.repositoryAddress+$scope.scriptItem.selected.imageurl+ '/'+$scope.scriptItem.selected.name+':'+ $scope.scriptItem.selected.tag
                }else{
                    $scope.imageurl  =$scope.scriptItem.selected.repositoryAddress+$scope.scriptItem.selected.imageurl+ '/'+$scope.scriptItem.selected.namespace+ '/'+$scope.scriptItem.selected.name+':'+ $scope.scriptItem.selected.tag
                }
                $scope.param = {};
               // $scope.param.image = $scope.imageurl;
                $scope.param.imageId = $scope.scriptItem.selected.id;
                $scope.param.storeId = row.id;
                 $scope.param.deployType = $scope.deployType;
                if (!$scope.param.imageId) {
                    $scope.pop("镜像版本不能为空");
                    return
                }

                httpLoad.loadData({
                    // url: '/application/rolling',
                    url: '/application/store/upgrade',
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

		app.controller('modulehistoryModalCtrl', ['$rootScope', '$scope','$state','httpLoad','$stateParams',function($rootScope, $scope,$state,httpLoad,$stateParams) {
		 $rootScope.link = '/statics/css/alarm.css';//引入页面样式

		 $scope.param = {
		 		page:1,
			        rows: 10,
			        params:angular.toJson([{"param":{"object":"layout"},"sign":"LK"}])
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
