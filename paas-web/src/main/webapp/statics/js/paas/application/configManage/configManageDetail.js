(function () {
	"use strict";

	app.controller('configManageDetailCtrl', ['$scope', 'httpLoad', '$rootScope', '$modal', '$state', '$timeout','$location','$stateParams',
		function ($scope, httpLoad, $rootScope, $modal, $state, $timeout,$location,$stateParams) {
            $rootScope.moduleTitle = '应用服务 > 配置管理 > 详情';//定义当前页
            $rootScope.link = '/statics/css/image.css';//引入页面样式
            var id = $stateParams.id;
            var url = '/config/manage/'+id+'/config';
            $scope.goBack = function(){
            	history.go(-1)
            };
            httpLoad.loadData({
                url:url,
                method:'GET',
                data: {},
                noParam:true,
                success:function(data){
                    if(data.success&&data.data){
                        $scope.supplierDetail = data.data.configManage;
                        $('#json-renderer').jsonViewer(data.data.configMap);

                    }
                }
            });
        }
	]);
})();
