app.directive('ngAuoComplete', ['httpLoad','$rootScope', function (httpLoad, $rootScope) {
  return {
    restrict: 'EA',
    template:'<input type="text" ng-model="userName" class="form-control search-input special" placeholder="请输入账号" ng-change="chageSearchUser()"/><span class="autocomplete-me" ng-click="autoComplete(1)">我</span>'+
    '<ul ng-if="userDataList.length>0" class="dropdown-menu search searchListli">'+
    '<li class="state" key={{key}} ng-repeat="(key,item) in userDataList | limitTo:8" ng-click="setName(item)" value="{{item.name}}">'+
    '	<a ng-bind-html="item.name"></a></li></ul>',
    scope:{
      userName:'=',
      userId:'='
    },
    link: function (scope, element, attrs) {
      //模糊搜索
      scope.chageSearchUser = function(flag){
        if(!scope.userName){
          scope.userDataList = [];
          return;
        }
        var param = [{param:{name:scope.userName},sign:'LK'}]
        httpLoad.loadData({
          url:'/user/list',
          method:'POST',
          noParam:true,
          loadinghide:true,
          data:{
            simple:true,
            params:JSON.stringify(param)
          },
          success:function(data){
            if(data.success){
              if(!flag) scope.userId = '';
              scope.userDataList = data.data.rows;
            }
          }
        });
      };
      scope.setName = function(item){
        scope.userName = item.name;
        scope.userId = item.id;
      };
      //自动填充创建人
      scope.autoComplete = function(){
        scope.userName = $rootScope.userData.name;
        scope.userId = $rootScope.userData.id;
        scope.chageSearchUser(1);
      };
      //键盘联想
      var keyAction = function(keyType) {
        var index = $(element).find('.searchListli li.select-search').attr("key");
        var length = scope.userDataList.length > 8 ? 8 : scope.userDataList.length;
        switch (keyType) {
          //向上
          case 40:
            if (!index) index = -1;
            index++;
            break;
          //向下
          case 38:
            if (!index) index = length;
            index--;
            if (index < 0) index = length + 1;
            break;
          //回车
          case 13:
            if (!index) return $(element).find(".searchListli").hide();;
            $(element).find(".searchListli .select-search").trigger('click');
            break;
        }
        $(element).find(".searchListli li").removeClass('select-search');
        scope.$apply(function() {
          scope.userName = $(element).find(".searchListli li").eq(index).addClass('select-search').attr('value') || searchText;
        })
      }
      $(element).find('.search-input').bind('keyup', function(e) {
        e.preventDefault();
      })
      var searchText = ''; //记录输入内容
      $(element).find('.search-input').bind('focus', function() {
        $(element).find(".searchListli").show();
        $(this).off('keyup').on('keyup', function(e) {
          if (e.which === 40 || e.which === 38 || e.which === 13) {
            e.preventDefault();
            keyAction(e.which)
          } else if (e.which !== 37 && e.which !== 39) {
            $(element).find(".searchListli li").removeClass('select-search');
            searchText = scope.userName;
          }
        });
        //阻止浏览器默认事件
        $(this).off('keydown').on('keydown', function(e) {
          if (e.which === 40 || e.which === 38 || e.which === 13) e.preventDefault();
        });
      });
      $(element).on('mouseenter', '.state', function() {
        $(element).find(".searchListli li").removeClass('select-search');
        $(this).addClass('select-search')
      });
      $(element).on('mouseleave', function() {
        $(element).find(".searchListli li").removeClass('select-search');
      });
      $(element).on('click', function(e) {
        e.stopPropagation();
        var target = $(e.target);
        if(target.is('a') || target.is('li')) $(element).find(".searchListli").hide();
      });
      $(document).on('click', function(e) {
        e.stopPropagation();
        $(element).find(".searchListli").hide();
      })
    }
  }
}]);
