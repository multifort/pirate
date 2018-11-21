(function() {
    "use strict";
    //用户修改密码、退出登录验证
    angular.module('app').controller('updatePasswordCtrl', ['$scope','$state','$rootScope','$stateParams','$modal','httpLoad','$window',function($scope,$state,$rootScope,$stateParams,$modal,httpLoad,$window) {

        $scope.updatePassword = function($event){
            var modalInstance = $modal.open({
                templateUrl : '/statics/tpl/access/updatePassword.html',
                controller : 'updateModalCtrl',
                size : 'sm'
            });
        };
        $scope.getSpecification = function($event){
            var modalInstance = $modal.open({
                templateUrl : '/statics/tpl/dashboard/specification.html',
                controller : 'openModalCtrl',
                size : 'lg'
            });
        };

        $scope.loginout = function($event){
            $event.stopPropagation();
            var modalInstance = $modal.open({
                templateUrl: '/statics/tpl/template/delModal.html',
                controller: 'delModalCtrl',
                backdrop: 'static',
                resolve:{
                    tip: function () {
                        return '你确定要退出登录吗？';
                    },
                    btnList:function(){
                        return  [{name:'确定',type:'btn-info'},{name:'取消',type:'btn-cancel'}];
                    }
                }
            });
            modalInstance.result.then(function() {
                httpLoad.loadData({
                    url: '/logout',
                    method: 'POST',
                    success: function(data){
                        if(data.success){
                            if(localStorage.getItem('entry')=='sso') $window.open('http://uatportal.jsbchina.cn','_self');
                            else $state.go('access.login');
                        }
                    }
                });
            });
        };
		   $scope.usermessage = function($event){
          $state.go('paas.personal.info');

        };
        $scope.about = function($event){
            $state.go('paas.about');

        };
    }]);
    //用户修改密码--新增ctrl
    angular.module('app').controller('updateModalCtrl',['$scope','$rootScope','$modalInstance','$location','httpLoad',
        function($scope,$rootScope,$modalInstance,$location,httpLoad){ //依赖于modalInstance
            $scope.isSame = false;
            $scope.isChange = false;
            $scope.updateData={};
            $scope.getCurrFocus = function($event){
                if($scope.updateData.newPassword == $scope.updateData.oldPassword){
                    $scope.isChange = true;
                }else{
                    $scope.isChange = false;
                }
                if($scope.updateData.newPassword == $scope.updateData.confirmPassword){
                    $scope.isSame = false;
                }
            };
            $scope.getFocus = function($event){
                if($scope.updateData.newPassword !== $scope.updateData.confirmPassword){
                    $scope.isSame = true;
                }else{
                    $scope.isSame = false;
                }
            };

            $scope.ok = function(){
                $scope.getCurrFocus();
                $scope.getFocus();
                if( (!$scope.isSame)&&(!$scope.isChange)){
                    var updateData = {id:$rootScope.userData.id,password:$scope.updateData.newPassword,oldPassword:$scope.updateData.oldPassword};
                    httpLoad.loadData({
                        url:'/user/change',
                        method:'POST',
                        data:updateData,
                        success:function(data){
                            if(data.success){
                                //console.log($scope.updateData);
                                //var id = data.data["id"];
                                $scope.pop('密码修改成功');
                                $modalInstance.close();
                                $location.path('/access/login');
                            }
                        }
                    });
                }
            };
            $scope.cancel = function(){
                $modalInstance.dismiss('cancel'); // ????
            };
    }]);
    //说明书
    angular.module('app').controller('openModalCtrl',['$scope','$rootScope','$modalInstance','$location','$state',
        function($scope,$rootScope,$modalInstance,$location,$state){ //依赖于modalInstance

            $scope.goOpen = function($event){
                $modalInstance.dismiss('cancel');
                $state.go('paas.environment.environmentAdd');
            };

            $scope.cancel = function(){
                $modalInstance.dismiss('cancel'); // ????
            };
        }]);
    //关于
    angular.module('app').controller('aboutModalCtrl',['$rootScope','$scope','$state','httpLoad',

        function($rootScope,$scope,$state,httpLoad){ //依赖于modalInstance
            $rootScope.moduleTitle = '关于';//定义当前页

            $scope.getHistory = function (page) {
                var data = [{"name":"<1>数据纵览页展示平台管理的各种资源使用状况；<br>"+
                "<2>环境资源中主要包括环境创建与管理以及资源展示和存储相关操作功能，实现各功能界面化操作；<br>"+
                "<3>镜像仓库针模块对仓库与镜像进行管理，对仓库与镜像进行批量管理；<br>"+
                "<4>应用管理模块主要对应用的创建，服务模板的管理，服务创建，以及对发布的服务的管理和容器生命周期的维护；<br>"+
                "<5>流程管控模块主要对CI流程的管理操作；<br>",
                    "value":"<1>对环境资源中的环境创建功能进行细化操作，完善集群创建功能<br>"+
                "<2>进一步完善商店组件模板的上传功能<br>"+
                "<3>进一步完善对应用的删除操作，对服务管理<br>"+
                "<4>进一步完善对流程管控的流程操作<br>"}]
                $scope.userList = data;
            }
            $scope.getHistory(1)
        }]);

})();