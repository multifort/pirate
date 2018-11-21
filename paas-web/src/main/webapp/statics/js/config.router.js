'use strict';

/**
 * Config for the router
 */
angular.module('app')
    .run(['$rootScope', '$state', '$stateParams', '$location', 'IGNOREPERMISSION',
        function ($rootScope, $state, $stateParams, $location, IGNOREPERMISSION) {
            $rootScope.$state = $state;
            $rootScope.$stateParams = $stateParams;

            //加入权限控制
            $rootScope.$on('$stateChangeStart', function (event, toState, toParams, fromState, fromParams) {
                var currentUrl = toState.name;
                $rootScope.currentUrl = currentUrl;//记录当前页
                if (!fromState.name) return;
                var searchVal = currentUrl.replace(/\./g, '');
                if (IGNOREPERMISSION.IGNORELIST.indexOf(searchVal) < 0) {
                    // $location.path('/access/login');
                    // $state.go('access.login');
                }
            });
        }]
    ).config(['$stateProvider', '$urlRouterProvider', '$locationProvider', 'JQ_CONFIG', 'MODULE_CONFIG',
        function ($stateProvider, $urlRouterProvider, $locationProvider, JQ_CONFIG, MODULE_CONFIG) {
            var layout = "/statics/tpl/app.html";
            $urlRouterProvider
                .otherwise('/access/login');
            $stateProvider
                .state('access', {
                    url: '/access',
                    template: '<div ui-view class="fade-in-right-big smooth"></div>'
                }).state('access.login', {
                url: '/login',
                templateUrl: '/statics/tpl/access/login.html',
                controller: 'LoginCtrl',
                resolve: load(['/statics/js/paas/access/login.js'])
            }).state('access.sso', {
                url: '/sso',
                templateUrl: '/statics/tpl/access/sso.html',
                controller: 'ssoCtrl',
                resolve: load(['/statics/js/paas/access/sso.js'])
            }).state('paas', {
                abstract: true,
                url: '/paas',
                templateUrl: layout,
                resolve: load(['/statics/js/directives/hide_detail.js'])
            }).state('paas.dashboard', {
                url: '/dashboard',
                template: '<div ui-view class="fade-in-up"></div>',
                resolve: load(['dashboard', '/statics/libs/assets/echarts/china.js'])
            }).state('paas.dashboard.dashboard', {
                url: '/dashboard',
                templateUrl: '/statics/tpl/dashboard/dashboard.html',
                resolve: load(['/statics/js/paas/dashboard/dashboard.js', '/statics/js/services/meterService.js', '/statics/css/environment.css'])
            }).state('paas.environment', {
                url: '/environment',
                template: '<div ui-view class="fade-in-up"></div>',
                resolve: load(['assetsLayout', 'smart-table', 'dashboard', '/statics/css/image.css', '/statics/css/environment.css'])
            }).state('paas.environment.environment', {
                url: '/environment',
                templateUrl: '/statics/tpl/environment/environment/list.html',
                resolve: load(['/statics/js/paas/environment/environment/environment.js'])
            }).state('paas.environment.environmentdetail', {
                url: '/environmentdetail/:id',
                templateUrl: '/statics/tpl/environment/environment/detail.html',
                resolve: load(['/statics/js/paas/environment/environment/environmentDetail.js',
                    '/statics/libs/jquery/topology/jtopo-all-min.js',
                    'smart-table', 'codeMirror', 'ui.select',
                    '/statics/js/directives/graph_assets.js',
                    'echarts', '/statics/libs/assets/echarts/shine.js',
                    '/statics/js/paas/application/instance/apply_dir.js',
                    '/statics/css/user.css'])
            }) .state('paas.environment.newEnvironmentdetail', {
                        url: '/newEnvironmentdetail/:id',
                        templateUrl: '/statics/tpl/environment/environment/newDetail.html',
                        resolve: load(['/statics/js/paas/application/instance/apply_dir.js',
                            '/statics/libs/jquery/topology/jtopo-all-min.js',
                            '/statics/js/directives/graph_assets.js',
                            '/statics/libs/angular/terminal/container-terminal.css',
                            '/statics/libs/angular/terminal/xterm.css',
                            '/statics/js/paas/environment/environment/newEnvironmentDetail.js',
                            'smart-table',
                            '/statics/css/image.css'])
            }).state('paas.environment.environmentAdd', {
                url: '/environmentAdd',
                templateUrl: '/statics/tpl/environment/environment/environmentAdd.html',
                resolve: load(['/statics/js/paas/environment/environment/environmentAdd.js'])
            }).state('paas.environment.host', {
                url: '/host',
                templateUrl: '/statics/tpl/environment/host/list.html',
                resolve: load([
                    '/statics/js/paas/environment/host/tab.js'])
            }).state('paas.environment.hostDetail', {
                url: '/hostDetail/:id/:hostName/:envId/:ip',
                templateUrl: '/statics/tpl/environment/host/detail.html',
                resolve: load(['/statics/js/paas/environment/host/resourceDetail.js', 'echarts',
                    '/statics/libs/angular/terminal/container-terminal.css',
                    '/statics/libs/angular/terminal/xterm.css',
                    '/statics/css/image.css', '/statics/js/paas/application/instance/apply_dir.js',
                ])
                //负载管理
            }).state('paas.environment.loadbalance', {
                url: '/loadbalance',
                templateUrl: '/statics/tpl/environment/loadbalance/list.html',
                resolve: load(['/statics/js/paas/environment/loadbalance/loadbalance.js'])
            }).state('paas.environment.loadbalancedetail', {
                url: '/loadBalancedetail/:id',
                templateUrl: '/statics/tpl/environment/loadbalance/detail.html',
                resolve: load(['/statics/js/paas/environment/loadbalance/loadbalanceDetail.js',
                    '/statics/libs/jquery/topology/jtopo-all-min.js',
                    '/statics/js/directives/graph_assets.js'
                ])
            }).state('paas.environment.container', {
                url: '/kubernetes',
                templateUrl: '/statics/tpl/environment/container/list.html',
                resolve: load(['/statics/js/paas/application/instance/apply_dir.js',
                    '/statics/libs/angular/terminal/container-terminal.css',
                    '/statics/libs/angular/terminal/xterm.css',
                    '/statics/js/paas/environment/container/container.js',
                    'smart-table',
                    '/statics/css/image.css'])
            }).state('paas.environment.containerdetail', {
                url: '/containerdetail',
                templateUrl: '/statics/tpl/environment/container/detail.html',
                resolve: load(['iscroll', '/statics/js/paas/application/instance/apply_dir.js',
                    '/statics/libs/angular/terminal/container-terminal.css',
                    '/statics/libs/angular/terminal/xterm.css',
                    '/statics/js/paas/environment/container/containerDetail.js',
                    'smart-table',
                    '/statics/css/image.css'])
            }).state('paas.environment.storage', {
                url: '/storage',
                templateUrl: '/statics/tpl/environment/storage/list.html',
                resolve: load(['/statics/js/paas/environment/storage/storage.js'])
            }).state('paas.environment.storagedetail', {
                url: '/storagedetail/:id',
                templateUrl: '/statics/tpl/environment/storage/detail.html',
                resolve: load(['/statics/js/paas/environment/storage/storageDetail.js'])
            }).state('paas.environment.network', {
                url: '/network',
                templateUrl: '/statics/tpl/environment/network/list.html',
                resolve: load(['/statics/js/paas/environment/network/network.js'])
            }).state('paas.environment.networkDetail', {
                    url: '/networkDetail/:id',
                    templateUrl: '/statics/tpl/environment/network/detail.html',
                    resolve: load(['/statics/js/paas/environment/network/networkDetail.js', 'echarts',
                        '/statics/css/image.css', '/statics/js/paas/application/instance/apply_dir.js',
                        '/statics/libs/jquery/topology/jtopo-all-min.js',
                        '/statics/js/directives/graph_assets.js'
                    ])
                })

                //镜像管理
                .state('paas.repository', {
                    url: '/repository',
                    template: '<div ui-view class="fade-in-up"></div>',
                    resolve: load(['codeMirror', 'echarts', '/statics/libs/assets/echarts/shine.js', '/statics/css/image.css', '/statics/css/environment.css'])
                })
                .state('paas.repository.repository', {
                    url: '/repository',
                    templateUrl: '/statics/tpl/repository/repository/list.html',
                    resolve: load(['ui.select', '/statics/js/paas/repository/repository/repository.js',
                        '/statics/js/paas/application/template/tpl_task.js'])
                })
                .state('paas.repository.repositorydetail', {
                    url: '/repositorydetail/:id/:port',
                    templateUrl: '/statics/tpl/repository/repository/detail.html',
                    resolve: load(['/statics/js/paas/repository/repository/repositoryDetail.js'])
                })
                .state('paas.repository.dockerimage', {
                    url: '/dockerimage',
                    templateUrl: '/statics/tpl/repository/image/list.html',
                    resolve: load(['easyui', 'ui.select', '/statics/js/paas/repository/image/image.js',
                        '/statics/js/paas/application/template/tpl_task.js'])
                })
                .state('paas.repository.imagedetail', {
                    url: '/dockerimagedetail/:id',
                    templateUrl: '/statics/tpl/repository/image/detail.html',
                    resolve: load(['/statics/js/paas/repository/image/imageDetail.js'])
                })
                //部署
                .state('paas.repository.imagetiondeploy', {
                    url: '/imagetiondeploy/:id',
                    templateUrl: '/statics/tpl/repository/image/gridin.html',
                    resolve: load(['ui.select', '/statics/js/paas/repository/image/imagegridin.js',
                        '/statics/js/paas/application/instance/apply_dir.js',
                    ])
                }).state('paas.repository.imagebuild', {
                    url: '/imagebuild',
                    templateUrl: '/statics/tpl/repository/image/appcreate.html',
                    resolve: load(['/statics/js/paas/repository/image/appCreateIn.js', 'ui.select'])
                })
                //应用
                .state('paas.application', {
                    url: '/application',
                    template: '<div ui-view class="fade-in-up"></div>',
                    resolve: load(['/statics/css/image.css', '/statics/css/environment.css'])
                    // 模板
                }).state('paas.application.template', {
                url: '/template/:tab',
                templateUrl: '/statics/tpl/application/template/list.html',
                resolve: load(['/statics/js/paas/application/template/template.js', 'smart-table', 'modelController'])
                // 模板详情
            }).state('paas.application.templatedetail', {
                url: '/templatedetail/:id',
                templateUrl: '/statics/tpl/application/template/detail.html',
                resolve: load(['/statics/js/paas/application/template/templateDetail.js', 'modelController'])
                // 模板版本
            }).state('paas.application.templateversion', {
                url: '/templateversion/:id',
                templateUrl: '/statics/tpl/application/template/versionlist.html',
                resolve: load(['/statics/js/paas/application/template/tempversion.js', 'smart-table', 'modelController', 'codeMirror', '/statics/js/paas/application/template/tpl_task.js'])
                // 模板版本详情
            }).state('paas.application.templateversiondetail', {
                url: '/templateversiondetail/:id',
                templateUrl: '/statics/tpl/application/template/templateversiondetail.html',
                resolve: load(['/statics/js/paas/application/template/templateversiondetail.js'])
                // 模板版本实例化
            }).state('paas.application.templateinstance', {
                url: '/templateinstance/:id',
                templateUrl: '/statics/tpl/application/template/tempinstance.html',
                resolve: load(['/statics/js/paas/application/template/tempinstance.js'])
                // 配置管理
            }).state('paas.application.configManage', {
                url: '/configManage',
                templateUrl: '/statics/tpl/application/configManage/list.html',
                resolve: load(['/statics/js/paas/application/configManage/configManage.js'])
                // 实例模板化(模板文件方式)
            }).state('paas.application.configManageDetail', {
                url: '/configManageDetail/:id',
                templateUrl: '/statics/tpl/application/configManage/detail.html',
                resolve: load(['jsonViewer', '/statics/js/paas/application/configManage/configManageDetail.js'])
                // 实例模板化(模板文件方式)
            }).state('paas.application.instancetemplate', {
                url: '/instancetemplate/:id/:name/:namespace',
                templateUrl: '/statics/tpl/application/instance/instancetemp.html',
                resolve: load(['/statics/js/paas/application/instance/instancetemp.js', 'codeMirror', '/statics/js/paas/application/template/tpl_task.js'])
                // 实例模板化(模板文件方式)
            }).state('paas.application.instancetemplatetemp', {
                url: '/instancetemplatetemp/:file',
                templateUrl: '/statics/tpl/application/template/instancetemptemp.html',
                resolve: load(['/statics/js/paas/application/template/instancetemptemp.js', 'codeMirror', '/statics/js/paas/application/template/tpl_task.js'])
                // 实例模板化(模板文件方式)
            }).state('paas.application.instancetemplatetempdetail', {
                url: '/instancetemplatetemp/:filetemp/:filedesc',
                templateUrl: '/statics/tpl/application/template/instancetemptempdetail.html',
                resolve: load(['/statics/js/paas/application/template/instancetemptempdetail.js'])
                //编排部署
            }).state('paas.application.templategridin', {
                    url: '/templategridin/:id',
                    templateUrl: '/statics/tpl/application/template/applyGridin.html',
                    resolve: load(['/statics/js/paas/application/template/templategridin.js', 'ui.select', 'modelController'])
                })
                // 应用服务 > 应用编排 > 模板创建文件
                .state('paas.application.addmodel', {
                    url: '/addmodel',
                    templateUrl: '/statics/tpl/application/template/addmodul.html',
                    resolve: load(['/statics/js/paas/application/template/template.js'])
                })
                //应用实例
                .state('paas.application.instance', {
                    url: '/instance',
                    templateUrl: '/statics/tpl/application/instance/list.html',
                    resolve: load(['/statics/js/paas/application/instance/apply.js', 'echarts',
                        '/statics/css/image.css', '/statics/js/paas/application/instance/apply_dir.js'])
                })
                .state('paas.application.instancetiondetail', {
                    // url: '/instancetiondetail/:id/:clusterId',
                    url: '/instancetiondetail/:id/:envId',
                    templateUrl: '/statics/tpl/application/instance/detail.html',
                    resolve: load(['jsonViewer',
                        '/statics/libs/angular/terminal/container-terminal.css',
                        '/statics/libs/angular/terminal/xterm.css',
                        '/statics/js/paas/application/instance/applyDetail.js',
                        '/statics/libs/jquery/topology/jtopo-all-min.js',
                        '/statics/js/directives/graph_assets.js',
                        'statics/libs/jquery/bootstrap/dist/js/bootstrap.min.js',
                        'smart-table', 'codeMirror', 'ui.select',
                        'echarts', '/statics/libs/assets/echarts/shine.js',
                        '/statics/js/paas/application/instance/apply_dir.js',

                        '/statics/css/user.css',
                    ])
                })
                .state('paas.application.store', {
                    url: '/store',
                    templateUrl: '/statics/tpl/application/store/list.html',
                    resolve: load(['ui.select', '/statics/js/paas/application/store/module.js', '/statics/css/image.css', 'modelController'])
                })
                .state('paas.application.storeupload', {
                    url: '/storeupload',
                    templateUrl: '/statics/tpl/application/store/upload.html',
                    resolve: load(['/statics/js/paas/application/store/moduleadd.js', '/statics/css/image.css', 'modelController'])
                })
                .state('paas.application.addmodule', {
                    url: '/addmodule/:modelid/:name/:appId/:type/:envId',
                    // url: '/addmodule',
                    templateUrl: '/statics/tpl/application/store/add.html',
                    resolve: load(['/statics/js/paas/application/store/modulelgridin.js', 'ui.select', 'modelController'])
                })
                .state('paas.application.storedetail', {
                    url: 'moduledetail/:id',
                    templateUrl: '/statics/tpl/application/store/detail.html',
                    resolve: load(['/statics/js/paas/application/store/moduleDetail.js', 'modelController'])
                })

                //部署
                .state('paas.application.instancetiondeploy', {
                    url: '/applytiondeploy/:id',
                    templateUrl: '/statics/tpl/application/instance/gridin.html',
                    resolve: load(['/statics/js/paas/application/instance/applygridin.js',
                        'smart-table', 'ui.select',
                        '/statics/js/paas/application/instance/apply_dir.js',
                        '/statics/css/user.css', 'modelController'])
                })
                //流程管控
                .state('paas.process', {
                    url: '/process',
                    template: '<div ui-view class="fade-in-up"></div>',
                    resolve: load(['/statics/css/image.css', '/statics/css/environment.css'])
                })
                .state('paas.process.layout', {
                    url: '/layout',
                    templateUrl: '/statics/tpl/process/workflow/list.html',
                    resolve: load(['/statics/js/paas/process/workflow/layout.js', 'smart-table',])
                })
                .state('paas.process.layoutdetail', {
                    url: '/layoutdetail/:name/:version',
                    templateUrl: '/statics/tpl/process/workflow/detail.html',
                    resolve: load(['jsonViewer', '/statics/js/paas/process/workflow/layoutDetail.js', '/statics/libs/jquery/topology/jtopo-all-min.js',
                        '/statics/js/directives/_directive_spiderZJS.js', 'smart-table'])
                })
                //新增编排
                .state('paas.process.newlayout', {
                    url: '/newlayout/:name/:version/:workflowId/:status',
                    templateUrl: '/statics/tpl/process/layout/add.html',
                    resolve: load(['taskLayout', '/statics/js/paas/process/layout/addlayout.js',
                        '/statics/js/paas/process/file_upload.js',
                        'modelController'])
                })
                .state('paas.process.addlayout', {
                    url: '/addlayout/:name/:version/:workflowId/:status',
                    templateUrl: '/statics/tpl/process/workflow/add.html',
                    resolve: load(['taskLayout', '/statics/js/paas/process/workflow/addlayout.js',
                        '/statics/js/paas/process/file_upload.js',
                        'modelController'])
                })
                .state('paas.process.workflowdetail', {
                    url: '/workflowdetail/:id/:name/:version',
                    templateUrl: '/statics/tpl/process/workflow/workflowdetail.html',
                    resolve: load(['jsonViewer', '/statics/js/paas/process/workflow/workflowDetail.js', '/statics/libs/jquery/topology/jtopo-all-min.js',
                        '/statics/js/directives/_directive_spiderZJS.js', 'smart-table'])
                })
                .state('paas.process.plugin', {
                    url: '/plugin',
                    templateUrl: '/statics/tpl/process/plugin/list.html',
                    resolve: load(['/statics/js/paas/process/plugin/plugin.js', 'smart-table',])
                })
                .state('paas.process.plugindetail', {
                    url: '/plugindetail/:id',
                    templateUrl: '/statics/tpl/process/plugin/detail.html',
                    resolve: load(['/statics/js/paas/process/plugin/pluginDetail.js'
                    ])
                    //代码仓库
                }).state('paas.process.codeRepository', {
                url: '/codeRepository',
                templateUrl: '/statics/tpl/process/codeRepository/list.html',
                resolve: load(['/statics/js/paas/process/codeRepository/codeRepository.js'])
            }).state('paas.process.codeRepositoryDetail', {
                url: '/codeRepositoryDetail/:id',
                templateUrl: '/statics/tpl/process/codeRepository/detail.html',
                resolve: load(['/statics/js/paas/process/codeRepository/codeRepositoryDetail.js'])
            }).state('paas.userCenter', {
                url: '/userCenter',
                template: '<div ui-view class="fade-in-up"></div>',
                resolve: load(['easyui', 'tableGrid', '/statics/css/environment.css'])
            }).state('paas.userCenter.user', {
                url: '/user',
                templateUrl: '/statics/tpl/userCenter/user/user.html',
                resolve: load(['/statics/js/paas/userCenter/user.js'])
            }).state('paas.userCenter.userResource', {
                url: '/userResource/:id',
                templateUrl: '/statics/tpl/userCenter/user/bindhost.html',
                resolve: load(['/statics/js/paas/userCenter/userResource.js', 'smart-table'])
            }).state('paas.userCenter.role', {
                url: '/role',
                templateUrl: '/statics/tpl/userCenter/role/role.html',
                resolve: load(['/statics/js/paas/userCenter/role.js'])
            }).state('paas.userCenter.permission', {
                url: '/permission',
                templateUrl: '/statics/tpl/userCenter/permission/permission.html',
                resolve: load(['/statics/js/paas/userCenter/permission.js'])
            }).state('paas.userCenter.department', {
                url: '/department',
                templateUrl: '/statics/tpl/userCenter/department/department.html',
                resolve: load(['/statics/js/paas/userCenter/department.js'])
            }).state('paas.userCenter.departmentResource', {
                url: '/departmentResource/:id',
                templateUrl: '/statics/tpl/userCenter/department/bindhost.html',
                resolve: load(['/statics/js/paas/userCenter/departmentResource.js', 'smart-table'])
            }).state('paas.userCenter.group', {
                url: '/group',
                templateUrl: '/statics/tpl/userCenter/group/group.html',
                resolve: load(['/statics/js/paas/userCenter/group.js', 'smart-table'])
            }).state('paas.userCenter.userGroupDetail', {
                url: '/userGroupDetail/:id',
                templateUrl: '/statics/tpl/userCenter/group/userGroup/detail.html',
                resolve: load(['/statics/js/paas/userCenter/userGroupDetail.js', 'smart-table'])
            }).state('paas.userCenter.resGroupDetail', {
                url: '/resGroupDetail/:id',
                templateUrl: '/statics/tpl/userCenter/group/resourceGroup/detail.html',
                resolve: load(['/statics/js/paas/userCenter/resourceGroupDetail.js', 'smart-table'])
            }).state('paas.personal', {
                url: '/personal',
                template: '<div ui-view class="fade-in-up"></div>',
                resolve: load(['easyui', 'tableGrid', '/statics/css/environment.css'])
            }).state('paas.personal.info', {
                url: '/info',
                templateUrl: '/statics/tpl/personal/info/index.html',
                resolve: load(['/statics/js/paas/personal/info.js'])
            }).state('paas.personal.approval', {
                url: '/approval',
                templateUrl: '/statics/tpl/personal/approval/index.html',
                resolve: load(['smart-table', '/statics/js/paas/personal/approval.js'])
            }).state('paas.personal.message', {
                url: '/message',
                templateUrl: '/statics/tpl/personal/message/index.html',
                resolve: load(['/statics/js/paas/personal/message.js'])
            }).state('paas.personal.applyservice', {
                url: '/applyservice',
                templateUrl: '/statics/tpl/personal/applyservice/index.html',
                resolve: load(['/statics/js/paas/personal/apply_service.js'])
            }).state('paas.personal.vmadd', {
                    url: '/vmadd/:id/:type',
                    templateUrl: '/statics/tpl/personal/applyservice/addVm.html',
                    resolve: load(['/statics/js/paas/personal/vmAdd.js'])
                })
                //系统运维
                .state('paas.system', {
                    url: '/system',
                    template: '<div ui-view class="fade-in-up"></div>',
                    resolve: load(['easyui', 'tableGrid', '/statics/css/environment.css'])
                }).state('paas.system.monitor', {
                url: '/monitor',
                templateUrl: '/statics/tpl/system/monitor/index.html',
                resolve: load(['/statics/js/paas/system/monitor.js'])
            }).state('paas.system.log', {
                url: '/log',
                templateUrl: '/statics/tpl/system/log/list.html',
                resolve: load(['/statics/js/paas/system/log.js', 'smart-table'])
            }).state('paas.system.parameter', {
                url: '/parameter',
                templateUrl: '/statics/tpl/system/parameter/list.html',
                resolve: load(['/statics/js/paas/system/parameter.js', '/statics/css/image.css',])
                // 服务目录
            }).state('paas.service_catalog', {
                url: '/service_catalog',
                template: '<div ui-view class="fade-in-up"></div>',
            }).state('paas.service_catalog.list', {
                url: '/list',
                templateUrl: '/statics/tpl/service_catalog/list.html',
                resolve: load(['/statics/js/paas/service_catalog/list.js', '/statics/css/image.css'])
            }).state('paas.about', {
                    url: '/about',
                    templateUrl: '/statics/tpl/access/historymessage.html',
                    resolve: load(['/statics/js/controllers/updatePassword.js', '/statics/css/alarm.css',])
                })
                .state('paas.catafunction', {
                    url: '/catafunction',
                    template: '<div ui-view class="fade-in-up"></div>',
                    resolve: load(['codeMirror', 'echarts', '/statics/libs/assets/echarts/shine.js', '/statics/css/image.css', '/statics/css/environment.css'])
                })
                // 镜像上传
                .state('paas.catafunction.imageupload', {
                    url: '/imageupload',
                    templateUrl: '/statics/tpl/cataFunction/imageupload.html',
                    resolve: load(['/statics/js/paas/cataFunction/imageUpload.js', 'ui.select'])
                })//镜像同步
                .state('paas.catafunction.imageSync', {
                    url: '/imageSync',
                    templateUrl: '/statics/tpl/cataFunction/imageSync.html',
                    resolve: load(['/statics/js/paas/cataFunction/imageSync.js'])
                    //公有库同步
                }).state('paas.catafunction.publicSync', {
                    url: '/publicSync',
                    templateUrl: '/statics/tpl/cataFunction/publicSync.html',
                    resolve: load(['/statics/js/paas/cataFunction/publicSync.js', 'ui.select'])
                })	//安全扫描
                .state('paas.catafunction.securityScan', {
                    url: '/securityScan',
                    templateUrl: '/statics/tpl/catafunction/securityScan.html',
                    resolve: load(['/statics/js/paas/catafunction/securityScan.js', 'ui.select'])
                })
            function load(srcs, callback) {
                return {
                    deps: ['$ocLazyLoad', '$q',
                        function ($ocLazyLoad, $q) {
                            var deferred = $q.defer();
                            var promise = false;
                            srcs = angular.isArray(srcs) ? srcs : srcs.split(/\s+/);
                            if (!promise) {
                                promise = deferred.promise;
                            }
                            angular.forEach(srcs, function (src) {
                                //console.log(src);
                                promise = promise.then(function () {
                                    if (JQ_CONFIG[src]) {
                                        return $ocLazyLoad.load(JQ_CONFIG[src]);
                                    }
                                    //if(src.substr(src.length-3, 3) === '.js') {
                                    //    name = src + '?v=' + OpsConstant.O_VERSION;
                                    //} else {
                                    name = src;
                                    //}
                                    return $ocLazyLoad.load(name);
                                });
                            });
                            deferred.resolve();
                            return callback ? promise.then(function () {
                                return callback();
                            }) : promise;
                        }]
                }
            }

            // 去掉angular url的#号
            //$locationProvider.html5Mode(true);
        }
    ]
);
