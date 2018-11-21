(function () {
	"use strict";
	app.controller('networkCtrl', ['$scope', 'httpLoad','webSocket', '$rootScope', '$modal', '$state', '$timeout','$location',
	'$anchorScroll','CommonData',
		function ($scope, httpLoad,webSocket, $rootScope, $modal, $state, $timeout,$location,$anchorScroll,CommonData) {
			$rootScope.moduleTitle = '<a ui-sref="paas.repository.repository">资源管理</a> > 集群管理';//定义当前页
	    $rootScope.link = '/statics/css/image.css';//引入页面样式
		  $scope.isListView = true;
			$scope.Attribute = CommonData.openstackListAttribute;
			$scope.typeData = CommonData.openstackListType;
			$scope.param = {
				page:1,
				rows: 10,
			};
			//websocket异步操作
			webSocket.onmessage({
				message:function (data) {
					if($rootScope.currentUrl=='paas.environment.network'){
						$scope.getData();
					}
				}
			});

			//获取云主机列表
			$scope.getData = function (page) {
				$scope.param.page = page || $scope.param.page;
				httpLoad.loadData({
					url: '/registry/list',
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
			//获概述列表
			$scope.getopenDetail = function (page) {
				$scope.param.page = page || $scope.param.page;
				httpLoad.loadData({
					url: '/registry/count',
					method: 'POST',
					data: $scope.param,
					noParam: true,
					success: function (data) {
						if (data.success) {
							$scope.countopenDetail = data.data;

						}
					}
				});
			};

			$scope.getopenDetail(1);
			$scope.getData(1);



			//详情
			$scope.detail = function(id,port){
				$state.go('paas.environment.networkDetail', {id: id,port:port})

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

				var param2 = {
					property:$scope.$parent.popr,
					type:$scope.$parent.type

				};
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

			//返回
			$scope.goAction = function (flag,row) {
				switch (flag / 1) {
					case 1:
					//新增
					var modalInstance = $modal.open({
							templateUrl : '/statics/tpl/environment/network/add.html',
							controller : 'addnetworkModalCtrl',// 初始化模态范围
								backdrop: 'static',
							resolve : {
								id : function(){
									return null;
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
                templateUrl : '/statics/tpl/environment/network/add.html',
                controller : 'addnetworkModalCtrl',// 初始化模态范围
								backdrop: 'static',
								resolve : {
									id : function(){
										return row.id;
									},

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
									templateUrl : '/statics/tpl/environment/network/remove.html',
									controller : 'removeNetworkModalCtrl',
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
						//加入节点
						var modalInstance = $modal.open({
								templateUrl : '/statics/tpl/environment/network/addnode.html',
							controller : 'addNodeNetworkModalCtrl',
							backdrop: 'static',
							resolve : {
								id : function(){
									return row.id;
								},
								node : function(){
									return flag;
								},

							}
						});
						modalInstance.result.then(function(){
							$scope.getData(1);
						},function(){});

						break;
						case 5:
						var modalInstance = $modal.open({
								templateUrl : '/statics/tpl/environment/network/addnode.html',
							controller : 'addNodeNetworkModalCtrl',
							backdrop: 'static',
							resolve : {
								id : function(){
									return row.id;
								},
								node : function(){
									return flag;
								},

							}
						});
						modalInstance.result.then(function(){
							$scope.getData(1);
						},function(){});
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
					templateUrl : '/statics/tpl/repository/repository/remove.html',
					controller : 'removeUserModalCtrl',
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
	 angular.module('app').controller('addnetworkModalCtrl',['$scope','$modalInstance','LANGUAGE','httpLoad','id',
			 function($scope,$modalInstance,LANGUAGE,httpLoad,id){ //依赖于modalInstance
				 var editObj = ['name','registryPort', 'address',"password", "username", 'remark'];
   	 		$scope.modalName = '集群创建';
   	 		$scope.check;
   	 		$scope.modefy = false;
					var url = '/registry/create';
					 $scope.addData = {};
						//检查名称

					 //如果为编辑，进行赋值
	        if (id) {
	                url = '/registry/modify';
	                $scope.modalName = '集群编辑';
	                $scope.modefy = true;
									$scope.check = false;
	                httpLoad.loadData({
	                    url: '/registry/detail',
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
							$scope.isLoadingCheck = true
							$scope.checkName = function(address,username,password){
											if(address && username && password){
												var params={"address":address,"username":username,"password":password}
												httpLoad.loadData({
													url: '/openshift/cluster/master/check',
													method: 'POST',
													data: params,
													//noParam: true,
													success: function (data) {
														if (data.success) {
													$scope.pop(data.message);
														$scope.isLoadingCheck = false
														}
													}
												});
											}

										}
					$scope.ok = function () {
							 var param = {id:id};
							 editObj.forEach(function(attr){
								 param[attr] = $scope.addData[attr];
								})
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
	angular.module('app').controller('removeNetworkModalCtrl',['$scope','$modalInstance','httpLoad','LANGUAGE','id',
		function($scope,$modalInstance,httpLoad,LANGUAGE,id){ //依赖于modalInstance
			$scope.content = '您确定要删除吗？';
			$scope.ok = function(){
				httpLoad.loadData({
					url:'/registry/remove',
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
  //新增节点
   angular.module('app').controller('addNodeNetworkModalCtrl',['$scope','$modalInstance','LANGUAGE','httpLoad','id','node',
       function($scope,$modalInstance,LANGUAGE,httpLoad,id,node){ //依赖于modalInstance
          $scope.modalName = '增加节点';
					$scope.id = node;
              //检查名称
          httpLoad.loadData({
              url: '/network/addnode',
              method: 'GET',
              data: {id: id},
              success: function (data) {
                  if (data.success) {
                      var data = data.data;
                        $scope.addNode =  data.add
                        $scope.removeNode = data.remove
                  }
              }
          });
          //删除
          $scope.addNodes= function(){
            for(var i=0;i<$scope.addNode.length;i++){
              var item = $scope.addNode[i]
              if(item.select){
                $scope.removeNode.push(item);
								$scope.addNode.splice(i,1)
								item.select = false
              }
            }
          }
          //删除
          $scope.removeNodes = function(){
            for(var i=0;i<$scope.removeNode.length;i++){
              var item = $scope.removeNode[i]
              if(item.select){
                $scope.addNode.push(item);
								$scope.removeNode.splice(i,1)
								item.select = false
              }
            }
          }
          $scope.isLoadingCheck = true
          $scope.checkName = function(address,username,password){
                      if(address && username && password){
                        var params={"address":address,"username":username,"password":password}
                        httpLoad.loadData({
                          url: '/openshift/cluster/master/check',
                          method: 'POST',
                          data: params,
                          //noParam: true,
                          success: function (data) {
                            if (data.success) {
                          $scope.pop(data.message);
                            $scope.isLoadingCheck = false
                            }
                          }
                        });
                      }

                    }
          $scope.ok = function () {
               var param = {id:id};
               editObj.forEach(function(attr){
                 param[attr] = $scope.addData[attr];
                })
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

//历史记录
	app.controller('historyOpenstackModalCtrl', ['$rootScope','$timeout', '$scope','$state','httpLoad','$stateParams','$anchorScroll',
	function($rootScope,$timeout, $scope,$state,httpLoad,$stateParams,$anchorScroll) {
			$rootScope.link = '/statics/css/alarm.css';//引入页面样式
		    $scope.param = {
		    		page:1,
			        rows: 10,
			        params:angular.toJson([{"param":{"object":"registry"},"sign":"LK"}])
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
			//详情

		}]);

})();
