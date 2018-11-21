app.controller('applyDetailModalCtrl', ['$scope', 'httpLoad', '$rootScope', '$modal', '$state', '$timeout', '$stateParams', '$sce',
    function ($scope, httpLoad, $rootScope, $modal, $state, $timeout, $stateParams, $sce) {

        $rootScope.moduleTitle = $sce.trustAsHtml('应用服务 > <span>应用实例</span> > 详情');

        $scope.active1 = '';
        ///
        (function () {
            var id = $stateParams.id;
            //  获取应用详情
            httpLoad.loadData({
                url: '/application/detail',
                method: 'GET',
                data: {id: id},
                success: function (data) {
                    if (data.success && data.data) {
                        $scope.supplierDetail = data.data;
                        $scope.showDetail = $scope.isActive = true;

                    }
                }
            });
        })();
        $scope.deploy = function(){
            $state.go('paas.application.instancetiondeploy', {id: $stateParams.id})
        }
        $scope.goBack = function () {
            $state.go('paas.application.instance');
        };
    }
]);
app.controller('applyopenstackModalCtrl', ['$rootScope', '$interval', 'webSocket', '$modal', '$timeout', '$scope', '$state', 'httpLoad', 'LANGUAGE', '$stateParams',
    function ($rootScope, $interval, webSocket, $modal, $timeout, $scope, $state, httpLoad, LANGUAGE, $stateParams) {
        var socket;
        var pageType;
        $scope.alarmStatusData={0:"正常",1:"告警",2:"无策略"};
        $scope.options = {
            theme:'sd',
            unit:''
        }


        $scope.param = {
            rows: 10,
            page: 1
        };

        //  websocket异步操作
        webSocket.onmessage({
            message: function (data) {
                if ($rootScope.currentUrl == 'paas.application.instancetiondetail') {
                    $scope.subSearch($scope.tableNameSign);
	  				$scope.search(1);

                }
            }
        });
        $scope.$on("$destroy", function(event) {
            $interval.cancel($scope.timeInterval);
            $interval.cancel($scope.timeIntervalApply);
            $scope.timeInterval = '';
            $scope.timeIntervalApply = '';
        });
        //获取服务信息
        $scope.search = function (page) {
            $scope.listData=[];
            $scope.param.page = page || $scope.param.page;
            $scope.param.params = JSON.stringify({"applicationId": $stateParams.id})
            httpLoad.loadData({
                url: '/service/list',
                // url: '/app/statistic/list',
                method: 'POST',
                data: {params: $scope.param.params},
                // data: $scope.param,
                noParam: true,
                success: function (data) {
                    if (data.success) {
                        //处理数据
                        $scope.ingressItems = data.data.ingress.items;
                        $scope.serviceItems = data.data.service;
                        $scope.deployment = data.data.deployment;
                        var selectParam;
                        if ($scope.serviceItems&&$scope.serviceItems.length >= 1) {
                            $scope.serviceItems.forEach(function (item) {
                                var mydata = {};
                                selectParam = item.metadata.name;
                                mydata.name = selectParam;
                                mydata.alarmStatus=item.alarmStatus;
                                mydata.creationTimestamp = item.metadata.creationTimestamp;
                                mydata.namespace = item.metadata.namespace;
                                mydata.port=[];
                                mydata.nodePort=[];
                                mydata.portStr="";
                                mydata.nodePortStr="";
                                item.spec.ports.forEach(function (v) {
                                    mydata.port = v.port;
                                    mydata.portStr+=v.port+"、";
                                    if(v.nodePort){
                                        mydata.nodePort = v.nodePort;
                                        mydata.nodePortStr+=v.nodePort+"、";
                                    }
                                })
                                if(mydata.portStr)mydata.portStr=mydata.portStr.substring(0,mydata.portStr.length-1);
                                if(mydata.nodePortStr)mydata.nodePortStr=mydata.nodePortStr.substring(0,mydata.nodePortStr.length-1);
                                mydata.clusterIP = item.spec.clusterIP;
                                mydata.selector = item.spec.selector;
                                if(  $scope.ingressItems.length>=1){
                                    $scope.ingressItems[0].spec.rules.forEach(function (item1) {
                                        if (item1.http.paths[0].backend.serviceName == selectParam) {
                                            mydata.path = item1.http.paths[0].path;
                                            var list = $scope.ingressItems[0].status.loadBalancer.ingress;
                                            if(list.length >= 1){
                                            	 mydata.ip = "https://"+list[0].ip+mydata.path;
                                            }
                                        }
                                    })
                                }

                                $scope.deployment.forEach(function (item2) {
                                    item2.items.forEach(function (item3) {
                                        if (item3.metadata.name == selectParam) {
                                            mydata.replicas = item3.spec.replicas;
                                            mydata.image = item3.spec.template.spec.containers[0].image;
                                        }
                                    })
                                })
                                $scope.listData.push(mydata);
                            })
                        }

                        $scope.total=$scope.listData.length;
                        if ($scope.listData.length>=1) {
                            $scope.isImageData = false;
                        } else {
                            $scope.isImageData = true;
                        }
                    }
                }
            });
        };

        //获取job列表信息
        $scope.getJob = function () {
            $scope.jobListData=[];
            httpLoad.loadData({
                url: '/service/list/job',
                method: 'POST',
                data: {"applicationId":$stateParams.id},
                success: function (data) {
                    if (data.success) {
                        $scope.jobListData=data.data;
                        $scope.jobListData1=[];
                        $scope.jobTotal=$scope.jobListData.length;
                        if ($scope.jobListData.length>=1) {
                            $scope.jobListData.forEach(function (item) {
                                $scope.jobListData1.push({
                                    name:item.metadata.name,
                                    namespace:item.metadata.namespace,
                                    completions:item.spec.completions,
                                    succeeded:item.status.succeeded||0,
                                    creationTimestamp:item.metadata.creationTimestamp,
                                    selector:item.spec.template.metadata.labels
                                })
                            })
                            $scope.isJobData = false;
                        } else {
                            $scope.isJobData = true;
                        }
                    }
                }
            });
        };
        //应用监控
        $scope.monitoredApply = function () {
            $scope.performanceApply =  $scope.monitorDeta;
            var paramObj = {
                type:'application',
                appId:$stateParams.id
            };
            httpLoad.loadData({
                url: '/application/monitor',
                method: 'GET',
                data:paramObj,
                //noParam:true,
                success: function(data){
                    if(data.success) {

                        $scope.performanceApply = data.data;
                        $scope.timeIntervalApply = $interval(function () {
                            $scope.monitoredApply();
                        }, 120 * 1000);
                    }else if($scope.timeIntervalApply){
                        $interval.cancel($scope.timeIntervalApply);
                        $scope.timeIntervalApply = '';
                    }
                }
            });

        }

        //子表格
        $scope.subParam = {
            rows: 10
        };
        $scope.subSearch = function (row,page,type) {
            $scope.subParam =  { "appId": $stateParams.id,"labels": row.selector}
            httpLoad.loadData({
                url: '/container/list',
                method: 'POST',
                data: $scope.subParam,
                //  noParam: true,
                success: function (data) {
                    if (data.success) {
                        $scope.sublistData = data.data;
                        $scope.subtotal = data.data.length;
                        if(!$scope.subtotal){
                            $scope.subisImageData = true;
                        } else {
                            $scope.subisImageData = false;
                        }
                    }
                }
            });
        };
        $scope.topology = function () {
            //拓扑
            httpLoad.loadData({
                url: '/application/topology',
                method: 'GET',
                data: {applicationId:$stateParams.id},
                success: function (data) {
                    if (data.success) {
                        $scope.itemData = data.data;
                        $scope.nodeData =  $scope.itemData;

                    }
                }
            });
        }
        //详情
        $scope.goBack = function () {
            $scope.isActive = false;
            $timeout(function () {
                $scope.showDetail = false;
                //终止监测
                $interval.cancel($scope.timeInterval);
                $interval.cancel($scope.timeIntervalApply);
                $scope.timeIntervalApply = '';
                $scope.timeInterval = '';
            }, 200);
            if (socket) {
                socket.close();
            }

            $scope.showUp = false
        };
        //监控的初始数据
        $scope.monitorDeta ={
            "NETWORK": {
                "keys": [],
                "values": [
                    {
                        "data": [	0,0,0,0,0,0,0,0,0,0,0,0,0,0,0],
                        "name": "Send"
                    },
                    {
                        "data": [	0,0,0,0,0,0,0,0,0,0,0,0,0,0,0],
                        "name": "Received"
                    }
                ]
            },
            "MEMORY": {
                "keys": [],
                "values": [
                    {
                        "data": [	0,0,0,0,0,0,0,0,0,0,0,0,0,0,0],
                        "name": "Memory"
                    }
                ]
            },
            "CPU": {
                "keys": [],
                "values": [
                    {
                        "data": [	0,0,0,0,0,0,0,0,0,0,0,0,0,0,0],
                        "name": "CPU"
                    }
                ]
            }
        } ;
        ////滚动
        $scope.options = {
            bucketDuration: 0,
            cpu: {
                theme: 'Cpu',
                unit: 'Millicores'
            },
            memorynode: {
                theme: 'Memory',
                unit: 'Gib'
            },
            memorypod: {
                theme: 'Memory',
                unit: 'MiB'
            },
            network: {
                theme: 'Network',
                unit: 'KiB/s'
            },
        }
        $scope.paramObj = {
            resourceName: '',
            namespace: ''
        };
        $scope.timeTypeData = [
            {
                name: 'Last hour',
                value: 0
            }, {
                name: 'Last 4 hour',
                value: 1
            }, {
                name: 'Last day',
                value: 2
            }, {
                name: 'Last 3 day',
                value: 3
            }, {
                name: 'Last week',
                value: 4
            }
        ]
        $scope.KubernetesData = {
            row: '',
            state: ''
        }
        var bucketDuration = [60, 60, 60, 60, 60]
        var startTime = ['1h', '4h', '1d', '3d', '1w'];
        $scope.clickTimes=1;
        $scope.tableNameSign={};
        //子表格展示
        $scope.goDetail = function (row, type, $event) {
            // console.log($scope.tableNameSign+row.name)
            // console.log(row);
            if($scope.tableNameSign.name==row.name){
                $scope.clickTimes++;
                $scope.tableName=""; //是否显示运行实例列表
                if($scope.clickTimes==3){
                    $scope.clickTimes=1;
                    $scope.tableName=row.name;
                    $scope.subSearch(row);
                }
            }else{
                $scope.clickTimes=1;
                $scope.tableName=row.name;
                $scope.subSearch(row);
            }
            $scope.tableNameSign=row;
        }
        $scope.detailAction = function (flag, row, $event) {
        	switch (flag / 1) {
        	case 1:
				//新增

        		$state.go('paas.application.configManageDetail', {id: row.id})
					break;
        	}
        }
        //跳转详情页
        $scope.detail = function (row, type, $event,flag) {
            // console.log("parent");
            $scope.KubernetesData.row = row;
            $scope.paramObj.resourceName = row.name;
            $scope.paramObj.namespace = row.namespace;
            var params={
                "applicationId":$stateParams.id,
                "name":row.name,
                "resourceType": type,
                "namespace": row.namespace,
                "resourceName": row.name,
                id: $stateParams.envId
            }
            switch (type) {
                case 'Master':
                    $scope.showDetail = 1;
                    break;
                case 'Node':
                    $scope.showDetail = 2;
                    var data = {};
                    data.params = angular.toJson([{"param": {}}])
                    // httpLoad.loadData({
                    //     url: '/host/list',
                    //     method: 'POST',
                    //     data: {
                    //     	   params: angular.toJson([{"param": {"ip": row.name,"envId":$stateParams.envId}, "sign": "EQ"}])
                    //     },
                    //     noParam: true,
                    //     success: function (data) {
                    //         if (data.success && data.data) {
                    //             $scope.serviceDetail= data.data.rows[0];
                    //             $state.go('paas.environment.hostDetail', {id:$scope.serviceDetail.id,hostName:row.name,envId:$stateParams.envId})
                    //         }
                    //     }
                    // });
                 
                    break;
                case 'ReplicationController':
                    $scope.showDetail = 3;
                    break;
                case 'Service':
                    $scope.showDetail = 4;

                    httpLoad.loadData({
                        url: '/service/detail',
                        method: 'POST',
                        data: params,
                        success: function (data) {
                            if (data.success && data.data) {
                                $scope.serviceDetail= row;

                            }
                        }
                    });


                    break;
                case 'Job':
                    $scope.showDetail = 7;
                    var params1={
                        "appId":$stateParams.id,
                        "resourceName": row.name,
                        "resourceType": "Job"

                    }
                    httpLoad.loadData({
                        url: '/container/detail',
                        method: 'POST',
                        data: params1,
                        success: function (data) {
                            if (data.success && data.data) {
                                $scope.jobDetail= data.data;

                            }
                        }
                    });


                    break;
                case 'Pod':
                    $scope.pageNum = 100;
                	$scope.Teminal=false;
                    var params2={
                        "appId":$stateParams.id,
                        "resourceName": row.name,
                        "resourceType": "Pod"

                    }
                    $scope.showDetail = 5;
                    httpLoad.loadData({
                        url: '/container/detail',
                        // url: '/service/detail',
                        method: 'POST',
                        data: params2,
                        success: function (data) {
                            if (data.success && data.data) {
                                $scope.supplierDetail = data.data;
                                if (type == 'Pod') {
                                    $scope.Status = $scope.supplierDetail.Status[0];
                                    $scope.Container = $scope.supplierDetail.Container;
                                    $scope.Template = $scope.supplierDetail.Template;
                                    $scope.Volumes = $scope.supplierDetail.Volumes;
                                    $scope.getpodJson(row.name)
                                    for (var i = 0; i < $scope.Container.length; i++) {
                                        var item = $scope.Container[i]
                                        if (item.state == 'running') {
                                            $scope.KubernetesData.state = true;
                                            $scope.KubernetesData.states = false;
                                            $scope.options.bucketContainer = item.containerName;
                                            $scope.options.bucketT = item.containerName;
                                            $scope.options.bucketL = item.containerName;
                                            $scope.getLog();
                                            if (!$scope.KubernetesData.states) {
                                                $scope.prompts = true;
                                              
                                            } else {
                                                $scope.prompts = false
                                            }
                                            $scope.chackIn(type, row)
                                            setTimeout(function () {
                                                if (!$('.kuberb option').eq(0).html()) {
                                                    $('.kubera option').eq(0).remove()
                                                    $('.kuberb option').eq(0).remove()
                                                    $('.kuberc option').eq(0).remove()
                                                }
                                            }, 100)
                                            return
                                        } else if (item.state == 'Completed') {
                                            $scope.options.bucketContainer = item.containerName;
                                            $scope.options.bucketT = item.containerName
                                            $scope.options.bucketL = item.containerName
                                            $scope.KubernetesData.state = true
                                            $scope.KubernetesData.states = true
                                            $scope.getLog();
                                            if (!$scope.KubernetesData.states) {
                                                $scope.prompts = true;
                                              
                                            } else {
                                                $scope.prompts = false
                                            }
                                            $scope.chackIn(type, row)
                                            setTimeout(function () {
                                                if (!$('.kuberb option').eq(0).html()) {
                                                    $('.kubera option').eq(0).remove()
                                                    $('.kuberb option').eq(0).remove()
                                                    $('.kuberc option').eq(0).remove()
                                                }
                                            }, 100)
                                            return
                                        } else {
                                            $scope.options.bucketContainer = $scope.Container[0].containerName;
                                            $scope.options.bucketT = $scope.Container[0].containerName
                                            $scope.options.bucketL = $scope.Container[0].containerName
                                            $scope.KubernetesData.state = false
                                            $scope.KubernetesData.states = true;
                                        }
                                    }
                                    setTimeout(function () {
                                        if (!$('.kuberb option').eq(0).html()) {
                                            $('.kubera option').eq(0).remove()
                                            $('.kuberb option').eq(0).remove()
                                            $('.kuberc option').eq(0).remove()
                                        }
                                    }, 100)
                                    $scope.getLog();
                                    if (!$scope.KubernetesData.states) {
                                        $scope.prompts = true;
                                        
                                    } else {
                                        $scope.prompts = false
                                    }
                                    $scope.chackIn(type, row);
                                }

                            }
                        }
                    });

                    break;
                case 'HorizontalPodAutoscaler':
                    $scope.showDetail = 6;
                    break;

            }
            $event.stopPropagation();
            $scope.isActive = true;
            var paramEvent={
                "appId":$stateParams.id,
                "resourceName": row.name

            }

            //事
            if (type != 'Master' && type != 'Node') {
                httpLoad.loadData({
                    url: '/container/event',
                    method: 'POST',
                    data: paramEvent,
                    success: function (data) {
                        if (data.success) {
                            $scope.isDataLoad = true;
                            $scope.listEventData = data.data;
                            $scope.totalEvent = data.data.length;
                            if (!$scope.totalEvent) {
                                $scope.isImageDatad = true;
                            } else {
                                $scope.isImageDatad = false;
                            }
                        }
                    }
                });
            }

        };
        //终端
        $scope.getTeminal = function () {
            var row = $scope.KubernetesData.row;
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
        //服务json
        $scope.getJson = function (row,type) {
            if(type==0){
                $scope.serviceItems.forEach(function(item){
                    if(item.metadata.name == row){
                        $('#json-renderer').jsonViewer(item);

                    }
                })
            }else if(type == 1){
                $scope.deployment.forEach(function (item2) {
                    item2.items.forEach(function (item3) {
                        if (item3.metadata.name == row) {
                            $('#json-rendererpod').jsonViewer(item3);
                        }
                    })
                })

            }else if(type==2){
                $('#json-rendererpod2').jsonViewer(row);
            }

        }
        //实例json
        $scope.getpodJson = function (row) {
            var params = {
                name: row,
                applicationId: $stateParams.id
            }
            httpLoad.loadData({
                url: '/container/template',
                method: 'GET',
                data: params,
                success: function (data) {
                    if (data.data != null) {
                        $('#json-pod-renderer').jsonViewer(data.data);


                    } else {

                    }
                }
            });

        }
        //鏡像依賴/service/list
        $scope.getImage = function (row,type) {
            var Arrey =[];
            if(type==0){
                $scope.deployment.forEach(function(item){
                    item.items.forEach(function(items){
                        if(items.metadata.name == row){
                            items.spec.template.spec.containers.forEach(function(con){
                                var cImg = con.image;
                                Arrey.push(cImg)
                                //if(cImg.split("/").length ==3){
                                // var name = cImg.split("/")[0],
                                // tag = cImg.split("/")[1],
                                // address = cImg.split("/")[2];
                                // var obj={"image.name":address.split(':')[0],"image.tag":address.split(':')[1],"repository_image_info.namespace":tag, "repository.address":name.split(':')[0]};
                                // var param = {"param":obj,"sign": "EQ"}
                                //Arrey.push(param)
                                //}

                            })

                        }
                    })

                })
            }else if(type==1){
                row.spec.template.spec.containers.forEach(function (item) {
                    Arrey.push(item.image);
                })
            }
            var param = {
                images:	Arrey
            }

            httpLoad.loadData({
                url: '/service/list',
                method: 'GET',
                data: {images:Arrey},
                // noParam:true,
                success: function (data) {
                    if (data.data != null) {
                        $scope.countData = data.data;
                    } else {

                    }
                }
            });
        }
        //镜像详情

        $scope.imageDetail = function (id) {
            $state.go('paas.repository.imagedetail', {id: id})
        }
        //服务host
        $scope.getHost = function (serviceDetail) {
            var params = {
                labels: serviceDetail.selector||serviceDetail.labels,
                applicationId: $stateParams.id
            }
            httpLoad.loadData({
                url: '/service/host',
                method: 'GET',
                data: params,
                success: function (data) {
                    if (data.data != null) {
                        $scope.hostlistData = data.data;
                        $scope.hostlistData.forEach(function(item){
                            var status =  item.status.split(',');
                            item.statusObj={};
                            if(status[0].split('=')[1] == "True"){
                                item.statusObj.statusA = "正常"
                            }else {
                                item.statusObj.statusA = "异常"
                            }
                            if(status[1].split('=')[1] == "True"){
                                item.statusObj.statusB = "不可调度"
                            }else {
                                item.statusObj.statusB = "可调度"
                            }
                        })
                        $scope.hosttotal = data.data.length;
                    } else {

                    }
                }
            });
        }
        //配置管理
        $scope.getconfig = function (serviceDetail,type) {
            var params = {
                name: serviceDetail.name,
                applicationId: $stateParams.id,
                resourceType: type
            }
            httpLoad.loadData({
                url: '/service/config',
                method: 'GET',
                data: params,
                success: function (data) {
                    if (data.data != null) {
                        $scope.configlistData = data.data;
                    
                        $scope.configtotal = data.data.length;
                    } else {

                    }
                }
            });
        }
        //日志
        $scope.getLog = function (page) {
            var row = $scope.KubernetesData.row
            var params = {

                "appId": $stateParams.id,
                "resourceName": row.name,
                'containerName': $scope.options.bucketL,
                'status': row.status,
                'line':page||100,

            }
            httpLoad.loadData({
                url: '/container/log',
                method: 'GET',
                data: params,
                success: function (data) {
                    if (data.data == null) {
                        $scope.logPod = data.data;
                    } else {
                        $scope.logPod = data.data.replace(/\n/g, '<br>');
                    }
                }
            });
        }

        $scope.showUps = function () {
            $scope.showUp = true
        }
        $scope.chackIn = function (type, row) {
            if (type != 'Pod') {
                return
            }
            if (type == 'Pod' && !$scope.KubernetesData.state) {
                $scope.prompt = false;
                return
            } else {
                $scope.prompt = true;
            }
            $scope.monitored();
            $scope.timeInterval = $interval(function () {
                $scope.monitored();
            }, 120 * 1000);
        }
        //监控
        $scope.monitored = function () {
            var paramObj = {
                type: 'pod',
                startTime: startTime[$scope.options.bucketDuration],
                bucketDuration: bucketDuration[$scope.options.bucketDuration],
                resourceName: $scope.paramObj.resourceName,
                namespace: $scope.paramObj.namespace,
                appId:$stateParams.id,
                id: $stateParams.clusterId
            };
            httpLoad.loadData({
                url: '/application/monitor',
                method: 'GET',
                data: paramObj,
                //  noParam:true,
                success: function (data) {
                    if (data.success) {
                        $scope.time = data.data.CPU.keys[data.data.CPU.keys.length - 1]
                        $scope.performance = data.data;

                    } else if ($scope.timeInterval) {
                        $interval.cancel($scope.timeInterval);
                        $scope.timeInterval = '';
                    }
                }
            });

        }

        $scope.goAction = function (flag, row, type, $event) {
            switch (flag / 1) {
                //yaml

                case 2:
                    //删除
                    var modalInstance = $modal.open({
                        templateUrl: '/statics/tpl/application/instance/remove.html',
                        controller: 'removeApplyTypeModalCtrl',
                        backdrop: 'static',
                        resolve: {
                            row: function () {
                                return row;
                            },
                            type: function () {
                                return type;
                            },

                        }
                    });
                    modalInstance.result.then(function () {
                        $scope.search(1);
                        $scope.isCheck = false;
                    }, function () {
                    });

                    break;
                case 3:
                    //详情
                    $scope.goDetail(row, $event)
                    break;
                case 4:
                    //资源
                    var modalInstance = $modal.open({
                        templateUrl: '/statics/tpl/application/instance/resource.html',
                        controller: 'applyResourceModalCtrl',// 初始化模态范围
                        backdrop: 'static',
                        resolve: {
                            row: function () {
                                return row;
                            },
                            type: function () {
                                return type;
                            },
                        }
                    });
                    modalInstance.result.then(function () {
                        $scope.search(1);
                    }, function () {
                    });

                    break;
                case 5:
                    //pod
                    var modalInstance = $modal.open({
                        templateUrl: '/statics/tpl/application/instance/pod.html',
                        controller: 'applyPodModalCtrl',// 初始化模态范围
                        backdrop: 'static',
                        resolve: {
                            row: function () {
                                return row;
                            },
                            type: function () {
                                return type;
                            },

                        }
                    });
                    modalInstance.result.then(function () {
                        $scope.search(1);
                        $scope.tableName = "";
                        $scope.clickTimes=2;
                        // $scope.goDetail(row, $event)
                        // $scope.subSearch($scope.tableNameSign);
                    }, function () {
                    });
                    break;
                case 6:
                    //版本回滚

                    var modalInstance = $modal.open({
                        templateUrl: '/statics/tpl/application/instance/chagePod.html',
                        controller: 'applyRollbackModalCtrl',// 初始化模态范围
                        backdrop: 'static',
                        resolve: {
                            row: function () {
                                return row;
                            }
                        }
                    });
                    modalInstance.result.then(function () {
                    	
                    }, function () {
                    });

                    break;
                case 7:
                    //滚动升级
                    var modalInstance = $modal.open({
                        templateUrl: '/statics/tpl/application/instance/updeta.html',
                        controller: 'applyUpgradeModalCtrl',// 初始化模态范围
                        backdrop: 'static',
                        resolve: {
                            row: function () {
                                return row;
                            }

                        }
                    });
                    modalInstance.result.then(function () {
                    	
                    }, function () {
                    });

                    break;
                case 8:
                    //修改实例
                    /*   var param = {};
                     param.params = angular.toJson([{"param": {}}])
                     param.resourceType = "ReplicationController";
                     param.resourceName = row.name;
                     param.namespace = row.namespace;
                     param.appId = $stateParams.id;
                     httpLoad.loadData({
                     url: '/application/getHPA',
                     method: 'POST',
                     data: param,
                     noParam: true,
                     success: function (data) {history.go(-1)
                     if (data.data.cpuTargetUtilization) {
                     var modalInstance = $modal.open({
                     templateUrl: '/statics/tpl/template/delModal.html',
                     controller: 'delModalCtrl',
                     backdrop: 'static',
                     resolve: {
                     tip: function () {
                     return '当前部署已设置弹性伸缩，不支持手动修改实例数，若需要手动修改实例数，请删除该部署的弹性伸缩';
                     },
                     btnList: function () {
                     return [{name: '取消', type: 'btn-cancel'}];
                     }
                     }
                     });
                     } else {*/
                    var modalInstance = $modal.open({
                        templateUrl: '/statics/tpl/application/instance/chagePod.html',
                        controller: 'applychagePodModalCtrl',// 初始化模态范围
                        backdrop: 'static',
                        size: 'lg',
                        resolve: {
                            row: function () {
                                return row;
                            },
                            type: function () {
                                return type;
                            },

                        }
                    });
                    modalInstance.result.then(function () {
                        $scope.search(1);
                    }, function () {
                    });
                    // }
                    /*
                     }
                     })*/


                    break;
                case 9:
                    //部署历史
                    $event.stopPropagation();
                    var param = {
                        page: 1,
                        rows: 10,
                        params: angular.toJson([{"param": {"serviceName": row.name,"appId":$stateParams.id}, "sign": "EQ"}])
                    };
                    httpLoad.loadData({
                        url: '/deployHistory/list',
                        method: 'POST',
                        data: param,
                        noParam: true,
                        success: function (data) {
                            $scope.showDetail = 2;
                            $scope.isActive = true;
                            if (data.success && data.data) {
                                $scope.userList = data.data.rows;
                                $scope.totalCount = data.data.total;

                            
                                if (data.data.rows == []) {
                                    $scope.pop("返回数据为空");
                                }
                                if (data.data.rows.length == 0) {
                                    $scope.isImageDatad = true;
                                    return;
                                } else $scope.isImageDatad = false;

                            }

                        }
                    });
                    break;
                case 10:
                    //删除弹性
                    var modalInstance = $modal.open({
                        templateUrl: '/statics/tpl/template/delModal.html',
                        controller: 'delModalCtrl',
                        backdrop: 'static',
                        resolve: {
                            tip: function () {
                                return '是否删除该服务的弹性伸缩';
                            },
                            btnList: function () {
                                return [{name: '确定', type: 'btn-info'},{name: '取消', type: 'btn-cancel'}];
                            }
                        }
                    });
                    modalInstance.result.then(function () {
                        httpLoad.loadData({
                            url: '/service/hpa',
                            method: 'DELETE',
                            data: {applicationId:$stateParams.id,name:row.name},
                            success: function (data) {
                                if (data.success) {
                                    $scope.pop(data.message);
                                    $scope.search(1);
                                }
                            }
                        });
                    });
                    break;
                case 12:
                    sessionStorage.setItem('layoutTemplateId', $state.params.id);
                    $state.go('paas.application.instancetemplate', { id: $stateParams.id,name:row.name,namespace:row.namespace })
                    break;
                case 13:
                       //服务告警设置
                    if((!row.replicas)||(row.replicas<=0)){
                        $scope.pop('该服务不存在任何运行实例数，无法进行策略设置','error');
                        return false;
                    }
                    var modalInstance = $modal.open({
                        templateUrl: '/statics/tpl/application/instance/serviceWarning.html',
                        controller: 'serviceWarningSetModalCtrl',
                        backdrop: 'static',
                        resolve: {
                            row: function () {
                                return row;
                            }
                        }
                    });
                    modalInstance.result.then(function () {
                        $scope.search(1);
                    }, function () {
                    });
                    break;
                case 14:
                    //删除批处理任务
                    var modalInstance = $modal.open({
                        templateUrl: '/statics/tpl/application/instance/remove.html',
                        controller: 'removeApplyTypeModalCtrl',
                        backdrop: 'static',
                        resolve: {
                            row: function () {
                                return row;
                            },
                            type: function () {
                                return type;
                            },

                        }
                    });
                    modalInstance.result.then(function () {
                        $scope.getJob();
                    });
                    break;
            }
        };


    }]);

//Resource
angular.module('app').controller('applyResourceModalCtrl', ['$scope', '$stateParams', '$modalInstance', 'LANGUAGE', 'httpLoad', 'row', 'type',
    function ($scope, $stateParams, $modalInstance, LANGUAGE, httpLoad, row, type) { //依赖于modalInstance
        var editObj = ['cpuRequest', 'cpuLimit', 'memoryRequest', 'memoryLimit'];
        $scope.addData = {};
        $scope.$parent.cpuRequest = $scope.$parent.cpuLimit = "m"
        $scope.$parent.memoryRequest = $scope.$parent.memoryLimit = "M"
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

        //获取资源
        var param = {};
        //param.params = angular.toJson([{"param": {}}])
        param.id = $stateParams.id;
        param.name = row.name;
        httpLoad.loadData({
            url: '/service/resource',
            method: 'GET',
            data: param,
            //  noParam: true,
            success: function (data) {
                if (data.success) {
                    var data =data.data;
                    if (data.cpuLimit.match(/\d+/gi)) {
                        $scope.addData.cpuLimit = data.cpuLimit.match(/\d+/gi).join()/1;
                    }
                    if (data.cpuRequest.match(/\d+/gi)) {
                        $scope.addData.cpuRequest = data.cpuRequest.match(/\d+/gi).join()/1;
                    }
                    if (data.memoryRequest.match(/\d+/gi)) {
                        $scope.addData.memoryRequest = data.memoryRequest.match(/\d+/gi).join()/1;
                    }
                    if (data.memoryLimit.match(/\d+/gi)) {
                        $scope.addData.memoryLimit = data.memoryLimit.match(/\d+/gi).join()/1;
                    }
                    if (data.cpuLimit.match(/[a-zA-Z]+/gi)) {
                        $scope.$parent.cpuLimit = data.cpuLimit.match(/[a-zA-Z]+/gi).join();
                    } else {
                        $scope.$parent.cpuLimit = ''
                    }
                    if (data.cpuRequest.match(/[a-zA-Z]+/gi)) {
                        $scope.$parent.cpuRequest = data.cpuRequest.match(/[a-zA-Z]+/gi).join();
                    } else {
                        $scope.$parent.cpuRequest = ''
                    }
                    if (data.memoryRequest.match(/[a-zA-Z]+/gi)) {
                        var Request = data.memoryRequest.match(/[a-zA-Z]+/gi).join();
                        if (Request != 'M' && Request != 'G' && Request != 'Mi' && Request != 'Gi') {
                            var a = {
                                name: Request,
                                value: Request
                            }
                            $scope.appResource.push(a)
                            $scope.$parent.memoryRequest = Request
                        } else {
                            $scope.$parent.memoryRequest = Request
                        }
                    }
                    if (data.memoryLimit.match(/[a-zA-Z]+/gi)) {
                        var Limit = data.memoryLimit.match(/[a-zA-Z]+/gi).join();

                        if (Limit != 'M' && Limit != 'G' && Limit != 'Mi' && Limit != 'Gi') {
                            var b = {
                                name: Limit,
                                value: Limit
                            }
                            $scope.appResource.push(b)
                            $scope.$parent.memoryLimit = Limit
                        } else {
                            $scope.$parent.memoryLimit = Limit
                        }
                    }

                }
            }
        });


        $scope.ok = function () {

            var cpuLimit = $scope.addData.cpuLimit + $scope.$parent.cpuLimit;
            var cpuRequest = $scope.addData.cpuRequest + $scope.$parent.cpuRequest;
            var memoryRequest = $scope.addData.memoryRequest + $scope.$parent.memoryRequest;
            var memoryLimit = $scope.addData.memoryLimit + $scope.$parent.memoryLimit;

            // var cpuLimit = $scope.addData.cpuLimit*$scope.unit[$scope.$parent.cpuLimit] + "m";
            // var cpuRequest = $scope.addData.cpuRequest*$scope.unit[$scope.$parent.cpuRequest] + "m";
            // var memoryLimit = $scope.addData.memoryLimit*$scope.unit[$scope.$parent.memoryLimit] + "M";
            // var memoryRequest = $scope.addData.memoryRequest*$scope.unit[$scope.$parent.memoryRequest] + "M";
//CPU
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

            var limits = {};
            limits.cpu = cpuLimit;
            limits.memory = memoryLimit;
            var request = {};
            if ($scope.addData.cpuRequest) {
                request.cpu = cpuRequest;
            }
            if ($scope.addData.memoryRequest) {
                request.memory = memoryRequest
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
            var param = {
                params: angular.toJson({
                    "name": row.name,
                    "limits": limits,
                    "requests": request,
                    applicationId: $stateParams.id
                })
            };
            httpLoad.loadData({
                url: '/service/scale',
                method: 'POST',
                data: param,
                noParam: true,
                success: function (data) {
                    if (data.success) {
                        $scope.pop(data.message);
                        $modalInstance.close();
                    }
                }
            });
        }

        $scope.cancel = function () {
            $modalInstance.dismiss('cancel');
        };
    }]);
//删除ctrl
angular.module('app').controller('removeApplyTypeModalCtrl', ['$scope', '$stateParams', '$modalInstance', 'httpLoad', 'LANGUAGE'
    , 'row', 'type',
    function ($scope, $stateParams, $modalInstance, httpLoad, LANGUAGE, row, type) { //依赖于modalInstance
        if (type == 'Pod') {
            $scope.messageApply = ' 是否确认删除该条实例？';
            var param = {};
            param.params=angular.toJson([{"param":{}}])
            param.resourceType = type;
            param.resourceName = row.name;
            param.namespace = row.namespace;
            param.appId = $stateParams.id;
            $scope.ok = function() {
                $scope.isCheck = true;
                httpLoad.loadData({
                    url: '/application/deleteResource',
                    method: 'POST',
                    data: param,
                    noParam: true,
                    success: function (data) {
                        if (data.success) {
                            $scope.pop(data.message);
                            $modalInstance.close();
                        }
                    }
                });
            }
        } else if (type == 'Service') {
            $scope.messageApply = ' 服务删除将会导致应用异常，确认删除？';
            var param1 = {};
            param1.name = [row.name];
            param1.applicationId = $stateParams.id;
            $scope.ok = function () {
                $scope.isCheck = true;
                httpLoad.loadData({
                    url: '/service/remove',
                    method: 'POST',
                    data: param1,
                    // noParam: true,
                    success: function (data) {
                        if (data.success) {
                            $scope.pop(data.message);
                            $modalInstance.close();
                        }
                    }
                });
            };

        }else if (type == 'Job') {
            $scope.messageApply = '是否确认删除该批处理任务？';
            var param2 = {};
            param2.jobName = row.name;
            param2.applicationId = $stateParams.id;
            $scope.ok = function () {
                $scope.isCheck = true;
                httpLoad.loadData({
                    url: '/service/job',
                    method: 'DELETE',
                    data: param2,
                    // noParam: true,
                    success: function (data) {
                        if (data.success) {
                            $scope.pop(data.message);
                            $modalInstance.close();
                        }
                    }
                });
            };

        } else if (type == 'ReplicationController') {
            $scope.messageApply = ' 删除部署则相关的实例也会删除，确认删除？';
        } else if (type == 'HorizontalPodAutoscaler') {
            $scope.messageApply = ' 弹性伸缩将被删除，确认删除？';
        } else {
            $scope.messageApply = '部署和运行实例都将被彻底删除，确认删除？';
        }

        $scope.cancel = function () {
            $modalInstance.dismiss('cancel');
        }
    }]);
//pod
angular.module('app').controller('applyPodModalCtrl', ['$scope','$timeout','$modal', '$stateParams', '$modalInstance', 'LANGUAGE', 'httpLoad', 'row', 'type',
    function ($scope,$timeout,$modal, $stateParams, $modalInstance, LANGUAGE, httpLoad, row, type) { //依赖于modalInstance
        var editObj = ['requestValue', 'minReplicas', 'maxReplicas'];
        $scope.addData = {};
        $scope.apply = 1;
        $scope.addData.minReplicas = 1;
        $scope.addData.maxReplicas = 100;
        //获取资源
        var param = {};
        param.params = angular.toJson([{"param": {}}])
        param.resourceType = "ReplicationController";
        param.resourceName = row.name;
        param.namespace = row.namespace;
        param.appId = $stateParams.id;
//        httpLoad.loadData({
//            url: '/application/getPodResource',
//            method: 'POST',
//            data: param,
//            noParam: true,
//            success: function (data) {
//                if (data.success) {
//                    if (!data.data.cpuRequest) {
//                        $scope.podmessage = '该资源没设置cpu的请求值，请先更改资源限制！'
//                        $scope.chackIn = true;
//                    } else {
//                        $scope.chackIn = false
//                    }
//                }
//            }
//        });
        $scope.slider = {
            value: 0,
            options: {
                floor: 0,
                ceil: 100
            }

        };
        httpLoad.loadData({
            url: '/service/hpa',
            method: 'GET',
            data: {applicationId:$stateParams.id,name:row.name},
            // noParam: true,
            success: function (data) {
                if (data.success) {

                    if(data.data){
                        var spec = data.data.spec;
                        $scope.podshow =false
                        //选择CPU核数和内存大小
                        if (!spec.targetCPUUtilizationPercentage) {
                            $scope.slider = {
                                value: 0,
                                options: {
                                    floor: 0,
                                    ceil: 100
                                }

                            };
                            $scope.addData = {
                                minReplicas: spec.minReplicas,
                                maxReplicas: spec.maxReplicas,
                                options: {
                                    floor: 1,
                                    ceil: 100
                                }

                            };
                        } else {
                            $timeout(function () {
                                $scope.slider = {
                                    value: spec.targetCPUUtilizationPercentage,
                                    options: {
                                        floor: 0,
                                        ceil: 100
                                    }

                                };
                                $scope.addData = {
                                    minReplicas: spec.minReplicas,
                                    maxReplicas: spec.maxReplicas,
                                    options: {
                                        floor: 1,
                                        ceil: 100
                                    }

                                };
                            }, 200);

                        }
                    }else{
                        $scope.podshow =true
                    	 $timeout(function () {
                    		 $scope.slider = {
                                     value: 0,
                                     options: {
                                         floor: 0,
                                         ceil: 100
                                     }

                                 };
                             $scope.addData = {
                                 minReplicas: 1,
                                 maxReplicas: 100,
                                 options: {
                                     floor: 1,
                                     ceil: 100
                                 }

                             };
                         }, 200);
                    }


                } else {
                    //选择CPU核数和内存大小
                    $scope.slider = {
                        value: 0,
                        options: {
                            floor: 0,
                            ceil: 100
                        }

                    };
                    $scope.addData = {
                        minReplicas: 1,
                        maxReplicas: 100,
                        options: {
                            floor: 1,
                            ceil: 100
                        }

                    };
                }
            }
        });

        $scope.sliderApply = {
            value:row.replicas,
            options: {
                floor: 0,
                ceil: 100
            }
        }
        $scope.delete = function () {
            var modalInstance = $modal.open({
                templateUrl: '/statics/tpl/template/delModal.html',
                controller: 'delModalCtrl',
                backdrop: 'static',
                resolve: {
                    tip: function () {
                        return '是否删除该服务的弹性伸缩';
                    },
                    btnList: function () {
                        return [{name: '确定', type: 'btn-info'},{name: '取消', type: 'btn-cancel'}];
                    }
                }
            });
            modalInstance.result.then(function () {
                httpLoad.loadData({
                    url: '/service/hpa',
                    method: 'DELETE',
                    data: {applicationId:$stateParams.id,name:row.name},
                    success: function (data) {
                        if (data.success) {
                            $scope.pop(data.message);
                            $modalInstance.close();
                        }
                    }
                });
            });
        }
        $scope.ok = function () {

            if($scope.apply==1){
                var paramhpa = {};
                paramhpa.hpa = {
                    
                }
                editObj.forEach(function (attr) {
                    if (attr == "requestValue") {
                        paramhpa.hpa[attr] = $scope.addData[attr];
                    } else {
                        paramhpa.hpa[attr] = $scope.addData[attr];
                    }
                });
                if (paramhpa.hpa.maxReplicas <= paramhpa.hpa.minReplicas) {
                    $scope.pop('最小实例数必须小于最大实例数', 'error');
                    return
                }
                paramhpa.applicationId = $stateParams.id;
                paramhpa.name = row.name;
                paramhpa.hpa.cpuPercent = $scope.slider.value;
                if(paramhpa.hpa.cpuPercent==0){
                    $scope.pop('百分比不能为0', 'error');
                    return
                }
                httpLoad.loadData({
                    url: '/service/hpa',
                    method: 'POST',
                    data: paramhpa,
                    //  noParam: true,
                    success: function (data) {
                        if (data.success) {
                            $scope.pop(data.message);
                            $modalInstance.close();
                        }
                    }
                });
            }else{
                var param = {};
                param.name = row.name;
                param.applicationId = $stateParams.id;
                param.number = $scope.sliderApply.value;
                httpLoad.loadData({
                    url: '/service/scale',
                    method: 'POST',
                    data: param,
                    //  noParam: true,
                    success: function (data) {
                        if (data.success) {
                            $scope.pop(data.message);

                        } else {

                        }
                        $modalInstance.close();
                    }
                });
            }

        }

        $scope.cancel = function () {
            $modalInstance.dismiss('cancel');
        };
    }]);
//Rollback
angular.module('app').controller('applyRollbackModalCtrl', ['$scope', '$stateParams', '$modalInstance', 'LANGUAGE', 'httpLoad', 'row',
    function ($scope, $stateParams, $modalInstance, LANGUAGE, httpLoad, row) { //依赖于modalInstance

        var param = {
            simple:true,
            params: angular.toJson([{"param": {"serviceName": row.name,"appId":$stateParams.id}, "sign": "EQ"}])
        };
        httpLoad.loadData({
            url: '/deployHistory/list',
            method: 'POST',
            data: param,
            noParam: true,
            success: function (data) {
                $scope.showDetail = 2;
                $scope.isActive = true;
                if (data.success && data.data) {
                    $scope.visionData = data.data.rows;


                }

            }
        });

        $scope.toJson = function (data) {
            $scope.visionJson = angular.fromJson(data);
        }
        $scope.ok = function () {

            $scope.param={applicationId:$stateParams.id, name:row.name,paramInfo:$scope.visionJson.paramInfo}
           
            httpLoad.loadData({
                url: '/service/back',
                method: 'post',
                data: $scope.param,
                //noParam: true,
                success: function (data) {
                    if (data.success) {
                        $scope.pop(data.message);
                        $modalInstance.close();
                    }

                  
                }
            });
        }

        $scope.cancel = function () {
            $modalInstance.dismiss('cancel');
        };
    }]);

//upgrade
angular.module('app').controller('applyUpgradeModalCtrl', ['$scope', '$stateParams', '$modalInstance', 'LANGUAGE', 'httpLoad', 'row',
    function ($scope, $stateParams, $modalInstance, LANGUAGE, httpLoad, row) { //依赖于modalInstance
        $scope.serviceName = row.image.split("/")[row.image.split("/").length-1].split(":")[0];
        $scope.scriptItem = {}
        $scope.scriptItem.selected= {};
        var params = {
            simple: true,
            params: angular.toJson([{"param": {"image.name":$scope.serviceName, "repository.status":0}, "sign": "EQ"}])
        }
        httpLoad.loadData({
            url: '/image/list',
            method: 'POST',
            data: params,
            noParam: true,
            success: function (data) {
                if (data.success) {
                    $scope.dataList = data.data.rows;
                    var list = new Set();
                    $scope.dataList.forEach(function(item){
                    	list.add(item.repositoryName)
                    })
                    $scope.warehoseData = [];
                    list.forEach(function(item){
                    	$scope.warehoseData.push(item)
                    })
                
                    
                }
            }
        });
        $scope.getnameSpace = function () {
        	 $scope.namespaceItem = "namespve";
        	 $scope.scriptItem.selected = ""
        	  var list = new Set();
              $scope.dataList.forEach(function(item){
            	  if(item.repositoryName==$scope.warehoseItem){
            		  list.add(item.namespace)
            	  }
              	
              })
              $scope.namespaceData = [];
              list.forEach(function(item){
              	$scope.namespaceData.push(item)
              })
        }
        $scope.namespaceItem = "namespve"
        $scope.getVision = function () {
        	 $scope.scriptItem.selected = ""
        	if($scope.namespaceItem == "namespve"){
        		 $scope.pop("请选择项目","error");
        		 return false;
        	} 
        	 
        	$scope.dataType = [];
             $scope.dataList.forEach(function(item){
           	  if(item.repositoryName==$scope.warehoseItem&&item.namespace==$scope.namespaceItem){
           		$scope.dataType.push(item)
           	  } 	
             })
         
        }
        $scope.ok = function () {
  
            $scope.scriptItem.selected.imageurl = ($scope.scriptItem.selected.repositoryType !=1) ?":"+$scope.scriptItem.selected.repositoryPort:'';
            if($scope.scriptItem.selected.namespace==""){
            	 $scope.imageurl  =$scope.scriptItem.selected.repositoryAddress+$scope.scriptItem.selected.imageurl+ '/'+$scope.scriptItem.selected.name+':'+ $scope.scriptItem.selected.tag
            }else{
            	 $scope.imageurl  =$scope.scriptItem.selected.repositoryAddress+$scope.scriptItem.selected.imageurl+ '/'+$scope.scriptItem.selected.namespace+ '/'+$scope.scriptItem.selected.name+':'+ $scope.scriptItem.selected.tag    
            }
            $scope.param = {};
            $scope.param.applicationId = $stateParams.id;
            $scope.param.image = $scope.imageurl;
            $scope.param.name = row.name;
            // $scope.param.namespace = row.namespace;
            if (!$scope.param.image) {
                $scope.pop("镜像名称不能为空");
                return
            }
           
            httpLoad.loadData({
                // url: '/application/rolling',
                url: '/service/rolling',
                method: 'post',
                data: $scope.param,
                //  noParam: true,
                success: function (data) {
                    if (data.success) {
                        $scope.pop(data.message);
                        $modalInstance.close();
                    }
                }
            });
        }

        $scope.cancel = function () {
            $modalInstance.dismiss('cancel');
        };
    }]);

//applychagePodModalCtrl
angular.module('app').controller('applychagePodModalCtrl', ['$scope', '$stateParams', '$modalInstance', 'LANGUAGE', 'httpLoad', 'row', 'type','$timeout',
    function ($scope, $stateParams, $modalInstance, LANGUAGE, httpLoad, row, type,$timeout) { //依赖于modalInstance
        $timeout(function () {
            $scope.slider = {
                value:row.replicas,
                options: {
                    floor: 0,
                    ceil: 100
                }
            }
        },10)

        $scope.ok = function () {
            var param = {};
            param.name = row.name;
            param.applicationId = $stateParams.id;
            param.number = $scope.slider.value;
            httpLoad.loadData({
                url: '/service/scale',
                method: 'POST',
                data: param,
                //  noParam: true,
                success: function (data) {
                    if (data.success) {
                        $scope.pop(data.message);
                        $modalInstance.close();
                    } else {
                        $modalInstance.close();
                    }
                }
            });
        }

        $scope.cancel = function () {
            $modalInstance.dismiss('cancel');
        };
    }]);
//服务告警
angular.module('app').controller('serviceWarningSetModalCtrl', ['$scope', '$stateParams', '$modalInstance', 'LANGUAGE', 'httpLoad', 'row',
    function ($scope, $stateParams, $modalInstance, LANGUAGE, httpLoad, row) { //依赖于modalInstance
        $scope.currentPod=row.replicas;
        $scope.content="设置服务告警策略";
        $scope.warningData={};
        // 查详情
        if((row.alarmStatus=='0')||(row.alarmStatus=='1')){
            $scope.content="修改服务告警策略";
            httpLoad.loadData({
                url: '/service/alarm',
                method: 'GET',
                data: {"name":row.name,"applicationId":$stateParams.id},
                // noParam: true,
                success: function (data) {
                    if (data.success && data.data) {
                        $scope.warningData = data.data;
                    }

                }
            });
            $scope.ok = function () {
                if($scope.warningData.number>$scope.currentPod){
                    $scope.pop('告警实例数不能超过当前实例总数，请重新填写','error');
                    return false;
                }
                $scope.param={
                    "id":$scope.warningData.id,
                    "name":row.name,
                    "number":$scope.warningData.number,
                    "email":$scope.warningData.email,
                    "applicationId":$stateParams.id,
                    "status":$scope.warningData.status
                }

                httpLoad.loadData({
                    url: '/service/update/alarm',
                    method: 'POST',
                    data:$scope.param ,
                    // noParam: true,
                    success: function (data) {
                        if (data.success) {
                            $scope.pop(data.message);
                            $modalInstance.close();
                        }
                    }
                });
            }
        }else if(row.alarmStatus=='2'){
            $scope.ok = function () {
                if($scope.warningData.number>$scope.currentPod){
                    $scope.pop('告警实例数不能超过当前实例总数，请重新填写','error');
                    return false;
                }
                $scope.param={
                    "name":row.name,
                    "number":$scope.warningData.number,
                    "email":$scope.warningData.email,
                    "applicationId":$stateParams.id
                }
                httpLoad.loadData({
                    url: '/service/alarm',
                    method: 'POST',
                    data:$scope.param,
                    // noParam: true,
                    success: function (data) {
                        if (data.success) {
                            $scope.pop(data.message);
                            $modalInstance.close();
                        }
                    }
                });
            }
        }

        $scope.cancel = function () {
            $modalInstance.dismiss('cancel');
        };
    }]);

app.controller('applyhistoryModalCtrl', ['$rootScope', '$scope', '$state', 'httpLoad', '$stateParams', function ($rootScope, $scope, $state, httpLoad, $stateParams) {
    $rootScope.link = '/statics/css/alarm.css';//引入页面样式
    $scope.param = {
        page: 1,
        rows: 10,
        params: angular.toJson([{"param": {"object": "image"}, "sign": "LK"}])
    };
    $scope.getData = function (page) {
        var id = $stateParams.id;
        httpLoad.loadData({
            url: '/log/list',
            method: 'POST',
            data: $scope.param,
            noParam: true,
            success: function (data) {
                if (data.success && data.data) {
                    $scope.userList = data.data.rows;
                    $scope.totalCount = data.data.total;
                    if (data.data.rows == []) {
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

}]);
