/**
 * Created by Zhang Haijun on 2017/3/10.
 * 点击其他地方隐藏详情
 */
app.directive('ngHideDetail', [function () {
	return {
		restrict: 'EA',
		scope: true,
		link: function (scope, element, attrs) {
			$(document).on('click', function () {
				scope.$apply(function () {
					scope.goBack();
				});
			});
			$(element).on('click', function ($event) {
				$event.stopPropagation();
			});
		}
	}
}]);