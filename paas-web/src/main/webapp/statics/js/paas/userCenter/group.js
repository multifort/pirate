(function(){
    app.controller('groupTabCtrl', ['$scope', 'httpLoad', '$rootScope','$modal','$state','$timeout',
        function($scope, httpLoad, $rootScope, $modal,$state, $timeout) {
            $rootScope.moduleTitle = '系统管理 > 分组管理';//定义当前页
            $rootScope.link = '/statics/css/user.css';
            if(sessionStorage.getItem('grouptabLocation')!=null){
                var grouptabLocation = JSON.parse(sessionStorage.getItem('grouptabLocation'));
                if(!grouptabLocation){
                    $scope.active1 = true;
                } else{
                    if(grouptabLocation.indexOf("user")>=0) $scope.active1 = true;
                    else $scope.active2 = true;
                }
            }else{
                $scope.active1 = true;$scope.active2 = false;
            }
        }
    ]);
    app.controller('userGroupCtrl', ['$scope', 'httpLoad', '$rootScope','$modal','$state','$timeout',
        function($scope, httpLoad, $rootScope, $modal,$state, $timeout) {
            //$rootScope.moduleTitle = '系统管理 > 分组管理';
            //$rootScope.link = '/statics/css/user.css';//引入页面样式
            $scope.param = {
                rows: 10
            };

            //获取列表
            $scope.getData = function(page){
                $scope.param.page = page || $scope.param.page;
                var params = {
                        page: $scope.param.page,
                        rows: $scope.param.rows
                    },
                    searchParam = [];
                if($scope.searchByUsername&&$scope.searchByUsername!=""){
                    searchParam.push({"param":{"name":$scope.searchByUsername},"sign":"LK"});
                }
                params.params = JSON.stringify(searchParam);
                httpLoad.loadData({
                    url:'/user/group/list',
                    method: 'POST',
                    data: params,
                    noParam: true,
                    success:function(data){
                        if(data.success&&data.data.rows){
                            $scope.userGroupList = data.data.rows;
                            $scope.totalCount = data.data.total;
                            if(data.data.rows.length==0) {
                                $scope.isImageData = true;
                            } else $scope.isImageData = false;
                        }else{
                            $scope.isImageData = true;
                        }
                    }
                });
            };
            if(sessionStorage.getItem('grouptabLocation')!=null){
                var grouptabLocation = JSON.parse(sessionStorage.getItem('grouptabLocation'));
                if(grouptabLocation.indexOf("user")>=0) $scope.getData(1);
            }else{
                $scope.getData(1);
            }

            //跳转详情页
            $scope.detail = function(id){
                $state.go('paas.userCenter.userGroupDetail',{id:id});
            };
            //新增
            $scope.add = function($event,size){
                var modalInstance = $modal.open({
                    templateUrl : '/statics/tpl/userCenter/group/userGroup/addUserGroup.html',
                    controller : 'addUserGroupModalCtrl',
                    size : size
                });
                modalInstance.result.then(function(){
                    $scope.getData();
                },function(){});
            };
            //编辑
            $scope.update = function($event,$index,row,key,size){
                $event.stopPropagation();
                var modalInstance = $modal.open({
                    templateUrl : '/statics/tpl/userCenter/group/userGroup/updateUserGroup.html',
                    controller : 'updateUserGroupModalCtrl',
                    size : size,
                    resolve : {
                        id : function(){
                            return row.id;
                        },
                        updateData : function(){
                            return $scope.userGroupList[$index];
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
                var action = 1;
                $scope.removeData= {"id" : id};
                var modalInstance = $modal.open({
                    templateUrl : '/statics/tpl/userCenter/group/remove.html',
                    controller : 'removeUserGroupModalCtrl',
                    resolve : {
                        action : function(){
                            return action;
                        },
                        removeData : function(){
                            return $scope.removeData;
                        }
                    }
                });
                modalInstance.result.then(function(){
                    $scope.getData();
                },function(){});
            };
            //添加用户
            $scope.addUser = function($event,$index,id,size){
                $event.stopPropagation();
                var modalInstance = $modal.open({
                    templateUrl : '/statics/tpl/userCenter/group/userGroup/addUser.html',  //指向上面创建的视图
                    controller : 'addUseruserGroupModalCtrl',// 初始化模态范围
                    size : size,
                    resolve : {
                        id : function(){
                            return id;
                        }
                    }
                });
            };
            //资源分组
            $scope.addResGroup = function($event,$index,id,size){
                $event.stopPropagation();
                var modalInstance = $modal.open({
                    templateUrl : '/statics/tpl/userCenter/group/userGroup/addResGroup.html',  //指向上面创建的视图
                    controller : 'addUserresGroupModalCtrl',// 初始化模态范围
                    size : size,
                    resolve : {
                        id : function(){
                            return id;
                        }
                    }
                });
            };
        }
    ]);
    //新增ctrl
    angular.module('app').controller('addUserGroupModalCtrl',['$scope','$modalInstance','httpLoad',
        function($scope,$modalInstance,httpLoad){ //依赖于modalInstance
            $scope.addData={};

            $scope.ok = function(){
                httpLoad.loadData({
                    url:'/user/group/create',
                    method:'POST',
                    data: $scope.addData,
                    success:function(data){
                        if(data.success){
                            $scope.pop('用户分组新增成功');
                            $modalInstance.close();
                        }
                    }
                });
            };
            $scope.cancel = function(){
                $modalInstance.dismiss('cancel'); // 退出
            }
        }]);
    //编辑ctrl
    angular.module('app').controller('updateUserGroupModalCtrl',['$scope','$modalInstance','httpLoad','updateData',
        function($scope,$modalInstance,httpLoad,updateData){ //依赖于modalInstance
            var aa=JSON.stringify(updateData);
            $scope.updateData=JSON.parse(aa);

            $scope.ok = function(){
                httpLoad.loadData({
                    url:'/user/group/modify',
                    method:'POST',
                    data: $scope.updateData,
                    success:function(data){
                        if(data.success){
                            $scope.pop('用户分组编辑成功');
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
    angular.module('app').controller('removeUserGroupModalCtrl',['$scope','$modalInstance','httpLoad','removeData','action',
        function($scope,$modalInstance,httpLoad,removeData,action){ //依赖于modalInstance
            var text,url;
            if(action==1){
                $scope.content = '是否删除用户分组？';
                text = '用户分组删除成功';
                url = '/user/group/remove';
            }else if(action==2){
                $scope.content = '是否删除资源分组？';
                text = '资源分组删除成功';
                url = '/res/group/remove';
            }
            $scope.ok = function(){
                httpLoad.loadData({
                    url:url,
                    method:'POST',
                    data: removeData,
                    success:function(data){
                        if(data.success){
                            $scope.pop(text);
                            $modalInstance.close();
                        }
                    }
                });
            };
            $scope.cancel = function(){
                $modalInstance.dismiss('cancel'); // 退出
            }
        }]);
    //添加用户ctrl
    angular.module('app').controller('addUseruserGroupModalCtrl',['$scope','$modalInstance','httpLoad','id',
        function($scope,$modalInstance,httpLoad,id){ //依赖于modalInstance
            $scope.itemsByPage = 6;//定义每页的条数
            var selectList = [];$scope.isSelectAll = false;

            $scope.getList = function(){
                httpLoad.loadData({
                    url:'/user/group/users',
                    method: 'POST',
                    data:{id:id},
                    success:function(data){
                        if(data.success){
                            $scope.userList = data.data;
                            $scope.total = data.data.length;
                            angular.forEach($scope.userList, function(data,index){
                                if(data.checked) $scope.selectItem(data);
                            });
                        }
                    }
                });
            };
            $scope.getList();
            //对选择的数据进行操作
            var setSelectList = function(data){
                if(data.checked){
                    selectList.push(data);
                }else{
                    for(var j = 0; j < selectList.length; j++){
                        var item = selectList[j];
                        if(item.id == data.id) selectList.splice(j,1)
                    }
                }
            };
            //全选
            $scope.selectAll = function(){
                for(var a in $scope.userList){
                    var item = $scope.userList[a];
                    if($scope.isSelectAll != item.checked){
                        item.checked = $scope.isSelectAll;
                        setSelectList(item);
                    }else item.checked = $scope.isSelectAll;
                }
            };
            //选择一个
            $scope.selectItem = function (row) {
                setSelectList(row);
            };

            $scope.ok = function(){
                var users = '';
                selectList.forEach(function (item) {
                    users += ','+ item.id;
                });
                users = users.substr(1);
                $scope.grantData ={id:id,users:users};
                httpLoad.loadData({
                    url: '/user/group/addUser',
                    method: 'POST',
                    data: $scope.grantData,
                    success:function(data){
                        if(data.success){
                            $scope.pop('添加用户成功');
                            $modalInstance.close();
                        }
                    }
                });
            };
            $scope.cancel = function(){
                $modalInstance.dismiss('cancel'); // 退出
            }
        }]);
    //资源分组ctrl
    angular.module('app').controller('addUserresGroupModalCtrl',['$scope','$modalInstance','httpLoad','id',
        function($scope,$modalInstance,httpLoad,id){ //依赖于modalInstance
            $scope.itemsByPage = 6;//定义每页的条数
            var selectList = [];$scope.isSelectAll = false;

            $scope.getList = function(){
                httpLoad.loadData({
                    url:'/user/group/resGroups',
                    method: 'POST',
                    data:{id:id},
                    success:function(data){
                        if(data.success){
                            $scope.resGroupList = data.data;
                            $scope.total = data.data.length;
                            angular.forEach($scope.resGroupList, function(data,index){
                                if(data.checked) $scope.selectItem(data);
                            });
                        }
                    }
                });
            };
            $scope.getList();
            //对选择的数据进行操作
            var setSelectList = function(data){
                if(data.checked){
                    selectList.push(data);
                }else{
                    for(var j = 0; j < selectList.length; j++){
                        var item = selectList[j];
                        if(item.id == data.id) selectList.splice(j,1)
                    }
                }
            };
            //全选
            $scope.selectAll = function(){
                for(var a in $scope.resGroupList){
                    var item = $scope.resGroupList[a];
                    if($scope.isSelectAll != item.checked){
                        item.checked = $scope.isSelectAll;
                        setSelectList(item);
                    }else item.checked = $scope.isSelectAll;
                }
            };
            //选择一个
            $scope.selectItem = function (row) {
                setSelectList(row);
            };

            $scope.ok = function(){
                var resGroups = '';
                selectList.forEach(function (item) {
                    resGroups += ','+ item.id;
                });
                resGroups = resGroups.substr(1);
                $scope.grantData ={id:id,resGroups:resGroups};
                httpLoad.loadData({
                    url: '/user/group/addResGroup',
                    method: 'POST',
                    data: $scope.grantData,
                    success:function(data){
                        if(data.success){
                            $scope.pop('添加资源分组成功');
                            $modalInstance.close();
                        }
                    }
                });
            };
            $scope.cancel = function(){
                $modalInstance.dismiss('cancel'); // 退出
            }
        }]);
    app.controller('resourceGroupCtrl', ['$scope', 'httpLoad', '$rootScope','$modal','$state','$timeout',
        function($scope, httpLoad, $rootScope, $modal,$state, $timeout) {
            //$rootScope.moduleTitle = '系统管理 > 分组管理';
            $rootScope.link = '/statics/css/user.css';//引入页面样式
            $scope.param = {
                rows: 10
            };
            //获取列表
            $scope.getData = function(page){
                $scope.param.page = page || $scope.param.page;
                var params = {
                        page: $scope.param.page,
                        rows: $scope.param.rows
                    },
                    searchParam = [];
                if($scope.searchByResourcename&&$scope.searchByResourcename!=""){
                    searchParam.push({"param":{"name":$scope.searchByResourcename},"sign":"LK"});
                }
                params.params = JSON.stringify(searchParam);
                httpLoad.loadData({
                    url:'/res/group/list',
                    method: 'POST',
                    data: params,
                    noParam: true,
                    success:function(data){
                        if(data.success&&data.data.rows){
                            $scope.resourceGroupList = data.data.rows;
                            $scope.totalCount = data.data.total;
                            if(data.data.rows.length==0) {
                                $scope.isImageData = true;
                            } else $scope.isImageData = false;
                        }else{
                            $scope.isImageData = true;
                        }
                    }
                });
            };
            if(sessionStorage.getItem('grouptabLocation')!=null){
                var grouptabLocation = JSON.parse(sessionStorage.getItem('grouptabLocation'));
                if(grouptabLocation.indexOf("resource")>=0) $scope.getData(1);
            }

            //跳转详情页
            $scope.detail = function(id){
                $state.go('paas.userCenter.resGroupDetail',{id:id});
            };
            //新增
            $scope.add = function($event,size){
                var modalInstance = $modal.open({
                    templateUrl : '/statics/tpl/userCenter/group/resourceGroup/addResourceGroup.html',
                    controller : 'addResourceGroupModalCtrl',
                    size : size
                });
                modalInstance.result.then(function(){
                    $scope.getData();
                },function(){});
            };
            //编辑
            $scope.update = function($event,$index,row,key,size){
                $event.stopPropagation();
                var modalInstance = $modal.open({
                    templateUrl : '/statics/tpl/userCenter/group/resourceGroup/updateResourceGroup.html',
                    controller : 'updateResourceGroupModalCtrl',
                    size : size,
                    resolve : {
                        id : function(){
                            return row.id;
                        },
                        updateData : function(){
                            return $scope.resourceGroupList[$index];
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
                var action = 2;
                $scope.removeData= {"id" : id};
                var modalInstance = $modal.open({
                    templateUrl : '/statics/tpl/userCenter/group/remove.html',
                    controller : 'removeUserGroupModalCtrl',
                    resolve : {
                        action : function(){
                            return action;
                        },
                        removeData : function(){
                            return $scope.removeData;
                        }
                    }
                });
                modalInstance.result.then(function(){
                    $scope.getData();
                },function(){});
            };
            //添加资源
            $scope.addResource = function($event,$index,id,size){
                $event.stopPropagation();
                var modalInstance = $modal.open({
                    templateUrl : '/statics/tpl/userCenter/group/resourceGroup/addResource.html',  //指向上面创建的视图
                    controller : 'addResGroupresourceModalCtrl',// 初始化模态范围
                    backdrop: 'static',
                    size:'lg',
                    resolve : {
                        id : function(){
                            return id;
                        }
                    }
                });
            };
        }
    ]);
    //新增ctrl
    angular.module('app').controller('addResourceGroupModalCtrl',['$scope','$modalInstance','httpLoad',
        function($scope,$modalInstance,httpLoad){ //依赖于modalInstance
            $scope.addData={};

            $scope.ok = function(){
                httpLoad.loadData({
                    url:'/res/group/create',
                    method:'POST',
                    data: $scope.addData,
                    success:function(data){
                        if(data.success){
                            $scope.pop('资源分组新增成功');
                            $modalInstance.close();
                        }
                    }
                });
            };
            $scope.cancel = function(){
                $modalInstance.dismiss('cancel'); // 退出
            }
        }]);
    //编辑ctrl
    angular.module('app').controller('updateResourceGroupModalCtrl',['$scope','$modalInstance','httpLoad','updateData',
        function($scope,$modalInstance,httpLoad,updateData){ //依赖于modalInstance
            var aa=JSON.stringify(updateData);
            $scope.updateData=JSON.parse(aa);

            $scope.ok = function(){
                httpLoad.loadData({
                    url:'/res/group/modify',
                    method:'POST',
                    data: $scope.updateData,
                    success:function(data){
                        if(data.success){
                            $scope.pop('资源分组编辑成功');
                            $modalInstance.close();
                        }
                    }
                });
            };
            $scope.cancel = function(){
                $modalInstance.dismiss('cancel'); // 退出
            }
        }]);
    //添加资源ctrl
    angular.module('app').controller('addResGroupresourceModalCtrl',['$scope','$modalInstance','httpLoad','id',
        function($scope,$modalInstance,httpLoad,id){ //依赖于modalInstance
            $scope.param = {
                page: 1,
                rows: 6,
                target: id,
                category: "GROUP"
            };
            var selectList = [];
            $scope.getList = function(page){
                $scope.param.page = page || $scope.param.page;
                $scope.isSelectAll = false;
                //获取已选主机的IPlist
                var ipList = [];
                for(var a in selectList){
                    ipList.push(selectList[a].ip);
                }

                var searchParam = [];
                if($scope.name&&$scope.name!=""){
                    searchParam.push({"param":{"name":$scope.name},"sign":"LK"});
                }
                $scope.param.params = JSON.stringify(searchParam);
                httpLoad.loadData({
                    url:'/res/list',
                    method:'POST',
                    data:$scope.param,
                    noParam: true,
                    success:function(data){
                        if(data.success){
                            $scope.listData = data.data.rows;
                            //数据反显
                            for(var i = 0; i < $scope.listData.length; i++){
                                var item = $scope.listData[i];
                                var flag = ipList.indexOf(item.ip);
                                if(flag > -1) $scope.listData[i] = selectList[flag];
                            }
                            $scope.totalPage = data.data.total;
                        }
                    }
                });
            };
            $scope.getList();
            //对选择的数据进行操作
            var setSelectList = function(data){
                if(data.isSelected){
                    selectList.push(data);
                }else{
                    for(var j = 0; j < selectList.length; j++){
                        var item = selectList[j];
                        if(item.ip == data.ip) selectList.splice(j,1)
                    }
                }
            };
            //全选
            $scope.selectAll = function(){
                for(var a in $scope.listData){
                    var item = $scope.listData[a];
                    if($scope.isSelectAll != item.isSelected){
                        item.isSelected = $scope.isSelectAll;
                        setSelectList(item);
                    }else item.isSelected = $scope.isSelectAll;
                }
            };
            //选择一个
            $scope.selectItem = function ($event,row) {
                $event.stopPropagation();
                setSelectList(row);
            };
            $scope.ok = function () {
                var res = '';
                selectList.forEach(function (item) {
                    res+= ','+ item.id;
                });
                res = res.substr(1);
                httpLoad.loadData({
                    url:'/res/group/addRes',
                    data:{
                        id:id,
                        res:res
                    },
                    success: function(data){
                        if(data.success){
                            $scope.pop("添加资源成功");
                            $modalInstance.close(data);
                        }
                    }
                });
            };
            $scope.cancel = function () {
                $modalInstance.dismiss('cancel');
            };
        }]);
})();