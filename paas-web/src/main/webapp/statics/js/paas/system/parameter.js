(function () {
    "use strict";

    app.controller('parameterSystemCtrl', ['$scope', 'httpLoad', '$rootScope', '$modal', '$state', '$timeout','$location','$anchorScroll',
        function ($scope, httpLoad, $rootScope,$modal, $state, $timeout,$location,$anchorScroll) {
            $rootScope.moduleTitle = '系统运维 > 系统参数';//定义当前页
            $rootScope.link = '/statics/css/user.css';//引入页面样式
            $scope.isEditInfo = 1;$scope.isEditMessage = 1;
            $scope.editBtn = false;
            //获取云主机列表
            $scope.getData = function () {

                httpLoad.loadData({
                    url: '/dict/statistic',
                    method: 'GET',
                    data:{},
                    noParam: true,
                    success: function (data) {
                        if (data.success) {
                            $scope.countData = data.data;
                            if($scope.countData){
                                for(var k in $scope.countData){
                                    $scope.editBtn = true;
                                    return false;
                                }
                            }

                        }
                    }
                });
            };

            $scope.getData(1);
            //新增
            $scope.add = function () {
                var modalInstance = $modal.open({
                    templateUrl : '/statics/tpl/system/parameter/addmodul.html',
                    controller : 'addParameterModalCtrl',// 初始化模态范围
                    backdrop: 'static',
                });
                modalInstance.result.then(function(){
                    $scope.getData(1);
                },function(){});
            };
            $scope.edit = function(index){
                $scope.isEditInfo = 2;
                $scope.countData1 = angular.copy($scope.countData);
            };
            $scope.cancel = function(){
                $scope.isEditInfo = 1;
            };
            $scope.ok=function () {
                var dists=[];
               var paramsObj=['name','id','dictKey','dictValue','pvalue','softwareType'];
                var allData=[];
                for(var k in $scope.countData1){
                    $scope.countData1[k].forEach(function (item) {
                        allData.push(item);
                    })
                }
                   allData.forEach(function (item) {
                       var dist={};
                       paramsObj.forEach(function (attr) {
                       dist[attr]=item[attr];
                   })
                       dists.push(dist);
                })
                httpLoad.loadData({
                    url: '/dict/batchModify',
                    method: 'POST',
                    data:{dicts:dists},
                    // noParam: true,
                    success: function (data) {
                        if (data.success) {
                            $scope.pop(data.message);
                            $scope.isEditInfo = 1;
                            $scope.getData(1);
                        }
                    }
                });
            }
            $scope.gohistory = function($event){
                //历史记录
                $event.stopPropagation();
                $scope.isActive = true ;
                $scope.showHistory = true;
            };
            $scope.goBack = function(){
                $scope.isActive = false;
                $timeout(function() {
                    $scope.showHistory = false;
                }, 200);
            };
        }
    ]);
    app.controller('addParameterModalCtrl', ['$scope','$modalInstance', '$modal', '$stateParams', '$timeout', 'httpLoad',
        function ($scope,$modalInstance, $modal, $stateParams, $timeout, httpLoad) {
            $scope.modalName ="创建系统参数";
            //获取模板
            $scope.getTpl=function () {
                httpLoad.loadData({
                    url: "/dict/template",
                    method:'GET',
                    success:function(data){
                        if(data.success){
                              data.data=angular.fromJson(data.data);
                            $scope.modelData = data.data.parameters;
                        }
                    }
                })
            }
            $scope.getTpl();
            $scope.getsoftwareType=function (item) {
                $scope.modelData.forEach(function(item1){
                   if(item1.name=='softwareType'){
                       item1.value=item.value[item.select];
                   }
                })
            }
            //保存按钮
            $scope.ok = function () {
                $scope.param = {};
                $scope.modelData.forEach(function(item){
                        $scope.param[item.name] = item.select;
                })
                    httpLoad.loadData({
                        url: '/dict/create',
                        method:'POST',
                        data: $scope.param,
                        // noParam: true,
                        success:function(data){
                            if(data.success){
                                $scope.pop(data.message);
                                $modalInstance.close();
                            }
                        }
                    });

            }
            $scope.cancel = function () {
                $modalInstance.dismiss('cancel');
            };
        }
    ]);
    app.controller('parameterHistoryModalCtrl', ['$rootScope', '$scope','$state','httpLoad','$stateParams',function($rootScope, $scope,$state,httpLoad,$stateParams) {
        $rootScope.link = '/statics/css/alarm.css';//引入页面样式
        $scope.param = {
            page:1,
            rows: 10,
            params:angular.toJson([{"param":{"object":"dict"},"sign":"LK"}])
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

})();
