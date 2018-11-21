//echarts基本参数
angular.module('app').factory('$echartsLineConfig', function () {
    return {
        title: {
            text: ''
        },
        tooltip: {
            trigger: 'axis',
            position: ['50%', '2%'],
            textStyle:{
                align:'left'
            }
        },
        legend: {
            data:[]
        },
        toolbox: {
            show: true,
            feature: {
                dataView: {readOnly: true},
                saveAsImage: {},
                dataZoom: {
                    yAxisIndex: 'none'
                }
            }
        },
        //dataZoom: {        //下面的区域缩放轴
        //    show: true,
        //    start: 0,
        //    end: 100
        //},
        grid: {
            left: '2%',
            right: '3%',
            bottom: '3%',
            top: 85,
            containLabel: true
        },
        xAxis: [
            {
                type: 'category',
                boundaryGap: false,
                data : []
                //data: (function (){
                //    var now = new Date();
                //    var res = [];
                //    var len = 10;
                //    while (len--) {
                //        res.unshift(now.toLocaleTimeString().replace(/^\D*/,''));
                //        now = new Date(now - 2000);
                //    }
                //    return res;
                //})()
            }
        ],
        yAxis: [
            {
                type: 'value',
                name: '',
                min: 0
            }
        ],
        series: [],
        animationEasing: 'elasticOut',
        animationEasingUpdate : 100
    };
});

//echarts指令
angular.module('app').directive('lineEcharts', ['$echartsLineConfig','$window','$timeout', function ($echartsLineConfig,$window,$timeout) {
    return {
        restrict: 'AE',
        scope : {
            xaxisData                :'=',
            measurement        : '=',
            trendInfo            :'=',
            unit                 : '=',
            monitorData        : '=',
            timeline            :'=',
            type               :'=',
            idData               :'=',
            getData   : '&'
        },
        link: function (scope, element, attrs) {
            scope.$watch('xaxisData', function (newValue,oldValue){
                if(newValue==undefined) return;
                //处理Y轴数据
                var legendData = [],seriesData = [];
                for(var a in scope.monitorData){
                    if(scope.monitorData[a].name&&scope.monitorData[a].name!=''){
                        legendData.push(scope.monitorData[a].name);
                        seriesData.push({
                            name:scope.monitorData[a].name,
                            type:scope.type,
                            //symbol:'none',  //去掉点
                            showSymbol:false, //只有在 tooltip hover 的时候显示点
                            smooth:true,    //让曲线平滑
                            areaStyle: {normal: {   //每条线下面的部分填充
                                opacity:0.2
                            }},
                            //markPoint: {
                            //    data: [
                            //        {type: 'max', name: '最大值'},
                            //        {type: 'min', name: '最小值'}
                            //    ]
                            //},
                            //markLine: {
                            //    data: [
                            //        {type: 'average', name: '平均值'}
                            //    ]
                            //},
                            // itemStyle:{
                            //     normal:{color:color}
                            // },
                            //lineStyle:{
                            //    normal:{width:1}
                            //},
                            data:scope.monitorData[a].yAxis
                        })
                    }
                }

                scope.searchCostOption = {
                    title: {
                        text: scope.measurement,
                        left: 'left',          //title的位置
                        textStyle:{
                            fontSize: 16
                        }
                    },
                    legend: {
                        data:legendData,      //每个监控项的名字
                        top:'5.5%',
                        left:'9.2%',
                        orient:'horizontal'
                    },
                    xAxis : [
                        {
                            type : 'category',
                            boundaryGap : false,
                            axisLine: {onZero: false},
                            data : scope.xaxisData.map(function (str,index) {
                                var value = new Date(str);
                                var hours = ''+value.getHours();var minutes = ''+value.getMinutes();var seconds = ''+value.getSeconds();
                                if(hours.length==1) hours = '0'+hours;
                                if(minutes.length==1) minutes = '0'+minutes;
                                if(seconds.length==1) seconds = '0'+seconds;
                                var date1 = [value.getFullYear(),value.getMonth()+1,value.getDate()].join('-') + ' ' +  hours + ':' + minutes + ':' + seconds;
                                var date2 = [value.getMonth()+1,value.getDate()].join('-') + ' ' +  hours + ':' + minutes + ':' + seconds;
                                if(index==0) return date1.replace(' ', '\n');
                                else return date2.replace(' ', '\n');
                            })
                            //axisLabel: {      //设置时间轴的格式
                            //    formatter: function (value, index) {
                            //        var date = new Date(value);
                            //        return index === 0 ? value : [date.getFullYear(),date.getMonth() + 1, date.getDate()].join('-');
                            //    }
                            //}
                        }
                    ],
                    yAxis : [
                        {
                            name : scope.unit,
                            type : 'value',
                            nameGap:10
                        }
                    ],
                    series : seriesData
                };
                //设置副标题
                //if(scope.trendInfo&&scope.trendInfo!=""){
                //    scope.searchCostOption.title.subtext = scope.trendInfo;
                //}

                var option=angular.extend({},$echartsLineConfig,scope.searchCostOption);
                if(scope.timeline==6){   //如果是动态数据，去掉图表渲染的动画效果
                    option.animation = false;
                }
                if(scope.type=="bar"){    //如果是柱状图
                    option.tooltip.axisPointer = {
                        type : 'shadow'
                    }
                }

                scope.$echartsInstance = [];
                if (scope.idData) {
                    scope.$echartsInstance[scope.idData] = echarts.init(element[0]);
                    scope.$echartsInstance[scope.idData].setOption(option);
                    $timeout(function (){
                        $window.onresize = function () {
                            scope.$echartsInstance[scope.idData].resize();
                        };
                    },200);
                } else {
                    var myChart = echarts.init(element[0],'shine');
                    myChart.setOption(option);
                    $timeout(function (){
                        $window.onresize = function () {
                            myChart.resize();
                        };
                    },200);
                }
            },true);
        }
    };
}]);