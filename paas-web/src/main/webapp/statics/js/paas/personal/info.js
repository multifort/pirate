(function(){
    "use strict";
    app.controller('infoCtrl', ['$scope', 'httpLoad', '$rootScope','$modal','$state','$timeout',
        function($scope, httpLoad, $rootScope, $modal,$state, $timeout) {
            $rootScope.moduleTitle = '个人中心 > 个人信息';//定义当前页
            $rootScope.link = '/statics/css/user.css';//引入页面样式
            $scope.isEditInfo = 1;$scope.isEditMessage = 1;
            $scope.modeData = [{"value":"SMS","name":"短信","isRegionActive":false},{"value":"EMAIL","name":"邮件","isRegionActive":false},{"value":"MESSAGE","name":"站内信","isRegionActive":false}];

            //获取用户信息
            $scope.getUserData = function(){
                var id = $rootScope.userData.id;
                httpLoad.loadData({
                    url:'/user/detail',
                    method:'GET',
                    data: {id:id},
                    success:function(data){
                        if(data.success&&data.data){
                            $scope.userDetail = data.data;
                        }
                    }
                });
            };
            $scope.getUserData();

            $scope.edit = function(index){
                $scope.isEditInfo = 2;
                var aa=JSON.stringify($scope.userDetail);
                $scope.userDetail2=JSON.parse(aa);
            };
            $scope.cancel = function(){
                $scope.isEditInfo = 1;
            };
            $scope.chooseRegion = function($index){
                $scope.modeData[$index].isRegionActive = !$scope.modeData[$index].isRegionActive;
            };
            $scope.ok = function(){
                httpLoad.loadData({
                    url:'/user/modify',
                    method:'POST',
                    data: $scope.userDetail2,
                    success:function(data){
                        if(data.success){
                            $scope.pop('基本信息修改成功');
                            $scope.getUserData();
                            $scope.isEditInfo = 1;
                        }
                    }
                });
            };
        }
    ]);
})();