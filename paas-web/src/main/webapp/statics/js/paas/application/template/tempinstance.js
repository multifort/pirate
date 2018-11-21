(function () {
    "use strict";
    app.controller('tempinstanceCtrl', ['$scope', 'httpLoad', '$rootScope', '$modal', '$state', '$timeout', '$location', '$anchorScroll',
        function ($scope, httpLoad, $rootScope, $modal, $state, $timeout, $location, $anchorScroll) {
            $rootScope.moduleTitle = '应用服务 > 应用编排模板 > 版本 > 模板实例化';
            $scope.param = {
                id: $state.params.id
            };
            //获取版本详情数据
            $scope.getData = function () {
                httpLoad.loadData({
                    url: '/layout/template/version/detail',
                    method: 'GET',
                    data: $scope.param,
                    success: function (data) {
                        if (data.success) {
                            $scope.versionData = data.data;
                            $scope.modelData = JSON.parse($scope.versionData.fileDescContent).parameters;
                        }
                    }
                });
            };
            $scope.getData();
            //返回版本页面
            $scope.goBacks = function () {
                let layoutTemplateId = sessionStorage.getItem('layoutTemplateId');
                $state.go('paas.application.templateversion',{id: layoutTemplateId});
            }

            $scope.addEnvs = function (map) {   //添加map样子的数组
                map.push({ key: '', value: '' })
            }
            $scope.removeEnv = function (key) {
                if (key.length == 1) return $scope.pop('请至少添加一组', 'error');
                key.splice(key.length - 1, 1);
            }
            $scope.addList = function (map) {  //添加list样子的数组
                map.push('')
            }
            $scope.removeLisr = function (key) {
                if (key.length == 1) return $scope.pop('请至少添加一组', 'error');
                key.splice(key.length - 1, 1);
            }

            //保存按钮
            $scope.ok = function () {
                $scope.param = {};
                var validateFlag1 = 1;
                $scope.modelData.forEach(function (item) {     //对数据进行重组，，1，
                    if (item.type == "Map") {
                        var inputParametersList = [];
                        for (var i = 0; i < item.value.length; i++) {
                            var mapList = item.value[i];
                            if ((!mapList.key && mapList.value) || (mapList.key && !mapList.value)) {
                                $scope.pop('请添加完整的输入项key和value', 'error');
                                validateFlag1++;
                            } else if (!mapList.key && !mapList.value) {
                                continue
                            } else {
                                inputParametersList.push({ "key": mapList.key, "value": mapList.value });
                            }
                        }
                        $scope.param[item.name] = inputParametersList;
                    } else {
                        $scope.param[item.name] = item.select;
                    }
                })
                $scope.param.type = $scope.type;
                $scope.param.envId = $scope.envId;
                var validateFlag = 1;
                angular.forEach($scope.param, function (v, k) {
                    if (k == "capacity") {
                        var reg1 = /^[1-9]\d*$/;
                        if (!reg1.test(v)) {
                            validateFlag++;
                            $scope.pop('容量值请输入大于1的整数', 'error');
                        }
                    } else if (k == "path") {
                        if (/^\/$/.test(v)) {
                            validateFlag++;
                            $scope.pop('存储路径格式不正确', 'error');
                        }
                    } else if (($scope.param.type == "NFS") && (k == "ip")) {
                        var reg2 = /^((\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5]))?$/;
                        if (!reg2.test(v)) {
                            validateFlag++;
                            $scope.pop('NFS服务地址IP格式不正确', 'error');
                        }
                    }
                })
                if (validateFlag == 1 && validateFlag1 == 1) {
                    var url = '/layout/template/version/instantiation';
                    //var url = '/layout/modellist';
                    $scope.data = {
                        id: $state.params.id,
                        params: JSON.stringify($scope.param)
                    }
                    httpLoad.loadData({
                        url: url,
                        method: 'POST',
                        data: $scope.data,
                        noParam: true,
                        success: function (data) {
                            if (data.success) {
                                $scope.fileData = data.data;
                                $scope.pop(data.message);
                                var modalInstance = $modal.open({
                                    templateUrl: '/statics/tpl/application/template/generatefile.html',
                                    controller: 'generateFileCtrl',
                                    backdrop: 'static',
                                    resolve: {
                                        fileData: function () {
                                            return $scope.fileData;
                                        },
                                    }
                                });
                            }
                        }
                    });
                }
            }
            $scope.cancel = function () {

            };
        }
    ]);


    //生成文件Ctrl
    angular.module('app').controller('generateFileCtrl', ['$scope', '$state', '$modalInstance', 'httpLoad', 'LANGUAGE', 'fileData',
        function ($scope, $state, $modalInstance, httpLoad, LANGUAGE, fileData) { //依赖于modalInstance
            $scope.modalName = '编辑';
            var editObj = ['name', 'remark'];
            $scope.addData = {};
            $scope.fileData = fileData;
            $scope.ok = function () {
                var param = {};
				editObj.forEach(function (attr, a) {
					param[attr] = $scope.addData[attr];
                })
                param['type'] = 'yaml';
                param['fileName'] = '';
                param['fileContent'] = $scope.fileData;
                httpLoad.loadData({
                    url: '/layout/create',
                    method: 'POST',
                    data: param,
                    success: function (data) {
                        if (data.success) {
                            var text = '成功';
                            $scope.pop(text);
                            $modalInstance.close();
                            $state.go('paas.application.template');
                        }
                    }
                });
            };
            $scope.cancel = function () {
                $modalInstance.dismiss('cancel');
            }
        }]);
})();
