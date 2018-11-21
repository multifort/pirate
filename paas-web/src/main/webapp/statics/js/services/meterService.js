/**
 * Created by Li Wending on 2016/11/17.
 */
(function() {
	"use strict";
	app.service("meterService", function() {
		// 生成参数对
		this.construcPara = function(name, value) {
			var para = {};
			var paramObj = {};
			para[name] = value;
			paramObj.param = para;
			paramObj.sign = 'EQ';
			return paramObj;
		}
		// 饼图函数
		this.eCharts = function(dataArr, title, legend, divid) {
			var myChart = echarts.init(document.getElementById(divid));
			var option = {
				title : {
					text : title,
					x : 'center'
				},
				tooltip : {
					trigger : 'item',
					formatter : "{a} <br/>{b} : {c} ({d}%)"
				},
				legend : {
					top : '50px',
					data : legend
				},
				series : [ {
					type : 'pie',
					radius : '30%',
					center : [ '50%', '60%' ],
					data : dataArr,
					itemStyle : {
						emphasis : {
							shadowBlur : 10,
							shadowOffsetX : 0,
							shadowColor : 'rgba(0, 0, 0, 0.5)'
						}
					}
				} ]
			};
			myChart.setOption(option);
		};

		// 柱型图函数
		var eChartBar = function(legends, xAxis, series, divid) {
			var myChart = echarts.init(document.getElementById(divid));
			var option = {
				tooltip : {
					trigger : 'axis',
					axisPointer : { // 坐标轴指示器，坐标轴触发有效
						type : 'shadow' // 默认为直线，可选为：'line' | 'shadow'
					}
				},
				legend : {
					data : legends
				},
				grid : {
					left : '3%',
					right : '4%',
					bottom : '3%',
					containLabel : true
				},
				xAxis : [ {
					type : 'category',
					data : xAxis
				} ],
				yAxis : [ {
					type : 'value'
				} ],
				series : series
			};
			myChart.setOption(option);
		}
		var monthLastDay = function(month){
			var monthTem = month.substring(5);
//			var monthTem = month.split('/')[1];
			if(monthTem==="01"||monthTem==="03"||monthTem==="05"||monthTem==="07"||monthTem==="08"||monthTem==="10"||monthTem==="12"){
				return '/31';
			}
			if(monthTem==="04"||monthTem==="06"||monthTem==="09"||monthTem==="11"){
				return '/30';
			}
			if(monthTem==="02"){
				return '/28';
			}
		}
		
		//时间参数
		this.addTimepara = function(params,beginDate,endDate){
      //'2016/11
		      if(beginDate!==''){
		        params.push(this.construcPara('beginDate', beginDate+'/01'));
		      }
		      if(endDate!==''){
		        params.push(this.construcPara('endDate', endDate+monthLastDay(endDate)));
		      }
		    };
		    
		// 图表数据
		this.constructChartData = function(rows, name) {
			var dimensions = [ 'instanceNum', 'fee', 'cpuNum', 'memoryDrp',
					'diskDrp' ];
			var userDataFlag = false;
			if(rows.length===0){
				userDataFlag = false;
			}else{
				userDataFlag = !!!rows[0].name;
			}
			var legends = this.filterData(rows, 'name', userDataFlag);
			var xAxis = this.filterData(rows, 'meterDate', userDataFlag);
			var formatXAxis = [];
			$.each(xAxis, function(index, value) {
				formatXAxis.push(value.substring(0, 7));
			});
			var dimensionSeries;
			$.each(dimensions, function(index, dimension) {
				dimensionSeries = constructSeriesData(rows, legends, dimension,
						userDataFlag);
				eChartBar(legends, formatXAxis, dimensionSeries, name + index);
			});
		}
		
		// 提取单个数据
		this.filterData = function(rows, key, userDataFlag) {
			var results = [];
			var value;
			$.each(rows, function(index, rowData) {
				if ('name' === key && userDataFlag) {
					value = rowData.instanceName;
				} else {
					value = rowData[key];
				}
				if (-1 == results.indexOf(value)) {
					results.push(value);
				}
			});
			return results;
		};

		// 构建series数据
		var constructSeriesData = function(rows, legends, key, userDataFlag) {
			var results = [];
			var serieArr = [];
			var serieObj;
			$.each(legends, function(index, legendName) {
				serieArr = [];
				serieObj = {
					name : legendName,
					type : 'bar'
				};
				$.each(rows, function(index, rowData) {
					if (userDataFlag && legendName === rowData.instanceName) {
						serieArr.push(rowData[key]);
					} else if (legendName === rowData.name) {
						serieArr.push(rowData[key]);
					}
				});
				serieObj.data = serieArr;
				results.push(serieObj);
			});
			return results;
		}
		this.exportExcel = function(url, para) {
			document.getElementById("testExport").setAttribute("src",
					url + "?params=" + para);
		};
	});
})();