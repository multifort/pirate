(function () {
	"use strict";
	app.directive('ngApplyPieLoopChart', [function () {
		return {
			restrict: 'EA',
			scope:{
				data:'=',
				options:'='
			},
			link: function (scope, element, attrs) {
				var chart = echarts.init($(element)[0]);
				var initEchart = function (legend,series) {
					var option = {
						backgroundColor:'#fff',
						title:{
							show:scope.options.showTitle,
							text:scope.options.theme,
							   x:'right',
							textStyle:{
								fontWeight: 'bold',
								fontSize: 16,
								color:'#333'
							}
						},
						tooltip : {
							trigger: 'item',
							formatter: function(params,ticket,callback){
						        var res = params.data.realValue

						        return params.name+':'+res;
						    }
						},
						legend: {

							x : 'center',
        			y : 'bottom',
							data:legend
					},

						series : [
							{
								avoidLabelOverlap: false,
								label: {

										normal: {
											show: true,
									formatter: '{b} :{d}%'
										},
										emphasis: {
												show: true,
												textStyle: {
														fontSize: '15'
													//	fontWeight: 'bold'
												}
										}
								},
								type:'pie',
								radius : '50%',
            		center: ['50%', '46%'],
								data:series
							}
						]
					};
					chart.setOption(option);
				};
				var legendData = [];
				(function(){
					scope.data.forEach(function (item) {
						item.name = scope.options.dataMap[item.name];
						legendData.push(item.name);
						item.itemStyle = {
							normal:{
								color:scope.options.colorMap[item.name],
								opacity:0.98
							}
						}
					});

					initEchart(legendData,scope.data);
				})();
			}
		}
	}]);
	app.directive('ngApplyPieLineCharts2', ['$timeout', function ($timeout) {
		return {
			restrict: 'EA',
			scope:{
				data:'=',
				options:'='
			},
			link: function (scope, element, attrs) {
				var chart = echarts.init($(element)[0]);
				var initEchart = function (xAxis,legend,series) {
					var option = {
						backgroundColor:'#fff',
						title:{
							text:scope.options.theme,

							textStyle:{
								fontWeight: 'bold',
								fontSize: 16,
								color:'#333'
							}
						},
						tooltip : {
							trigger: 'axis',
							formatter: function(params,ticket,callback){
								var m ='';
								params.forEach(function(item){
									m=item.name+'<br/>';
								})
								params.forEach(function(item){
									if(item.seriesName=='Memory'){
										m +=item.seriesName+' : '+item.value+ " MiB"+'</br>'
									}else if(item.seriesName=='Send'){
                                        m +=item.seriesName+' : '+item.value+ " KiB/s"+'</br>'
                                    }else if(item.seriesName=='Received'){
                                        m +=item.seriesName+' : '+item.value+ " KiB/s"+'</br>'
                                    }else if(item.seriesName=='Network Send'){
										m +=item.seriesName+' : '+item.value+ " KiB/s"+'</br>'
									}else if(item.seriesName=='Network Received'){
											m +=item.seriesName+' : '+item.value+ " KiB/s"+'</br>'
									}else if(item.seriesName=='CPU'){
										m +=item.seriesName+' : '+item.value+ " Millicores"+'</br>'
									}else if(item.seriesName=='内存统计（M）'){
										m +=item.seriesName+' : '+item.value+ " MiB"+'</br>'
									}else if(item.seriesName=='磁盘统计（M）'){
										m +=item.seriesName+' : '+item.value+ " MiB"+'</br>'
									}else if(item.seriesName=='出网kbs'){
										m +=item.seriesName+' : '+item.value+ " KiB/s"+'</br>'
									}else if(item.seriesName=='入网kbs'){
                                        m +=item.seriesName+' : '+item.value+ " KiB/s"+'</br>'
									}else if(item.seriesName=='memUsage'){
                                        m +=item.seriesName+' : '+item.value+ " MiB"+'</br>'
                                    }else if(item.seriesName=='temp'){
                                        m +=item.seriesName+' : '+item.value+ " ℃ "+'</br>'
                                    }else if(item.seriesName=='gpuUsage'){
                                        m +=item.seriesName+' : '+item.value+ ""+'</br>'
                                    }else {
											m +=item.seriesName+' : '+item.value+ " Millicores"+'</br>'
									}
								})


										return m;
								}
						},
						legend: {
							data:legend,
							orient : 'horizontal',
							top:'1%'
						},
						toolbox: {
							show : false,//是否显示工具
							feature : {
								mark : {show: true},
								magicType : {show: true, type: ['line', 'stack', 'tiled','bar']},
								saveAsImage : {show: true}
							}
						},
						calculable : true,
						// dataZoom: [
						// 		{
						// 				show: true,
						// 				realtime: true,
						// 				start: 65,
						// 				end: 85
						// 		},
						// 		{
						// 				type: 'inside',
						// 				realtime: true,
						// 				start: 65,
						// 				end: 85
						// 		}
						// ],
						xAxis : [
							{
								type : 'category',
								boundaryGap: false,
								 minInterval: 5,
								data : xAxis
							}
						],
						yAxis : [
							{
								name: scope.options.unit,
								type : 'value'
							}
						],
						grid: {
							left: '1%',
							right: '30',
							top:'10%',
							bottom: '6%',
							containLabel: true
						},
						series : series
					};
					chart.setOption(option);
				};
				scope.$watch('data',function (newValue,oldValue) {
					var legendData = [],series = [];
					scope.data.values.forEach(function (item) {
							legendData.push(item.name);
							series.push({
								name:item.name,
								type:'line',
								smooth:true,
								itemStyle: {
									normal: {
										// areaStyle: {type: 'default'},
										opacity:0.98
									}},
								data:item.data
							});
						});
				//	var week = ['星期一','星期二','星期三','星期四','星期五','星期六','星期日']
						var week = ['Sun','Mon','Tue','Wed','Thu','Fri','Sat']
					var keysTime = []
						scope.data.keys.forEach(function (item) {
							var a = new Date(item/1);
							var b;
							if(a.getMinutes().toString().length==1){
								b = 	 week[a.getDay()]+' '+a.getHours()+':'+'0'+a.getMinutes();
							}else {
									b = 	 week[a.getDay()]+' '+a.getHours()+':'+a.getMinutes();
							}

							keysTime.push(b)
							});
					initEchart(keysTime,legendData,series);
					setTimeout(function(){
						 	chart.resize();
					},100)

				});
			}}
}]);

app.directive('ngApplyPieLineCharts', ['$timeout', function ($timeout) {
	return {
		restrict: 'EA',
		scope:{
			data:'=',
			options:'='
		},
		link: function (scope, element, attrs) {
			var chart = echarts.init($(element)[0]);
			var initEchart = function (xAxis,legend,series) {
				var option = {
					backgroundColor:'#fff',
					title:{
						text:scope.options.theme,

						textStyle:{
							fontWeight: 'bold',
							fontSize: 16,
							color:'#333'
						}
					},
					tooltip : {
						trigger: 'axis'
					
					},
					legend: {
						data:legend,
						orient : 'horizontal',
						top:'1%'
					},
					toolbox: {
						show : false,//是否显示工具
						feature : {
							mark : {show: true},
							magicType : {show: true, type: ['line', 'stack', 'tiled','bar']},
							saveAsImage : {show: true}
						}
					},
					calculable : true,
					xAxis : [
						{
							type : 'category',
							boundaryGap: false,
							 minInterval: 5,
							data : xAxis
						}
					],
					yAxis : [
						{
							name: scope.options.unit,
							type : 'value'
						}
					],
					grid: {
						left: '1%',
						right: '30',
						top:'10%',
						bottom: '6%',
						containLabel: true
					},
					series : series
				};
				chart.setOption(option);
			};
			scope.$watch('data',function (newValue,oldValue) {
				var legendData = [],series = [];
				if(scope.data){
					scope.data.values.forEach(function (item) {
						legendData.push(item.name);
						series.push({
							name:item.name,
							type:'line',
							smooth:true,
							itemStyle: {
								normal: {
									// areaStyle: {type: 'default'},
									opacity:0.98
								}},
							data:item.data
						});
					});
				}
				
			//	var week = ['星期一','星期二','星期三','星期四','星期五','星期六','星期日']
					var week = ['Sun','Mon','Tue','Wed','Thu','Fri','Sat']
				var keysTime = []
					if(scope.data){
						scope.data.keys.forEach(function (item) {
							var a = new Date(item/1);
							var b;
							if(a.getMinutes().toString().length==1){
								b = 	 week[a.getDay()]+' '+a.getHours()+':'+'0'+a.getMinutes();
							}else {
									b = 	 week[a.getDay()]+' '+a.getHours()+':'+a.getMinutes();
							}

							keysTime.push(b)
							});
						initEchart(keysTime,legendData,series);
					}
					
				setTimeout(function(){
						chart.resize();
				},100)

			});
		}}
}]);

})();
