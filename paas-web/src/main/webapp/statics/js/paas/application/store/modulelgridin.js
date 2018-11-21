
//新增ctrl
 angular.module('app').controller('addmoduleModalCtrl',['$rootScope','$scope','$state','$stateParams','LANGUAGE','httpLoad','$timeout',
     function($rootScope,$scope,$state,$stateParams,LANGUAGE,httpLoad,$timeout){ //依赖于modalInstance
       $rootScope.link = '/statics/css/image.css';
       $rootScope.moduleTitle ='应用服务 > <span>应用商店</span> > 组件部署'
       $scope.goBack = function(){
    	   history.go(-1)
       };

         $scope.$parent.cpuRequest = $scope.$parent.cpuLimit = "m"
         $scope.$parent.memoryRequest = $scope.$parent.memoryLimit = "M"
         $scope.addData = {};
         $scope.dependvalue = true;
         $scope.vmListselect = {};
         $scope.vmListselect.selected={};
         $scope.labelList = [{keys:'',values:''}];
      $scope.addData.name =$stateParams.name;
      $scope.addData.appId = $stateParams.appId;
      $scope.addData.envId = $stateParams.envId;
      $scope.addData.app = $stateParams.appId;
         $scope.deploy = $stateParams.type;
         $scope.store='pv';
         $scope.scheduledType='host';
         if( $scope.deploy == 0 ){
             $scope.deployType='单节点';
         }else if( $scope.deploy==1){
             $scope.deployType='集群';
         }else{
             $scope.deployType='单节点';
         }
         $scope.type = [];
         $scope.storageList = [{keys:'',values:''}];
        $scope.addData.cpuLimit=2000;
         $scope.addData.cpuRequest =200;
         $scope.addData.memoryRequest =64;
         $scope.addData.memoryLimit =4096;
     // 选环境
         $scope.getenv = function(){
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
         }
        
         $scope.getHost = function(envId){
             var data = {
                 params: angular.toJson([{"param": {"envId": envId, "status":"3"}, "sign": "EQ"}])
             }
             if($scope.addData.envId){
            	 httpLoad.loadData({
                     url: '/host/list',
                     method: 'POST',
                     data: data,
                     noParam: true,
                     success: function (data) {
                         if (data.success && data.data.rows) {
                             $scope.vmList = data.data.rows;

                         } else {
                             $scope.isImageData = true;
                         }
                     }
                 });
       	 }else{
       		 $scope.pop("请先选择环境","error") 
       	 }
         }
         //高级选项中端口信息变为多个
         $scope.containerPorts = {};
         $scope.porttargetPort =[];
         $scope.portList = [{}];
         $scope.targetPortArr=[];
         $scope.addports = function(key){
             $scope.portList.push({});
             $timeout(function () {
                 $(".portlista").hide().last().show()
             },100)
         }
         $scope.removeports = function(key){
             if($scope.portList.length == 1) return $scope.pop('请至少添加一组','error');
             $scope.portList.splice(key,1);
             $timeout(function () {
                 $(".portlista").last().show()
             },100)
         }
         $scope.targetPortChange=function (item) {
             $scope.targetPortArr=[];
             $scope.portSame=false;
             $scope.portList.forEach(function (v) {
                 if(v.targetPort){
                     if($scope.targetPortArr.indexOf(v.targetPort)>-1){
                         $scope.portSame=true;
                     }else{
                         $scope.targetPortArr.push(v.targetPort)
                     }
                 }
             })
             if($scope.portSame){
                 item.targetPort="";
                 $scope.pop('目标端口不能重复,请重新选择','error');
                 return false;
             }
             item.port = item.targetPort;
             item.protocol=$scope.protocolMap[item.targetPort];
         }
         $scope.checkport = function (item) {
             if(!item.targetPort){
                 $scope.pop('请先选择目标端口','error');
                 item.optionsM=false;
             }
             if(item.optionsM){
                 item.nodePort = 30000+ Math.floor(Math.random() * (32767- 30000+ 1))
             }else{ item.nodePort=""}
         }
         //校验随机端口
         $scope.checknum=function () {
             $scope.nodePortCheck=true;
             $scope.portList.forEach(function (item) {
                 if(30000<item.nodePort/1&&item.nodePort/1<32767){

                 }else if(!item.nodePort){

                 }else{
                     $scope.pop('随机端口需要在30000~32767之间','error');
                     $scope.nodePortCheck=false;
                 }
             })
         }
         $scope.setStore=function (n) {
             $scope.store=n;
         }
         //调度方式
         $scope.setSchedule=function (n) {
             $scope.scheduledType=n;
         }
         //标签
         $scope.addLabels = function(){
             //做验证-》只有上面用户组有用户下面才可以继续添加用户组
             $scope.labelList.push({keys:'',values:''})
             $timeout(function () {
                 $(".labellista").hide().last().show()
             },100)
         }
         $scope.removeLabel = function(key){
             if($scope.labelList.length == 1) return $scope.pop('请至少添加一组','error');
             $scope.labelList.splice(key,1);
             $timeout(function () {
                 $(".labellista").last().show()
             },100)
         }
        $scope.getApp = function(envId){
        	 httpLoad.loadData({
                 url: '/application/list',
                 method: 'POST',
                 data: {params:angular.toJson([{"param":{"envId":envId},"sign":"EQ"}])},
                 noParam: true,
                 success: function (data) {
                   if (data.success) {
                     $scope.dataType = data.data.rows;
                     $scope.vmListselect.selected={};
                     $scope.labelList = [{keys:'',values:''}];
                  $scope.addData.name =$stateParams.name;
                  $scope.addData.appId = $stateParams.appId;
                  $scope.addData.app = $stateParams.appId;
                     $scope.deploy = $stateParams.type;
                     $scope.store='pv';
                     $scope.scheduledType='host';
//                     if( $scope.deploy == 0 ){
//                         $scope.deployType='单节点';
//                     }else if( $scope.deploy==1){
//                         $scope.deployType='集群';
//                     }else{
//                         $scope.deployType='单节点';
//                     }

                     $scope.storageList = [{keys:'',values:''}];
                    $scope.addData.cpuLimit=2000;
                     $scope.addData.cpuRequest =200;
                     $scope.addData.memoryRequest =64;
                     $scope.addData.memoryLimit =4096;
                     $scope.StringData.forEach(function(item){
                         if(item.type=="String"){
                             item.select = ""
                         }
                     })
                   }
                 }
               }); 
        	 $scope.addData.tempalate = ""
        		 $scope.type.forEach(function(item){
        			 if(item == "Volumes"){             
                         $scope.getStorage(envId);                                           
                      }
                	 if(item == "NodeSelector"){
                    	  $scope.getHost(envId)  
                      }
        		 })
            $scope.goload(envId);

        }
         //存储券
         $scope.addstorages = function(){
             //做验证-》只有上面用户组有用户下面才可以继续添加用户组
             $scope.storageList.push({keys:'',values:''})
             $timeout(function () {
                 $(".storagelista").hide().last().show()
             },100)
         }
         $scope.removestorages = function(key){

             if($scope.storageList.length == 1) return $scope.pop('请至少添加一组','error');
             $scope.storageList.splice(key,1);
             $timeout(function () {
                 $(".storagelista").last().show()
             },100)
         }
         $scope.getStorage = function(){
        
             var pvParam = {
                 simple: true,
                 params: angular.toJson([{"param": {"status": "Available", "envId": $scope.addData.envId}, "sign": "EQ"}])
             }
             if($scope.addData.envId){
            	 httpLoad.loadData({
                     url: '/pv/list',
                     method: 'POST',
                     data: pvParam,
                     noParam: true,
                     success: function (data) {
                         if (data.success && data.data.rows) {
                             $scope.storageData = data.data.rows;
                         } else {
                             $scope.isImageData = true;
                         }
                     }
                 });
       	 }else{
       		 $scope.pop("请先选择环境","error") 
       	 }
            
         }
         if($stateParams.envId){
        	 var envId = $stateParams.envId;
        	 $scope.addData.tempalate = ""
            
         }else{
        	 $scope.getenv()
         }
         $scope.chackStore = function(item,index){
             for(var j=0;j<$scope.storageList.length;j++){
                 var items = $scope.storageList[j];
                 //var dependvalue = angular.fromJson(items.value);
                 //item = angular.fromJson(item);
                 if(index == j){
                     continue
                 }else{
                     if(items.keys == item){
                         $scope.pop('持久化不能重复','error');
                         $scope.dependvalue = false;
                         return;
                     }
                     else{
                         $scope.dependvalue = true
                     }
                 }
             }
         }

         //点击部署方式请求模板列表
         $scope.getTempalateList = function (type) {

                 httpLoad.loadData({
                     url: '/application/store/deploy/type',
                     method: 'GET',
                     data: {"deployType": type},
                     success: function (data) {
                         if (data.success) {
                             $scope.tempalateList = data.data;
                           
                         }
                     }
                 });
                
           
         }
        // $scope.getTempalateList($scope.deployType);
         //bu需要其他参数的api接口
         $scope.goload = function(envId){   //添加map样子的数组
                var data = {};
                var key = ""
                for(var i=0;i< $scope.StringData.length;i++){
                	var item =  $scope.StringData[i];
                	if(item.type=="Api"&&(item.event=='load'||item.event=='double_load')){
                        if(item.name=="ZK_SVC_NAME"){
                        	key = i;
                            data={"envId":envId||$stateParams.envId, "applicationId":  $scope.addData.appId || $stateParams.appId}
                            data = angular.extend(data,item.params)
                        }
                        if(!data.envId)return
                        httpLoad.loadData({
                            url: item.interface,
                            data:data,
                            method:item.method,
                           // noParam: true,
                            ignoreError: true,
                            success:function(data){
                            	if(data.data == []||data.data == null){
                            		 $scope.pop("请先添加ZK组件，再部署该服务","error") 
                            	}
                            	$scope.StringData[key].value  = data.data;

                            }
                        })

                    }
                }
           
         }

         //bu需要其他参数的api接口
         $scope.getParams = function(msg){   //添加map样子的数组
             msg =  angular.fromJson(msg);
            $scope.StringData.forEach(function(item){
                    if(item.name=="ZK_SVC_NAME"){
                        item.select = msg.ZK_SVC_NAME;
                    }
                if(item.name=="ZK_NAME"){
                    item.select =  msg.ZK_NAME;
                }
            })
         }
         $scope.goApi = function (item) {
             return
        	 if(!item.select){return}
        	 if($scope.addData.envId){
        		  httpLoad.loadData({
                      url: "/service/existed",
                      method: 'POST',
                      data: {"name": item.select, envId:$scope.addData.envId, applicationId:$scope.addData.appId},
                      success: function (data) {
                          if (data.success) {
                         	 $scope.nameCheck = data.success;

                          }else{
                              $scope.nameCheck = data.success;
                          }
                      }
                  });
        	 }else{
        		 $scope.pop("请先选择环境，再填名称","error") 
        	 }
           
         }
         //显示模板内容
         $scope.getTempalate = function (type) {
        	 $scope.deployType = type;
             if ($stateParams.modelid) {

                 httpLoad.loadData({
                     url: '/application/store/tempalate',
                     method: 'GET',
                     data: {"storeId": $stateParams.modelid, deployType:type},
                     success: function (data) {
                         if (data.success) {
                             $scope.StringData = data.data.parameters;
                             $scope.StringData.forEach(function(item){
                                 if(item.required=="true"){
                                     item.required = "required_true"
                                 }else{
                                     item.required = "required_false"
                                 }
                                 if(item.type=='Volumes'){
                                	  if($stateParams.envId){
                                     	 var envId = $stateParams.envId;
                                         	 $scope.getStorage(envId);                                           
                                      }
                                	  $scope.type.push("Volumes") ;
                                 }
                                 if(item.type=='Number'){
                               	item.select=item.select/1 ;
                              }   if(item.type=='Ports'){
                                     httpLoad.loadData({
                                         url: "/application/store/image/port",
                                         method: 'GET',
                                         data: {"storeId": $stateParams.modelid, deployType:type},
                                         success: function (data) {
                                             if (data.success) {
                                                 var envlist = data.data;
                                                 if(data.data){
                                                     $scope.isLoadingActive =false
                                                     $scope.protocolMap = {};
                                                     $scope.porttargetPort =[];
                                                     envlist.forEach(function(item){
                                                         var obj =  {key:item.key,value:item.value};
                                                         $scope.porttargetPort.push(obj);

                                                         $scope.protocolMap[item.key] = item.value;
                                                         $scope.containerPorts[item.key] = item.value;

                                                     })
                                                 }else{
                                                     $scope.pop('没有端口信息','error');
                                                     $scope.isLoadingActive =true
                                                 }


                                             }else{

                                             }
                                         }
                                     });
                                 }
                                 if(item.type=='NodeSelector'){
                               	  if($stateParams.envId){
                                    	 var envId = $stateParams.envId;
                                    	    $scope.getHost(envId)                                          
                                     } 
                               	 $scope.type.push("NodeSelector") ;
                                }
                             })
                             $scope.addData.id= $stateParams.modelid;
                             
                             $scope.goload($scope.addData.envId)
                         }
                     }
                 });

             }
         }
         $scope.getTempalate($scope.deployType)
         $scope.appResource = [
             {
                 name: 'Mi',
                 value: 'Mi'
             },
             {
                 name: 'GI',
                 value: 'Gi'
             },
             {
                 name: 'M',
                 value: 'M'
             },
             {
                 name: 'G',
                 value: 'G'
             }
         ]

         $scope.unit = {
             'K': 1,
             'M': 1000,
             'G': 1000 * 1000,
             'T': 1000 * 1000 * 1000,
             'P': 1000 * 1000 * 1000 * 1000,
             'E': 1000 * 1000 * 1000 * 1000 * 1000,
             'Ki': 1,
             'Mi': 1024,
             'Gi': 1024 * 1024,
             'Ti': 1024 * 1024 * 1024,
             'Pi': 1024 * 1024 * 1024 * 1024,
             'Ei': 1024 * 1024 * 1024 * 1024 * 1024
         }
        $scope.ok = function () {
        	 var storageList=[];
        	// if(!$scope.nameCheck){ $scope.pop("这个名称已经存在，不能重复","error");return false;}
            if(!$scope.dependvalue){   $scope.pop('持久化不能重复','error');return false;  }
             var param = {
                 storeId:$scope.addData.id,
                 envId:$scope.addData.envId,
                 template:{},
                 deployType:$scope.deployType,
                 applicationId:$scope.addData.appId
             };
            if($scope.store=="pv"){
                for(var j=0;j<$scope.storageList.length;j++){
                    var items = $scope.storageList[j];
                    if(!items.keys&&!items.values){
                        continue
                    }else if((!items.keys&&items.values)||(items.keys&&!items.values)){
                        $scope.pop('请添加完整的持久化','error');
                        return;
                    }else{
                        var storage = {"pv":items.keys,"path":items.values}
                        storageList.push(storage);
                    }
                }

            }else if($scope.store=="tv"){
                var storage = {"pv":"","path":$scope.addData.tv_innerpath}
                storageList.push(storage);
            }else{

            }
            var cpuLimit = $scope.addData.cpuLimit + $scope.$parent.cpuLimit;
            var cpuRequest = $scope.addData.cpuRequest + $scope.$parent.cpuRequest;
            var memoryRequest = $scope.addData.memoryRequest + $scope.$parent.memoryRequest;
            var memoryLimit = $scope.addData.memoryLimit + $scope.$parent.memoryLimit;

            if ($scope.$parent.cpuLimit == "" && $scope.$parent.cpuRequest != "") {
                if (($scope.addData.cpuLimit * 1000) < ($scope.addData.cpuRequest)) {
                    $scope.pop("CPU的limit必须大于或等于request", 'error');
                    return;
                }
            } else if ($scope.$parent.cpuLimit != "" && $scope.$parent.cpuRequest == "") {
                if (($scope.addData.cpuLimit) < ($scope.addData.cpuRequest * 1000)) {
                    $scope.pop("CPU的limit必须大于或等于request", 'error');
                    return;
                }
            } else {
                if (($scope.addData.cpuLimit) < ($scope.addData.cpuRequest)) {
                    $scope.pop("CPU的limit必须大于或等于request", 'error');
                    return;
                }
            }
            //Memory
            if ($scope.$parent.memoryLimit.length == 1 && $scope.$parent.memoryRequest.length == 2) {
                if (($scope.addData.memoryLimit * $scope.unit[$scope.$parent.memoryLimit]) < ($scope.addData.memoryRequest * $scope.unit[$scope.$parent.memoryRequest] / 1024 * 1000)) {
                    $scope.pop("Memory的limit必须大于或等于request", 'error');
                    return
                }
            } else if ($scope.$parent.memoryLimit.length == 2 && $scope.$parent.memoryRequest.length == 1) {
                if (($scope.addData.memoryLimit * $scope.unit[$scope.$parent.memoryLimit] / 1024 * 1000) < ($scope.addData.memoryRequest * $scope.unit[$scope.$parent.memoryRequest])) {
                    $scope.pop("Memory的limit必须大于或等于request", 'error');
                    return
                }
            } else {
                if (($scope.addData.memoryLimit * $scope.unit[$scope.$parent.memoryLimit]) < ($scope.addData.memoryRequest * $scope.unit[$scope.$parent.memoryRequest])) {
                    $scope.pop("Memory的limit必须大于或等于request", 'error');
                    return
                }
            }
            if ($scope.addData.memoryLimit * $scope.unit[$scope.$parent.memoryLimit] < 4000) {
                $scope.pop("limit必须大于4M", 'error');
                return
            } else {
                if ($scope.addData.memoryLimit.toString().split(".")[1]) {
                    if ($scope.addData.memoryLimit.toString().split(".")[1].length > 2) {
                        $scope.pop("小数点不能多于2位", 'error');
                        return
                    }
                } else {
                }
            }
            var labelList = {};
            for(var j=0;j<$scope.labelList.length;j++){
                var items = $scope.labelList[j];
                if((!items.keys&&items.values)||(items.keys&&!items.values)){
                    $scope.pop('请添加完整的标签','error');
                    return;
                }else if(!items.keys&&!items.values){
                    continue
                }else{
                    labelList[items.keys] = items.values;
                }
            }
          for(var i = 0;i<$scope.StringData.length;i++){
        	  var item = $scope.StringData[i];
        	  if(item.type == "Resource"){
                  param.template.LIMITS_CPU= cpuLimit;
                  param.template.REQUESTS_CPU = cpuRequest;
                  param.template.LIMITS_MEMORY = memoryLimit;
                  param.template.REQUESTS_MEMORY = memoryRequest;

              }else if(item.type == "Volumes"){
                  param.template.volumes={type:$scope.store,arrays:storageList};
              }else if(item.type == "NodeSelector"){
             	  var nodeSelector = {}
                   if($scope.scheduledType=="host"){
                	   nodeSelector  = {nodeName:$scope.vmListselect.selected.name||""};
                   }else{
                       nodeSelector = labelList
                   };
             	  if(nodeSelector.nodeName){
             			
             	  }else{
             		  $scope.pop('请选择调度方式','error');return  
             	  }
                  param.template.nodeSelector={"type":$scope.scheduledType||'',"value":nodeSelector};
            
              }else if(item.type == "Ports"){
            	  $scope.targetPortArr.forEach(function (item) {
                      delete $scope.containerPorts[item];
                  });
                  //高级选项开启时,组合端口信息数据
                  $scope.portInfo=[];
                  $scope.nodePorts=[];
                  $scope.portList.forEach(function (item) {
                      if(item.targetPort){
                          if(item.optionsM){
                              $scope.portInfo.push(item);
                          }else{
                              $scope.nodePorts.push(item);
                          }
                      }
                  })
                  param.template.portsExpose = {}
                  param.template.portsExpose.containerPorts = $scope.containerPorts;
                  param.template.portsExpose.ports = $scope.portInfo;
                  param.template.portsExpose.nodePorts = $scope.nodePorts;

              }
              else if(item.type == "Number"){
                  param.template[item.name] = item.select+item.defaultUnit;
              }else{
                  param.template[item.name] = item.select;
              }
          }

             httpLoad.loadData({
                 url:'/application/store/deploy',
                 method:'POST',
                 data: param,
                 success:function(data){
                     if(data.success){
                       $scope.pop(data.message);
//                         $state.go('paas.application.instancetiondetail', {id: $scope.addData.appId});
                       $state.go('paas.application.instancetiondetail', {id: data.data});
                     }
                 }
             });
         }

         $scope.cancel = function(){
            $modalInstance.dismiss('cancel');
         };
     }]);
