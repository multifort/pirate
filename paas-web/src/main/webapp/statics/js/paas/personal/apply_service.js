/**
 * Created by Zhang Haijun on 2016/8/31.
 */
(function(){
	//我的申请
	app.controller('PersonalVmApplyListCtrl', ['$rootScope', '$scope', 'httpLoad', '$state', '$timeout',  '$modal', function($rootScope, $scope, httpLoad, $state, $timeout, $modal) {
	$rootScope.link = '/statics/css/user.css';//引入页面样式
		 $rootScope.moduleTitle = '个人中心 > 申请服务';
	$scope.param = {
		rows: 10,
		page:1
	};
	$scope.getData = function (page) {
		$scope.param.page = page || $scope.param.page;
		$scope.param.params = angular.toJson([{param:{ownerId:$rootScope.userData.id},sign:'EQ'}])
		httpLoad.loadData({
			url: '/applyRecord/list',
			method: 'POST',
			data: $scope.param,
			noParam: true,
			success: function (data) {
				if (data.success) {
					$scope.vmList = data.data.rows;
					$scope.totalCount = data.data.total;
					if (data.data.rows.length == 0) {
						$scope.isImageData = true;
					} else $scope.isImageData = false;
				} else {
					$scope.isImageData = true;
				}
			}
		});
	};
	//返回
	$scope.goBack = function () {
		$scope.isActive = false;
		$timeout(function () {
			$scope.showDetail = false;
		}, 200);
	};
		//对参数进行处理，去除空参数
		var toObjFormat = function(obj) {
			for (var a in obj) {
				if (obj[a] == "") delete obj[a];
			}
			return obj;
		}
		$scope.search = function(){
			var params = [];
			var param1 = toObjFormat({
				name: $scope.name,
				
			});
			var param2 = toObjFormat({
				ownerId:$rootScope.userData.id
			});
			if (angular.toJson(param1).length > 2) params.push({param: param1, sign: 'LK'});
			if (angular.toJson(param2).length > 2) params.push({param: param2, sign: 'EQ'});
			$scope.param = {
				page: 1,
				rows: 10,
				params: angular.toJson(params)
			}
			$scope.getData()
		};
		$scope.apply = function (id) {
			var modalInstance = $modal.open({
				templateUrl: '/statics/tpl/personal/applyservice/applyModal.html',
				controller: 'vmApplyModalCtrl',
				backdrop: 'static',
				resolve: {
					id: function() {
						return id;
					},
					type: function() {
						return 'task';
					}
				}
			});
			modalInstance.result.then(function(data) {
				$scope.getData();
			});
		};
	//跳转详情页
	$scope.detail = function ($event, row) {
		$event.stopPropagation();
		$scope.showDetail = $scope.isActive = true;
		$scope.detailData = angular.fromJson(row.params);
		$scope.detailData.type = row.catalog;
		if($scope.detailData.type=='OPENSTACK') $scope.getDetail();
	};
	//openstack详情
		(function(){
			$scope.updateData = {};$scope.account = 'root';
			//获取可用区域数据
			var getRegion = function(regionList){
				httpLoad.loadData({
					url:'/region/list',
					method: 'POST',
					data: {"id":$scope.detailData.id},
					success:function(data){
						if(data.success){
							$scope.regionData = data.data;
							angular.forEach($scope.regionData, function(data,index){
								data.isRegionActive = false;
								for(var a in regionList){
									if(regionList[a]==data.id) data.isRegionActive = true;
								}
							});
						}
					}
				});
			};
			//获取镜像数据
		var getImageData = function(){
				var params = {
						simple : true
					},
					searchParam = [{"param":{"vendorId":$scope.detailData.id,"sign":"EQ"}}];
				params.params = JSON.stringify(searchParam);
				httpLoad.loadData({
					url:'/image/list',
					method:'POST',
					data: params,
					noParam: true,
					success:function(data){
						if(data.success){
							$scope.imageList = data.data.rows;
						}
					}
				});
			};
			//获取网络数据
			var getNetworkData = function(networkId){
				var params = {
						simple : true
					},
					searchParam = [{"param":{"vendorId":$scope.detailData.id},"sign":"EQ"}];
				params.params = JSON.stringify(searchParam);
				httpLoad.loadData({
					url:'/network/list',
					method:'POST',
					data: params,
					noParam: true,
					success:function(data){
						if(data.success){
							$scope.networkList = data.data.rows;
							if((networkId+"").indexOf(",") != -1){
								var netData = networkId.split(',');
								angular.forEach($scope.networkList, function(data,index){
									data.isNetworkActive = false;
									angular.forEach(netData, function(data1,index1){
										if(data.id==data1) data.isNetworkActive = true;
									});
								});
							}else{
								angular.forEach($scope.networkList, function(data,index){
									data.isNetworkActive = false;
									if(data.id==networkId) data.isNetworkActive = true;
								});
							}
						}
					}
				});
			};
			//获取配置实例数据
			var getSizeData = function(){
				var params = {
						simple : true
					},
					searchParam = [{"param":{"vendorId":$scope.detailData.id},"sign":"EQ"}];
				params.params = JSON.stringify(searchParam);
				httpLoad.loadData({
					url:'/flavor/list',
					method: 'POST',
					data: params,
					noParam: true,
					success:function(data){
						if(data.success){
							$scope.sizeList = data.data.rows;
							$scope.sizeList.forEach(function (item) {
								if(item.id == $scope.updateData.flavorId){
									selectSize(item);
								}
							})
						}
					}
				});
			};
			//选择配置实例
			var selectSize = function(row){
				if(row==undefined) return;
				if(row!=""){
					var data = angular.fromJson(row);
					$scope.detailData.disk = data.disk+'';
					$scope.isselectSize = true;
					//选择CPU核数和内存大小
					$scope.slider = {
						value : data.cpu,
						options: {
							showTicks: true,
							readOnly: true,
							stepsArray : [
								{value:"1"},
								{value:"2"},
								{value:"4"},
								{value:"8"},
								{value:"16"}
							],
							translate: function(value) {
								return value+'核';
							}
						}
					};
					$scope.slider1 = {
						value : data.memory/1024,
						options: {
							showTicks: true,
							readOnly: true,
							stepsArray : [
								{value: 0.5},
								{value: 2},
								{value: 4},
								{value: 6},
								{value: 8},
								{value: 16}
							],
							translate: function(value) {
								return value+'G';
							}
						}
					};
					$scope.sliderDone = true;
					}
			};
			//获取详情数据反显
			$scope.getDetail = function(){
				var updateItem = ['name','region','remark','id','flavorId','networkId','imageId','password'];
				for(var a in updateItem){
					var item = updateItem[a];
					$scope.updateData[item] = $scope.detailData[item];
				}
				var regionList = [];
				$scope.updateData.region = $scope.updateData.region +"";
				if($scope.updateData.region.indexOf()<0)  regionList.push($scope.updateData.region);
				else  regionList = $scope.updateData.region.split(',');
				//获取可用区域数据
				getRegion(regionList);
				//获取镜像数据
				getImageData();
				//获取网络数据
				getNetworkData($scope.detailData.networkId);
				//获取配置实例数据
				getSizeData();
			};
		})();
		//新增
		$scope.add = function () {
			var modalInstance = $modal.open({
				templateUrl: '/statics/tpl/personal/applyservice/platformModal.html',
				controller: 'selectPlatformModalCtrl',
				backdrop: 'static'
			});
			modalInstance.result.then(function (data) {
				if(data.type == 'VMWARE'){
					$state.go('paas.personal.vmadd',{id:data.id,type:data.type});
				}else {
					$state.go('paas.personal.vmadd',{id:data.id,type:data.type});
				}
			});
		};
}]);
	//选择平台modal
	app.controller('selectPlatformModalCtrl',['$rootScope','$scope','$modalInstance','httpLoad',
		function($rootScope,$scope,$modalInstance,httpLoad){
		var instanceId;
		$scope.platformData = [{name:'VMWARE'},{name:'OPENSTACK'}];
			//获取区域数据
			$scope.getInstance = function(){
				httpLoad.loadData({
					url: '/cloudVendor/list',
					method: 'POST',
					data: {
						page: 1,
						rows: 10000,
						params: JSON.stringify([{"param": {"type": $scope.type}, "sign": "EQ"}]),
					},
					noParam: true,
					success: function (data) {
						if (data.success) {
							$scope.instanceData = data.data.rows;
							instanceId = '';
							if($scope.instanceData.length>0){
								$scope.selectInstance($scope.instanceData[0]);
							}
						}
					}
				});
			};
			$scope.selectPlatform = function(item){
				if(item.active) return;
				angular.forEach($scope.platformData, function(data,index){
					data.active = false;
				});
				item.active = true;$scope.type = item.name;
				$scope.getInstance();
			};
			$scope.selectPlatform($scope.platformData[0]);
			$scope.selectInstance = function (item) {
				if(item.active) return;
				angular.forEach($scope.instanceData, function(data,index){
					data.active = false;
				});
				item.active = true;
				instanceId = item.id;
			}
			$scope.ok = function(){
				$modalInstance.close({id:instanceId,type:$scope.type});
			};
			$scope.cancel = function(){
				$modalInstance.dismiss('cancel'); // 退出
			};
		}]);
	//审批
	app.controller('vmApplyModalCtrl', ['$scope', '$modalInstance', 'httpLoad', 'id', 'type',
		function($scope, $modalInstance,  httpLoad, id,type) {
			$scope.userList = [{id:''}];
			//获取用户数据
			httpLoad.loadData({
				url: '/applyRecord/applyUser',
				noParam: true,
				success: function(data){
					if(data.success){
						$scope.userListData = data.data;
					}
				}
			});
			$scope.addGroup = function(){
				$scope.userList.push({id:''})
			}
			$scope.removeGroup = function(key){
				if($scope.userList.length == 1) return $scope.pop('请至少添加一个审批人','error');
				$scope.userList.splice(key,1);
			}
			//保存按钮
			$scope.ok = function(){
				//组合用户数据
				var approvers = '',userobj = {};
				$scope.userList.forEach(function (item) {
					if(userobj[item.id]) return;
					userobj[item.id] = true;
					approvers += item.id + ',';
				});
				httpLoad.loadData({
					url:'/applyRecord/apply',
					data: {
						id:id,
						content:$scope.content,
						approvers:approvers.substring(0,approvers.length-1)
					},
					success: function(data){
						if(data.success){
							$scope.pop('审批提交成功');
							$modalInstance.close(data);
						}
					}
				});
			}
			$scope.cancle = function() {
				$modalInstance.dismiss('cancel');
			};
		}
	]);
	app.controller('PersonalVmListCtrl', ['$rootScope', '$scope', 'httpLoad', '$state', '$timeout',  '$modal', function($rootScope, $scope, httpLoad, $state, $timeout, $modal) {
		$rootScope.moduleTitle = '个人中心 > 申请服务';
		$rootScope.link = '/statics/css/user.css';//引入页面样式
		$scope.param = {
			rows: 10
		};
		//获取云主机列表
		$scope.getData = function (page) {
			$scope.showDetail = false;
			$scope.param.page = page || $scope.param.page;
			var params = {
					page: $scope.param.page,
					rows: $scope.param.rows
				},
				searchParam = [{"param": {"createrId": $rootScope.userData.id, "isTemplate": false}, "sign": "EQ"}];
			if ($scope.searchByName && $scope.searchByName != "") {
				searchParam.push({"param": {"name": $scope.searchByName}, "sign": "LK"});
			}
			if ($scope.searchByStatus && $scope.searchByStatus != "") {
				var a = 0;
				for (var i = 0; i < searchParam.length; i++) {
					if (searchParam[i].sign == "LK") {
						a = 1;
						searchParam[i].param.status = $scope.searchByStatus;
					}
				}
				if (a == 0) searchParam.push({"param": {"status": $scope.searchByStatus}, "sign": "LK"});
			}
			params.params = JSON.stringify(searchParam);
			httpLoad.loadData({
				url: '/vm/list',
				method: 'POST',
				data: params,
				noParam: true,
				success: function (data) {
					if (data.success && data.data.rows) {
						$scope.vmList = data.data.rows;
						$scope.totalCount = data.data.total;
						if (data.data.rows.length == 0) {
							$scope.isImageData = true;
							return;
						} else $scope.isImageData = false;
						angular.forEach($scope.vmList, function (data, index) {
							//状态
							data.isShowRecover = data.isRecover = data.isShowPaused = data.isPaused = data.isShowSuspended = data.isSuspended = data.isShowActive = data.isActive = data.isShowStart = data.isStart = data.isShowStop = data.isStop = false;
							switch (data.status) {
								case 'RUNNING':
									data.isShowStop = true;
									data.isStop = false;
									data.isShowRestart = true;
									data.isRestart = false;
									data.isShowStart = false;
									data.isShowPaused = true;
									data.isPaused = false;
									data.isShowSuspended = true;
									data.isSuspended = false;
									data.statusColor = 'success';
									break;
								case 'STOPPED':
									data.isShowStop = false;
									data.isShowRestart = true;
									data.isRestart = true;
									data.isShowStart = true;
									data.isStart = false;
									data.isShowRecover = true;
									data.isRecover = false;
									data.isShowActive = true;
									data.isActive = false;
									data.statusColor = 'warning';
									break;
								case 'STOPPING':
									data.isShowStop = false;
									data.isShowRestart = true;
									data.isRestart = true;
									data.isShowStart = true;
									data.isStart = true;
									data.isShowRecover = true;
									data.isRecover = true;
									data.isShowActive = true;
									data.isActive = true;
									data.statusColor = 'default';
									break;
								case 'STARTING':
									data.isShowStop = true;
									data.isStop = true;
									data.isShowRestart = true;
									data.isRestart = false;
									data.isShowStart = false;
									data.isShowPaused = true;
									data.isPaused = true;
									data.isShowSuspended = true;
									data.isSuspended = true;
									data.statusColor = 'default';
									break;
								case 'SUSPENDED':
									data.isShowStop = false;
									data.isShowRestart = true;
									data.isRestart = true;
									data.isShowStart = false;
									data.isShowActive = true;
									data.isActive = false;
									data.statusColor = 'primary';
									break;
								case 'SUSPENDING':
									data.isShowStop = false;
									data.isShowRestart = true;
									data.isRestart = true;
									data.isShowStart = false;
									data.isShowActive = true;
									data.isActive = true;
									data.statusColor = 'default';
									break;
								case 'ACTIVING':
									data.isShowStop = true;
									data.isStop = true;
									data.isShowRestart = true;
									data.isRestart = false;
									data.isShowStart = false;
									data.isShowPaused = true;
									data.isPaused = true;
									data.isShowSuspended = true;
									data.isSuspended = true;
									data.statusColor = 'default';
									break;
								case 'PAUSED':
									data.isShowStop = false;
									data.isShowRestart = true;
									data.isRestart = true;
									data.isShowStart = false;
									data.isShowRecover = true;
									data.isRecover = false;
									data.statusColor = 'warning';
									break;
								case 'PAUSING':
									data.isShowStop = false;
									data.isShowRestart = true;
									data.isRestart = true;
									data.isShowStart = false;
									data.isShowRecover = true;
									data.isRecover = true;
									data.statusColor = 'default';
									break;
								case 'RECOVERING':
									data.isShowStop = false;
									data.isShowRestart = true;
									data.isRestart = true;
									data.isShowStart = false;
									data.isShowPaused = true;
									data.isPaused = true;
									data.isShowSuspended = true;
									data.isSuspended = true;
									data.statusColor = 'default';
									break;
								case 'PAUSING':
									data.isShowStop = true;
									data.isStop = true;
									data.isShowRestart = true;
									data.isRestart = false;
									data.isShowStart = false;
									data.statusColor = 'default';
									break;
								case 'BUILDING':
									data.isShowStop = false;
									data.isShowRestart = true;
									data.isRestart = true;
									data.isShowStart = true;
									data.isStart = true;
									data.isShowRecover = true;
									data.isRecover = true;
									data.isShowActive = true;
									data.isActive = true;
									data.statusColor = 'default';
									break;
								case 'RESTARTING':
									data.isShowStop = false;
									data.isShowRestart = true;
									data.isRestart = true;
									data.isShowStart = false;
									data.isShowPaused = true;
									data.isPaused = true;
									data.isShowSuspended = true;
									data.isSuspended = true;
									data.statusColor = 'default';
									break;
								case 'EXPIRED':
									data.isShowStop = false;
									data.isShowRestart = true;
									data.isRestart = true;
									data.isShowStart = true;
									data.isStart = true;
									data.isShowRecover = true;
									data.isRecover = true;
									data.isShowActive = true;
									data.isActive = true;
									data.statusColor = 'default';
									break;
								case 'EXCEPTION':
									data.isShowStop = false;
									data.isShowRestart = true;
									data.isRestart = true;
									data.isShowStart = true;
									data.isStart = true;
									data.isShowRecover = true;
									data.isRecover = true;
									data.isShowActive = true;
									data.isActive = true;
									data.statusColor = 'danger';
									break;
								case 'SYNSEXCEPTION':
									data.isShowStop = false;
									data.isShowRestart = true;
									data.isRestart = true;
									data.isShowStart = true;
									data.isStart = true;
									data.isShowRecover = true;
									data.isRecover = true;
									data.isShowActive = true;
									data.isActive = true;
									data.statusColor = 'danger';
									break;
							}
						});
					} else {
						$scope.isImageData = true;
					}
				}
			});
		};
		//状态数据
		$scope.statusData = [{"value": "RUNNING", "name": "运行中"}, {
			"value": "STOPPED",
			"name": "关机"
		}, {"value": "SUSPENDED", "name": "挂起"}, {"value": "PAUSED", "name": "停止"}, {"value": "EXCEPTION", "name": "异常"}];
		//返回
		$scope.goBack = function () {
			$scope.isActive = false;
			$timeout(function () {
				$scope.showDetail = false;
			}, 200);
		};
		$scope.paramd = {
			page: 1,
			rows: 10
		};
		//详情操作记录
		$scope.getOperateData = function (page, id) {
			$scope.paramd.page = page || $scope.paramd.page;
			$scope.paramd.params = JSON.stringify([{"param": {"vmId": id || $scope.vmDetail.id}, "sign": "EQ"}]);
			httpLoad.loadData({
				url: '/res/event/list',
				method: 'POST',
				data: $scope.paramd,
				noParam: true,
				success: function (data) {
					if (data.success) {
						if (data.data) {
							$scope.eventList = data.data.rows;
							$scope.totalCountd = data.data.total;
							$scope.isImageDatad = false;
						}
					} else {
						$scope.isImageDatad = true;
					}
				}
			});
		};
		//跳转详情页
		$scope.detail = function ($event, id) {
			$event.stopPropagation();
			httpLoad.loadData({
				url: '/vm/detail',
				method: 'GET',
				data: {id: id},
				success: function (data) {
					if (data.success && data.data) {
						$scope.vmDetail = data.data;
						$scope.showDetail = $scope.isActive = true;
						//状态处理
						switch ($scope.vmDetail.status) {
							case 'RUNNING':
								$scope.vmDetail.statusColor = "success";
								break;
							case 'STOPPING':
							case 'STARTING':
							case 'SUSPENDING':
							case 'ACTIVING':
							case 'RECOVERING':
							case 'PAUSING':
							case 'RESTARTING':
							case 'BUILDING':
								$scope.vmDetail.statusColor = "default";
								break;
							case 'SUSPENDED':
								$scope.vmDetail.statusColor = "primary";
								break;
							case 'PAUSED':
							case 'STOPPED':
								$scope.vmDetail.statusColor = "warning";
								break;
							case 'EXCEPTION':
							case 'SYNSEXCEPTION':
								$scope.vmDetail.statusColor = "danger";
								break;
						}
						
						//密码处理
						var password = '';
						if ($scope.vmDetail.password) {
							for (var i = 0; i < $scope.vmDetail.password.length; i++) {
								password += '*';
							}
							$scope.vmDetail.password = password;
						}
					}
				}
			});
			//获取列表数据
			$scope.getOperateData(1, id);
		};
		//新增
		$scope.add = function () {
			$state.go('app.config.vmwareAdd', {id: VmWareData.platformId});
		};
	}]);
	
	//申请详情
	app.controller('approvalDetailModalCtrl',['$scope','$modalInstance','httpLoad','id',
		function($scope,$modalInstance,httpLoad,id){
			httpLoad.loadData({
				url: '/apply/detail',
				method:'GET',
				data: {
					id:id
				},
				success: function(data){
					$scope.detailData = data.data;
					$scope.userList = data.data.approvers;
				}
			});
			$scope.close = function() {
				$modalInstance.close();
			};
		}]);
	app.controller('PersonalApplyCtrl', ['$rootScope', '$scope', 'httpLoad', '$state', '$modal', function($rootScope, $scope, httpLoad, $state,$modal) {
		$rootScope.moduleTitle = '个人中心 > 申请服务';
		$rootScope.link = '/statics/css/personal.css';//引入页面样式
		$scope.itemsByPage = 10;
		$scope.getList = function(){
			httpLoad.loadData({
				url: '/apply/deal',
				noParam:true,
				success: function(data){
					$scope.listData = data.data;
					$scope.total = $scope.listData.length;
				}
			});
		};
		$scope.check = function (id) {
			var modalInstance = $modal.open({
				templateUrl: '/statics/tpl/personal/approval/checkModal.html',
				controller: 'approvalCheckModalCtrl',
				backdrop: 'static',
				resolve: {
					id: function() {
						return id;
					}
				}
			});
			modalInstance.result.then(function(data) {
				$scope.getList();
			});
		};
		//详情
		$scope.goDetail = function(id){
			var modalInstance = $modal.open({
				templateUrl: '/statics/tpl/config/managescript/detailModal.html',
				controller: 'detailScriptModalCtrl',
				backdrop: 'static',
				size:'lg',
				keyboard:false,
				resolve: {
					id: function () {
						return id;
					}
				}
			});
		};
	}]);
	//详情ctrl
	app.controller('detailScriptModalCtrl', ['$scope', '$modalInstance', 'httpLoad', '$timeout','id',
		function ($scope, $modalInstance, httpLoad, $timeout, id) {
			(function(){
				httpLoad.loadData({
					url:'/script/detail',
					method:'GET',
					data:{
						id:id
					},
					success:function(data){
						if(data.success){
							data.data.open =  data.data.open.toString();
							$scope.detailData = data.data;
							$timeout(function () {
								$scope.codeMirror.options.readOnly = true;
								$scope.codeMirror.setValue(data.data.content);
							},11);
						}
					}
				});
			})();
			$scope.cancle = function () {
				$modalInstance.dismiss('cancel');
			};
		}
	]);
	app.controller('PersonalApprovaledCtrl', ['$rootScope', '$scope', 'httpLoad', '$state', '$modal', function($rootScope, $scope, httpLoad, $state,$modal) {
		$rootScope.moduleTitle = '个人中心 > 我的审批';
		$rootScope.link = '/statics/css/personal.css';//引入页面样式
		$scope.param = {
			page: 1,
			rows: 10
		};
		$scope.getList = function(page){
			$scope.param.page = page || $scope.param.page;
			httpLoad.loadData({
				url: '/apply/done',
				data: $scope.param,
				noParam:true,
				success: function(data){
					$scope.listData = data.data.rows;
					$scope.totalPage = data.data.total;
				}
			});
		};
		//对参数进行处理，去除空参数
		var toObjFormat = function(obj) {
			for (var a in obj) {
				if (obj[a] == "") delete obj[a];
			}
			return obj;
		}
		$scope.detail = function (id) {
			var modalInstance = $modal.open({
				templateUrl: '/statics/tpl/personal/approval/detailModal.html',
				controller: 'approvalDetailModalCtrl',
				backdrop: 'static',
				resolve: {
					id: function() {
						return id;
					}
				}
			});
			modalInstance.result.then(function(data) {
			});
		};
		//搜索
		$scope.search = function(){
			//对时间进行处理
			var toFormatTime = function(time, place) {
				if (!time) return "";
				var date = time.split(' - ');
				return date[place/1];
			}
			var params = [];
			var param1 = toObjFormat({
				name: $scope.name,
				
			});
			// var param2 = toObjFormat({
			// 	kind: 'BACKUP'
			// });
			if (angular.toJson(param1).length > 2) params.push({param: param1, sign: 'LK'});
			// if (angular.toJson(param2).length > 2) params.push({param: param2, sign: 'EQ'});
			if ($scope.date) {
				params.push({param: {gmtCreate: toFormatTime($scope.date, 0)}, sign: 'GET'});
				params.push({param: {gmtCreate: toFormatTime($scope.date, 1)}, sign: 'LET'});
			}
			$scope.param = {
				page: 1,
				rows: 10,
				params: angular.toJson(params)
			}
			$scope.getList()
		}
		//重置搜索条件
		$scope.reset = function(){
			var obj = ['name'];
			angular.forEach(obj,function(data){
				$scope[data] = '';
			})
		}
	}]);
	app.controller('approvalDetailModalCtrl',['$scope','$modalInstance','httpLoad','id',
		function($scope,$modalInstance,httpLoad,id){
			httpLoad.loadData({
				url: '/apply/detail',
				method:'GET',
				data: {
					id:id
				},
				success: function(data){
					$scope.detailData = data.data;
					$scope.userList = data.data.approvers;
				}
			});
			$scope.close = function() {
				$modalInstance.close();
			};
		}]);
	//在线编辑指令
	app.directive('ngCodeMirrors', ['$timeout', function ($timeout) {
		return {
			restrict: 'EA',
			scope: {
				codeMirror: '='
			},
			link: function (scope, element, attrs) {
				var editor = $(element).find('.textarea')[0];
				$timeout(function () {
					scope.codeMirror = CodeMirror.fromTextArea(editor, {
						theme: 'erlang-dark',
						mode: 'shell',
						lineNumbers: true,
						readOnly: false,
						extraKeys: {
							"F11": function (cm) {
								cm.setOption("fullScreen", !cm.getOption("fullScreen"));
							},
							"Esc": function (cm) {
								if (cm.getOption("fullScreen")) cm.setOption("fullScreen", false);
							}
						}
					});
					//全屏展示
					$(element).find('.icon-size-fullscreen').on('click', function () {
						var escTip = $('<div style="z-index:10000;font-size:16px;color:#f05050;position: fixed;top: 10px;left:50%;text-align: center;opacity: 1;font-weigth:bold;background-color: #e7fff8;padding:5px;width:400px;margin-left:-200px;">您现在处于全屏模式，按ESC键可以退出全屏！</div>');
						$(document.body).append(escTip);
						escTip.animate({
							opacity: '0'
						}, 5000, function () {
							escTip.remove();
						});
						scope.codeMirror.setOption("fullScreen", true);
					});
				}, 10);
			}
		}
	}]);
	app.controller('approvalCheckModalCtrl',['$scope','$modalInstance','httpLoad','id',
		function($scope,$modalInstance,httpLoad,id){
			var recordId;
			httpLoad.loadData({
				url: '/apply/approveDetail',
				method:'GET',
				data: {
					id:id
				},
				success: function(data){
					$scope.detailData = data.data;
					$scope.userList = data.data.approvers;
					recordId = $scope.userList[$scope.userList.length-1].id;
				}
			});
	
			$scope.approve = function () {
				httpLoad.loadData({
					url: '/apply/approve',
					data: {
						id:recordId,
						applyId:id,
						reason:$scope.reason
					},
					success: function(data){
					if(data.success){
						$scope.pop('申请通过成功');
						$modalInstance.close()
					}
					}
				});
			};
			$scope.refuse = function () {
				httpLoad.loadData({
					url: '/apply/refuse',
					data: {
						id:recordId,
						applyId:id,
						reason:$scope.reason
					},
					success: function(data){
						if(data.success){
							$scope.pop('申请拒绝成功');
							$modalInstance.close()
						}
					}
				});
			};
			$scope.close = function() {
				$modalInstance.dismiss('cancel');
			};
		}]);
})();