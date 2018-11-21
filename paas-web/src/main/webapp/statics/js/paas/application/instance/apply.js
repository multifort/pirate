(function () {
	"use strict";

	app.controller('adhibitionAppCtrl', ['$scope','$interval', 'webSocket','httpLoad', '$rootScope', '$modal', '$state', '$timeout','$location','$anchorScroll',
		function ($scope,$interval,webSocket,httpLoad, $rootScope, $modal, $state, $timeout,$location,$anchorScroll) {
			$rootScope.moduleTitle = '应用服务 > 应用实例';//定义当前页

			//websocket异步操作
			webSocket.onmessage({
				message:function (data) {
					if($rootScope.currentUrl=='paas.application.instance'&&(data.operate=='application/imageDeploy')){
						$scope.getData();
					}else if($rootScope.currentUrl=='paas.application.instance'&&(data.operate=='application/layoutDeploy')){
						$scope.getData();
					}
				}
			});
		  $scope.isListView = true;
			$scope.param = {
				page:1,
				rows: 9
			};
		//获取云主机列表
			$scope.getData = function (page) {
				$scope.isSetx = true;
				$scope.param.page = page || $scope.param.page;
				httpLoad.loadData({
					url: '/application/list',
					method: 'POST',
					data: $scope.param,
					noParam: true,
					success: function (data) {
						if (data.success) {
							$scope.countData = data.data.rows;
                            $scope.countData.forEach(function (item) {
                                httpLoad.loadData({
                                    url: '/application/resource',
                                    method: 'GET',
                                    data:{applicationId:item.id},
                                   // noParam: true,
                                    success: function (datas) {
                                        if (datas.success) {
                                        	if(datas.data.servicePath){
                                        		var array = datas.data.servicePath.split(',');
                                        		datas.data.servicePathF = array[array.length-1];
                                        	}
                                            angular.extend(item,datas.data);
										}

                                    }
                                });
                            })
                            $scope.totalCount = data.data.total;
							if(!$scope.totalCount){
								$scope.isImageData = true;
								} else {
									$scope.isImageData = false;
							}
						}
					}
				});
			};
			$scope.getData(1);
			//详情
			$scope.detail = function(id,clusterId,envId){
				$state.go('paas.application.instancetiondetail', {id: id,clusterId:clusterId,envId:envId})
			}
			//搜索
			$scope.search = function () {
				//对参数进行处理，去除空参数
				var toObjFormat = function (obj) {
					for (var a in obj) {
						if (obj[a] == "") delete obj[a];
					}
					return obj;
				}
				var params = [];
				var param1 = toObjFormat({
					"a.name": $scope.searchByName
				});
				if (angular.toJson(param1).length > 2) params.push({param: param1, sign: 'LK'});
				$scope.param = {
					page: 1,
					rows: 10,
					params: angular.toJson(params)
				}
				$scope.getData();
			}
			//重置搜索条件
			$scope.reset = function () {
				var obj = ['name'];
				angular.forEach(obj, function (data) {
					$scope[data] = '';
				})
			}
			$scope.goBack = function(){
						 $scope.isActive = false;
						 $timeout(function() {
								 $scope.showHistory = false;
						 }, 200);
				 };
				 $scope.gohistory = function($event){
					 //历史记录
						$event.stopPropagation();
					 $scope.isActive = true ;
					 $scope.showHistory = true;
		};
			//全选
			$scope.operation = {};
			$scope.operation.isBatch1 = true;$scope.operation.isALl = false;
			$scope.selectALl = function(){
				$scope.operation.isBatch1 = !$scope.operation.isALl;
						$scope.countData.forEach(function(item){
							item.select = $scope.operation.isALl;
						});
			}
			$scope.choose = function(){
				var a = 0,b=0;
				$scope.countData.forEach(function(item){
					if(item.select==true) a++;
					else b=1;
				});
				if(a>=1) $scope.operation.isBatch1 = false;
				else $scope.operation.isBatch1 = true;
				if(b==1) $scope.operation.isALl = false;
				else $scope.operation.isALl = true;
			};
			//返回
			$scope.goAction = function (flag, id, $event,row) {
				switch (flag / 1) {
					case 1:
					//新增

					var modalInstance = $modal.open({
						templateUrl : '/statics/tpl/application/instance/add.html',
						controller : 'addApplyModalCtrl',// 初始化模态范围
						//size : sm, //大小配置
						backdrop: 'static',
						resolve : {
							id : function(){
								return id;
							},
						}
				});
				modalInstance.result.then(function(){
						$scope.getData(1);
				},function(){});

						break;
					case 2:
					//编辑
							var modalInstance = $modal.open({
									templateUrl : '/statics/tpl/application/instance/add.html',
								controller : 'addApplyModalCtrl',
								backdrop: 'static',
								resolve : {
									id : function(){
										return id;
									},

								}
							});
							modalInstance.result.then(function(){
								$scope.getData(1);
							},function(){});

						break;
					case 3:
						//删除
								if($event) $event.stopPropagation();
								var ids=[];
								ids.push(id);
								var modalInstance = $modal.open({
									templateUrl : '/statics/tpl/application/instance/remove.html',
									controller : 'removeApplyModalCtrl',
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
					$state.go('paas.application.instancetiondeploy', {id: id})
						break;
                    case 5:
                        //服务暴露
                        var modalInstance = $modal.open({
                            templateUrl: '/statics/tpl/application/instance/serveropen.html',
                            controller: 'applyServerModalCtrl',// 初始化模态范围
                            backdrop: 'static',
							size:'lg',
                            resolve: {
                                row: function () {
                                    return id;
                                }

                            }
                        });
                        modalInstance.result.then(function () {
                            $scope.getData(1);
                        }, function () {
                        });
                        break;
                    case 6:
                        //资源配额
                        var modalInstance = $modal.open({
                            templateUrl: '/statics/tpl/application/instance/servershare.html',
                            controller: 'applyServerShareModalCtrl',// 初始化模态范围
                            backdrop: 'static',
                            size:'lg',
                            resolve: {
                                row: function () {
                                    return row;
                                }

                            }
                        });
                        modalInstance.result.then(function () {
                            $scope.getData(1);
                        }, function () {
                        });
                        break;
				}
			};

			$scope.deteleAll = function ($event) {
				var ids = [];
				for(var i=0;i<$scope.countData.length;i++){
					var item = $scope.countData[i]
					if(item.select){
						ids.push(item.id);
					}
				}
				var modalInstance = $modal.open({
					templateUrl : '/statics/tpl/application/instance/remove.html',
					controller : 'removeApplyModalCtrl',
						backdrop: 'static',
					resolve : {
						id : function(){
							return ids;
						},

					}
				});
				modalInstance.result.then(function(){
					$scope.getData(1);
					$scope.operation.isALl = false;
					$scope.operation.isBatch1 = true;
					angular.forEach($scope.countData, function (data, index) {
						data.select = false;
				})
				},function(){});
			};
		}
	]);
	//新增ctrl
	 angular.module('app').controller('addApplyModalCtrl',['$scope','$modalInstance','LANGUAGE','httpLoad','id',
			 function($scope,$modalInstance,LANGUAGE,httpLoad,id){ //依赖于modalInstance
				 var editObj = ['name','envId', 'remark'];
   	 		$scope.modalName = '应用创建';
					var url = '/application/create';
					 $scope.addData = {};
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
					 //如果为编辑，进行赋值
	        if (id) {
	                url = '/application/modify';
	                $scope.modalName = '应用编辑';
	                httpLoad.loadData({
	                    url: '/application/detail',
	                    method: 'GET',
	                    data: {id: id},
	                    success: function (data) {
	                        if (data.success) {
	                            var data = data.data;
	                            for (var a in editObj) {
	                                var attr = editObj[a];
	                                $scope.addData[attr] = data[attr];
	                            }

	                        }
	                    }
	                });
	            }
					$scope.ok = function () {
							 var param = {};
							 for (var a in editObj) {
									 var attr = editObj[a];
									 param[attr] = $scope.addData[attr];
							 }
							 if (id) param.id = id;
							 httpLoad.loadData({
									 url:url,
									 method:'POST',
									 data: param,
									 success:function(data){
											 if(data.success){
													$scope.pop(data.message);
													 $modalInstance.close();
											 }
									 }
							 });
					 }

					 $scope.cancel = function(){
							$modalInstance.dismiss('cancel');
					 };
			 }]);

	//删除ctrl
	angular.module('app').controller('removeApplyModalCtrl',['$scope','$modalInstance','httpLoad','LANGUAGE','id',
		function($scope,$modalInstance,httpLoad,LANGUAGE,id){ //依赖于modalInstance
			$scope.messageApply = '本应用包含的部署、服务和运行实例都将彻底删除，确认删除？';

			$scope.ok = function(){
				$scope.isCheck = true;
				httpLoad.loadData({
					url:'/application/remove',
					method:'POST',
					data: {ids:id},
					success:function(data){
						if(data.success){
							//console.log(removeData);
							$scope.pop(data.message);
							$modalInstance.close();
						}else{
									$modalInstance.dismiss('cancel');
									$scope.operation.isBatch1 = false;
						}
					}
				});
			};
			$scope.cancel = function(){
			$modalInstance.dismiss('cancel');
			$scope.operation.isBatch1 = true;
			}
		}]);

    //applyServerModalCtrl
    angular.module('app').controller('applyServerModalCtrl', ['$scope', '$stateParams', '$modalInstance', 'LANGUAGE', 'httpLoad', 'row','$timeout',
        function ($scope, $stateParams, $modalInstance, LANGUAGE, httpLoad, row,$timeout) { //依赖于modalInstance
            $scope.addData = {};
            $scope.sername = "http";
            $scope.getlist = function () {
                httpLoad.loadData({
                    url: '/application/ingress',
                    method: 'GET',
                    data: {applicationId:row},
                    //  noParam: true,
                    success: function (data) {
                        if (data.success) {
                            $scope.countData  = data.data.ingress;
                            $scope.serverData  = data.data.services;
                        } else {

                        }
                    }
                });
			}
            $scope.getlist()
			$scope.deleteServer = function (name) {
                httpLoad.loadData({
                    url: '/application/ingress',
                    method: 'DELETE',
                    data: {applicationId:row,svcName:name},
                    //  noParam: true,
                    success: function (data) {
                        $scope.pop(data.message);
                        $scope.getlist()
                    }
                })
            }
            $scope.getsername = function () {
            	if($scope.name){
            	 	   httpLoad.loadData({
                           url: '/application/service/port',
                           method: 'GET',
                           data: {applicationId:row,type: $scope.sername,serviceName:$scope.name},
                           //  noParam: true,
                           success: function (data) {
                               if (data.success) {
                            	   $scope.portData   = data.data;
                                 
                               } else {

                               }
                           }
                       })
            	} 
            }
            $scope.getsername("http");
            $scope.ok = function () {
                var param = {};
                param.serviceName =  $scope.name;
                param.type = $scope.sername;
                param.port = $scope.clusterId;
                param.applicationId = row;
                // param.projectPath = $scope.projectPath;
                httpLoad.loadData({
                    url: '/application/ingress',
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

            $scope.cancel = function () {
                $modalInstance.dismiss('cancel');
            };
        }]);
    //applyServerShareModalCtrl
    angular.module('app').controller('applyServerShareModalCtrl', ['$scope', '$modal','$stateParams', '$modalInstance', 'LANGUAGE', 'httpLoad', 'row','$timeout',
        function ($scope,$modal, $stateParams, $modalInstance, LANGUAGE, httpLoad, row,$timeout) { //依赖于modalInstance
            $scope.addData = {};
            $scope.pod = {}
            $scope.msgName = "创建服务配额";
            $scope.selectB = true

            $scope.defaultContainer = {}
            $scope.container = {}
            $scope.unitData = {};
            $scope.limitRange = {
                pod:{},
                defaultContainer:{},
                container:{}
			}
            $scope.unitData.cpuRequest = $scope.unitData.cpuLimit = $scope.unitData.maxPodCpu = $scope.unitData.minPodCpu = $scope.unitData.defaultCpu = $scope.unitData.defaultReqCpu = $scope.unitData.maxContainerCpu = $scope.unitData.minContainerCpu = "m"
            $scope.unitData.memoryRequest = $scope.unitData.memoryLimit =  $scope.unitData.maxPodMemory = $scope.unitData.minPodMemory = $scope.unitData.defaultMemory = $scope.unitData.defaultReqMemory = $scope.unitData.maxContainerMemory = $scope.unitData.minContainerMemory = "M"
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
            $scope.mapList = {
                PODS:"pods",
                CONFIGMAPS:"configmaps",
                PERSISTENTVOLUMECLAIMS:"persistentvolumeclaims",
                SERVICES:"services",
                SERVICES_NODEPORTS:"services.nodeports",
			}
            $scope.map = [
                {
                    name: '运行实例数',
                    value: 'PODS'
                },
                {
                    name: '配置实例数',
                    value: 'CONFIGMAPS'
                },
                {
                    name: 'pvc数',
                    value: 'PERSISTENTVOLUMECLAIMS'
                },
                {
                    name: '服务数',
                    value: 'SERVICES'
                },
                {
                    name: '服务节点端口数',
                    value: 'SERVICES_NODEPORTS'
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
            $scope.selectpod = $scope.selectdefaultContainer = $scope.selectcontainer = false
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
			var url = '/application/quota' ;
            if(row.quotaStatus=="QUOTA"){
                $scope.selectD = true
                $scope.deleteBtn = true
                $scope.msgName = "修改服务配额"
                url = '/application/update/quota' ;
                httpLoad.loadData({
                    url:'/application/quota',
                    method:'GET',
                    data: {applicationId:row.id},
                    success:function(data){
                        if(data.success){
                        	var dataObj = data.data;
							if(dataObj.COMPUTE_RESOURCE !=null){
                                $scope.addData.memoryRequest = dataObj.COMPUTE_RESOURCE.REQUESTS_MEMORY.match(/\d+/gi).join()/1;
                                $scope.unitData.memoryRequest = dataObj.COMPUTE_RESOURCE.REQUESTS_MEMORY.match(/[a-zA-Z]+/gi).join();
                                
                                $scope.addData.memoryLimit = dataObj.COMPUTE_RESOURCE.LIMITS_MEMORY.match(/\d+/gi).join()/1;
                                $scope.unitData.memoryLimit = dataObj.COMPUTE_RESOURCE.LIMITS_MEMORY.match(/[a-zA-Z]+/gi).join();
                                
                                $scope.addData.cpuRequest = dataObj.COMPUTE_RESOURCE.REQUESTS_CPU.match(/\d+/gi).join()/1 ;
                                if(dataObj.COMPUTE_RESOURCE.REQUESTS_CPU.match(/[a-zA-Z]+/gi)){
                                	$scope.unitData.cpuRequest = dataObj.COMPUTE_RESOURCE.REQUESTS_CPU.match(/[a-zA-Z]+/gi).join();	
                                }else{
                                	$scope.unitData.cpuLimit = ""
                                }
                             
                                $scope.addData.cpuLimit = dataObj.COMPUTE_RESOURCE.LIMITS_CPU.match(/\d+/gi).join()/1
                               
                                if(dataObj.COMPUTE_RESOURCE.LIMITS_CPU.match(/[a-zA-Z]+/gi)){
                                	 $scope.unitData.cpuLimit = dataObj.COMPUTE_RESOURCE.LIMITS_CPU.match(/[a-zA-Z]+/gi).join();
                                }else{
                                	$scope.unitData.cpuLimit = ""
                                }

							}
                            if(dataObj.OBJECT_COUNT !=null){
                            	$scope.selectA = true
                                $scope.getConfigManageList = []
								for(var item in dataObj.OBJECT_COUNT){
                                    $scope.getConfigManageList.push({name:item,path:dataObj.OBJECT_COUNT[item]/1})
								}
                            }
                            if(dataObj.LIMIT_RANGE !=null){
                                $scope.selectC = true
								if(dataObj.LIMIT_RANGE_DEFAULT_CONTAINER){
                                    $scope.selectdefaultContainer = true
									var obj = dataObj.LIMIT_RANGE_DEFAULT_CONTAINER
                                    $scope.defaultContainer.defaultCpu = obj.defaultCpu.match(/\d+/gi).join()/1;
                                    
                                    if(obj.defaultCpu.match(/[a-zA-Z]+/gi)){
                                   	 $scope.unitData.defaultCpu = obj.defaultCpu.match(/[a-zA-Z]+/gi).join();	
                                   }else{
                                	   $scope.unitData.defaultCpu = ""
                                   }
                                    
                                   
                                  
                                    $scope.defaultContainer.defaultMemory = obj.defaultMemory.match(/\d+/gi).join()/1;
                                    $scope.unitData.defaultMemory = obj.defaultMemory.match(/[a-zA-Z]+/gi).join();

                                    $scope.defaultContainer.defaultReqCpu = obj.defaultReqCpu.match(/\d+/gi).join()/1;
                                    if(obj.defaultCpu.match(/[a-zA-Z]+/gi)){
                                    	  $scope.unitData.defaultReqCpu = obj.defaultReqCpu.match(/[a-zA-Z]+/gi).join();
                                   }else{
                                	   $scope.unitData.defaultReqCpu = ""
                                   }
                                  

                                    $scope.defaultContainer.defaultReqMemory = obj.defaultReqMemory.match(/\d+/gi).join()/1;
                                    $scope.unitData.defaultReqMemory = obj.defaultReqMemory.match(/[a-zA-Z]+/gi).join();
								}
                                
                                if(dataObj.LIMIT_RANGE_CONTAINER){
                                    $scope.selectcontainer = true
                                    var obj = dataObj.LIMIT_RANGE_CONTAINER
                                    $scope.container.maxContainerCpu = obj.maxContainerCpu.match(/\d+/gi).join()/1;
                                    if(obj.maxContainerCpu.match(/[a-zA-Z]+/gi)){
                                  	  $scope.unitData.maxContainerCpu = obj.maxContainerCpu.match(/[a-zA-Z]+/gi).join();
                                 }else{
                              	   $scope.unitData.maxContainerCpu = ""
                                 }
                                   
                                    $scope.container.maxContainerMemory = obj.maxContainerMemory.match(/\d+/gi).join()/1;
                                    $scope.unitData.maxContainerMemory = obj.maxContainerMemory.match(/[a-zA-Z]+/gi).join();

                                    $scope.container.minContainerCpu = obj.minContainerCpu.match(/\d+/gi).join()/1;
                                    if(obj.minContainerCpu.match(/[a-zA-Z]+/gi)){
                                  	  $scope.unitData.minContainerCpu = obj.minContainerCpu.match(/[a-zA-Z]+/gi).join();
                                 }else{
                              	   $scope.unitData.minContainerCpu = ""
                                 }
                                    

                                    $scope.container.minContainerMemory = obj.minContainerMemory.match(/\d+/gi).join()/1;
                                    $scope.unitData.minContainerMemory = obj.minContainerMemory.match(/[a-zA-Z]+/gi).join();
                                }
                                
                                if(dataObj.LIMIT_RANGE_POD){
                                    $scope.selectpod = true;
                                    var obj = dataObj.LIMIT_RANGE_POD;
                                    $scope.pod.maxPodCpu = obj.maxPodCpu.match(/\d+/gi).join()/1;
                                    if(obj.maxPodCpu.match(/[a-zA-Z]+/gi)){
                                  	  $scope.unitData.maxPodCpu = obj.maxPodCpu.match(/[a-zA-Z]+/gi).join();
                                 }else{
                              	   $scope.unitData.maxPodCpu = ""
                                 }
                                    
                                    $scope.pod.maxPodMemory = obj.maxPodMemory.match(/\d+/gi).join()/1;
                                    $scope.unitData.maxPodMemory = obj.maxPodMemory.match(/[a-zA-Z]+/gi).join();

                                    $scope.pod.minPodCpu = obj.minPodCpu.match(/\d+/gi).join()/1;
                                    if(obj.minPodCpu.match(/[a-zA-Z]+/gi)){
                                  	  $scope.unitData.minPodCpu = obj.minPodCpu.match(/[a-zA-Z]+/gi).join();
                                 }else{
                              	   $scope.unitData.minPodCpu = ""
                                 }
                                   
                                    $scope.pod.minPodMemory = obj.minPodMemory.match(/\d+/gi).join()/1;
                                    $scope.unitData.minPodMemory = obj.minPodMemory.match(/[a-zA-Z]+/gi).join();
                                }
                            }


                        }
                    }
                });

                httpLoad.loadData({
                    url:'/application/quota/used',
                    method:'GET',
                    data: {applicationId:row.id},
                    success:function(data){
                        if(data.success){
                            $scope.supplierDetail = data.data.COMPUTE_RESOURCE;
                            $scope.supplier = data.data.OBJECT_COUNT;
                        }
                    }
                });
			}
            $scope.checkConfigManageList = function (key) {
                var keyArr=[];
                $scope.getConfigManageFlag=true;
                $scope.getConfigManageList.forEach(function (item) {
                    if((item.name)&&(keyArr.indexOf(item.name)>-1)){
                        $scope.getConfigManageFlag=false;
                    }else{
                        keyArr.push(item.name);
                    }
                })
                if(!$scope.getConfigManageFlag) {
                	$scope.pop('对象数不能重复，请修改选择项','error');
                    $scope.getConfigManageList.splice(key,1,[{name:'',path:''}])
                }
            }
            $scope.setStore = function(flag) {
                switch (flag / 1) {
                    case 1:
                        $scope.selectpod = true
                        break;
                    case 2:
                        $scope.selectdefaultContainer = true
                        break;
                    case 3:
                        $scope.selectcontainer = true
                        break;
                }
            }
            $scope.delete = function(){
            	  var modalInstance = $modal.open({
                      templateUrl: '/statics/tpl/template/delModal.html',
                      controller: 'delModalCtrl',
                      backdrop: 'static',
                      resolve:{
                          tip: function () {
                              return '你确定要删除吗？';
                          },
                          btnList:function(){
                              return  [{name:'确定',type:'btn-info'},{name:'取消',type:'btn-cancel'}];
                          }
                      }
                  });
                  modalInstance.result.then(function() {
                	    httpLoad.loadData({
                            url:'/application/quota',
                            method:'DELETE',
                            data: {applicationId:row.id},
                            success:function(data){
                                if(data.success){
                                    $scope.pop(data.message);
                                    $modalInstance.close();
                                }
                            }
                        });
                  });
            	
            
			}
            $scope.ok = function(){
            	var params={
                    applicationId:row.id,
					hard:{}
				}
				if($scope.selectB){
                    params.hard={
                        "requests.cpu":$scope.addData.cpuRequest+ $scope.unitData.cpuRequest,
						"requests.memory":$scope.addData.memoryRequest+ $scope.unitData.memoryRequest,
						"limits.cpu":$scope.addData.cpuLimit+ $scope.unitData.cpuLimit,
						"limits.memory":$scope.addData.memoryLimit+ $scope.unitData.memoryLimit,
					}
				} if($scope.selectA){
                    for(var r=0;r<$scope.getConfigManageList.length;r++){
                        var items = $scope.getConfigManageList[r];
                        if((!items.name&&items.path)||(items.name&&!items.path)){
                            $scope.pop('请添加完整的配置管理','error');
                            return;
                        }else if(!items.name&&!items.path){
                            continue;
                        }else{
                            params.hard[$scope.mapList[items.name]]=items.path
                        }
                    }
				} if($scope.selectpod){
                    $scope.limitRange.pod = {
                    "maxPodCpu":$scope.pod.maxPodCpu + $scope.unitData.maxPodCpu,
                    "maxPodMemory":$scope.pod.maxPodMemory + $scope.unitData.maxPodMemory,
                    "minPodCpu":$scope.pod.minPodCpu + $scope.unitData.minPodCpu,
                    "minPodMemory":$scope.pod.minPodMemory + $scope.unitData.minPodMemory,
					}
				} if($scope.selectdefaultContainer){
                    $scope.limitRange.defaultContainer ={
                        "defaultCpu":$scope.defaultContainer.defaultCpu + $scope.unitData.defaultCpu,
						"defaultMemory":$scope.defaultContainer.defaultMemory + $scope.unitData.defaultMemory,
						"defaultReqCpu":$scope.defaultContainer.defaultReqCpu + $scope.unitData.defaultReqCpu,
						"defaultReqMemory":$scope.defaultContainer.defaultReqMemory + $scope.unitData.defaultReqMemory,
                    }
                } if($scope.selectcontainer){
                    $scope.limitRange.container = {
                        "maxContainerCpu":$scope.container.maxContainerCpu + $scope.unitData.maxContainerCpu,
						"maxContainerMemory":$scope.container.maxContainerMemory + $scope.unitData.maxContainerMemory,
						"minContainerCpu":$scope.container.minContainerCpu + $scope.unitData.minContainerCpu,
						"minContainerMemory":$scope.container.minContainerMemory + $scope.unitData.minContainerMemory,
                    }
                }
                params.limitRange = $scope.limitRange;
                httpLoad.loadData({
                    url:url,
                    method:'POST',
                    data: params,
                    success:function(data){
                        if(data.success){
                            $scope.pop(data.message);
                            $modalInstance.close();
                        }
                    }
                });
            };
            $scope.cancel = function(){
                $modalInstance.dismiss('cancel');
            }
        }]);
//导入
    angular.module('app').controller('gridinModalCtrl',['$scope','$modalInstance','httpLoad','LANGUAGE',
			function($scope,$modalInstance,httpLoad,LANGUAGE){ //依赖于modalInstance

				$scope.ok = function(){
					httpLoad.loadData({
						url:'/user/remove',
						method:'POST',
						data: removeData,
						success:function(data){
							if(data.success){
								//console.log(removeData);
								$scope.pop(data.message);
								$modalInstance.close();
							}
						}
					});
				};
				$scope.cancel = function(){
			$modalInstance.dismiss('cancel');
				}
			}]);
			app.controller('applyhistoryModalCtrl', ['$rootScope', '$scope','$state','httpLoad','$stateParams',function($rootScope, $scope,$state,httpLoad,$stateParams) {
				$rootScope.link = '/statics/css/alarm.css';//引入页面样式
			  $scope.param = {
			  		page:1,
			 	        rows: 10,
			 	        params:angular.toJson([{"param":{"object":"application"},"sign":"LK"}])
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

})();
