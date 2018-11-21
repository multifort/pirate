(function () {
	"use strict";
	app.controller('warehouseCtrl', ['$scope', 'webSocket','httpLoad', '$rootScope', '$modal', '$state', '$timeout','$location',
	'$anchorScroll','CommonData',
		function ($scope,webSocket, httpLoad, $rootScope, $modal, $state, $timeout,$location,$anchorScroll,CommonData) {
		$rootScope.moduleTitle = '镜像仓库 > 镜像管理';//定义当前页
	    $rootScope.link = '/statics/css/image.css';//引入页面样式
				$scope.typeData = CommonData.networkType;
			$scope.param = {
				page:1,
				rows: 10
			};
			var params = {
							simple: true
			}
			httpLoad.loadData({
					url: '/registry/list',
					method: 'POST',
					data: params,
				 noParam: true,
					success: function (data) {
						if (data.success) {
							$scope.warehoseData = data.data.rows;

						}
					}
				});
				httpLoad.loadData({
						url: '/user/list',
						method: 'POST',
						data: params,
						noParam: true,
						success: function (data) {
							if (data.success) {
								$scope.userData = data.data.rows;

							}
						}
					});

				//websocket异步操作
				webSocket.onmessage({
					message:function (data) {
						if($rootScope.currentUrl=='paas.repository.dockerimage'){
							   
							$scope.getData();
						}
					}
				});

			//获取云主机列表
			$scope.getData = function (page) {
				$scope.param.page = page || $scope.param.page;

				httpLoad.loadData({
					url: '/image/list',
					method: 'POST',
					data: $scope.param,
					noParam: true,
					success: function (data) {
						if (data.success) {
							$scope.countData = data.data.rows;
							$scope.totalCount = data.data.total;
							if(!$scope.totalCount){
								$scope.isImageData = true;
								} else {
									$scope.isImageData = false;
							}
						}
					}
				});
			};
			$scope.getData(1);
			//详情
			$scope.detail = function(id){
				$state.go('paas.repository.imagedetail', {id: id})

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
					"image.name": $scope.searchByName
				});
				var param2 = toObjFormat({
					property:$scope.$parent.popr,
					type:$scope.$parent.type,
					"cu.id":$scope.$parent.userid,
					"repositoryId":$scope.$parent.repositoryId

				});
				if (angular.toJson(param1).length > 2) params.push({param: param1, sign: 'LK'});
				if (angular.toJson(param2).length > 2) params.push({param: param2, sign: 'EQ'});
				$scope.param = {
					page: 1,
					rows: 10,
					params: angular.toJson(params)
				}
				$scope.getData();
			}
			//重置搜索条件
			$scope.reset = function () {
				var obj = ['name'];
				angular.forEach(obj, function (data) {
					$scope[data] = '';
				})
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
			$scope.goAction = function (flag, id, $event) {
				switch (flag / 1) {
					case 1:
					//新增
						var modalInstance = $modal.open({
							templateUrl : '/statics/tpl/repository/image/add.html',
							controller : 'addWarehouseModalCtrl',// 初始化模态范围
								backdrop: 'static',
							resolve : {
								id : function(){
									return id;
								},
							}
					});
					modalInstance.result.then(function(){
							$scope.getData(1);
					},function(){});

						break;
					case 2:
					//编辑
							var modalInstance = $modal.open({
									templateUrl : '/statics/tpl/repository/image/add.html',
								controller : 'addWarehouseModalCtrl',
								backdrop: 'static',
								resolve : {
									id : function(){
										return id;
									},

								}
							});
							modalInstance.result.then(function(){
								$scope.getData(1);
							},function(){});

						break;
					case 3:
						//删除
								if($event) $event.stopPropagation();
								var ids=[];
								ids.push(id);
								var modalInstance = $modal.open({
									templateUrl : '/statics/tpl/repository/image/remove.html',
									controller : 'removeWarehouseModalCtrl',
										backdrop: 'static',
									resolve : {
										id : function(){
											return ids;
										},

									}
								});
								modalInstance.result.then(function(){
									$scope.getData(1);
									$scope.isCheck = false;
								},function(){});

						break;
					case 4:
					//部署
					$state.go('paas.repository.imagetiondeploy', {id: id})
						break;
					case 5:
						//导入
						var modalInstance = $modal.open({
								templateUrl : '/statics/tpl/repository/image/gridinpull.html',
							controller : 'gridinarehouseModalCtrl',
					backdrop: 'static',
							resolve : {
								id : function(){
									return id;
								},

							}
						});
						modalInstance.result.then(function(){
							$scope.getData(1);
						},function(){});
						break;
						case 6:
							//导入
							var modalInstance = $modal.open({
									templateUrl : '/statics/tpl/repository/image/add.html',
								controller : 'authorizationModalCtrl',
						backdrop: 'static',
								resolve : {
									id : function(){
										return id;
									},

								}
							});
							modalInstance.result.then(function(){
								$scope.getData(1);
							},function(){});
							break;
							case 7:
							//部署
							$state.go('paas.repository.imagebuild')
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
					templateUrl : '/statics/tpl/repository/image/remove.html',
					controller : 'removeWarehouseModalCtrl',
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
	//新增ctrl
	 angular.module('app').controller('addWarehouseModalCtrl',['$scope','$modalInstance','LANGUAGE','httpLoad','id',
			 function($scope,$modalInstance,LANGUAGE,httpLoad,id){ //依赖于modalInstance
				 var editObj = ['name', 'remark'];

				 $scope.modalName = '镜像部署';
				 var url = '/image/create';
				 $scope.addData = {};
					 //如果为编辑，进行赋值
	        if (id) {
	                url = '/image/modify';
	                $scope.modalName = '镜像编辑';
	                httpLoad.loadData({
	                    url: '/image/detail',
	                    method: 'GET',
	                    data: {id: id},
	                    success: function (data) {
	                        if (data.success) {
	                            var data = data.data;
	                            for (var a in editObj) {
	                                var attr = editObj[a];
	                                $scope.addData[attr] = data[attr];
	                            }

	                        }
	                    }
	                });
	            }
					$scope.ok = function () {
							 var param = {};
                        for (var a in editObj) {
                            var attr = editObj[a];
                            param[attr] = $scope.addData[attr];
                        }
                        if (id) param.id = id;
							 httpLoad.loadData({
									 url:url,
									 method:'POST',
									 data: param,
									 success:function(data){
											 if(data.success){
													$scope.pop(data.message);
													 $modalInstance.close();
											 }
									 }
							 });
					 }

					 $scope.cancel = function(){
							$modalInstance.dismiss('cancel');
					 };
			 }]);

	//删除ctrl
	angular.module('app').controller('removeWarehouseModalCtrl',['$scope','$modalInstance','httpLoad','LANGUAGE','id',
		function($scope,$modalInstance,httpLoad,LANGUAGE,id){ //依赖于modalInstance
			$scope.content = '是否删除？';
			$scope.ok = function(){
				httpLoad.loadData({
					url:'/image/remove',
					method:'POST',
					data: {ids:id},
					success:function(data){
						if(data.success){
							//console.log(removeData);
							$scope.pop(data.message);
							$modalInstance.close();

						}
					}
				});
			};
			$scope.cancel = function(){
			$modalInstance.dismiss('cancel');
			$scope.operation.isBatch1 = true;
			}
		}]);
		//导入
		angular.module('app').controller('gridinarehouseModalCtrl',['$scope','$modalInstance','httpLoad','LANGUAGE',
			function($scope,$modalInstance,httpLoad,LANGUAGE){ //依赖于modalInstance
			$scope.scriptItemSpace = {}
			$scope.scriptItemSpace.selected = "";
				//$scope.scriptItemSpace.selected.name   //镜像名称
				var params = {
			                     simple: true
			             }
				$scope.websocketUrl='/uploadService';
		        httpLoad.loadData({
		            url: '/registry/list',
		            method: 'POST',
		            data: params,
		            noParam: true,
		            success: function (data) {
		              if (data.success) {
		                $scope.warehoseData = data.data.rows;

		              }
		            }
		          });
				$scope.getImage = function (id) {
                    httpLoad.loadData({
                        url: '/image/getProjects',
                        method: 'GET',
                        data: {registryId:id},
                        noParam: true,
                        ignoreError: true,
                        success: function (data) {
                            if (data.success) {
                                $scope.imageData = data.data;
                                $scope.imageshow = true;

                            }else{
                            	$scope.scriptItemSpace.selected = "";
                            	 $scope.imageshow = false;
                            }
                        }
                    });
                }
				$scope.cancel = function(){
					$modalInstance.close();

				}
			}]);

//新增ctrl
angular.module('app').controller('authorizationModalCtrl',['$scope','$modalInstance','httpLoad','LANGUAGE','id',
function($scope,$modalInstance,httpLoad,LANGUAGE,id){ //依赖于modalInstance

	//角色授权
	$scope.getRoleData = function(){
		httpLoad.loadData({
		  	url: '/department/list',
			method:'POST',
			data: {'parentId':0},
			noParam:true,
			success:function(data){

					if(data.success&&data.data){
							$scope.treeData = data.data;

					}
			}
		});
	};
	$scope.getRoleData();
	$scope.ok = function(){
		var sValues = $("#mycombotree2").combotree("getValues");
		var params={
			imageId : id,
			deptId : sValues[0]
		}
		httpLoad.loadData({
			url:'/image/authorize',
			method:'POST',
			data: params,
			success:function(data){

				if(data.success){

					$scope.pop(data.message);
					$modalInstance.close(id);
				}
			}
		});
	};
	$scope.cancel = function(){
		$modalInstance.dismiss('cancel'); // 退出
	}
}]);

angular.module('app').directive('userCombotree',
	['$rootScope', '$timeout', 'httpLoad', function ($rootScope,$timeout,httpLoad) {
		return {
			restrict: 'AE',
			scope : {
				treeData        : '=',
				groupId          : '=',
				groupName         :'=',
				index              : '='
			},
			link: function (scope, element, attrs) {
				scope.$watch('treeData',function(newValue,oldValue){
					$('#mycombotree2').combotree({
						data: scope.treeData,
						textField :"text",
						valueField : "id",
						emptyText : '请选择',
						onBeforeExpand: function(row,param){
							$('#mycombotree2').combotree('tree').tree('options').url = '/department/list?parentId='+row.id;
						},
						onSelect:function(row) {
							scope.groupId = row.id;

						},
						onLoadSuccess :function(node, data){
							if(scope.groupId){

								defaultValue('mycombotree2',scope.groupId,scope.groupName);

							}
							//deftext：生成节点的文本用于显示
							function defaultValue(cbtid,defVal,defText){
								var combotree =$("#"+cbtid);
								var tree = combotree.combotree('tree');
								var defNode = tree.tree("find",defVal);
								if(!defNode){
									tree.tree('append', {
										data: [{
											id: defVal,
											name: defText,
											parentId:0,
											children:"",
											checked:false
										}]
									});
									defNode = tree.tree("find",defVal);
									combotree.combotree('setValue',defVal);
									tree.tree('select',defNode.target);
									defNode.target.style.display='none';
								}else{
									combotree.combotree('setValue',defVal);
								}
							}
						},
						loadFilter: function(rows,parent){
							if(rows.success) rows = rows.data;
							var nodes = [];
							// get the top level nodes
							for(var i=0; i<rows.length; i++){
								var row = rows[i];
								var state = 'open';
								//if (!exists(rows, row.parentId)){
								if(row.children){
									state = 'closed';
									if(row.children=="[]") row.children=[];
								} else state = 'open';
								//}
								nodes.push({
									id:row.id,
									text:row.name,
									parentId:row.parentId,
									children:row.children,
									checked:row.checked,
									state:state
								});
							}
							return nodes;
						}
					});
				});
			}
		};
	}]);
	//
	app.controller('warehousehistoryModalCtrl', ['$rootScope', '$scope','$state','httpLoad','$stateParams',function($rootScope, $scope,$state,httpLoad,$stateParams) {
	 $rootScope.link = '/statics/css/alarm.css';//引入页面样式
	 $scope.param = {
	 		page:1,
		        rows: 10,
		        params:angular.toJson([{"param":{"object":"image"},"sign":"LK"}])
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
