(function () {
  app.service('workflowData', [function () {
    var service = {
      taskName: [],
      labelName: [],
      jobName:[],
        node:{},
        Diagramnodes:{}
    };
    return service;
  }])
  app.controller('addArrangeModalCtrl', ['$rootScope', '$scope','$state','httpLoad','$stateParams','LANGUAGE','$timeout',
   function($rootScope, $scope,$state,httpLoad,$stateParams,LANGUAGE,$timeout) {
     $rootScope.link = '/statics/css/sortware.css';
     $scope.content = "增加编排"
     if($stateParams.name){ $scope.content = "编排编排"}
     $scope.goBack = function(){
         $state.go('paas.process.layout');
     };
  }]);

app.factory('NewTaskService',['$rootScope','workflowData','httpLoad','$modal', '$timeout', '$stateParams',	function ($rootScope,workflowData, httpLoad, $modal, $timeout, $stateParams) {
  var NewTaskService = function () {
    this.Init();
    this.selectItem = {x:0,nodeType:''};
    this.LinkLists = [];
    this.scriptTask = false;
      this.modalName = "配置";

  }

  NewTaskService.prototype = {
    Init:function(){
      //初始化内容
      this.InitContent();
      this.InitTab();
      this.DrawStartNode();
      //事件绑定
      this.EveSet();

    },
    InitContent:function(){
      this.scene = new JTopo.Scene();
      this.scene.background = '/statics/img/topology/bg.jpg';
      var canvas = document.getElementById('canvas');
      canvas.height = window.innerHeight * 0.7;
      canvas.width = (window.innerWidth-265) * 0.7;
      this.stage = new JTopo.Stage(canvas);
      //this.stage.eagleEye.visible = true;
      this.stage.add(this.scene);
      this.ShowJTopoToolBar(this.stage);
    },
    InitTab:function(){
      var data = [{
        "nodeType": "git_clone",
        "Image": "icon_git_5.png",
        "text": "git克隆"
      }, {
        "nodeType": "code_scan",
        "Image": "icon_codescan_5.png",
        "text": "代码扫描"
      },
          {
              "nodeType": "package",
              "Image": "icon_package_5.png",
              "text": "代码打包"
          },
          {
              "nodeType": "build_image",
              "Image": "icon_build_5.png",
              "text": "构建镜像"
          },
          {
        "nodeType": "image_scan",
        "Image": "icon_imagescan_5.png",
        "text": "镜像扫描"
      },

      {
        "nodeType": "push_image",
        "Image": "icon_pushimage_5.png",
        "text": "推送镜像"
      },
          // {
      //   "nodeType": "deploy",
      //   "Image": "icon_deploy_5.png",
      //   "text": "镜像部署"
      // },
          {
              "nodeType": "check_out",
              "Image": "icon_svn_5.png",
              "text": "svn检出"
          }

      ];

      var html = [];
      data.forEach(function (item) {
        html.push('<div  class="item" type="'+item.nodeType+'" image="'+item.Image+'">');
        html.push('<img class="node-img" src="/statics/img/task/'+item.Image+'" />');
        html.push('<span>'+item.text+'</span></div>');
      });
      $('.task-tab').html(html.join(''));
    },
    //工具栏
    ShowJTopoToolBar:function(stage){
      stage.mode = 'edit';
      var that = this;
      var toobarDiv = $('<div class="jtopo_toolbar">').html(''
        // +'<label class="i-checks i-checks-sm m-r-xs"> <input type="radio" name="modeRadio" value="normal" checked id="r1"><i></i>默认</label>'
        // +'<label class="i-checks i-checks-sm m-r-xs"> <input type="radio" name="modeRadio" value="select" id="r2"><i></i>框选</label>'
        // +'<label class="i-checks i-checks-sm m-r-xs"> <input type="radio" name="modeRadio" value="drag" id="r3"><i></i>平移</label>'
        // +'<label class="i-checks i-checks-sm m-r-md"> <input type="radio" name="modeRadio" value="edit" id="r4"><i></i>编辑</label>'
        +'<input type="text" id="findText" class="form-control" value="" onkeydown="findButton.click()" style="display: inline-block;width: 200px;height: 26px">'
        +'<button class="btn m-b-xs btn-xs btn-primary m-l-xs m-t-xs" id="findButton"><i class="icon-magnifier"></i> 查 询</button>'
        +'<button class="btn btn-xs btn-success pull-right m-t-xs" id="exportButton"><i class="fa fa-floppy-o"></i> 导出PNG</button>'
        +'<button class="btn btn-xs btn-info m-r-xs pull-right m-t-xs"  id="fullScreenButton"><i class="icon-size-fullscreen"></i> 全 屏</button>'
        +'<button class="btn btn-xs btn-primary m-r-xs pull-right m-t-xs"  id="centerButton"><i class="fa fa-align-center"></i> 居 中</button>'
        +'<button class="btn btn-xs btn-success m-r-xs pull-right m-t-xs"  id="zoomOutButton"><i class="icon-magnifier-add"></i>  放 大</button>'
        +'<button class="btn btn-xs btn-danger m-r-xs pull-right m-t-xs"  id="zoomInButton"><i class="icon-magnifier-remove"></i>  缩 小</button>'
        +'<label class="i-checks i-checks-sm m-r m-t-xs pull-right" ><input type="checkbox" id="zoomCheckbox" ><i></i>鼠标缩放</label>'
      );
    //  $('#content').prepend(toobarDiv);

      // 工具栏按钮处理
      // $("input[name='modeRadio']").click(function(){
      // 	stage.mode = $("input[name='modeRadio']:checked").val();
      // 	console.log(that.stage.mode);
      // });
      $('#centerButton').click(function(){
        stage.centerAndZoom(); //缩放并居中显示
      });
      $('#zoomOutButton').click(function(){
        stage.zoomOut();
      });
      $('#zoomInButton').click(function(){
        stage.zoomIn();
      });
      $('#exportButton').click(function(){
        stage.saveImageInfo();
      });
      $('#zoomCheckbox').click(function(){
        $(this).toggleClass('checked');
        if($('#zoomCheckbox').hasClass('checked')){
          stage.wheelZoom = 1.15; // 设置鼠标缩放比例
        }else{
          stage.wheelZoom = null; // 取消鼠标缩放比例
        }
      });
      $('#fullScreenButton').click(function(){
        runPrefixMethod(stage.canvas, "RequestFullScreen")
      });
      // 查询
      $('#findButton').click(function(){
        var text = $('#findText').val().trim();
        var nodes = stage.find('node[text="'+text+'"]');
        if(nodes.length > 0){
          var node = nodes[0];
          node.selected = true;
          var location = node.getCenterLocation();
          // 查询到的节点居中显示
          stage.setCenter(location.x, location.y);
          function nodeFlash(node, n){
            if(n == 0) {
              node.selected = false;
              return;
            };
            node.selected = !node.selected;
            setTimeout(function(){
              nodeFlash(node, n-1);
            }, 300);
          }
          // 闪烁几下
          nodeFlash(node, 6);
        }
      });
    },
    //绘制开始结束节点
    DrawStartNode:function(){
      var that = this;
      var nodeData = [{
        x:80,
        y:180,
        text:'开始节点',
        type:'START',
        img:'start_normal.png'
      },{
        x:450,
        y:180,
        text:'结束节点',
        type:'END',
        img:'end_normal.png'
      }];
      nodeData.forEach(function (item) {
        that.AddNode(item.x,item.y,item.text,item.img,'Bottom_Center',item.type);
      });
    },
    //初始绘制节点
    DrawNode:function(data){
      var that = this;
      //清除节点数据
      that.scene.clear();
      var img = {
        START:'start_normal.png',
        END:'end_normal.png',
          git_clone:'icon_git_5.png',
          code_scan:'icon_codescan_5.png',
          image_scan:'icon_imagescan_5.png',
          push_image:'icon_pushimage_5.png',
          package:'icon_package_5.png',
          deploy:'icon_deploy_5.png',
          build_image:'icon_build_5.png',
          check_out:'icon_svn_5.png',

      }
      data.nodes.forEach(function(item){
        if(item.start){
          that.AddNode(item.location.x,item.location.y,'开始节点',img.START,'Bottom_Center','START',item);
        }else	if(item.end){
          that.AddNode(item.location.x,item.location.y,'结束节点',img.END,'Bottom_Center','END',item);
        }else {
          that.AddNode(item.location.x,item.location.y,item.text,img[item.data.name],'Bottom_Center',item.data.name,item);
        }
      });
      data.links.forEach(function (item) {
          var nodeA = that.scene.findElements(function (e) {
            return e.id == item.nodeAid;
          });
        var nodeZ = that.scene.findElements(function (e) {
          return e.id == item.nodeZid;
        });
        if (nodeA[0] && nodeZ[0]) {
          that.AddLink(nodeA[0], nodeZ[0], item.text);
        }
      });
    },
    //组合线路图
    DiagramData:function(){
      var data = this.scene.childs;
      var nodes = [],diagram = false;
      for(var i in data){
        var item = data[i];
        if(item.elementType == 'node'){
          if(item.nodeType == 'SIMPLE'||item.nodeType == 'DYNAMIC'||item.nodeType == 'HTTP'){
                nodes.push(item);
          }else if(item.nodeType == 'DECISION'||item.nodeType == 'FORK_JOIN'){
            diagram = true
          }
        }
      }
      for(var i=0;i<nodes.length-1;i++){
         for(var j=i+1;j<nodes.length;j++){
                 if (nodes[i].x>nodes[j].x){
                        var temp=nodes[i];
                         nodes[i]=nodes[j];
                         nodes[j]=temp;
                 }
         }
      }
      return {nodes:nodes,diagram:diagram}
    },
    //组合数据
    FormatData:function(){
      var data = this.scene.childs;
      var nodes = [], links = [] ,tasks = [];
      var judgeNode = function(item){
        item.inLinks = item.inLinks || [];item.outLinks = item.outLinks || [];
        var nodeFlash = function(item,text,n){
          if(n == 0){
            item.alarm = null;
            return;
          }
          if(item.alarm){
            item.alarm = null;
          }else{
            item.alarm = text;
          }
          setTimeout(function(){
            nodeFlash(item,text,n-1)
          },600)
        };
        if(item.nodeType == 'START'){
          if(item.inLinks.length > 0 || item.outLinks.length == 0){
            $rootScope.pop('请确保【开始节点】入度为0，出度不为0','error');
            nodeFlash(item,'有误',8)
            return false;
          }
        }else if(item.nodeType == 'END'){
          if(item.inLinks.length == 0 || item.outLinks.length > 0){
            $rootScope.pop('请确保【结束节点】入度不为0，出度为0','error');
            nodeFlash(item,'有误',8)
            return false;
          }
        }else{
          if(item.inLinks.length == 0 || item.outLinks.length == 0){
            $rootScope.pop('请确保【普通节点】的入度、出度均不为0','error');
            nodeFlash(item,'有误',8)
            return false;
          }else if(!item.nodeData){
             $rootScope.pop('请填写节点数据','error');
             item.alarm = '必填';
             return false;
          }else if(item.outLinks.length!=1 && item.nodeType != "DECISION" && item.nodeType != "FORK_JOIN"){
             $rootScope.pop('只有选择流或者分支能有多个出度','error');
             item.alarm = '必填';
             return false;
          }else if(['1','14'].indexOf($stateParams.flag) > -1 && item.nodeData&&item.nodeData.targets.length ==0){//模板创建目标机器的验证
            $rootScope.pop('请选择目标机器','error');
            item.alarm = '必填';
            return false;
          }
        };
        return true;
      }
      for(var i in data){
        var item = data[i];

        if(item.elementType == 'node'){
          if(!judgeNode(item)) 		return false;
          var obj = {
            text:item.text,
            nodeId:item.id,
            location:{
              x:item.x,
              y:item.y
            },
            data:item.nodeData,
            start:item.nodeType == 'START' ? true : false,
            end:item.nodeType == 'END' ? true : false,
          };
          nodes.push(obj);
          if(item.nodeType == "DECISION" || item.nodeType == "FORK_JOIN"){
            if(item.x < this.selectItem.x){
                this.selectItem = item
            }else if(this.selectItem.x==0){
              this.selectItem = item
            }
          }

          if(item.nodeData){
            item.nodeData.inLinks =item.inLinks.length;
            item.nodeData._id =item.id;
            item.nodeData.location ={
                x:item.x,
                y:item.y
              };
        	  tasks.push(item.nodeData);
          }

        }else if(item.elementType == 'link'){

          var obj = {
            nodeAid: item.nodeA.id,
            nodeZid: item.nodeZ.id,
            text:item.text
          };
          var objlink = {
            nodeAid: item.nodeA.id,
            nodeZid: item.nodeZ.id,
            item:item,
            text:item.text
          };
          links.push(obj);
          this.LinkLists.push(objlink)
        }
      }
      return {nodes:nodes,links:links,tasks:tasks}
    },
    //对连线进行判断
    JudgeLink:function(beginNode,endNode){
      var data = this.scene.childs;
      var flag = true;
      for(var i in data){
        var item = data[i];
        if(item.elementType == 'link'){
          if((beginNode.id == item.nodeA.id && endNode.id == item.nodeZ.id) || (beginNode.id == item.nodeZ.id && endNode.id == item.nodeA.id)){
            flag = false;
            return false;
          }
        }
      };
      return flag;
    },
    //添加节点
    AddNode:function(x, y, str, img, textPosition, nodeType, nodeData){
      var node = new JTopo.Node(str);
      node.setLocation(x, y);
      node.id = new Date().getTime() + x;
      if(nodeData){
        node.id = nodeData.nodeId;
        node.nodeData = nodeData.data;
      }
      node.nodeType = nodeType;
      if (null != img) {
        node.setImage('/statics/img/task/' + img, false);
        node.Image = img;
      }
      node.textPosition = textPosition;//设置文字位置
      node.fontColor = '88,102,110';//设置文字颜色
      node.setSize(40,40);//设置图标大小
      this.EveSetNode(node);
      this.scene.add(node);
      return node;
    },
    //打开编辑模态框
    OpenEditModal:function (node) {
      node.alarm = null;
      this.AddTaskNode(node);
  },
    //添加连线
    AddLink:function(nodeA,nodeZ,str){
      var link = new JTopo.Link(nodeA, nodeZ, str);
      link.text = str
      link.lineWidth = 2;//线宽
      link.bundleGap = 20;//线条之间的间隔
      link.bundleOffset = 0; // 折线拐角处的长度
      link.textOffsetY = 3;//文本偏移量（向下3个像素）
      link.arrowsRadius = 7;//箭头大小
      link.fontColor = '0, 0, 0';//字体颜色
      link.font = '20';//字体颜色
      link.strokeColor = '0, 200, 255';//线条颜色
      this.EveSetLink(link);
      this.scene.add(link);
    },
    //添加脚本节点
    AddTaskNode:function(node){

      var that = this;
        if(node.inLinks&&node.outLinks){
            $rootScope.$apply(function () {

                that.scriptTask = false;
            })
            var Diagramnodes=  this.DiagramData();
            workflowData.node = node;
            workflowData.Diagramnodes = Diagramnodes;
            $rootScope.$apply(function () {
                $(".setting_task").hide()
                that.modalName = node.text+"任务";
                that.scriptTask = true;
            })
        }else{
            $rootScope.$apply(function () {
                $rootScope.pop('请确保节点都连线','error');
            })
        }
    },
  //绘制动态连线
    DrawDynamicLink:function(){
      var that = this;
      var tempNodeA = new JTopo.Node('tempA');
      tempNodeA.setSize(1, 1);
      var tempNodeZ = new JTopo.Node('tempZ');
      tempNodeZ.setSize(1, 1);
      var link = new JTopo.Link(tempNodeA, tempNodeZ);
      var beginNode = that.currentNode;
      that.scene.add(link);
      tempNodeA.setLocation(beginNode.x+40, beginNode.y+40);
      tempNodeZ.setLocation(beginNode.x+40, beginNode.y+40);
      that.scene.click(function(e){
        if(e.button == 2 || !beginNode){
          that.scene.remove(link);
          return;
        }
        if(e.target != null && e.target instanceof JTopo.Node){
          if(beginNode !== e.target){
            var endNode = e.target;
            if(that.JudgeLink(beginNode,endNode)) that.AddLink(beginNode,endNode,'');
            beginNode = null;
            that.scene.remove(link);
          }else{
            that.scene.remove(link);
             beginNode = null;
          }
        }else{
          that.scene.remove(link);
          beginNode = null;
        }
      });
      that.scene.mousedown(function(e){
        if(e.target == null || e.target === beginNode || e.target === link){
          that.scene.remove(link);
          beginNode = null;
        }
      });
      that.scene.mousemove(function(e){
          tempNodeZ.setLocation(e.x, e.y);
      });
    },
    //对内容节点的操作
    EveSetNode:function(node){
      var that = this;
      //鼠标释放
      node.addEventListener('mouseup', function (event) {
        $(".node-menu").hide();
        if (event.button == 2) {//右键
          var offsetY = $('.wrapper-md').height() - window.innerHeight * 0.7;
          var x = event.offsetX + 200;
          var y = event.offsetY + 100 ;
          if(node.nodeType == 'START' || node.nodeType == 'END'){
            $("#contextmenustart").css({
              top: y,
              left: x
            }).show();
          }else{
            $("#contextmenu").css({
              top: y,
              left: x
            }).show();
          }
          that.currentNode = node;
        }
      });
      //双击
      node.addEventListener('dbclick', function (event) {
        that.OpenEditModal(node);
      });
    },
    //对连线的操作
    EveSetLink:function(link){
      var that = this;
      link.addEventListener('mouseup', function (event) {
        $(".node-menu").hide();
        if (event.button == 2) {//右键
          var offsetY = $('.wrapper-md').height() - window.innerHeight * 0.7 + 10;
          var x = event.offsetX + 40;
          var y = event.offsetY + offsetY;
          $("#linkmenu").css({
            top: y,
            left: x
          }).show();
          that.currentLink = link;
        }
      });
      //双击
      link.addEventListener('dbclick', function (event) {
        $("#changelink").css({
                    top: event.pageY,
                    left: event.pageX
                }).show();
        $("#changelink input").val(this.text);
        that.changelink(link);


      });
    },
    changelink:function(link){
      $("#changelink input").blur(function(event){
        link.text = $("#changelink input").val();
      $("#changelink").hide();
      link='';
      })
    },
    //右键菜单
    EveSet:function(){
      var that = this;
      //删除节点
      var delNode = function (flag,node) {
        var modalInstance = $modal.open({
          templateUrl: '/statics/tpl/process/workflow/delModal.html',
          controller: 'delModalCtrl',
          backdrop: 'static',
          resolve:{
            tip: function () {
              return '你确定要删除该元素吗？';
            },
            btnList: function () {
              return  [{name:'删除',type:'btn-danger'},{name:'取消',type:'btn-default'}];
            }
          }
        });
        modalInstance.result.then(function() {
          that.scene.remove(node);
          for(var i=0;i<workflowData.taskName.length;i++){
            if(workflowData.taskName[i] == node.nodeData.taskReferenceName){
              workflowData.taskName.splice(i,1)
            }
          };
          for(var i=0;i<workflowData.labelName.length;i++){
            if(workflowData.labelName[i] == node.nodeData.name){
              workflowData.labelName.splice(i,1)
            }
          }
          node = null;
        });
      };
      $("#contextmenu a,#contextmenustart a").on('click',function (e) {
        var tip = $(this).attr('tip');
        switch(tip/1){
          case 1:
            that.OpenEditModal(that.currentNode);
            break;
          case 2:
            that.currentNode.rotate += 0.5;
            break;
          case 3:
            that.currentNode.rotate -= 0.5;
            break;
          case 4:
            delNode(1,that.currentNode);
            break;
          case 5:
            $("node-menu").hide();
            break;
          case 6:
            that.DrawDynamicLink();
            break;
        }
        $(".node-menu").hide();
      });
      $("#linkmenu a").on('click',function () {
        var tip = $(this).attr('tip');
        switch(tip/1){
          case 1:
            delNode(2,that.currentLink);
            break;
          case 2:
            $("node-menu").hide();
            break;
        }
        $(".node-menu").hide();
      });
      $(document).on('click',function (event) {
          if (navigator.userAgent.indexOf('Firefox') >= 0){
              var oEvent = arguments.callee.caller.arguments[0] || event;
              oEvent.cancelBubble = true;
          }
        event.stopPropagation();
        $(".node-menu").hide();
      });
      var _target;
      //拖拽效果
      $('.task-tab>div.item').draggable({
        helper:"clone",
        start: function(){
          _target = $(this);
        },
        stop: function(){
          // var _dragIcon = _target.find('.node-img');
          // _dragIcon.animate({width:0,height:1,opacity: 0},100,function(){
          //   _target.attr('style','position: relative;');
          //   _dragIcon.removeAttr('style');
          // });
        }
      });
      $('#content').droppable({
        drop:function(e){
          var offset = $('#canvas').offset();
          var x = e.pageX - offset.left - 40;
          var y = e.pageY - offset.top - 40;
            that.AddNode(x,y,_target.context.innerText,_target.attr('image'),'Bottom_Center',_target.attr('type'));
        }
      });
    }
  };
   return NewTaskService;
}]);
app.controller('NewTaskCopyCtrl', ['$scope','workflowData', '$modal','$rootScope', '$stateParams', '$state','NewTaskService', 'httpLoad',
  function ($scope,workflowData, $modal,$rootScope, $stateParams, $state, NewTaskService, httpLoad) {
    $rootScope.moduleTitle = '流程管控 > 流程编排 >添加编排';//定义当前页
    $rootScope.link = '/statics/css/task.css';//引入页面样式
    var task = new NewTaskService();
    $scope.task = task;
    $scope.name_Data={"git_clone":"git克隆","code_scan":"代码扫描","package":"代码打包","build_image":"构建镜像","image_scan":"镜像扫描","push_image":"推送镜像","deploy":"镜像部署","check_out":"svn检出"}
    //获取模板列表
    var getTplList = function(template){
      httpLoad.loadData({
        url: '/task/graph/list',
        data:{
          params:JSON.stringify([{"param":{"kind":template},"sign":"EQ"}]),
          simple:true
        },
        noParam: true,
        success:function(data){
          if(data.success){
            $scope.tplListData = data.data.rows;
          }
        }
      })
    };

    //获取编辑数据
    var getEditData = function(){
      //对编辑数据进行处理回现
      httpLoad.loadData({
        url: '/workflow/workflowDefJSON',
        data: {name: $stateParams.name,version: $stateParams.version},
        method:'GET',
        success:function(data){
          if(data.success){
            $scope.remark = data.remark;
            var workflowJson = angular.fromJson(data.data.workflowJson);
            $scope.rowdata = workflowJson;
            $scope.rowdata.id = data.data.id;
            task.DrawNode(workflowJson.postData);
          //task.DrawNode(data.data.ceshi);
          }
        }
      })
    };
    //如果为编辑，进行赋值
    if ($stateParams.name) {
          $rootScope.moduleTitle = '流程管控 > 流程编排 >编辑编排';//定义当前页
          getEditData()
        }
    $scope.nodeLists = []; //节点集合;
    $scope.nodeListAdd=[];
    $scope.addBasename = function(postData){
      var  nodes = postData.nodes;
      var jobNameAry = [];
      if(!task.selectItem.nodeType){
        nodes.forEach(function(item){
          if(item.nodeType == "" || nodeType == ""){
            jobNameAry.push(item)
          }
        })
      }
    }
//分支集合
    $scope.selectDateF =function(item){
      var join = [];
      var links = task.LinkLists;
      var jiedian = item.nodeData;
      $scope.nodeLists.push(item.id);
      jiedian.forkTasks = [];
        var allarry = []
      join.push(jiedian);
      var duLength = item.outLinks;
      for(var i=0;i<duLength.length;i++){
        var a=[];
        var chuItem = duLength[i].nodeZ.id;
        for(var m=0;m<links.length;m++){
          if(chuItem == links[m].nodeAid){
            if(links[m].item.nodeA.nodeType  == "JOIN"){
                join.push(links[m].item.nodeA)
            }else if(links[m].item.nodeA.text  == "FORK_JOIN"){
            var o =  $scope.selectDateF(links[m].item.nodeA);
              a.push(o[0]);
              a.push(o[1].nodeData)
              chuItem = o[1].id;
              m=0
            }else if(links[m].item.nodeA.nodeType  == "DECISION"){
              var o =  $scope.selectDateS(links[m].item.nodeA);
              a.push(o)
              chuItem = 0;
              m=0
            }else if(links[m].item.nodeA.inLinks.length  == duLength.length){
              if(links[m].item.nodeA.nodeType  == "JOIN"){
                join.push(links[m].nodeZid)
              }else{
                $scope.pop("分支后面必须有合并","error");
                return;
              }
            }else{
              a.push(links[m].item.nodeA.nodeData);
              $scope.nodeLists.push(links[m].item.nodeA.id)
              chuItem = links[m].nodeZid;
              m=0;
            }
          }
        }

        jiedian.forkTasks.push(a) ;
      }
       console.log(join)
        return join
    }
    //选择流集合
    $scope.selectDateS =function(item){
      var links = task.LinkLists;
      var jiedian = item.nodeData;
      $scope.nodeLists.push(item.id);
      jiedian.decisionCases =   {}
      var duLength = item.outLinks;
      for(var i=0;i<duLength.length;i++){
          var chuItem = duLength[i].nodeZ.id;
          var a ;
          if(duLength[i].text){
            a= jiedian.decisionCases[duLength[i].text] = [];
          }else{
            a= jiedian.decisionCases[i] = [];
          }
        for(var m=0;m<links.length;m++){
          if(chuItem == links[m].nodeAid){
            if(links[m].item.nodeA.nodeType  == "FORK_JOIN"){
            var o =  $scope.selectDateF(links[m].item.nodeA);
              a.push(o[0]);
             // a.push(o[1].nodeData)
              chuItem = o[1].id;
              m=0
            }else if(links[m].item.nodeA.nodeType  == "DECISION"){
              var o =  $scope.selectDateS(links[m].item.nodeA);
              a.push(o)
              chuItem = 0;
              m=0
            }else{
              a.push(links[m].item.nodeA.nodeData);
              $scope.nodeLists.push(links[m].item.nodeA.id)
              chuItem = links[m].nodeZid;
              m=0;
            }
          }
        }
        //去掉选择流的合并点
        for(var p = a.length-1;p>=0;p--){
          if(a[p]){
            if(a[p].inLinks>1&&a[p].type !="JOIN"){
              for(var w = p;w<a.length;w++){
                $scope.nodeListAdd.push(a[w]._id);//把合并点及以后的点加入到数组里
              }
              a.splice(p,a.length-p);
              console.log($scope.nodeListAdd)
            }else{
              $scope.nodeListAdd.push()
            }
          }
        }
        //jiedian.push(a);
      }
       console.log(jiedian)
        return jiedian
    }
    //根据数据流组合数据
    $scope.streamDate =function(dataAry,postData){
      var nodesArr =postData.nodes;
      var endNode = task.scene.childs[1];
        var tasks = [];
      //去掉重复项
        for(var i=0;i<$scope.nodeLists.length;i++){
          for(var j = 0;j<$scope.nodeListAdd.length;j++){
            if($scope.nodeListAdd[j]==$scope.nodeLists[i]){
              $scope.nodeLists.splice(i,1);
            }
          }
        }
        console.log($scope.nodeLists)
      //所有点去掉组成数据的，剩下的点
      nodesArr.splice(0,2);
      for(var ia=0;ia<nodesArr.length;ia++){
        for(var jc = 0;jc<$scope.nodeLists.length;jc++){
          if(nodesArr[ia]){
            if($scope.nodeLists[jc]==nodesArr[ia].nodeId){
              if(nodesArr[ia].data.type =="FORK_JOIN"){
                continue
              }else{
                  nodesArr.splice(ia,1);
              }

            }
          }else{

          }
        }
      }
      console.log(nodesArr)
      if(task.selectItem.nodeType == "DECISION"){
        tasks.push(dataAry);
        nodesArr.forEach(function (item) {
          tasks.push(item.data)
        })
          $scope.lineData(task.selectItem.inLinks[0].nodeA,tasks)
      }else if(task.selectItem.nodeType == "FORK_JOIN"){
          nodesArr.forEach(function (item) {
              tasks.push(item.data)
          })
       //   $scope.lineData(task.selectItem.inLinks[0].nodeA,tasks)
      }else{
          $scope.lineData(endNode.inLinks[0].nodeA,tasks)
      }
        return tasks
    }
    //z直线像前获取数据
      $scope.lineData = function (node,tasks) {
          if(node.nodeType=="START"){
              return tasks;
          }else{
              if(node.nodeData){
               tasks.unshift(node.nodeData)
              }
              $scope.lineData(node.inLinks[0].nodeA,tasks)
          }

      }
    $scope.execute = function(flag){
        var postData = task.FormatData();
        var postDatacopy  = angular.copy(postData);
        console.log(postData)
      var itemArray;
      if(task.selectItem.nodeType == "DECISION"){ //选择流
        itemArray  =  $scope.selectDateS(task.selectItem);
      }else if(task.selectItem.nodeType == "FORK_JOIN"){ //分支
        var Data =  $scope.selectDateF(task.selectItem);
        itemArray = []
        itemArray.push(Data[0],Data[1].nodeData)
        console.log(itemArray);
      }else{

      }
 
      var copyData = postData;
      postData.tasks =  $scope.streamDate(itemArray,copyData);

      postData.nodes = postDatacopy.nodes;
        var modalInstance = $modal.open({
              templateUrl : '/statics/tpl/process/workflow/savelayout.html',
              controller : 'savemodelModalCtrl',
              backdrop: 'static',
              size:'lg',
                resolve : {
                  postData : function(){
                    return postData;
                  },
                  rowdata : function(){
                    return $scope.rowdata;
                  },

                }
          });
          modalInstance.result.then(function(){
            $state.go('paas.process.layout')
          },function(){});
        };
      $(".taskModel").css("height",window.innerHeight * 0.7-48);
      $(".task_pop").css("padding-top",(window.innerHeight * 0.7-48)*0.65)

  }
]);
app.controller('addScriptTaskModalCtrl', ['$scope','workflowData' , '$modal', '$stateParams', '$timeout', 'httpLoad',   'CommonData',
  function ($scope,workflowData, $modal, $stateParams, $timeout, httpLoad, CommonData) {
  var node = workflowData.node;
  var Diagramnodes = workflowData.Diagramnodes;
      $(".taskModel").css("height",window.innerHeight * 0.7 - 120)
      $scope.edit = false;
    //    node = angular.fromJson(node)
    $scope.websocketUrl='/uploadService';
    $scope.type = node.nodeType;
    $scope.diagram = {}

    var paramType = {
			type: "SIMPLE",
		};
    $scope.getSelect = function(name,inputdata){   //添加map样子的数组
      httpLoad.loadData({
           url: "/task/ouput/param",
           data:{taskName:$scope.diagram[name]},
           method:'GET',
           success:function(data){
             $scope.diagramDataB = data.data;
             if(inputdata){
            	 $scope.gridinPullName=$scope.filePath
             $scope.StringData.forEach(function(item){
               if(item.type=="T_select"){
                var out =  inputdata[item.name];
                out = out.substring(2,out.length-1)
                var outAry = out.split('.');
                item.select = outAry[0];
                item.value = outAry[2];
               }
             })
             }
           }
         })
         }
    $scope.loopSys = function(node){
    	 if(node.inLinks[0].nodeA.nodeData){
             $scope.diagram[node.inLinks[0].nodeA.nodeData.taskReferenceName] =  node.inLinks[0].nodeA.nodeData.name;
         }
      }
      //多条循环
      $scope.dubleLoop = function (node) {
        if(node.inLinks.length>1){
            $scope.loopSys(node)
        }else {
            if(node.nodeType=="START"){
                return
            }else{
                if(node.nodeData){
                    if(node.nodeType == 'DYNAMIC'){
                        $scope.diagram[node.nodeData.taskReferenceName] = node.nodeData.taskReferenceName;
                    }else if(node.nodeType == 'SIMPLE'){
                        $scope.diagram[node.nodeData.taskReferenceName] =  node.nodeData.name;
                    }
                }
                $scope.dubleLoop(node.inLinks[0].nodeA)
            }
        }


      }
    //简单小模板下获取类列表
    $scope.getJenkins = function(name,inputdata){   //添加map样子的数组
        httpLoad.loadData({
             url: "/task/param",
             data:{taskName:name},
             method:'GET',
             success:function(data){
            	  $scope.StringData = data.data;
                $scope.StringData.forEach(function(item){
                  if(item.required=="true"){
                      item.required = "required_true"
                  }else{
                      item.required = "required_false"
                  }
                })
                $scope.goload()
                if(inputdata){
                $scope.StringData.forEach(function(item){
                    if(item.type=="T_select"){
                        var out =  inputdata[item.name];
                        out = out.substring(2,out.length-1);
                        var outAry = out.split('.');
                        $scope.getSelect(outAry[0],inputdata)
                       }else if(item.type=="Query"){
                    	   item.value1=inputdata[item.name1];
                    	   item.value2=inputdata[item.name2];
                       }else{
                    	   item.select = inputdata[item.name];
                       }
                   if(item.event=='double_load'){
                	   item.paramsArr=inputdata[item.name];
                 	 
                    }
                
                })
                  $scope.goApi()
                }
             }
           })
        $scope.eleLoop(node)
     };

     $scope.eleLoop = function (node) {
         if(node.inLinks[0].nodeA.nodeType=="START"){
             return
         }else{
             if(node.inLinks[0].nodeA.nodeData){
            	  $scope.diagram[node.inLinks[0].nodeA.nodeData.taskReferenceName] =  node.inLinks[0].nodeA.nodeData.name;
             }
             $scope.eleLoop(node.inLinks[0].nodeA)
         }

     }

   $scope.getparms = function(item){
	   var parmsApi = {}
	   item = angular.fromJson(item)
	   for(var k=0;k<item.parm.length;k++){
		   obj = item.parm[k]
		   for(var i=0;i< $scope.StringData.length;i++){
               if(obj==$scope.StringData[i].name){
            	   parmsApi[obj]=$scope.StringData[i].select
               }
             }
	   }
//	   if(item.interface=="git/branches"){
// 			parmsApi.username=escape(parmsApi.username);
// 			parmsApi.password=escape(parmsApi.password);
// 		}else{
//	  			parmsApi.username=unescape(parmsApi.username);
//	  			parmsApi.password=unescape(parmsApi.password);
//
// 		}

	 
	   return parmsApi
   }
 //bu需要其他参数的api接口
   $scope.goload = function(){   //添加map样子的数组
       var data = {};
       var params = [];
       $scope.StringData.forEach(function(item){
           if(item.type=="Api"&&(item.event=='load'||item.event=='double_load')){
             if(node.nodeType=="git_clone"||node.nodeType=="check_out"){ 
            	 params.push({param: item.params, sign: 'EQ'})
            	 data={params : angular.toJson(params)}
            	 }else{
                 data={simple:true}
             }
        	   httpLoad.loadData({
                   url: item.interface,
                   data:data,
                   method:item.method,
                   noParam: true,
                   ignoreError: true,
                   success:function(data){
                       item.value  = data.data.rows;
                   }
               })
         
           }
       })
   }
 //需要把返回值放入对应模板里
   $scope.getParams = function(params){  
	   params = angular.fromJson(params)
	   paramsArr = angular.fromJson(params.paramsArr)
	   params.select = paramsArr[params.response]
           $scope.StringData.forEach(function(item){
				for(var i in paramsArr){
					 if(item.name==i){
						 item.select = paramsArr[i]
					 }
				}
             
           })
       }
//需要其他参数的api接口
  $scope.goApi = function(){   //添加map样子的数组
          $scope.StringData.forEach(function(item){
              if(item.type=="Api"){
            	  if(!item.event){
            		  	var mastList = true
            		    var parmsApi = $scope.getparms(item)
            		    for(var p=0;p<item.mast.length;p++){
            		    	var mastEle = item.mast[p]
            		    	if(!parmsApi[mastEle]){
            		    		mastList =false;
            		    		break
            		    	}else{
            		    		
            		    	}
            		    }
            		  	if(mastList){
            		  		httpLoad.loadData({
                                 url: item.interface,
                                 data:parmsApi,
                                 method:item.method,
                                 noParam: true,
                                 ignoreError: true,
                                 success:function(data){
                                     item.value  = data.data.rows;
                                 }
                             })
            		  	}
                        
            	  }
            
              }
          })
      }
    $scope.getList = function(){   //添加map样子的数组
    	var map = [];
    	$scope.modelData.forEach(function(item){
    		if(item.name == 'inputParameters'){
    			   for(var i=0;i<item.value.length;i++){
    		            var mapList = item.value[i];
    		            if(!mapList.key){
    		                  $scope.pop('请至少输入一个参数','error');
    		                  return;
    		            }else{
    		                  map.push(mapList.key)
    		               }
    		          }
    		}else if(item.type == 'select_list'){
    			item.value = map;
    		}
    	})
     }
      if(node.nodeData){                     //对数据进行梳理，如果是新建并且没有填写过。弄成模板对应的样子
          $scope.getJenkins(node.nodeData.name,node.nodeData.inputParameters);
    }else{
          $scope.getJenkins($scope.type);
      }
    $scope.addEnvs = function(map){   //添加map样子的数组
    	map.push({key:'',value:''})
     }
    $scope.removeEnv = function(key){
      if(key.length == 1) return $scope.pop('请至少添加一组','error');
      key.splice(key.length-1,1);
    }
    $scope.addList = function(map){  //添加list样子的数组
            map.push("")
     }
    $scope.removeList = function(key){
      if(key.length == 1) return $scope.pop('请至少添加一组','error');
      key.splice(key.length-1,1);
    }
    //保存按钮
    $scope.ok = function () {

    $scope.param = {};
        var inputParameters = {};
        if($scope.StringData){
            for(var n=0;n<$scope.StringData.length;n++){
                var item = $scope.StringData[n];
                if(item.type=="T_select"){
                    inputParameters[item.name] = '${'+item.select+'.output.'+item.value+'}';
                }else if(item.type=="file"){
                    inputParameters[item.name] = $scope.filePath;
                }else if (item.type=="Api") {
                    if(item.event =="confirm"){
                        var mastList = true
                        var parmsApi = $scope.getparms(item)
                        for(var p=0;p<item.mast.length;p++){
                            var mastEle = item.mast[p]
                            if(!parmsApi[mastEle]){
                                mastList =false;
                                break
                            }else{
                            }
                        }
                        $scope.itemName = item.name;
                        if(mastList){
                            httpLoad.loadData({
                                url: item.interface,
                                data:parmsApi,
                                method:item.method,
                                noParam: true,
                                success:function(dataid){
                                    if (dataid.success) {
                                        inputParameters[$scope.itemName] = dataid.data;
                                        node.nodeData = angular.extend($scope.param,paramType);
                                    }
                                }
                            })
                        }
                    }else{
                        inputParameters[item.name] = item.select;
                    }
                }else if (item.type =="warn") {
                }else if (item.escape) {
                    item.select = unescape(item.select);
                    inputParameters[item.name] = item.select;
                }else{
                    inputParameters[item.name] = item.select;
                }
            }
        }
        $scope.param.name = $scope.type;
        var timestamp
        if(node.nodeData){
            var name = node.nodeData.taskReferenceName.split('_');
            timestamp= name[name.length-1];
        }else{
            timestamp= Date.parse(new Date());
        }
        $scope.param.taskReferenceName = $scope.param.name+"_"+timestamp;
        workflowData.taskName.push($scope.param.taskReferenceName);
        $scope.param.inputParameters = inputParameters;

    node.nodeData = angular.extend($scope.param,paramType);
        $scope.pop("保存成功")
    }

  }
]);
//保存
app.controller('savemodelModalCtrl', ['$scope', '$modalInstance', '$modal', '$stateParams', '$timeout','$state',  'httpLoad',  'postData', 'rowdata',
  function ($scope, $modalInstance, $modal, $stateParams, $timeout,$state,  httpLoad, postData, rowdata) {
    var editObj = ['name','version','inputParameters','outputParameters', 'description'];
    var url ="/workflow/workflowDef";
    var id;
    //健康检查
  $scope.enabled=false;
  $scope.enabledData={true:"是",false:"否"}
    $scope.addData = {};
    $scope.modalName = "保存编排";
    $scope.name = $stateParams.name;
    $scope.inputParameters = [''];
    $scope.outputParameters = [{keys:'',values:''}];
    $scope.addEnvs = function(){
                $scope.inputParameters.push('')
     }
     $scope.addLabels = function(){
        //做验证-》只有上面用户组有用户下面才可以继续添加用户组
                 $scope.outputParameters.push({keys:'',values:''})
      }
     $scope.removeEnv = function(key){
       if($scope.inputParameters.length == 1) return $scope.pop('请至少添加一组','error');
       $scope.inputParameters.splice(key,1);
     }
     $scope.removeLabel = function(key){
       if($scope.outputParameters.length == 1) return $scope.pop('请至少添加一组','error');
       $scope.outputParameters.splice(key,1);
     }
     //
     httpLoad.loadData({
         url: '/workflow/input/param',
         data:{tasks:postData.tasks},
         method:'GET',
         success:function(data){
           if(data.success){
        	   $scope.inputParam = data.data;
           }
         }
       })
     if(rowdata){
    	 url ="/workflow/update/workflowDef";
    	 id = rowdata.id;
       var data = rowdata;
       for (var a in editObj) {
           var attr = editObj[a];
           $scope.addData[attr] = data[attr];
           var mapObj = [];
          //  var maplist = $scope.addData.outputParameters;
          //  for(var i in maplist){
        	//    mapObj.push({key:i,value:maplist[i]})
          //    }
           $scope.outputParameters = 	mapObj;
       }
       if($scope.outputParameters){
    	   $scope.outputParameters.push({key:"",value:""})
       }
     }
    //保存按钮
    $scope.ok = function () {

      var outputParameters = {};

      // for(var j=0;j<$scope.outputParameters.length;j++){
      //   var items = $scope.outputParameters[j];
      //   if((!items.keys&&items.values)||(items.keys&&!items.values)){
      //         $scope.pop('请添加完整的输出项','error');
      //         return;
      //      }else if(!items.keys&&!items.values){
      //        continue
      //      }else{
      //   	   outputParameters[items.keys] = items.values;
      //      }
      // }
           if(!postData) return;

           var data = {
             name: $scope.addData.name,
             version:$scope.addData.version,
             inputParameters: $scope.inputParameters,
             outputParameters: outputParameters,
              postData:postData,
             description:$scope.addData.description
           };
           var workflow = {
                   id: id,
                   workflowId:$stateParams.workflowId,
                   status: $stateParams.status,
                 };
           var params={
        		   data:data,
        		    tasks:postData.tasks,
        		    workflow:workflow,
        		  
           }
           httpLoad.loadData({
             url: url,
             data:params,
             method:'POST',
             success:function(data){
               if(data.success){
                   $scope.pop(data.message);
                  // if($scope.isSetHigh){
                   if(!rowdata){
                     $scope.pop("正在启动······");
                     var row = {
                       name: $scope.addData.name,
                      version:$scope.addData.version,}
                     $scope.dismiss(row);
                   }else{
                       $modalInstance.close();
                   }
               }
             }
           })
    }
    $scope.dismiss = function (row) {
      httpLoad.loadData({
          url:'/workflow/start',
          method:'POST',
          data: {name:row.name,version:row.version,input:{}},
          success:function(data){
              if(data.success){
                $scope.pop(data.message);
                  $modalInstance.close();
               $state.go('paas.process.layout')

              }
          }
      });
    }
    $scope.cancel = function () {
      $modalInstance.dismiss('cancel');
    };
  }
]);
})();