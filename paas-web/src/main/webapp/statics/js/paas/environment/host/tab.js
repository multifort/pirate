(function(){
    app.controller('virtualCtrl', ['$scope', 'httpLoad', '$rootScope','$modal','$state','$timeout','webSocket',
        function($scope, httpLoad, $rootScope, $modal,$state, $timeout,webSocket) {
            $rootScope.moduleTitle = '环境资源 > 主机管理';//定义当前页
            $scope.statusTypeData = {1:"正常",2:"异常",3:"可调度",4:"不可调度",5:"添加中",6:"移出中"};
            $scope.nodeSourceData = {'receive':"接管",'create':"创建"};
            var itemsNotRemove="";
            var itemsNotDelete="";
            var itemsNotJoin="";
            $scope.param = {
                rows: 10,
                page:1
            };
            //  websocket异步操作
            webSocket.onmessage({
                message:function (data) {
                    if($rootScope.currentUrl=='paas.environment.host'){
                            $scope.getData(1);
                    }
                }
            });
            //获取云主机列表及根据条件查询
            $scope.getData = function (page) {
                $scope.isBatch = true;$scope.selectAll = false;
                $scope.param.page = page || $scope.param.page;
                var params = {
                        page: $scope.param.page,
                        rows: $scope.param.rows
                    },
                    searchParam = [];
                if ($scope.searchByName && $scope.searchByName != "") {
                    searchParam.push({"param": {"name": $scope.searchByName}, "sign": "LK"});

                }
                if ($scope.searchByStatus && $scope.searchByStatus != "") {
                    searchParam.push({"param": {"status": $scope.searchByStatus}, "sign": "EQ"});
                }
                params.params = JSON.stringify(searchParam);
                httpLoad.loadData({
                    url: '/host/list',
                    method: 'POST',
                    data: params,
                    noParam: true,
                    success: function (data) {
                        if (data.success && data.data.rows) {
                            $scope.vmList = data.data.rows;
                            $scope.totalCount = data.data.total;
                            // 单个主机的操作限制
                            $scope.vmList.forEach(function(item){
                                item.canRemove=((item.source=='create')&&((item.status==3)||(item.status==4)));
                                item.canJoin=(item.status==1);
                                item.canDelete=((!item.envName)&&((item.status==1)||(item.status==2)));
                            });
                        } else {
                            $scope.isImageData = true;
                        }
                    }
                });
            };
            $scope.getData(1);
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

            //状态数据
            $scope.statusData = [{"value": "1", "name": "正常"}, {
                "value": "2",
                "name": "异常"
            }, {
                "value": "3",
                "name": "可调度"
            }, {
                "value": "4",
                "name": "不可调度"
            },{
                "value": "5",
                "name": "添加中"
            },{
                "value": "6",
                "name": "移出中"
            }];
            $scope.isBatch = true;$scope.selectAll = false;
            //批量操作
            $scope.chooseAll = function(){
                $scope.notRemoveAll=false;
                $scope.notJoinAll=false;
                $scope.notDeleteAll=false;
                $scope.notInSameEnv=false;
                itemsNotRemove="";
                itemsNotDelete="";
                itemsNotJoin="";
                var envNameArr=[];
                $scope.isBatch = !$scope.selectAll;
                $scope.vmList.forEach(function(item){
                    item.isSelect = $scope.selectAll;
                    if(item.isSelect==true) {
                        if(!item.canRemove) {
                            itemsNotRemove+=(item.name)+",";
                            $scope.notRemoveAll=true;
                        }
                        if(!item.canJoin) {
                            itemsNotJoin+=(item.name)+",";
                            $scope.notJoinAll=true;
                        }
                        if(!item.canDelete) {
                            itemsNotDelete+=(item.name)+",";
                            $scope.notDeleteAll=true;
                        }
                        if(item.envName){
                            if(envNameArr.indexOf(item.envName)==-1)envNameArr.push(item.envName);
                        }
                    }
                });
                if(envNameArr.length>1)$scope.notInSameEnv=true;
            };
            // 批量删除的限制条件（只有当主机未处于环境中且状态正常或异常时才可删除）
            //批量加入时有限制条件(只有当主机处于正常状态时才可加入环境)
            //批量移出时有限制条件（只有处于相同环境且处于可调度或不可调度且来源为创建的节点才能移除）
            $scope.choose = function(){
                var a = 0,b=0;
                $scope.notRemoveAll=false;
                $scope.notJoinAll=false;
                $scope.notDeleteAll=false;
                $scope.notInSameEnv=false;
                 itemsNotRemove="";
                 itemsNotDelete="";
                 itemsNotJoin="";
                var envNameArr=[];
                $scope.vmList.forEach(function(item){
                    if(item.isSelect==true) {
                        if(!item.canRemove) {
                            itemsNotRemove+=(item.name)+",";
                            $scope.notRemoveAll=true;
                        }
                        if(!item.canJoin) {
                            itemsNotJoin+=(item.name)+",";
                            $scope.notJoinAll=true;
                        }
                        if(!item.canDelete) {
                            itemsNotDelete+=(item.name)+",";
                            $scope.notDeleteAll=true;
                        }
                        if(item.envName){
                            if(envNameArr.indexOf(item.envName)==-1)envNameArr.push(item.envName);
                        }
                        a++;
                    }
                    else b=1;
                });
                if(a>=1) $scope.isBatch = false;
                else $scope.isBatch = true;
                if(b==1) $scope.selectAll = false;
                else $scope.selectAll = true;

                if(envNameArr.length>1)$scope.notInSameEnv=true;
            };
            $scope.batch = function($event,value){
                $event.stopPropagation();
                $scope.operationData = [];var operationData =[];
                $scope.vmList.forEach(function(item){
                    if(item.isSelect==true) operationData .push(item.id);
                });
                $scope.operationData = operationData;
                switch (value / 1) {
                    case 6:
                        //删除
                        if( $scope.notDeleteAll) {
                            $scope.pop('只有主机未处于环境中且状态正常或异常时才可删除，主机'+itemsNotDelete+'条件不符合，请重新选择','error');
                            return false;
                        }
                        $scope.remove($event,{'id':$scope.operationData,'status':'','batchUrl':1});
                        break;
                    case 2:
                        // 加入环境
                        if( $scope.notJoinAll) {
                            $scope.pop('只有当主机处于正常状态时才可加入环境，主机'+itemsNotJoin+'条件不符合，请重新选择','error');
                            return false;
                        }
                        $scope.addMaster($event,{'id':$scope.operationData,'status':'','batchUrl':1});
                        break;
                    case 1:
                      // 移出环境
                        if( $scope.notRemoveAll) {
                            $scope.pop('只有主机处于可调度或不可调度状态且节点来源为创建时才能移出环境，主机'+itemsNotRemove+'条件不符合，请重新选择','error');
                            return false;
                        }
                        if($scope.notInSameEnv){
                            $scope.pop('主机符合移出条件且处于相同环境中时才能批量移出,当前选择项未处于同一环境中，请重新选择','error');
                            return false;
                        }
                        $scope.removeMaster($event,{'id':$scope.operationData,'status':'','batchUrl':1});
                        break;
                }

            };
            //跳转详情页
            $scope.goDetail = function ($event,row) {
              	$state.go('paas.environment.hostDetail', {id:row.id,hostName:row.hostName,envId:row.envId,ip:row.ip})
            };
            //新增
            $scope.add = function (a,row) {
              var modalInstance = $modal.open({
                  templateUrl : '/statics/tpl/environment/host/addModal.html',
                  controller : 'addResourceModalCtrl',// 初始化模态范围
                    backdrop: 'static',
                  resolve : {
                    id : function(){
                      return null;
                    }
                  }
              });
              modalInstance.result.then(function(){
                  $scope.getData(1);
              },function(){});
            };
            //编辑
            $scope.edit = function (a,row) {
                var modalInstance = $modal.open({
                    templateUrl : '/statics/tpl/environment/host/addModal.html',
                    controller : 'editResourceModalCtrl',// 初始化模态范围
                    backdrop: 'static',
                    resolve : {
                        id : function(){
                            return row.id;
                        }
                    }
                });
                modalInstance.result.then(function(){
                    $scope.getData(1);
                },function(){});
            };
            //加入环境
            $scope.addMaster = function ($event,row) {
                var modalInstance = $modal.open({
                    templateUrl: '/statics/tpl/environment/host/addMaster.html',
                    controller: 'addResourceMasterModalCtrl',
                    backdrop: 'static',
                    resolve: {
                        id :  function () {
                            var oldId=row.id;
                            var newId=[];
                            if(oldId.constructor == Array){
                                newId=oldId;
                            }else{
                                newId.push(oldId);
                            }
                            return newId;
                        }
                    }
                });
                modalInstance.result.then(function (data) {
                    $scope.getData(1);
                },function(){});
            };
            //移除环境
            $scope.removeMaster = function ($event,row) {
                var modalInstance = $modal.open({
                    templateUrl: '/statics/tpl/environment/host/remove.html',
                    controller: 'removeResourceMasterModalCtrl',
                    resolve: {
                      id :  function () {
                          var oldId=row.id;
                          var newId=[];
                          if(oldId.constructor == Array){
                              newId=oldId;
                          }else{
                              newId.push(oldId);
                          }
                          return newId;
                      }
                    }
                });
                modalInstance.result.then(function (data) {
                    $scope.getData();
                },function(){});
            };
            //节点调度
            $scope.changeStatus = function ($event,row) {
                var modalInstance = $modal.open({
                    templateUrl: '/statics/tpl/environment/host/remove.html',
                    controller: 'changeStatusModalCtrl',
                    resolve: {
                        id :  function () {
                            return row.id;
                        }
                    }
                });
                modalInstance.result.then(function (data) {
                    $scope.getData();
                });
            };

            //删除6,备份3，快照4，恢复0，暂停7，挂起8，激活9，重启1，开机2，关机5
            $scope.remove = function($event,row){
                var modalInstance = $modal.open({
                    templateUrl : '/statics/tpl/environment/host/remove.html',
                    controller : 'removeResourceModalCtrl',
                    resolve : {
                        id : function(){
                            var oldId=row.id;
                            var newId=[];
                           if(oldId.constructor == Array){
                               newId=oldId;
                           }else{
                               newId.push(oldId);
                           }
                            return newId;
                        }
                    }
                });
                modalInstance.result.then(function(){
                    $scope.getData();
                },function(){});
            };
        }
    ]);

    //新增ctrl
     angular.module('app').controller('addResourceModalCtrl',['$scope','$modalInstance','LANGUAGE','httpLoad','id','$timeout',
         function($scope,$modalInstance,LANGUAGE,httpLoad,id,$timeout){ //依赖于modalInstance
          $scope.addData = {};
          $scope.modalName = '主机创建';
             $scope.startIpLast="";
             $scope.addData.labels = [{keys:'',values:''}];
             $scope.addData.ipRange = "";
             $scope.endIPStr="";
             $scope.endIPEdit=true;
             $scope.isCheck=true;
             $scope.addLabels = function(){
                 //做验证-》只有上面用户组有用户下面才可以继续添加用户组
                 $scope.addData.labels.push({keys:'',values:''})
                 $timeout(function () {
                     $(".labellista").hide().last().show()
                 },100)
             }
             $scope.removeLabel = function(key){
                 if($scope.addData.labels.length == 1) return $scope.pop('请至少添加一组','error');
                 $scope.addData.labels.splice(key,1);
                 $timeout(function () {
                     $(".labellista").last().show()
                 },100)
             }
             $scope.endIPShow=function (flag) {
                 if(flag){
                     var arr= $scope.addData.ip.split(".");
                         $scope.startIpLast=arr.pop();
                         var str=arr.join(".");
                     $scope.endIPStr= str+".";
                     $scope.endIPEdit=false;
                 }else{
                     $scope.endIPStr= "";
                     $scope.addData.ipRange = "";
                     $scope.endIPEdit=true;
                 }
             }
        // 创建主机确认
            $scope.ok = function () {
                    var param =$scope.addData ;
                if((param.ipRange)&&(param.ipRange<$scope.startIpLast)){
                    $scope.pop('结束IP不能小于起始IP，请重新填写','error')
                    return false;
                }else if((param.ipRange)&&(param.ipRange==$scope.startIpLast)){
                    param.ipRange=""
                }
                    httpLoad.loadData({
                        url:'/host/create',
                        method: 'POST',
                        data: param,
                        noParam: false,
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

    angular.module('app').controller('editResourceModalCtrl',['$scope','$modalInstance','LANGUAGE','httpLoad','id','$timeout',
        function($scope,$modalInstance,LANGUAGE,httpLoad,id,$timeout){ //依赖于modalInstance
            $scope.addData ={};
            $scope.isCheck =true;
            $scope.modalName = '主机编辑';
            $scope.addData.labels = [{keys:'',values:''}];
            url = '/host/detail';
            httpLoad.loadData({
                url: url,
                method: 'GET',
                data: {id: id},
                success: function (data) {
                    if (data.success) {
                        data.data.labels=angular.fromJson(data.data.labels);
                        if(!data.data.labels){
                            data.data.labels=[{keys:'',values:''}];
                        }
                        $scope.addData=data.data;
                        if($scope.addData.source=='create'){
                            $scope.isCheck =true;
                        }else if($scope.addData.source=='receive'){
                            $scope.isCheck =false;
                        }
                        }
                    }
            });
            $scope.addLabels = function(){
                //做验证-》只有上面用户组有用户下面才可以继续添加用户组
                $scope.addData.labels.push({keys:'',values:''})
            }
            $scope.removeLabel = function(key){
                if($scope.addData.labels.length == 1) return $scope.pop('请至少添加一组','error');
                $scope.addData.labels.splice(key,1);
            }

            // 编辑确认
            $scope.ok = function () {
                var param=$scope.addData;
                if(((param.password)&&(!param.username))||((!param.password)&&(param.username))){
                    $scope.pop('请填写完整用户名和密码','error');
                    return false;
                }
                httpLoad.loadData({
                    url:'/host/modify',
                    method: 'POST',
                    data: param,
                    noParam: false,
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
angular.module('app').controller('removeResourceModalCtrl',['$scope','$modalInstance','httpLoad','LANGUAGE','id',
	function($scope,$modalInstance,httpLoad,LANGUAGE,id){ //依赖于modalInstance
		$scope.content = '您确定要删除吗？';
		$scope.ok = function(){
			httpLoad.loadData({
				url:'/host/remove',
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
    //节点调度ctrl
    angular.module('app').controller('changeStatusModalCtrl',['$scope','$modalInstance','httpLoad','LANGUAGE','id',
        function($scope,$modalInstance,httpLoad,LANGUAGE,id){ //依赖于modalInstance
            $scope.content = '您确定要调度该节点的状态吗？';
            $scope.ok = function(){
                httpLoad.loadData({
                    url:'/host/scheduleNode',
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

//加入环境
 angular.module('app').controller('addResourceMasterModalCtrl',['$scope','$modalInstance','LANGUAGE','httpLoad','id','$timeout',
     function($scope,$modalInstance,LANGUAGE,httpLoad,id,$timeout){ //依赖于modalInstance
      $scope.modalName = '加入环境';
         httpLoad.loadData({
             url:'/environment/queryNormalEnv' ,
             method:'POST',
             data: {},
             success:function(data){
                 if(data.success){
                     $scope.masterList = data.data;
                 }
             }
         });
        $scope.ok = function () {
             httpLoad.loadData({
                 url:'/environment/addNode' ,
                 method:'POST',
                 data: {ids:id,envId:$scope.master},
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
     //移出ctrl
     angular.module('app').controller('removeResourceMasterModalCtrl',['$scope','$modalInstance','httpLoad','LANGUAGE','id',
     	function($scope,$modalInstance,httpLoad,LANGUAGE,id){ //依赖于modalInstance
     		$scope.content = '您确定要把主机移出环境吗？';
     		$scope.ok = function(){
     			httpLoad.loadData({
     				url:'/environment/deleteNode',
     				method:'POST',
     				data: {ids:id},
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
    app.controller('hostHistoryModalCtrl', ['$rootScope', '$scope','$state','httpLoad','$stateParams',function($rootScope, $scope,$state,httpLoad,$stateParams) {
        $rootScope.link = '/statics/css/alarm.css';//引入页面样式
        $scope.param = {
            page:1,
            rows: 10,
            params:angular.toJson([{"param":{"object":"host"},"sign":"LK"}])
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
    }]);
    function handleId(id) {
        var oldId=id;
        var newId=[];
        if(oldId.constructor == Array){
            newId=oldId;
        }else{
            newId.push(oldId);
        }
        return newId;
    }
})();
