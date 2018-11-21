/**
 * Created by Zhang Haijun on 2016/10/27.
 */
(function(){
	"use strict";
	app.controller('PersonalMessageCtrl', ['$scope', 'httpLoad', '$rootScope','$modal','$timeout','$state','LANGUAGE','CommonData',
		function($scope, httpLoad, $rootScope, $modal, $timeout, $state, LANGUAGE, CommonData) {
			$rootScope.moduleTitle = '个人中心 > 我的消息';//定义当前页
			$rootScope.link = '/statics/css/personal.css';
			$scope.statusData = CommonData.messageStatus;
			$scope.param = {
				page: 1,
				rows: 10
			};
			$scope.getList = function(page){
				$scope.param.page = page || $scope.param.page;
				httpLoad.loadData({
					url:'/message/list',
					method:'POST',
					noParam: true,
					data:$scope.param,
					success:function(data){
						if(data.success){
							$scope.listData = data.data.rows;
							$scope.totalPage = data.data.total;
						}
					}
				});
			}
			$scope.getList();
			//对参数进行处理，去除空参数
			var toObjFormat = function(obj) {
				for (var a in obj) {
					if (obj[a] == "") delete obj[a];
				}
				return obj;
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
					name:$scope.name,
				});
				var param2 = toObjFormat({
					status: $scope.status
				});
				if (angular.toJson(param1).length > 2) params.push({param: param1, sign: 'LK'});
				if (angular.toJson(param2).length > 2) params.push({param: param2, sign: 'EQ'});
				if ($scope.date) {
					params.push({param: {gmtCreate: toFormatTime($scope.date, 0)}, sign: 'GET'});
					params.push({param: {gmtCreate: toFormatTime($scope.date, 1)}, sign: 'LET'});
				}
				$scope.param = {
					page: 1,
					rows: 10,
					params: angular.toJson(params)
				}
				$scope.getList(1)
			}
			//重置搜索条件
			$scope.reset = function(){
				var obj = ['name','status','date'];
				angular.forEach(obj,function(data){
					$scope[data] = '';
				})
			};
			$scope.remove = function(id){
				var modalInstance = $modal.open({
					templateUrl: '/statics/tpl/operation/template/delModal.html',
					controller: 'delModalCtrl',
					backdrop: 'static',
					resolve:{
						tip: function () {
							return '你要确认该消息吗？';
						},
						btnList: function () {
							return  [{name:'删除',type:'btn-danger'},{name:'取消',type:'btn-default'}];
						}
					}
				});
				modalInstance.result.then(function() {
					httpLoad.loadData({
						url: '/message/remove',
						data: {id:id},
						success: function(data){
							if(data.success){
								$scope.pop(LANGUAGE.PERSONAL.MESSAGE.DEL_SUCCESS);
								$scope.getList();
							}
						}
					});
				});
			};
			//详情
			$scope.goDetail = function(row){
				var modalInstance  = $modal.open({
					templateUrl: '/statics/tpl/personal/message/detailModal.html',
					controller: 'messageDetailModalCtrl',
					backdrop: 'static',
					resolve:{
						id: function () {
							return row.id;
						}
					}
				});
				modalInstance.result.then(function() {
					row.status = 'READ';
				});
			}
		}
	]);
//详情
	app.controller('messageDetailModalCtrl', ['$scope', '$modalInstance', 'httpLoad', 'id',
		function($scope, $modalInstance, httpLoad, id) {
			httpLoad.loadData({
				url: '/message/detail',
				method:'GET',
				data: {id:id},
				success: function(data){
					if(data.success){
						$scope.content = data.data.content;
					}
				}
			});
			$scope.ok = function() {
				$modalInstance.close();
			};
		}
	]);
})()

