(function () {
	"use strict";
    angular.module('app').controller('imageSyncModalCtrl', ['$scope','$interval', 'webSocket','httpLoad', '$rootScope', '$modal', '$state', '$timeout','$location','$anchorScroll',
        function ($scope,$interval,webSocket,httpLoad, $rootScope, $modal, $state, $timeout,$location,$anchorScroll) { //依赖于modalInstance
            $rootScope.moduleTitle = '服务目录 > 镜像同步';//定义当前页

            //  websocket异步操作
            webSocket.onmessage({
                message: function (data) {
                    if ($rootScope.currentUrl == 'paas.catafunction.imageSync' && ((data.operate == '/paas/registry/sycn'))) {
                        $scope.pop(data.message,data.success?'success':'error');
                        $state.go('paas.repository.dockerimage', {repositoryId: $scope.repositoryId})
                    }
                }
            });
            
            var params = {
                simple: true
            }
            httpLoad.loadData({
                url: '/paas/registry/list',
                method: 'POST',
                data: params,
                noParam: true,
                success: function (data) {
                    if (data.success) {
                        $scope.warehoseData = data.data.rows;

                    }
                }
            });
            $scope.goBack=function () {
                $state.go('paas.service_catalog.list');
            }
            $scope.ok = function () {
                var param = {ids:$scope.repositoryId};
                httpLoad.loadData({
                    url: '/paas/registry/sycn',
                    method: 'POST',
                    data: param,
                    //  noParam: true,
                    success: function (data) {
                        if (data.success) {
                            $scope.pop(data.message);
                        } else {

                        }
                    }
                });
            }
        }]);
})();
