(function () {
    "use strict";
    app.controller('instancetempTempCtrl', ['$scope', 'httpLoad', '$rootScope', '$modal', '$state', '$stateParams', '$timeout', '$location', '$anchorScroll',
        function ($scope, httpLoad, $rootScope, $modal, $state, $stateParams, $timeout, $location, $anchorScroll) {
            $rootScope.moduleTitle = '我的应用 > 模板 > 实例模板化(模板文件方式)';
            $scope.param = {
                id: $state.params.id
            };
            //返回版本页面
            $scope.goBacks = function () {
                $state.go('paas.application.templateversion');
            };
            //设置初始文件实例值
            let fileContent = $stateParams.file;
            $timeout(function () {
                $scope.codeMirror.setValue(fileContent);
            }, 100);

            //初始化页面
            $scope.isImageData = true;
            $scope.description = "";
            $scope.displayName = "";
            $scope.name = "";
            $scope.value = "";
            $scope.select = "";
            $scope.type = "";
            $scope.tableData = [];

            //代码放大
            $scope.codeExpand = function () {
                let x = parseInt($('.CodeMirror-code').css('fontSize').split('px').join(''));
                if (x / 7 < 4) {
                    x += 7;
                    $('.CodeMirror-code').css('fontSize', x+'px');
                    let file = $scope.codeMirror.getValue();
                    $scope.codeMirror.setValue(file);
                }
            }
            //代码缩小
            $scope.codeNarrow = function () {
                let x = parseInt($('.CodeMirror-code').css('fontSize').split('px').join(''));
                if (x / 7 > 2) {
                    x -= 7;
                    $('.CodeMirror-code').css('fontSize', x+'px');
                    let file = $scope.codeMirror.getValue();
                    $scope.codeMirror.setValue(file);
                }
            }
            //添加
            $scope.addRow = function () {
                let hasVal = false;
                let item = {};
                let arr = ['description', 'displayName', 'name', 'value', 'select', 'type'];
                for (let i = 0; i < arr.length; i++) {
                    item[arr[i]] = $scope[arr[i]];
                    if ($scope[arr[i]] !== '') hasVal = true;
                }
                if (hasVal) {
                    $scope.tableData.push(item);
                    $scope.isImageData = false;
                }
                $scope.description = "";
                $scope.displayName = "";
                $scope.name = "";
                $scope.value = "";
                $scope.select = "";
                $scope.type = "";
            };
            //生成描述文件内容
            let fileDescContent = { "parameters": [] };
            $scope.ok = function () {
                for (let i = 0; i < $scope.tableData.length; i++) {
                    fileDescContent.parameters.push($scope.tableData[i]);
                }
                fileDescContent = JSON.stringify(fileDescContent);
                fileDescContent = fileDescContent.split('{"parameters":').join('{"parameters":\n  ');
                fileDescContent = fileDescContent.split('[').join('[\n    ');
                fileDescContent = fileDescContent.split(' {').join(' {\n      ');
                fileDescContent = fileDescContent.split('","').join('",\n      "');
                fileDescContent = fileDescContent.split('},{').join('\n    },\n    {');
                fileDescContent = fileDescContent.split('}]}').join('\n    }\n]}');
                let fileTemplateContent = $scope.codeMirror.getValue();
                //跳转详情页面准备生成版本
                $state.go("paas.application.instancetemplatetempdetail", { filetemp: fileTemplateContent, filedesc: fileDescContent })
            }
        }
    ]);
})();
