'use strict';

/* Controllers */

angular.module('app')
.controller('AppCtrl', ['$scope', '$localStorage', '$window','$rootScope',
  function(              $scope,   $localStorage,   $window,$rootScope) {
    // add 'ie' classes to html
      $rootScope.maskShow = false;
      var isIE = !!navigator.userAgent.match(/MSIE/i);
      isIE && angular.element($window.document.body).addClass('ie');
      isSmartDevice($window) && angular.element($window.document.body).addClass('smart');
      $scope.app = {
        name: 'PaaS',
        version: '1.0.0',
        // for chart colors
        color: {
          primary: '#7266ba',
          info: '#23b7e5',
          success: '#27c24c',
          warning: '#fad733',
          danger: '#f05050',
          light: '#e8eff0',
          dark: '#3a3f51',
          black: '#1c2b36'
        },
        settings: {
          themeID: 6,
          navbarHeaderColor: 'bg-boyun',
          navbarCollapseColor: 'bg-white-only',
          asideColor: 'bg-blove',
          headerFixed: true,
          asideFixed: true,
          asideFolded: false,
          asideDock: false,
          container: false
        }
      };


// 主题切换代码
      // save settings to local storage
      // if (angular.isDefined($localStorage.settings)) {
      //   $scope.app.settings = $localStorage.settings;
      // } else {
      //   $localStorage.settings = $scope.app.settings;
      // }
      // $scope.$watch('app.settings', function() {
      //   if ($scope.app.settings.asideDock && $scope.app.settings.asideFixed) {
      //     // aside dock and fixed must set the header fixed.
      //     $scope.app.settings.headerFixed = true;
      //   }
      //   // for box layout, add background image
      //   $scope.app.settings.container ? angular.element('html').addClass('bg') : angular.element('html').removeClass('bg');
      //   // save to local storage
      //   $localStorage.settings = $scope.app.settings;
      // }, true);

      function isSmartDevice($window) {
        // Adapted from http://www.detectmobilebrowsers.com
        var ua = $window['navigator']['userAgent'] || $window['navigator']['vendor'] || $window['opera'];
        // Checks for iOs, Android, Blackberry, Opera Mini, and Windows mobile devices
        return (/iPhone|iPod|iPad|Silk|Android|BlackBerry|Opera Mini|IEMobile/).test(ua);
      }
    }
  ])
  //生成左侧菜单 获取前端权限列表 启动消息websocket以及单点登录验证
  .controller('MenuCtrl', ['$scope', '$rootScope', '$modal', '$state', 'IGNOREPERMISSION', 'webSocket', 'httpLoad', '$interval', function($scope, $rootScope, $modal, $state, IGNOREPERMISSION, webSocket, httpLoad, $interval) {
    $scope.menuData = JSON.parse(localStorage.getItem('menuData'));
    if(!$scope.menuData) $state.go('access.login');
    //前端权限控制
    var permission = IGNOREPERMISSION.IGNORELIST;
    var getUrl = function(data){
      for(var a in data){
        var item = data[a];
        permission += item.actionUrl + '#';
        if(item.children) getUrl(item.children);
      }
    }
    getUrl($scope.menuData);
    IGNOREPERMISSION.IGNORELIST　=　permission.replace(/\./g,'');
    //启动webSocket
    webSocket.init();
    //单点登录验证
   var interval =  $interval(function(){
      httpLoad.loadData({
        url: '/user/checkUser',
        data:{
          id:$rootScope.userData.id
        },
        loadinghide:true,
        ignoreError:true,
        success:function (data) {
          if(!data.success){
            $interval.cancel(interval);
            var modalInstance = $modal.open({
              templateUrl: '/statics/tpl/template/delModal.html',
              controller: 'delModalCtrl',
              backdrop: 'static',
              resolve:{
                tip: function () {
                  return '用户已在其他地方登录！';
                },
                btnList:function(){
                  return  [{name:'关闭',type:'btn-default'}];
                }
              }
            });
            modalInstance.result.then(function() {
              $state.go('access.login')
            });
          }
        },
        error:function(){
          $interval.cancel(interval);
        }
      })
    },60000)
    }
  ])
  //将删除提示框提出统一的方法
  .controller('delModalCtrl', ['$scope','$modalInstance','tip','btnList',
  function($scope, $modalInstance,tip,btnList) {
    $scope.btnList = btnList;
    $scope.tip = tip;
    $scope.ok = function(){
      $modalInstance.close();
    }
    $scope.cancle = function(){
      $modalInstance.dismiss('cancel');
    };
  }
])
  .run([  '$rootScope', '$window', '$location', '$state', 'toaster', 'httpLoad',
  function($rootScope,   $window,   $location,   $state,   toaster,   httpLoad) {
    //消息提示框设为默认方法
    $rootScope.pop = function(text, type, title) {
      var type = type || 'success';
      var title = title || '';
      toaster.pop(type, title, text);
    };

    //保存用户信息到全局变量
    var userData = function() {
      var userDataObj = JSON.parse(localStorage.getItem('bycUserData'));
      if (userDataObj === undefined || userDataObj === null) {
        return {};
      }
      return userDataObj;
    };
    $rootScope.userData = userData();
  }])
