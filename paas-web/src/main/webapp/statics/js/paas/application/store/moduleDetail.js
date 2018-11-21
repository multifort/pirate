
app.controller('modelDetailModalCtrl', ['$scope', 'httpLoad', '$rootScope','$modal','$state','$timeout','$stateParams','$sce',
     function($scope, httpLoad, $rootScope, $modal,$state, $timeout,$stateParams,$sce) {
    $rootScope.link = '/statics/css/image.css';//引入页面样式
       $rootScope.moduleTitle =$sce.trustAsHtml('应用服务 > <span>应用编排</span> > 详情');
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
         $scope.param.params =  JSON.stringify([{"param": {"ali.layout_id": $stateParams.id}, "sign": "EQ"}])

         httpLoad.loadData({
             url: '/application/listByLayout',
             method: 'POST',
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

app.controller('modelopenstackModalCtrl', ['$rootScope','$modal', '$scope','$state','httpLoad','LANGUAGE','$stateParams',
function($rootScope,$modal, $scope,$state,httpLoad,LANGUAGE,$stateParams) {
  //详情
  $scope.detail = function(id){
    $state.go('paas.repository.imagedetail', {id: id})

  }
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
    $scope.goAction = function (flag, id, $event) {
      switch (flag / 1) {
        case 1:
        //删除
            if($event) $event.stopPropagation();
            var ids=[];
            ids.push(id);
            var modalInstance = $modal.open({
              templateUrl : '/statics/tpl/repository/repository/remove.html',
              controller : 'removeOpenstackModalCtrl',
              	backdrop: 'static',
              resolve : {
                id : function(){
                  return ids;
                },

              }
            });
            modalInstance.result.then(function(){
              $scope.getData(1);
              $scope.isCheck = false;
            },function(){});

        break;
        case 2:
        //部署
        $state.go('paas.repository.imagetiondeploy', {id: id})
          break;
        case 3:
          //删除
              if($event) $event.stopPropagation();
              var ids=[];
              ids.push(id);
              var modalInstance = $modal.open({
                templateUrl : '/statics/tpl/repository/image/remove.html',
                controller : 'removeUserModalCtrl',
                	backdrop: 'static',
                resolve : {
                  id : function(){
                    return ids;
                  },

                }
              });
              modalInstance.result.then(function(){
                $scope.getData(1);
                $scope.isCheck = false;
              },function(){});

          break;

      }
    };

}]);
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
