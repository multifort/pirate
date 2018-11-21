/**
 * Created by Zhang Haijun on 2016/10/17.
 */
//多选下拉框  mu-select-data="muSelectData" 展示的数据  mu-select-id="muSelectId" 下拉框的值  select-name="tagName" 显示数据的属性名称
app.directive('ngMuselect', ['$document', '$compile', function($document, $compile) {
	return {
		restrict: "EA",
		scope: {
			itemData:"=",
			muSelectData: '=',
			changeMuOption: '&'
		},
		link: function(scope, element, attr) {
			scope.selectMuName = '--请选择--';
			var itemName = attr.selectName;
			var getHtml = function(muSelectData) {
				var selectHtml = ['<div ng-click="showSelect($event)" title="{{selectMuName}}"><span class="input" ng-bind="itemData.selectMuName"></span><span class="fa-down"><i class="fa fa-caret-down"></i></span></div>'];
				selectHtml.push('<div class="select_body" ng-show="ifMuShow"> <ul class="option">');
				selectHtml.push('<li ng-repeat="item in muSelectData"><label class="i-checks"><input type="checkbox" value=""  ng-change="changeMuOption(itemData)" ng-model="item.flag"><i></i>{{item["' + itemName + '"]}}</li>');
				selectHtml.push('</ul></div>');
				return selectHtml.join('');
			}
			//单个选择
			// scope.changeMuOption = function() {
			// 	scope.muSelectId = [];
			// 	scope.selectMuName = '';
			// 	scope.defaultMu = false;
			// 	for (var record in scope.muSelectData) {
			// 		if (scope.muSelectData[record].flag) {
			// 			scope.muSelectId.push(scope.muSelectData[record].id);
			// 			scope.selectMuName += scope.muSelectData[record].name + ',';
			// 		}
			// 	}
			// 	scope.selectMuName = scope.selectMuName.substr(0, scope.selectMuName.length - 1) || '--请选择--';
			// }
			scope.showSelect = function(e) {
				e.stopPropagation();
				if (scope.ifMuShow) scope.ifMuShow = false;
				else scope.ifMuShow = true;
			};
			$(document).on('click', function(e) {
				scope.$apply(function() {
					scope.ifMuShow = false;
				})
			});
			$(element).on('click', function(e) {
				e.stopPropagation();
			});
			$(element).append($compile(getHtml(scope.muSelectData))(scope));
			//初始化
			scope.changeMuOption();
		}
	}
}])
