(function(){
    app.controller('codeRepositoryCtrl', ['$scope', 'httpLoad', '$rootScope','$modal','$state','$timeout','webSocket',
        function($scope, httpLoad, $rootScope, $modal,$state, $timeout,webSocket) {
            $rootScope.moduleTitle = '流程管控 > 代码仓库';//定义当前页
            $scope.statusTypeData = {0:"激活",1:"锁定"};
            $scope.typeData =['公有','私有'];
            $scope.nodeSourceData = {'receive':"接管",'create':"创建"};
            $scope.param = {
                rows: 10,
                page:1
            };
            //  websocket异步操作
            webSocket.onmessage({
                message:function (data) {
                    if($rootScope.currentUrl=='paas.process.codeRepository'){
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
                var eqParams={"param": {}, "sign": "EQ"};
                if ($scope.searchByStatus && $scope.searchByStatus != "") eqParams.param.status=$scope.searchByStatus;
                if ($scope.searchByType && $scope.searchByType != "") eqParams.param.type=$scope.searchByType;
                if (($scope.searchByStatus && $scope.searchByStatus != "")||($scope.searchByType && $scope.searchByType != "")) searchParam.push(eqParams);
                params.params = JSON.stringify(searchParam);
                httpLoad.loadData({
                    url: '/code/repository/code',
                    method: 'GET',
                    data: params,
                    noParam: true,
                    success: function (data) {
                        if (data.success && data.data.rows) {
                            $scope.codeList = data.data.rows;
                            $scope.totalCount = data.data.total;
                        } else {
                            $scope.isImageData = true;
                        }
                    }
                });
            };
            $scope.getData(1);
            //websocket异步操作
            webSocket.onmessage({
                message:function (data) {
                    if($rootScope.currentUrl=='paas.environment.host'&&$scope.active2&&data.operate.indexOf('vm')>-1){
                        $scope.pop(data.message,data.success?'success':'error');
                        $scope.getData();
                    }
                }
            });
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
            $scope.statusData = [{"value": "0", "name": "激活"}, {"value": "1", "name": "锁定"}];
            $scope.isBatch = true;$scope.selectAll = false;
            //批量操作
            $scope.chooseAll = function(){
                $scope.isBatch = !$scope.selectAll;
                $scope.codeList.forEach(function(item){
                    item.isSelect = $scope.selectAll;
                });
            };
            $scope.choose = function(){
                var a = 0,b=0;
                $scope.codeList.forEach(function(item){
                    if(item.isSelect==true) a++;
                    else b=1;
                });
                if(a>=1) $scope.isBatch = false;
                else $scope.isBatch = true;
                if(b==1) $scope.selectAll = false;
                else $scope.selectAll = true;
            };
            $scope.batch = function($event,value){
                $event.stopPropagation();
                $scope.operationData = [];var operationData =[];
                $scope.codeList.forEach(function(item){
                    if(item.isSelect==true) operationData .push(item.id);
                });
                $scope.operationData = operationData;
                switch (value / 1) {
                    case 6:
                        //删除
                        $scope.remove($event,{'id':$scope.operationData,'status':'','batchUrl':1});
                        break;
                }

            };
            //跳转详情页
            $scope.goDetail = function ($event,row) {
              	$state.go('paas.process.codeRepositoryDetail', {id:row.id})
            };
            //新增
            $scope.add = function (a,row) {
                var id="";
                if(row){
                    id = row.id
                }else {id = null}
              var modalInstance = $modal.open({
                  templateUrl : '/statics/tpl/process/codeRepository/addModal.html',
                  controller : 'addcodeRepositoryModalCtrl',// 初始化模态范围
                    backdrop: 'static',
                  resolve : {
                    id : function(){
                      return id;
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
                    templateUrl : '/statics/tpl/process/codeRepository/addModal.html',
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
            //修改状态
            $scope.changeStatus = function ($event,row) {
                var modalInstance = $modal.open({
                    templateUrl: '/statics/tpl/process/codeRepository/remove.html',
                    controller: 'changeCodeStatusModalCtrl',
                    resolve: {
                        row :  function () {
                            return row;
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
                    templateUrl : '/statics/tpl/process/codeRepository/remove.html',
                    controller : 'removeCodeModalCtrl',
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
     angular.module('app').controller('addcodeRepositoryModalCtrl',['$scope','$modalInstance','LANGUAGE','httpLoad','id','$timeout',
         function($scope,$modalInstance,LANGUAGE,httpLoad,id,$timeout){ //依赖于modalInstance
             var editObj = ['name', 'codeSource','softwareType','type','username','password','remark'];
          $scope.addData = {};
          $scope.addData.softwareType = 0;
             $scope.addData.type = "公有";
          $scope.modalName = '代码仓库创建';
             var url = '/code/repository/code';
             //如果为编辑，进行赋值
             if (id) {
                 url = '/code/repository/update/code';
                 $scope.modalName = '代码仓库编辑';
                 httpLoad.loadData({
                     url: '/code/repository/'+id+'/code',
                     method: 'GET',
                     data: {},
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
        // 创建主机确认
            $scope.ok = function () {
                var param = {};
                for (var a in editObj) {
                    var attr = editObj[a];
                    param[attr] = $scope.addData[attr];
                }
                if (id) param.id = id;
                    httpLoad.loadData({
                        url:url,
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
angular.module('app').controller('removeCodeModalCtrl',['$scope','$modalInstance','httpLoad','LANGUAGE','id',
	function($scope,$modalInstance,httpLoad,LANGUAGE,id){ //依赖于modalInstance
		$scope.content = '您确定要删除吗？';
		$scope.ok = function(){
			httpLoad.loadData({
				url:'/code/repository/code',
				method:'DELETE',
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
    //改变状态
    angular.module('app').controller('changeCodeStatusModalCtrl',['$scope','$modalInstance','httpLoad','LANGUAGE','row',
        function($scope,$modalInstance,httpLoad,LANGUAGE,row){ //依赖于modalInstance
            var id=row.id;
            var text="";
            if(row.status==0){
                text="锁定";
            }else if(row.status==1){
                text="激活";
            }
            $scope.content = '您确定要'+text+'吗？';
            $scope.ok = function(){
                httpLoad.loadData({
                    url:'/code/repository/update/'+id+'/status',
                    method:'POST',
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
    app.controller('codeRepositoryHistoryModalCtrl', ['$rootScope', '$scope','$state','httpLoad','$stateParams',function($rootScope, $scope,$state,httpLoad,$stateParams) {
        $rootScope.link = '/statics/css/alarm.css';//引入页面样式
        $scope.param = {
            page:1,
            rows: 10,
            params:angular.toJson([{"param":{"object":"code"},"sign":"LK"}])
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
