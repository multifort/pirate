
app.controller('loadBalanceDetailModalCtrl', ['$scope', 'httpLoad', '$rootScope','$modal','$state','$timeout','$stateParams','$sce',
     function($scope, httpLoad, $rootScope, $modal,$state, $timeout,$stateParams,$sce) {
    $rootScope.link = '/statics/css/image.css';//引入页面样式
       $rootScope.moduleTitle =$sce.trustAsHtml('环境资源 > <span>负载管理</span> > 详情');
         $scope.statusData = {"0":"正常","1":"不正常"};
         $scope.typeData = {"1":"NGINX","2":"F5"};
       $scope.param = {
         page:1,
         rows: 10,
       //				params: JSON.stringify([{"param": {"type": "VMWARE"}, "sign": "EQ"}])
       };
       (function(){
         var id = $stateParams.id;
       httpLoad.loadData({
           url:'/loadBalance/detail',
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
       $scope.initDetail = function(){
          httpLoad.loadData({
          url: '/pdb/detail',
          method: 'GET',
          data: {},
          success: function (data) {
            if (data.success) {
               $scope.supplierDetail = data.data;
              $scope.nodeData = {
                targetName: $scope.supplierDetail.name,
                targetCategory: 'DB',
                relations: $scope.supplierDetail.resourceRelations
              };
            }
          }
        });
        }
        $scope.goBack = function(){
           $state.go('paas.environment.loadbalance');
       };
     }
 ]);

app.controller('datastoreApplicationListModalCtrl', ['$rootScope','$modal', '$scope','$state','httpLoad','LANGUAGE','$stateParams',
function($rootScope,$modal, $scope,$state,httpLoad,LANGUAGE,$stateParams) {
    $scope.param = {
        page:1,
        rows: 10
    };
    $scope.getData = function (page) {
      $scope.param.page = page || $scope.param.page;
      $scope.param.params=angular.toJson([{"param":{"b. loadbalance_id":$stateParams.id},"sign":"EQ"}])
      httpLoad.loadData({
        url: 'loadBalance/listApps',
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
