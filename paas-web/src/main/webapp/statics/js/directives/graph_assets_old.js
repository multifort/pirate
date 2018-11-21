/**
 * Created by Zhang Haijun on 2016/12/27.
 */
app.directive('ngSimpleAssetsGraph', [ function () {
	return {
		restrict: 'EA',
		templateUrl:'/statics/tpl/dashboard/topologyDire.html',
		scope:{
			nodeData:'='
		},
		link: function (scope, element, attrs) {
			var SimpleTopology = function () {
				this.InitContent();
			}
			SimpleTopology.prototype = {
				InitContent:function(){
					this.scene = new JTopo.Scene();
					this.scene.background = '/statics/img/topology/bg.jpg';
					var canvas = document.getElementById('simplecanvas');
					canvas.height = $(element).height();
					canvas.width = $(element).width();
					this.stage = new JTopo.Stage(canvas);
					this.stage.add(this.scene);
					this.stage.wheelZoom = 1.15;//鼠标缩放
				},
				//初始绘制节点
				DrawNode:function(data){
					var that = this;
					 that.scene.clear();
					var imgMap = {
						Computer:'other.jpg',
						Storage:'storage.png',
						DB:'db.png',
						Middleware:'middle.png',
						LB:'lb.png',
						Switch:'switch.png',
						Router:'router.png',
						App:'app.png'
					};
					var  relationMap = {
						Contain:'包含',
						Running:'运行',
						Usage:'使用',
						Connect:'连接',
						Provide:'提供'
					};
					var cloudNode = that.AddNode(data.targetName,imgMap[data.targetCategory],$(element).width()/2,200);
					data.relations.forEach(function(item){
						var node =  that.AddNode(item.targetName,imgMap[item.targetCategory]);

						that.AddLink(cloudNode, node, relationMap[item.relation]);
					});
					JTopo.layout.layoutNode(this.scene, cloudNode, true);
					this.scene.addEventListener('mouseup', function(e){
						if(e.target && e.target.layout){
							JTopo.layout.layoutNode(that.scene, e.target, true);
						}
					});
				},
				//添加节点
				AddNode:function(name,img, x, y){
					var node = new JTopo.Node(name);
					if(x){
						node.setLocation(x, y);
					}else{
						node.setLocation(this.scene.width * Math.random(), this.scene.height * Math.random());
					}
					node.layout = {type: 'circle', radius:200};
					node.setImage('/statics/img/dashboard/infrastructure/'+img, false);
					node.Image = img;
					node.fontColor = '88,102,110';//设置文字颜色
					node.setSize(80,80);//设置图标大小
					this.scene.add(node);
					return node;
				},
				//添加连线
				AddLink:function(nodeA,nodeZ,str){
					var link = new JTopo.Link(nodeA, nodeZ, str);
					link.lineWidth = 3;//线宽
					link.bundleGap = 20;//线条之间的间隔
					link.arrowsRadius = 10;//箭头大小
					link.textOffsetY = 3;//文本偏移量（向下3个像素）
					link.fontColor = '88,102,110';//字体颜色
					link.strokeColor = '0, 200, 255';//线条颜色
					this.scene.add(link);
				}
			};
			var Topology = new SimpleTopology();
			
            scope.$watch('nodeData',function () {
            	
            	Topology.DrawNode(scope.nodeData)
            })
		}
	}
}]);
