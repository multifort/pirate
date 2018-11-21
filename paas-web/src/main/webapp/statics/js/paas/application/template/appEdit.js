angular.module('app').controller('appEditCtrl', ['$rootScope', '$scope', '$state', 'LANGUAGE', 'httpLoad', '$timeout',
    function ($rootScope, $scope, $state, LANGUAGE, httpLoad, $timeout) {
        $rootScope.moduleTitle = '服务目录 > 应用编排';
        var editObj = ['name', 'remark', 'type'];
        $scope.editType = 1;
        //获取编排模板列表
        $scope.getData = function (page, type) {
            httpLoad.loadData({
                url: '/layout/template/getList',
                method: 'GET',
                noParam: true,
                success: function (data) {
                    if (data.success) {
                        $scope.countData = data.data.rows;
                    }
                }
            });
        };
        $scope.getData(1);
        var url = '/layout/create';
        $scope.addData = {};
        $scope.addData.type = "yaml"
        //$scope.addData.type = 0;

        $scope.goBack = function () {
            $state.go('app.service_catalog.list');
        },
        $scope.mydata = {};
        $scope.ok = function () {
            var param = {};
            if($scope.editType==1){
                editObj.forEach(function (attr, a) {
                    param[attr] = $scope.addData[attr];
                })

                var re = /^[0-9]+.?[0-9]*$/;
                if (re.test($scope.addData.fileName)) {
                    $scope.pop("名称中不包含数字");
                    return false
                }
                param.fileName = $('#getNameFile').val()||"";
                param.fileContent = $scope.mydata.codeMirror.getValue();
                if ((param.fileContent == '')&&($scope.editType==1)) {
                    $scope.pop("脚本内容不能为空", "error");
                    return false
                }
            }else if( $scope.editType==2){
                url ='/layout/template/version/create'
                    param.version=$scope.mydata.version;
                    param.fileContent=$scope.mydata.codeMirrorVersion.getValue();
                param.layoutTemplateId=$scope.mydata.layoutTemplateId;
                param.remark=$scope.mydata.remark;
                if (param.fileContent == '') {
                    $scope.pop("版本内容不能为空", "error");
                    return false
                }
            }
            httpLoad.loadData({
                url: url,
                method: 'POST',
                data: param,
                success: function (data) {
                    if (data.success) {
                        var text = '添加成功';
                        $scope.pop(text);
                        $state.go('paas.application.template');
                    }
                }
            });
        }
    }]);