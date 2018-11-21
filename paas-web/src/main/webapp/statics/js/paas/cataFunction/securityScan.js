(function () {
	"use strict";
    angular.module('app').controller('securityScanCtrl', ['$scope','$interval', 'webSocket','httpLoad', '$rootScope', '$modal', '$state', '$timeout','$location','$anchorScroll',
        function ($scope,$interval,webSocket,httpLoad, $rootScope, $modal, $state, $timeout,$location,$anchorScroll) { //依赖于modalInstance
            $rootScope.moduleTitle = '服务目录 > 安全扫描';//定义当前页

            //  websocket异步操作
            webSocket.onmessage({
                message: function (data) {
                    if ($rootScope.currentUrl == 'paas.catafunction.securityScan' && ((data.operate == '/paas/image/scan'))) {
                        $scope.pop(data.message,data.success?'success':'error');
                    }
                }
            });
            //本地仓库
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
            //是否显示项目列表
            $scope.scriptItemSpace={};
            $scope.getProgectAndImage = function (id) {
                if(id){
                    httpLoad.loadData({
                        url: '/paas/image/getProjects',
                        method: 'GET',
                        data: { registryId: id },
                        noParam: true,
                        ignoreError: true,
                        success: function (data) {
                            if (data.success) {
                                $scope.imageData = data.data;
                                $scope.projectShow = true;
                                $scope.imageShow=false;
                                $scope.scriptItemSpace.selected= "";
                            } else {
                                $scope.projectShow = false;
                                $scope.getImageList();
                            }
                        }
                    });
                }else{
                    $scope.projectShow = false;
                    $scope.imageShow=false;
                }

            };
            // 镜像
            $scope.mydata={};
            $scope.imageShow=false;
            $scope.getImageList=function () {
                if($scope.repositoryId){
                    $scope.mydata.imageInfo="";
                    $scope.imageShow=true;
                    $scope.search();
                }else{
                    $scope.imageShow=false;
                }
            }
            $scope.search = function () {
                var url= '/paas/image/list';
                var params = [];
                var param2 = {
                    "repositoryId":$scope.repositoryId

                };
                params.push({param: param2, sign: 'EQ'});
                $scope.param = {
                    page: 1,
                    rows: 9999,
                    params: angular.toJson(params)
                }
                if($scope.projectShow){
                    $scope.param = {
                        "registryId":$scope.repositoryId,
                        "projectId":$scope.scriptItemSpace.selected.id
                    }
                    url= '/paas/image/getImagesByProject';
                }
                httpLoad.loadData({
                    url: url,
                    method: 'POST',
                    data: $scope.param,
                    noParam: true,
                    success: function (data) {
                        if (data.success) {
                            $scope.countData = data.data.rows;
                        }
                    }
                });
            }

            $scope.goBack=function () {
                $state.go('paas.service_catalog.list');
            }

            $scope.ok = function () {

                if(($scope.projectShow)&&(!$scope.scriptItemSpace.selected.id)){
                    $scope.pop('请选择一个项目','error');
                    return false;
                }
                $scope.mydata.imageInfo1=angular.fromJson(angular.copy($scope.mydata.imageInfo));
                    var param = {
                        "id":$scope.mydata.imageInfo1.id,
                    };
                    httpLoad.loadData({
                        url: '/paas/image/scan',
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
            $scope.cancel = function(){
                $state.go('paas.service_catalog.list');
            };
        }]);
})();
