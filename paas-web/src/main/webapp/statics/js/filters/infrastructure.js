/**
 * Created by Zhang Haijun on 2016/12/28.
 */
app.filter('infrastructure',function(){
	return function (value) {
		var str = '是';
		if(value) str = '否';
		return str;
	}
});