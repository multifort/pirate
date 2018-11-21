(function () {
	"use strict";
	app.controller('allstackCtrl', ['$scope', '$state', function ($scope, $state) {
		//从模板版本页面返回时直接跳到tab第二栏
		if ($state.params.tab === '2') {
			$scope.fileActive = false;
			$scope.tempActive = true;
		} else {
			$scope.fileActive = true;
			$scope.tempActive = false;
		}
		//切换到文件页
		$scope.changeFile = function () {
			$scope.fileActive = true;
			$scope.tempActive = false;
			$scope.$broadcast('isFileActive', 'fileTab');
		}
		//切换到模板页
		$scope.changeTemp = function () {
			$scope.fileActive = false;
			$scope.tempActive = true;
			$scope.$broadcast('isTempActive', 'tempTab');
		}
	}])
	app.controller('modelstackCtrl', ['$scope', 'httpLoad', '$rootScope', '$modal', '$state', '$timeout', '$location', '$anchorScroll',
		function ($scope, httpLoad, $rootScope, $modal, $state, $timeout, $location, $anchorScroll) {
			$rootScope.moduleTitle = '应用服务 > 应用编排';//定义当前页
			$rootScope.link = '/statics/css/image.css';//引入页面样式
			$scope.isListView = true;
			$scope.param = {
				page: 1,
				rows: 10,
				//				params: JSON.stringify([{"param": {"type": "VMWARE"}, "sign": "EQ"}])
			};
			//获取云主机列表
			$scope.getData = function (page, type) {
				$scope.param.page = page || $scope.param.page;
				httpLoad.loadData({
					url: '/layout/list',
					method: 'POST',
					data: $scope.param,
					noParam: true,
					success: function (data) {
						if (data.success) {
							$scope.countData = data.data.rows;
							$scope.totalCount = data.data.total;
							if (!$scope.totalCount) {
								$scope.isImageData = true;
							} else {
								$scope.isImageData = false;
							}
						}
					}
				});
			};
			//页面加载时加载数据
			if ($scope.fileActive) $scope.getData(1);
			//点击切换时刷新数据
			$scope.$on('isFileActive', function (event, data) {
				$scope.getData(1);
			})
			//详情
			$scope.detail = function (id) {
				$state.go('paas.application.templatedetail', { id: id })

			}
			$scope.goBack = function () {
				$scope.isActive = false;
				$timeout(function () {
					$scope.showHistory = false;
				}, 200);
			};
			$scope.gohistory = function ($event) {
				//历史记录
				$event.stopPropagation();
				$scope.isActive = true;
				$scope.showHistory = true;
			};
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
				if (angular.toJson(param1).length > 2) params.push({ param: param1, sign: 'LK' });
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
			$scope.operation.isBatch1 = true; $scope.operation.isALl = false;
			$scope.selectALl = function () {
				$scope.operation.isBatch1 = !$scope.operation.isALl;
				$scope.countData.forEach(function (item) {
					item.select = $scope.operation.isALl;
				});
			}
			$scope.choose = function () {
				var a = 0, b = 0;
				$scope.countData.forEach(function (item) {
					if (item.select == true) a++;
					else b = 1;
				});
				if (a >= 1) $scope.operation.isBatch1 = false;
				else $scope.operation.isBatch1 = true;
				if (b == 1) $scope.operation.isALl = false;
				else $scope.operation.isALl = true;
			};
			//返回
			$scope.goAction = function (flag, id, $event) {
				switch (flag / 1) {
					case 1:
						//新增
						var modalInstance = $modal.open({
							templateUrl: '/statics/tpl/application/template/add.html',
							controller: 'addmodelfileModalCtrl',// 初始化模态范围
							backdrop: 'static',
							resolve: {
								id: function () {
									return id;
								},
							}
						});
						modalInstance.result.then(function () {
							$scope.getData(1);
						}, function () { });
						break;
					case 2:
						//编辑
						var modalInstance = $modal.open({
							templateUrl: '/statics/tpl/application/template/add.html',
							controller: 'addmodelfileModalCtrl',
							backdrop: 'static',
							resolve: {
								id: function () {
									return id;
								},
							}
						});
						modalInstance.result.then(function () {
							$scope.getData(1);
						}, function () { });
						break;
					case 3:
						//删除
						if ($event) $event.stopPropagation();
						var ids = [];
						ids.push(id);
						var modalInstance = $modal.open({
							templateUrl: '/statics/tpl/application/template/remove.html',
							controller: 'removemodelflieModalCtrl',
							backdrop: 'static',
							resolve: {
								id: function () {
									return ids;
								},
							}
						});
						modalInstance.result.then(function () {
							$scope.getData(1);
							$scope.isCheck = false;
						}, function () { });
						break;
					case 4:
						//部署
						$state.go('paas.application.templategridin', { id: id })
						break;
					case 5:
						//部署
						$state.go('paas.application.addmodel', { id: id })
						break;
				}
			};
			$scope.deteleAll = function ($event) {
				var ids = [];
				for (var i = 0; i < $scope.countData.length; i++) {
					var item = $scope.countData[i]
					if (item.select) {
						ids.push(item.id);
					}
				}
				var modalInstance = $modal.open({
					templateUrl: '/statics/tpl/application/template/remove.html',
					controller: 'removemodelflieModalCtrl',
					backdrop: 'static',
					resolve: {
						id: function () {
							return ids;
						},
					}
				});
				modalInstance.result.then(function () {
					$scope.getData(1);
					$scope.operation.isALl = false;
					$scope.operation.isBatch1 = true;
					angular.forEach($scope.countData, function (data, index) {
						data.select = false;
					})
				}, function () { });
			};
		}
	]);
	//新增ctrl
	angular.module('app').controller('addmodelfileModalCtrl', ['$scope', '$modalInstance', 'LANGUAGE', 'httpLoad', 'id', '$timeout',
		function ($scope, $modalInstance, LANGUAGE, httpLoad, id, $timeout) { //依赖于modalInstance
			var editObj = ['name', 'remark', 'type'];
			$scope.apply = 2;
			var url = '/layout/create';
			$scope.addData = {};
			$scope.addData.type = "yaml"
			$scope.modalName = '编排文件创建';
			//$scope.addData.type = 0;
			//如果为编辑，进行赋值
			if (id) {
				url = '/layout/modify';
				$scope.modalName = '编排文件编辑';
				httpLoad.loadData({
					url: '/layout/detail',
					method: 'GET',
					data: { id: id },
					success: function (data) {
						if (data.success) {
							var data = data.data;
							$timeout(function () {
								$scope.codeMirror.setValue(data.fileContent);
							}, 100);
							for (var a in editObj) {
								var attr = editObj[a];
								$scope.addData[attr] = data[attr];
							}
						}
					}
				});
			}
			$scope.ok = function () {
				var param = {};
				editObj.forEach(function (attr, a) {
					param[attr] = $scope.addData[attr];
				})

				if (id) param.id = id;
				var re = /^[0-9]+.?[0-9]*$/;
				if (re.test($scope.addData.fileName)) {
					$scope.pop("名称中不包含数字");
					return false
				}
				param.fileName = $('#getNameFile').val()
				param.fileContent = $scope.codeMirror.getValue();
				if (param.fileContent == '') {
					$scope.pop("脚本内容不能为空", "error");
					return false
				}
				httpLoad.loadData({
					url: url,
					method: 'POST',
					data: param,
					success: function (data) {
						if (data.success) {
							var text = '添加成功';
							if (id) text = '编辑成功';
							$scope.pop(text);
							$modalInstance.close();
						}
					}
				});
			}
			$scope.cancel = function () {
				$modalInstance.dismiss('cancel');
			};
		}]);
	//删除ctrl
	angular.module('app').controller('removemodelflieModalCtrl', ['$scope', '$modalInstance', 'httpLoad', 'LANGUAGE', 'id',
		function ($scope, $modalInstance, httpLoad, LANGUAGE, id) { //依赖于modalInstance
			$scope.content = '是否删除？';
			$scope.ok = function () {
				httpLoad.loadData({
					url: '/layout/remove',
					method: 'POST',
					data: { id: id },
					success: function (data) {
						if (data.success) {
							//console.log(removeData);
							$scope.pop(LANGUAGE.MONITOR.APP_MESS.DEL_SUCCESS);
							$modalInstance.close();
							angular.forEach($scope.countData, function (data, index) {
								data.select = false;

							})
						}
					}
				});
			};
			$scope.cancel = function () {
				$modalInstance.dismiss('cancel');
			}
		}]);
	app.controller('modelhistoryModalCtrl', ['$rootScope', '$scope', '$state', 'httpLoad', '$stateParams', function ($rootScope, $scope, $state, httpLoad, $stateParams) {
		$rootScope.link = '/statics/css/alarm.css';//引入页面样式

		$scope.param = {
			page: 1,
			rows: 10,
			params: angular.toJson([{ "param": { "object": "layout" }, "sign": "LK" }])
		};
		$scope.getHistory = function (page) {
			$scope.param.page = page || $scope.param.page
			httpLoad.loadData({
				url: '/app/log/list',
				method: 'POST',
				data: $scope.param,
				noParam: true,
				success: function (data) {
					if (data.success && data.data) {
						$scope.userList = data.data.rows;
						$scope.totalCount = data.data.total;
						if (data.data.rows == []) {
							$scope.pop("返回数据为空");
						}
					}
				}
			});
		}
		$scope.getHistory(1)
	}]);





	// 新增应用编排模板tab页面Controller
	app.controller('templatemodelstackCtrl', ['$scope', 'httpLoad', '$rootScope', '$modal', '$state', '$timeout', '$location', '$anchorScroll',
		function ($scope, httpLoad, $rootScope, $modal, $state, $timeout, $location, $anchorScroll) {
			$rootScope.moduleTitle = '应用服务 > 应用编排';//定义当前页
			$rootScope.link = '/statics/css/image.css';//引入页面样式
			$scope.isListView = true;
			/*$scope.param = {
				page: 1,
				rows: 10,
				//				params: JSON.stringify([{"param": {"type": "VMWARE"}, "sign": "EQ"}])
			};*/

			//获取编排模板列表
			$scope.getData = function (page, type) {
				// $scope.param.page = page || $scope.param.page;
				httpLoad.loadData({
					url: '/layout/template/list',
					method: 'POST',
					data: $scope.param,
					noParam: true,
					success: function (data) {
						if (data.success) {
							$scope.listData = data.data.rows;
							$scope.total= data.data.rows.length;
							if (!$scope.total) {
								$scope.isImageData = true;
							} else {
								$scope.isImageData = false;
							}
						}
					}
				});
			};
			//页面加载时加载数据
			if ($scope.tempActive) $scope.getData();
			//点击切换时刷新数据
			$scope.$on('isTempActive', function (event, data) {
				$scope.getData(1);
			})
			//详情
			$scope.detail = function (id) {
				$state.go('paas.application.templateversion', { id: id })
			}
			$scope.goBack = function () {
				$scope.isActive = false;
				$timeout(function () {
					$scope.showHistory = false;
				}, 200);
			};
			$scope.gohistory = function ($event) {
				//历史记录
				$event.stopPropagation();
				$scope.isActive = true;
				$scope.showHistory = true;
			};
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
				if (angular.toJson(param1).length > 2) params.push({ param: param1, sign: 'LK' });
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
			$scope.operation.isBatch1 = true; $scope.operation.isALl = false;
			$scope.selectALl = function () {
				$scope.operation.isBatch1 = !$scope.operation.isALl;
				$scope.listData.forEach(function (item) {
					item.select = $scope.operation.isALl;
				});
			}
			$scope.choose = function () {
				var a = 0, b = 0;
				$scope.listData.forEach(function (item) {
					if (item.select == true) a++;
					else b = 1;
				});
				if (a >= 1) $scope.operation.isBatch1 = false;
				else $scope.operation.isBatch1 = true;
				if (b == 1) $scope.operation.isALl = false;
				else $scope.operation.isALl = true;
			};
			//返回
			$scope.goAction = function (flag, row, $event) {
				switch (flag / 1) {
					case 1:
						//新增
						var modalInstance = $modal.open({
							templateUrl: '/statics/tpl/application/template/addtemplate.html',
							controller: 'addmodeltemplateModalCtrl',// 初始化模态范围
							backdrop: 'static',
							resolve: {
								row: function () {
									return row;
								},
							}
						});
						modalInstance.result.then(function () {
							$scope.getData(1);
						}, function () { });
						break;
					case 2:
						//编辑
						var modalInstance = $modal.open({
							templateUrl: '/statics/tpl/application/template/addtemplate.html',
							controller: 'addmodeltemplateModalCtrl',
							backdrop: 'static',
							resolve: {
								row: function () {
									return row;
								},
							}
						});
						modalInstance.result.then(function () {
							$scope.getData(1);
						}, function () { });
						break;
					case 3:
						//删除
						if ($event) $event.stopPropagation();
						var ids = [];
						ids.push(row.id);
						var modalInstance = $modal.open({
							templateUrl: '/statics/tpl/application/template/remove.html',
							controller: 'removetemplatemodelModalCtrl',
							backdrop: 'static',
							resolve: {
								id: function () {
									return ids;
								},
							}
						});
						modalInstance.result.then(function () {
							$scope.getData(1);
							$scope.isCheck = false;
						}, function () { });
						break;
				}
			};
			$scope.deteleAll = function ($event) {
				var ids = [];
				for (var i = 0; i < $scope.listData.length; i++) {
					var item = $scope.listData[i]
					if (item.select) {
						ids.push(item.id);
					}
				}
				var modalInstance = $modal.open({
					templateUrl: '/statics/tpl/application/template/remove.html',
					controller: 'removetemplatemodelModalCtrl',
					backdrop: 'static',
					resolve: {
						id: function () {
							return ids;
						},
					}
				});
				modalInstance.result.then(function () {
					$scope.getData(1);
					$scope.operation.isALl = false;
					$scope.operation.isBatch1 = true;
					angular.forEach($scope.listData, function (data, index) {
						data.select = false;
					})
				}, function () { });
			};
		}
	]);
	//新增,编辑应用编排模板ctrl
	angular.module('app').controller('addmodeltemplateModalCtrl', ['$scope', '$modalInstance', 'LANGUAGE', 'httpLoad', '$timeout', 'row',
		function ($scope, $modalInstance, LANGUAGE, httpLoad, $timeout, row) { //依赖于modalInstance
			var editObj = ['name', 'remark'];
			$scope.apply = 2;
			var url = '/layout/template/create';
			$scope.addData = {};
			$scope.modalName = '编排模板创建';
			//$scope.addData.type = 0;
			//如果为编辑，进行赋值
			if (row) {
				url = '/layout/template/modify';
				$scope.modalName = '编排模板编辑';
				for (var a in editObj) {
					var attr = editObj[a];
					$scope.addData[attr] = row[attr];
				}
			}
			$scope.ok = function () {
				var param = {};
				if (row) {
					for (var key in row) {
						param[key] = row[key];
					}
				}
				editObj.forEach(function (attr, a) {
					param[attr] = $scope.addData[attr];
				})
				httpLoad.loadData({
					url: url,
					method: 'POST',
					data: param,
					success: function (data) {
						if (data.success) {
							var text = '添加成功';
							if (row) text = '编辑成功';
							$scope.pop(text);
							$modalInstance.close();
						}
					}
				});
			}
			$scope.cancel = function () {
				$modalInstance.dismiss('cancel');
			};
		}]);
	//删除应用编排模板ctrl
	angular.module('app').controller('removetemplatemodelModalCtrl', ['$scope', '$modalInstance', 'httpLoad', 'LANGUAGE', 'id',
		function ($scope, $modalInstance, httpLoad, LANGUAGE, id) { //依赖于modalInstance
			$scope.content = '是否删除？';
			$scope.ok = function () {
				httpLoad.loadData({
					url: '/layout/template/remove',
					method: 'DELETE',
					data: { id: id },
					success: function (data) {
						if (data.success) {
							$scope.pop(LANGUAGE.MONITOR.APP_MESS.DEL_SUCCESS);
							$modalInstance.close();
							angular.forEach($scope.listData, function (data, index) {
								data.select = false;
							})
						}
					}
				});
			};
			$scope.cancel = function () {
				$modalInstance.dismiss('cancel');
			}
		}]);


	// 应用服务 > 应用编排 > 模板创建文件
	app.controller('modeladdModalCtrl', ['$rootScope', '$scope','$state','httpLoad','$stateParams',function($rootScope, $scope,$state,httpLoad,$stateParams) {
		$rootScope.link = '/statics/css/alarm.css';//引入页面样式
		$rootScope.moduleTitle = '应用服务 > 应用编排 > 模板创建文件';
		$scope.param = {
			params:angular.toJson([{"param":{"object":"layout"},"sign":"LK"}])
		};
		$scope.goBack = function(){
			$state.go('paas.application.template');
		};
		var params = {
			simple: true
		}
		$scope.getHistory = function (page) {
			httpLoad.loadData({
				url: '/layout/list',
				method: 'POST',
				data: params,
				noParam: true,
				success: function (data) {
					if (data.success) {
						$scope.countData = data.data.rows;
						$scope.totalCount = data.data.total;
						if(data.data.rows==[]){
							$scope.pop("返回数据为空");
						}
					}

				}
			});
		}
		$scope.getHistory(1);
		$scope.selectModel = function () {
			$scope.param.appId=$scope.modelstackId;
			httpLoad.loadData({
				url: '/layout/modellist',
				method: 'POST',
				data: $scope.param,
				noParam: true,
				success: function (data) {
					if (data.success) {
						$scope.modelData = data.data;

					}
				}
			});
		}
		$scope.ok = function () {
			$scope.param = $scope.modelData;
			httpLoad.loadData({
				url: '/layout/modellist',
				method: 'POST',
				data: $scope.param,
				noParam: true,
				success: function (data) {
					if (data.success) {
						$state.go('paas.application.template');

					}
				}
			});
		}

	}]);

})();
