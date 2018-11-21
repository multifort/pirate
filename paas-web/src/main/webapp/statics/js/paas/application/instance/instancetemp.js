(function () {
    "use strict";
    app.controller('instancetempCtrl', ['$scope', 'httpLoad','$stateParams', '$rootScope', '$modal', '$state', '$timeout', '$location', '$anchorScroll',
        function ($scope, httpLoad, $stateParams,$rootScope, $modal, $state, $timeout, $location, $anchorScroll) {
            $rootScope.moduleTitle = '我的应用 > 模板 > 实例模板化';
            $scope.param = {
                id: $state.params.id
            };
            //返回版本页面
            $scope.goBacks = function () {
                var layoutTemplateId = sessionStorage.getItem('layoutTemplateId');
                history.go(-1);
            };
            //获取模板数据
                //$scope.fileContent = "apiVersion: v1 \nkind: PersistentVolume \nmetadata:\n annotations:\n <%\nfor(label in annotations){\n%>\n ${label.value}\n<% } %>\n labels:\n <%\nfor(label in labels){\n%>\n ${label.value}\n<% } %>\n name: ${name}\nspec:\n capacity:\n storage: ${capacity}Gi\n accessModes:\n - ${access}\n persistentVolumeReclaimPolicy: ${policy}\n hostPath:\n path: ${path}\n\n\n \n";
             httpLoad.loadData({
                url: '/application/templatable',
                method: 'POST',
                data: {"id":$stateParams.id,name:$stateParams.name,namespace:$stateParams.namespace},
                success: function (data) {
                    if (data.success) {
                        $scope.fileContent = data.data;                       
                    }
                }
            });
            //实例化
            $scope.mode = '';
            $scope.doTemp = function () {
                if ($scope.mode === "execute") {
                    var modalInstance = $modal.open({
                        templateUrl: '/statics/tpl/application/template/generatefile.html',
                        controller: 'instancetempexeCtrl',
                        backdrop: 'static',
                        resolve: {
                            fileContent: function () {
                                return $scope.fileContent;
                            },
                        }
                    });
                } else if ($scope.mode === "template") {
                    var x = 5;
                    $state.go('paas.application.instancetemplatetemp', { file: $scope.fileContent });
                }
            }
        }
    ]);
    //可执行文件方式Ctrl
    app.controller('instancetempexeCtrl', ['$scope', 'httpLoad', '$rootScope', '$modalInstance', '$state', '$timeout', '$location', 'fileContent',
        function ($scope, httpLoad, $rootScope, $modalInstance, $state, $timeout, $location, fileContent) {
            $scope.modalName = '生成编排文件';
            var editObj = ['name', 'remark'];
            $scope.addData = {};
            $scope.fileData = fileContent;
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
        }
    ]);




})();
