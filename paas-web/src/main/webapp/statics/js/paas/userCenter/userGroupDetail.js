angular.module('app').controller('userGroupDetailCtrl', ['$rootScope', '$scope','$state','httpLoad','$stateParams',function($rootScope, $scope,$state,httpLoad,$stateParams) {
    $rootScope.link = '/statics/css/user.css';
    $rootScope.moduleTitle = '系统管理 > 分组管理';
    (function(){
        $scope.itemsByPage = 8;//定义每页的条数
        //加载列表
        var loadData = function(){
            httpLoad.loadData({
                url:'/user/group/detail',
                method:'GET',
                data: {id: $stateParams.id},
                success:function(data){
                    if(data.success){
                        $scope.userDetail = data.data;
                        $scope.showDetail = $scope.isActive = true;
                    }
                }
            });
            httpLoad.loadData({
                url:'/user/group/listUser',
                method:'POST',
                data:{
                    id:$stateParams.id
                },
                success:function(data){
                    if(data.success){
                        $scope.userListData= data.data;
                        $scope.total1 = data.data.length;
                    }
                }
            });
            httpLoad.loadData({
                url:'/user/group/listResGroup',
                method:'POST',
                data:{
                    id:$stateParams.id
                },
                success:function(data){
                    if(data.success){
                        $scope.resGroupListData= data.data;
                        $scope.total2 = data.data.length;
                    }
                }
            });
        };
        loadData();
        //跳转详情页
        $scope.detail = function(id){
            $state.go('paas.userCenter.resGroupDetail',{id:id});
        };
    })();
    $scope.goBack = function(){
        $state.go('paas.userCenter.group');
        sessionStorage.setItem('grouptabLocation', JSON.stringify('userGroup'));
    };
}]);