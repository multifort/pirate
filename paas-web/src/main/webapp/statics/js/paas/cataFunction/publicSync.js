(function () {
	"use strict";
    angular.module('app').controller('publicSyncModalCtrl', ['$scope','$interval', 'webSocket','httpLoad', '$rootScope', '$modal', '$state', '$timeout','$location','$anchorScroll',
        function ($scope,$interval,webSocket,httpLoad, $rootScope, $modal, $state, $timeout,$location,$anchorScroll) { //依赖于modalInstance
            $rootScope.moduleTitle = '服务目录 > 公有库同步';//定义当前页

            //  websocket异步操作
            webSocket.onmessage({
                message: function (data) {
                    if ($rootScope.currentUrl == 'paas.catafunction.publicSync' && ((data.operate == '/paas/image/sycn'))) {
                        $scope.pop(data.message,data.success?'success':'error');
                        $state.go('paas.repository.dockerimage', {repositoryId: $scope.repositoryId})
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
                                $scope.listShow=false;
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
                    $scope.listShow=false;
                }

            };
            // 镜像
            $scope.imageShow=false;
            $scope.getImageList=function () {
                if($scope.repositoryId){
                    $scope.mydata.imageInfo="";
                    $scope.listShow=false;
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

            //公有库信息
            $scope.listShow=false;
            $scope.mydata={};
            $scope.getPublicList=function () {
                if($scope.mydata.imageInfo){
                    $scope.mydata.imageInfo1=angular.fromJson(angular.copy($scope.mydata.imageInfo));
                    $scope.operation.selectItem="";
                    $scope.countData1=[];
                    $scope.listShow=true;
                    $scope.searchList();
                }else{
                    $scope.listShow=false;
                }
            }
            $scope.searchList = function () {
                httpLoad.loadData({
                    url: '/paas/image/tags',
                    method: 'POST',
                    data: {imageName:$scope.mydata.imageInfo1.name},
                    noParam: true,
                    success: function (data) {
                        if (data.success) {
                            $scope.countData1 = data.data;
                            if( $scope.countData1.length<1){
                                $scope.isImageData=true;
                            }else{
                                $scope.isImageData=false;
                            }
                        }
                    }
                });
            }

            $scope.operation = {};
            $scope.choose = function(row){
                if(row.select==true){
                    $scope.countData1.forEach(function(item){
                        item.select=false;
                    });
                    row.select=true;
                    $scope.operation.selectItem=row;
                }else{
                    $scope.operation.selectItem="";
                }
            };

            $scope.goBack=function () {
                $state.go('paas.service_catalog.list');
            }

            $scope.ok = function () {
                if(($scope.projectShow)&&(!$scope.scriptItemSpace.selected.id)){
                    $scope.pop('请选择一个项目','error');
                    return false;
                }

                if(!$scope.operation.selectItem){
                  $scope.pop('请选择一条公有库镜像','error');
                }else{
                    var param = {
                        "id":$scope.mydata.imageInfo1.id,
                        "version":$scope.operation.selectItem.layer,
                        'tagName':$scope.operation.selectItem.tag||""
                    };
                    if((param.tagName)&&(!(/^[A-Za-z0-9]*$/).test(param.tagName))){
                        $scope.pop('选中的公有库镜像添加标签时，请输入数字和字母格式','error');
                        return false;
                    }
                    httpLoad.loadData({
                        url: '/paas/image/sycn',
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
            }
        }]);
})();
