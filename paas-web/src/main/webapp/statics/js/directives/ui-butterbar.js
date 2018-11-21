angular.module('app')
  .directive('uiButterbar', ['$rootScope', '$anchorScroll', function($rootScope, $anchorScroll) {
     return {
      restrict: 'AC',
      template:'<span class="bar"></span>',
      link: function(scope, el, attrs) {       
        el.addClass('butterbar');
        $rootScope.isLoadingActive = false;
      }
     };
  }]);