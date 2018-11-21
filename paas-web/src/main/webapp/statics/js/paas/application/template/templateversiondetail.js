(function () {
    "use strict";
    app.controller('tempverdetailCtrl', ['$scope', 'httpLoad', '$rootScope', '$modal', '$state', '$timeout', '$location', '$anchorScroll',
        function ($scope, httpLoad, $rootScope, $modal, $state, $timeout, $location, $anchorScroll) {
            $rootScope.moduleTitle = '应用服务 > 应用编排模板 > 版本 > 详情';
            $scope.param = {
                id: $state.params.id
            };
            //格式化时间
            function formatDate(now) {
                var year = now.getFullYear();
                var month = now.getMonth() + 1;
                if (month < 10) month = '0' + month;
                var date = now.getDate();
                if (date < 10) date = '0' + date;
                var hour = now.getHours();
                if (hour < 10) hour = '0' + hour;
                var minute = now.getMinutes();
                if (minute < 10) minute = '0' + minute;
                var second = now.getSeconds();
                if (second < 10) second = '0' + second;
                return year + "-" + month + "-" + date + "   " + hour + ":" + minute + ":" + second;
            }
            //获取版本详情数据
            $scope.getData = function () {
                httpLoad.loadData({
                    url: '/layout/template/version/detail',
                    method: 'GET',
                    data: $scope.param,
                    success: function (data) {
                        if (data.success) {
                            $scope.versionData = data.data;
                            $scope.versionData.createTime = formatDate(new Date($scope.versionData.gmtCreate));
                            $scope.versionData.modifyTime = formatDate(new Date($scope.versionData.gmtModify));
                        }
                    }
                });
            };
            $scope.getData();
            //返回版本页面
            $scope.goBacks = function () {
                let layoutTemplateId = sessionStorage.getItem('layoutTemplateId');
                $state.go('paas.application.templateversion', { id: layoutTemplateId });
            }
        }
    ]);
})();
