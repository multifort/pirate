(function () {
	"use strict";

	app.controller('modelarrangeCtrl', ['$scope', 'httpLoad', '$rootScope', '$modal', '$state', '$timeout','$location','$anchorScroll',
		function ($scope, httpLoad, $rootScope, $modal, $state, $timeout,$location,$anchorScroll) {
			$rootScope.moduleTitle = '流程管控 > 流程编排';//定义当前页
	    $rootScope.link = '/statics/css/image.css';//引入页面样式
		  $scope.isListView = true;
			$scope.param = {
					page:1,
					rows: 10,

			//	params: JSON.stringify([{"param": {"type": "VMWARE"}, "sign": "EQ"}])
			};

			//获取云主机列表
			$scope.getData = function (page) {
				$scope.param.page = page || $scope.param.page;
				httpLoad.loadData({
					url: '/workflow/workflowDefs',
					method: 'GET',
					data: $scope.param,
					noParam: true,
					success: function (data) {
						if (data.success) {
							$scope.displayListData = [];
							$scope.totalCount = data.data.total;
							$scope.listData = data.data.rows;
							$scope.listData.forEach(function(item){
									var task =[];
									var json = eval('(' + item.workflowDef + ')');
									if(json.tasks){
                                        json.tasks.forEach(function(key){
                                            task.push(key.name)
                                        })
                                        json.task = task;
									}

									if(json.inputParameters == ''){
										json.inputParameter =''
									}else{
										json.inputParameter = json.inputParameters
									}
									json.workflowId = item.workflowId;
									json.status = item.status;
									json.id = item.id;
									$scope.displayListData.push(json)

							})

							$scope.total = data.data.total;
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

			$scope.getopenDetail(1);
			$scope.getData(1);
			//详情
			$scope.detail = function(row){
				 $state.go('paas.process.workflowdetail', {id: row.workflowId,name:row.name,version : row.version})
//				$state.go('paas.process.layoutdetail', {name: row.name,version:row.version})

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
//				$scope.param.page = page || $scope.param.page;
				//对参数进行处理，去除空参数
				$scope.param = {
				 		page:1,
					        rows: 10,
					        params:angular.toJson([{"param":{"name":$scope.searchByName},"sign":"LK"}])
					    };
				httpLoad.loadData({
					url: '/workflow/workflowDefs',
					method: 'GET',
					data: $scope.param,
					noParam: true,
					success: function (data) {
						if (data.success) {
							$scope.displayListData = [];
							$scope.totalCount = data.data.total;
							$scope.listData = data.data.rows;
							$scope.listData.forEach(function(item){
									var task =[];
									var json = eval('(' + item.workflowDef + ')'); 
									json.tasks.forEach(function(key){
											task.push(key.name)
									})
									json.task = task;
									if(json.inputParameters == ''){
										json.inputParameter =''
									}else{
										json.inputParameter = json.inputParameters
									}
									json.workflowId = item.workflowId;
									json.status = item.status;
									json.id = item.id;
									$scope.displayListData.push(json)

							})

							$scope.total = data.data.total;
							if(!$scope.total){
								$scope.isImageData = true;
								} else {
									$scope.isImageData = false;
							}
						}
					}
				});
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
                        $state.go('paas.process.newlayout')
						break;
					case 2:
					//编辑
                        $state.go('paas.process.newlayout',{name:row.name,version:row.version,workflowId:row.workflowId,status:row.status})

                        break;
					case 3:
						$scope.pop("正在启动运行······");
						 httpLoad.loadData({
							 url:'/workflow/running',
							 method:'POST',
							 data: {id:row.id},
							 success:function(data){
									 if(data.success){
										 $scope.pop(data.message);
										 $scope.getData(1);
									 }
							 }
					 });

						break;
					case 4:
					//启动
					$scope.pop("正在启动运行······");
				
					 var modalInstance = $modal.open({
					 	templateUrl : '/statics/tpl/process/workflow/startmodel.html',
					 	controller : 'startmodelModalCtrl',
					 		backdrop: 'static',
					 		  size: 'lg',
					 	resolve : {
					 		row : function(){
					 			return row;
					 		},
					
					 	}
					 });
					 modalInstance.result.then(function(){
					 	$scope.getData(1);
					 	$scope.isCheck = false;
					 },function(){});
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
					templateUrl : '/statics/tpl/process/model/remove.html',
					controller : 'removemodelModalCtrl',
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
//删除ctrl
	angular.module('app').controller('removemodelModalCtrl',['$scope','$modalInstance','httpLoad','LANGUAGE','id',
		function($scope,$modalInstance,httpLoad,LANGUAGE,id){ //依赖于modalInstance
			$scope.content = '是否删除？';
			$scope.ok = function(){
				httpLoad.loadData({
					url:'/layout/remove',
					method:'POST',
					data: {id:id},
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
			        params:angular.toJson([{"param":{"object":"workflow"},"sign":"LK"}])
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
		//启动
		 angular.module('app').controller('startmodelModalCtrl',['$scope','$modalInstance','LANGUAGE','httpLoad','row','$timeout',
				 function($scope,$modalInstance,LANGUAGE,httpLoad,row,$timeout){ //依赖于modalInstance
					$scope.modalName = '启动';
						$scope.inputParametersList = [];
						$scope.addData = {};
					 var editObj = ['name','version'];
					var data = row;
					for (var a in editObj) {
							var attr = editObj[a];
							$scope.addData[attr] = data[attr];
					}
					row.inputParameters.forEach(function(item){
						$scope.inputParametersList.push({key:item,value:''})
					})
					$scope.addEnvs = function(){
											$scope.inputParametersList.push({key:'',value:''})
					 }

					 $scope.removeEnv = function(key){
						 if($scope.inputParametersList.length == 1) return $scope.pop('请至少添加一组','error');
						 $scope.inputParametersList.splice(key,1);
					 }

						$scope.ok = function () {
								 var param = {};
								 editObj.forEach(function(attr,a){
									 param[attr] = $scope.addData[attr];
							})
							// var inputParametersList = {};
							// for(var i=0;i<$scope.inputParametersList.length;i++){
							// 	var item = $scope.inputParametersList[i];
							// 	if((!item.key&&item.value)||(item.key&&!item.value)){
							// 				$scope.pop('请添加完整的输入项','error');
							// 				return;
							// 		 }else if(!item.key&&!item.value){
							// 			 continue
							// 		 }else{
							// 				inputParametersList[item.key] = item.value;
							// 		 }
							// }
							// 	 param.input = inputParametersList;
							// 	 param.input.onlyFlag = param.name+'_'+param.version +'_';

								 httpLoad.loadData({
										 url:'/workflow/start',
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

})();
