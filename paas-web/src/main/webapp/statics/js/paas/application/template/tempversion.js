(function () {
    "use strict";
    app.controller('tempversionCtrl', ['$scope', 'httpLoad', '$rootScope', '$modal', '$state', '$timeout', '$location', '$anchorScroll',
        function ($scope, httpLoad, $rootScope, $modal, $state, $timeout, $location, $anchorScroll) {
            $rootScope.moduleTitle = '应用服务 > 应用编排模板 > 版本';//定义当前页
            $rootScope.link = '/statics/css/image.css';//引入页面样式
            $scope.isListView = true;
            $scope.goBacks = function () {
                $state.go('paas.application.template', { tab: "2" });
            };
            $scope.param = {
                params: JSON.stringify([{ "param": { "layoutTemplateId": $state.params.id }, "sign": "EQ" }])
            };
            //获取版本列表
            $scope.getData = function (page, type) {
                $scope.param.page = page || $scope.param.page;
                httpLoad.loadData({
                    url: '/layout/template/version/list',
                    method: 'POST',
                    data: $scope.param,
                    noParam: true,
                    success: function (data) {
                        if (data.success) {
                            $scope.countData = data.data.rows;
                            $scope.total = data.data.rows.length;
                            if (!$scope.total) {
                                $scope.isImageData = true;
                            } else {
                                $scope.isImageData = false;
                            }
                        }
                    }
                });
            };
            $scope.getData(1);
            //详情
            $scope.detail = function (id) {
                sessionStorage.setItem('layoutTemplateId', $state.params.id);
                $state.go('paas.application.templateversiondetail', { id: id });
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
                            templateUrl: '/statics/tpl/application/template/addversion.html',
                            controller: 'addmodelModalCtrl',// 初始化模态范围
                            backdrop: 'static',
                            resolve: {
                                id: function () {
                                    return id;
                                },
                                upgrade: function () {
                                    return false;
                                },
                                edit: function () {
                                    return false;
                                }
                            }
                        });
                        modalInstance.result.then(function () {
                            $scope.getData(1);
                        }, function () { });
                        break;
                    case 2:
                        //编辑
                        var modalInstance = $modal.open({
                            templateUrl: '/statics/tpl/application/template/addversion.html',
                            controller: 'addmodelModalCtrl',
                            backdrop: 'static',
                            resolve: {
                                id: function () {
                                    return id;
                                },
                                upgrade: function () {
                                    return false;
                                },
                                edit: function () {
                                    return true;
                                }
                            }
                        });
                        modalInstance.result.then(function () {
                            $scope.getData(1);
                        }, function () { });
                        break;
                    case 3:
                        //升级
                        var modalInstance = $modal.open({
                            templateUrl: '/statics/tpl/application/template/addversion.html',
                            controller: 'addmodelModalCtrl',// 初始化模态范围
                            backdrop: 'static',
                            resolve: {
                                id: function () {
                                    return id;
                                },
                                upgrade: function () {
                                    return true;
                                },
                                edit: function () {
                                    return false;
                                }
                            }
                        });
                        modalInstance.result.then(function () {
                            $scope.getData(1);
                        }, function () { });
                        break;
                    case 4:
                        //删除
                        var ids = [];
                        ids.push(id);
                        if ($event) $event.stopPropagation();
                        var modalInstance = $modal.open({
                            templateUrl: '/statics/tpl/application/template/remove.html',
                            controller: 'removemodelModalCtrl',
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
                    case 5:
                        //模板实例化
                        sessionStorage.setItem('layoutTemplateId', $state.params.id);
                        $state.go('paas.application.templateinstance', { id: id })
                        break;
                    case 6:
                        //实例模板化
                        sessionStorage.setItem('layoutTemplateId', $state.params.id);
                        $state.go('paas.application.instancetemplate', { id: id })
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
                    controller: 'removemodelModalCtrl',
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
    angular.module('app').controller('addmodelModalCtrl', ['$scope', '$state', '$modalInstance', 'LANGUAGE', 'httpLoad', 'id', 'upgrade', 'edit', '$timeout',
        function ($scope, $state, $modalInstance, LANGUAGE, httpLoad, id, upgrade, edit, $timeout) { //依赖于modalInstance
            var editObj = ['version', 'remark'];
            $scope.apply = 2;
            var url = '/layout/template/version/create';
            $scope.addData = {};
            $scope.addData.type = "yaml"
            $scope.modalName = '新增版本';
            $scope.versionContent = '';
            $scope.isEdit = false;
            //如果为升级，进行赋值
            if (upgrade) {
                $scope.modalName = '升级版本';
                httpLoad.loadData({
                    url: '/layout/template/version/upgrade',
                    method: 'POST',
                    data: { id: id },
                    success: function (data) {
                        if (data.success) {
                            var data = data.data;
                            $timeout(function () {
                                $scope.codeMirror.setValue(data);
                            }, 100);
                            for (var a in editObj) {
                                var attr = editObj[a];
                                $scope.addData[attr] = data[attr];

                            }
                        }
                    }
                });
            }
            //如果为编辑，进行赋值
            if (edit) {
                $scope.isEdit = true;
                url = '/layout/template/version/modify';
                $scope.modalName = '编辑版本';
                // $timeout(function () {
                //     $scope.codeMirror.setValue(id.fileContent);
                // }, 100);
                $scope.versionContent = id.fileContent;
                $scope.addData['version'] = id.version;
                $scope.addData['remark'] = id.remark;
            }
            $scope.ok = function () {
                var param = {};
                if (edit) {
                    param = id;
                    delete param.creatorName;
                    delete param.menderName;
                    delete param.fileContent;
                };
                param['layoutTemplateId'] = $state.params.id;
                editObj.forEach(function (attr, a) {
                    param[attr] = $scope.addData[attr];
                })

                var re = /^[0-9]+.?[0-9]*$/;
                if (re.test($scope.addData.fileName)) {
                    $scope.pop("名称中不包含数字");
                    return false
                }
                if (!edit) {
                    param.fileContent = $scope.codeMirror.getValue();
                }
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
                            if (upgrade) text = '升级成功';
                            if (edit) text = '编辑成功';
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
    angular.module('app').controller('removemodelModalCtrl', ['$scope', '$modalInstance', 'httpLoad', 'LANGUAGE', 'id',
        function ($scope, $modalInstance, httpLoad, LANGUAGE, id) { //依赖于modalInstance
            $scope.content = '是否删除？';
            $scope.ok = function () {
                httpLoad.loadData({
                    url: '/layout/template/version/remove',
                    method: 'DELETE',
                    data: { id: id },
                    success: function (data) {
                        if (data.success) {
                            $scope.pop(data.message);
                            $modalInstance.close();
                            $scope.operation.isBatch1 = true; $scope.isALl = false;
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
    app.controller('modeladdModalCtrl', ['$rootScope', '$scope', '$state', 'httpLoad', '$stateParams', function ($rootScope, $scope, $state, httpLoad, $stateParams) {
        $rootScope.link = '/statics/css/alarm.css';//引入页面样式
        $rootScope.moduleTitle = '应用服务 > 应用编排 > 模板创建文件';
        $scope.param = {
            params: angular.toJson([{ "param": { "object": "layout" }, "sign": "LK" }])
        };
        $scope.goBack = function () {
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
                        if (data.data.rows == []) {
                            $scope.pop("返回数据为空");
                        }
                    }
                }
            });
        }
        $scope.getHistory(1);
        $scope.selectModel = function () {
            $scope.param.appId = $scope.modelstackId;
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
