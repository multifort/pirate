angular.module('app').controller('imageUploadCtrl', ['$rootScope','$scope', '$state', 'httpLoad', 'LANGUAGE',
    function ($rootScope, $scope, $state,  httpLoad, LANGUAGE) { 
        $rootScope.moduleTitle = '服务目录 > 镜像上传';
        $scope.scriptItemSpace = {}
        $scope.scriptItemSpace.selected = "";
        //$scope.scriptItemSpace.selected.name   //镜像名称
        var params = {
            simple: true
        }
        $scope.goBack = function () {
            $state.go('paas.service_catalog.list');
        };
        $scope.websocketUrl = '/uploadService';
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
        $scope.getImage = function (id) {
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
                            $scope.imageshow = true;
                            $scope.scriptItemSpace.selected = "";
                        } else {
                            $scope.scriptItemSpace.selected = "";
                            $scope.imageshow = false;
                        }
                    }
                });
            }else{
                $scope.imageshow = false;
            }

        };
        $scope.aa={};
        $scope.cancel = function(){
            $state.go('paas.repository.dockerimage', {repositoryId: $scope.aa.warehoseItem});
        };
    }]);