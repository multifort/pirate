angular.module('app').directive('easyTree',
   ['$rootScope', '$timeout', 'httpLoad', function ($rootScope,$timeout,httpLoad) {
       return {
           restrict: 'AE',
           transclude: true,
           scope : {
              treeData        : '=',
              idData          : '=',
			  identification   : '=',
			  id               : '='
           },
           link: function (scope, element, attrs) {
        	 var id = scope.idData,noParam;
			 //判断是租户授权还是角色授权，新增租户的时候授权
			 var identification = scope.identification,url;
        	 scope.$watch('treeData',function(newValue,oldValue){
				identification = scope.identification;
      		    element.tree({
                   data: scope.treeData,
 	       		   checkbox:true,
                   animate: true,
                   onBeforeExpand: function(node,param){
					   if(identification=='tenant'){
						   url = '/tenant/auths';
						   noParam = false;
						   dataPost = {parentId:node.id,id:id};
						   var treeId = 'myTree';
					   }
					   if(identification=='createTenant'){
						   url = '/auth/list';
						   noParam = true;
						   dataPost = {parentId:node.id};
						   var treeId = 'myTree';
					   }
					   if(identification=='role'){
						   url = '/role/auths';
						   noParam = false;
						   dataPost = {parentId:node.id,id:id};
						   var treeId = 'myTree1';
					   }

                       //$('#myTree').tree('options').url = '/resources/data/operation/permission/test.json?parentId='+node.id+'&id='+node.id;
                	   var tg = $(this);
                	   if(node.children=="[]") node.children=[];
                       if(node&&node.children.length==0){
						   httpLoad.loadData({
							   url: url,
							   method:'POST',
							   data: dataPost,
							   noParam:noParam,
							   success:function(data){
								   if(data.success&&data.data){
									   $('#'+treeId).tree('append',{
										   parent: node.target,
										   data: data.data
									   });
									   $('#'+treeId).tree('collapse', node.target);
									   $('#'+treeId).tree('expand', node.target);
								   }
							   }
						   });
                        }
                        //return false;
                   },
 	   		       onLoadSuccess: function(node, data){
 	   		    	
 	   		       },
	 	   		   loadFilter: function(rows,parent){
			 	   		var nodes = [];
			 	   		// get the top level nodes
			 	   		for(var i=0; i<rows.length; i++){
			 	   			var row = rows[i];
			 	   			var state = 'open';
			 	   			//if (!exists(rows, row.parentId)){
			 	   				if(row.children){
			 	   					state = 'closed';
			 	   					if(row.children=="[]") row.children=[];
			 	   				} else state = 'open';
			 	   			//}
				 	   		nodes.push({
		 	   					id:row.id,
		 	   					text:row.name,
		 	   				    parentId:row.parentId,
		 	   				    children:row.children,
		 	   				    checked:row.checked,
		 	   				    state:state
		 	   				});
			 	   		}
			 	   		return nodes;
	 	   	       },
				   onCheck: function (node, checked) {
						/*if (checked) {
							var parentNode = $("#myTree").tree('getParent', node.target);
							if (parentNode != null) {
								$("#myTree").tree('check', parentNode.target);
							}
						} else {
							var childNode = $("#myTree").tree('getChildren', node.target);
							if (childNode.length > 0) {
								for (var i = 0; i < childNode.length; i++) {
									$("#myTree").tree('uncheck', childNode[i].target);
								}
							}
						}*/
				   }
      		   }); 
        	 });                	   
           }
       };
}]);