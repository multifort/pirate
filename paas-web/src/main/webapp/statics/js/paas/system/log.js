(function(){
	app.controller('logCtrl', ['$rootScope', '$scope', 'httpLoad', function($rootScope, $scope, httpLoad) {
		$rootScope.moduleTitle = '系统运维 > 日志中心';
		$scope.param = {
			page: 1,
			rows: 10
		};
		$scope.getList = function(page){
			$scope.param.page = page || $scope.param.page;
			httpLoad.loadData({
				url: '/log/list',
				data: $scope.param,
				noParam: true,
				success: function(data){
					if(data.success){
						$scope.listData = data.data.rows;
						$scope.totalPage = data.data.total;
						if ($scope.listData.length == 0) {
							$scope.isImageData = true;
							return;
						} else $scope.isImageData = false;
					}
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
		//搜索
		$scope.condition={};
		$scope.search = function(){
			//对时间进行处理
			var toFormatTime = function(time, place) {
				if (!time) return "";
				var date = time.split(' - ');
				return date[place/1];
			}
			var params = [];
			var param1 = toObjFormat({
				requestIp: $scope.condition.requestIp,
				responseIp: $scope.condition.responseIp
			});
			if (angular.toJson(param1).length > 2) params.push({param: param1, sign: 'LK'});
			if ($scope.condition.date) {
				params.push({param: {gmtCreate: toFormatTime($scope.condition.date, 0)}, sign: 'GET'});
				params.push({param: {gmtCreate: toFormatTime($scope.condition.date, 1)}, sign: 'LET'});
			}
			$scope.param = {
				page: 1,
				rows: 10,
				params: angular.toJson(params)
			}
			$scope.getList()
		}
	}]);
	app.controller('eslogCtrl', ['$rootScope', '$scope', 'httpLoad', function($rootScope, $scope, httpLoad) {
		$rootScope.link = '/statics/css/alarm.css';//引入页面样式
		$scope.param = {
			page: 1,
			rows: 10
		};
		$scope.getList = function(page){
			$scope.param.page = page || $scope.param.page;
			//获取应用日志列表
			httpLoad.loadData({
				url: '/eslog/list',
				method: 'POST',
				data: $scope.param,
				noParam: true,
				success: function(data){
					if(data.success){
						$scope.eslogList = data.data.rows;
						$scope.totalCount = data.data.total;
						if($scope.eslogList==[]){
							$scope.pop("应用日志返回数据为空");
						}else if($scope.eslogList.length>=1){
							$scope.eslogList.forEach(function (item) {
								for(k in item){
									if(k=="@timestamp"){
										item.timestamp=item[k];
									}
								}
							})
						}
						if ($scope.eslogList.length == 0) {
							$scope.isImageData = true;
							return;
						} else $scope.isImageData = false;
					}
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
		//搜索
		$scope.searchArr={};
		$scope.search = function(){
			$scope.param = {
				page: 1,
				rows: 10,
				params: angular.toJson(toObjFormat($scope.searchArr))
			}
			$scope.getList()
		}
	}]);
	app.controller('sysLogCtrl', ['$rootScope', '$scope', 'httpLoad','$timeout', function($rootScope, $scope, httpLoad,$timeout) {
		$scope.sysLogListA = []
		$scope.sysLogListB = [];
		$scope.totalPageB = $scope.totalPageA =''
		$scope.getList = function(page){
			$scope.param.page = page || $scope.param.page;
			httpLoad.loadData({
				url: '/eslog/sysLogList',
				method: 'POST',
				noParam: true,
				success: function(data){
					if(data.success){
						$scope.sysLogListA = data.data;
						$scope.totalPageA = data.data.length;
						$scope.sysLogList = $scope.sysLogListA.concat($scope.sysLogListB);
						$scope.totalPage = $scope.totalPageA + $scope.totalPageB
					}
				}
			});
			httpLoad.loadData({
				url: '/eslog/system',
				method: 'POST',
				noParam: true,
				success: function(data){
					if(data.success){
						$scope.sysLogListB = data.data;
						$scope.sysLogList = $scope.sysLogListA.concat($scope.sysLogListB);
						$scope.totalPageB = data.data.length;
						$scope.totalPage = $scope.totalPageA + $scope.totalPageB
					}
				}
			});
		
			
		};
		$scope.isActive=false;
		$scope.detailShow=false;
		$scope.goDetail=function (file,filePath,$event) {
			$event.stopPropagation();
			$scope.isActive=true;
			$scope.detailShow=true;
			$scope.filePath=filePath;
			$scope.file=file;
			$scope.getLog();
		}
		$scope.getLog = function () {
			httpLoad.loadData({
				url: '/eslog/sysLogDetail',
				method: 'POST',
				data: {"filePath":$scope.filePath},
				success: function (data) {
					if (data.data == null) {
						$scope.logDetail = data.data;
					} else {
						$scope.logDetail = data.data.replace(/\n/g, '<br>');
					}
				}
			});
		}
		$scope.goBack = function () {
			$scope.isActive = false;
			$timeout(function() {
				$scope.detailShow = false;
			}, 200);
		};
	}]);
})();
