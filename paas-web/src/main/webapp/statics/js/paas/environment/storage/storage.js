(function () {
	"use strict";
	app.controller('storeCtrl', ['$scope', 'httpLoad','webSocket', '$rootScope', '$modal', '$state', '$timeout','$location',
	'$anchorScroll','CommonData',
		function ($scope, httpLoad,webSocket, $rootScope, $modal, $state, $timeout,$location,$anchorScroll,CommonData) {
			$rootScope.moduleTitle = '<a ui-sref="paas.repository.repository">环境资源</a> >存储卷';//定义当前页
	    $rootScope.link = '/statics/css/image.css';//引入页面样式
		  $scope.isListView = true;
			$scope.typeData = ["hostPath","NFS","ceph"];
			$scope.param = {
				page:1,
				rows: 10,
			};
			//websocket异步操作
			webSocket.onmessage({
				message:function (data) {
					if($rootScope.currentUrl=='paas.environment.storage'){
						$scope.getData();
					}
				}
			});

			//获取存储卷列表
			$scope.getData = function (page) {
				$scope.param.page = page || $scope.param.page;
				httpLoad.loadData({
					url: '/pv/list',
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
				$state.go('paas.environment.storagedetail', {id: id})

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
					type:$scope.$parent.type
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
			$scope.goAction = function (flag,row) {
				switch (flag / 1) {
					//新增
					case 1:
						//创建存储弹窗
						var modalInstance = $modal.open({
							templateUrl: '/statics/tpl/environment/storage/addmodul.html',
							controller: 'addStoreModalCtrl',
							backdrop: 'static',
							size: 'md',
							resolve: {
								row: function () {
									return null;
								},
								node:function () {
									return flag;
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
							templateUrl: '/statics/tpl/environment/storage/editmodul.html',
							controller: 'addStoreModalCtrl',
							backdrop: 'static',
							size: 'md',
							resolve: {
								row: function () {
									return row;
								},
								node:function () {
									return flag;
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
									controller : 'removeStoreModalCtrl',
										backdrop: 'static',
									resolve : {
										id : function(){
											return ids;
										},

									}
								});
								modalInstance.result.then(function(){
									$scope.getData();
									$scope.isCheck = false;
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
					controller : 'removeStoreModalCtrl',
						backdrop: 'static',
					resolve : {
						id : function(){
							return ids;
						},

					}
				});
				modalInstance.result.then(function(){
					$scope.getData();
					$scope.operation.isALl = false;
					$scope.operation.isBatch1 = true;
					angular.forEach($scope.countData, function (data, index) {
						data.select = false;

				})

				},function(){});
			};
		}
	]);
	//选择存储类型和环境
	angular.module('app').controller('selectTypeAndEnvModalCtrl',['$scope','$modalInstance','httpLoad','LANGUAGE','row',
		function($scope,$modalInstance,httpLoad,LANGUAGE,row){ //依赖于modalInstance
			$scope.typeData = ["hostPath","NFS"];
			$scope.param = {
				page:1,
				rows: 100000,
				params:angular.toJson([{"param":{"status":"2"},"sign":"EQ"}])
			};
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

			$scope.ok = function(){
				var result={type:$scope.type,envId:$scope.envId};
							$modalInstance.close(result);

			};
			$scope.cancel = function(){
				$modalInstance.dismiss('cancel');
			}
		}]);
	app.controller('addStoreModalCtrl', ['$scope', '$modalInstance', '$modal', '$stateParams', '$timeout', 'httpLoad','row','node',
		function ($scope, $modalInstance, $modal, $stateParams, $timeout, httpLoad,row,node) {
			$scope.showTpl=false;
			if(node==1){
				$scope.typeData = ["hostPath","NFS","ceph"];
				$scope.param = {
					page:1,
					rows: 100000,
					params:angular.toJson([{"param":{"status":"2,4"},"sign":"IN"}])
				};
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
			}
			$scope.modalName ="创建存储";
			$scope.reGet=function () {
				if( $scope.type){
					$scope.getTpl();
					$scope.showTpl=true;
				}else{
					$scope.showTpl=false;
				}

			}
			//获取模板
			$scope.getTpl=function () {
			if(node==2){
				$scope.type = row.type;
				$scope.envId = row.envId;
			}
				var paramType = {
					type: $scope.type,
				};
				// console.log(paramType);
				httpLoad.loadData({
					url: "/pv/queryPVTemplate",
					data:paramType,
					method:'POST',
					success:function(data){
						if(data.success){
							$scope.modelData = data.data.parameters;

							//编辑存储时
							if(node==2){
								$scope.modalName ="编辑存储";
								$scope.modalType ="edit";
								httpLoad.loadData({
									url:'/pv/detail' ,
									method:'GET',
									data:{id:row.id},
									success:function(data){
										if(data.success){
											data.data.labels=angular.fromJson(data.data.labels);
											data.data.annotations=angular.fromJson(data.data.annotations);
											data.data.monitors=angular.fromJson(data.data.monitors);
											$scope.nodeData=data.data;
											$scope.modelData.forEach(function(item){
												if(item.type == 'Map'){
													item.value = $scope.nodeData[item.name];
													if(!item.value){
														item.value=[{key:'',value:''}];
													}
													if(item.value&&item.value.length<1){
														item.value.push({key:'',value:''})
													}
													$scope.nodeData[item.name]=item.value;
												}
												else{
													item.select = $scope.nodeData[item.name];
													$scope.nodeData[item.name]=item.select;
												}

											})
										}
									}
								});
							}
						}
					}
				})
			}
			if(node==2){
				$scope.getTpl();
			}
			$scope.addEnvs = function(map){   //添加map样子的数组
				map.push({key:'',value:''})
			}
			$scope.removeEnv = function(key){
				if(key.length == 1) return $scope.pop('请至少添加一组','error');
				key.splice(key.length-1,1);
			}
			$scope.addList = function(map){  //添加list样子的数组
				map.push('')
			}
			$scope.removeLisr = function(key){
				if(key.length == 1) return $scope.pop('请至少添加一组','error');
				key.splice(key.length-1,1);
			}

			//保存按钮
			$scope.ok = function () {
				$scope.param = {};
				if(node==2){
					$scope.param=$scope.nodeData;
				}
				var validateFlag1=1;
				$scope.modelData.forEach(function(item){     //对数据进行重组，，1，
					if(item.type =="Map"){
						var inputParametersList=[];
						for(var i=0;i<item.value.length;i++){
							var mapList = item.value[i];
							if((!mapList.key&&mapList.value)||(mapList.key&&!mapList.value)){
								$scope.pop('请添加完整的输入项key和value','error');
								validateFlag1++;
							}else if(!mapList.key&&!mapList.value){
								continue
							}else{
								inputParametersList.push({"key":mapList.key,"value":mapList.value});
								// inputParametersList[mapList.key] = mapList.value;
							}
						}
						$scope.param[item.name] = inputParametersList;
					}else{
						$scope.param[item.name] = item.select;
					}
				})

				// node.nodeData = angular.extend($scope.param,paramType);
				$scope.param.type=$scope.type;
				$scope.param.envId=$scope.envId;
				var validateFlag=1;
				angular.forEach($scope.param,function (v,k) {
					if(k=="capacity"){
						var reg1=/^[1-9]\d*$/;
						if(!reg1.test(v)){
							validateFlag++;
							$scope.pop('容量值请输入大于1的整数','error');
						}
					}else if(k=="path"){
						if(/^\/$/.test(v)){
							validateFlag++;
							$scope.pop('存储路径格式不正确','error');
						}
					}else if(($scope.param.type=="NFS")&&(k=="ip")){
						var reg2 = /^((\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5]))?$/;
						if(!reg2.test(v)){
							validateFlag++;
							$scope.pop('NFS服务地址IP格式不正确','error');
						}
					}else if(($scope.param.type=="ceph")&&(k=="monitors")){
					/*	var reg2 = /^((\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5]))?$/;
						if(!reg2.test(v)){
							validateFlag++;
							$scope.pop('监控节点格式不正确','error');
						}*/
						var keyArr=[];
						v.forEach(function (item) {
							if(keyArr.indexOf(item.key)>-1){
								validateFlag++;
							}else{
								keyArr.push(item.key);
							}
						})
						if(validateFlag!=1) $scope.pop('监控节点IP不能重复','error');
					}
				})
				if(node==2){
					$scope.param.id=row.id;
				}
           if(validateFlag==1&&validateFlag1==1){
			   var url='/pv/create';
			   if(node==2) {
				   url = '/pv/modify';
			   }
			   httpLoad.loadData({
				   url:url,
				   method:'POST',
				   data: $scope.param,
				   // noParam: true,
				   success:function(data){
					   if(data.success){
						   $scope.pop(data.message);
						   $modalInstance.close();
					   }
				   }
			   });
		   }
			}
			$scope.cancel = function () {
				$modalInstance.dismiss('cancel');
			};
		}
	]);

	//删除ctrl
	angular.module('app').controller('removeStoreModalCtrl',['$scope','$modalInstance','httpLoad','LANGUAGE','id',
		function($scope,$modalInstance,httpLoad,LANGUAGE,id){ //依赖于modalInstance
			$scope.content = '您确定要删除吗？';
			$scope.ok = function(){
				httpLoad.loadData({
					url:'/pv/remove',
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
//历史记录
	app.controller('storageHistoryModalCtrl', ['$rootScope','$timeout', '$scope','$state','httpLoad','$stateParams','$anchorScroll',
	function($rootScope,$timeout, $scope,$state,httpLoad,$stateParams,$anchorScroll) {
			$rootScope.link = '/statics/css/alarm.css';//引入页面样式
		    $scope.param = {
		    		page:1,
			        rows: 10,
			        params:angular.toJson([{"param":{"object":"pv"},"sign":"LK"}])
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
