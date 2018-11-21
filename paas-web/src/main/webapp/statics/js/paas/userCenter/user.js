(function(){
	"use strict";
	app.controller('userCtrl', ['$scope', 'httpLoad', '$rootScope','$modal','$state','$timeout','LANGUAGE',
		function($scope, httpLoad, $rootScope, $modal,$state, $timeout,LANGUAGE) {
			$rootScope.moduleTitle = '用户中心 > 用户管理';//定义当前页
			$rootScope.link = '/statics/css/user.css';//引入页面样式
			$scope.param = {
				rows: 10
			};
			$scope.categoryData = [{"name":"女","value":"false"},{"name":"男","value":"true"}];
			//获取用户列表
			$scope.getData = function(page){
				$scope.showDetail = false;
				$scope.param.page = page || $scope.param.page;
				var params = {
					 page: $scope.param.page,
					 rows: $scope.param.rows
				},
				searchParam = [];
				if($scope.searchBySex&&$scope.searchBySex!=""){
					if($scope.searchBySex==='true') searchParam.push({"param":{"sex":true},"sign":"EQ"});
					else if($scope.searchBySex==='false') searchParam.push({"param":{"sex":false},"sign":"EQ"});
				}
				if($scope.searchByUsername&&$scope.searchByUsername!=""){
					searchParam.push({"param":{"username":$scope.searchByUsername},"sign":"LK"});
				}
				if($scope.searchByUserId&&$scope.searchByUserId!=""){
					if(searchParam.length==0) searchParam.push({"param":{"userId":$scope.searchByUserId},"sign":"LK"});
					else {
						var a = 0;
						for(var i=0;i<searchParam.length;i++){
							if(searchParam[i].sign=="LK") {
								a = 1;
								searchParam[i].param.userId = $scope.searchByUserId;
							}
						}
						if(a==0) searchParam.push({"param":{"userId":$scope.searchByUserId},"sign":"LK"});
					}
				}
				if($scope.searchByName&&$scope.searchByName!=""){
					if(searchParam.length==0) searchParam.push({"param":{"name":$scope.searchByName},"sign":"LK"});
					else {
						var a = 0;
						for(var i=0;i<searchParam.length;i++){
							if(searchParam[i].sign=="LK") {
								a = 1;
								searchParam[i].param.name = $scope.searchByName;
							}
						}
						if(a==0) searchParam.push({"param":{"name":$scope.searchByName},"sign":"LK"});
					}
				}
				if($scope.searchByPhone&&$scope.searchByPhone!=""){
					if(searchParam.length==0) searchParam.push({"param":{"phone":$scope.searchByPhone},"sign":"LK"});
					else {
						var a = 0;
						for(var i=0;i<searchParam.length;i++){
							if(searchParam[i].sign=="LK") {
								a = 1;
								searchParam[i].param.phone = $scope.searchByPhone;
							}
						}
						if(a==0) searchParam.push({"param":{"phone":$scope.searchByPhone},"sign":"LK"});
					}
				}
				if($scope.searchByMobile&&$scope.searchByMobile!=""){
					if(searchParam.length==0) searchParam.push({"param":{"mobile":$scope.searchByMobile},"sign":"LK"});
					else {
						var a = 0;
						for(var i=0;i<searchParam.length;i++){
							if(searchParam[i].sign=="LK") {
								a = 1;
								searchParam[i].param.mobile = $scope.searchByMobile;
							}
						}
						if(a==0) searchParam.push({"param":{"mobile":$scope.searchByMobile},"sign":"LK"});
					}
				}
				if($scope.searchByState&&$scope.searchByState!=""){
					if(searchParam.length==0) searchParam.push({"param":{"status":$scope.searchByState},"sign":"EQ"});
					else {
						var a = 0;
						for(var i=0;i<searchParam.length;i++){
							if(searchParam[i].sign=="EQ") {
								a = 1;
								searchParam[i].param.status = $scope.searchByState;
							}
						}
						if(a==0) searchParam.push({"param":{"status":$scope.searchByState},"sign":"EQ"});
					}
				}
				params.params = JSON.stringify(searchParam);
				httpLoad.loadData({
					url:'/user/list',
					method: 'POST',
					data: params,
					noParam: true,
					success:function(data){
						if(data.success&&data.data.rows){
							$scope.userList = data.data.rows;
							$scope.totalCount = data.data.total;
							if(data.data.rows.length==0) {
								$scope.isImageData = true;
								return;
							} else $scope.isImageData = false;
						}else{
							$scope.isImageData = true;
						}						
					}
				});
			};
			$scope.getData(1);

			//获取组织机构数据
			httpLoad.loadData({
				url: '/department/list',
				method: 'POST',
				data: {'parentId':0},
				noParam:true,
				success: function(data){
					if(data.success && data.data && data.data.length!=0){
						$scope.treeData = data.data;
					}
				}
			});
			//返回
			$scope.goBack = function(){
				$scope.isActive = false;
				$timeout(function() {
					$scope.showDetail = false;
				}, 200);
			};
			//跳转详情页
			$scope.detail = function (id) {
				httpLoad.loadData({
					url:'/user/detail',
					method:'GET',
					data: {id: id},
					success:function(data){
						if(data.success&&data.data){
							$scope.userDetail = data.data;
							$scope.showDetail = $scope.isActive = true;
						}
					}
				});
			};
			//资源绑定
			$scope.bindHost = function(id){
				$state.go('paas.userCenter.userResource',{id:id})
			};
            //通知资源
            $scope.strategy = function(id){
                $scope.treeData = $scope.treeData||"";
                var modalInstance = $modal.open({
                    templateUrl : '/statics/tpl/userCenter/user/strategy.html',
                    controller : 'strategyModalCtrl',
                    size : 'sm',
                    resolve : {
                        id : function(){
                            return id;
                        }
                    }
                });
                modalInstance.result.then(function(result){
                    $scope.getData();
                },function(){});
            };
			//授权
			$scope.grant = function($event,$index,id,key,size){
				$event.stopPropagation();
				$scope.permissionList = {"id":id};
				$scope.nodeId = [];
				httpLoad.loadData({
					url:'/user/roles',
					method:'POST',
					data: $scope.permissionList,
					success:function(data){
						$scope.roledata = data.data;
						var modalInstance = $modal.open({
							templateUrl : '/statics/tpl/userCenter/user/grant.html',  //指向上面创建的视图
							controller : 'grantUserModalCtrl',// 初始化模态范围
							size : size,
							resolve : {
								permissionList : function(){
									return $scope.permissionList;
								},
								roledata : function(){
									return $scope.roledata;
								},
								id : function(){
									return id;
								}
							}
						});
						modalInstance.result.then(function(result){
							$scope.getData();
						},function(){});
					}
				});
			};
			//重置密码
			$scope.reset = function($event,$index,id,key){
				$event.stopPropagation();
				httpLoad.loadData({
					url:'/user/reset',
					method:'POST',
					data:{id: id},
					success:function(data){
						if(data.success){
							$scope.pop(LANGUAGE.OPERATION.USER.RESET);
							$scope.getData();
						}
					}
				});
			};
			//批量导入
			$scope.export = function($event,size){
				$event.stopPropagation();
				var modalInstance = $modal.open({
					templateUrl : '/statics/tpl/userCenter/user/export.html',
					controller : 'exportUserModalCtrl',
					size : size
				});
				modalInstance.result.then(function(result){
					$scope.getData();
				},function(){});
			};
			//新增
			$scope.add = function($event,size){
				$scope.treeData = $scope.treeData||"";
				var modalInstance = $modal.open({
					templateUrl : '/statics/tpl/userCenter/user/add.html',
					controller : 'addUserModalCtrl',
					size : size,
					backdrop: 'static',
					resolve : {
						treeData: function(){
							return $scope.treeData;
						},
						userList : function(){
							return $scope.userList;
						}
					}
				});
				modalInstance.result.then(function(result){
					$scope.getData();
				},function(){});
			};
			//编辑
			$scope.update = function($event,$index,row,key,size){
				$event.stopPropagation();
				$scope.treeData = $scope.treeData||"";
				var modalInstance = $modal.open({
					templateUrl : '/statics/tpl/userCenter/user/update.html',
					controller : 'updateUserModalCtrl',
					size : size,
					backdrop: 'static',
					resolve : {
						id : function(){
							return row.id;
						},
						updateData : function(){
							return $scope.userList[$index];
						},
						treeData: function(){
							return $scope.treeData;
						}
					}
				});
				modalInstance.result.then(function(){
					$scope.getData();
				},function(){});
			};
			//删除
			$scope.remove = function(id,$event,$index,key){
				if($event) $event.stopPropagation();
				$scope.removeData= {"id" : id};
				var modalInstance = $modal.open({
					templateUrl : '/statics/tpl/userCenter/user/remove.html',
					controller : 'removeUserModalCtrl',
					resolve : {
						index : function(){
							return $index;
						},
						removeData : function(){
							return $scope.removeData;
						},
						userList : function(){
							return $scope.userList;
						}
					}
				});
				modalInstance.result.then(function(){
					$scope.getData();
					$scope.isCheck = false;
				},function(){});
			};
			//冻结
			$scope.freeze = function($event,id){
				$event.stopPropagation();
				$scope.freezeData= {"id" : id};
				var modalInstance = $modal.open({
					templateUrl : '/statics/tpl/userCenter/user/freeze.html',
					controller : 'freezeUserModalCtrl',
					resolve : {
						freezeData : function(){
							return $scope.freezeData;
						}
					}
				});
				modalInstance.result.then(function(){
					$scope.getData();
				},function(){});
			};
			//解冻
			$scope.thaw = function($event,id){
				$event.stopPropagation();
				$scope.thawData= {"id" : id};
				var modalInstance = $modal.open({
					templateUrl : '/statics/tpl/userCenter/user/thaw.html',
					controller : 'thawUserModalCtrl',
					resolve : {
						thawData : function(){
							return $scope.thawData;
						}
					}
				});
				modalInstance.result.then(function(){
					$scope.getData();
				},function(){});
			};
			//配额
			$scope.size = function(item){  //打开模态
				var modalInstance = $modal.open({
					templateUrl : '/statics/tpl/userCenter/user/size.html',  //指向上面创建的视图
					controller : 'sizeuserModalCtrl',// 初始化模态范围
					resolve : {
						item: function() {
							return item;
						}
					}
				});
				modalInstance.result.then(function(){
					$scope.getData();
				},function(){});
			};
		}
	]);

	//授权ctrl
	angular.module('app').controller('grantUserModalCtrl',['$scope','$modalInstance','httpLoad','permissionList','roledata','id','LANGUAGE',
		function($scope,$modalInstance,httpLoad,permissionList,roledata,id,LANGUAGE){ //依赖于modalInstance
			$scope.permissionList = permissionList;
			$scope.roledata = roledata;
			$scope.ok = function(){
				var nodeId = '';
				angular.forEach($scope.roledata, function(data,index){
					if(data.checked){
						nodeId += ','+ data.id;
					}
				});
				nodeId = nodeId.substr(1);
				$scope.grantData ={id:id,roles:nodeId};
				httpLoad.loadData({
					url: '/user/accredit',
					method: 'POST',
					data: $scope.grantData,
					success:function(data){
						if(data.success){
							//console.log($scope.grantData);
							$scope.pop(LANGUAGE.OPERATION.USER.GRANT_SUCCESS);
							$modalInstance.close();
						}
					}
				});
			};
			$scope.cancel = function(){
				$modalInstance.dismiss('cancel'); // 退出
			}
		}]);
    //通知资源ctrl
	angular.module('app').controller('strategyModalCtrl',['$scope','$modalInstance','httpLoad','id','LANGUAGE',
        function($scope,$modalInstance,httpLoad,id,LANGUAGE){ //依赖于modalInstance
            $scope.modeData = [{"value":"SMS","name":"短信","isRegionActive":false},{"value":"EMAIL","name":"邮件","isRegionActive":false},{"value":"MESSAGE","name":"站内信","isRegionActive":false}];
            var getid='';
            httpLoad.loadData({
                url:'/notice/strategy/getNotice',
                method:'GET',
                data: {"userId":id},
                success:function(data){
                    if(data.success&&data.data){
                        $scope.messageDetail = data.data;
                        $scope.mode = "";
                        getid = $scope.messageDetail.id;
                        if($scope.messageDetail.mode.indexOf(',')>=0){
                            var list = $scope.messageDetail.mode.split(',');
                            for(var i=0;i<list.length;i++){
                                angular.forEach($scope.modeData, function(data,index){
                                    if(list[i]==data.value) {
                                        $scope.mode = $scope.mode+' '+data.name;
                                        $scope.modeData[index].isRegionActive=true
                                    }
                                });
                            }
                        }else{
                            angular.forEach($scope.modeData, function(data,index){
                                if($scope.messageDetail.mode==data.value){
                                    $scope.mode = data.name;
                                    $scope.modeData[index].isRegionActive=true
								}
                            });
                        }
                    }
                }
            });
            $scope.chooseRegion = function($index){
                $scope.modeData[$index].isRegionActive = !$scope.modeData[$index].isRegionActive;
            };
            $scope.ok = function(){
                var mode='';
                angular.forEach($scope.modeData, function(data,index){
                    if(data.isRegionActive==true) {
                        mode = mode + ','+data.value;
                    }
                });
                mode = mode.substr(1);
  			var datalist = {id:getid,mode:mode};
                httpLoad.loadData({
                    url:'/notice/strategy/modify',
                    method:'POST',
                    data: datalist,
                    success:function(data){
                        if(data.success){
                            // var id = data.data["id"];
                            $scope.pop('基本信息修改成功');
                            $modalInstance.close();
                        }
                    }
                });
            };
            $scope.cancel = function(){
                $modalInstance.dismiss('cancel'); // 退出
            }
        }]);
	//批量导入ctrl
	angular.module('app').controller('exportUserModalCtrl',['$scope','$modalInstance','httpLoad',
		function($scope,$modalInstance,httpLoad){ //依赖于modalInstance
			$scope.websocketUrl='/uploadService';
			$scope.cancel = function(action){
				if(action==0) $modalInstance.close();
				$modalInstance.dismiss('cancel');  // 退出
			}
		}]);
	//新增ctrl
	angular.module('app').controller('addUserModalCtrl',['$scope','$modalInstance','httpLoad','userList','LANGUAGE','treeData',
		function($scope,$modalInstance,httpLoad,userList,LANGUAGE,treeData){ //依赖于modalInstance
			$scope.isSame=false;
			$scope.index = 2;
			var aa=JSON.stringify(treeData);$scope.treeData=JSON.parse(aa);
			$scope.categoryData = [{"name":"女","value":"false"},{"name":"男","value":"true"}];

			$scope.addData={};
			$scope.addData.loginStatus=0;
			if($scope.confirmPassword!=$scope.addData.password){
				$scope.isSame=true;
				$scope.addUserForm.$invalid=false;
			}else{
				$scope.isSame=false;
			}
		/*	//check 用户名      唯一性
			$scope.checkusername = function(){
				if(!$scope.addData.username) return;
				if($scope.addData.username=="") return;
				httpLoad.loadData({
					url:'/user/checkUsername',
					method:'POST',
					data: {"username":$scope.addData.username},
					success:function(data){
						if(data.success){
							$scope.addUserForm.$invalid = false;
						}else{
							//$scope.pop(data.message,'error');
							$scope.addUserForm.$invalid = true;
							$scope.addData.username = "";
						}
					}
				});
			};*/

			$scope.ok = function(){
				var sValues = $("#mycombotree2").combotree("getValues");
				if(sValues[0]!="") $scope.addData.departId = sValues[0];
				if(!$scope.addData.departId||$scope.addData.departId==""){
					$scope.pop("请选择组织机构",'error');
					return;
				}
				httpLoad.loadData({
					url:'/user/create',
					method:'POST',
					data: $scope.addData,
					success:function(data){
						if(data.success&&data.data){
							var id = data.data["id"];
							$scope.pop(LANGUAGE.OPERATION.USER.ADD_SUCCESS);
							$modalInstance.close(id);
						}
					}
				});
			};
			$scope.cancel = function(){
				$modalInstance.dismiss('cancel'); // 退出
			}
	}]);
	//编辑ctrl
	angular.module('app').controller('updateUserModalCtrl',['$scope','$modalInstance','httpLoad','updateData','LANGUAGE','treeData',
		function($scope,$modalInstance,httpLoad,updateData,LANGUAGE,treeData){ //依赖于modalInstance
			$scope.index = 1;var bb=JSON.stringify(treeData);$scope.treeData=JSON.parse(bb);
			$scope.categoryData = [{"name":"女","value":"false"},{"name":"男","value":"true"}];

			var aa=JSON.stringify(updateData);
			$scope.updateData=JSON.parse(aa);

			$scope.groupId = $scope.updateData.departId;$scope.groupName = $scope.updateData.departName;
			$scope.updateData.sex = ""+$scope.updateData.sex;

			$scope.ok = function(){
				var sValues = $("#mycombotree1").combotree("getValues");
				if(sValues[0]!="") $scope.updateData.departId = sValues[0];
				if(!$scope.updateData.departId||$scope.updateData.departId==""){
					$scope.pop("请选择组织机构",'error');
					return;
				}
				httpLoad.loadData({
					url:'/user/modify',
					method:'POST',
					data: $scope.updateData,
					success:function(data){
						if(data.success){
							//console.log($scope.updateData);
							$scope.pop(LANGUAGE.OPERATION.USER.EDIT_SUCCESS);
							$modalInstance.close();
						}
					}
				});
			};
			$scope.cancel = function(){
				$modalInstance.dismiss('cancel'); // 退出
			}
		}]);
	//删除ctrl
	angular.module('app').controller('removeUserModalCtrl',['$scope','$modalInstance','httpLoad','removeData','userList','index','LANGUAGE',
		function($scope,$modalInstance,httpLoad,removeData,userList,index,LANGUAGE){ //依赖于modalInstance
			$scope.content = '是否删除用户？';
			$scope.ok = function(){
				httpLoad.loadData({
					url:'/user/remove',
					method:'POST',
					data: removeData,
					success:function(data){
						if(data.success){
							//console.log(removeData);
							$scope.pop(LANGUAGE.OPERATION.USER.DEL_SUCCESS);
							$modalInstance.close();
						}
					}
				});
			};
			$scope.cancel = function(){
				$modalInstance.dismiss('cancel'); // 退出
			}
		}]);
	//冻结ctrl
	angular.module('app').controller('freezeUserModalCtrl',['$scope','$modalInstance','httpLoad','freezeData','LANGUAGE',
		function($scope,$modalInstance,httpLoad,freezeData,LANGUAGE){ //依赖于modalInstance
			$scope.ok = function(){
				httpLoad.loadData({
					url:'/user/lock',
					method:'POST',
					data: freezeData,
					success:function(data){
						if(data.success){
							//console.log(freezeData);
							$scope.pop(LANGUAGE.OPERATION.USER.FREEZE_SUCCESS);
							$modalInstance.close();
						}
					}
				});
			};
			$scope.cancel = function(){
				$modalInstance.dismiss('cancel'); // 退出
			}
	}]);
	//解冻ctrl
	angular.module('app').controller('thawUserModalCtrl',['$scope','$modalInstance','httpLoad','thawData','LANGUAGE',
		function($scope,$modalInstance,httpLoad,thawData,LANGUAGE){ //依赖于modalInstance
			$scope.ok = function(){
				httpLoad.loadData({
					url:'/user/active',
					method:'POST',
					data: thawData,
					success:function(data){
						if(data.success){
							//console.log(thawData);
							$scope.pop(LANGUAGE.OPERATION.USER.THAW_SUCCESS);
							$modalInstance.close();
						}
					}
				});
			};
			$scope.cancel = function(){
				$modalInstance.dismiss('cancel'); // 退出
			}
		}]);
	//配额ctrl
	angular.module('app').controller('sizeuserModalCtrl',['$scope','$modalInstance','item','LANGUAGE','httpLoad',
		function($scope,$modalInstance,item,LANGUAGE,httpLoad){ //依赖于modalInstance
			$scope.sizeData = {
				"id":item.id,
				"cpu":item.cpu,
				"memory":item.memory,
				"disk":item.disk
			};

			$scope.ok = function(){
				httpLoad.loadData({
					url:'/user/quota',
					method:'POST',
					data: $scope.sizeData,
					success:function(data){
						if(data.success){
							$scope.pop(LANGUAGE.OPERATION.USER.SIZE_SUCCESS);
							$modalInstance.close();
						}
					}
				});
			};
			$scope.cancel = function(){
				$modalInstance.dismiss('cancel'); // 退出
			};
		}]);


	angular.module('app').directive('userCombotree',
		['$rootScope', '$timeout', 'httpLoad', function ($rootScope,$timeout,httpLoad) {
			return {
				restrict: 'AE',
				scope : {
					treeData        : '=',
					groupId          : '=',
					groupName         :'=',
					index              : '='
				},
				link: function (scope, element, attrs) {
					scope.$watch('treeData',function(newValue,oldValue){
						element.combotree({
							data: scope.treeData,
							textField :"text",
							valueField : "id",
							emptyText : '请选择',
							onBeforeExpand: function(row,param){
								$('#mycombotree'+scope.index).combotree('tree').tree('options').url = '/department/list?parentId='+row.id;
							},
							onSelect:function(row) {
								scope.groupId = row.id;
								//httpLoad.loadData({
								//	url:'/department/detail',
								//	method:'GET',
								//	data: {id: row.id},
								//	success:function(data){
								//		if(data.success){
								//			scope.path = data.data.path;
								//		}
								//	}
								//});
							},
							onLoadSuccess :function(node, data){
								if(scope.groupId){
									/*for(var a in data){
									 if(data[a].id==scope.groupId){
									 scope.groupName = data[a].text;
									 }
									 }*/
									defaultValue('mycombotree1',scope.groupId,scope.groupName);
									/*$('#mycombotree').combotree('setValue',{
									 id: scope.groupId,
									 text: scope.groupName
									 });*/
								}
								//deftext：生成节点的文本用于显示
								function defaultValue(cbtid,defVal,defText){
									var combotree =$("#"+cbtid);
									var tree = combotree.combotree('tree');
									var defNode = tree.tree("find",defVal);
									if(!defNode){
										tree.tree('append', {
											data: [{
												id: defVal,
												name: defText,
												parentId:0,
												children:"",
												checked:false
											}]
										});
										defNode = tree.tree("find",defVal);
										combotree.combotree('setValue',defVal);
										tree.tree('select',defNode.target);
										defNode.target.style.display='none';
									}else{
										combotree.combotree('setValue',defVal);
									}
								}
							},
							loadFilter: function(rows,parent){
								if(rows.success) rows = rows.data;
								var nodes = [];
								// get the top level nodes
								for(var i=0; i<rows.length; i++){
									var row = rows[i];
									var state = 'open';
									//if (!exists(rows, row.parentId)){
									if(row.children){
										state = 'closed';
										if(row.children=="[]") row.children=[];
									} else state = 'open';
									//}
									nodes.push({
										id:row.id,
										text:row.name,
										parentId:row.parentId,
										children:row.children,
										checked:row.checked,
										state:state
									});
								}
								return nodes;
							}
						});
					});
				}
			};
		}]);

	angular.module('app').directive('ngFileUpload2', ['$rootScope', 'httpLoad', function ($rootScope, httpLoad) {
		return {
			restrict: 'EA',
			scope:true,
			link: function (scope, element, attrs) {
				scope.item = {};
				scope.isProgressbar = false;scope.isUploadbtn = false;scope.isCancelbtn = false;
				$('#btnFileUpload').on('change', function (event) {
					var files = event.target.files, list = [];
					scope.filePath = '';
					//if(/[\u4e00-\u9fa5]/.test(files[0].name)){
					//	$rootScope.pop('文件名不允许存在中文','error');
					//};
					//对文件大小和类型进行过滤
					var arr = files[0].name.split('.');
					if (['xls','xlsx','xlsm','xltx','xltm','xlsb','xlam'].indexOf(arr[arr.length - 1]) == -1) {
						scope.$apply(function () {
							$rootScope.pop('请上传EXCEL文件，【.xls,.xlsx,.xlsm,.xltx,.xltm,.xlsb,.xlam】','error');
						});
						return;
					}
					scope.isProgressbar = false;scope.isUploadbtn = true;scope.isCancelbtn = false;
					$('#btnFileUpload').html($('#btnFileUpload').html());
					scope.$apply(function () {
						scope.gridinPullName = files[0].name;
					});
					scope.isUploadbtn = true;
					scope.item  = {file: files[0]};
				});
				var socket ;
				scope.cancelFile = function (item) {
					if(socket){
						if(socket.onopen.readyState==4) {
							socket.send(JSON.stringify({
								'UPLOAD_CANCEL': 'UPLOAD_CANCEL'
							}));
							scope.updataBtn = false
						}
					}
				};

				scope.upload = function (item) {
					socket = new WebSocket('ws://' + location.host + scope.websocketUrl);
					var i = 0;var startSize = 0,endSize = 0;
					var paragraph = 4 * 1024 * 1024;    //以4MB为一个分片
					var count = parseInt(item.file.size / paragraph) + 1;
					socket.onopen = function () {
						item.isUploading = true;
						socket.send(JSON.stringify({
							'filename': scope.gridinPullName,
							registryId:scope.warehoseItem,
							'upload': 'file'
						}));
					};
					socket.onmessage = function (event) {
						var sendFile = function(){
							if(startSize < item.file.size) {
								var blob;
								startSize = endSize;
								endSize += paragraph;

								if (item.file.webkitSlice) {
									blob = item.file.webkitSlice(startSize, endSize);
								} else if (item.file.mozSlice) {
									blob = item.file.mozSlice(startSize, endSize);
								} else {
									blob = item.file.slice(startSize, endSize);
								}
								var reader = new FileReader();
								reader.readAsArrayBuffer(blob);

								reader.onload = function loaded(evt) {
									if(socket.readyState == 3){
										return
									}
									var result = evt.target.result;
									i++;
									var isok = (i / count) * 100;
									item.progress = parseInt(isok);

									socket.send(result);
								};
							}else{
								item.progress = 100;
								socket.send(JSON.stringify({
									'sendover': 'sendover'
								}));
							}
						};
						scope.updataBtn = true;
						item.isUploading = true;
						item.isCancel = false;
						scope.isProgressbar = true;scope.isUploadbtn = false;scope.isCancelbtn = true;
						var obj = JSON.parse(event.data);
						console.log(obj)
						if (obj.category == "UPLOAD_ACK") {
							item.filePath = obj.content;
							sendFile();
						} else if (obj.category == 'UPLOAD') {
							if (obj.content == 'SAVE_FAILURE') {
								scope.updataBtn = false;
								scope.isProgressbar = false;scope.isUploadbtn = true;scope.isCancelbtn = false;
								scope.pop('文件上传失败','error');
							} else if (obj.content == 'SAVE_SUCCESS') {
								sendFile();
							}else if (obj.content.indexOf('TRUE') >= 0) {
								scope.pop("文件上传成功");
								scope.isProgressbar = true;scope.isUploadbtn = false;scope.isCancelbtn = false;
								var filePath = obj.content.substr(obj.content.indexOf(',') + 1);
								scope.filePath = filePath;

								item.isReady = true; item.isSuccess = true;item.isUploading = false;
								socket.close();
							}
						} else if (obj.category == 'UPLOAD_CANCEL') {
							if (obj.content == 'CANCEL_SUCCESS') {
								item.progress = 0;
							}
							scope.pop('已取消文件上传','info');
							item.progress = 0;
							scope.updataBtn = false;
							scope.isProgressbar = false;scope.isUploadbtn = true;scope.isCancelbtn = false;
							socket.close();
						}
						scope.$apply(scope.progress);
					};
				};

				scope.ok = function(){
					if(!scope.filePath&&scope.filePath == '') {
						scope.pop('请先上传文件','error');
						return;
					}
					httpLoad.loadData({
						url:'/user/leadIn',
						method:'POST',
						data: {
							path:scope.filePath
						},
						success:function(data){
							if(data.success){
								scope.pop(data.message);
								scope.cancel(0);
								scope.updataBtn = false;
							}else{
								scope.pop(data.message);
								scope.updataBtn = false;
							}
						}
					});
				};

			}
		}
	}]);
})();