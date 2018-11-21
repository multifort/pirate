
app.controller('modelDetailModalCtrl', ['$scope', 'httpLoad', '$rootScope','$modal','$state','$timeout','$stateParams','$sce',
     function($scope, httpLoad, $rootScope, $modal,$state, $timeout,$stateParams,$sce) {
    $rootScope.link = '/statics/css/image.css';//引入页面样式
       $rootScope.moduleTitle =$sce.trustAsHtml('应用服务 > 应用编排文件 > 详情');
       $scope.param = {
         page:1,
         rows: 10,
       //				params: JSON.stringify([{"param": {"type": "VMWARE"}, "sign": "EQ"}])
       };
       (function(){
         var id = $stateParams.id;
       httpLoad.loadData({
           url:'/layout/detail',
           method:'GET',
           data: {id: id},
           success:function(data){
               if(data.success&&data.data){
                   $scope.supplierDetail = data.data;
                   $scope.showDetail = $scope.isActive = true;
                   $scope.test()

               }
           }
       });
       })();
       $scope.codeMirror = {};
       $scope.test = function () {
         $scope.code = true;
          if($scope.supplierDetail){
            $timeout(function () {
              $scope.codeMirror.codeMirror.setValue($scope.supplierDetail.fileContent);
            }, 100);
          }

       }
       
       $scope.selectTab = function(page){

         $scope.param.page = page || $scope.param.page;
         $scope.param.params =  JSON.stringify([{"param": {"layout.id": $stateParams.id}, "sign": "EQ"}])

         httpLoad.loadData({
             url: '/layout/used',
             method: 'GET',
             data: $scope.param,
             noParam: true,
             success: function (data) {
               if (data.success) {
                 $scope.countData = data.data.rows;
                 $scope.totalCount = data.data.total;
                 if(!$scope.totalCount) {
                  $scope.isImageData = true;
                } else {
                  $scope.isImageData = false;
                }

               }
             }
           });
       };
       $scope.goBack = function(){
           $state.go('paas.application.template');
       };
     }
 ]);
//删除ctrl
angular.module('app').controller('removeOpenstackModalCtrl',['$scope','$modalInstance','httpLoad','LANGUAGE','id',
  function($scope,$modalInstance,httpLoad,LANGUAGE,id){ //依赖于modalInstance
    $scope.content = '是否删除？';
    $scope.ok = function(){
      httpLoad.loadData({
        url:'/image/removeImage',
        method:'POST',
        data: {id:id},
        success:function(data){
          if(data.success){
            //console.log(removeData);
            $scope.pop(LANGUAGE.MONITOR.APP_MESS.DEL_SUCCESS);
            $modalInstance.close();
          }
        }
      });
    };
    $scope.cancel = function(){
    $modalInstance.dismiss('cancel');
    }
  }]);
