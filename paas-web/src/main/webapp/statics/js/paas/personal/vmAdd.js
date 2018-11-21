app.controller('vmAddMainPersonalCtrl', ['$rootScope', '$scope','$stateParams',function($rootScope, $scope,$stateParams) {
   $scope.type = $stateParams.type;
}]);
app.controller('vmAddVmPersonalCtrl', ['$rootScope', '$scope','$state','httpLoad','$stateParams','$modal',function($rootScope, $scope,$state,httpLoad,$stateParams,$modal) {
    $rootScope.link = '/statics/css/user.css';
    $rootScope.moduleTitle = '个人中心 > 申请服务 > 虚机创建';
    $scope.param = {
        rows: 10
    };
    $scope.showDetail = 1;
    $scope.showVmware=1;

    $scope.goBack = function(){
        $state.go('paas.personal.applyservice');
    };

    (function(){
        $scope.addData = {};$scope.param={};
        $scope.isShowTemplate = true;
        //获取数据存储数据
        $scope.getDatastore = function(vhostId){
            var params = {
                    simple : true
                },
                searchParam = [{"param":{"vendorId":$stateParams.id,"vhostId":vhostId},"sign":"EQ"}];
            params.params = JSON.stringify(searchParam);
            httpLoad.loadData({
                url:'/datastore/list',
                method: 'POST',
                data:params,
                //data: {"id":$stateParams.id,"vdcId":vdcId,"clusterId":clusterId,"vhostId":vhostId},
                noParam: true,
                success:function(data){
                    if(data.success){
                        $scope.datastoreData = data.data.rows;
                    }
                }
            });
        };
        //获取目标主机数据
        $scope.getVhost = function(id,from){
            var searchParam = [];
            if(from=='vdc'){
                searchParam = [{"param":{"vendorId":$stateParams.id,"vdcId":id},"sign":"EQ"}]
            }else if(from=='cluster'){
                searchParam = [{"param":{"vendorId":$stateParams.id,"clusterId":id},"sign":"EQ"}]
            }
            var params = {
                simple : true
            };
            params.params = JSON.stringify(searchParam);
            httpLoad.loadData({
                url:'/vhost/list',
                method: 'POST',
                data:params,
                //data: {"id":$stateParams.id,"vdcId":vdcId,"clusterId":clusterId},
                noParam: true,
                success:function(data){
                    if(data.success){
                        $scope.vhostData = data.data.rows;
                    }
                }
            });
        };
        //获取集群数据
        $scope.getCluster = function(vdcId){
            var params = {
                    simple : true
                },
                searchParam = [{"param":{"vendorId":$stateParams.id,"vdcId":vdcId},"sign":"EQ"}];
            params.params = JSON.stringify(searchParam);
            httpLoad.loadData({
                url:'/cluster/list',
                method: 'POST',
                data:params,
                //data: {"id":$stateParams.id,"vdcId":vdcId},
                noParam: true,
                success:function(data){
                    if(data.success){
                        $scope.clusterData = data.data.rows;
                    }
                }
            });
        };
        //获取数据中心数据
        $scope.getVdc = function(){
            var params = {
                    simple : true
                },
                searchParam = [{"param":{"vendorId":$stateParams.id},"sign":"EQ"}];
            params.params = JSON.stringify(searchParam);
            httpLoad.loadData({
                url:'/vdc/list',
                method: 'POST',
                data:params,
                //data: {"id":$stateParams.id},
                noParam: true,
                success:function(data){
                    if(data.success){
                        $scope.vdcData = data.data.rows;
                    }
                }
            });
        };
        if($scope.showVmware==1) $scope.getVdc();
        //选择数据中心
        $scope.chooseVdc = function(item){
            $scope.vdcData.forEach(function(data){
                data.isVdcActive = false;
                if(data.id==item.id){
                    data.isVdcActive = !item.isVdcActive;
                }
                if(data.isVdcActive){
                    $scope.vdcId = data.id;
                    $scope.clusterId = '';$scope.addData.hostname = '';$scope.addData.datastore = '';$scope.datastoreData = '';
                    $scope.getCluster($scope.vdcId);
                    $scope.getVhost($scope.vdcId,'vdc');
                    return;
                }
            });
        };
        //选择集群
        $scope.chooseCluster = function(item){
            $scope.clusterData.forEach(function(data){
                data.isClusterActive = false;
                if(data.id==item.id){
                    data.isClusterActive = !item.isClusterActive;
                }
                if(data.isClusterActive){
                    $scope.clusterId = data.id;
                    $scope.addData.hostname = '';$scope.addData.datastore = '';$scope.datastoreData = '';
                    $scope.getVhost($scope.clusterId,'cluster');
                    return;
                }
            });
        };
        //选择目标主机
        $scope.chooseVhost = function(item){
            $scope.vhostData.forEach(function(data){
                data.isVhostActive = false;
                if(data.id==item.id){
                    data.isVhostActive = !item.isVhostActive;
                }
                if(data.isVhostActive){
                    $scope.addData.hostname = data.name;
                    $scope.addData.hostId = data.id;
                    $scope.addData.datastore = '';$scope.datastoreData = '';
                    $scope.getDatastore(data.id);
                    return;
                }
            });
        };
        //选择数据存储
        $scope.chooseDatastore = function(item){
            $scope.datastoreData.forEach(function(data){
                data.isDatastoreActive = false;
                if(data.id==item.id){
                    data.isDatastoreActive = !item.isDatastoreActive;
                }
                if(data.isDatastoreActive){
                    $scope.addData.datastore = data.name;
                    $scope.addData.storeId = data.id;
                    return;
                }
            });
        };
        //获取模板数据
        $scope.getTemplateData = function(){
            var params = {
                    simple : true
                },
                searchParam = [{"param":{"vendorId":$stateParams.id,"isTemplate":true,"status":"STOPPED"},"sign":"EQ"}];
            params.params = JSON.stringify(searchParam);
            httpLoad.loadData({
                url:'/vm/list',
                method:'POST',
                data: params,
                noParam: true,
                success:function(data){
                    if(data.success){
                        $scope.templateList = data.data.rows;
                    }
                }
            });
        };
        if($scope.isShowTemplate == true){
            if($scope.showVmware==1) $scope.getTemplateData();
        }
        //检验CPU，内存，磁盘的大小
        $scope.check = function(action,value,min,max){
            var number = parseInt(value);
            if(action==0) var name = 'CPU'+'范围是'+min+'-'+max+','+'请重新输入';
            if(action==1) var name = '内存'+'范围是'+min+'-'+max+','+'请重新输入';
            if(action==2) var name = '磁盘'+'范围是'+min+'-'+max+','+'请重新输入';
            if(value<min||value>max){
                $scope.pop(name,'error');
                return;
            }
        };

        $scope.ok = function(){
            //云供应商的ID
            $scope.addData.id = $stateParams.id;
            if(!$scope.addData.templateId){
                $scope.pop('请选择虚机模板','error');
                return;
            }else{
                $scope.templateList.forEach(function(data){
                    if(data.id==$scope.addData.templateId){
                        $scope.addData.template = data.name;
                    }
                });
            }
            if(!$scope.addData.hostname&&$scope.addData.hostname==''){
                $scope.pop('请选择目标主机','error');
                return;
            }
            if(!$scope.addData.datastore&&$scope.addData.datastore==''){
                $scope.pop('请选择数据存储','error');
                return;
            }
            httpLoad.loadData({
                url:'/applyRecord/create',
                method:'POST',
                data: {
                    catalog:'VMWARE',
                    target:$stateParams.id,
                    params:$scope.addData
                },
                success:function(data){
                    if(data.success){
                        $scope.pop(data.message);
                        $scope.goBack();
                    }
                }
            });
        };
        $scope.cancel = function(){
            $scope.goBack();
        };
    })();
}]);
app.controller('vmAddOpPersonalCtrl', ['$rootScope', '$scope','$state','httpLoad','$stateParams','$modal',function($rootScope, $scope,$state,httpLoad,$stateParams,$modal) {
    $rootScope.link = '/statics/css/user.css';
    $rootScope.moduleTitle = '个人中心 > 申请服务 > 虚机创建';
    $scope.param = {
        rows: 10
    };
    $scope.showDetail = 1;
    $scope.showOpenstack=1;
    
    $scope.goBack = function(){
        $state.go('paas.personal.applyservice');
    };
    
    (function(){
        $scope.addData = {};$scope.param={};$scope.isselectSize = false;
        $scope.account = 'root';
        //获取地域数据
        $scope.getRegion = function(){
            httpLoad.loadData({
                url:'/region/list',
                method: 'POST',
                data: {"id":$stateParams.id},
                success:function(data){
                    if(data.success){
                        $scope.regionData = data.data;
                        angular.forEach($scope.regionData, function(data,index){
                            //默认选中第一个区域
                            if(index==0){
                                data.isRegionActive = true;
                                $scope.addData.region = data.id;
                            }
                            else data.isRegionActive = false;
                        });
                    }
                }
            });
        };
        if($scope.showOpenstack==1) $scope.getRegion();
        //选择地域
        $scope.chooseRegion1 = function(item){
            angular.forEach($scope.regionData, function(data,index){
                data.isRegionActive = false;
                if(data.id==item.id){
                    data.isRegionActive = !item.isRegionActive;
                }
                if(data.isRegionActive){
                    $scope.addData.region = data.id;
                }
            });
        };
        //获取镜像数据
        $scope.getImageData = function(){
            var params = {
                  simple : true
              },
              searchParam = [{"param":{"vendorId":$stateParams.id},"sign":"EQ"}];
            params.params = JSON.stringify(searchParam);
            httpLoad.loadData({
                url:'/image/list',
                method:'POST',
                data: params,
                noParam: true,
                success:function(data){
                    if(data.success){
                        $scope.imageList = data.data.rows;
                    }
                }
            });
        };
        if($scope.showOpenstack==1) $scope.getImageData();
        //获取网络数据
        $scope.getNetworkData = function(){
            var params = {
                  simple : true
              },
              searchParam = [{"param":{"vendorId":$stateParams.id},"sign":"EQ"}];
            params.params = JSON.stringify(searchParam);
            httpLoad.loadData({
                url:'/network/list',
                method:'POST',
                data: params,
                noParam: true,
                success:function(data){
                    if(data.success){
                        $scope.networkList = data.data.rows;
                        angular.forEach($scope.networkList, function(data,index){
                            //默认选中第一个
                            if(index==0){
                                data.isNetworkActive = true;
                                $scope.addData.networkId = data.id;
                            }
                            else data.isNetworkActive = false;
                        });
                    }
                }
            });
        };
        if($scope.showOpenstack==1) $scope.getNetworkData();
        //获取配置实例数据
        $scope.getSizeData = function(){
            var params = {
                  simple : true
              },
              searchParam = [{"param":{"vendorId":$stateParams.id},"sign":"EQ"}];
            params.params = JSON.stringify(searchParam);
            httpLoad.loadData({
                url:'/flavor/list',
                method: 'POST',
                data: params,
                noParam: true,
                success:function(data){
                    if(data.success){
                        $scope.sizeList = data.data.rows;
                    }
                }
            });
        };
        if($scope.showOpenstack==1) $scope.getSizeData();
        //选择CPU核数和内存大小
        $scope.slider = {
            options: {
                showTicks: true,
                readOnly: true,
                stepsArray : [
                    {value:"1"},
                    {value:"2"},
                    {value:"4"},
                    {value:"8"},
                    {value:"16"}
                ],
                translate: function(value) {
                    return value+'核';
                },
                onChange : function(sliderId, modelValue, highValue, pointerType){
                    var str = modelValue;
                    $scope.cpu = str.substring(0,str.length-1);
                }
            }
        };
        $scope.slider1 = {
            options: {
                showTicks: true,
                readOnly: true,
                stepsArray : [
                    {value: 0.5},
                    {value: 2},
                    {value: 4},
                    {value: 6},
                    {value: 8},
                    {value: 16}
                ],
                translate: function(value) {
                    return value+'G';
                },
                onChange : function(sliderId, modelValue, highValue, pointerType){
                    //var str = modelValue;
                    //$scope.memory = str.substring(0,str.length-1);
                }
            }
        };
        
        //选择可用区域
        $scope.chooseRegion2 = function(item){
            angular.forEach($scope.regionData, function(data,index){
                data.isRegionActive = false;
                if(data.id==item.id){
                    data.isRegionActive = !item.isRegionActive;
                }
                if(data.isRegionActive){
                    $scope.addData.region = data.id;
                }
            });
        };
        //选择镜像
        //$scope.selectImage = function(){
        //    if($scope.addData.imageId==undefined) return;
        //    angular.forEach($scope.imageList, function(data,index){
        //        if(data.id==$scope.addData.imageId){
        //            //$scope.addData.osName = data.value;
        //            //$scope.addData.osVersion = data.osVersion;
        //        }
        //    });
        //};
        //选择网络
        $scope.chooseNetwork = function(item,$index){
            angular.forEach($scope.networkList, function(data,index){
                if(data.id==item.id){
                    data.isNetworkActive = !item.isNetworkActive;
                }
            });
        };
        //选择配置实例
        $scope.selectSize = function(row){
            if(row==undefined) return;
            if(row!=""){
                var data = JSON.parse(row);
                $scope.addData.flavorId = data.flavorId;
                $scope.disk = data.disk+'';
                $scope.slider.value = data.cpu+'';$scope.cpu = data.cpu+'';
                $scope.slider1.value = data.memory/1024;$scope.memory = data.memory;
                $scope.isselectSize = true;$scope.slider.options.readOnly = true;$scope.slider1.options.readOnly = true;
            }
        };
        $scope.ok = function(){
            //云供应商的ID
            $scope.addData.id = $stateParams.id;
            //处理网络数据
            $scope.addData.networkId = '';
            angular.forEach($scope.networkList, function(data,index){
                if(data.isNetworkActive){
                    $scope.addData.networkId = $scope.addData.networkId + ',' + data.value;
                }
            });
            $scope.addData.networkId = $scope.addData.networkId.substr(1);
            if(!$scope.addData.region){
                $scope.pop('请选择地域','error');
                return;
            }
            if(!$scope.addData.imageId){
                $scope.pop('请选择镜像','error');
                return;
            }
            if($scope.addData.networkId==''){
                $scope.pop('请选择网络','error');
                return;
            }
            if(!$scope.addData.flavorId){
                $scope.pop('请选择配置实例','error');
                return;
            }
            httpLoad.loadData({
                url:'/applyRecord/create',
                method:'POST',
                data: {
                    catalog:'OPENSTACK',
                    target:$stateParams.id,
                    params:$scope.addData
                },
                success:function(data){
                    if(data.success){
                        $scope.pop(data.message);
                        $scope.goBack();
                    }
                }
            });
        };
        $scope.cancel = function(){
            $scope.goBack();
        };
    })();
}]);