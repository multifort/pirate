app.directive('ngSpiderCommonTen',
	['$rootScope', '$timeout', 'httpLoad', function ($rootScope, $timeout, httpLoad) {
		return {
			restrict: 'EA',
			templateUrl:'/statics/tpl/process/topologyDire.html',
			scope:{
				nodeData:'=',
				changeData:"=",
				arrayTasks:"="
			},
			link: function (scope, element, attrs) {
				var NewTaskService = function () {
					$('#close-topo').click(function(){
							$('.node-topo').hide()
					})
		    this.Init();
		  }
			NewTaskService.prototype = {
		    Init:function(){
		      //初始化内容
		      this.InitContent();
		    },
		    InitContent:function(){
		      this.scene = new JTopo.Scene();
		      this.scene.background = '/statics/img/topology/bg.jpg';
		      var canvas = document.getElementById('canvas');
		      canvas.height = window.innerHeight * 0.7;
		      canvas.width = window.innerWidth * 0.80;
		      this.stage = new JTopo.Stage(canvas);
		      this.stage.eagleEye.visible = true;
		      this.stage.add(this.scene);

		    },
	  //初始绘制节点
		    DrawNode:function(data){
		      var that = this;
		      //清除节点数据
		      that.scene.clear();
		      var img = {
		        START:'start_normal.png',
		        END:'end_normal.png',
		        FORK_JOIN:'icon_branch_5.png',
		        DYNAMIC:'icon_dynamic_5.png',
		        FORK_JOIN_DYNAMIC:'icon_dynamicbranch_5.png',
		        HTTP:'icon_http_5.png',
		        EVENT:'icon_incident_5.png',
		        JOIN:'icon_merge_5.png',
		        SIMPLE:'icon_simpleness_5.png',
		        DECISION:'icon_stream_5.png',
		        WAIT:'icon_wait_5.png',
		        SUB_WORKFLOW:'icon_workflow_5.png',
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
		          that.AddNode(item.location.x,item.location.y,item.text,img[item.data.name],'Bottom_Center',item.data.type,item);
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

		      node.textPosition = textPosition;//设置文字位置
		      node.fontColor = '88,102,110';//设置文字颜色
		      node.setSize(40,40);//设置图标大小
					if(scope.changeData){
						  this.EveSetNode(node);
					}
					if(node.nodeType != 'START' && node.nodeType != 'END' && scope.arrayTasks){
							var data  = this.changeColor(node,img)
							if(data.img){
								img = data.img;
								node.taskId = data.taskId;	
							}
							
					}
					if (null != img) {
					 node.setImage('/statics/img/task/' + img, false);
					 node.Image = img;
				 }
		      this.scene.add(node);
		      return node;
		    },
				changeColor : function(node,img){
		      var arrayTasks = scope.arrayTasks;
		      var data = {}
					arrayTasks.forEach(function(item){
						if(item.referenceTaskName == node.nodeData.taskReferenceName){
							var tip = item.status;
							switch(tip){
								case "IN_PROGRESS":
									data.taskId = item.taskId;
									data.img = img.replace('5','1');
									break;
								case "CANCELED":
									data.taskId = item.taskId;
									data.img= img.replace('5','4');
									break;
								case "FAILED":
									data.taskId = item.taskId;
									data.img = img.replace('5','2');
									break;
								case "COMPLETED":
									data.taskId = item.taskId;
									data.img = img.replace('5','3');
									break;
								case "SCHEDULED":
									data.taskId = item.taskId;
									data.img = img.replace('5','5');
									break;
								case "TIMED_OUT":
									data.taskId = item.taskId;
									data.img = img.replace('5','2');
									break;
							}
						}
					})
					return data
		    },
		    //添加连线
		    AddLink:function(nodeA,nodeZ,str){
		      var link = new JTopo.Link(nodeA, nodeZ, str);
		      link.text = str;
		      link.lineWidth = 3;//线宽
		      link.bundleGap = 20;//线条之间的间隔
		      link.bundleOffset = 0; // 折线拐角处的长度
		      link.textOffsetY = 3;//文本偏移量（向下3个像素）
		      link.arrowsRadius = 10;//箭头大小
		   		link.fontColor = '0, 0, 0';//字体颜色
		      link.strokeColor = '0, 200, 255';//线条颜色
		      this.scene.add(link);
		    },

			 getDataMeg : function(node){
			      httpLoad.loadData({
			        url: '/task/task',
			        method:'GET',
			        data:{
			         taskId:node.taskId
			        },
			       
			        success:function(data){
			          if(data.success){
							  if(data.data.inputData&&data.data.inputData.password){
								  data.data.inputData.password = "*********"
							  }
			        	  $('#json-workflowDetail').jsonViewer(data.data); 
			          }
			        }
			      })
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

					if (event.button == 0) {//右键					
						if(node.taskId){
							$('.node-topo').show()
							that.getDataMeg(node)
						}			
					}
				});

			},

		};
				var NewTask = new NewTaskService();
				NewTask.DrawNode(scope.nodeData)
                scope.$watch('arrayTasks',function () {
                	$timeout(function(){
                		NewTask.DrawNode(scope.nodeData)
                	},100)
                	
                })
			}
		}
}]);
