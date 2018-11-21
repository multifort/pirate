(function(){
    app.controller('systemMonitorCtrl', ['$scope', 'httpLoad', '$rootScope','$modal','$state','$timeout','webSocket','$sce',
        function($scope, httpLoad, $rootScope, $modal,$state, $timeout,webSocket,$sce) {
            $rootScope.moduleTitle = '系统运维 > 监测平台';//定义当前页
                 //查环境
            $scope.param = {
                page:1,
                rows: 100000,
                params:angular.toJson([{"param":{"status":"2"},"sign":"EQ"}])
            };
            $scope.isShow=true;
            httpLoad.loadData({
                url:'/environment/list' ,
                method:'POST',
                data:$scope.param,
                noParam: true,
                success:function(data){
                    if(data.success){
                        $scope.masterList = data.data.rows;
                        if($scope.masterList.length<1){
                            $scope.pop("未查询到可用的监测环境","error");
                            $scope.isShow=false;
                        }else{
                            $scope.searchByEnv= $scope.masterList[0].id;
                            $scope.changeUrl();
                        }
                    }
                }
            });
              $scope.changeUrl=function () {
                    httpLoad.loadData({
                        url:'/environment/getMonitorUrl' ,
                        method:'POST',
                        data:{id:$scope.searchByEnv},
                        noParam:false,
                        success:function(data){
                            if(data.success){
                            	$scope.isShow=true;
                                $scope.iframeUrl=$sce.trustAsResourceUrl(data.data.url+"/dashboard/db/cluster");
                            }else{
                            	 $scope.isShow=false;
                            }
                        }
                    });
            }
            $scope.width="100%";
            $scope.height2=document.documentElement.clientHeight-90+'px';
            $scope.height=document.documentElement.clientHeight-90+'px';
            window.onresize = function(){
                $scope.height2=document.documentElement.clientHeight-90+'px';
                $scope.height=document.documentElement.clientHeight-90+'px';
                $scope.style.height=$scope.height2;
                $scope.$apply();
              /*  $timeout(function () {
                    $scope.height=document.documentElement.clientHeight-90+'px';
                    $scope.height2=document.documentElement.clientHeight-90+'px';
                },10)*/
            }
            $scope.style = {
                "padding": "0",
                "overflow": "hidden",
                "height":$scope.height2
            }
            //用jquery实现
          /*  $("#system").height(document.documentElement.clientHeight-90+'px');
            $("#system2").height(document.documentElement.clientHeight-90+'px');
            window.onresize = function(){
                $("#system").height(document.documentElement.clientHeight-90+'px');
                $("#system2").height(document.documentElement.clientHeight-90+'px');
            }*/

        }
    ])})()
