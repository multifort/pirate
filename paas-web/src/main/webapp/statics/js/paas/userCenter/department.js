(function(){
    "use strict";
    app.controller('departmentCtrl', ['$scope', 'httpLoad', '$rootScope','$modal','$state','$timeout',
        function($scope, httpLoad, $rootScope, $modal,$state, $timeout) {
            $rootScope.moduleTitle = '用户中心 > 组织机构';//定义当前页
            $rootScope.link = '/statics/css/user.css';//引入页面样式
            $scope.param = {
                rows: 10
            };
            //$scope.categoryData = [{"value":"menu","name":"菜单"},{"value":"api","name":"接口"}];
            //获取权限列表
            $scope.getData = function(param){
                $scope.showDetail = false;
                param = param||{'parentId':0};
                httpLoad.loadData({
                    url: '/department/list',
                    method: 'POST',
                    data: param,
                    noParam:true,
                    success: function(data){
                        if(data.success){
                            $scope.treeData = data.data;
                            //$('#dg').treegrid('loadData',$scope.treeData);
                            $scope.isLoad = true;
                        }else{
                            $scope.isLoad = false;
                        }
                    }
                });
            };
            $scope.getData();

            //添加/编辑
            $scope.add = function(flag,item,$event){
                if($event) $event.stopPropagation();
                var modalInstance = $modal.open({
                    templateUrl: '/statics/tpl/userCenter/department/add.html',
                    controller: 'addDepartmentModalCtrl',
                    backdrop: 'static',
                    resolve: {
                        flag: function(){
                            return flag;
                        },
                        itemData: function() {
                            return item;
                        }
                    }
                });
                modalInstance.result.then(function(data) {
                    if(flag == 2) {
                        $scope.getData();
                        /*angular.extend(item,data);
                         $('#dg').treegrid('update',{
                         id: item.id,
                         row: {
                         name: item.name,
                         status: item.status,
                         props: item.props,
                         gmtModify: item.gmtModify,
                         remark: item.remark
                         }
                         });*/
                    } else{
                        $scope.getData();
                        //添加节点
                        /*$('#dg').treegrid('append',{
                         parent: data.id,  // the node has a 'id' value that defined through 'idField' property
                         data: [{
                         id: data.id,
                         parent: data.parentId,
                         name: data.name,
                         props: data.props,
                         remark: data.remark
                         }]
                         });*/
                    }
                });
            };
            //删除
            $scope.remove = function (item,$event){
                if($event) $event.stopPropagation();
                var modalInstance = $modal.open({
                    templateUrl: '/statics/tpl/userCenter/department/remove.html',
                    controller: 'delDepartmentModalCtrl',
                    backdrop: 'static',
                    resolve: {
                        id: function() {
                            return  item.id;
                        }
                    }
                });
                modalInstance.result.then(function() {
                    $scope.getData();
                    //$('#dg').treegrid('remove',item.id);
                });
            };
            //配额
            $scope.size = function(item){  //打开模态
                var modalInstance = $modal.open({
                    templateUrl : '/statics/tpl/userCenter/department/size.html',  //指向上面创建的视图
                    controller : 'sizeDepartmentModalCtrl',// 初始化模态范围
                    resolve : {
                        item: function() {
                            return item;
                        }
                    }
                });
                modalInstance.result.then(function(data){
                    $scope.getData();
                },function(){});
            };
            //返回
            $scope.goBack = function(){
                $scope.isActive = false;
                $timeout(function() {
                    $scope.showDetail = false;
                }, 200);
            };
            //跳转详情页
            $scope.detail = function (data) {
                httpLoad.loadData({
                    url:'/department/detail',
                    method:'GET',
                    data: {id: data.id},
                    success:function(data){
                        if(data.success&&data.data){
                            $scope.departmentDetail = data.data;
                            $scope.showDetail = $scope.isActive = true;
                        }
                    }
                });
            };
            //资源绑定
            $scope.bindHost = function(item,$event){
                $state.go('paas.userCenter.departmentResource',{id:item.id})
            };
        }
    ]);
    //添加/编辑ctrl
    angular.module('app').controller('addDepartmentModalCtrl', ['$scope', '$modalInstance', 'httpLoad', 'flag', 'itemData',
        function($scope, $modalInstance,  httpLoad, flag, itemData) {
            //编辑对象
            var editObj = ['id','name','remark','props'];
            $scope.modalName = '添加组织机构';
            var url = '/department/create';
            //如果为编辑，进行赋值
            if(flag == 2){
                url = '/department/modify';
                $scope.modalName = '编辑组织机构';
                for(var a in editObj){
                    var attr = editObj[a];
                    $scope[attr] = itemData[attr];
                }
            }
            //保存按钮
            $scope.ok = function(){
                var param = {};
                for(var a in editObj){
                    var attr = editObj[a];
                    param[attr] = $scope[attr];
                }
                if(flag == 2) param.id = itemData.id;
                else param.parentId = itemData.id || 0;
                httpLoad.loadData({
                    method: 'POST',
                    url: url,
                    data: param,
                    success: function(data){
                        if(data.success){
                            console.log(param);
                            var popText = '组织机构添加成功';
                            if(flag == 2) popText = '组织机构修改成功';
                            $scope.pop(popText);
                            $modalInstance.close();
                        }
                    }
                });
            };
            $scope.cancel = function() {
                $modalInstance.dismiss('cancel');
            };
        }
    ]);
    //删除机房ctrl
    angular.module('app').controller('delDepartmentModalCtrl', ['$scope', '$modalInstance', 'httpLoad', 'id',
        function($scope, $modalInstance, httpLoad, id) {
            $scope.content = '是否删除组织机构？';
            $scope.ok = function(){
                httpLoad.loadData({
                    url: '/department/remove',
                    data: {id: id},
                    success: function(data){
                        if(data.success){
                            $scope.pop("组织机构删除成功");
                            $modalInstance.close();
                        }
                    }
                });
            }
            $scope.cancel = function(){
                $modalInstance.dismiss('cancel');
            };
        }
    ]);
    //配额ctrl
    angular.module('app').controller('sizeDepartmentModalCtrl',['$scope','$modalInstance','item','httpLoad',
        function($scope,$modalInstance,item,httpLoad){ //依赖于modalInstance
            $scope.sizeData = {
                "id":item.id,
                "cpu":item.cpu,
                "memory":item.memory,
                "disk":item.disk
            };

            $scope.ok = function(){
                httpLoad.loadData({
                    url:'/department/quota',
                    method:'POST',
                    data: $scope.sizeData,
                    success:function(data){
                        if(data.success){
                            $scope.pop("组织机构配额成功");
                            $modalInstance.close();
                        }
                    }
                });
            };
            $scope.cancel = function(){
                $modalInstance.dismiss('cancel'); // 退出
            };
        }]);

    angular.module('app').directive('easyGrid2', ['$rootScope', '$timeout', 'httpLoad',
        function ($rootScope,$timeout,httpLoad) {
            return {
                restrict: 'AEC',
                transclude: true,
                scope : {
                    treeData        : '=',
                    getData    : '&',
                    add     : '&',
                    remove     : '&',
                    detail     : '&',
                    bind        : '&'
                },
                link: function (scope, element, attrs) {
                    scope.$watch('treeData',function(newValue,oldValue){
                        element.treegrid({
                            //method: 'GET',
                            //url:'/resources/data/operation/permission/permissionList.json',
                            data: scope.treeData,
                            dataType:'json',
                            idField:'id',
                            treeField:'name',
                            animate: true,
                            fitColumns:true,
                            onBeforeExpand: function(row,param){
                                if(row&&row.children1){
                                    //$(this).treegrid('options').url='/resources/data/operation/permission/test.json?parentId='+row.id+'&id='+row.id;
                                    httpLoad.loadData({
                                        url: '/department/list',
                                        method: 'POST',
                                        data: {"parentId":row.id},
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
                                {field:'name',title:'名称',width:'20%',align:'left'},
                                {field:'status',title:'状态',width:'10%',align:'center'},
                                {field:'props',title:'其他属性',hidden:true},
                                {field:'gmtModify',title:'修改时间',width:'20%',align:'center'},
                                {field:'remark',title:'描述',width:'20%',align:'center'},
                                {field:'cpu',title:'cpu',width:'10%',align:'center',hidden:true},
                                {field:'memory',title:'memory',width:'10%',align:'center',hidden:true},
                                {field:'disk',title:'disk',width:'10%',align:'center',hidden:true},
                                {field:'operate',title:'操作',width:'30%',align:'center',
                                    formatter: function(value,row,index){
                                        row.parentId = row.parentId || 0;
                                        var id=row.id,
                                            parentId=row.parentId,
                                            name=row.name,
                                            remark=row.remark||"",
                                            cpu=row.cpu||"",
                                            memory=row.memory||"",
                                            disk=row.disk||"",
                                            props=row.props||"",
                                            gmtModify=row.gmtModify||"";
                                        var html='';
                                        html+='<button class="btn btn-info btn-sm addName" id="'+id+'" parentId="'+parentId+'" name="'+name+'"  remark="'+remark+'" props="'+props+'" ><i class="fa fa-plus icon-font"></i><span class="icon-txt" style="font-size: 14px;padding-left: 3px;">新增</span></button>'+ '&nbsp;&nbsp;';//默认只有二级节点
                                        html+='<button class="btn btn-success btn-sm updateName" id="'+id+'" parentId="'+parentId+'" name="'+name+'"  remark="'+remark+'" props="'+props+'"><i class="fa fa-pencil-square-o icon-font"></i><span class="icon-txt" style="font-size: 14px;padding-left: 3px;">编辑</span></button>'+ '&nbsp;&nbsp;';
                                        html+='<button class="btn btn-danger btn-sm deleteName" id="'+id+'" parentId="'+parentId+'" name="'+name+'"  remark="'+remark+'" props="'+props+'" ><i class="fa fa-trash-o icon-font"></i><span class="icon-txt" style="font-size: 14px;padding-left: 3px;">删除</span></button>'+ '&nbsp;&nbsp;';
                                        //html+='<button class="btn btn-warning btn-sm sizeName" id="'+id+'" parentId="'+parentId+'" name="'+name+'" remark="'+remark+'" cpu="'+cpu+'" memory="'+memory+'" disk="'+disk+'" props="'+props+'"><i class="fa fa-cog icon-font"></i><span class="icon-txt" style="font-size: 14px;padding-left: 3px;">配额</span></button>'+ '&nbsp;&nbsp;';
                                       // html+='<button class="btn btn-success btn-sm bindName" id="'+id+'" parentId="'+parentId+'" name="'+name+'" remark="'+remark+'" props="'+props+'"><i class="fa fa-laptop icon-font"></i><span class="icon-txt" style="font-size: 14px;padding-left: 3px;">资源分配</span></button>'+ '&nbsp;&nbsp;';
                                        html+='<button class="btn btn-primary btn-sm detailName" id="'+id+'" parentId="'+parentId+'" name="'+name+'"  remark="'+remark+'" props="'+props+'" gmtModify="'+gmtModify+'"><i class="fa fa-info icon-font"></i><span class="icon-txt" style="font-size: 14px;padding-left: 3px;">详情</span></button>';

                                        return html;
                                    }
                                }
                            ]],
                            onLoadSuccess: function(row, data){
                                $(".datagrid-body").on('click','button.addName',function($event){
                                    $event.stopPropagation();
                                    var _this =$(this);
                                    var flag= 1;
                                    var item = {
                                        id :  _this.attr("id"),
                                        parentId : _this.attr("parentId"),
                                        name :  _this.attr("name"),
                                        remark :  _this.attr("remark"),
                                        props : _this.attr("props")
                                    };
                                    scope.add({flag:flag,item:item});
                                });
                                $(".datagrid-body").on('click','button.updateName',function($event){
                                    $event.stopPropagation();
                                    var _this =$(this);
                                    var flag= 2;
                                    var item = {
                                        id :  _this.attr("id"),
                                        parentId : _this.attr("parentId"),
                                        name :  _this.attr("name"),
                                        remark :  _this.attr("remark"),
                                        props : _this.attr("props")
                                    };
                                    scope.add({flag:flag,item:item});
                                });
                                $(".datagrid-body").on('click','button.deleteName',function($event){
                                    $event.stopPropagation();
                                    var _this =$(this);
                                    var item = {
                                        id : _this.attr("id")
                                    };
                                    scope.remove({item:item});
                                });
                                $(".datagrid-body").on('click','button.sizeName',function($event){
                                    $event.stopPropagation();
                                    var _this =$(this);
                                    var item = {
                                        id : _this.attr("id"),
                                        cpu : _this.attr("cpu"),
                                        memory : _this.attr("memory"),
                                        disk : _this.attr("disk")
                                    };
                                    scope.size({item:item});
                                });
                                $(".datagrid-body").on('click','button.detailName',function($event){
                                    $event.stopPropagation();
                                    var _this =$(this);
                                    var item = {
                                        id : _this.attr("id")
                                    };
                                    scope.detail({item:item});
                                });
                                $(".datagrid-body").on('click','button.bindName',function($event){
                                    $event.stopPropagation();
                                    var _this =$(this);
                                    var item = {
                                        id : _this.attr("id")
                                    };
                                    scope.bind({item:item});
                                });
                            },
                            loadFilter: function(data,parentId){
                                function setData(){
                                    var todo = [];
                                    for(var i=0; i<data.length; i++){
                                        if(data[i].status=="NORMAL") data[i].status = "正常";
                                        if(data[i].status=="ABNORMAL") data[i].status = "异常";
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