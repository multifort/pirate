/**
 * Created by Zhang Haijun on 2016/8/31.
 */
(function () {
	'use strict';
	app.constant('TASKMESSAGE', {});
	angular.module('webSocket.services', [])
		.service('webSocket', ['$rootScope', function ($rootScope) {
			return {
				socket:'',
				init: function(options){
					var that = this;
					if ('WebSocket' in window) {
						var url = 'ws://' + location.host + '/messageService';
						if(!this.socket) this.socket = new WebSocket(url);//确保只启用一个websocket
						this.socket.onopen = function (data) {
							console.log('websocket connect');
							that.onmessage(options);
						};
						this.socket.onclose = function (data) {
							console.log('websocket close');
						}
					}else{
						console.log('Websocket not supported');
					}
				},
				onmessage: function(options) {
					if(this.socket){//解决刷新页面时websocket未启动报错，然后(else)启动websocket
						this.socket.onmessage = function (event) {
							var data = JSON.parse(JSON.parse(event.data).content);
							$rootScope.$apply(function () {
								$rootScope.pop(data.message,data.success?'success':'error');
							});
							if(!options) return;//容错处理
							var message = options.message;
							console.log(data);
							if (message && typeof message == 'function') {
								message(data);
							}
						};
					}else{
						this.init(options);
					}
				}
			};
		}]);
})();
