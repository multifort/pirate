(function() {
  angular.module('Analytic.services', []).
  service('GoogleTagManager', ['$window', function($window) {
    this.push = function(data) {
      try {
        $window.dataLayer.push(data);
      } catch (e) {}
    };
  }]);
})();
