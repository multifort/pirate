'use strict';
angular.module('app', [
  'ngAnimate',
  // 'ngAria',
  // 'ngCookies',
  'toaster',
  // 'ngMessages',
  // 'ngResource',
  'ngSanitize',
  // 'ngTouch',
  'ngStorage',
  'http.load',
  // 'Analytic.services',
  'webSocket.services',
  'ui.router',
  'ui.bootstrap',
  'ui.utils',
  'ui.load',
  'ui.jq',
  'oc.lazyLoad',
  // 'ngMaterial',
  'validation',
  'validation.rule',
  'rzModule',
  'uiSwitch',
  'treeControl',
  'g1b.datetime-inputs',
  'kubernetesUI'
])
.config(function(kubernetesContainerSocketProvider) {
              kubernetesContainerSocketProvider.WebSocketFactory = "CustomWebSockets";
          })


          /* Our custom WebSocket factory adapts the url */
          .factory("CustomWebSockets", function($rootScope) {
              return function CustomWebSocket(url, protocols) {
                  url = $rootScope.baseUrl + url;
                  if ($rootScope.accessToken)
                      url += "&access_token=" + $rootScope.accessToken;
                  return new WebSocket(url, protocols);
              };
          });
