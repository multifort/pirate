/**
 * Created by Zhang Haijun on 2016/3/21.
 */
	//自定义滚动条
angular.module('app').directive('mCustomScrollbar', [
	function() {
		return {
			link: function(scope, elem, attrs) {
				$(elem).mCustomScrollbar({
					theme:'dark-2',
					scrollButtons:{
						enable:false,
						scrollType:"continuous",
						scrollSpeed:10,
						scrollAmount:100
					},
					mouseWheelPixels: 280,
					horizontalScroll:false
				});
			}
		};
	}
]);