/**
 * Created by Zhang Haijun on 2016/6/13.
 */
(function () {
	'use strict';

	angular.module('http.load', [])
		.factory('httpLoad', ['$http', '$document', '$q', '$timeout', '$rootScope', '$state', 'CONFIG', function ($http, $document, $q, $timeout, $rootScope, $state,CONFIG) {

			var deferred = $q.defer(); // 声明延后执行，表示要去监控后面的执行
			// 参数： 接口名， json或者字符串参数，成功回调，load动画， 报错提示
			return {
				loadData: function(options) {
					var params = options.data || {};
					var loadinghide = options.loadinghide || false; //默认显示loading时动画
					var ignoreError = options.ignoreError || false; //默认不忽略报错提示
					loadinghide ? '' : $rootScope.isLoadingActive = true;
		
					if(!options.noParam) params = {params: JSON.stringify(params)};
					var method = ISONLINE ? (options.method || 'POST') : 'GET';
					var config = {
							method: method,
							url: ISONLINE ? options.url : CONFIG[options.url],
							timeout: 1000*60*10,
							headers: {'Content-Type': 'application/x-www-form-urlencoded', 'BsmAjaxHeader': true}
					}
					method == 'POST' ? config.data = $.param(params) : config.params = params;
					$http(config).success(function(data, status, headers, config) {
						$timeout(function () {
							$rootScope.isLoadingActive = false;
						}, 800);
						//请求返回失败统一处理 true：调用成功方法， false: 直接抛出
						if(!data.success) {
							if(data.status == 401 || data.status == 509) 	return  $state.go('access.login');
							if(!ignoreError) $rootScope.pop(data.message,'error');
						}
						var success = options.success;
						if (success && typeof success == 'function') {
							options.success(data);
						}
						}).
						error(function(data, status, headers, config) {
							$rootScope.isLoadingActive = false;
							switch (status){
								case 404:
									$rootScope.pop('请求未找到','error');
									break;
								case 500:
									$rootScope.pop('服务异常','error');
									break;
								case 401:
									$rootScope.pop('无权限','error');
									$state.go('access.login');
									break;
								default :
									$rootScope.pop('服务器异常','error');
							}
							var error = options.error;
							if (error && typeof error == 'function')
								error(data);
						});
				}
			};
		}]);
})();