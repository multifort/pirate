
app.controller('applydeployModalCtrl', ['$scope', 'httpLoad', '$rootScope','$modal','$state','$timeout','$stateParams','$sce','webSocket',
     function($scope, httpLoad, $rootScope, $modal,$state, $timeout,$stateParams,$sce,webSocket) {
       $rootScope.link = '/statics/css/image.css';
       $rootScope.moduleTitle =$sce.trustAsHtml('应用服务 > <span>应用实例</span> > 部署');

       (function(){
         var id = $stateParams.id;
       httpLoad.loadData({
           url:'/application/detail',
           method:'GET',
           data: {id: id},
           success:function(data){
               if(data.success&&data.data){
                   $scope.supplierDetail = data.data;


               }
           }
       });
       //集群
       $scope.param={
    		   "appId":$stateParams.id,
    		   params:angular.toJson([{"param":{}}])

       }
   /*    httpLoad.loadData({
          url: '/openshift/cluster/listOpenshiftClusterByAppId',
         method: 'POST',
         data: $scope.param,
         noParam: true,
         success: function (data) {
           if (data.success) {
             $scope.masterData = data.data;

           }
         }
       });*/
       })();
       $scope.goBack = function(){
           $state.go('paas.application.instance');
       };
     }
 ]);
app.controller('applydeployCtrl', ['$rootScope','$timeout', '$scope','$state','httpLoad','$stateParams','LANGUAGE',function($rootScope,$timeout, $scope,$state,httpLoad,$stateParams,LANGUAGE) {
    $scope.envList = [{key:'',value:''}];
      $scope.labelList = [{keys:'',values:''}];
    $scope.storageList = [{keys:'',values:'/data'}];
      $scope.dependList = [{id:'',value:{}}];
      $scope.addData = {};
      $scope.store='';
      $scope.addData.tv_innerpath = "/data"
    $scope.name;
    $scope.addData.cpuRequest = 200;
    $scope.addData.memoryRequest = 64;
    $scope.addData.memoryLimit  = 128;
    $scope.addData.cpuLimit = 1000;
    $scope.appCount=1;
    $scope.scriptItemRepository = {};
    $scope.scriptItemRepository.selected={};
    $scope.scriptItemSpace = {};
    $scope.scriptItemSpace.selected="搜索一个项目";
    $scope.scriptItemImage = {};
    $scope.scriptItemImage.selected="";
    $scope.scriptItemVision = {};
    $scope.scriptItemVision.selected={};
    $scope.imageProp = true;
    $scope.vmListselect = {};
    $scope.loadBalanceListselect = {};
    $scope.vmListselect.selected={};
    $scope.loadBalanceListselect.selected={};
    $scope.dependvalue = true;
    $scope.dependstore = true;
    $scope.envType='auto';
    $scope.deployStatus='Deployment';
    //健康检查
    $scope.readinessProbe = {};
    $scope.livenessProbe = {};
    $scope.livenessProbe.type ='TCP'
    $scope.readinessProbe.type ='TCP'

   var id = $stateParams.id;
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
    }
    $scope.getOpreateList= [{values:''}];
    $scope.getOpreateFlag=true;
    $scope.getConfigList = function(){
        httpLoad.loadData({
            url: '/config/manage/config',
            method: 'GET',
            data: {
                simple :true,
                params:angular.toJson([{"param":{"appId":id},"sign":"EQ"}])
            },
            noParam: true,
            success: function (data) {
                if (data.success && data.data.rows) {
                    $scope.configList = data.data.rows;
                }
            }
        });
    }
    $scope.getConfigList();
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
    //项目的列表
    $scope.getRepository = function(obj){
        var params = {
        		  simple: true,
            params: angular.toJson([{"param": {"repository.id": obj.id}, "sign": "EQ"}])
        }
        httpLoad.loadData({
            url: '/image/list',
            method: 'POST',
            data: params,
            noParam: true,
            success: function (data) {
                if (data.success && data.data.rows) {
                	var rows = data.data.rows;
                	var arry = new Set()
                
                 	var arryList=[];
                	rows.forEach(function(item){
                		arry.add(item.namespace)
                	})
                	arry.forEach(function(item){arryList.push(item)})
                    $scope.scriptItemSpace.selected = '搜索一个项目';
                    $scope.dataTypeSpace = arryList

                }
            }
        });

    }
    //镜像的列表
    $scope.getSpaces = function(obj){
        var params = {
            simple: true,
            params: angular.toJson([{"param": {"repository.id": $scope.scriptItemRepository.selected.id,"repository_image_info.namespace":obj}, "sign": "EQ"}])
        }
        httpLoad.loadData({
            url: '/image/list',
            method: 'POST',
            data: params,
            noParam: true,
            success: function (data) {
                if (data.success && data.data.rows) {
                	var rows = data.data.rows;
                	var arry = new Set();
                	var arryList=[];
                	rows.forEach(function(item){
                		arry.add(item.name)
                	})
                arry.forEach(function(item){arryList.push(item)});
                    $scope.scriptItemImage.selected = '';
                    $scope.dataTypeImage = arryList;

                }
            }
        });

    }
    //版本的列表
    $scope.getImages = function(obj){
        var params = {
            simple: true,
            params: angular.toJson([{"param": {"repository.id": $scope.scriptItemRepository.selected.id,"repository_image_info.namespace":$scope.scriptItemSpace.selected,"image.name":obj}, "sign": "EQ"}])
        }
        httpLoad.loadData({
            url: '/image/list',
            method: 'POST',
            data: params,
            noParam: true,
            success: function (data) {
                if (data.success && data.data.rows) {
                    $scope.scriptItemVision.selected = '';
                    $scope.dataTypeVision = data.data.rows;

                }
            }
        });

    }
    //点击镜像 获取环境信息
    $scope.getEnvDetail = function(obj){
      if(obj.id){
        httpLoad.loadData({
               url: '/image/inspectImage',
               method: 'POST',
               data: {imageId:obj.id},
               success: function (data) {
                   if (data.success) {

                     $scope.totalMessage = data.data;
                     if($scope.totalMessage.exposedPort){
                         $scope.imageProp = false;
                         $scope.exposedPort = angular.fromJson($scope.totalMessage.exposedPort); //获取镜像接口得到的端口数据
                         $scope.getPortInfo();
                         $scope.envList = [];
                         var envlist = angular.fromJson($scope.totalMessage.env);
                         envlist.forEach(function(item){
                             var ary = item.split("=");
                             var obj =  {key:ary[0],value:ary[1]};
                             $scope.envList.push(obj);
                         })
                     }else{
                         $scope.pop('当前镜像内部无端口，无法进行服务部署，请重新选择','error');
                     }
                       $scope.imageId = obj.id;
                       obj.imageurl = (obj.repositoryType !=1) ?":"+obj.repositoryPort:'';
                       if(obj.namespace==""){
                           $scope.imageurl  =obj.repositoryAddress+obj.imageurl+  '/'+obj.name+':'+ obj.tag
                       }else{
                           $scope.imageurl  =obj.repositoryAddress+obj.imageurl+ '/'+obj.namespace+ '/'+obj.name+':'+ obj.tag
                       }
                     }


                   
               }
           });
      }

    }
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
      var data = {
          params: angular.toJson([{"param": {"envId": $scope.supplierDetail.envId, "status":"3"}, "sign": "EQ"}])
      }
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
    }
    $scope.getStorage = function(){
    	 var pvParam = {
    			  simple: true,
    	          params: angular.toJson([{"param": {"status": "Available", "envId": $scope.supplierDetail.envId}, "sign": "EQ"}])
    	      }
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
                        item.server = data.data.service;
                    } else {

                    }
                }
            });
    }


    $scope.initDatas=function (isSetHigh) {
    if(!isSetHigh){
        $scope.labelList = [{keys:'',values:''}];
        $scope.storageList = [{keys:'',values:''}];
        $scope.dependList = [{id:'',value:{}}];
        $scope.store='';
        $scope.addData.tv_innerpath = "/data"
        $scope.addData.cpuRequest = 200;
        $scope.addData.memoryRequest = 64;
        $scope.addData.memoryLimit  = 128;
        $scope.addData.cpuLimit = 1000;
        //健康检查
        $scope.livenessProbe = {};
        $scope.readinessProbe = {};
        $scope.vmListselect.selected.name="";
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
        	 if(!$scope.scriptItemVision.selected.id)$scope.pop('请先选择一个镜像版本,否则目标端口没有可选资源','error')
            break;
          case 4:

            $scope.getStorage();
            break;
          case 5:
            $scope.loabBalance='inner';
            $scope.getloadBalance();
            break;
          case 6:
              break;
          case 7:

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
    httpLoad.loadData({
        url: '/registry/list',
        method: 'POST',
        data: params,
        noParam: true,
        success: function (data) {
            if (data.success) {
                $scope.dataTypeRepository = data.data.rows;

            }
        }
    });
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
    $scope.checkName=function () {
        if(!$scope.serviceName)return
        httpLoad.loadData({
            url: '/service/existed',
            method: 'POST',
            data: {applicationId:$stateParams.id,name:$scope.serviceName},
          //  noParam: true,
            success: function (data) {
                if (!data.success) {

                    return false
                }else {
                    return true;
                }
            }
        });
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
             data: {applicationId:$stateParams.id,name:$scope.serviceName},
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
         if(!$scope.imageurl){   $scope.pop('请选择镜像','error');return false;  }
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
            /* $scope.portInfo.forEach(function (v) {
                 if(!v.nodePort||!v.optionsM) {
                     delete item[nodePort];
                     delete item[optionsM];
                 }
             })*/
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
         
        var id = $stateParams.id;
       var param = {
         applicationId:id,
         deployment:{
           name:$scope.serviceName,
             deployStatus:$scope.deployStatus,
             imageId:$scope.imageId,
            image:$scope.imageurl,
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
                    $state.go('paas.application.instancetiondetail', {id: $stateParams.id})
                  }
              }
          });
      }
}]);
app.controller('applygridinCtrl', ['$rootScope', '$scope','$state','httpLoad','$stateParams','LANGUAGE','$timeout',
                                 function($rootScope, $scope,$state,httpLoad,$stateParams,LANGUAGE,$timeout) {
	var params = {
	        simple: true
	}
	$scope.codemire = function(){
		 $scope.code = true;
	}
  $scope.scriptItem = {};

  $scope.scriptItem.selected ={};
	 httpLoad.loadData({
	        url: '/layout/list',
	        method: 'POST',
	        data: params,
	        noParam: true,
	        success: function (data) {
	            if (data.success) {
	                $scope.composeList = data.data.rows;
	            }
	        }
	    });
    $scope.layoutDeploy = function () {
        if (! $scope.scriptItem.selected.id) {
            $scope.pop("脚本内容不能为空");
            return
        }
   	 httpLoad.loadData({
 	        // url: '/application/layoutDeploy',
 	        url: '/service/deploy',
 	        method: 'POST',
 	        data: {
             // clusterId:$scope.clusterId,
                applicationId:$stateParams.id,
                layoutId:$scope.scriptItem.selected.id
           },

 	        success: function (data) {
 	            if (data.success) {
                	$scope.pop(data.message);
                $state.go('paas.application.instancetiondetail', {id: $stateParams.id});
 	            }
 	        }
 	    });
    }
	// 获取选择文件id然后通过接口获得文件内容
    $scope.codeMirror = {};
     $scope.showComposeFile = function(){
     	 httpLoad.loadData({
  	        url: '/layout/detail',
  	        method: 'GET',
  	        data: {id:$scope.scriptItem.selected.id},
  	      //  noParam: true,
  	        success: function (data) {
  	            if (data.success) {
  	            	 $scope.code = true;
  	              $scope.supplierDetail = data.data;
  	            $timeout(function () {
  	            	 $scope.codeMirror.codeMirror.setValue($scope.supplierDetail.fileContent);
  	          }, 100);
  	            }
  	        }
  	    });
     };

}]);
app.controller('applyStoreModalCtrl', ['$rootScope', '$scope','$state','httpLoad','$stateParams','LANGUAGE','$timeout',
                                 function($rootScope, $scope,$state,httpLoad,$stateParams,LANGUAGE,$timeout) {
  $rootScope.link = '/statics/css/image.css';//引入页面样式
     $scope.param = {
				name:""
			};

                                     //获取云主机列表
                                     $scope.getData = function (page,type) {
                                         $scope.param.page = page || $scope.param.page;
                                         httpLoad.loadData({
                                             url: '/application/store/list',
                                             method: 'GET',
                                             data: $scope.param,
                                             //noParam: true,
                                             success: function (data) {
                                                 if (data.success) {
                                                     $scope.countData = data.data;
                                                     for(var obj in $scope.countData){
                                                         $scope.countData[obj].forEach(function (item) {
                                                             if(!item.picturePath){item.picturPath="/statics/img/image/"+item.icon}else{
                                                            	 item.picturPath =window.location.protocol+'//'+window.location.host+'/picture'+item.picturePath.split('/tmp/application_store/picture')[1]+'/'+item.icon
                                                             }
                                                         })
                                                     }
                                                     $scope.totalCount = data.data.total;

                                                 }
                                             }
                                         });
                                     };

                                     $scope.getData(1);
                                     //详情
                                     $scope.detail = function(id){
                                         $state.go('paas.application.storedetail', {id: id})

                                     }

                                     //返回
                                     $scope.goAction = function (flag, row, $event) {
                                         switch (flag / 1) {
                                             case 1:
                                                 //新增
                                                 $state.go('paas.application.addmodule');

                                                 break;
                                             case 2:
                                                 //编辑
                                                 $state.go('paas.application.addmodule', {modelid: row.id,name:row.name,appId:$stateParams.id,type:row.deployType,envId:$scope.supplierDetail.envId});
                                                 break;
                                             case 3:
                                                 //删除
                                                 if($event) $event.stopPropagation();
                                                 var ids=[];
                                                 ids.push(id);
                                                 var modalInstance = $modal.open({
                                                     templateUrl : '/statics/tpl/application/store/remove.html',
                                                     controller : 'removemoduleModalCtrl',
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
                                             case 4:
                                                 //部署
                                                 // $state.go('paas.application.storegridin', {id: id})

                                                 break;
                                         }
                                     };
                                 }]);
