/**
 * Created by Zhang Haijun on 2016/10/28.
 */
//选择服务器ctrl
app.controller('selectServerModalCtrl', ['$scope', 'httpLoad', '$modalInstance','selectList',
	function($scope, httpLoad, $modalInstance, selectList) {
		$scope.param = {
			page: 1,
			rows: 10
		}
		var selectList = angular.fromJson(selectList);
		$scope.getList = function(page){
			$scope.param.page = page || $scope.param.page;
			$scope.isSelectAll = false;
			//获取已选主机的IPlist
			var ipList = [];
			for(var a in selectList){
				ipList.push(selectList[a].ip);
			}
			httpLoad.loadData({
				url:'/target/server/list',
				method:'POST',
				noParam: true,
				data:$scope.param,
				success:function(data){
					if(data.success){
						$scope.listData = data.data.rows;
						//数据反显
						for(var i = 0; i < $scope.listData.length; i++){
							var item = $scope.listData[i];
							item.locked = false;
							var mark = ipList.indexOf(item.ip);
							if(mark > -1) {
								item.isSelected = true;
								item.locked = selectList[mark].locked;
							}
						};
						$scope.totalPage = data.data.total;
					}
				}
			});
		}
		$scope.getList();
		//对选择的数据进行操作
		$scope.setSelectList = function(data){
			if(data.isSelected){
				selectList.push(data);
			}else{
				data.locked = false;
				for(var j = 0; j < selectList.length; j++){
					var item = selectList[j];
					if(item.ip == data.ip) selectList.splice(j,1)
				}
			}
		}
		//全选
		$scope.selectAll = function(){
			$scope.listData.forEach(function (item) {
				if(!item.ip) return;//无ip主机无法操作
				if($scope.isSelectAll != item.isSelected){
					item.isSelected = $scope.isSelectAll;
					$scope.setSelectList(item);
				}else item.isSelected = $scope.isSelectAll;
			});
		};
		$scope.selectLock = function (row) {
			if(!row.ip) return;//无ip主机无法操作
			if(!row.isSelected) {
				row.isSelected = true;
				$scope.setSelectList(row);
			};
			for(var j = 0; j < selectList.length; j++){
				var item = selectList[j];
				if(item.ip == row.ip) item.locked = row.locked;
			}
		};
		//对参数进行处理，去除空参数
		var toObjFormat = function(obj) {
			for (var a in obj) {
				if (obj[a] == "") delete obj[a];
			}
			return obj;
		};
		//搜索
		$scope.search = function(){
			var params = [];
			var param1 = toObjFormat({
				name:$scope.name,
				ip:$scope.ip,
				platform:$scope.platform
			});
			if (angular.toJson(param1).length > 2) params.push({param: param1, sign: 'LK'});
			$scope.param = {
				page: 1,
				rows: 10,
				params: angular.toJson(params)
			}
			$scope.getList(1)
		}
		$scope.ok = function () {
			$modalInstance.close(selectList);
		}
		$scope.cancle = function () {
			$modalInstance.dismiss('cancel');
		};
	}
])