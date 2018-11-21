(function() {
	"use strict";
	app.filter("formatDateYM",function(){
        return function(input){
            return input.substring(0, 7);
        }
    });
	app.filter("formatDateYMD",function(){
        return function(input){
            return input.substring(0, 10);
        }
    });
})();