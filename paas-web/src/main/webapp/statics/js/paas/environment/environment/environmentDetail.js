
app.controller('environmentDetailModalCtrl', ['$scope', 'httpLoad', '$rootScope','$modal','$state','$timeout','$stateParams','$sce',
     function($scope, httpLoad, $rootScope, $modal,$state, $timeout,$stateParams,$sce) {
    $rootScope.link = '/statics/css/image.css';//引入页面样式
       $rootScope.moduleTitle =$sce.trustAsHtml('环境资源 > <span>环境管理</span> > 详情');
         $scope.statusData = {1:"不可用",2:"激活状态",3:"冻结状态",4:"异常状态",5:"创建中",6:"死亡"};
         $scope.typeData = {1:"kubernetes",2:"swarm"};
       $scope.param = {
         page:1,
         rows: 10,
       //				params: JSON.stringify([{"param": {"type": "VMWARE"}, "sign": "EQ"}])
       };
       (function(){
         var id = $stateParams.id;
       httpLoad.loadData({
           url:'/environment/detail',
           method:'GET',
           data: {id: id},
           success:function(data){
               if(data.success&&data.data){
                   $scope.supplierDetail = data.data;
               }
           }
       });
       })();
        $scope.goBack = function(){
           $state.go('paas.environment.environment');
       };
         $scope.topology = function () {
             //拓扑
             httpLoad.loadData({
                 url: '/environment/topology',
                 method: 'GET',
                 data: {envId:$stateParams.id},
                 success: function (data) {
                     if (data.success) {
                         $scope.itemData = data.data;
                         $scope.nodeData =  $scope.itemData;

                     }
                 }
             });
         }
     }
 ]);

app.controller('environmentVmListModalCtrl', ['$rootScope','$modal', '$scope','$state','httpLoad','LANGUAGE','$stateParams',
function($rootScope,$modal, $scope,$state,httpLoad,LANGUAGE,$stateParams) {
    $scope.statusTypeData = {1:"正常",2:"异常",3:"可调度",4:"不可调度",5:"添加中",6:"移出中"};
    $scope.param = {
        rows: 10
    };
    //获取云主机列表及根据条件查询
    $scope.getData = function (page) {
        $scope.param.page = page || $scope.param.page;
        var params = {
                page: $scope.param.page,
                rows: $scope.param.rows
            },
            searchParam = [];
            searchParam.push({"param": {"env_id": $stateParams.id}, "sign": "EQ"});
        params.params = JSON.stringify(searchParam);
        httpLoad.loadData({
            url: '/host/list',
            method: 'POST',
            data: params,
            noParam: true,
            success: function (data) {
                if (data.success && data.data.rows) {
                    $scope.vmList = data.data.rows;
                    $scope.totalCount = data.data.total;
                } else {
                    $scope.isImageData = true;
                }
            }
        });
    };
    $scope.getData(1);
}]);
