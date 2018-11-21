(function () {
	"use strict";
	app.controller('openstackCtrl', ['$scope', 'httpLoad','webSocket', '$rootScope', '$modal', '$state', '$timeout','$location',
	'$anchorScroll','CommonData',
		function ($scope, httpLoad,webSocket, $rootScope, $modal, $state, $timeout,$location,$anchorScroll,CommonData) {
			$rootScope.moduleTitle = '<a ui-sref="paas.repository.repository">镜像仓库</a> > 仓库管理';//定义当前页
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
					if($rootScope.currentUrl=='paas.repository.repository'){
						 
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
				$state.go('paas.repository.repositorydetail', {id: id,port:port})

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
			$scope.goAction = function (flag, id,row,$event) {
				switch (flag / 1) {
					case 1:
					//新增
					var modalInstance = $modal.open({
							templateUrl : '/statics/tpl/repository/repository/add.html',
							controller : 'addtenantModalCtrl',// 初始化模态范围
								backdrop: 'static',
							resolve : {
								id : function(){
									return id;
								},
							}
					});
					modalInstance.result.then(function(){
							$scope.getData(1);
                        $scope.getopenDetail(1);
					},function(){});

						break;
					case 2:
					//编辑
							var modalInstance = $modal.open({
									templateUrl : '/statics/tpl/repository/repository/add.html',
								controller : 'addtenantModalCtrl',
								backdrop: 'static',
								resolve : {
									id : function(){
										return id;
									}

								}
							});
							modalInstance.result.then(function(){
								$scope.getData(1);
                                $scope.getopenDetail(1);
							},function(){});

						break;
					case 3:
						//删除

								var ids=[];
								ids.push(id);
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
                                    $scope.getopenDetail(1);
									$scope.isCheck = false;
								},function(){});

						break;
					case 4:
						//导入
						var modalInstance = $modal.open({
								templateUrl : '/statics/tpl/repository/repository/gridinpull.html',
							controller : 'gridinopenstackModalCtrl',
							backdrop: 'static',
							resolve : {
								id : function(){
									return id;
								},
								row : function(){
									return row;
								},

							}
						});
						modalInstance.result.then(function(){
							$scope.getData(1);
                            $scope.getopenDetail(1);
						},function(){});

						break;
					case 5:
						var modalInstance = $modal.open({
							templateUrl: '/statics/tpl/template/delModal.html',
							controller: 'delModalCtrl',
							backdrop: 'static',
							resolve: {
								tip: function () {
									return '删除过程中不允许对该仓库和镜像进行操作？';
								},
								btnList: function () {
									return [{name: '确定', type: 'btn-info'}, {name: '取消', type: 'btn-cancel'}];
								}
							}
						});
						/*modalInstance.result.then(function () {
							var param = {};
							param.params=angular.toJson([{"param":{}}]);
							param.id = id;
							httpLoad.loadData({
								url: '/registry/gc',
								method: 'POST',
								data: param,
		                        noParam: true,
								success: function (data) {
									if (data.success) {
										$scope.pop(data.message);
										$scope.getData(1);
									}
								}
							})
						});*/

						break;
					case 6:
					var modalInstance = $modal.open({
						templateUrl: '/statics/tpl/template/delModal.html',
						controller: 'delModalCtrl',
						backdrop: 'static',
						resolve: {
							tip: function () {
								return '是否执行镜像同步操作？';
							},
							btnList: function () {
								return [{name: '同步', type: 'btn-info'}, {name: '取消', type: 'btn-cancel'}];
							}
						}
					});
					modalInstance.result.then(function () {
						var param = {};
						param.params=angular.toJson([{"param":{}}]);
						param.id = id;
						httpLoad.loadData({
							url: '/registry/sycn/image',
							method: 'GET',
							data: param,
													noParam: true,
							success: function (data) {
								if (data.success) {
									$scope.pop(data.message);
									$scope.getData(1);
								}
							}
						})
					});

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
                    $scope.getopenDetail(1);
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
	 angular.module('app').controller('addtenantModalCtrl',['$scope','$modalInstance','LANGUAGE','httpLoad','id',
			 function($scope,$modalInstance,LANGUAGE,httpLoad,id){ //依赖于modalInstance
				 var editObj = ['name','port', 'address',"password", "username", 'remark','authMode','protocol'];
				 $scope.protocolTypeData=[{name:'http协议',value:0},{name:'https协议',value:1}];
				 $scope.authMethodData=[{name:'公共认证',value:0},{name:'私有认证',value:1}];
				$scope.modalName = '仓库创建';
				$scope.check;$scope.id = id;
                 $scope.housesize = "";
					var url = '/registry/create';
					 $scope.addData = {};
				 $scope.housetype = 0;
				 $scope.addData.authMode = 0;
						//检查名称
						$scope.typeChange = function(type,$event){
							if(id){return}
							$scope.housetype =type;
                            if(type == 1){
								$scope.addData.authMode = 1;
								if($scope.addData.protocol==0) $scope.housesize=80;
								else if($scope.addData.protocol==1) $scope.housesize=443;
							}else {
								$scope.addData.authMode =0;
								if($scope.addData.protocol==0) $scope.housesize=5000;
								else if($scope.addData.protocol==1) $scope.housesize=443;
							}
						}
				 $scope.changePort=function () {
					 if(($scope.addData.protocol==0)&&($scope.addData.authMode ==0))$scope.housesize=5000;
					else if(($scope.addData.protocol==0)&&($scope.addData.authMode ==1)) $scope.housesize=80;
					else if($scope.addData.protocol==1) $scope.housesize=443;
				 }

					 //如果为编辑，进行赋值
	        if (id) {
	                url = '/registry/modify';
	                $scope.modalName = '仓库编辑';
					$scope.check = false;
	                httpLoad.loadData({
	                    url: '/registry/detail',
	                    method: 'GET',
	                    data: {id: id},
	                    success: function (data) {
	                        if (data.success) {
	                            var data = data.data;
								$scope.housetype =data.type;
	                            for (var a in editObj) {
	                                var attr = editObj[a];
	                                $scope.addData[attr] = data[attr];
	                            }

	                        }
	                    }
	                });
	            }
							$scope.ok = function () {
												 var param = {id:id};
												 editObj.forEach(function(attr){
													 param[attr] = $scope.addData[attr];
													})
													if(param.authMode ==0){
														param.password = '';
                                                        param.username = '';
													}
													param.type = $scope.housetype;
								           if(!param.port){
								    	        param.port=$scope.housesize;
										    }
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
	angular.module('app').controller('removeUserModalCtrl',['$scope','$modalInstance','httpLoad','LANGUAGE','id',
		function($scope,$modalInstance,httpLoad,LANGUAGE,id){ //依赖于modalInstance
			$scope.content = '删除仓库会删除仓库数据库中的所有镜像信息，您确定要删除吗？';
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
				$scope.operation.isBatch1 = true;
			}
		}]);
	//导入
	angular.module('app').controller('gridinopenstackModalCtrl',['$scope','$timeout','$modalInstance','httpLoad','LANGUAGE','id','row',
		function($scope,$timeout,$modalInstance,httpLoad,LANGUAGE,id,row){ //依赖于modalInstance
			$scope.websocketUrl='/uploadService';
			$scope.warehoseItem = id;
			$scope.warehoseName = row.name;
            $scope.scriptItemSpace = {}
            $scope.scriptItemSpace.selected = "";
			// httpLoad.loadData({
			// 	url: '/registry/list',
			// 	method: 'POST',
			// 	data: $scope.param,
			// 	noParam: true,
			// 	success: function (data) {
			// 		if (data.success) {
			// 			$scope.warehoseData = data.data.rows;
            //
			// 		}
			// 	}
			// });
			if(row.type == 1){
				$timeout(function(){$scope.imageshow = true;},100)              
                httpLoad.loadData({
                    url: '/image/getProjects',
                    method: 'GET',
                    data: {registryId:id},
                    noParam: true,
                    ignoreError: true,
                    success: function (data) {
                        if (data.success) {
                            $scope.imageData = data.data;


                        }
                    }
                });
			}
			$scope.cancel = function(){
			$modalInstance.dismiss('cancel');
			}
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
