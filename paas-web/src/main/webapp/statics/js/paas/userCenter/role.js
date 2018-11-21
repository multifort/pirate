(function(){
    "use strict";
    app.controller('roleCtrl', ['$scope', 'httpLoad', '$rootScope','$modal','$state','$timeout','LANGUAGE',
        function($scope, httpLoad, $rootScope, $modal,$state, $timeout,LANGUAGE) {
            $rootScope.link = '/statics/css/user.css';
            $rootScope.moduleTitle = '用户中心 > 角色管理';//定义当前页
            $scope.param = {
                rows: 10
            };
            //获取角色列表
            $scope.getData = function(page){
                $scope.showDetail = false;
                $scope.param.page = page || $scope.param.page;
                var params = {
                        page: $scope.param.page,
                        rows: $scope.param.rows
                    },
                    searchParam = [];
                if($scope.searchByName&&$scope.searchByName!=""){
                    searchParam.push({"param":{"name":$scope.searchByName},"sign":"LK"});
                }
                if($scope.searchByState&&$scope.searchByState!=""){
                    searchParam.push({"param":{"status":$scope.searchByState},"sign":"EQ"});
                }
                params.params = JSON.stringify(searchParam);
                httpLoad.loadData({
                    url:'/role/list',
                    method:'POST',
                    data: params,
                    noParam: true,
                    success:function(data){
                        if(data.success&&data.data.rows){
                            $scope.roleList = data.data.rows;
                            $scope.totalCount = data.data.total;
                            $scope.isImageData = false;
                        }else{
                            $scope.isImageData = true;
                        }
                    }
                });
            };
            $scope.getData(1);

            //返回
            $scope.goBack = function(){
                $scope.isActive = false;
                $timeout(function() {
                    $scope.showDetail = false;
                }, 200);
            };
            //角色授权数据
            //高危命令授权数据
            //授权
            $scope.grant = function(item){
                $scope.permissionList= {id:item.id,parentId:0};
                    var modalInstance = $modal.open({
                        templateUrl : '/statics/tpl/userCenter/role/grant.html',
                        controller : 'grantRoleModalCtrl',
                        resolve : {
                            id: function() {
                                return item.id;
                            },
                            itemData: function() {
                                return item;
                            },
                            permissionList : function(){
                                return $scope.permissionList;
                            }
                        }
                    });
            };
            //新增
            $scope.add = function(){  //打开模态
                var modalInstance = $modal.open({
                    templateUrl : '/statics/tpl/userCenter/role/add.html',
                    controller : 'addroleModalCtrl'// 初始化模态范围
                });
                modalInstance.result.then(function(data){
                    $scope.getData();
                },function(){});
            };
            //编辑
            $scope.update = function(item){  //打开模态
                var modalInstance = $modal.open({
                    templateUrl : '/statics/tpl/userCenter/role/update.html',  //指向上面创建的视图
                    controller : 'updateroleModalCtrl',// 初始化模态范围
                    resolve : {
                        itemData: function() {
                            return item;
                        }
                    }
                });
                modalInstance.result.then(function(data){
                    angular.extend(item,data);
                },function(){});
            };
            //删除
            $scope.remove = function(id){  //打开模态
                var modalInstance = $modal.open({
                    templateUrl : '/statics/tpl/userCenter/role/remove.html',
                    controller : 'removeroleModalCtrl',
                    resolve : {
                        id: function() {
                            return  id;
                        }
                    }
                });
                modalInstance.result.then(function(){
                    $scope.getData();
                    $scope.isCheck = false;
                },function(){});
            };
        }
    ]);
    //授权ctrl
    angular.module('app').controller('grantRoleModalCtrl',['$scope','$modalInstance','itemData','permissionList','LANGUAGE','id','httpLoad',
        function($scope,$modalInstance,itemData,permissionList,LANGUAGE,id,httpLoad){
            //打开默认是角色授权
            $scope.permissionList = permissionList;
            $scope.idData = id;
            $scope.identification = 'role';
            //角色授权
            $scope.getRoleData = function(){
                httpLoad.loadData({
                    url:'/role/auths',
                    method:'POST',
                    data: $scope.permissionList,
                    success:function(data){
                        if(data.success&&data.data){
                            $scope.permissiondata = data.data;
                            $scope.identification = 'role';
                        }
                    }
                });
            };
            $scope.getRoleData();
            //命令授权
            $scope.getCommandData = function(){
                httpLoad.loadData({
                    url:'/role/commands',
                    method:'POST',
                    data: $scope.permissionList,
                    success:function(data){
                        if(data.success&&data.data){
                            $scope.treeData = data.data;
                            $scope.identification = 'command';
                        }
                    }
                });
            };

            $scope.ok = function(){
                var nodes,nodeId='',url,language;
                $scope.commit = function(url,data,language){
                    httpLoad.loadData({
                        url:url,
                        method:'POST',
                        data: data,
                        success:function(data){
                            if(data.success){
                                $scope.pop(language);
                                $modalInstance.close();
                            }
                        }
                    });
                };

                if($scope.identification == 'role'){
                    var nodes1 = $('#myTree1').tree('getChecked');
                    //if(nodes1.length!=0){
                        nodes = nodes1;
                        url = '/role/accredit';
                        for(var i=0;i<nodes.length;i++){
                            nodeId = nodeId +','+ nodes[i].id;
                        }
                        nodeId = nodeId.substr(1);
                        $scope.grantData ={id:itemData.id,auths:nodeId};
                        language = '角色授权权限成功';
                        $scope.commit(url,$scope.grantData,language);
                    //}else{
                    //    $scope.pop('请选择权限','error');
                    //    return;
                    //}
                }
                if($scope.identification == 'command'){
                    var nodes2 = $('#dg').treegrid('getCheckedNodes');
                    //if(nodes2.length!=0){
                        nodes = nodes2;
                        url = '/role/accCommand';
                        for(var i=0;i<nodes.length;i++){
                            nodeId = nodeId +','+ nodes[i].id;
                        }
                        nodeId = nodeId.substr(1);
                        $scope.grantData ={id:itemData.id,commands:nodeId};
                        language = '角色授权命令成功';
                        $scope.commit(url,$scope.grantData,language);
                    //}else{
                    //    $scope.pop('请选择命令','error');
                    //    return;
                    //}
                }
            };
            $scope.cancel = function(){
                $modalInstance.dismiss('cancel');
            }
    }]);
    //新增ctrl
    angular.module('app').controller('addroleModalCtrl',['$scope','$modalInstance','LANGUAGE','httpLoad',
        function($scope,$modalInstance,LANGUAGE,httpLoad){
            $scope.ok = function(){
                httpLoad.loadData({
                    url:'/role/create',
                    method:'POST',
                    data: $scope.addData,
                    success:function(data){
                        if(data.success){
                            $scope.pop(LANGUAGE.OPERATION.ROLE.ADD_SUCCESS);
                            $modalInstance.close($scope.addData);
                        }
                    }
                });
            };
            $scope.cancel = function(){
                $modalInstance.dismiss('cancel'); // 退出
            };
        }]);
    //编辑ctrl
    angular.module('app').controller('updateroleModalCtrl',['$scope','$modalInstance','itemData','LANGUAGE','httpLoad',
        function($scope,$modalInstance,itemData,LANGUAGE,httpLoad){
            $scope.updateData = {};
            $scope.updateData.name = itemData.name;
            $scope.updateData.remark = itemData.remark;
            $scope.ok = function(){
                $scope.updateData.id = itemData.id;
                httpLoad.loadData({
                    url:'/role/modify',
                    method:'POST',
                    data: $scope.updateData,
                    success:function(data){
                        if(data.success){
                            $scope.pop(LANGUAGE.OPERATION.ROLE.EDIT_SUCCESS);
                            $modalInstance.close($scope.updateData);
                        }
                    }
                });
            };
            $scope.cancel = function(){
                $modalInstance.dismiss('cancel'); // 退出
            };
        }]);
    //删除ctrl
    angular.module('app').controller('removeroleModalCtrl',['$scope','$modalInstance','id','LANGUAGE','httpLoad',
        function($scope,$modalInstance,id,LANGUAGE,httpLoad){ //依赖于modalInstance
            $scope.ok = function(){
                httpLoad.loadData({
                    url:'/role/remove',
                    method:'POST',
                    data:{id: id},
                    success:function(data){
                        if(data.success){
                            $scope.pop(LANGUAGE.OPERATION.ROLE.DEL_SUCCESS);
                            $modalInstance.close();
                        }
                    }
                });
            };
            $scope.cancel = function(){
                $modalInstance.dismiss('cancel'); // 退出
            }
        }]);

    angular.module('app').directive('easyGrid1', ['$rootScope', '$timeout', 'httpLoad',
        function ($rootScope,$timeout,httpLoad) {
            return {
                restrict: 'AEC',
                transclude: true,
                scope : {
                    treeData        : '=',
                    idData        :'=',
                    getData    : '&'
                },
                link: function (scope, element, attrs) {
                    var id = scope.idData;
                    scope.$watch('treeData',function(newValue,oldValue){
                        element.treegrid({
                            data: scope.treeData,
                            dataType:'json',
                            idField:'id',
                            treeField:'name',
                            animate: true,
                            fitColumns:true,
                            checkbox: true,
                            onBeforeExpand: function(row,param){
                                if(row&&row.children1){
                                    httpLoad.loadData({
                                        url: '/role/commands',
                                        method: 'POST',
                                        data: {"parentId":row.id,"id":id},
                                        noParam:true,
                                        success: function(data){
                                            if(data.success){
                                                if (row.children1){
                                                    $('#dg').treegrid('append',{
                                                        parent: row.id,
                                                        data: data.data
                                                    });
                                                    row.children1 = undefined;
                                                    $('#dg').treegrid('collapse', row.id);
                                                    $('#dg').treegrid('expand', row.id);
                                                }
                                                return row.children1 == undefined;
                                            }
                                        },
                                        error: function (XMLHttpRequest, textStatus, errorThrown) {}
                                    });
                                }
                                //return true;
                            },
                            columns:[[
                                {field:'name',title:'名称',width:'40%',align:'left'},
                                {field:'level',title:'级别',width:'20%',align:'center'},
                                {field:'content',title:'内容',width:'40%',align:'center'}
                            ]],
                            onLoadSuccess: function(row, data){

                            },
                            loadFilter: function(data,parentId){
                                function setData(){
                                    var todo = [];
                                    for(var i=0; i<data.length; i++){
                                        if(data[i].level=="DEADLY") data[i].level = "致命";
                                        if(data[i].level=="HIGH") data[i].level = "严重";
                                        if(data[i].level=="SERIOUS") data[i].level = "高危";
                                        if(data[i].level=="MEDIUM") data[i].level = "中等";
                                        if(data[i].level=="COMMON") data[i].level = "普通";
                                        todo.push(data[i]);
                                    }
                                    while(todo.length){
                                        var node = todo.shift();
                                        if (node.children){
                                            node.state = 'closed';
                                            node.children1 = node.children;
                                            node.children = undefined;
                                            todo = todo.concat(node.children1);
                                        }
                                    }
                                }
                                setData(data);
                                return data;
                            }
                        });
                    });
                }
            };
        }]);

})();

