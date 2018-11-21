(function () {
	"use strict";
	app.controller('loadbalanceCtrl', ['$scope', 'httpLoad','webSocket', '$rootScope', '$modal', '$state', '$timeout','$location',
	'$anchorScroll','CommonData',
		function ($scope, httpLoad,webSocket, $rootScope, $modal, $state, $timeout,$location,$anchorScroll,CommonData) {
			$rootScope.moduleTitle = '<a ui-sref="paas.repository.repository">环境资源</a> > 负载管理';//定义当前页
	    $rootScope.link = '/statics/css/image.css';//引入页面样式
		  $scope.isListView = true;
			$scope.statusData = {"0":"正常","1":"不正常"};
			$scope.typeData = {"1":"NGINX","2":"F5"};
			$scope.param = {
				page:1,
				rows: 10
			};
			//websocket异步操作
			webSocket.onmessage({
				message:function (data) {
					if($rootScope.currentUrl=='paas.environment.loadbalance'){
						$scope.getData();
					}
				}
			});

			//获取云主机列表
			$scope.getData = function (page) {
				$scope.param.page = page || $scope.param.page;
				var params = {
						page: $scope.param.page,
						rows: $scope.param.rows
					},
					searchParam = [];
				if ($scope.searchByName && $scope.searchByName != "") {
					searchParam.push({"param": {"name": $scope.searchByName}, "sign": "LK"});
				}
				if ($scope.searchByType && $scope.searchByType != "") {
					searchParam.push({"param": {"type": $scope.searchByType}, "sign": "EQ"});
				}if ($scope.searchByStatus && $scope.searchByStatus != "") {
					searchParam.push({"param": {"status": $scope.searchByStatus}, "sign": "EQ"});
				}
				params.params = JSON.stringify(searchParam);
				httpLoad.loadData({
					url: '/loadBalance/list',
					method: 'POST',
					data: params,
					noParam: true,
					success: function (data) {
						if (data.success) {
							$scope.countData = data.data.rows;
							$scope.totalCount = data.data.total;
							if(!$scope.totalCount){
								$scope.isImageData = true;
								} else {
									$scope.isImageData = false;
							}
							$scope.gopost = $rootScope.userData.id
						}
					}
				});
			};
		/*	//获概述列表
			$scope.getopenDetail = function (page) {
				$scope.param.page = page || $scope.param.page;
				httpLoad.loadData({
					url: '/registry/count',
					method: 'POST',
					data: $scope.param,
					noParam: true,
					success: function (data) {
						if (data.success) {
							$scope.countopenDetail = data.data;

						}
					}
				});
			};

			$scope.getopenDetail(1);*/
			$scope.getData(1);



			//详情
			$scope.detail = function(id,port){
				$state.go('paas.environment.loadbalancedetail', {id: id})

			}
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

			//返回
			$scope.goAction = function (flag,row) {
				switch (flag / 1) {
					case 1:
					//新增
					var modalInstance = $modal.open({
							templateUrl : '/statics/tpl/environment/loadbalance/add.html',
							controller : 'adddatastoreModalCtrl',// 初始化模态范围
								backdrop: 'static',
							resolve : {
								id : function(){
									return null;
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
                templateUrl : '/statics/tpl/environment/loadbalance/add.html',
                controller : 'adddatastoreModalCtrl',// 初始化模态范围
								backdrop: 'static',
								resolve : {
									id : function(){
										return row.id;
									},

								}
							});
							modalInstance.result.then(function(){
								$scope.getData(1);
							},function(){});

						break;
					case 3:
						//删除

								var ids=[];
								ids.push(row.id);
								var modalInstance = $modal.open({
									templateUrl : '/statics/tpl/environment/loadbalance/remove.html',
									controller : 'removedatastoreModalCtrl',
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
						//应用管理
						var modalInstance = $modal.open({
								templateUrl : '/statics/tpl/environment/loadbalance/addnode.html',
							controller : 'applydatastoreModalCtrl',
							backdrop: 'static',
							resolve : {
								id : function(){
									return row.id;
								},


							}
						});
						modalInstance.result.then(function(){
							$scope.getData(1);
						},function(){});

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
					templateUrl : '/statics/tpl/environment/loadbalance/remove.html',
					controller : 'removedatastoreModalCtrl',
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
	 angular.module('app').controller('adddatastoreModalCtrl',['$scope','$modalInstance','LANGUAGE','httpLoad','id',
			 function($scope,$modalInstance,LANGUAGE,httpLoad,id){ //依赖于modalInstance
				 // var editObj = ['name','managerIp', 'port',"type", "envId", 'remark'];
   	 		$scope.modalName = '负载创建';
         var url = '/loadBalance/create';
				 $scope.addData = {};
				 $scope.nginx=true;
				 $scope.addData.type=1;
				 $scope.selectType=function (type) {
					 if(type==1){
						 $scope.nginx=true;
						 $scope.addData.type=1;
						 $scope.f5=false;
					 }else if(type==2){
						 $scope.nginx=false;
						 $scope.f5=true;
						 $scope.addData.type=2;
					 }
				 }
	        if (id) {
	                url = '/loadBalance/modify';
	                $scope.modalName = '负载编辑';
	                httpLoad.loadData({
	                    url: '/loadBalance/detail',
	                    method: 'GET',
	                    data: {id: id},
	                    success: function (data) {
	                        if (data.success) {
	                            var data = data.data;
	                          /*  for (var a in editObj) {
	                                var attr = editObj[a];
	                                $scope.addData[attr] = data[attr];
	                            }*/
								$scope.addData= data;
								$scope.selectType($scope.addData.type);

	                        }
	                    }
	                });
	            }
							$scope.isLoadingCheck = true
							$scope.checkName = function(address,username,password){
											if(address && username && password){
												var params={"address":address,"username":username,"password":password}
												httpLoad.loadData({
													url: '/openshift/cluster/master/check',
													method: 'POST',
													data: params,
													//noParam: true,
													success: function (data) {
														if (data.success) {
													$scope.pop(data.message);
														$scope.isLoadingCheck = false
														}
													}
												});
											}

										}
					$scope.ok = function () {
							 var param = {};
						param= $scope.addData;
						if(id){
							param.id=id;
						}
							 /*editObj.forEach(function(attr){
								 console.log($scope.addData);
								 if($scope.addData[attr]){
									 param[attr] = $scope.addData[attr];
								 }
								})*/
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
	angular.module('app').controller('removedatastoreModalCtrl',['$scope','$modalInstance','httpLoad','LANGUAGE','id',
		function($scope,$modalInstance,httpLoad,LANGUAGE,id){ //依赖于modalInstance
			$scope.content = '您确定要删除吗？';
			$scope.ok = function(){
				httpLoad.loadData({
					url:'/loadBalance/remove',
					method:'POST',
					data: {id:id},
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
  //应用管理
   angular.module('app').controller('applydatastoreModalCtrl',['$scope','$modalInstance','LANGUAGE','httpLoad','id',
       function($scope,$modalInstance,LANGUAGE,httpLoad,id){ //依赖于modalInstance
          $scope.modalName = '增加节点';
              //检查名称
          httpLoad.loadData({
              url: '/datastore/addnode',
              method: 'GET',
              data: {id: id},
              success: function (data) {
                  if (data.success) {
                      var data = data.data;
                        $scope.addNode =  data.add
                        $scope.removeNode = data.remove
                  }
              }
          });
          //删除
          $scope.addNodes= function($event){
          	$event.stopPropagation();
            var ids = [];
            for(var i=0;i<$scope.addNode.length;i++){
              var item = $scope.addNode[i]
              if(item.select){
                removeNode.push(item);
              }
            }
          }
          //删除
          $scope.removeNodes = function(){
            var ids = [];
            for(var i=0;i<$scope.removeNode.length;i++){
              var item = $scope.removeNode[i]
              if(item.select){
                addNode.push(item);
              }
            }
          }
          $scope.isLoadingCheck = true
          $scope.checkName = function(address,username,password){
                      if(address && username && password){
                        var params={"address":address,"username":username,"password":password}
                        httpLoad.loadData({
                          url: '/openshift/cluster/master/check',
                          method: 'POST',
                          data: params,
                          //noParam: true,
                          success: function (data) {
                            if (data.success) {
                          $scope.pop(data.message);
                            $scope.isLoadingCheck = false
                            }
                          }
                        });
                      }

                    }
          $scope.ok = function () {
               var param = {id:id};
               editObj.forEach(function(attr){
                 param[attr] = $scope.addData[attr];
                })
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

//历史记录
	app.controller('loadbalanceHistoryModalCtrl', ['$rootScope','$timeout', '$scope','$state','httpLoad','$stateParams','$anchorScroll',
	function($rootScope,$timeout, $scope,$state,httpLoad,$stateParams,$anchorScroll) {
			$rootScope.link = '/statics/css/alarm.css';//引入页面样式
		    $scope.param = {
		    		page:1,
			        rows: 10,
			        params:angular.toJson([{"param":{"object":"loadbalance"},"sign":"LK"}])
			    };
			$scope.getHistory = function (page) {
				$scope.param.page = page||$scope.param.page
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
		            }

		        }
		    });
		  }

			$scope.getHistory(1)
			//详情

		}]);

})();
