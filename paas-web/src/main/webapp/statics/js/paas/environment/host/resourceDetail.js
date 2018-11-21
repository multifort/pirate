
app.controller('resourceDetailModalCtrl', ['$scope','$interval', 'httpLoad', '$rootScope','$modal','$state','$timeout','$stateParams','$sce',
     function($scope,$interval, httpLoad, $rootScope, $modal,$state, $timeout,$stateParams,$sce) {
    $rootScope.link = '/statics/css/image.css';//引入页面样式
       $rootScope.moduleTitle =$sce.trustAsHtml('环境资源 > <span>主机管理</span> > 详情');
         $scope.statusData =  {1:"正常",2:"异常",3:"可调度",4:"不可调度",5:"添加中",6:"移出中"};
       $scope.param = {
         page:1,
         rows: 10
       };
         var socket;
       (function(){
         var id = $stateParams.id;
       httpLoad.loadData({
           url:'/host/detail',
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
           bucketDuration:"1h",
           gpuBucketDuration:"1h",
         applyCpu:{
           theme:'Cpu',
             unit:'Millicores'
         },
           applyMemory:{
               theme:'Memory',
               unit:'MiB'
           },
           applyNet:{
               theme:'Network',
               unit:'KiB/s'
           },
           file:{
               theme:'File',
               unit:'GiB'
           },
           applyGpu:{
               theme:'gpu使用率',
               unit:'%'
           },
           applyMemUsage:{
               theme:'内存使用量',
               unit:'MiB'
           },
           applyTemp:{
               theme:'温度',
               unit:'℃'
           }

       }
         $scope.timeTypeData =[
             {
                 name :'Last hour',
                 value:"1h"
             },{
                 name :'Last 4 hour',
                 value:"4h"
             },{
                 name :'Last day',
                 value:"1d"
             },{
                 name :'Last 3 day',
                 value:"3d"
             },{
                 name :'Last week',
                 value:"1w"
             }
         ];
         $scope.gpuTimeNum ={
             "1h":"1",
             "4h":"4",
             "1d":"1",
             "3d":"3",
             "1w":"1",
         };
         $scope.gpuTimeUnit={
             "1h":"HOUR",
             "4h":"HOUR",
             "1d":"DAY",
             "3d":"DAY",
             "1w":"WEEK",
         }
       //监控
       $scope.monitored = function (item) {
         var paramObj = {
             type:'node',
             startTime: $scope.options.bucketDuration,
             bucketDuration: 60,
             resourceName:$stateParams.hostName,
             namespace: "_system",
             envId: $stateParams.envId
         };
          httpLoad.loadData({
              url: '/application/monitor',
              method: 'GET',
              data:paramObj,
              //noParam:true,
              success: function(data){
                if(data.success) {
                  if(data.data){
                      $scope.performance = data.data;
                  }
              }else if($scope.timeInterval){
                  $interval.cancel($scope.timeInterval);
                  $scope.timeInterval = '';
              }
              }
            });

       }
         $scope.gpuMonitored = function (item) {
             var paramObj = {
                 // type:'node',
                 num:  $scope.gpuTimeNum[$scope.options.gpuBucketDuration],
                 timeUnit: $scope.gpuTimeUnit[$scope.options.gpuBucketDuration],
                 // resourceName:$stateParams.hostName,
                 // namespace: "_system",
                 // envId: $stateParams.envId,
                 id:$stateParams.id
             };
             httpLoad.loadData({
                 url: '/host/monitor/gpu',
                 method: 'GET',
                 data:paramObj,
                 //noParam:true,
                 success: function(data){
                     if(data.success) {
                         if(data.data){
                             $scope.gpuPerformance = data.data;
                         }
                     }else if($scope.timeInterval){
                         $interval.cancel($scope.timeInterval);
                         $scope.timeInterval = '';
                     }
                 }
             });

         }
       //$scope.monitored();
         var setTimeFun = function () {
             $scope.timeInterval = $interval(function () {
                 $scope.monitored ();
                 $scope.gpuMonitored ();
             }, 120*1000);
         };
         setTimeFun();
       $scope.goBack = function(){
           history.go(-1);
        //   $state.go('paas.environment.host');
           //终止监测
           $interval.cancel($scope.timeInterval);
           $scope.timeInterval = '';
           if(socket){
               socket.close();
           }
       };
     }
 ]);

app.controller('resourceVmListModalCtrl', ['$rootScope','$modal', '$scope','$state','httpLoad','LANGUAGE','$stateParams','$timeout','$interval',
function($rootScope,$modal, $scope,$state,httpLoad,LANGUAGE,$stateParams,$timeout,$interval) {
    var socket;
    $scope.getData = function (page) {
        var params = [];
        var param1 = {
            ip:$stateParams.ip,
            id:$stateParams.envId
        }
        if (angular.toJson(param1).length > 2) params.push({param: param1, sign: 'LK'});
        $scope.param = {
            page: 1,
            rows: 10,
            params: angular.toJson(params)
        }
        httpLoad.loadData({
            url: '/container/node/list',
            method: 'POST',
            data: $scope.param,
            noParam: true,
            success: function (data) {
                if (data.success) {
                    $scope.listDatapod = data.data;
                    $scope.totalpod = data.data.length;
                    if(!$scope.totalpod) {
                        $scope.isImageDatapod = true;
                    } else {
                        $scope.isImageDatapod = false;
                    }
                }else {
                }
            }
        });

    };
    // 实例列表详情返回
    $scope.goBack = function(){
        $scope.isActive = false;
        $timeout(function() {
            $scope.showDetail = false;
        }, 200);
        //终止监测
        $interval.cancel($scope.timeInterval);
        $scope.timeInterval = '';
        if(socket){
            socket.close();
        }
        $scope.performance='';
        $scope.showUp = false
    };
    // 实例列表详情
    $scope.paramObj = {
        resourceName:'',
        namespace:''
    };
    var KubernetesData={
        platformId: $stateParams.envId,
        type: 'Pod',
        row: '',
        state:'',
        host:{}
    };
    $scope.listDetail = function (row,type,$event) {
        $scope.paramObj.resourceName = row.name;
        $scope.paramObj.namespace = row.namespace;
        KubernetesData.row = row;
        $event.stopPropagation();
        $scope.isActive = true ;
        var params = {"resourceType":type,"namespace":row.namespace,"resourceName":row.name,id:KubernetesData.platformId}
        switch (type) {
            case 'Pod':
                $scope.showDetail = 5;
                break;
        };

        //详情基本信息
        $scope.getBasicMessage(params,type,row);
        //事件
        if(type!='Master'){
            httpLoad.loadData({
                url: '/container/event',
                method: 'POST',
                data: params,
                success: function(data){
                    if(data.success) {
                        $scope.isDataLoad = true;
                        $scope.listEventData = data.data;
                        $scope.totalEvent = data.data.length;
                        if(!$scope.totalEvent){
                            $scope.isImageDatad = true;
                        } else {
                            $scope.isImageDatad = false;
                        }
                    }
                }
            });
        }
        setTimeFun();
    };
    $scope.getBasicMessage = function (params,type,row) {
        httpLoad.loadData({
            url:'/container/detail',
            method:'POST',
            data: params,
            success:function(data){
                if(data.success&&data.data){
                    $scope.Container = []
                    $scope.supplierDetail = data.data;
                    if(type=='Pod'){
                        $scope.Container = $scope.supplierDetail.Container;

                        $scope.Status = $scope.supplierDetail.Status[0];
                        $scope.Template = $scope.supplierDetail.Template;
                        $scope.Volumes = $scope.supplierDetail.Volumes;
                        for(var i=0;i<$scope.Container.length;i++){
                            var item = $scope.Container[i]
                            if(item.state=='running'){
                                KubernetesData.state = true;
                                KubernetesData.states = false;
                                $scope.options.bucketContainer =item.containerName;
                                $scope.options.bucketT =item.containerName
                                $scope.options.bucketL =item.containerName
                                $scope.getLog(row,type,item.state);
                             //   $scope.getTeminal(row,type);
                                $scope.chackIn(type,row)
                                   if (item.state) {
                                                $scope.prompts = true;
                                            } else {
                                                $scope.prompts = false
                                            }
                                setTimeout(function(){
                                    if(!$('.kuberb option').eq(0).html()){
                                        $('.kubera option').eq(0).remove()
                                        $('.kuberb option').eq(0).remove()
                                        $('.kuberc option').eq(0).remove()
                                    }
                                },100)

                                return
                            }else  if(item.state=='Completed'){
                                $scope.options.bucketContainer =item.containerName;
                                $scope.options.bucketT =item.containerName
                                $scope.options.bucketL =item.containerName
                                KubernetesData.state = true
                                KubernetesData.states = true
                                $scope.getLog(row,type,item.state);
                              //  $scope.getTeminal(row,type);
                                $scope.chackIn(type,row)
                                 if (item.state) {
                                                $scope.prompts = true;
                                            } else {
                                                $scope.prompts = false
                                            }
                                setTimeout(function(){
                                    if(!$('.kuberb option').eq(0).html()){
                                        $('.kubera option').eq(0).remove()
                                        $('.kuberb option').eq(0).remove()
                                        $('.kuberc option').eq(0).remove()
                                    }
                                },100)
                                return
                            }else {
                                $scope.options.bucketContainer =$scope.Container[0].containerName;
                                $scope.options.bucketT =$scope.Container[0].containerName
                                $scope.options.bucketL =$scope.Container[0].containerName
                                KubernetesData.state = false
                                KubernetesData.states = true;
                            }
                        }
                        setTimeout(function(){
                            if(!$('.kuberb option').eq(0).html()){
                                $('.kubera option').eq(0).remove()
                                $('.kuberb option').eq(0).remove()
                                $('.kuberc option').eq(0).remove()
                            }
                        },100)
                        $scope.getLog(row,type,row.status);
                        $scope.getTeminal(row,type);
                        $scope.chackIn(type,row)

                    }else{
                        $scope.Pvc = $scope.supplierDetail.Pvc[0];
                        $scope.Source = $scope.supplierDetail.Source[0];
                        $scope.basicDetails = $scope.supplierDetail.basicDetails[0];
                        if($scope.basicDetails.labels){
                            $scope.labels =  $scope.basicDetails.labels.replace(/,/g,'/<br>')
                        }
                    }
                }
            }
        });
    }
    //终端
    $scope.getTeminal = function () {
        var row = KubernetesData.row;
        var notNormal = false;
        $rootScope.baseUrl = "ws://"+row.proxyUrl+":"+row.port;
        $rootScope.selfLink = "/api/v1/namespaces/"+row.namespace+"/pods/"+row.name;
        $rootScope.containerName = $scope.options.bucketT;
        $rootScope.accessToken = "";
        $rootScope.preventSocket = true;
        $timeout(function () {
            $scope.Teminal = true;
        }, 100)
    }

    //日志
    $scope.getLog = function (row,type,status) {
        var row = KubernetesData.row
        var params = {
            "resourceType":'Pod',
            "namespace":row.namespace,
            "resourceName":row.name,
            'containerName':$scope.options.bucketL,
            'status':status,
            id:KubernetesData.platformId
        }
        httpLoad.loadData({
            url: '/container/log',
            method: 'GET',
            data: params,
            success: function(data){
                if(data.data==null){
                    $scope.logPod = data.data;
                }else{
                    $scope.logPod = data.data.replace(/\n/g,'<br>');
                }
            }
        });
    }

    $scope.showUps = function () {
        $scope.showUp = true
    }
    $scope.chackIn = function (type,row) {
        if(type!='Node'&&type!='Pod'){return}
        if(type == 'Pod'&&!KubernetesData.state){
            $scope.prompt = false;return
        }else{
            $scope.prompt = true;
        }
        $scope.monitored();


    }
    var setTimeFun = function () {
        $scope.timeInterval = $interval(function () {
            $scope.monitored ();
        }, 120*1000);
    };
    //监控
    $scope.options = {
        bucketDuration:0,
        cpu:{
            theme:'CPU',
            unit:'Millicores'
        },
        memorynode:{
            theme:'Memory',
            unit:'MiB'
        },
        memorypod:{
            theme:'Memory',
            unit:'MiB'
        },
        network:{
            theme:'Network',
            unit:'KiB/s'
        }
    };
    $scope.timeTypeData =[
        {
            name :'Last hour',
            value:0
        },{
            name :'Last 4 hour',
            value:1
        },{
            name :'Last day',
            value:2
        },{
            name :'Last 3 day',
            value:3
        },{
            name :'Last week',
            value:4
        }
    ];
    var bucketDuration =[60,60,60,60,60];
    var startTime =['1h','4h','1d','3d','1w'];
    $scope.monitored = function () {

        var namespace = ''
        if(KubernetesData.type=='Node'){
            namespace ='_system'
        }else{
            namespace =$scope.paramObj.namespace
        }
        var paramObj = {
            type:KubernetesData.type.toLowerCase(),
            startTime:startTime[$scope.options.bucketDuration],
            bucketDuration:bucketDuration[$scope.options.bucketDuration],
            resourceName:$scope.paramObj.resourceName,
            containerName:$scope.options.bucketContainer,
            namespace:namespace,
            envId:KubernetesData.platformId
        };
        httpLoad.loadData({
            url: '/application/monitor',
            method: 'GET',
            data:paramObj,
            //  noParam:true,
            success: function(data){
                if(data.success) {

                    $scope.performance = data.data;
                }else if($scope.timeInterval){
                    $interval.cancel($scope.timeInterval);
                    $scope.timeInterval = '';
                }
            }
        });

    }
    $scope.$on("$destroy", function(event) {
        $interval.cancel($scope.timeInterval);
        $scope.timeInterval = '';
    });
    $scope.getnodepod = function(row){
        var params = [];
        var param1 = {
            nodeName:row.name,
            id:KubernetesData.platformId
        }

        if (angular.toJson(param1).length > 2) params.push({param: param1, sign: 'LK'});
        // if (angular.toJson(param2).length > 2) params.push({param: param2, sign: 'EQ'});
        $scope.param = {
            page: 1,
            rows: 10,
            params: angular.toJson(params)
        }
        httpLoad.loadData({
            url: '/container/node/list',
            method: 'POST',
            data: $scope.param,
            noParam: true,
            success: function (data) {
                if (data.success) {
                    $scope.listDatapod = data.data;
                    $scope.totalpod = data.data.length;
                    if(!$scope.totalpod) {
                        $scope.isImageDatapod = true;
                    } else {
                        $scope.isImageDatapod = false;
                    }
                }else {
                }
            }
        });
    }
}]);
