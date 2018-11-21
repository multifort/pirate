app.controller('ServiceCatalogListCtrl', ['$rootScope', '$scope', 'httpLoad', '$modal', '$timeout','webSocket', '$state', function($rootScope, $scope, httpLoad, $modal, $timeout, webSocket, $state) {
	$rootScope.moduleTitle = '资产目录 > 服务目录';
	$scope.listData = [
		{
			name: '开发工具',
			data:[{
				name:'GitLab',
				icon:'icon-gitlab'
			},{
				name:'sonarQube',
				icon:'icon-sonar'
			},{
				name:'JIRA',
				icon:'icon-JIRA'
			},{
				name:'禅道系统',
				icon:'icon-chandaoxitong'
			},{
				name:'Jenkins',
				icon:'icon-jenkins'
			},{
				name:'Maven',
				icon:'icon-Neuxs-maven'
			},{
				name:'Harbor',
				icon:'icon-harbor'
			},{
				name:'容器平台',
				icon:'icon-rongqi'
			},{
				name:'SVN',
				icon:'icon-svn'
			},{
				name:'OA系统',
				icon:'icon-OAxitong'
			}]
		},
	/*	{
			name: '应用服务',
			data:[{
				name:'镜像上传',
				url:'paas.catafunction.imageupload',
				icon:'icon-exalt'
			},{
				name:'镜像同步',
				url:'paas.catafunction.imageSync',
				icon:'icon-tongbusynchronize4'
			},{
				name:'公有库同步',
				url:'paas.catafunction.publicSync',
				icon:'icon-synch'
			},{
				name:'应用部署',
				url:'paas.application.instance',
				icon:'icon-bushu'
			},{
				name:'应用编排',
				url:'paas.application.template',
				icon:'icon-bianpai'
			},{
				name:'安全扫描',
				url:'paas.catafunction.securityScan',
				icon:'icon-anquan'
			}]
		},*/
		{
			name: 'DevOps',
			data:[{
				name:'流程定义',
				url:'paas.process.newlayout',
				icon:'icon-liucheng'
			}]
		},
		{
			name: '应用运维',
			data:[{
				name:'日志中心',
				url:'paas.system.log',
				icon:'icon-rizhizhongxin'
			},{
				name:'监控告警',
				url:'paas.system.monitor',
				icon:'icon-jiankonggaojing'
			}]
		}];
	$scope.goTo=function (item,name) {
		if(name=='开发工具'){
			var modalInstance = $modal.open({
				templateUrl : '/statics/tpl/service_catalog/selectUrl.html',
				controller : 'selectUrlModalCtrl',// 初始化模态范围
				backdrop: 'static',
				resolve : {
					item : function(){
						return item;
					}
				}
			});
			modalInstance.result.then(function(){
				$scope.getData(1);
			},function(){});
		}else {
			$state.go(item.url);
		}
	}
}]);
//选择地址ctrl
app.controller('selectUrlModalCtrl',['$scope','$modalInstance','httpLoad','LANGUAGE','item',
	function($scope,$modalInstance,httpLoad,LANGUAGE,item){ //依赖于modalInstance
		httpLoad.loadData({
			url:'/dict/list',
			method:'GET',
			data: {simple:true, params:angular.toJson([{"param":{"pvalue":"开发工具","softwareType":item.name},"sign":"EQ"}])},
			noParam:true,
			success:function(data){
				if(data.success){
					$scope.urlList=data.data.rows;
				}
			}
		});
		$scope.ok = function(){
            $modalInstance.close();
			window.open($scope.url,'_blank');
		};
		$scope.cancel = function(){
			$modalInstance.dismiss('cancel');
		}
	}]);
