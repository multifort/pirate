(function () {
	"use strict";
	app.controller('environmentCtrl', ['$scope', 'httpLoad','webSocket', '$rootScope', '$modal', '$state', '$timeout','$location',
	'$anchorScroll','CommonData',
		function ($scope, httpLoad,webSocket, $rootScope, $modal, $state, $timeout,$location,$anchorScroll,CommonData) {
			$rootScope.moduleTitle = '<a ui-sref="paas.repository.repository">环境资源</a> > 环境管理';//定义当前页
	    $rootScope.link = '/statics/css/image.css';//引入页面样式
		  $scope.isListView = true;
			$scope.Attribute = CommonData.openstackListAttribute;
			$scope.typeData = {1:"不可用",2:"激活状态",3:"冻结状态",4:"异常状态",5:"创建中",6:"死亡"};
			$scope.platfromData = {1:"kubernetes",2:"swarm"};
			$scope.nodeSourceData = {'receive':"接管",'create':"创建"};
			$scope.param = {
				page:1,
				rows: 10
			};
			//websocket异步操作
			webSocket.onmessage({
				message:function (data) {
					if($rootScope.currentUrl=='paas.environment.environment'){
						$scope.getData();
					}
				}
			});

			//获取云主机列表
			$scope.getData = function (page) {
				$scope.param.page = page || $scope.param.page;
				httpLoad.loadData({
					url: '/environment/list',
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
							$scope.gopost = $rootScope.userData.id
						}
					}
				});
			};

			$scope.getData(1);

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

			//详情
			$scope.detail = function(id,port){
				// $state.go('paas.environment.environmentdetail', {id: id})
				$state.go('paas.environment.newEnvironmentdetail', {id: id})

			}
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
					name: $scope.searchByName
				});

				var param2 =  toObjFormat({
					status:$scope.$parent.status
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
			$scope.goAction = function (flag,row) {
				switch (flag / 1) {
					case 0:
						//接管集群
						var modalInstance = $modal.open({
							templateUrl : '/statics/tpl/environment/environment/takeOverControl.html',
							controller : 'takeOverControlModalCtrl',// 初始化模态范围
							backdrop: 'static',
							resolve : {
								id : function(){
									return row.id;
								}
							}
						});
						modalInstance.result.then(function(){
							$scope.getData(1);
						},function(){});

						break;
					case 1:
					//新增
					var modalInstance = $modal.open({
							templateUrl : '/statics/tpl/environment/environment/add.html',
							controller : 'addenvironmentModalCtrl',// 初始化模态范围
								backdrop: 'static',
							resolve : {
								row : function(){
									return null;
								}
							}
					});
					modalInstance.result.then(function(){
							$scope.getData(1);
					},function(){});

						break;
					case 2:
					//编辑
							var modalInstance = $modal.open({
                templateUrl : '/statics/tpl/environment/environment/add.html',
                controller : 'addenvironmentModalCtrl',// 初始化模态范围
								backdrop: 'static',
								resolve : {
									row : function(){
										return row;
									}

								}
							});
							modalInstance.result.then(function(){
								$scope.getData(1);
							},function(){});

						break;
					case 3:
						//删除

								var ids=[];
								ids.push(row.id);
								var modalInstance = $modal.open({
									templateUrl : '/statics/tpl/environment/environment/remove.html',
									controller : 'removeEnvironmentModalCtrl',
										backdrop: 'static',
									resolve : {
										id : function(){
											return ids;
										}

									}
								});
								modalInstance.result.then(function(){
									$scope.getData(1);
									$scope.isCheck = false;
								},function(){});

						break;
					case 4:
						var text = '冻结';
						if(row.status=='3'){text = '激活'}
						var modalInstance = $modal.open({
							templateUrl: '/statics/tpl/template/delModal.html',
							controller: 'delModalCtrl',
							backdrop: 'static',
							resolve: {
								tip: function () {
									return '是否'+text+"?";
								},
								btnList: function () {
									return [{name: text, type: 'btn-info'}, {name: '取消', type: 'btn-cancel'}];
								}
							}
						});
						var status = ""
						if(row.status==2){
							status = 3
						}else if(row.status==3){
							status = 2
						}
						modalInstance.result.then(function () {
							httpLoad.loadData({
								url: '/environment/operate',
								method: 'POST',
								data: {id: row.id,status:status},
								success: function (data) {
									if (data.success) {

										$scope.getData();
										$scope.pop(data.message);
									}
								}
							});
						});

						break;
					case 6:
						//加入节点
						var modalInstance = $modal.open({
							templateUrl : '/statics/tpl/environment/environment/addnode.html',
							controller : 'addNodeEnvModalCtrl',
							backdrop: 'static',
							resolve : {
								id : function(){
									return row.id;
								},
								node : function(){
									return flag;
								}

							}
						});
						modalInstance.result.then(function(){
							$scope.getData(1);
							$scope.isCheck = false;
						},function(){});

						break;
					case 5:
						//删除节点
						var modalInstance = $modal.open({
							templateUrl : '/statics/tpl/environment/environment/removenode.html',
							controller : 'removeNodeEnvModalCtrl',
							backdrop: 'static',
							resolve : {
								id : function(){
									return row.id;
								},
								node : function(){
									return flag;
								}

							}
						});
						modalInstance.result.then(function(){
							$scope.getData(1);
							$scope.isCheck = false;
						},function(){});
						break;
					case 7:
						//高可用
						var modalInstance = $modal.open({
							templateUrl : '/statics/tpl/environment/environment/highUse.html',
							controller : 'highUseModalCtrl',
							backdrop: 'static',
							resolve : {
								id : function(){
									return row.id;
								},
								node : function(){
									return flag;
								}

							}
						});
						modalInstance.result.then(function(){
							$scope.getData(1);
							$scope.isCheck = false;
						},function(){});
						break;
					case 8:
						//创建集群
						var modalInstance = $modal.open({
							templateUrl : '/statics/tpl/environment/environment/createMaster.html',
							controller : 'createMasterModalCtrl',// 初始化模态范围
							backdrop: 'static',
							resolve : {
								id : function(){
									return row.id;
								}
							}
						});
						modalInstance.result.then(function(){
							$scope.getData(1);
						},function(){});

						break;

					case 9:
						// 创建环境
						$state.go('paas.environment.environmentAdd');
				}

			};
			$scope.deteleAll = function ($event) {
				var ids = [];
				for(var i=0;i<$scope.countData.length;i++){
					var item = $scope.countData[i]
					if(item.select){
						ids.push(item.id);
						if(item.status==5){
							$scope.pop('当前选择项中存在创建中的环境，创建中的环境不能删除，请重新选择','error');
							return false;
						}
					}
				}
				var modalInstance = $modal.open({
					templateUrl : '/statics/tpl/environment/environment/remove.html',
					controller : 'removeEnvironmentModalCtrl',
						backdrop: 'static',
					resolve : {
						id : function(){
							return ids;
						}

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
	//新增节点
	angular.module('app').controller('addNodeEnvModalCtrl',['$scope','$modalInstance','LANGUAGE','httpLoad','id','node',
		function($scope,$modalInstance,LANGUAGE,httpLoad,id,node){ //依赖于modalInstance
			if(node==6){
				//增加节点
				$scope.modalName = '增加节点';
				//查询可以添加到环境中的主机
				httpLoad.loadData({
					url: '/host/queryNormalHost',
					method: 'POST',
					data:null,
					success: function (data) {
						if (data.success) {
							$scope.addNode =  data.data;
							$scope.removeNode = [];
						}
					}
				});
			}else if(node==5){
				// 删除节点
				$scope.modalName = '删除节点';
				//查询在环境中的主机
				httpLoad.loadData({
					url: '/host/queryHostInEnv',
					method: 'POST',
					data: {id:id},
					success: function (data) {
						if (data.success) {
							$scope.addNode =  data.data;
							$scope.removeNode = [];
						}
					}
				});
			}

			//添加
			$scope.addNodes= function(){
				for(var i=0;i<$scope.addNode.length;i){
					var item = $scope.addNode[i]
					if(item.select){
						$scope.removeNode.push(item);
						$scope.addNode.splice(i,1)
						item.select = false
					}else{
						i++;
					}
				}
			}
			//删除
			$scope.removeNodes = function(){
				for(var i=0;i<$scope.removeNode.length;i){
					var item = $scope.removeNode[i]
					if(item.select){
						$scope.addNode.push(item);
						$scope.removeNode.splice(i,1)
						item.select = false
					}else{
						i++;
					}
				}
			}
			$scope.isLoadingCheck = true
			$scope.ok = function () {
				var param =[];
				$scope.removeNode.forEach(function(item){
					param.push(item.id);
				})
				var url;
				if(node==6){
					url='/environment/addNode';
				}else if(node==5){
					url='/environment/deleteNode';
				}

				httpLoad.loadData({
					url:url,
					method:'POST',
					data: {ids:param,envId:id},
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
	// 刪除节点
	angular.module('app').controller('removeNodeEnvModalCtrl',['$scope','$modalInstance','LANGUAGE','httpLoad','id','node',
		function($scope,$modalInstance,LANGUAGE,httpLoad,id,node){ //依赖于modalInstance
			$scope.statusTypeData = {1:"正常",2:"异常",3:"可调度",4:"不可调度",5:"添加中",6:"移出中"};
				//查询在环境中的主机
				httpLoad.loadData({
					url: '/host/queryHostInEnv',
					method: 'POST',
					data: {id:id},
					success: function (data) {
						if (data.success) {
							$scope.addNode =  data.data;
							$scope.itemsByPage=5;
							if(data.data){
								$scope.totalCount=data.data.length;
							}
						}
					}
				});

			$scope.operation = {};
			$scope.choose = function(row){
				if(row.select==true){
					$scope.addNode.forEach(function(item){
						item.select=false;
					});
					row.select=true;
					$scope.operation.selectItem=row.id;
				}else{
					$scope.operation.selectItem="";
				}
			};
			
			$scope.ok = function () {
				if(!$scope.operation.selectItem){
					$scope.pop('请选择一条节点信息','error');
					return false;
				}
				var param =[];
					param.push($scope.operation.selectItem);
				httpLoad.loadData({
					url:'/environment/deleteNode',
					method:'POST',
					data: {ids:param,envId:id},
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
	//高可用ctrl
	angular.module('app').controller('highUseModalCtrl',['$scope','$modalInstance','$timeout','httpLoad','LANGUAGE','id',
		function($scope,$modalInstance,$timeout,httpLoad,LANGUAGE,id){ //依赖于modalInstance
			$scope.modalName="高可用";
			$scope.number=5;

			// httpLoad.loadData({
			// 	url:'/application/getReplicas',
			// 	method:'POST',
			// 	data: {params:angular.toJson([{"param":{}}]),namespace:row.namespace,resourceName:row.name,resourceType:type,appId:$stateParams.id},
			// 	noParam: true,
			// 	success:function(data){
			// 		if(data.success){
			$timeout(function(){
				$scope.slider = {
					value:5,
					options: {
						floor: 1,
						ceil: 10
					}
				};
			},10);

					// }
					// else {
					/*	$scope.slider = {
							value:  0,
							options: {
								floor: 0,
								ceil: 100
							}

						};*/
					// }
			// 	}
			// });
			$scope.ok = function(){
				httpLoad.loadData({
					url:'/environment/receiveCluster',
					method:'POST',
					data: {envId:id},
					success:function(data){
						if(data.success){
							$scope.pop(data.message);
							$modalInstance.close();

						}
					}
				});
			};
			$scope.cancel = function(){
				$modalInstance.dismiss('cancel');
			}
		}]);
	//接管集群ctrl
	angular.module('app').controller('takeOverControlModalCtrl',['$scope','$modalInstance','httpLoad','LANGUAGE','id',
		function($scope,$modalInstance,httpLoad,LANGUAGE,id){ //依赖于modalInstance
			$scope.ip="";
			$scope.ok = function(){
				httpLoad.loadData({
					url:'/environment/receiveCluster',
					method:'POST',
					data: {ip:$scope.ip,envId:id},
					success:function(data){
						if(data.success){
							$scope.pop(data.message);
							$modalInstance.close();

						}
					}
				});
			};
			$scope.cancel = function(){
				$modalInstance.dismiss('cancel');
			}
		}]);
	//创建集群ctrl
	angular.module('app').controller('createMasterModalCtrl',['$scope','$modalInstance','httpLoad','LANGUAGE','id',
		function($scope,$modalInstance,httpLoad,LANGUAGE,id){ //依赖于modalInstance
			$scope.modalName = '创建集群';
			httpLoad.loadData({
				url: '/host/queryNormalHost',
				method: 'POST',
				data:null,
				success: function (data) {
					if (data.success) {
						$scope.addNode =  data.data;
						$scope.removeNode = [];
					}
				}
			});
			//添加
			$scope.addNodes= function(){
				for(var i=0;i<$scope.addNode.length;i){
					var item = $scope.addNode[i]
					if(item.select){
						$scope.removeNode.push(item);
						$scope.addNode.splice(i,1)
						item.select = false
					}else{
						i++;
					}
				}
			}
			//删除
			$scope.removeNodes = function(){
				for(var i=0;i<$scope.removeNode.length;i){
					var item = $scope.removeNode[i]
					if(item.select){
						$scope.addNode.push(item);
						$scope.removeNode.splice(i,1)
						item.select = false
					}else{
						i++;
					}
				}
			}
			$scope.ok = function(){
				var param =[];
				$scope.removeNode.forEach(function(item) {
					param.push({"ip": item.ip, "username": item.username, "password": item.password,'envId':id,'labels':angular.fromJson(item.labels),'id':item.id,'name':item.name});
				});
			 		httpLoad.loadData({
					url:'/environment/createKubernetesCluser',
					method:'POST',
					data: {'hosts':param},
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
			}
		}]);
	//新增ctrl
	 angular.module('app').controller('addenvironmentModalCtrl',['$scope','$modalInstance','LANGUAGE','httpLoad','row',
			 function($scope,$modalInstance,LANGUAGE,httpLoad,row){ //依赖于modalInstance
				 // var editObj = ['name','remark'];
   	 		$scope.modalName = '环境创建';

					var url = '/environment/create';
					 $scope.addData = {};
						//检查名称

					 //如果为编辑，进行赋值
	        if (row) {
	                url = '/environment/modify';
	                $scope.modalName = '环境编辑';

	                httpLoad.loadData({
	                    url: '/environment/detail',
	                    method: 'GET',
	                    data: {id: row.id},
	                    success: function (data) {
	                        if (data.success) {
	                            var data = data.data;
	                            // for (var a in editObj) {
	                            //     var attr = editObj[a];
	                            //     $scope.addData[attr] = data[attr];
	                            // }
								$scope.addData = data;
	                        }
	                    }
	                });
	            }
							$scope.isLoadingCheck = true
					$scope.ok = function () {
							 var param = {};
							/* editObj.forEach(function(attr){
								 param[attr] = $scope.addData[attr];
								})*/
							 param =$scope.addData;
								if(row){
									param.id = row.id;
									param.status = row.status;
									param.ownerId = row.ownerId;
								}
								param.platform = "1";
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
	angular.module('app').controller('removeEnvironmentModalCtrl',['$scope','$modalInstance','httpLoad','LANGUAGE','id',
		function($scope,$modalInstance,httpLoad,LANGUAGE,id){ //依赖于modalInstance
			$scope.content = '您确定要删除吗？';
			$scope.ok = function(){
				httpLoad.loadData({
					url:'/environment/remove',
					method:'POST',
					data: {id:id},
					success:function(data){
						if(data.success){
						$scope.pop(data.message);
							$modalInstance.close();

					}
					}
				});
			};
			$scope.cancel = function(){
				$modalInstance.dismiss('cancel');
			}
		}]);
	app.controller('environmentHistoryModalCtrl', ['$rootScope', '$scope','$state','httpLoad','$stateParams',function($rootScope, $scope,$state,httpLoad,$stateParams) {
		$rootScope.link = '/statics/css/alarm.css';//引入页面样式
		$scope.param = {
			page:1,
			rows: 10,
			params:angular.toJson([{"param":{"object":"environment"},"sign":"LK"}])
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
