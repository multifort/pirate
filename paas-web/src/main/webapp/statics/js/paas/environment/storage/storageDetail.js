
app.controller('storageDetailModalCtrl', ['$scope', 'httpLoad', '$rootScope','$modal','$state','$timeout','$stateParams','$sce',
    function($scope, httpLoad, $rootScope, $modal,$state, $timeout,$stateParams,$sce) {
        $rootScope.link = '/statics/css/image.css';//引入页面样式
        $rootScope.moduleTitle =$sce.trustAsHtml('环境资源 > <span>存储卷</span> > 详情');
        $scope.statusData = {1:"不可用",2:"激活状态",3:"冻结状态",4:"异常状态"};
        $scope.param = {
            page:1,
            rows: 10,
            //				params: JSON.stringify([{"param": {"type": "VMWARE"}, "sign": "EQ"}])
        };
        (function(){
            var id = $stateParams.id;
            httpLoad.loadData({
                url:'/pv/detail',
                method:'GET',
                data: {id: id},
                success:function(data){
                    if(data.success&&data.data){
                        $scope.basicDetail = data.data;
                        $scope.showDetail = $scope.isActive = true;

                    }
                }
            });
        })();
        $scope.goBack = function(){
            $state.go('paas.environment.storage');
        };
    }
]);

app.controller('environmentopenstackModalCtrl', ['$rootScope','$modal', '$scope','$state','httpLoad','LANGUAGE','$stateParams',
    function($rootScope,$modal, $scope,$state,httpLoad,LANGUAGE,$stateParams) {
        $scope.param = {
            page:1,
            rows: 10
        };
        $scope.getData = function (page) {
            var port = $stateParams.port;
            $scope.param.page = page || $scope.param.page;
            $scope.param.params=angular.toJson([{"param":{"repository_id":$stateParams.id},"sign":"EQ"}])

            httpLoad.loadData({
                url: '/registry/images',
                method: 'POST',
                data: $scope.param,
                noParam: true,
                success: function (data) {
                    if (data.success) {
                        $scope.countData = data.data.rows;
                        $scope.totalCount = data.data.total;
                    }
                }
            });
        };
    }]);
