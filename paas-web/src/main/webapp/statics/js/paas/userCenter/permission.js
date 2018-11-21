(function(){
    "use strict";
    app.controller('permissionCtrl', ['$scope', 'httpLoad', '$rootScope','$modal','$state','$timeout','LANGUAGE',
        function($scope, httpLoad, $rootScope, $modal,$state, $timeout,LANGUAGE) {
            $rootScope.moduleTitle = '用户中心 > 权限管理';//定义当前页
            $rootScope.link = '/statics/css/user.css';//引入页面样式
            $scope.param = {
                rows: 10
            };
            $scope.isbatchDelete = true;
            $scope.categoryData = [{"value":"menu","name":"菜单"},{"value":"api","name":"接口"}];
            //获取权限列表
            $scope.getData = function(param){
                $scope.showDetail = false;
                param = param||{'parentId':0};
                httpLoad.loadData({
                    url: '/auth/list',
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
                    templateUrl: '/statics/tpl/userCenter/permission/add.html',
                    controller: 'addPermissionModalCtrl',
                    backdrop: 'static',
                    resolve: {
                        flag: function(){
                            return flag;
                        },
                        itemData: function() {
                            return item;
                        },
                        categoryData: function(){
                            return $scope.categoryData;
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
                    templateUrl: '/statics/tpl/userCenter/permission/remove.html',
                    controller: 'delPermissionModalCtrl',
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
                    url:'/auth/detail',
                    method:'GET',
                    data: {id: data.id},
                    success:function(data){
                        if(data.success&&data.data){
                            $scope.permissionDetail = data.data;
                            $scope.showDetail = $scope.isActive = true;
                        }
                    }
                });
            };
        }
    ]);
    //添加/编辑ctrl
    angular.module('app').controller('addPermissionModalCtrl', ['$scope', '$modalInstance', 'httpLoad', 'flag', 'itemData','categoryData',
        function($scope, $modalInstance,  httpLoad, flag, itemData,categoryData) {
            $scope.isShowMenu = false;   //编辑的时候默认显示一级菜单，parentId是0的话不显示
            //点击图表上面的新增时，需要多一个图标的下拉
            if(flag==4){
                $scope.flag = flag;
                //获取图标数据
                httpLoad.loadData({
                    method: 'POST',
                    url: '/auth/list/icon',
                    //data: param,
                    noParam:true,
                    success: function(data){
                        if(data.success){
                            $scope.iconData = data.data;
                        }
                    }
                });
            }
            if((flag == 2)&&(itemData.parentId!=0)){
                $scope.isShowMenu = true;
                $scope.changeparentId = itemData.parentId;
            }
            //获取一级菜单数据
            httpLoad.loadData({
                method: 'POST',
                url: '/auth/listParents',
                //data: param,
                noParam:true,
                success: function(data){
                    if(data.success){
                        $scope.menuData = data.data;
                    }
                }
            });
            //编辑对象
            var editObj = ['id','name','priority','remark','props','actionUrl','category'];
            $scope.modalName = '添加权限';
            $scope.categoryData = categoryData;
            var url = '/auth/create';
            //如果为编辑，进行赋值
            if(flag == 2){
                url = '/auth/modify';
                $scope.modalName = '编辑权限';
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

                if((flag == 2)&&(itemData.parentId!=0)) param.parentId = $scope.changeparentId;  //编辑的时候,parentId是0的话,可以修改一级菜单
                if((flag == 2)&&(itemData.parentId==0)) param.parentId = itemData.parentId;

                if(flag==4){
                    param.icon = $scope.icon;
                }
                httpLoad.loadData({
                    method: 'POST',
                    url: url,
                    data: param,
                    success: function(data){
                        if(data.success){
                           console.log(param);
                            var popText = '权限添加成功';
                            if(flag == 2) popText = '权限修改成功';
                            $scope.pop(popText);
                            $modalInstance.close();
                        }
                    }
                });
            }
            $scope.cancel = function() {
                $modalInstance.dismiss('cancel');
            };
        }
    ])
    //删除机房ctrl
    angular.module('app').controller('delPermissionModalCtrl', ['$scope', '$modalInstance', 'httpLoad', 'id',
        function($scope, $modalInstance, httpLoad, id) {
            $scope.ok = function(){
                httpLoad.loadData({
                    url: '/auth/remove',
                    data: {id: id},
                    success: function(data){
                        if(data.success){
                            $scope.pop("权限删除成功");
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
})();