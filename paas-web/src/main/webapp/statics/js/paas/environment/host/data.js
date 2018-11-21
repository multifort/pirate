app.controller('resourceTabCtrl', ['$scope', 'httpLoad', '$rootScope','$modal','$state','$timeout',
    function($scope, httpLoad, $rootScope, $modal,$state, $timeout) {
        $rootScope.moduleTitle = '资源管理 > 主机资源';//定义当前页
        $rootScope.link = '/statics/css/tpl-card.css';
        sessionStorage.removeItem('networktabLocation');sessionStorage.removeItem('datastoretabLocation');

        sessionStorage.setItem('resourcetabLocation1', JSON.stringify(''));
        var resourcetabLocation = sessionStorage.getItem('resourcetabLocation');
        $scope.active1 = $scope.active2 = $scope.active3 = $scope.active4 =false;
          $scope.active3 = true;
    }
]);
app.controller('resourceDataCtrl', ['$rootScope', '$scope', 'httpLoad','$filter',function($rootScope, $scope, httpLoad,$filter) {
    $rootScope.moduleTitle = '资源管理 > 主机资源';
    $rootScope.link = '/statics/css/graph.css';
    $scope.getData = function(){
        httpLoad.loadData({
            url: '/statistic/computer',
            method: 'GET',
            noParam:true,
            success: function(data){
                if(data.success) {
                    $scope.dashboardData = data.data;
                    $scope.host = [];$scope.server = [];
                    $scope.dashboardData.host.map(function(value){
                        if(value.name!='instances'){
                            value.name = $filter('resource')(value.name,'count');$scope.host.push(value);
                        }else $scope.hostNum = value.value;
                    });
                    $scope.dashboardData.server.map(function(value){
                        if(value.name!='instances'){
                            value.name = $filter('resource')(value.name,'count');$scope.server.push(value);
                        }else $scope.serverNum = value.value;
                    });
                    if($scope.hostNum!=0) $scope.isDataLoad1 = true;
                    else $scope.isDataLoad1 = false;
                    if($scope.serverNum!=0) $scope.isDataLoad2 = true;
                    else $scope.isDataLoad2 = false;
                }
            }
        });
    };
    var resourcetabLocation = sessionStorage.getItem('resourcetabLocation');
    if(!resourcetabLocation) $scope.getData();
}]);
