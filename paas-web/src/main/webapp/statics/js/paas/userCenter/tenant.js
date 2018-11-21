(function(){
    "use strict";
    app.controller('tenantCtrl', ['$scope', 'httpLoad', '$rootScope','$modal','$state','$timeout','LANGUAGE',
        function($scope, httpLoad, $rootScope, $modal,$state, $timeout,LANGUAGE) {
            $rootScope.moduleTitle = '租户管理 > 租户列表';//定义当前页
            $rootScope.link = '/statics/css/user.css';//引入页面样式
            $scope.param = {
                rows: 10
            };
            $scope.isbatchDelete = true;
            //获取租户列表
            $scope.getData = function(page){
                $scope.param.page = page || $scope.param.page;
                var params = {
                        page: $scope.param.page,
                        rows: $scope.param.rows
                    },
                    searchParam = [];
                if($scope.searchByName&&$scope.searchByName!=""){
                    searchParam.push({"param":{"name":$scope.searchByName},"sign":"LK"});
                }
                if($scope.searchByTenantPhone&&$scope.searchByTenantPhone!=""){
                    if(searchParam.length==0) searchParam.push({"param":{"tenantPhone":$scope.searchByTenantPhone},"sign":"LK"});
                    else {
                        var a = 0;
                        for(var i=0;i<searchParam.length;i++){
                            if(searchParam[i].sign=="LK") {
                                a = 1;
                                searchParam[i].param.tenantPhone = $scope.searchByTenantPhone;
                            }
                        }
                        if(a==0) searchParam.push({"param":{"tenantPhone":$scope.searchByTenantPhone},"sign":"LK"});
                    }
                }
                if($scope.searchByCompany&&$scope.searchByCompany!=""){
                    if(searchParam.length==0) searchParam.push({"param":{"company":$scope.searchByCompany},"sign":"LK"});
                    else {
                        var a = 0;
                        for(var i=0;i<searchParam.length;i++){
                            if(searchParam[i].sign=="LK") {
                                a = 1;
                                searchParam[i].param.company = $scope.searchByCompany;
                            }
                        }
                        if(a==0) searchParam.push({"param":{"company":$scope.searchByCompany},"sign":"LK"});
                    }
                }
                if($scope.searchByState&&$scope.searchByState!=""){
                    searchParam.push({"param":{"status":$scope.searchByState},"sign":"EQ"});
                }
                params.params = JSON.stringify(searchParam);
                httpLoad.loadData({
                    url:'/tenant/list',
                    method:'POST',
                    data: params,
                    noParam: true,
                    success:function(data){
                        if(data.success&&data.data.rows){
                            $scope.tenantList = data.data.rows;
                            $scope.totalCount = data.data.total;
                            if(data.data.rows.length==0) {
                                $scope.isImageData = true;
                                return;
                            } else $scope.isImageData = false;
                            angular.forEach($scope.tenantList, function(data,index){
                                if(data.isLocked){
                                    data.isfreeze=false;
                                    data.isthaw=true;
                                }else{
                                    data.isfreeze=true;
                                    data.isthaw=false;
                                }
                            });
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
            //跳转详情页
            $scope.detail = function(id){
                $state.go('app.tenant.tenantDetail',{id:id});
            };
            //资源绑定
            $scope.bindHost = function(id,vendorId){
                $state.go('app.tenant.tenantResource',{id:id,vendorId:vendorId})
            };
            //重置密码
            $scope.reset = function($event,$index,id,key){
                $event.stopPropagation();
                httpLoad.loadData({
                    url:'/tenant/reset',
                    method:'POST',
                    data:{id: id},
                    success:function(data){
                        if(data.success){
                            $scope.pop(LANGUAGE.OPERATION.TENANT.RESET);
                        }
                    }
                });
            };
            //新增
            $scope.add = function(size){  //打开模态
                var permissionList= {"parentId":0};
                httpLoad.loadData({
                    url:'/auth/list',
                    method:'POST',
                    data: permissionList,
                    noParam:true,
                    success:function(data){
                        if(data.success&&data.data){
                            var permissiondata = data.data;
                            $scope.identification = 'createTenant';
                            var modalInstance = $modal.open({
                                templateUrl : '/statics/tpl/userCenter/tenant/add.html',
                                controller : 'addtenantModalCtrl',// 初始化模态范围
                                size : size, //大小配置
                                resolve : {
                                    permissionList : function(){
                                        return permissionList;
                                    },
                                    permissiondata : function(){
                                        return permissiondata;
                                    },
                                    identification: function(){
                                        return $scope.identification;
                                    }
                                }
                            });
                            modalInstance.result.then(function(){
                                $scope.getData();
                            },function(){});
                        }
                    }
                });
            };
            //编辑
            $scope.update = function(item){  //打开模态
                var modalInstance = $modal.open({
                    templateUrl : '/statics/tpl/userCenter/tenant/update.html',  //指向上面创建的视图
                    controller : 'updatetenantModalCtrl',// 初始化模态范围
                    resolve : {
                        itemData: function() {
                            return item;
                        }
                    }
                });
                modalInstance.result.then(function(){
                    $scope.getData();
                },function(){});
            };
            //删除
            $scope.remove = function(id){  //打开模态
                var modalInstance = $modal.open({
                    templateUrl : '/statics/tpl/userCenter/tenant/remove.html',
                    controller : 'removeTenantModalCtrl',
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
            //冻结
            $scope.freeze = function($event,id){
                $event.stopPropagation();
                $scope.freezeData= {"id" : id};
                var modalInstance = $modal.open({
                    templateUrl : '/statics/tpl/userCenter/tenant/freeze.html',
                    controller : 'freezeTenantModalCtrl',
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
                    templateUrl : '/statics/tpl/userCenter/tenant/thaw.html',
                    controller : 'thawTenantModalCtrl',
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
                    templateUrl : '/statics/tpl/userCenter/tenant/size.html',  //指向上面创建的视图
                    controller : 'sizetenantModalCtrl',// 初始化模态范围
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
            //用户（展示租户下面的用户列表）
            $scope.user = function($event,id){
                $event.stopPropagation();
                $state.go('app.tenant.tenantUser',{id:id});
            };
            //角色（展示租户下面的角色列表）
            $scope.role = function($event,id){
                $event.stopPropagation();
                $state.go('app.tenant.tenantRole',{id:id});
            };
            //授权
            $scope.grant = function(item){
                $scope.permissionList= {id:item.id,parentId:0};
                httpLoad.loadData({
                    url:'/tenant/auths',
                    method:'POST',
                    data: $scope.permissionList,
                    success:function(data){
                        if(data.success&&data.data){
                            //console.log(data.data);
                            $scope.permissiondata = data.data;
                            $scope.identification = 'tenant';
                            var modalInstance = $modal.open({
                                templateUrl : '/statics/tpl/userCenter/tenant/grant.html',  //指向上面创建的视图
                                controller : 'grantTenantModalCtrl',  //初始化模态范围
                                resolve : {
                                    id: function() {
                                        return item.id;
                                    },
                                    itemData: function() {
                                        return item;
                                    },
                                    permissionList : function(){
                                        return $scope.permissionList;
                                    },
                                    permissiondata : function(){
                                        return $scope.permissiondata;
                                    },
                                    identification: function(){
                                        return $scope.identification;
                                    }
                                }
                            });
                        }
                    }
                });
            };
        }
    ]);
    //新增ctrl
    angular.module('app').controller('addtenantModalCtrl',['$scope','$modalInstance','LANGUAGE','httpLoad','permissionList','permissiondata','identification',
        function($scope,$modalInstance,LANGUAGE,httpLoad,permissionList,permissiondata,identification){ //依赖于modalInstance
            $scope.permissionList = permissionList;
            $scope.permissiondata = permissiondata;
            $scope.identification = identification;

            $scope.addData = {};
            //获取云平台的数据
            $scope.getSupplier = function(){
                httpLoad.loadData({
                    url:'/tenant/vendor',
                    method:'POST',
                    data:{id: 0},
                    success:function(data){
                        if(data.success){
                            $scope.regionData = data.data;
                            angular.forEach($scope.regionData, function(data,index){
                                //默认选中第一个区域
                                if(index==0){
                                    data.isRegionActive = true;
                                    $scope.addData.vendors = data.id;
                                }
                                else data.isRegionActive = false;
                            });
                        }
                    }
                });
            };
            $scope.getSupplier();
            //选择云平台
            $scope.chooseRegion = function(item,$index){
                $scope.vendors = "";
                $scope.regionData[$index].isRegionActive = !$scope.regionData[$index].isRegionActive;
                angular.forEach($scope.regionData, function(data,index){
                    if(data.isRegionActive){
                        $scope.vendors = $scope.vendors + ',' +data.id;
                    }
                });
                $scope.addData.vendors = $scope.vendors.substr(1);
            };

            //check 邮箱
            $scope.checkContactEmail = function(){
                if(!$scope.addData.contactEmail||$scope.addData.contactEmail=="") return;
                httpLoad.loadData({
                    url:'/tenant/checkTenant',
                    method:'POST',
                    data: {"email":$scope.addData.contactEmail},
                    success:function(data){
                        if(data.success){
                            $scope.addTenantForm.$invalid = false;
                        }else{
                            $scope.addTenantForm.$invalid = true;
                            $scope.addData.contactEmail = "";
                        }
                    }
                });
            };

            $scope.ok = function(){
                var nodes = $('#myTree').tree('getChecked');
                var nodeId = '';

                for(var i=0;i<nodes.length;i++){
                    nodeId = nodeId + ','+ nodes[i].id;
                }
                nodeId = nodeId.substr(1);
                $scope.addData.auths =nodeId;
                if($scope.addData.auths==''){
                    $scope.pop('请给租户授权权限','error');
                    return;
                }
                if(!$scope.addData.vendors||$scope.addData.vendors==""){
                    $scope.pop('请选择云平台','error');
                    return;
                }
                httpLoad.loadData({
                    url:'/tenant/create',
                    method:'POST',
                    data: $scope.addData,
                    success:function(data){
                        if(data.success){
                            //console.log($scope.addData);
                            $scope.pop(LANGUAGE.OPERATION.TENANT.ADD_SUCCESS);
                            $modalInstance.close();
                        }
                    }
                });
            };
            $scope.cancel = function(){
                $modalInstance.dismiss('cancel'); // 退出
            };
        }]);
    //编辑ctrl
    angular.module('app').controller('updatetenantModalCtrl',['$scope','$modalInstance','itemData','LANGUAGE','httpLoad',
        function($scope,$modalInstance,itemData,LANGUAGE,httpLoad){ //依赖于modalInstance
            $scope.updateData = {};
            $scope.updateData.name = itemData.name;
            $scope.updateData.remark = itemData.remark;
            $scope.updateData.props = itemData.props;
            $scope.updateData.company = itemData.company;
            $scope.updateData.address = itemData.address;
            $scope.updateData.contacter = itemData.contacter;
            $scope.updateData.contactPhone = itemData.contactPhone;
            $scope.updateData.contactEmail = itemData.contactEmail;
            $scope.updateData.tenantPhone = itemData.tenantPhone;
            $scope.updateData.vendors = "";

            //获取云平台的数据
            $scope.getSupplier = function(){
                httpLoad.loadData({
                    url:'/tenant/vendor',
                    method:'POST',
                    data:{id: itemData.id},
                    success:function(data){
                        if(data.success){
                            $scope.regionData = data.data;

                            angular.forEach($scope.regionData, function(data,index){
                                if(data.checked==true){
                                    data.isRegionActive = true;
                                    $scope.updateData.vendors = $scope.updateData.vendors + ',' +data.id;
                                } else data.isRegionActive = false;
                            });
                            $scope.updateData.vendors = $scope.updateData.vendors.substr(1);
                        }
                    }
                });
            };
            $scope.getSupplier();
            //选择云平台
            $scope.chooseRegion = function(item,$index){
                $scope.vendors = "";
                $scope.regionData[$index].isRegionActive = !$scope.regionData[$index].isRegionActive;
                angular.forEach($scope.regionData, function(data,index){
                    if(data.isRegionActive){
                        $scope.vendors = $scope.vendors + ',' +data.id;
                    }
                });
                $scope.updateData.vendors = $scope.vendors.substr(1);
            };

            $scope.ok = function(){
                $scope.updateData.id = itemData.id;

                if($scope.updateData.vendors==""){
                    $scope.pop('请选择云平台','error');
                    return;
                }
                httpLoad.loadData({
                    url:'/tenant/modify',
                    method:'POST',
                    data: $scope.updateData,
                    success:function(data){
                        if(data.success){
                            $scope.pop(LANGUAGE.OPERATION.TENANT.EDIT_SUCCESS);
                            $modalInstance.close();
                        }
                    }
                });
            };
            $scope.cancel = function(){
                $modalInstance.dismiss('cancel'); // 退出
            };
        }]);
    //删除ctrl
    angular.module('app').controller('removeTenantModalCtrl',['$scope','$modalInstance','id','LANGUAGE','httpLoad',
        function($scope,$modalInstance,id,LANGUAGE,httpLoad){ //依赖于modalInstance
            $scope.content = '是否删除租户？';
            $scope.ok = function(){
                httpLoad.loadData({
                    url:'/tenant/remove',
                    method:'POST',
                    data:{id: id},
                    success:function(data){
                        if(data.success){
                            $scope.pop(LANGUAGE.OPERATION.TENANT.DEL_SUCCESS);
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
    angular.module('app').controller('freezeTenantModalCtrl',['$scope','$modalInstance','httpLoad','freezeData','LANGUAGE',
        function($scope,$modalInstance,httpLoad,freezeData,LANGUAGE){ //依赖于modalInstance
            $scope.ok = function(){
                httpLoad.loadData({
                    url:'/tenant/lock',
                    method:'POST',
                    data: freezeData,
                    success:function(data){
                        if(data.success){
                            $scope.pop(LANGUAGE.OPERATION.TENANT.FREEZE_SUCCESS);
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
    angular.module('app').controller('thawTenantModalCtrl',['$scope','$modalInstance','httpLoad','thawData','LANGUAGE',
        function($scope,$modalInstance,httpLoad,thawData,LANGUAGE){ //依赖于modalInstance
            $scope.ok = function(){
                httpLoad.loadData({
                    url:'/tenant/active',
                    method:'POST',
                    data: thawData,
                    success:function(data){
                        if(data.success){
                            $scope.pop(LANGUAGE.OPERATION.TENANT.THAW_SUCCESS);
                            $modalInstance.close();
                        }
                    }
                });
            };
            $scope.cancel = function(){
                $modalInstance.dismiss('cancel'); // 退出
            }
    }]);
    //授权ctrl
    angular.module('app').controller('grantTenantModalCtrl',['$scope','$modalInstance','itemData','permissionList','permissiondata','LANGUAGE','id','httpLoad','identification',
        function($scope,$modalInstance,itemData,permissionList,permissiondata,LANGUAGE,id,httpLoad,identification){ //依赖于modalInstance
            $scope.permissionList = permissionList;
            $scope.permissiondata = permissiondata;
            $scope.idData = id;
            $scope.identification = identification;

            $scope.ok = function(){
                /*var tree = $.fn.zTree.getZTreeObj("_grant_table");
                 var nodeId = '';
                 var items = tree.getCheckedNodes();
                 for (var i in items) if (items.hasOwnProperty(i)) {
                 nodeId = nodeId +','+ items[i].id;
                 }*/
                var nodes = $('#myTree').tree('getChecked');
                var nodeId='';
                for(var i=0;i<nodes.length;i++){
                    nodeId = nodeId + ',' + nodes[i].id;
                }
                nodeId = nodeId.substr(1);
                $scope.grantData ={id:itemData.id,auths:nodeId};
                httpLoad.loadData({
                    url:'/tenant/accredit',
                    method:'POST',
                    data: $scope.grantData,
                    success:function(data){
                        if(data.success){
                            $scope.pop(LANGUAGE.OPERATION.TENANT.GRANT_SUCCESS);
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
    angular.module('app').controller('sizetenantModalCtrl',['$scope','$modalInstance','item','LANGUAGE','httpLoad',
        function($scope,$modalInstance,item,LANGUAGE,httpLoad){ //依赖于modalInstance
            $scope.sizeData = {
                "id":item.id,
                "cpu":item.cpu,
                "memory":item.memory,
                "disk":item.disk,
                "instance":item.instance
            };

            $scope.check = function(action,value,min,max){
                var number = parseInt(value);
                if(action==0) var name = 'CPU'+'范围是'+min+'-'+max+','+'请重新输入';
                if(action==1) var name = '内存'+'范围是'+min+'-'+max+','+'请重新输入';
                if(action==2) var name = '磁盘'+'范围是'+min+'-'+max+','+'请重新输入';
                if(action==3) var name = '实例'+'范围是'+min+'-'+max+','+'请重新输入';
                if(value<min||value>max){
                    $scope.pop(name,'error');
                    return;
                }
            };

            $scope.ok = function(){
                httpLoad.loadData({
                    url:'/tenant/quota',
                    method:'POST',
                    data: $scope.sizeData,
                    success:function(data){
                        if(data.success){
                            $scope.pop(LANGUAGE.OPERATION.TENANT.SIZE_SUCCESS);
                            $modalInstance.close();
                        }
                    }
                });
            };
            $scope.cancel = function(){
                $modalInstance.dismiss('cancel'); // 退出
            };
        }]);
})();