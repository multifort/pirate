
app.controller('modelgridinCtrl', ['$rootScope', '$scope','$state','httpLoad','$stateParams','LANGUAGE','$timeout',
 function($rootScope, $scope,$state,httpLoad,$stateParams,LANGUAGE,$timeout) {
   $rootScope.link = '/statics/css/image.css';
   $rootScope.moduleTitle ='应用服务 > 应用编排文件 > 部署'
   $scope.goBack = function(){
       $state.go('paas.application.template');
   };
     $scope.param = {
         page:1,
         rows: 100000,
         params:angular.toJson([{"param":{"status":"2,4"},"sign":"IN"}])
     };
     httpLoad.loadData({
         url:'/environment/list' ,
         method:'POST',
         data:$scope.param,
         noParam: true,
         success:function(data){
             if(data.success){
                 $scope.masterList = data.data.rows;
             }
         }
     });
  var id = $stateParams.id;
 httpLoad.loadData({
    url:'/layout/detail',
    method:'GET',
    data: {id: id},
    success:function(data){
        if(data.success&&data.data){
            $scope.supplierDetail = data.data;
            $timeout(function () {
              $scope.codeMirror.codeMirror.setValue($scope.supplierDetail.fileContent);
            }, 100);
        }
    }
 });
 var params = {
         simple: true
 }
     $scope.getApp = function(){
         httpLoad.loadData({
             url: '/application/list',
             method: 'POST',
             data: {params:angular.toJson([{"param":{"envId":$scope.addData.envId},"sign":"EQ"}])},
             noParam: true,
             success: function (data) {
                 if (data.success) {
                     $scope.dataType = data.data.rows;

                 }
             }
         });
     }


     $scope.scriptItem = {}
     $scope.scriptItem.selected = {}
/*  httpLoad.loadData({
     url: '/openshift/cluster/list',
    method: 'POST',
    data: $scope.param,
    noParam: true,
    success: function (data) {
      if (data.success) {
        $scope.masterData = data.data.rows;

      }
    }
  });*/
    $scope.layoutDeploy = function () {
        if($scope.scriptItem.selected.id){
            httpLoad.loadData({
                url: '/service/deploy',
                method: 'POST',
                data: {
                    layoutId:$stateParams.id,
                    // clusterId:$scope.clusterId,
                    applicationId:$scope.scriptItem.selected.id
                },
                success: function (data) {
                    if (data.success) {
                        $state.go('paas.application.instancetiondetail', {id: $scope.scriptItem.selected.id});
                    }
                }
            });
        }else{
            $scope.pop("请选择应用名称","error");
        }

    }
	// 获取选择文件id然后通过接口获得文件内容
    $scope.codeMirror = {};


}]);
