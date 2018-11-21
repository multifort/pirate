angular.module('app').controller('tenantUserCtrl', ['$rootScope', '$scope','$state','httpLoad','$stateParams',function($rootScope, $scope,$state,httpLoad,$stateParams) {
    $rootScope.link = '/statics/css/user.css';//引入页面样式
    $rootScope.moduleTitle = '系统管理 > 租户管理';
    $scope.param = {
        rows: 10
    };
    (function(){
        var id = $stateParams.id;
        //获取租户下的用户列表
        $scope.getData = function(page){
            $scope.param.page = page || $scope.param.page;
            var params = {
                page: $scope.param.page,
                rows: $scope.param.rows
            };
            params.params = JSON.stringify({id: id});
            httpLoad.loadData({
                url:'/tenant/users',
                method:'GET',
                data: params,
                success:function(data){
                    if(data.success&&data.data.rows&&data.data.rows.length!=0){
                        $scope.userList = data.data.rows;
                        $scope.showDetail = $scope.isActive = true;
                        $scope.totalCount = data.data.total;
                        $scope.isImageData = false;
                    }else{
                        $scope.isImageData = true;
                    }
                }
            });
        };
        $scope.getData(1);
    })();
    $scope.goBack = function(){
        $state.go('paas.userCenter.tenant');
    };
}]);