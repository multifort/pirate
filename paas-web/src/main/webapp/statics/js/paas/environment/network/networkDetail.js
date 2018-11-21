
app.controller('networkDetailModalCtrl', ['$scope', 'httpLoad', '$rootScope','$modal','$state','$timeout','$stateParams','$sce',
     function($scope, httpLoad, $rootScope, $modal,$state, $timeout,$stateParams,$sce) {
    $rootScope.link = '/statics/css/image.css';//引入页面样式
       $rootScope.moduleTitle =$sce.trustAsHtml('资源管理 > <span>集群管理</span> > 详情');
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
       $scope.options = {
         apply:{
           theme:'',
             unit:''
         },
       }
       $scope.monitorDeta ={
         keys: [],
         values: [
         {
         data: [	0,0,0,0,0,0,0,0,0,0,0,0,0,0,0],
         name: "Memory",
         },
         {
           data: [	0,0,0,0,0,0,0,0,0,0,0,0,0,0,0],
         name: "Network Send",
         },
         {
           data: [	0,0,0,0,0,0,0,0,0,0,0,0,0,0,0],
         name: "Network Received",
         },
         {
           data: [	0,0,0,0,0,0,0,0,0,0,0,0,0,0,0],
         name: "CPU",
         }
       ],
       } ;
       //监控
       $scope.monitored = function (item) {
         var paramObj = {
             type:'pod',


         };
          httpLoad.loadData({
              url: '/application/monitor',
              method: 'GET',
              data:paramObj,
              //noParam:true,
              success: function(data){
                if(data.success) {
                  if(data.data.keys){

                    $scope.performance = data.data[0];
                  }
              }else if($scope.timeInterval){
                  $interval.cancel($scope.timeInterval);
                  $scope.timeInterval = '';
              }
              }
            });

       }
       $scope.monitored();
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
           $state.go('paas.environment.network');
       };
     }
 ]);

app.controller('networkopenstackModalCtrl', ['$rootScope','$modal', '$scope','$state','httpLoad','LANGUAGE','$stateParams',
function($rootScope,$modal, $scope,$state,httpLoad,LANGUAGE,$stateParams) {
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
     $scope.initDetail()
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
  app.controller('networkhistoryModalCtrl', ['$rootScope', '$scope','$state','httpLoad','$stateParams',function($rootScope, $scope,$state,httpLoad,$stateParams) {
    $rootScope.link = '/statics/css/alarm.css';//引入页面样式
    $scope.param = {
        page:1,
            rows: 10,
            params:angular.toJson([{"param":{"object":"image"},"sign":"LK"}])
        };
      $scope.getHistory = function (page) {
        var id = $stateParams.id;
        httpLoad.loadData({
          url: '/app/log/list',
            method:'POST',
            data: $scope.param,
            noParam:true,
            success:function(data){
                if(data.success&&data.data){
                    $scope.userList = data.data.rows;
                    $scope.totalCount = data.data.total;
                    if(data.data.rows==[]){
                        $scope.pop("返回数据为空");
                    }
                    if (data.data.rows.length == 0) {
                      $scope.isImageData = true;
                      return;
                    } else $scope.isImageData = false;
                }

            }
        });
    }
  $scope.getHistory(1)
  }]);
