angular.module('app').directive('easyGrid', ['$rootScope', '$timeout', 'httpLoad',
	function ($rootScope,$timeout,httpLoad) {
       return {
           restrict: 'AEC',
           transclude: true,
           scope : {
              treeData        : '=',
			  getData    : '&',
			  add     : '&',
			  remove     : '&',
			   detail     : '&'
           },
           link: function (scope, element, attrs) {
        	 scope.$watch('treeData',function(newValue,oldValue){
        		 element.treegrid({
               	     //method: 'GET',
               	     //url:'/resources/data/operation/permission/permissionList.json',
                     data: scope.treeData,
                     dataType:'json',
   	       		     idField:'id',
   	       		     treeField:'name',
                     animate: true,
                     fitColumns:true,
                     onBeforeExpand: function(row,param){
                         if(row&&row.children1){
                           //$(this).treegrid('options').url='/resources/data/operation/permission/test.json?parentId='+row.id+'&id='+row.id;
							 httpLoad.loadData({
                     			url: '/auth/list',
                     			method: 'POST',
                     			data: {"parentId":row.id},
								noParam:true,
                     			success: function(data){
									if(data.success){
										if (row.children1){
											$('#dg').treegrid('append',{
												parent: row.id,
												data: data.data
											});
											row.children1 = undefined;
											$('#dg').treegrid('collapse', row.id);
											$('#dg').treegrid('expand', row.id);
										}
										return row.children1 == undefined;
									}
                     			},
                                error: function (XMLHttpRequest, textStatus, errorThrown) {}
                     	    });
                          }
                          //return true;
                       },
	      		       columns:[[
	  	    		        {field:'name',title:'权限名称',width:'17%',align:'left'},
	  	    		        {field:'status',title:'状态',width:'8%',align:'center'},
	  	    		        {field:'props',title:'其他属性',hidden:true},
	  	    		        {field:'gmtModify',title:'修改时间',width:'20%',align:'center'},
						    {field:'priority',title:'优先级',width:'10%',align:'center'},
						    {field:'actionUrl',title:'请求路径',align:'center',hidden:true},
						   	{field:'category',title:'权限类型',align:'center',hidden:true},
	  	    		        {field:'remark',title:'描述',width:'15%',align:'center'},
	  	    		        {field:'operate',title:'操作',width:'30%',align:'center',
	  		                  formatter: function(value,row,index){
	  		                    row.parentId = row.parentId || 0;
	  		                    var id=row.id,
									parentId=row.parentId,
									name=row.name,
									remark=row.remark||"",
									props=row.props||"",
									gmtModify=row.gmtModify||"",
									actionUrl = row.actionUrl,
									category = row.category,
									priority = row.priority;
	  		                	var html='';
	  		                    html+='<button class="btn btn-info btn-sm addName" id="'+id+'" parentId="'+parentId+'" name="'+name+'" priority="'+priority+'" remark="'+remark+'" props="'+props+'" actionUrl="'+actionUrl+'"><i class="fa fa-plus icon-font"></i><span class="icon-txt" style="font-size: 14px;padding-left: 3px;">新增</span></button>'+ '&nbsp;&nbsp;';//默认只有二级节点
	  		                    html+='<button class="btn btn-success btn-sm updateName" id="'+id+'" parentId="'+parentId+'" name="'+name+'" priority="'+priority+'" remark="'+remark+'" props="'+props+'" actionUrl="'+actionUrl+'" category="'+category+'"><i class="fa fa-pencil-square-o icon-font"></i><span class="icon-txt" style="font-size: 14px;padding-left: 3px;">编辑</span></button>'+ '&nbsp;&nbsp;';
	       	                    html+='<button class="btn btn-danger btn-sm deleteName" id="'+id+'" parentId="'+parentId+'" name="'+name+'" priority="'+priority+'" remark="'+remark+'" props="'+props+'" actionUrl="'+actionUrl+'"><i class="fa fa-trash-o icon-font"></i><span class="icon-txt" style="font-size: 14px;padding-left: 3px;">删除</span></button>'+ '&nbsp;&nbsp;';
								html+='<button class="btn btn-primary btn-sm detailName" id="'+id+'" parentId="'+parentId+'" name="'+name+'" priority="'+priority+'" remark="'+remark+'" props="'+props+'" gmtModify="'+gmtModify+'"><i class="fa fa-info icon-font"></i><span class="icon-txt" style="font-size: 14px;padding-left: 3px;">详情</span></button>';

	  		                    return html;
	  		                 }
	  	                 }
	   	   		       ]],
	   	   		       onLoadSuccess: function(row, data){
	   	   		    	 $(".datagrid-body").on('click','button.addName',function($event){
	   	   		    		$event.stopPropagation();
	   	   		    		var _this =$(this);
	   	   		    		flag= 1;
	   	   		    		item = {
	   	   		    			id :  _this.attr("id"),
	   	   		    		    parentId : _this.attr("parentId"),
	       	   		    	    name :  _this.attr("name"),
	       	   		    	    remark :  _this.attr("remark"),
								props : _this.attr("props"),
								actionUrl : _this.attr("actionUrl"),
								priority : _this.attr("priority")
	   	   		    		};
	  	 	                scope.add({flag:flag,item:item});
	   	   		    	});
	   	   		    	$(".datagrid-body").on('click','button.updateName',function($event){
	  	 	   		    	$event.stopPropagation();
	  	   		    		var _this =$(this);
	  	   		    		flag= 2;
	  	   		    		item = {
	  	   		    			id :  _this.attr("id"),
	  	   		    		    parentId : _this.attr("parentId"),
	  	 	   		    	    name :  _this.attr("name"),
	  	 	   		    	    remark :  _this.attr("remark"),
								props : _this.attr("props"),
								actionUrl : _this.attr("actionUrl"),
								category : _this.attr("category"),
								priority : _this.attr("priority")
	  	   		    		};
	  		                scope.add({flag:flag,item:item});
	  	   		    	});
	  	 	   		    $(".datagrid-body").on('click','button.deleteName',function($event){
	  		 	   		    $event.stopPropagation();
	  	   		    		var _this =$(this);
	  	   		    		item = {
	  	   		    			id : _this.attr("id")
	  	   		    		};
	  		                scope.remove({item:item});
	  	   		    	});
					    $(".datagrid-body").on('click','button.detailName',function($event){
						   $event.stopPropagation();
						   var _this =$(this);
						   item = {
							   id : _this.attr("id")
						   };
						   scope.detail({item:item});
					    });
	   	   		       },
	   	   		       loadFilter: function(data,parentId){
	   	       				function setData(){
	   			              var todo = [];
	   			              for(var i=0; i<data.length; i++){
								if(data[i].status=="NORMAL") data[i].status = "正常";
								if(data[i].status=="ABNORMAL") data[i].status = "异常";
	   			                todo.push(data[i]);
	   			              }
	   			              while(todo.length){
	   			                var node = todo.shift();
	   			                if (node.children){
	   			                  node.state = 'closed';
	   			                  node.children1 = node.children;
	   			                  node.children = undefined;
	   			                  todo = todo.concat(node.children1);
	   			                }
	   			              }
	   			            }
	   	       				setData(data);
	   	       				return data;
	       	       	    }
	                 });
        	 });
           }
       };
}]);
