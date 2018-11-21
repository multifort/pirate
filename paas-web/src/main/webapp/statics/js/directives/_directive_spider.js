app.directive('ngSpiderCommon',
	['$rootScope', '$timeout', 'httpLoad', function ($rootScope, $timeout, httpLoad) {
		return {
			restrict: 'AE',
			templateUrl: '/statics/tpl/environment/spiderInfo.html',
			scope: {
				spiderData: '=',
				spider: '='
			},
			link: function (scope, element, attrs) {
				scope.userList = [];
				var SimpleTopology = function () {
					this.InitContent(scope.spiderData);
				}
				SimpleTopology.prototype = {
					InitContent: function (spiderData) {
						var that = this;
						
						if (spiderData.id == null) {
							that.relations(spiderData.targetCategory);
							scope.userList.push({relation: '', targetCategory: '', id: ''})
						} else {
							that.relations(spiderData.targetCategory);
							angular.forEach(spiderData.resourceRelations, function (data) {
								var obj = {
									target: data.target,
									relation: data.relation,
									targetName: data.targetName,
									targetCategory: data.targetCategory,
									type: {id: data.target, name: data.targetName}
								};
								scope.userList.push(obj)
								that.category(obj);
								that.loadres(obj);
							})
						}
					},
					//第一个下拉
					relations: function (value) {
						httpLoad.loadData({
							url: '/res/rule/relations',
							data: {
								category: value
							},
							success: function (data) {
								if (data.success) {
									scope.typeDataone = data.data;
									scope.spiderData.targetCategory = value;
								}
							}
						})
					},
					//第二个
					category: function (item) {
						httpLoad.loadData({
							url: '/res/rule/category',
							data: {
								relation: item.relation,
								category: scope.spiderData.targetCategory
							},
							success: function (data) {
								if (data.success) {
									item.typeDatatwo = data.data;
								}
							}
						})
					},
					//第三个
					loadres: function (item) {
						var targetCategory = '';
						if (scope.spiderData.id == null) {
							targetCategory = null;
						} else {
							targetCategory = scope.spiderData.targetCategory;
						}
						httpLoad.loadData({
							url: '/res/res',
							data: {
								category: item.targetCategory,
								type: targetCategory,
								target: scope.spiderData.id
							},
							success: function (data) {
								if (data.success) {
									item.typeDataThree = data.data;
								}
							}
						})
					},
					//添加数据
					Adddata: function () {
						var relation = [];
						angular.forEach(scope.userList, function (data) {
							if (data.relation != "" && data.type != undefined) {//如果没有进行操作选择则将其过滤掉
								var obj = {
									target: angular.fromJson(data.type).id,
									relation: data.relation,
									targetName: angular.fromJson(data.type).name,
									targetCategory: data.targetCategory
								};
								var flag = relation.some(function (item) {//过滤重复添加的连接信息
									return item.target == obj.target && item.relation == obj.relation && item.targetCategory == obj.targetCategory
								});
								if(!flag) relation.push(obj);
							}
						});
						return relation;
					},
					removeGroup: function (key) {
						// 减少新的一项
						scope.userList.splice(key, 1);
					},
					addGroup: function () {
						// 增加新的一项
						scope.userList.push({relation: '', targetCategory: '', id: ''})
					}
				};
				scope.spider = new SimpleTopology()
			}
		};
	}]);