/**
 * Created by Zhang Haijun on 2016/8/31.
 */
(function(){
	//申请详情
	app.controller('approvalDetailModalCtrl',['$scope','$modalInstance','httpLoad','id',
		function($scope,$modalInstance,httpLoad,id){
			httpLoad.loadData({
				url: '/applyRecord/detail',
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
	//待审批
	app.controller('PersonalApprovalCtrl', ['$rootScope', '$scope', 'httpLoad', '$state', '$modal','$timeout', function($rootScope, $scope, httpLoad, $state,$modal, $timeout) {
		$rootScope.moduleTitle = '个人中心 > 我的审批';
		$rootScope.link = '/statics/css/user.css';//引入页面样式
		$scope.itemsByPage = 10;
		$scope.getList = function(){
			httpLoad.loadData({
				url: '/applyRecord/deal',
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
		$scope.goBack = function () {
			$scope.isActive = false;
			$timeout(function () {
				$scope.showDetail = false;
			}, 200);
		};
	}]);
	//已审批
	app.controller('PersonalApprovaledCtrl', ['$rootScope', '$scope', 'httpLoad', '$state', '$modal', function($rootScope, $scope, httpLoad, $state,$modal) {
		$rootScope.moduleTitle = '个人中心 > 我的审批';
		$rootScope.link = '/statics/css/user.css';//引入页面样式
		$scope.param = {
			page: 1,
			rows: 10
		};
		$scope.getList = function(page){
			$scope.param.page = page || $scope.param.page;
			httpLoad.loadData({
				url: '/applyRecord/done',
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
		};
	}]);
	app.controller('approvalDetailModalCtrl',['$scope','$modalInstance','httpLoad','id',
		function($scope,$modalInstance,httpLoad,id){
			httpLoad.loadData({
				url: '/applyRecord/detail',
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
	app.controller('approvalCheckModalCtrl',['$scope','$modalInstance','httpLoad','id',
		function($scope,$modalInstance,httpLoad,id){
			var recordId;
			httpLoad.loadData({
				url: '/applyRecord/approveDetail',
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
					url: '/applyRecord/approve',
					data: {
						nodeId:recordId,
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
					url: '/applyRecord/refuse',
					data: {
						nodeId:recordId,
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