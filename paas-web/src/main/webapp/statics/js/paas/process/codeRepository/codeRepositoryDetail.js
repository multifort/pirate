
app.controller('codeRepositoryDetailModalCtrl', ['$scope','$interval', 'httpLoad', '$rootScope','$modal','$state','$timeout','$stateParams','$sce',
     function($scope,$interval, httpLoad, $rootScope, $modal,$state, $timeout,$stateParams,$sce) {
    $rootScope.link = '/statics/css/image.css';//引入页面样式
       $rootScope.moduleTitle =$sce.trustAsHtml('流程管控 > <span>代码仓库</span> > 详情');
         $scope.statusData =  {1:"正常",2:"异常",3:"可调度",4:"不可调度",5:"添加中"};
       $scope.param = {
         page:1,
         rows: 10
       };
         var socket;
       (function(){
         var id = $stateParams.id;
       httpLoad.loadData({
           url:'/code/repository/'+id+'/code',
           method:'GET',
           data: {},
           success:function(data){
               if(data.success&&data.data){
                   $scope.supplierDetail = data.data;
                   $scope.showDetail = $scope.isActive = true;
               }
           }
       });
       })();

       $scope.goBack = function(){
           history.go(-1);
        //   $state.go('paas.environment.host');

       };
     }
 ]);
