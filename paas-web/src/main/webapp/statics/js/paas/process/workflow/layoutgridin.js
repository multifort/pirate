
app.controller('layoutgridinCtrl', ['$rootScope', '$scope','$state','httpLoad','$stateParams','LANGUAGE','$timeout',
 function($rootScope, $scope,$state,httpLoad,$stateParams,LANGUAGE,$timeout) {
   $rootScope.link = '/statics/css/image.css';
   $rootScope.moduleTitle ='应用服务 > <span>应用编排</span> > 编排部署'
   $scope.goBack = function(){
       $state.go('paas.process.model');
   };
	var params = {
	        simple: true
	}
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
 httpLoad.loadData({
   url: '/application/list',
   method: 'POST',
   data: params,
   noParam: true,
   success: function (data) {
     if (data.success) {
       $scope.dataType = data.data.rows;

     }
   }
 });

  $scope.scriptItem = {}
  httpLoad.loadData({
     url: '/openshift/cluster/list',
    method: 'POST',
    data: $scope.param,
    noParam: true,
    success: function (data) {
      if (data.success) {
        $scope.masterData = data.data.rows;

      }
    }
  });
    $scope.layoutDeploy = function () {
   	 httpLoad.loadData({
 	        url: '/application/layoutDeploy',
 	        method: 'POST',
 	        data: {
 	         layoutId:$stateParams.id,
             clusterId:$scope.clusterId,
             appId:$scope.scriptItem.selected.id
          },
 	        success: function (data) {
 	            if (data.success) {
                $state.go('paas.process.apply');
 	            }
 	        }
 	    });
    }
	// 获取选择文件id然后通过接口获得文件内容
    $scope.codeMirror = {};


}]);
