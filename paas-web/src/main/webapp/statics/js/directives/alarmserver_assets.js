/**
 * Created by Zhang Haijun on 2016/12/27.
 */
app.directive('ngAlarmServerAssets', ['httpLoad','$stateParams', '$state', function (httpLoad,$stateParams,$state) {
	return {
		restrict: 'EA',
		templateUrl:'/statics/tpl/environment/alarmServer.html',
		scope:{
			type:'='
		},
		link: function (scope, element, attrs) {
			scope.itemsByPage = 5;
			var url;
			(function () {
				switch(scope.type){
					case 'dc':
						url = '/dc/alarms'
						break;
					case 'room':
						url = '/room/alarms';
						break;
					case 'rack':
						url = '/rack/alarms';
						break;
				};
				httpLoad.loadData({
					url: url,
					method: 'GET',
					data: {id: $stateParams.id},
					success: function(data){
						if(data.success){
							scope.serverListData = data.data;
							scope.total = scope.serverListData.length;
						}
					}
				});
			})();
			scope.goDetail = function(id){
				$state.go('app.monitor.managedetail', {id: id});
			}
		}
	}
}]);