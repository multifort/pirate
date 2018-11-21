
app.controller('AppCreateInTabCtrl', ['$rootScope', '$scope', 'httpLoad', '$modal', '$stateParams', '$state',function ($rootScope, $scope, httpLoad, $modal, $stateParams, $state) {
  $rootScope.moduleTitle = '镜像管理 > 镜像构建';
    $scope.scriptItem = {};
    $scope.addData = {};
    $scope.scriptItem.selected="";
    $scope.scriptItemSpace = {}
	$scope.scriptItemSpace.selected = "";
    $scope.addData.repositoryUsername ="";
    $scope.addData.repositoryPassword = ""
    $scope.goBack = function () {
        $state.go('paas.repository.dockerimage');
    };
    httpLoad.loadData({
        url: '/registry/list',
        method: 'POST',
        data: {},
        noParam: true,
        success: function (data) {
            if (data.success) {
                $scope.warehoseData = data.data.rows;

            }
        }
    });
    $scope.getImage = function (id) {
        httpLoad.loadData({
            url: '/image/getProjects',
            method: 'GET',
            data: {registryId:id},
            noParam: true,
            ignoreError: true,
            success: function (data) {
                if (data.success) {
                    $scope.imageData = data.data;
                    $scope.imageshow = true;

                }else{
                    $scope.scriptItemSpace.selected = "";
                    $scope.imageshow = false;
                }
            }
        });
    }
    $scope.getBeach = function () {
    	return
        if($scope.addData.repositoryUrl){

           var obj =  ( /((([A-Za-z]{3,9}:(?:\/\/)?)(?:[-;:&=\+\$,\w]+@)?[A-Za-z0-9.-]+|(?:www.|[-;:&=\+\$,\w]+@)[A-Za-z0-9.-]+)((?:\/[\+~%\/.\w]*)?\??(?:[-\+=&;%@.\w_]*)#?(?:[\w]*))?)/).test($scope.addData.repositoryUrl)
           if(!obj)return
            httpLoad.loadData({
                url: '/git/branches',
                method: 'GET',
                data: {
                    repositoryUrl:$scope.addData.repositoryUrl,
                    username:$scope.addData.repositoryUsername,
                    password:$scope.addData.repositoryPassword,
                },
                noParam: true,
                ignoreError: true,
                success: function (data) {
                    if(data.success){
                        $scope.repositoryBranch = data.data.rows;
                    }
                }
            });
        }

    }
    $scope.chanle = function (obj) {
        httpLoad.loadData({
            url: '/image/getProjects',
            method: 'GET',
            data: {registryId:id},
            noParam: true,
            ignoreError: true,
            success: function (data) {
                if (data.success) {
                    $scope.imageData = data.data;
                    $scope.imageshow = true;

                }else{
                    $scope.scriptItemSpace.selected = "";
                    $scope.imageshow = false;
                }
            }
        });
    }
    $scope.change = function(x){
        $scope.formShow = x;
        if(x==0){
        	var params = {
        		    simple: true,
        		    params: angular.toJson([{"param":{"repository_image_info.namespace":"s2i"},"sign":"EQ"}])
        		}
            httpLoad.loadData({
                url: '/image/list',
                method: 'POST',
                data: params,
                noParam: true,
                success: function (data) {
                    if (data.success) {
                        $scope.dataType = data.data.rows;
                        $scope.dataType.forEach(function(item){
                        	  var port = (item.repositoryType !=1) ?":"+item.repositoryPort:'';
                        	  if(item.namespace==""){
                        		  item.name  = item.repositoryAddress+port+ '/'+item.name+':'+ item.tag 
                        	  }else{
                        		  item.name  = item.repositoryAddress+port+ '/'+item.namespace+ '/'+item.name+':'+ item.tag
                        	  }                           
                        })                                              
                    }
                }
            });
        }
        if(x==1){

        }
        if(x==2){

        }
    }
    $scope.change(0)
    $scope.ok = function () {
        var params = $scope.addData;
        if((!$scope.addData.repositoryUsername&&$scope.addData.repositoryPassword)||($scope.addData.repositoryUsername&&!$scope.addData.repositoryPassword))
        {$scope.pop("请填写完整的用户名和密码","error");return
        }else if(!$scope.addData.repositoryUsername&&!$scope.addData.repositoryPassword){
            $scope.addData.repositoryUsername ="";
            $scope.addData.repositoryPassword = ""
        }
        params.pomPath="/"

        if($scope.imageshow){
        	if($scope.scriptItemSpace.selected.name){
        		params.project = $scope.scriptItemSpace.selected.name;
        	}else{
        		$scope.pop("请填写项目名称","error");return
        	}
        	
        }else{
        	params.project = ""
        }

        if($scope.scriptItem.selected.name){
            params.baseImage =  $scope.scriptItem.selected.name ;
        }else{$scope.pop("请填写基础镜像","error");return}
        httpLoad.loadData({
            url: '/image/buildBySource',
            method: 'GET',
            data: params,
            noParam: true,
            success: function (data) {
                if (data.success) {
                	 $scope.pop(data.message);
                    $state.go('paas.repository.dockerimage');
                }
            }
        });
    }

}])
