
app.controller('layoutDetailModalCtrl', ['$scope', 'httpLoad', '$rootScope','$modal','$state','$timeout','$stateParams','$sce',
     function($scope, httpLoad, $rootScope, $modal,$state, $timeout,$stateParams,$sce) {
    $rootScope.link = '/statics/css/image.css';//引入页面样式
       $rootScope.moduleTitle =$sce.trustAsHtml('流程管控 > <span>流程编排模板</span> > 详情');
       $scope.param = {
         page:1,
         rows: 10,
       //				params: JSON.stringify([{"param": {"type": "VMWARE"}, "sign": "EQ"}])
       };
       (function(){
         var name = $stateParams.name;
        var version = $stateParams.version;
       httpLoad.loadData({
           url:'/workflow/workflowDef',
           method:'GET',
           data: {name:name,version: version},
           success:function(data){
               if(data.success&&data.data){
                   $scope.supplierDetail = data.data;

                      $scope.changeData = false;
                   $scope.showDetail = $scope.isActive = true;
                     $('#json-renderer').jsonViewer($scope.supplierDetail);
                     $scope.selectTab()
               }
           }
       });
//       httpLoad.loadData({
//    	   url: '/workflow/workflowDefJSON',
//           method:'GET',
//           data: {name:name,version: version},
//           success:function(data){
//               if(data.success&&data.data){
//            	   var workflowJson = angular.fromJson(data.data.workflowJson);
//                    $scope.nodeData = workflowJson.postData;
//                    $scope.changeData = false;
//
//               }
//           }
//       });
       
       })();


       $scope.goBack = function(){
           $state.go('paas.process.layout');
       };
       $scope.detail = function(row){
    	     $state.go('paas.process.workflowdetail', {id: row.workflowId,name:$stateParams.name,version : $stateParams.version})

    	   }
   $scope.selectTab = function(page){
	   var version = $stateParams.version;
     httpLoad.loadData({
         url: '/workflow/search',
     method: 'GET',
         data: {
           name:$scope.supplierDetail.name,
           version: version,
           workflowType:$scope.supplierDetail.workflowType,
           status:$scope.supplierDetail.status
         },
         noParam: false,
         success: function (data) {
           if (data.success) {
             $scope.listData = data.data;
             $scope.total = data.data.length;
             if(!$scope.total){
               $scope.isImageData = true;
               } else {
                 $scope.isImageData = false;
             }

           }
         }
       });
   };
    	   
    	   //全选
    	   $scope.operation = {};
    	   $scope.operation.isBatch1 = true;$scope.operation.isALl = false;
    	   $scope.selectALl = function(){
    	     $scope.operation.isBatch1 = !$scope.operation.isALl;
    	         $scope.listData.forEach(function(item){
    	           item.select = $scope.operation.isALl;
    	         });
    	   }
    	   $scope.choose = function(){
    	     var a = 0,b=0;
    	     $scope.listData.forEach(function(item){
    	       if(item.select==true) a++;
    	       else b=1;
    	     });
    	     if(a>=1) $scope.operation.isBatch1 = false;
    	     else $scope.operation.isBatch1 = true;
    	     if(b==1) $scope.operation.isALl = false;
    	     else $scope.operation.isALl = true;
    	   };
    	   $scope.deteleAll = function ($event) {
    	     var ids = [];
    	     for(var i=0;i<$scope.listData.length;i++){
    	       var item = $scope.listData[i]
    	       if(item.select){
    	         ids.push(item.workflowId);
    	       }
    	     }
    	     var modalInstance = $modal.open({
    	       templateUrl : '/statics/tpl/process/remove.html',
    	       controller : 'removeworkflowModalCtrl',
    	         backdrop: 'static',
    	       resolve : {
    	         id : function(){
    	           return ids;
    	         },

    	       }
    	     });
    	     modalInstance.result.then(function(){
    	       $scope.selectTab(1);
    	       $scope.operation.isALl = false;
    	       $scope.operation.isBatch1 = true;
    	       angular.forEach($scope.listData, function (data, index) {
    	         data.select = false;

    	     })
    	     },function(){});
    	   };

     }
 ]);

 app.controller('modellayoutModalCtrl', ['$rootScope','$modal', '$scope','$state','httpLoad','LANGUAGE','$stateParams',
 function($rootScope,$modal, $scope,$state,httpLoad,LANGUAGE,$stateParams) {
   //详情
  
 }]);
 //删除ctrl
 	angular.module('app').controller('removeworkflowModalCtrl',['$scope','$modalInstance','$stateParams','httpLoad','LANGUAGE','id',
 		function($scope,$modalInstance,$stateParams,httpLoad,LANGUAGE,id){ //依赖于modalInstance
 			$scope.content = '是否删除？';
      var name = $stateParams.name;
     var version = $stateParams.version;
 			$scope.ok = function(){
 				httpLoad.loadData({
 					url:'/workflow/workflow',
 					method:'DELETE',
 					data: {ids:id,name:name,version: version},
 					success:function(data){
 						if(data.success){
 							//console.log(removeData);
 							$scope.pop(LANGUAGE.MONITOR.APP_MESS.DEL_SUCCESS);
 							$modalInstance.close();
 							$scope.operation.isBatch1 = true;$scope.isALl = false;
 							angular.forEach($scope.listData, function (data, index) {
 								data.select = false;

 						})
 						}
 					}
 				});
 			};
 			$scope.cancel = function(){
 			$modalInstance.dismiss('cancel');
 			}
 		}]);
 //执行ctrl
 angular.module('app').controller('executeOpenstackModalCtrl',['$scope','$modalInstance','httpLoad','LANGUAGE','row',
   function($scope,$modalInstance,httpLoad,LANGUAGE,row){ //依赖于modalInstance
     $scope.content = '是否执行编排任务？';
     $scope.ok = function(){
       httpLoad.loadData({
         url:'/workflow/execute',
         method:'POST',
         data: {
           workflowDefName:row.workflowType,
           workflowId:row.workflowId,
           version:row.version
         },
         success:function(data){
           if(data.success){
             //console.log(removeData);
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
