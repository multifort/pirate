
app.controller('openstackDetailModalCtrl', ['$scope', 'httpLoad', '$rootScope','$modal','$state','$timeout','$stateParams',
     function($scope, httpLoad, $rootScope, $modal,$state, $timeout,$stateParams) {
    $rootScope.link = '/statics/css/image.css';//引入页面样式
       $rootScope.moduleTitle = '镜像仓库 > 仓库管理 > 详情';
       $scope.param = {
    	        rows: 10
    	    };
    	    (function(){
    	      var id = $stateParams.id;
    	    httpLoad.loadData({
    	        url:'/registry/detail',
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
       $scope.goBack = function(){
           $state.go('paas.repository.repository');
       };
     }
 ]);

app.controller('openstackModalCtrl', ['$rootScope', '$scope','$modal', '$state','httpLoad','LANGUAGE','$stateParams',
function($rootScope, $scope,$modal,$state,httpLoad,LANGUAGE,$stateParams) {
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
            if(!$scope.totalCount){
              $scope.isImageData = true;
              } else {
                $scope.isImageData = false;
            }
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
    $scope.content = '是否删除该条记录？';
    $scope.ok = function(){
      httpLoad.loadData({
        url:'/image/remove',
        method:'POST',
        data: {ids:id},
        success:function(data){
          if(data.success){
          	$scope.pop(data.message);
            $modalInstance.close();
          }
        }
      });
    };
    $scope.cancel = function(){
  $modalInstance.dismiss('cancel');
    }
  }]);
