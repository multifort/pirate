(function () {
	app.service('KubernetesDataNew', [function () {
		var service = {
			platformId: '',
			type: 'Node',
			row: '',
			state:'',
			host:{}
		};
		return service;
	}])
	app.controller('newEnvironmentDetailCtrl', ['$scope','$interval', 'httpLoad', '$rootScope', '$modal', '$state', '$stateParams','$timeout','$location','$anchorScroll','$sce','KubernetesDataNew',
		function ($scope,$interval, httpLoad, $rootScope, $modal, $state,$stateParams, $timeout,$location,$anchorScroll,$sce,KubernetesDataNew) {
			$rootScope.moduleTitle =$sce.trustAsHtml('环境资源 > <span>环境管理</span> > 详情');//定义当前页
			$rootScope.link = '/statics/css/user.css';//引入页面样式
			KubernetesDataNew.platformId = $stateParams.id;
		  $scope.searchArr = {};
			var socket;
			$scope.param = {};
			$scope.statusData = {1:"不可用",2:"激活状态",3:"冻结状态",4:"异常状态",5:"创建中",6:"死亡"};
			$scope.typeData = {1:"kubernetes",2:"swarm"};
			(function(){
				var id = $stateParams.id;
				httpLoad.loadData({
					url:'/environment/detail',
					method:'GET',
					data: {id: id},
					success:function(data){
						if(data.success&&data.data){
							$scope.envDetail = data.data;
						}
					}
				});
			})();
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
				},
				  file:{
				  theme:'File',
				  unit:'GiB'
			  },
      }
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
			]

			var bucketDuration =[60,60,60,60,60]
			var startTime =['1h','4h','1d','3d','1w']

			$scope.pageGoBack = function(){
				$state.go('paas.environment.environment');
			};
			$scope.topology = function () {
				//拓扑
				httpLoad.loadData({
					url: '/environment/topology',
					method: 'GET',
					data: {envId:$stateParams.id},
					success: function (data) {
						if (data.success) {
							$scope.itemData = data.data;
							$scope.nodeData =  $scope.itemData;

						}
					}
				});
			}

			//获取项目
			$scope.getNamespaceList = function (envId) {
				httpLoad.loadData({
					url: '/environment/getNameSpace',
					method: 'POST',
					data:{id:envId},
					// noParam: true,
					success: function (data) {
						if (data.success&&data.data.length>=1) {
							var namespaceList=[];
							data.data.forEach(function (item) {
								namespaceList.push(item.metadata.name);
							});
							$scope.namespaceList = namespaceList;
						}
					}
				});

			};


			//获取云主机列表
			$scope.getData = function (page,type) {
				httpLoad.loadData({
					url: '/container/list',
					method: 'POST',
					data: $scope.param,
					// noParam: true,
					success: function (data) {
						if (data.success) {

							$scope.listData = data.data;
							$scope.total = data.data.length;
							if(type =="Node"){
									$scope.listData.forEach(function(item){
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
							}else if(type=="Job"){
								$scope.listData.forEach(function (item) {
									item.name=item.metadata.name;
									item.namespace=item.metadata.namespace;
									item.completions=item.spec.completions;
									item.succeeded=item.status.succeeded||0;
									item.creationTimestamp=item.metadata.creationTimestamp;
									item.selector=item.spec.template.metadata.labels;
								})
							}
							if(!$scope.total) {
								$scope.isImageData = true;
							} else {
								$scope.isImageData = false;
							}

						}else {
							$scope.listData = [];
							$scope.total = $scope.listData.length;
						}
					}
				});

			};

      //获概述列表
      $scope.getopenDetail = function (page) {
				var id = KubernetesDataNew.platformId
        $scope.param.page = page || $scope.param.page;
        httpLoad.loadData({
          url: '/container/total',
          method: 'GET',
          data: {id:id},
        //  noParam: true,
          success: function (data) {
            if (data.success) {
              $scope.countopenDetail = data.data;
							if(!$scope.countopenDetail){
								$scope.countopenDetail={
									Node:0,
									PersistentVolume:0,
									Pod:0,
									ReplicationController:0,
									Service:0,
									HorizontalPodAutoscaler:0
								}
							}

            }else{
        		$scope.countopenDetail={
						Node:0,
						PersistentVolume:0,
						Pod:0,
						ReplicationController:0,
						Service:0,
						HorizontalPodAutoscaler:0
					}
            }
          }
        });
      };


			//详情
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

				$scope.paramObj = {
					 resourceName:'',
					 namespace:''
				 };
				 $scope.showDetailContainer = true;
         //跳转详情页
			$scope.detail = function (row,type,$event,flag) {
						$scope.paramObj.resourceName = row.name;
						$scope.paramObj.namespace = row.namespace;
						KubernetesDataNew.row = row;
	         $event.stopPropagation();
	         $scope.isActive = true ;
			 var params = {"resourceType":type,"namespace":row.namespace,"resourceName":row.name,id:KubernetesDataNew.platformId}
								 switch (type) {
									case 'Master':
											$scope.showDetail = 1;
											break;
									case 'Node':
											$scope.showDetail = 2;
											break;
									case 'ReplicationController':
											$scope.showDetail = 3;
											break;
									case 'Service':
											$scope.showDetail = 4;
											break;
									case 'Pod':
										if(flag=='fromNode') KubernetesDataNew.type = type;
										$scope.showDetailContainer = false;
										$scope.Teminal=false;
										$scope.showDetail = 5;
										$timeout(function () {
											$scope.showDetailContainer = true;
										},10);
										break;
									case 'PersistentVolumes':
											$scope.showDetail = 6;
											break;
									case 'HorizontalPodAutoscaler':
										$scope.showDetail = 7;
										break;
									 case 'Job':
										 $scope.showDetail = 8;
										 break;
										};

										//详情基本信息
									 $scope.getBasicMessage(params,type,row)
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
       //详情的基本信息
			  $scope.getBasicMessage = function (params,type,row) {
					 				httpLoad.loadData({
					           url:'/container/detail',
					           method:'POST',
					           data: params,
					           success:function(data){
					         if(data.success&&data.data){
					        	 $scope.Container = []
					             $scope.supplierDetail = data.data;
					             if(type=='Master' || type == 'Node'){
												 $scope.chackIn(type,row);
													$scope.getnodepod(row);
					            	   $scope.allocatable = $scope.supplierDetail.allocatable[0];
					                   $scope.basicDetails = $scope.supplierDetail.basicDetails[0];
					                   $scope.capacity = $scope.supplierDetail.capacity[0];
														 if($scope.basicDetails.labels){
															   $scope.labels =  $scope.basicDetails.labels.replace(/,/g,'<br>')
														 }
					             }else if(type=='ReplicationController'){
												 if($scope.supplierDetail.labels){
															$scope.labels =  $scope.supplierDetail.labels.replace(/,/g,'<br>')
													}
												 if($scope.supplierDetail.selectors){
														$scope.selectors =  $scope.supplierDetail.selectors.replace(/,/g,'<br>')
												}
					             }else if(type=='Service'){
					            	 $scope.basicDetails = $scope.supplierDetail.basicDetails[0];
												 if($scope.basicDetails.selectors){
														 $scope.selectors =  $scope.basicDetails.selectors.replace(/,/g,'<br>')
													}
													if($scope.basicDetails.labels){
													 $scope.labels =  $scope.basicDetails.labels.replace(/,/g,'<br>')
													}
					             }else if(type=='Pod'){
												 	 $scope.Container = $scope.supplierDetail.Container;
									 $scope.pageNum = 100;
									                 $scope.Status = $scope.supplierDetail.Status[0];
									                 $scope.Template = $scope.supplierDetail.Template;
									                 $scope.Volumes = $scope.supplierDetail.Volumes;
									                 for(var i=0;i<$scope.Container.length;i++){
									                	 var item = $scope.Container[i]
																			 if(item.state=='running'){
																				 KubernetesDataNew.state = true;
																				 KubernetesDataNew.states = false;
																				 $scope.options.bucketContainer =item.containerName;
																				 $scope.options.bucketT =item.containerName
																				 $scope.options.bucketL =item.containerName
																				 $scope.getLog();
                                                                                 if(!KubernetesDataNew.states){
                                                                                     $scope.prompts = true;
                                                                                    
                                                                                 }else{
                                                                                     $scope.prompts = false
                                                                                 }
																				 $scope.chackIn(type,row)
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
																				 KubernetesDataNew.state = true
																				 KubernetesDataNew.states = true
																			   $scope.getLog();
                                                                                 if(!KubernetesDataNew.states){
                                                                                     $scope.prompts = true;
                                                                                    
                                                                                 }else{
                                                                                     $scope.prompts = false
                                                                                 }
																				 $scope.chackIn(type,row)
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
																				 KubernetesDataNew.state = false
																			 	KubernetesDataNew.states = true;
																			 }
									                 }
																	 setTimeout(function(){
																		 if(!$('.kuberb option').eq(0).html()){
																			 $('.kubera option').eq(0).remove()
																			$('.kuberb option').eq(0).remove()
																			$('.kuberc option').eq(0).remove()
																		 }
																	 },100)
																	 $scope.getLog();
																	 if(!KubernetesDataNew.states){
																		 $scope.prompts = true;
                                                                       
																	 }else{
																		 $scope.prompts = false
																	 }
																	 $scope.chackIn(type,row)

					           }else if(type=='Job'){}else{
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
					var row = KubernetesDataNew.row;
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
				$scope.getLog = function (page) {
					var row = KubernetesDataNew.row
					var params = {
						"resourceType":'Pod',
						"namespace":row.namespace,
						"resourceName":row.name,
						'containerName':$scope.options.bucketL,
						'status':row.status,
						'id':KubernetesDataNew.platformId,
                        'line':page||100,
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
					if(type == 'Pod'&&!KubernetesDataNew.state){
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
				 $scope.monitored = function () {

					 var namespace = ''
					 if(KubernetesDataNew.type=='Node'){
						 namespace ='_system'
					 }else{
						  namespace =$scope.paramObj.namespace
					 }
					 var paramObj = {
							 type:KubernetesDataNew.type.toLowerCase(),
							 startTime:startTime[$scope.options.bucketDuration],
							 bucketDuration:bucketDuration[$scope.options.bucketDuration],
							 resourceName:$scope.paramObj.resourceName,
							 containerName:$scope.options.bucketContainer,
							 namespace:namespace,
							 envId:KubernetesDataNew.platformId
					 };
					  httpLoad.loadData({
					      url: '/application/monitor',
					      method: 'GET',
								data:paramObj,
					    //  noParam:true,
					      success: function(data){
									if(data.success) {
										$scope.time = data.data.CPU.keys[data.data.CPU.keys.length-1]
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
							ip:row.ip,
							id:KubernetesDataNew.platformId
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
			//搜索
			$scope.namespace={};
			$scope.search = function (searchType,type) {
				if (!KubernetesDataNew.platformId){
					$scope.listData = [];
					$scope.total = 0;
					return;
				}
				var id = KubernetesDataNew.platformId

				KubernetesDataNew.type = type;
				//对参数进行处理，去除空参数
				$scope.listData = [];
				var toObjFormat = function (obj) {
					for (var a in obj) {
						if (obj[a] == "") delete obj[a];
					}
					return obj;
				}
				var params = [];
				var namespace="";
				if(type=='ReplicationController'){
					namespace=$scope.namespace.namespaceOfReplication;
				}else if(type=='Service'){
					// console.log(namespace);
					namespace=$scope.namespace.namespaceOfService;
				}else if(type=='Pod'){
					namespace=$scope.namespace.namespaceOfPod;
				}else if(type=='HorizontalPodAutoscaler'){
					namespace=$scope.namespace.namespaceOfScaler;
				}else if(type=='Job'){
					namespace=$scope.namespace.namespaceOfJob;
				}
				var param1 = toObjFormat({
					resourceName: $scope.searchArr.searchByName,
					resourceType: type || 'Node',
					envId:id,
					namespace:namespace
				});
				$scope.param = toObjFormat(param1);
				$scope.searchArr.searchByName = '';
				if(searchType==1)$scope.getNamespaceList(id);
				$scope.getData(1,type);
			}
			//重置搜索条件
			$scope.reset = function () {
				var obj = ['name'];
				angular.forEach(obj, function (data) {
					$scope[data] = '';
				})
			}

		}
	]);

})();
