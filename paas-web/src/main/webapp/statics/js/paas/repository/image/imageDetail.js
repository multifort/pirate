angular.module('app').controller('warehouseDetailModalCtrl', ['$rootScope', '$scope','$state','httpLoad','$stateParams',function($rootScope, $scope,$state,httpLoad,$stateParams) {
    $rootScope.link = '/statics/css/user.css';
    $rootScope.moduleTitle = '镜像仓库 > 镜像管理 > 详情';
    (function(){

        var imageId = $stateParams.id;
      httpLoad.loadData({
          url:'/image/inspectImage',
          method:'POST',
          data: {imageId: imageId},
          success:function(data){
              if(data.success&&data.data){
                  $scope.supplierDetail = data.data;
                  $scope.envObject={}
                     if($scope.supplierDetail.env){
                       var evn = $scope.supplierDetail.env;
                          $scope.supplierDetail.env = evn.substring(1,evn.length-1).replace(/,/g,'<br>')
                     }
                     if($scope.supplierDetail.exposedPort){
                         $scope.supplierDetail.exposedPort=angular.fromJson($scope.supplierDetail.exposedPort);
                         var str='';
                         for(k in $scope.supplierDetail.exposedPort){
                             str+=k+';    ';
                         }
                        // $scope.supplierDetail.exposedPort = $scope.supplierDetail.exposedPort.substring(1,$scope.supplierDetail.exposedPort.length-1)
                        $scope.supplierDetail.exposedPort=str;
                     }
                  //密码处理

              }
          }
      });
      })();
    $scope.goBack = function(){
        history.go(-1);
     //   $state.go('paas.repository.image');
    };


}]);
