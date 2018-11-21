app.controller('instrumentCtrl', ['$rootScope','$interval', '$scope', 'httpLoad','$state',function($rootScope,$interval, $scope, httpLoad,$state) {
  	$rootScope.moduleTitle = '数据纵览 > Dashboard';
    $rootScope.link = '/statics/css/common.css';//引入页面样式
		$scope.options = {
			vHost:{
				theme:'存储',
				colorMap:{
					'使用中':'#00D7AF',
					'未使用':'#5296EB',
					'异常':'#FF837E',
					'断开':'#CCCCCC'
				},
				dataMap:{
					ONLINE:'使用中',
					OFFLINE:'未使用',
					ERROR:'异常',
					UNKNOWN:'断开'
				}
			},
			vVm:{
				theme:'主机',
				colorMap:{
					'可调度':'#7BE299',
					'不可调度':'#FF837E',
					'异常':'#FFB07D',
					'正常':'#088fe0',
					'添加中':'#7266ba',
					'移出中':'#655209'
				},
				dataMap:{
					ONLINE:'可调度',
					OFFLINE:'不可调度',
					ERROR:'异常',
					NORMAL:'正常',
					ADD:'添加中',
					REMOVWE:'移出中'
				}
			},
			oHost:{
				showTitle:true,
				theme:'应用/容器',
				radius : ['40%','60%'],
				colorMap:{
					'运行中':'#25E2BE',
					'其他':'#FF6C8C',
					'异常':'#5296EB',
					'断开':'#CCCCCC'
				},
				legend:true,
				dataMap:{
					ONLINE:'运行中',
					OFFLINE:'其他',
					ERROR:'异常',
					UNKNOWN:'断开'
				}
			},
			oVm:{
				showTitle:true,
				theme:'虚拟机',
				radius : ['40%','60%'],
				colorMap:{
					'开机':'#62B1D8',
					'关机':'#FFB375',
					'异常':'#A66DE3',
					'断开':'#CCCCCC',
					'挂起':'#FFFF99'
				},
				legend:true,
				dataMap:{
					ONLINE:'开机',
					OFFLINE:'关机',
					ERROR:'异常',
					SUSPEND:'挂起',
					UNKNOWN:'断开'
				}
			},
			app:{
				showTitle:false,
				radius : ['70%','84%'],
				theme:'应用',
				label: {
					normal: {
						show: false,
						position: 'center'
					}
				},
				colorMap:{
					'开机':'#46B7EC',
					'关机':'#00D7AF',
					'异常':'#FDE669'
				},
				legend:false
			},
			alarm:{
				showTitle:false,
				radius : ['40%','55%'],
				theme:'告警',
				colorMap:{
					'警告':'#46B7EC',
					'严重':'#FF7A82'
				},
				dataMap:{
					DANGER:'严重',
					WARN:'警告'
				},
				legend:false
			}
		}
	$scope.envStatusData={1:"不可用",2:"激活",3:"冻结",4:"异常",5:"创建中",6:"死亡"};
    httpLoad.loadData({
        url: '/environment/list',
        method: 'POST',
        data: {page:1, rows: 10},
        noParam: true,
        success: function (data) {
            if (data.success) {
            	if(data.data.rows.length>0){
                    $scope.isnone = true;
                    $scope.isshow = false;
                    $scope.getData();
				}else{
                    $scope.isnone = false;
                    $scope.isshow = true;
				}
            }
        }
    });
    $scope.goOpen = function($event){
        $state.go('paas.environment.environmentAdd');
    };
		$scope.getData = function () {
            httpLoad.loadData({
                // url: '/stats/dashboard',
                url: '/statistic/statistic',
                method: 'GET',
                noParam:true,
                success: function(data){
                    if(data.success) {
                        $scope.isDataLoad = true;
                        $scope.dashboardData = data.data;

						/*	var arr=[];
						 if(($scope.dashboardData.environments.length>3)){
						 arr.push($scope.dashboardData.environments[0]);
						 arr.push($scope.dashboardData.environments[1]);
						 arr.push($scope.dashboardData.environments[2]);
						 $scope.dashboardData.environments=arr;
						 };*/
                        //饼状图
                        $scope.dashboardData.appUnrunNum=$scope.dashboardData.appTotal-$scope.dashboardData.appRunNum;
                        $scope.app=[
                            {"value":$scope.dashboardData.appRunNum, "name":"ONLINE"},
                            {"value":$scope.dashboardData.appUnrunNum, "name":"OFFLINE"},
                        ];
                        $scope.dashboardData.podUnrunTotal=$scope.dashboardData.podTotal-$scope.dashboardData.podRunTotal;
                        $scope.instance=[
                            {"value":$scope.dashboardData.podRunTotal, "name":"ONLINE"},
                            {"value":$scope.dashboardData.podUnrunTotal, "name":"OFFLINE"},
                        ];
                        $scope.hosts=[
                            {"value":$scope.dashboardData.normalHostNum, "name":"NORMAL"},
                            {"value":$scope.dashboardData.abnormalHostNum, "name":"ERROR"},
                            {"value":$scope.dashboardData.scheduNodeNum, "name":"ONLINE"},
                            {"value":$scope.dashboardData.unscheduNodeNum, "name":"OFFLINE"},
                            {"value":$scope.dashboardData.addingHostNum, "name":"ADD"},
                            {"value":$scope.dashboardData.outingHostNum, "name":"REMOVWE"},
                        ];
                        $scope.dashboardData.pvUnusedTotal=$scope.dashboardData.pvCapacityTotal-$scope.dashboardData.pvUsedTotal;
                        $scope.store=[
                            {"value":$scope.dashboardData.pvUsedTotal, "name":"ONLINE"},
                            {"value":$scope.dashboardData.pvUnusedTotal, "name":"OFFLINE"},
                        ]
                        //镜像数量排名
                        $scope.imageList=$scope.dashboardData.appMap.imageNumSort;
                        if($scope.imageList.length>6){
                            $scope.imageList.forEach(function (item,index) {
                                if(index>=6){
                                    $scope.imageList.splice(index,$scope.imageList.length);
                                }
                            })
                        }
                        // 更新排名
                        $scope.timeList=$scope.dashboardData.appMap.DateSort;
                        if($scope.timeList.length>6){
                            $scope.timeList.forEach(function (item,index) {
                                if(index>=6){
                                    $scope.timeList.splice(index,$scope.timeList.length);
                                }
                            })
                        }

                    }
                }
            });

            httpLoad.loadData({
                url: '/statistic/application',
                method: 'GET',
                noParam:true,
                success: function(data){
                    if(data.success){
                        $scope.cpuLists=data.data.cpu;
                        $scope.memoryLists=data.data.memory;
                    }
                }
            })
            // 操作日志
            httpLoad.loadData({
                url: '/log/list',
                method: 'POST',
                noParam:true,
                success: function(data){
                    if(data.success){
                        $scope.logInfo = data.data.rows;
                        if($scope.logInfo.length>6){
                            $scope.logInfo.forEach(function (item,index) {
                                if(index>=6){
                                    $scope.logInfo.splice(index,$scope.logInfo.length);
                                }
                            })
                        }
                        // console.log($scope.logInfo);
                    }
                }
            })
        }

    $scope.timeInterval = $interval(function () {
        $scope.getData();
    }, 120*1000);
    $scope.$on("$destroy", function(event) {
        $interval.cancel($scope.timeInterval);
        $scope.timeInterval = '';
    });
}]);
app.directive('ngDashboardPieLoopChart', [function () {
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
					tooltip: {
						trigger: 'item',
						formatter: "{b}: {c} ({d}%)",
						position: ['0%', '70%']
					},
					legend: {
						orient: 'vertical',
						x: 'left'
					},
					series: [
						{
							// name:'应用个数',
							type:'pie',
							radius: ['50%', '70%'],
							avoidLabelOverlap: false,
							label: {
								normal: {
									show: false,
									position: 'center'
								},
								emphasis: {
									show: false,
									textStyle: {
										fontSize: '16',
										fontWeight: 'normal'
									}
								}
							},
							labelLine: {
								normal: {
									show: false
								}
							},
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
