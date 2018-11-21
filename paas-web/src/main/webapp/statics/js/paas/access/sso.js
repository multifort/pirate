app.controller('ssoCtrl', ['$scope', '$rootScope', 'httpLoad', '$state','$location', function($scope, $rootScope, httpLoad, $state,$location) {
  $rootScope.link = '/statics/css/login.css';

  //登陆接口
  $scope.sso = function() {
	var absurl =$location.search()['query'];
    httpLoad.loadData({
      url : '/sso',
      method : 'GET',
      noParam: true,
      data : {
        query : $location.search()['query']
      },
      success : function(data) {
        if (data.success) {
          var userData = data.data.user;
          $.extend(true,userData,userData.username);
          localStorage.setItem('entry', 'sso');
          localStorage.setItem('bycUserData', JSON.stringify(userData));
          localStorage.setItem('menuData', JSON.stringify(data.data.auths));
          $rootScope.userData = userData;
          $state.go('paas.dashboard.dashboard');
        }else{
          $scope.refreshCode();
        };
      }
    });
  };
  $scope.sso();
}]);
app.directive('ngAutoPos', ['httpLoad','$rootScope', function (httpLoad, $rootScope) {
  return {
    restrict: 'EA',
    scope:false,
    link: function (scope, element, attrs) {
      var setPos = function(){
        var top = ($(window).height() - 669)/2;
        $(element).css({"margin-top":top+"px"});
      };
      setPos();
      window.onresize = function(){
        setPos();
      }
    }
  }
}]);
