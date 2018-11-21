<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en" data-ng-app="app">
<head>
	<base href="/">
	<meta charset="utf-8">
	<title>Pirates</title>
	<meta name="description" content="">
	<meta name="keywords" content="">
	<meta name="viewport" content="width=device-width,initial-scale=1,maximum-scale=1">
	<link rel="shortcut icon" href="/statics/img/pirates.png" type="image/x-icon" />
	<link rel="stylesheet" type="text/css" ng-href="/statics/build/css/common.min.css">
	<link rel="stylesheet" type="text/css" ng-href="/statics/css/app.css">
	<link rel="stylesheet" type="text/css" ng-href="/statics/css/tree-control.css">
	<link rel="stylesheet" type="text/css" ng-href="/statics/libs/angular/angular-datetime-inputs/datetime-inputs.min.css">
	<link rel="stylesheet" type="text/css" ng-href="{{link}}">
	<link rel="stylesheet" type="text/css" ng-href="/statics/fonts/iconfont/iconfont.css" >
	<link rel="stylesheet" type="text/css" ng-href="/statics/fonts/appiconfont/iconfont.css" >
</head>
<body ng-controller="AppCtrl">
<div class="app" id="app"
     ng-class="{'app-header-fixed':app.settings.headerFixed, 'app-aside-fixed':app.settings.asideFixed, 'app-aside-folded':app.settings.asideFolded, 'app-aside-dock':app.settings.asideDock, 'container':app.settings.container}"
     ui-view></div>
		 <div id="mask" class="mask" ng-if="maskShow">
				 <div id="spinner-message" class="spinner-message">
					 <font>页面加载中，请稍等······</font>
				 </div>
				 <i id="spinner" class="spinner ace-icon fa fa-spinner fa-spin white"></i>
		</div>
<toaster-container
		toaster-options="{'position-class': 'toast-top-right', 'close-button':true, 'max-opened':2}"></toaster-container>
	<script src="/statics/build/js/libs.min.js"></script>
	<script src="/statics/libs/angular/angular-validation/angular-validation-rule.js"></script>
	<script src="/statics/libs/angular/terminal/xterm.js"></script>
	<script src="/statics/libs/angular/terminal/container-terminal.js"></script>
	<script src="/statics/js/app.js"></script>
	<script src="/statics/js/config.js"></script>
	<script src="/statics/js/config.lazyload.js"></script>
	<script src="/statics/js/config.router.js"></script>
	<script src="/statics/js/main.js"></script>
	<script src="/statics/js/cn.js"></script>
	<script src="/statics/js/address.map.js"></script>
	<script src="/statics/js/services/http-load.js"></script>
	<script src="/statics/js/services/webSocket.js"></script>
	<script src="/statics/js/controllers/updatePassword.js"></script>
	<script src="/statics/libs/angular/angular-tree-control/angular-tree-control.js"></script>
	<script src="/statics/libs/angular/angular-datetime-inputs/moment.min.js"></script>
	<script src="/statics/libs/angular/angular-datetime-inputs/datetime-inputs.min.js"></script>
	<script src="/statics/build/js/base.min.js"></script>

</body>
</html>
