
app.controller('imagedeployCtrl', ['$scope', 'httpLoad', '$rootScope','$state','$timeout','$stateParams','$sce',
     function($scope, httpLoad, $rootScope, $state, $timeout,$stateParams,$sce) {
 var imageId = $stateParams.id;
     
         $scope.goBack = function () {
             $state.go('paas.repository.dockerimage');
         };
       $rootScope.link = '/statics/css/user.css';
       $rootScope.moduleTitle =$sce.trustAsHtml('镜像管理 > <span>镜像</span> > 部署');
       $scope.envList = [{key:'',value:''}];
         $scope.labelList = [{keys:'',values:''}];
         $scope.storageList = [{keys:'',values:'/data'}];
         $scope.dependList = [{id:'',value:{}}];
         $scope.addData = {};
         $scope.store='';
       $scope.name;
       $scope.addData.cpuRequest = 200;
       $scope.addData.memoryRequest = 64;
       $scope.addData.memoryLimit  = 128;
       $scope.addData.cpuLimit = 1000;
       $scope.appCount=1;
       $scope.scriptItem = {};
       $scope.scriptItem.selected={};
      $scope.scriptItem.selected.id =  imageId;
         $scope.dependvalue = true;
         $scope.dependstore = true;
         $scope.envType='auto';
       $scope.vmListselect = {};
       $scope.loadBalanceListselect = {};
       $scope.vmListselect.selected={};
       $scope.loadBalanceListselect.selected={};
         $scope.deployStatus='Deployment';
      var id = $stateParams.id;
         //健康检查
         $scope.readinessProbe = {};
         $scope.livenessProbe = {};
         $scope.livenessProbe.type ='TCP'
         $scope.readinessProbe.type ='TCP'
      var params = {
              simple: true
      }
       $scope.slider = {
           value:  $scope.appCount||1,
           options: {
               floor: 1,
               ceil: 10
           }

       };
         //环境变量为手动时的配置列表
         $scope.setEnvType=function (n) {
             $scope.envType=n;
             if(n=='operate'){
                 $scope.getConfigList();
             }
         }
         $scope.getOpreateList= [{values:''}];
         $scope.getOpreateFlag=true;
         $scope.getConfigList = function(){
             if($scope.addData.appId){
                 httpLoad.loadData({
                     url: '/config/manage/config',
                     method: 'GET',
                     data: {
                         simple :true,
                         params:angular.toJson([{"param":{"appId":$scope.addData.appId},"sign":"EQ"}])
                     },
                     noParam: true,
                     success: function (data) {
                         if (data.success && data.data.rows) {
                             $scope.configList = data.data.rows;
                         }
                     }
                 });
             }else{
                 $scope.pop('请先选择应用,如果没有应用，请先选择部署环境','error');
             }

         }
         $scope.addOpreates = function(){
             $scope.getOpreateList.push({value:''})
         }
         $scope.removeOpreate = function(key){
             if($scope.getOpreateList.length == 1) return $scope.pop('请至少添加一组','error');
             $scope.getOpreateList.splice(key,1);
         }

         $scope.checkConfig = function(){
             var keyArr=[];
             $scope.getOpreateFlag=true;
             $scope.getOpreateList.forEach(function (item) {
                 if((item.value)&&(keyArr.indexOf(item.value)>-1)){
                     $scope.getOpreateFlag=false;
                 }else{
                     keyArr.push(item.value);
                 }
             })
             if(!$scope.getOpreateFlag) $scope.pop('环境变量不能重复，请修改选择项','error');
         }
         //高级选项中添加配置管理
         $scope.getConfigManageFlag=true;
         $scope.getConfigManageList= [{name:'',path:''}];
         $scope.addConfigManages = function(){
             $scope.getConfigManageList.push({name:'',path:''});
             $timeout(function () {
                 $(".configlista").hide().last().show()
             },100)
         }
         $scope.removeConfigManage = function(key){
             if($scope.getConfigManageList.length == 1) return $scope.pop('请至少添加一组','error');
             $scope.getConfigManageList.splice(key,1);
             $timeout(function () {
                 $(".configlista").last().show()
             },100)
         }

         $scope.checkConfigManageList = function(){
             var keyArr=[];
             $scope.getConfigManageFlag=true;
             $scope.getConfigManageList.forEach(function (item) {
                 if((item.name)&&(keyArr.indexOf(item.name)>-1)){
                     $scope.getConfigManageFlag=false;
                 }else{
                     keyArr.push(item.name);
                 }
             })
             if(!$scope.getConfigManageFlag) $scope.pop('配置管理不能重复，请修改选择项','error');
         }
         //高级选项中端口信息变为多个
         $scope.portInfo=[];
         $scope.nodePorts=[];
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
       //是否开启高级设置
       $scope.isSetHigh=false;
       //点击镜像 获取环境信息
         var paramenv = {
             page:1,
             rows: 100000,
             params:angular.toJson([{"param":{"status":"2,4"},"sign":"IN"}])
         };
         httpLoad.loadData({
             url:'/environment/list' ,
             method:'POST',
             data:paramenv,
             noParam: true,
             success:function(data){
                 if(data.success){
                     $scope.masterList = data.data.rows;
                 }
             }
         });
       httpLoad.loadData({
           url:'/image/inspectImage',
           method:'POST',
           data: {imageId: imageId},
           success:function(data){
               if(data.success&&data.data){
                   $scope.supplierDetail = data.data;
                   $scope.scriptItem.selected.port = ($scope.supplierDetail.repositoryType !=1) ?":"+$scope.supplierDetail.repositoryPort:'';
                   if($scope.supplierDetail.namespace == ""){
                	   $scope.scriptItem.selected.name  = $scope.supplierDetail.repositoryAddress+$scope.scriptItem.selected.port+ '/'+$scope.supplierDetail.name+':'+ $scope.supplierDetail.tag
                  }else{
                	 $scope.scriptItem.selected.name  = $scope.supplierDetail.repositoryAddress+$scope.scriptItem.selected.port+ '/'+$scope.supplierDetail.namespace+ '/'+$scope.supplierDetail.name+':'+ $scope.supplierDetail.tag
                  }
                   if($scope.supplierDetail.exposedPort){
                       $scope.exposedPort = angular.fromJson($scope.supplierDetail.exposedPort);
                       $scope.getPortInfo();
                       $scope.envList = [];
                       var envlist = angular.fromJson($scope.supplierDetail.env);
                       envlist.forEach(function(item){
                           var ary = item.split("=");
                           var obj =  {key:ary[0],value:ary[1]};
                           $scope.envList.push(obj);
                       })
					   $scope.isLoadingCheck=false
                   }else{
                       $scope.pop('当前镜像内部无端口，无法进行服务部署，请重新选择','error');
					   $scope.isLoadingCheck=true
                   }


               }
           }
       });

         $scope.getPortInfo=function () {
             $scope.protocolMap=[];
             if($scope.exposedPort){
                 for(var i in $scope.exposedPort){
                     $scope.porttargetPort.push(i.split('/')[0])
                     $scope.containerPorts[i.split('/')[0]]= i.split('/')[1];
                     $scope.protocolMap[i.split('/')[0]]= i.split('/')[1];
                 }
             }
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

       $scope.getHost = function(){
         httpLoad.loadData({
                   url: '/host/list',
                   method: 'POST',
                   data: params,
                   noParam: true,
                   success: function (data) {
                       if (data.success && data.data.rows) {
                           $scope.vmList = data.data.rows;

                       } else {
                           $scope.isImageData = true;
                       }
                   }
               });
       }
         $scope.getApp = function(){
             httpLoad.loadData({
                 url: '/application/list',
                 method: 'POST',
                 data: {params:angular.toJson([{"param":{"envId":$scope.addData.envId},"sign":"EQ"}])},
                 noParam: true,
                 success: function (data) {
                     if (data.success) {
                         $scope.dataAppType = data.data.rows;

                     }
                 }
             });
         }
       $scope.getStorage = function(){
         httpLoad.loadData({
                   url: '/pv/list',
                   method: 'POST',
                   data: params,
                   noParam: true,
                   success: function (data) {
                       if (data.success && data.data.rows) {
                           $scope.storageData = data.data.rows;
                       } else {
                           $scope.isImageData = true;
                       }
                   }
               });
       }
       $scope.getloadBalance = function(){
         httpLoad.loadData({
                   url: '/loadBalance/list',
                   method: 'POST',
                   data: params,
                   noParam: true,
                   success: function (data) {
                       if (data.success && data.data.rows) {
                           $scope.loadBalanceList = data.data.rows;
                       } else {
                           $scope.isImageData = true;
                       }
                   }
               });
       }
         $scope.getapplication = function(){
             httpLoad.loadData({
                 url: '/application/list',
                 method: 'POST',
                 data: params,
                 noParam: true,
                 success: function (data) {
                     if (data.success && data.data.rows) {
                         $scope.applicationList = data.data.rows;
                     } else {

                     }
                 }
             });
         }
         $scope.ListData={};
         $scope.getservice = function(item){
             httpLoad.loadData({
                 url: '/service/list',
                 method: 'POST',
                 data: {applicationId:item.id},
                 success: function (data) {
                     if (data.success ) {
                         item.server = data.data.service.items;
                     } else {

                     }
                 }
             });
         }
         $scope.initDatas=function (isSetHigh) {
             if(!isSetHigh){
                 $scope.labelList = [{keys:'',values:''}];
                 $scope.dependList = [{id:'',value:{}}];
                 $scope.store='';

                 $scope.storageList = [{keys:'',values:'/data'}];
                 $scope.addData.cpuRequest = 200;
                 $scope.addData.memoryRequest = 64;
                 $scope.addData.memoryLimit  = 128;
                 $scope.addData.cpuLimit = 1000;
                 $scope.vmListselect.selected.name="";
                 //健康检查
                 $scope.livenessProbe = {};
                 $scope.readinessProbe = {};
                 $scope.loadBalanceListselect.selected.name="";
                 $scope.addData.enabled=false;
             }else{
                 $scope.store='pv';
                 $scope.setOption=1;
                 $scope.getapplication();
                 $scope.getHost();
                 $scope.scheduledType='host';
                 $scope.configType='cpu';
                 $scope.addData.gpu = 0;
                 $scope.configTypeGpu=false;
                 $scope.addData.enabled=false;
             }

         }
       //高级设置中选择设置项
       $scope.setOption=1;
       $scope.selsectHighSet=function (n) {
           $scope.setOption=n;
           switch(n/1){
             case 1:
                $scope.getHost();
               break;
             case 2:
               break;
             case 3:

               break;
             case 4:
                 $scope.store='pv';
               $scope.getStorage();
               break;
             case 5:
               $scope.loabBalance='inner';
               $scope.getloadBalance();
               break;
             case 6:
                 break;
             case 7:
                 $scope.getapplication();
                 break;
               case 8:
                   $scope.getConfigList();
                   break;
           }
       }
       //调度方式
       $scope.setSchedule=function (n) {
               $scope.scheduledType=n;
       }
         //资源配置
         $scope.setConfigType=function (type) {
             $scope.configType=type;
             if(type=='gpu')$scope.configTypeGpu=true;
             else $scope.configTypeGpu=false;
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
         //命令
         $scope.livenessProbe.command  = ['']
         $scope.addcommand = function(){
             //做验证-》只有上面用户组有用户下面才可以继续添加用户组
             $scope.livenessProbe.command.push('')

         }
         $scope.removecommand = function(key){
             if($scope.livenessProbe.command.length == 1) return $scope.pop('请至少添加一组','error');
             $scope.livenessProbe.command.splice(key,1);

         }
         $scope.readinessProbe.command  = ['']
         $scope.addcommand2 = function(){
             //做验证-》只有上面用户组有用户下面才可以继续添加用户组
             $scope.readinessProbe.command.push('')
             console.log( $scope.readinessProbe.command);

         }
         $scope.removecommand2 = function(key){
             if($scope.readinessProbe.command.length == 1) return $scope.pop('请至少添加一组','error');
             $scope.readinessProbe.command.splice(key,1);

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
         //标签
         $scope.addApply = function(){
             //做验证-》只有上面用户组有用户下面才可以继续添加用户组
             $scope.dependList.push({id:'',value:{}})
             $timeout(function () {
                 $(".applylista").hide().last().show()
             },100)
         }
         $scope.removeApply = function(key){
             if($scope.dependList.length == 1) return $scope.pop('请至少添加一组','error');
             $scope.dependList.splice(key,1);
             $timeout(function () {
                 $(".applylista").last().show()
             },100)
         }
       //挂载卷

       $scope.setStore=function (n) {
           $scope.store=n;
       }
       //负载
       $scope.setLoad=function (n) {
           $scope.loabBalance=n;
       }
       //健康检查
       $scope.enabledData={true:"关闭",false:"开启"}
      //应用部署镜像名称接口
       var param = {
              simple: true
               }


     //添加环境方法
          $scope.addEnvs = function(){
              $scope.envList.push({key:'',value:''})
           }
           $scope.removeEnv = function(key){
             if($scope.envList.length == 1) return $scope.pop('请至少添加一组','error');
             $scope.envList.splice(key,1);
           }
         $scope.checkvalue = function(item,key,index){
             for(var j=0;j<$scope.dependList.length;j++){
                 var items = $scope.dependList[j];
                 var dependvalue = angular.fromJson(items.value);
                 item = angular.fromJson(item);
                 if(index == j){
                     continue
                 }else{
                     if(item.name == dependvalue.name&&items.id == key){
                         $scope.pop('服务不能重复','error');
                         $scope.dependvalue = false;
                         return;
                     }
                     else{
                         $scope.dependvalue = true
                     }
                 }
             }
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
                         $scope.dependstore = false;
                         return;
                     }
                     else{
                         $scope.dependstore = true
                     }
                 }
             }
         }
         $scope.checkName=function () {
             if($scope.serviceName=="")return;
             if($scope.addData.appId){
                 httpLoad.loadData({
                     url: '/service/existed',
                     method: 'POST',
                     data: {applicationId:$scope.addData.appId,name:$scope.serviceName},
                     //  noParam: true,
                     success: function (data) {
                         if (!data.success) {

                             return false
                         }else {
                             return true;
                         }
                     }
                 });
             }else{
                 $scope.pop('请先选择应用,如果没有应用，请先选择部署环境','error');
             }


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
         $scope.checkcpu=function () {
             //、资源限制
             //CPU
             if(($scope.addData.cpuLimit) && ($scope.addData.cpuRequest)){
                 if (($scope.addData.cpuLimit) < ($scope.addData.cpuRequest)) {
                     $scope.pop("CPU的limit必须大于或等于request", 'error');
                     return false;
                 }
             }else{
                 $scope.pop("请填写完整的CPU的limit和request", 'error');
                 return false;
             }

             //Memory
             if(($scope.addData.memoryLimit) && ($scope.addData.memoryRequest)){
                 if (($scope.addData.memoryLimit) < ($scope.addData.memoryRequest)) {
                     $scope.pop("Memory的limit必须大于或等于request", 'error');
                     return false
                 }
             }else{
                 $scope.pop("请填写完整的Memory的limit和request", 'error');
                 return false
             }

             if ($scope.addData.memoryLimit< 4) {
                 $scope.pop("limit必须大于4M", 'error');
                 return false
             } else {
                 if ($scope.addData.memoryLimit.toString().split(".")[1]) {
                     if ($scope.addData.memoryLimit.toString().split(".")[1].length > 2) {
                         $scope.pop("小数点不能多于2位", 'error');
                         return false
                     }
                 } else {
                 }
             }
             return true;
         }
        $scope.ok = function () {
            httpLoad.loadData({
                url: '/service/existed',
                method: 'POST',
                data: {applicationId:$scope.addData.appId,name:$scope.serviceName},
                //  noParam: true,
                success: function (data) {
                    if (!data.success) {
                        return false
                    }else {
                        return true;
                    }
                }
            });
            if( $scope.isSetHigh){
                if(!$scope.checkcpu())	 return false;
                $scope.checkConfigManageList();
                if(!$scope.getConfigManageFlag)return false;
                $scope.checknum();
                if(!$scope.nodePortCheck)return false;
            }
            if(!$scope.dependvalue){   $scope.pop('服务项不能重复','error');return false;  }
            if(!$scope.dependstore){   $scope.pop('持久化不能重复','error');return false;  }
            $scope.checkConfig();
            if(!$scope.getOpreateFlag)return false;
            var envList = {};
            var labelList = {};
            var storageList = [];
            var dependList = [];
            for(var i=0;i<$scope.envList.length;i++){
              var item = $scope.envList[i];
              if((!item.key&&item.value)||(item.key&&!item.value)){
                    $scope.pop('请添加完整的环境变量','error');
                    return;
                 }else if(!item.key&&!item.value){
                   continue
                 }else{
                    envList[item.key] = item.value;
                 }
            }
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
            if($scope.store=="pv"){
                for(var j=0;j<$scope.storageList.length;j++){
                    var items = $scope.storageList[j];
                    if(!items.keys&&!items.values){
                        continue
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
            for(var j=0;j<$scope.dependList.length;j++){
             var items = $scope.dependList[j];
             var dependvalue = angular.fromJson(items.value);
             if((!items.id&&dependvalue.name)||(items.id&&!dependvalue.name)){
                    $scope.pop('请添加完整的依赖','error');
                    return;
                 }else if(!items.id&&!dependvalue.name){
                  continue
                 }else{
                  dependList.push({name:dependvalue.name,namespace:dependvalue.namespace});
                 }
            }
            var nodeSelector = {}
            if($scope.scheduledType=="host"){
             nodeSelector  = {nodeName:$scope.vmListselect.selected.name||''};
           }else{
             nodeSelector = labelList
           };
           var a = $scope.addData.cpuRequest||'200' ;
           var b = $scope.addData.memoryRequest||'64';
           var c  = $scope.addData.memoryLimit||'128';

            var resources = {
                "type":$scope.configType,
            }
            if($scope.configType=='cpu'){
                resources.requests={
                    "memory":b,
                    "cpu":a
                };
                resources.limits={
                    "memory":c ,
                    "cpu":$scope.addData.cpuLimit
                };
            }else if($scope.configType=='gpu'){
                resources.gpuNum=$scope.addData.gpu||0
            }
            if(!$scope.isSetHigh){
                resources={};
            } 
            $scope.targetPortArr.forEach(function (item) {
                delete $scope.containerPorts[item];
            });

            //高级选项开启时,组合端口信息数据
            $scope.portInfo=[];
            $scope.nodePorts=[];
            if($scope.isSetHigh){
                $scope.portList.forEach(function (item) {
                    if(item.targetPort){
                        if(item.optionsM){
                            $scope.portInfo.push(item);
                        }else{
                            $scope.nodePorts.push(item);
                        }
                    }
                })
            }
            if(!$scope.livenessProbe.select){
                $scope.livenessProbe = {}
            }else{
                var arr = []
                $scope.livenessProbe.command.forEach(function (item) {
                    if(item){
                        arr.push(item)
                    }
                })
                $scope.livenessProbe.command = arr;
            }
            if(!$scope.livenessProbe.select){
                $scope.livenessProbe = {}
            }else{
                var arr = []
                $scope.readinessProbe.command.forEach(function (item) {
                    if(item){
                        arr.push(item)
                    }
                })
                $scope.readinessProbe.command = arr;
            }
        	if(!$scope.isSetHigh||($scope.isSetHigh&&!$scope.readinessProbe.select)){
         		$scope.readinessProbe={};
          
         	}
          var param = {
            applicationId:$scope.addData.appId,
            deployment:{
              name:$scope.serviceName,
                deployStatus:$scope.deployStatus,
                imageId:imageId,
               image:$scope.scriptItem.selected.name||'',
               env:envList||{},
                envFrom:[],
               replicas:$scope.slider.value,
               nodeSelector:{"type":$scope.scheduledType||'',"value":nodeSelector},
               resources:resources,
               containerPorts:$scope.containerPorts,
                ports:$scope.portInfo||[],
                nodePorts:$scope.nodePorts||[],
                volumes:{type:$scope.store,arrays:storageList},
               // loadBalance:{"type":$scope.loabBalance||'',"component":$scope.loadBalanceListselect.selected.name||''},
                healthCheck:$scope.addData.enabled,
                 rely:dependList,
                healthCheck:{livenessProbe:$scope.livenessProbe,readinessProbe:$scope.readinessProbe},
                cmVolume:[]
            }

              };
            if($scope.envType=='operate'){
                var arr=[];
                $scope.getOpreateList.forEach(function (item) {
                    if(item.value)arr.push(item.value);
                })
                param.deployment.env={};
                param.deployment.envFrom=arr;
            }
            if($scope.isSetHigh){
                var cmVolumesArr = [];
                for(var r=0;r<$scope.getConfigManageList.length;r++){
                    var items = $scope.getConfigManageList[r];
                    if((!items.name&&items.path)||(items.name&&!items.path)){
                        $scope.pop('请添加完整的配置管理','error');
                        return;
                    }else if(!items.name&&!items.path){
                        continue;
                    }else{
                        cmVolumesArr.push(items);
                    }
                }
                param.deployment.cmVolume=cmVolumesArr;
            }
             httpLoad.loadData({
                  url: '/service/deploy',
                 method:'POST',
                 data: param,
                 success:function(data){
                     if(data.success){
                         $scope.pop('部署成功');
                       $state.go('paas.application.instancetiondetail', {id:$scope.addData.appId})
                     }
                 }
             });
         }

     }
 ]);
