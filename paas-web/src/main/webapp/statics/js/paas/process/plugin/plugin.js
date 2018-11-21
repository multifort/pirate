(function () {
	"use strict";

	app.controller('modeltaskCtrl', ['$scope', 'httpLoad', '$rootScope', '$modal', '$state', '$timeout','$location','$anchorScroll',
		function ($scope, httpLoad, $rootScope, $modal, $state, $timeout,$location,$anchorScroll) {
			$rootScope.moduleTitle = '流程管控 > 插件管理';//定义当前页
	    $rootScope.link = '/statics/css/image.css';//引入页面样式
		  $scope.isListView = true;
			$scope.param = {

			//	params: JSON.stringify([{"param": {"type": "VMWARE"}, "sign": "EQ"}])
			};

			//获取云主机列表
			$scope.getData = function () {
				httpLoad.loadData({
					url: '/task/taskDefs',
					method: 'GET',
					data: $scope.param,
				//	noParam: true,
					success: function (data) {
						if (data.success) {
							$scope.listData = data.data;
							$scope.total = data.data.length;
							if(!$scope.total){
								$scope.isImageData = true;
								} else {
									$scope.isImageData = false;
							}
						}
					}
				});
			};
			//获概述列表
			$scope.getopenDetail = function (page) {
				httpLoad.loadData({
					url: '/workflow/total',
					method: 'GET',
					data: $scope.param,
					noParam: true,
					success: function (data) {
						if (data.success) {
							$scope.countopenDetail = data.data;

						}
					}
				});
			};

		//	$scope.getopenDetail(1);
			$scope.getData(1);
			//详情
			$scope.detail = function(name){
				$state.go('paas.process.taskdetail', {id: name})

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

				var param1 = {
					name: $scope.searchByName,
					workflowType:$scope.searchbady,
					status:$scope.status
				};

				$scope.param =  param1

				$scope.getData();}
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
							templateUrl : '/statics/tpl/process/task/add.html',
						controller : 'addmodelModalCtrl',
						backdrop: 'static',
						resolve : {
							row : function(){
								return row;
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
									templateUrl : '/statics/tpl/process/task/add.html',
								controller : 'addmodelModalCtrl',
							backdrop: 'static',
								resolve : {
									row : function(){
										return row;
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
								ids.push(row.name);
								var modalInstance = $modal.open({
									templateUrl : '/statics/tpl/process/task/remove.html',
									controller : 'removemodelModalCtrl',
										backdrop: 'static',
									resolve : {
										name : function(){
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
				//	$state.go('paas.process.modelgridin', {id: id})

						break;
				}
			};

			$scope.execute = function ($event) {
				var modalInstance = $modal.open({
					templateUrl: '/statics/tpl/template/delModal.html',
					controller: 'delModalCtrl',
					backdrop: 'static',
					resolve: {
						tip: function () {
							return '请确认所有的任务被添加？';
						},
						btnList: function () {
							return [{name: '确定', type: 'btn-info'}, {name: '取消', type: 'btn-cancel'}];
						}
					}
				});

				modalInstance.result.then(function () {
					httpLoad.loadData({
						url: '/task/execute',
						method: 'GET',
						data: {},
						success: function (data) {
							if (data.success) {
								$scope.pop(data.message);
							}
						}
					});
				});

			};
			$scope.deteleAll = function ($event) {
				var ids = [];
				for(var i=0;i<$scope.listData.length;i++){
					var item = $scope.listData[i]
					if(item.select){
						ids.push(item.name);
					}
				}
				var modalInstance = $modal.open({
					templateUrl : '/statics/tpl/process/task/remove.html',
					controller : 'removemodelModalCtrl',
						backdrop: 'static',
					resolve : {
						name : function(){
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

	//新增ctrl
	 angular.module('app').controller('addmodelModalCtrl',['$scope','$modalInstance','LANGUAGE','httpLoad','row','$timeout',
			 function($scope,$modalInstance,LANGUAGE,httpLoad,row,$timeout){ //依赖于modalInstance
				var editObj = ['name','timeoutSeconds', 'description'];
				var url = '/task/task';
				$scope.addData = {};
				$scope.modalName = '创建任务模板';
				$scope.isLoadingCheck = true

					 //如果为编辑，进行赋值
	        if (row) {
						$scope.row = row
	        	$scope.isLoadingCheck = false
	                url = '/task/update/task';
	                $scope.modalName = '编辑任务模板';
	                httpLoad.loadData({
	                    url: '/task/taskDef',
	                    method: 'GET',
	                    data: {taskName: row.name},
	                    success: function (data) {
	                        if (data.success) {
	                            var data = data.data;
	                            for (var a in editObj) {
	                                var attr = editObj[a];
	                                $scope.addData[attr] = data[attr];

	                            }
	                            row = data;
                                $scope.addData.type = data.type;
	                        }
	                    }
	                });
	            }
			$scope.checkName = function(name){
				if(name){
					httpLoad.loadData({
						url:"/task/check/name",
						method:'POST',
						data: {taskName:$scope.addData.name},
						success: function (data) {
							if (data.success) {
							$scope.isLoadingCheck = false
							}
						}
					});
				}

			}
					$scope.ok = function () {
							 var task = {},param = {};
							 editObj.forEach(function(attr,a){
								 task[attr] = $scope.addData[attr];
						})
						if(row)param.id = row.id;
						param.type = $scope.addData.type;
						param.task = task;
						httpLoad.loadData({
							url:url,
							method:'POST',
							data: param,
							success:function(data){
									if(data.success){
										var text = '添加成功';
										if (row) text = '编辑成功';
										$scope.pop(text);
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
	angular.module('app').controller('removemodelModalCtrl',['$scope','$modalInstance','httpLoad','LANGUAGE','name',
		function($scope,$modalInstance,httpLoad,LANGUAGE,name){ //依赖于modalInstance
			$scope.content = '是否删除？';
			$scope.ok = function(){
				httpLoad.loadData({
					url:'/task/task',
					method:'DELETE',
					data: {taskName:name},
					success:function(data){
						if(data.success){
							//console.log(removeData);
							$scope.pop(LANGUAGE.MONITOR.APP_MESS.DEL_SUCCESS);
							$modalInstance.close();
							$scope.operation.isBatch1 = true;$scope.isALl = false;
							angular.forEach($scope.listData, function (data, index) {
								data.select = false;

						})
						}
					}
				});
			};
			$scope.cancel = function(){
			$modalInstance.dismiss('cancel');
			}
		}]);
		app.controller('modelhistoryModalCtrl', ['$rootScope', '$scope','$state','httpLoad','$stateParams',function($rootScope, $scope,$state,httpLoad,$stateParams) {
		 $rootScope.link = '/statics/css/alarm.css';//引入页面样式

		 $scope.param = {
		 		page:1,
			        rows: 10,
			        params:angular.toJson([{"param":{"object":"task"},"sign":"LK"}])
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
