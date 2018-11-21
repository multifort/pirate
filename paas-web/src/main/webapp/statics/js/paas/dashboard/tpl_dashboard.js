/**
 * Created by Zhang Haijun on 2016/12/5.
 */
app.directive('ngGraphDashboradLineChart', [ function () {
	return {
		restrict: 'EA',
		scope:{
			data:'='
		},
		link: function (scope, element, attrs) {
			var chart = echarts.init($(element)[0]);
			var initEchart = function (xAxis,legend,series) {
				var option = {
					tooltip : {
						trigger: 'axis'
					},
					legend: {
						data:legend
					},
					toolbox: {
						show : true,
						feature : {
							mark : {show: true},
							magicType : {show: true, type: ['line', 'bar', 'stack', 'tiled']},
							saveAsImage : {show: true}
						}
					},
					calculable : true,
					xAxis : [
						{
							type : 'category',
							boundaryGap : false,
							data : xAxis
						}
					],
					yAxis : [
						{
							type : 'value'
						}
					],
					grid: {
						left: '1%',
						right: '20',
						top:'10%',
						bottom: '6%',
						containLabel: true
					},
					series : series
				};
				chart.setOption(option);
			};
			var colorMap = {
				'执行中':'#00aeef',
				'成功':'#27c24c',
				'失败':'#f05050',
				'取消':'#fad733'
			}
			var legendData = [],series = [];
			//数据处理
			(function () {
				scope.data.values.forEach(function (item) {
					legendData.push(item.name);
					series.push({
						name:item.name,
						type:'line',
						smooth:true,
						itemStyle: {
							normal: {
								areaStyle: {type: 'default'},
								color:colorMap[item.name],
								opacity:0.8
							}},
						data:item.data
					});
				});
				initEchart(scope.data.keys,legendData,series);
			})();
		}}
}]);
app.directive('ngGraphDashboradPieChart', [function () {
	return {
		restrict: 'EA',
		scope:{
			theme:'=',
			data:'='
		},
		link: function (scope, element, attrs) {
			var chart = echarts.init($(element)[0]);
			var initEchart = function (legend,series) {
				var option = {
					tooltip : {
						trigger: 'item',
						formatter: "{a} <br/>{b} : {c} ({d}%)"
					},
					legend: {
						orient : 'vertical',
						x : 'left',
						data:legend
					},
					toolbox: {
						show : true,
						feature : {
							saveAsImage : {show: true}
						}
					},
					calculable : true,
					series : [
						{
							name:scope.theme,
							type:'pie',
							radius : '55%',
							center: ['50%', '60%'],
							data:series
						}
					]
				};
				chart.setOption(option);
			};
			var legendData = [];
			(function(){
				scope.data.forEach(function (item) {
					legendData.push(item.name);
				});
				initEchart(legendData,scope.data);
			})();
		}
	}
}]);
app.directive('ngGraphDashboradPieChart10', [function () {
	return {
		restrict: 'EA',
		scope:{
			theme:'=',
			data:'='
		},
		link: function (scope, element, attrs) {
			var chart = echarts.init($(element)[0]);
			var initEchart = function (legend,series) {
				var option = {
					//title: {
					//	text: scope.theme,
					//	textStyle:{
					//		fontWeight: 'bold',
					//		fontSize: 16,
					//		color:'#333'
					//	}
					//},
					tooltip : {
						trigger: 'item',
						formatter: "{b} : {c}台 ({d}%)"
					},
					legend: {
						orient : 'horizontal',
						x : 'center',
						top:'88%',
						left:'center',
						data:legend
					},
					toolbox: {
						show : true,
						//feature : {
						//	saveAsImage : {show: true}
						//}
					},
					calculable : true,
					series : [
						{
							name:scope.theme,
							type:'pie',
							radius : '55%',
							center: ['50%', '43%'],
							data:series
						}
					]
				};
				chart.setOption(option);
			};
			var legendData = [];
			(function(){
				var colorMap = {
					'开机数':'#6bcadc',
					'关机数':'#6fa7e7',
					'异常数':'#fc7e7e'
				};
				scope.data.forEach(function (item) {
					legendData.push(item.name);
					item.itemStyle = {
						normal:{
							label:{
								show: true,
								textStyle:{
									fontSize:14
								},
								formatter: '{b} : {c}台 ({d}%)'
							},
							labelLine :{show:true},
							color:colorMap[item.name],
							opacity:0.98
						}
					}
				});
				initEchart(legendData,scope.data);
			})();
		}
	}
}]);
app.directive('ngGraphDashboradBarChart', [ function () {
	return {
		restrict: 'EA',
		scope:{
			theme:'=',
			data:'='
		},
		link: function (scope, element, attrs) {
			var chart = echarts.init($(element)[0]);
			var initEchart = function (xAxis,legend,series) {
				var option = {
					tooltip : {
						trigger: 'axis'
					},
					legend: {
						data:legend
					},
					toolbox: {
						show : true,
						right:20,
						feature : {
							mark : {show: true},
							saveAsImage : {show: true}
						}
					},
					xAxis : [
						{
							type : 'category',
							data : xAxis
						}
					],
					yAxis : [
						{
							type : 'value'
						}
					],
					grid: {
						left: '0%',
						right: '1%',
						top:'10%',
						bottom: '6%',
						containLabel: true
					},
					series : series
				};
				chart.setOption(option);
			}
			var legendData = [];
			//数据处理
			(function () {
				var series = {
					name:scope.theme,
					type:'bar',
					data:scope.data.values
				};
				legendData = [scope.theme];
				initEchart(scope.data.keys,legendData,series);
			})();
		}}
}]);