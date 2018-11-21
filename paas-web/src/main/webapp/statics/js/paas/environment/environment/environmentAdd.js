//新增ctrl
angular.module('app').controller('createEnvironmentModalCtrl', ['$rootScope', '$scope','webSocket', '$state', '$stateParams', 'LANGUAGE', 'httpLoad', '$timeout',
    function ($rootScope, $scope,webSocket, $state, $stateParams, LANGUAGE, httpLoad, $timeout) { //依赖于modalInstance
        $rootScope.link = '/statics/css/image.css';
        $rootScope.moduleTitle = '环境资源 > <span>环境管理</span> > 环境创建';

        $scope.goBack = function () {
            $state.go('paas.environment.environment');
        };
        $scope.addData = {};
        $scope.addData.addNodeList = [];
        $scope.addData.removeNodeList = [];
        $scope.addData.net='10.10.0.0/16';
        $scope.addData.port='8080';
        $scope.nodeType = 1;
        $scope.setNodeType = function (type) {
            $scope.nodeType = type;
            if($scope.nodeType==1){
                $scope.queryHost();
            }
        }
        $scope.queryHost=function () {
            httpLoad.loadData({
                url: '/host/queryNormalHost',
                method: 'POST',
                data:null,
                ignoreError:true,
                success: function (data) {
                    if (data.success) {
                        $scope.addData.addNodeList =  data.data;
                        $scope.addData.removeNodeList = [];
                    }
                }
            });
        }
        $scope.queryHost();
        $scope.addData.dns = true;
        $scope.addData.load = true;
        $scope.addData.monitor = true;
        $scope.addData.log = true;
        $scope.addData.highAvai = false;
        $scope.addData.checkBoxClick = function (n) {
             if( (n=='highAvai')&&($scope.addData.removeNodeList.length<3) ){
                 $scope.addData.highAvai = false;
                 $scope.pop('若实现高可用，已选节点数必须大于等于3','error');
             }else if(n=='dns') {
                 if(!$scope.addData[n]){
                     $scope.addData.load = false;
                     $scope.addData.monitor = false;
                     $scope.addData.log = false;
                     $scope.pop('若不搭建dns，则监控及负载无法搭建，日志无法部署','error');
                 }
             }else if(((n=='load')||(n=='monitor')||(n=='log'))&&(!$scope.addData.dns)){
                 $scope.addData[n]=false;
                 $scope.pop('未选择搭建dns，'+n+'无法搭建或部署','error');
             }
        }

        //添加
        $scope.addNodebtn = function () {
            for (var i = 0; i < $scope.addData.addNodeList.length; i) {
                var item = $scope.addData.addNodeList[i]
                if (item.select) {
                    $scope.addData.removeNodeList.push(item);
                    $scope.addData.addNodeList.splice(i, 1)
                    item.select = false
                }else{
                    i++;
                }
            }
        }
        //删除
        $scope.removeNodebtn = function () {
            for (var i = 0; i < $scope.addData.removeNodeList.length; i) {
                var item = $scope.addData.removeNodeList[i]
                if (item.select) {
                    $scope.addData.addNodeList.push(item);
                    $scope.addData.removeNodeList.splice(i, 1)
                    item.select = false
                }else{
                    i++;
                }
            }
            if(($scope.addData.removeNodeList.length<3)&&($scope.addData.highAvai)){
                $scope.addData.highAvai = false;
                $scope.pop('已选节点数小于3，无法实现高可用','error');
            }
        }
        $scope.ok = function () {
         if($scope.nodeType==1){
             if($scope.addData.removeNodeList.length<1){
                 $scope.pop('请至少选择一个可用节点','error');
                 return;
             }
             var ids=[];
             $scope.addData.removeNodeList.forEach(function (item) {
                 ids.push(item.id);
             })
             var param={
                 ids:ids,
                 networkType:'flanneld',
                 networkSegment:$scope.addData.net,
                 load:$scope.addData.load+'',
                 monitor:$scope.addData.monitor+'',
                 dns:$scope.addData.dns+'',
                 log:$scope.addData.log+'',
                 remark:$scope.addData.remark,
                 envName:$scope.addData.name,
                 highAvai:$scope.addData.highAvai+'',
                 platform:'1',
                 virtualIp:$scope.addData.virtualIp
             };
             httpLoad.loadData({
                 url: '/environment/createKubernetesCluser',
                 method: 'POST',
                 data: param,
                 success: function (data) {
                     if (data.success) {
                         $scope.pop(data.message);
                         $state.go('paas.environment.environment');
                     }
                 }
             });
         }else if($scope.nodeType==2){
             var param1={
                 remark:$scope.addData.remark,
                 envName:$scope.addData.name,
                 platform:'1',
                 ip:$scope.addData.ip,
                 port:$scope.addData.port||'8080'
             };
             httpLoad.loadData({
                 url: '/environment/receiveCluster',
                 method: 'POST',
                 data: param1,
                 success: function (data) {
                     if (data.success) {
                         $scope.pop(data.message);
                         $state.go('paas.environment.environment');
                     }
                 }
             });
         }

        }
    }]);
