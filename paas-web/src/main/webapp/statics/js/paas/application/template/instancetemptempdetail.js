(function () {
    "use strict";
    app.controller('instancetempTempDetailCtrl', ['$scope', 'httpLoad', '$rootScope', '$modal', '$state', '$stateParams', '$timeout', '$location', '$anchorScroll',
        function ($scope, httpLoad, $rootScope, $modal, $state, $stateParams, $timeout, $location, $anchorScroll) {
            $rootScope.moduleTitle = '我的应用 > 模板 > 实例模板化(模板文件方式)';
            //返回版本页面
            $scope.goBacks = function () {
                $state.go('paas.application.templateversion');
            };
             //设置初始值
             $scope.fileContent = $stateParams.filetemp+'\n\n\n'+$stateParams.filedesc;
             $scope.layoutTemplateId = '';
             $scope.version = '';
             $scope.remark = '';
             $scope.layoutTemplateArr = ''; 
            //获取模板数据
            httpLoad.loadData({
                url: '/layout/template/getList',
                method: 'GET',
                data: {},
                success: function (data) {
                    if (data.success) {
                        $scope.layoutTemplateArr = data.data;                       
                    }
                }
            });
            //生成版本          
            $scope.generateVersion = function () {
                let param = {};
                param["layoutTemplateId"]=$scope.layoutTemplateId;
                param["version"]=$scope.version;
                param["remark"]=$scope.remark;
                param["fileContent"]=$scope.fileContent;
                httpLoad.loadData({
                    url: '/layout/template/version/create',
                    method: 'POST',
                    data: param,
                    success: function (data) {
                        if (data.success) {
                            var text = '添加成功';
                            $scope.pop(text);
                            $state.go('paas.application.templateversion',{id:$scope.layoutTemplateId})
                        }
                    }
                });
            }
        }
    ]);
})();
