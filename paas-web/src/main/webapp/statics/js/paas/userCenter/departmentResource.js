(function(){
	"use strict";
	app.controller('departmentResourceCtrl', ['$scope', 'httpLoad', '$rootScope','$modal','$state', '$stateParams','LANGUAGE',
		function($scope, httpLoad, $rootScope, $modal, $state, $stateParams, LANGUAGE) {
			$rootScope.moduleTitle = '系统管理 > 组织机构';//定义当前页
			$rootScope.link = '/statics/css/user.css';//引入页面样式
			$scope.itemsByPage = 20;//定义每页的条数

			//加载服务器列表
			var loadData = function(){
				httpLoad.loadData({
					url:'/department/res',
					method:'POST',
					data:{
						id:$stateParams.id
					},
					success:function(data){
						if(data.success){
							$scope.serverListData= data.data;
							$scope.total = data.data.length;
							angular.forEach($scope.serverListData, function(data,index){
								if(data.status=='RUNNING') data.status1='运行中';
								else if(data.status=='STOPPED') data.status1='停止';
								else if(data.status=='NORMAL') data.status1='正常';
								else if(data.status=='ABNORMAL') data.status1='不正常';
								else if(data.status=='EXCEPTION') data.status1='异常';
								else data.status1='异常';
							});
						}
					}
				});
			};
			loadData();
			//选择服务器
			$scope.selectServer = function(){
				var modalInstance = $modal.open({
					templateUrl: '/statics/tpl/userCenter/department/bindModal.html',
					controller: 'bindDepartmentModalCtrl',
					backdrop: 'static',
					size:'lg'
				});
				modalInstance.result.then(function (data) {
					loadData();
				});
			};
			$scope.goBack = function(){
				$state.go('paas.userCenter.department');
			};
			//删除
			$scope.remove = function (id,$event){
				if($event) $event.stopPropagation();
				var removeData = {"id":$stateParams.id,"resId":id};
				var modalInstance = $modal.open({
					templateUrl: '/statics/tpl/userCenter/department/remove.html',
					controller: 'removeDepartmentModalCtrl',
					backdrop: 'static',
					resolve: {
						removeData: function() {
							return  removeData;
						}
					}
				});
				modalInstance.result.then(function() {
					loadData();
				});
			};
		}
	]);
	//删除
	angular.module('app').controller('removeDepartmentModalCtrl', ['$scope', '$modalInstance', 'httpLoad', 'removeData',
		function($scope, $modalInstance, httpLoad, removeData) {
			$scope.content= '是否删除分配资源？';
			$scope.ok = function(){
				httpLoad.loadData({
					url: '/department/deleteRes',
					data: removeData,
					success: function(data){
						if(data.success){
							$scope.pop("分配资源删除成功");
							$modalInstance.close();
						}
					}
				});
			}
			$scope.cancel = function(){
				$modalInstance.dismiss('cancel');
			};
		}
	]);
	//新增绑定
	app.controller('bindDepartmentModalCtrl', ['$scope','httpLoad','$modalInstance','$stateParams',
		function($scope, httpLoad, $modalInstance, $stateParams) {
			$scope.param = {
				page: 1,
				rows: 8,
				target: $stateParams.id,
				category : "DEPT"
			};

			$scope.regionData = [{"value":"Computer","name":"计算资源","isRegionActive":false},{"value":"Storage","name":"存储资源","isRegionActive":false},{"value":"Network","name":"网络资源","isRegionActive":false},{"value":"DB","name":"数据库","isRegionActive":false},{"value":"Middleware","name":"中间件","isRegionActive":false},{"value":"LB","name":"负载","isRegionActive":false},{"value":"Switch","name":"交换机","isRegionActive":false},{"value":"Router","name":"路由器","isRegionActive":false},{"value":"Firewall","name":"防火墙","isRegionActive":false}];
			$scope.regionData2 = [{"value":"PHYSICAL","name":"物理资源","isRegionActive":false},{"value":"LOGICAL","name":"虚拟资源","isRegionActive":false}];
			//选择
			$scope.chooseRegion = function(item,action){
				var dataList;
				if(action==0) {
					dataList = $scope.regionData;
					$scope.category = "";
				}
				if(action==1) {
					dataList = $scope.regionData2;
					$scope.catalog = "";
				}
				angular.forEach(dataList, function(data,index){
					if(data.value==item.value){
						data.isRegionActive = !item.isRegionActive;
					}else data.isRegionActive = false;
					if(data.isRegionActive){
						if(action==0) $scope.category = data.value;
						if(action==1) $scope.catalog = data.value;
					}
				});
			};

			var selectList = [];
			$scope.getList = function(page){
				$scope.param.page = page || $scope.param.page;
				$scope.isSelectAll = false;
				//获取已选主机的IPlist
				var ipList = [];
				for(var a in selectList){
					ipList.push(selectList[a].ip);
				}

				var searchParam = [];
				if($scope.ip&&$scope.ip!=""){
					searchParam.push({"param":{"ip":$scope.ip},"sign":"LK"});
				}
				if($scope.name&&$scope.name!=""){
					if(searchParam.length==0) searchParam.push({"param":{"name":$scope.name},"sign":"LK"});
					else {
						var a = 0;
						for(var i=0;i<searchParam.length;i++){
							if(searchParam[i].sign=="LK"){
								a = 1;
								searchParam[i].param.name = $scope.name;
							}
						}
						if(a==0) searchParam.push({"param":{"name":$scope.name},"sign":"LK"});
					}
				}
				if($scope.category&&$scope.category!=""){
					searchParam.push({"param":{"category":$scope.category},"sign":"EQ"});
				}
				if($scope.catalog&&$scope.catalog!=""){
					if(searchParam.length==0) searchParam.push({"param":{"catalog":$scope.catalog},"sign":"EQ"});
					else {
						var a = 0;
						for(var i=0;i<searchParam.length;i++){
							if(searchParam[i].sign=="EQ") {
								a = 1;
								searchParam[i].param.catalog = $scope.catalog;
							}
						}
						if(a==0) searchParam.push({"param":{"catalog":$scope.catalog},"sign":"EQ"});
					}
				}
				$scope.param.params = JSON.stringify(searchParam);
				httpLoad.loadData({
					url:'/res/list',
					method:'POST',
					data:$scope.param,
					noParam: true,
					success:function(data){
						if(data.success){
							$scope.listData = data.data.rows;
							angular.forEach($scope.listData, function(data,index){
								if(data.status=='RUNNING') data.status1='运行中';
								else if(data.status=='STOPPED') data.status1='停止';
								else if(data.status=='NORMAL') data.status1='正常';
								else if(data.status=='ABNORMAL') data.status1='不正常';
								else if(data.status=='EXCEPTION') data.status1='异常';
								else data.status1='异常';
							});
							//数据反显
							for(var i = 0; i < $scope.listData.length; i++){
								var item = $scope.listData[i];
								var flag = ipList.indexOf(item.ip);
								if(flag > -1) $scope.listData[i] = selectList[flag];
							}
							$scope.totalPage = data.data.total;
						}
					}
				});
			};
			$scope.getList();
			//对选择的数据进行操作
			var setSelectList = function(data){
				if(data.isSelected){
					selectList.push(data);
				}else{
					for(var j = 0; j < selectList.length; j++){
						var item = selectList[j];
						if(item.ip == data.ip) selectList.splice(j,1)
					}
				}
			};
			//全选
			$scope.selectAll = function(){
				for(var a in $scope.listData){
					var item = $scope.listData[a];
					if($scope.isSelectAll != item.isSelected){
						item.isSelected = $scope.isSelectAll;
						setSelectList(item);
					}else item.isSelected = $scope.isSelectAll;
				}
			};
			//选择一个
			$scope.selectItem = function ($event,row) {
				$event.stopPropagation();
				setSelectList(row);
			};

			$scope.ok = function () {
				var resources = [];
				selectList.forEach(function (item) {
					resources.push({resId:item.id});
				});

				httpLoad.loadData({
					url:'/department/assign',
					data:{
						id:$stateParams.id,
						resources:resources
					},
					success: function(data){
						if(data.success){
							$scope.pop("资源分配添加成功");
							$modalInstance.close(data);
						}
					}
				});
			};
			$scope.cancel = function () {
				$modalInstance.dismiss('cancel');
			};
		}
	]);
})()

