// lazyload config

angular.module('app')
    /**
   * jQuery plugin config use ui-jq directive , config the js and css files that required
   * key: function name of the jQuery plugin
   * value: array of the css js file located
   */
  .constant('JQ_CONFIG', {
      easyPieChart:   [   '/statics/libs/jquery/jquery.easy-pie-chart/dist/jquery.easypiechart.fill.js'],
      sparkline:      [   '/statics/libs/jquery/jquery.sparkline/dist/jquery.sparkline.retina.js'],
      plot:           [   '/statics/libs/jquery/flot/jquery.flot.js',
                          '/statics/libs/jquery/flot/jquery.flot.pie.js',
                          '/statics/libs/jquery/flot/jquery.flot.resize.js',
                          '/statics/libs/jquery/flot.tooltip/js/jquery.flot.tooltip.min.js',
                          '/statics/libs/jquery/flot.orderbars/js/jquery.flot.orderBars.js',
                          '/statics/libs/jquery/flot-spline/js/jquery.flot.spline.min.js'],
      moment:         [   '/statics/libs/jquery/moment/moment.js'],
      screenfull:     [   '/statics/libs/jquery/screenfull/dist/screenfull.min.js'],
      slimScroll:     [   '/statics/libs/jquery/slimscroll/jquery.slimscroll.min.js'],
      sortable:       [   '/statics/libs/jquery/html5sortable/jquery.sortable.js'],
      nestable:       [   '/statics/libs/jquery/nestable/jquery.nestable.js',
                          '/statics/libs/jquery/nestable/jquery.nestable.css'],
      filestyle:      [   '/statics/libs/jquery/bootstrap-filestyle/src/bootstrap-filestyle.js'],
      slider:         [   '/statics/libs/jquery/bootstrap-slider/bootstrap-slider.js',
                          '/statics/libs/jquery/bootstrap-slider/bootstrap-slider.css'],
      switch:            [  '/statics/libs/angular/angular-ui-switch/angular-ui-switch.min.css',
                    '/statics/libs/angular/angular-ui-switch/angular-ui-switch.min.js'],
      chosen:         [   '/statics/libs/jquery/chosen/chosen.jquery.min.js',
                          '/statics/libs/jquery/chosen/bootstrap-chosen.css'],
      TouchSpin:      [   '/statics/libs/jquery/bootstrap-touchspin/dist/jquery.bootstrap-touchspin.min.js',
                          '/statics/libs/jquery/bootstrap-touchspin/dist/jquery.bootstrap-touchspin.min.css'],
      wysiwyg:        [   '/statics/libs/jquery/bootstrap-wysiwyg/bootstrap-wysiwyg.js',
                          '/statics/libs/jquery/bootstrap-wysiwyg/external/jquery.hotkeys.js'],
      dataTable:      [   '/statics/libs/jquery/datatables/media/js/jquery.dataTables.min.js',
                          '/statics/libs/jquery/plugins/integration/bootstrap/3/dataTables.bootstrap.js',
                          '/statics/libs/jquery/plugins/integration/bootstrap/3/dataTables.bootstrap.css'],
      vectorMap:      [   '/statics/libs/jquery/bower-jvectormap/jquery-jvectormap-1.2.2.min.js',
                          '/statics/libs/jquery/bower-jvectormap/jquery-jvectormap-world-mill-en.js',
                          '/statics/libs/jquery/bower-jvectormap/jquery-jvectormap-us-aea-en.js',
                          '/statics/libs/jquery/bower-jvectormap/jquery-jvectormap.css'],
      footable:       [   '/statics/libs/jquery/footable/dist/footable.all.min.js',
                          '/statics/libs/jquery/footable/css/footable.core.css'],
      fullcalendar:   [   '/statics/libs/jquery/moment/moment.js',
                          '/statics/libs/jquery/fullcalendar/dist/fullcalendar.min.js',
                          '/statics/libs/jquery/fullcalendar/dist/fullcalendar.css',
                          '/statics/libs/jquery/fullcalendar/dist/fullcalendar.theme.css'],
      daterangepicker:[   '/statics/libs/jquery/moment/moment.js',
                          '/statics/libs/jquery/bootstrap-daterangepicker/daterangepicker.js',
                          '/statics/libs/jquery/bootstrap-daterangepicker/daterangepicker-bs3.css'],
      tagsinput:      [   '/statics/libs/jquery/bootstrap-tagsinput/dist/bootstrap-tagsinput.js',
                          '/statics/libs/jquery/bootstrap-tagsinput/dist/bootstrap-tagsinput.css'],
      Jcrop:          [   '/statics/libs/jquery/jquery.Jcrop/jquery.Jcrop.min.js',
                          '/statics/libs/jquery/jquery.Jcrop/jquery.Jcrop.min.css'],
      codeMirror:     [   '/statics/libs/jquery/codemirror/codemirror.css',
                          '/statics/libs/jquery/codemirror/theme/erlang-dark.css',
                          '/statics/libs/jquery/codemirror/codemirror-min.js'],
      clipboard:      [   '/statics/libs/jquery/clipboard/clipboard.js'],
      mCustomScrollbar:[  '/statics/libs/jquery/scrollbar/jquery.mCustomScrollbar.min.css',
                          '/statics/libs/jquery/scrollbar/jquery.mousewheel.min.js',
                          '/statics/libs/jquery/scrollbar/jquery.mCustomScrollbar.min.js',
                          '/statics/js/directives/_directive_mCustomScrollbar.js'],
      easyui:           ['/statics/libs/jquery/easyui/easyui.css',
                          '/statics/libs/jquery/easyui/jquery.easyui.min.js'],

	   datepicker:           ['/statics/libs/jquery/jquery-datepicker/jquery-ui-1.10.2.min.js',
						   '/statics/libs/jquery/jquery.ui/jquery-ui-1.10.2.css'],

      tableGrid:        ['/statics/js/directives/_directive_easyui_tree.js',
                          '/statics/js/directives/_directive_tableGrid.js'],
      echarts:           ['/statics/libs/assets/echarts/echarts.min.js',
                           '/statics/js/directives/directive_echarts.js'],
    selectServer:         ['/statics/js/controllers/selectServerModal.js'],
    taskLayout:           ['/statics/libs/jquery/topology/jtopo-all-min.js',
                           '/statics/libs/jquery/jquery.ui/jquery.ui.min.js',
                           '/statics/libs/jquery/codemirror/codemirror.css',
                           '/statics/libs/jquery/codemirror/theme/erlang-dark.css',
                           '/statics/libs/jquery/codemirror/codemirror-min.js',
                           '/statics/js/controllers/selectServerModal.js',
                        //   '/statics/js/paas/operation/tpl_task.js'
                         ],
   modelController:      [   '/statics/libs/jquery/codemirror/codemirror.css',
                          '/statics/libs/jquery/codemirror/theme/erlang-dark.css',
                          '/statics/libs/jquery/codemirror/codemirror-min.js',
                           '/statics/js/paas/application/template/tpl_task.js'
                        ],
  modelTerminal:      [   '/statics/libs/angular/terminal/container-terminal.css',
                         '/statics/libs/angular/terminal/container-terminal.js',
                         '/statics/libs/angular/terminal/patternfly.css',
                          '/statics/libs/angular/terminal/patternfly-additions.css',
                         '/statics/libs/angular/terminal/xterm.css',
                          '/statics/libs/angular/terminal/xterm.js'
                       ],
    treeController:      ['/statics/libs/angular/angular-tree-control/angular-tree-control.js'],
    jsonViewer:          ['/statics/libs/jquery/json-viewer/jquery.json-viewer.css',
                          '/statics/libs/jquery/json-viewer/jquery.json-viewer.js'
                          ],
    dashboard:           ['/statics/libs/jquery/topology/jtopo-all-min.js',
                          '/statics/libs/assets/echarts/echarts.min.js',
                          '/statics/js/paas/dashboard/tpl_dashboard.js',
                          '/statics/js/directives/graph_assets.js'],
    assetsLayout:         ['/statics/libs/jquery/topology/jtopo-all-min.js',
	                         //'/statics/js/directives/_directive_spider.js',
                           '/statics/js/filters/infrastructure.js',
                           '/statics/js/directives/graph_assets.js',
                           '/statics/js/directives/alarmserver_assets.js'],
    iscroll:               ['/statics/libs/jquery/iscroll/iscroll.js']
    }
  )
  .constant('MODULE_CONFIG', [
      {
          name: 'ngGrid',
          files: [
              '/statics/libs/angular/ng-grid/build/ng-grid.min.js',
              '/statics/libs/angular/ng-grid/ng-grid.min.css',
              '/statics/libs/angular/ng-grid/ng-grid.bootstrap.css'
          ]
      },
      {
          name: 'ui.grid',
          files: [
              '/statics/libs/angular/angular-ui-grid/ui-grid.min.js',
              '/statics/libs/angular/angular-ui-grid/ui-grid.min.css',
              '/statics/libs/angular/angular-ui-grid/ui-grid.bootstrap.css'
          ]
      },
      {
          name: 'ui.select',
          files: [
              '/statics/libs/angular/angular-ui-select/dist/select.min.js',
              '/statics/libs/angular/angular-ui-select/dist/select.min.css'
          ]
      },
      {
          name:'angularFileUpload',
          files: [
            '/statics/libs/angular/angular-file-upload/angular-file-upload.js'
          ]
      },
      {
          name:'ui.calendar',
          files: ['/statics/libs/angular/angular-ui-calendar/src/calendar.js']
      },
      {
          name: 'ngImgCrop',
          files: [
              '/statics/libs/angular/ngImgCrop/compile/minified/ng-img-crop.js',
              '/statics/libs/angular/ngImgCrop/compile/minified/ng-img-crop.css'
          ]
      },
      {
          name: 'angularBootstrapNavTree',
          files: [
              '/statics/libs/angular/angular-bootstrap-nav-tree/dist/abn_tree_directive.js',
              '/statics/libs/angular/angular-bootstrap-nav-tree/dist/abn_tree.css'
          ]
      },
    {
      name: 'vr.directives.slider',
      files: [
        '/statics/libs/angular/venturocket-angular-slider/build/angular-slider.min.js',
        '/statics/libs/angular/venturocket-angular-slider/build/angular-slider.css'
      ]
    },
      {
          name: 'toaster',
          files: [
              '/statics/libs/angular/angularjs-toaster/toaster.js',
              '/statics/libs/angular/angularjs-toaster/toaster.css'
          ]
      },
      {
          name: 'textAngular',
          files: [
              '/statics/libs/angular/textAngular/dist/textAngular-sanitize.min.js',
              '/statics/libs/angular/textAngular/dist/textAngular.min.js'
          ]
      },
      {
          name: 'vr.directives.slider',
          files: [
              '/statics/libs/angular/venturocket-angular-slider/build/angular-slider.min.js',
              '/statics/libs/angular/venturocket-angular-slider/build/angular-slider.css'
          ]
      },
      {
          name: 'com.2fdevs.videogular',
          files: [
              '/statics/libs/angular/videogular/videogular.min.js'
          ]
      },
      {
          name: 'com.2fdevs.videogular.plugins.controls',
          files: [
              '/statics/libs/angular/videogular-controls/controls.min.js'
          ]
      },
      {
          name: 'com.2fdevs.videogular.plugins.buffering',
          files: [
              '/statics/libs/angular/videogular-buffering/buffering.min.js'
          ]
      },
      {
          name: 'com.2fdevs.videogular.plugins.overlayplay',
          files: [
              '/statics/libs/angular/videogular-overlay-play/overlay-play.min.js'
          ]
      },
      {
          name: 'com.2fdevs.videogular.plugins.poster',
          files: [
              '/statics/libs/angular/videogular-poster/poster.min.js'
          ]
      },
      {
          name: 'com.2fdevs.videogular.plugins.imaads',
          files: [
              '/statics/libs/angular/videogular-ima-ads/ima-ads.min.js'
          ]
      },
      {
          name: 'xeditable',
          files: [
              '/statics/libs/angular/angular-xeditable/dist/js/xeditable.min.js',
              '/statics/libs/angular/angular-xeditable/dist/css/xeditable.css'
          ]
      },
      {
          name: 'smart-table',
          files: [
              '/statics/libs/angular/angular-smart-table/dist/smart-table.min.js'
          ]
      },
      {
          name: 'angular-skycons',
          files: [
              '/statics/libs/angular/angular-skycons/angular-skycons.js'
          ]
      },
      {
          name: 'treeGrid',
          files: [
              '/statics/libs/angular/angular-tree-grid/tree-grid-directive.js',
              '/statics/libs/angular/angular-tree-grid/treeGrid.css'
          ]
      },
        {
            name: 'angularSwitch',
            files: [
                '/statics/libs/angular/angular-ui-switch/angular-ui-switch.min.css',
                '/statics/libs/angular/angular-ui-switch/angular-ui-switch.min.js'
            ]
        }
    ]
  )
  // oclazyload config
  .config(['$ocLazyLoadProvider', 'MODULE_CONFIG', function($ocLazyLoadProvider, MODULE_CONFIG) {
      // We configure ocLazyLoad to use the lib script.js as the async loader
      $ocLazyLoadProvider.config({
          debug:  false,
          events: true,
          modules: MODULE_CONFIG
      });
  }])
;
