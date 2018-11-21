
app.controller('workflowDetailModalCtrl', ['$scope', 'httpLoad', '$rootScope','$modal','$state','$timeout','$stateParams','$sce',
     function($scope, httpLoad, $rootScope, $modal,$state, $timeout,$stateParams,$sce) {
    $rootScope.link = '/statics/css/image.css';//引入页面样式
       $rootScope.moduleTitle =$sce.trustAsHtml('流程管控 > <span>流程编排模板</span> > 运行实例  > 运行实例详情');
       $scope.statusList ={
         "RUNNING":[{"key":"TERMINATED","value":"终止","class":"btn-danger"},{"key":"PAUSED","value":"暂停","class":"btn-warning"}],
         "PAUSED":[{"key":"RESUME","value":"继续","class":"btn-success"}],
         "TERMINATED":[{key:"RESTART",value:"重启",class:"btn-default"},{key:"RETRY",value:"重试",class:"btn-default"}],
           "CANCELED":[{key:"RESTART",value:"重启",class:"btn-default"},{key:"RETRY",value:"重试",class:"btn-default"}],
         "TIMED_OUT":[{key:"RESTART",value:"重启",class:"btn-default"}],
         "COMPLETED":[{key:"RESTART",value:"重启",class:"btn-default"}],
         "FAILED":[{key:"RESTART",value:"重启",class:"btn-default"},{key:"RETRY",value:"重试",class:"btn-default"}]
       }
       
        var  workflowId = $stateParams.id;
       var  name = $stateParams.name;
       var version = $stateParams.version;
       var socket = new WebSocket('ws://' + location.host +'/conductorService');
       $scope.webstocket = function(){
		socket.onopen = function () {
			
			socket.send(JSON.stringify({
					'workflowId': workflowId,
					"name":name,
					'version': version
				}));


		};
		socket.onmessage = function (event) {
			var obj = JSON.parse(event.data);
			console.log(obj)
			if (obj.context == "continue") {
                $timeout(function () {
                    $scope.supplierDetail = obj;
             
                    $scope.total = $scope.supplierDetail.tasks.length;
                    if ($scope.supplierDetail != null) {
                        $scope.statusListData =  $scope.statusList[$scope.supplierDetail.wFjson.status];
                        $scope.showDetail = $scope.isActive = true;
                        $('#json-renderer').jsonViewer($scope.supplierDetail.wFjson);
                        
                      
//                        $('#json-rendererin').jsonViewer($scope.supplierDetail.wFjson.input);
//                        $('#json-rendererout').jsonViewer($scope.supplierDetail.wFjson.output);
                    }
                },100)
				 socket.send(JSON.stringify({
						'workflowId': workflowId,
						"name":name,
						'version': version
					}));
			}else if (obj.context == "end") {
                $timeout(function () {
                    $scope.supplierDetail= obj;
                    $scope.total = $scope.supplierDetail.tasks.length;
                    if ($scope.supplierDetail != null) {
                        $scope.statusListData =  $scope.statusList[$scope.supplierDetail.wFjson.status];
                        $scope.showDetail = $scope.isActive = true;
                        $scope.supplierDetail.wFjson.tasks.forEach(function(item){
                        	if(item.inputData&&item.inputData.password){                        		
                        		item.inputData.password = "*********"
                        	}	
                        })
                         $scope.getData();
                        $('#json-renderer').jsonViewer($scope.supplierDetail.wFjson);
//                        $('#json-rendererin').jsonViewer($scope.supplierDetail.wFjson.input);
//                        $('#json-rendererout').jsonViewer($scope.supplierDetail.wFjson.output);
                    }
                },100)

				socket.close();
			} else {
				socket.close();
			}
		}
   }
     $scope.webstocket()
         $scope.$on("$destroy", function(event) {
             socket.close();
         });
       $scope.getData = function(status){
           // httpLoad.loadData({
           //     url:'/workflow/workflow',
           //     method:'GET',
           //     data: {workflowId:workflowId},
           //     success:function(data){
           //         if(data.success&&data.data){
           //             $scope.supplierDetail = data.data;
           //
           //             if ($scope.supplierDetail != null) {
           //          	   $scope.statusListData =  $scope.statusList[$scope.supplierDetail.WFjson.status];
           //                 $scope.showDetail = $scope.isActive = true;
           //                 $('#json-renderer').jsonViewer($scope.supplierDetail.WFjson);
           //                 $('#json-rendererin').jsonViewer($scope.supplierDetail.WFjson.input);
           //                 $('#json-rendererout').jsonViewer($scope.supplierDetail.WFjson.output);
			// 		   }
           //         }
           //     }
           // });
           httpLoad.loadData({
        	   url: '/workflow/workflowDefJSON',
               method:'GET',
               data: {name:name,version: version},
               success:function(data){
                   if(data.success&&data.data){
                	   $scope.detailJson = data.data;
                	   var workflowJson = angular.fromJson(data.data.workflowJson);
                	   $scope.postData = workflowJson.postData;
                       $scope.changeData = true;


                   }
               }
           });

       }
       $scope.getData();

       //改变状态
    $scope.changeColor = function(status){
      var arrayTasks = $scope.supplierDetail.tasks;
    }
       //流程图
     $scope.manager = function(status){
       var url ="";
       switch(status){
         case 'TERMINATED': //终止
          url ="/workflow/terminate";
           break;
         case 'PAUSED'://暂停
            url ="/workflow/pause";
           break;
         case 'RESTART'://重启
            url ="/workflow/restart";
           break;
         case 'RETRY': //重试
          url ="/workflow/retry";
           break;
         case 'RESUME': //继续
          url ="/workflow/resume";
           break;
       }
       httpLoad.loadData({
           url:url,
           method:'POST',
           data: {workflowId:workflowId},
           success:function(data){
               if(data.success){
                   location.reload()
               }
           }
       });

       };
     $scope.goBack = function(){
          var  name = $stateParams.name;
          var version = $stateParams.version;
           $state.go('paas.process.layout');
       };
       $scope.Taskdetail = function(row){
         var modalInstance = $modal.open({
           templateUrl : '/statics/tpl/process/workflow/taskbaskdetal.html',
           controller : 'taskbaskModalCtrl',
             backdrop: 'static',
           resolve : {
             row : function(){
               return row;
             },

           }
         });
         modalInstance.result.then(function(){
           $scope.getData();
           $scope.isCheck = false;
         },function(){});
       };
       $scope.Tasklist = function(row){
         var modalInstance = $modal.open({
           templateUrl : '/statics/tpl/process/workflow/taskbasklist.html',
           controller : 'taskbasklistModalCtrl',
           size:"lg",
             backdrop: 'static',
           resolve : {
             row : function(){
               return row;
             },

           }
         });
         modalInstance.result.then(function(){

         },function(){});
       }
     }
 ]);
 app.controller('taskbaskModalCtrl', ['$scope', '$modalInstance', '$modal', '$stateParams', '$timeout', 'httpLoad',  'row', 'CommonData',
   function ($scope, $modalInstance, $modal, $stateParams, $timeout, httpLoad, row, CommonData) {
     $scope.modalName = "详情展示";
     $scope.taskDetail = row;
     var item = $scope.taskDetail
     setTimeout(function(){
    	 if($scope.taskDetail.inputData){
    		
    		 if(item.inputData.password){
                 item.inputData.password = "*********"
             }
    	 }
    	 if($scope.taskDetail.output){
    		
    		 if(item.inputData.password){
                 item.inputData.password = "*********"
             }
    	 }
         
    	  $('#json-input').jsonViewer($scope.taskDetail.inputData);
    	     $('#json-output').jsonViewer($scope.taskDetail.outputData);
     },100)

     $scope.cancel = function () {
       $modalInstance.dismiss('cancel');
     };
   }
 ]);
 app.controller('taskbasklistModalCtrl', ['$scope', '$modalInstance', '$modal', '$stateParams', '$timeout', 'httpLoad',  'row', 'CommonData',
   function ($scope, $modalInstance, $modal, $stateParams, $timeout, httpLoad, row, CommonData) {
     $scope.modalName = "Job构建历史记录";
     var  workflowId = $stateParams.id;
     $scope.getData = function(){
       httpLoad.loadData({
           url:"/task/build/count",
           method:'GET',
           data: {workflowId:workflowId,taskId:row.taskId},
           success:function(data){
               if(data.success){
                 $scope.jobname = data.data.jobName ;
                 $scope.historyList = data.data.buildModels;
                $scope.totalList = data.data.buildModels.length;
              }
           }
       });
     }
     $scope.getData();
     $scope.cancel = function () {
       $modalInstance.dismiss('cancel');
     };
     $scope.aaa = function(row){
       httpLoad.loadData({
           url:"/task/build/output",
           method:'GET',
           data: {jobName:$scope.jobname,buildNum:row.number},
           success:function(data){
               if(data.success){
                 $scope.logPod = data.data.replace(/\\r\\n/g,"<br>");
                 $scope.showDetailContainer = true;
                 $scope.showDetail = 7;
              }
           }
       });
     
     }
     $scope.goBack = function(){
      
            $timeout(function() {
            	  $scope.showDetailContainer = false;
                $scope.showDetail = false;
            }, 200);
           //终止监测

        };
   }
 ]);
