angular.module('app').controller('resourceGroupDetailCtrl', ['$rootScope', '$scope','$state','httpLoad','$stateParams','$modal',function($rootScope, $scope,$state,httpLoad,$stateParams,$modal) {
    $rootScope.link = '/statics/css/user.css';
    $rootScope.moduleTitle = '系统管理 > 分组管理';
    (function(){
        $scope.itemsByPage = 8;//定义每页的条数
        //加载列表
        $scope.loadData = function(){
            httpLoad.loadData({
                url:'/res/group/detail',
                method:'GET',
                data: {id: $stateParams.id},
                success:function(data){
                    if(data.success){
                        $scope.resourceDetail = data.data;
                        $scope.showDetail = $scope.isActive = true;
                    }
                }
            });
            httpLoad.loadData({
                url:'/res/group/listRes',
                method:'POST',
                data:{
                    id:$stateParams.id
                },
                success:function(data){
                    if(data.success){
                        $scope.userListData= data.data;
                        $scope.total = data.data.length;
                        angular.forEach($scope.userListData, function(data,index){
                            if(data.status=='RUNNING') data.status1='运行中';
                            else if(data.status=='STOPPED') data.status1='停止';
                            else if(data.status=='NORMAL') data.status1='正常';
                            else if(data.status=='ABNORMAL') data.status1='不正常';
                            else if(data.status=='EXCEPTION') data.status1='异常';
                            else data.status1='异常';
                        });
                    }
                }
            });
        };
        $scope.loadData();
    })();
    //删除
    $scope.remove = function(id,$event,$index,key){
        if($event) $event.stopPropagation();
        var removeData= {"groupId":$stateParams.id,"resId":id};
        var modalInstance = $modal.open({
            templateUrl : '/statics/tpl/userCenter/group/remove.html',
            controller : 'removeResourceGroupResModalCtrl',
            resolve : {
                removeData : function(){
                    return removeData;
                }
            }
        });
        modalInstance.result.then(function(){
            $scope.loadData();
        },function(){});
    };

    $scope.goBack = function(){
        $state.go('paas.userCenter.group');
        sessionStorage.setItem('grouptabLocation', JSON.stringify('resourceGroup'));
    };
}]);

//删除ctrl
angular.module('app').controller('removeResourceGroupResModalCtrl',['$scope','$modalInstance','httpLoad','removeData',
    function($scope,$modalInstance,httpLoad,removeData){ //依赖于modalInstance
        $scope.content = '是否删除资源分组下的该条资源？';

        $scope.ok = function(){
            httpLoad.loadData({
                url:'/res/group/deleteRes',
                method:'POST',
                data: removeData,
                success:function(data){
                    if(data.success){
                        //console.log(removeData);
                        $scope.pop('资源分组下的该条资源删除成功');
                        $modalInstance.close();
                    }
                }
            });
        };
        $scope.cancel = function(){
            $modalInstance.dismiss('cancel'); // 退出
        }
    }]);