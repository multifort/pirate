(function() {
  angular.module('validation.rule', ['validation']).config(['$validationProvider',function($validationProvider) {
        $validationProvider.showSuccessMessage = false;

        $validationProvider.setErrorHTML(function(msg) {
          return "<label class=\"validation-invalid text-danger\">" + msg + "</label>";
        });

        var expression = {
          required: function(value) {
            return !!value;
          },
            required_true: function(value) {
                return !!value;
            },
            required_false: function(value) {
                return true;
            },
          notRequiredOrIp: function(value) {
            if(!value){
              return true;
            } else if(value&&(/^((\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5]))?$/).test(value)){
              return true;
            }else{
              return false;
            }
          },
         notRequiredOrPort : function(value) {
            if(!value){
              return true;
            } else if(value&&( /^([1-9]|[1-9]\d|[1-9]\d{2}|[1-9]\d{3}|[1-5]\d{4}|6[0-4]\d{3}|65[0-4]\d{2}|655[0-2]\d|6553[0-5])$/).test(value)){
              return true;
            }else{
              return false;
            }
          },
            ipPart: function(value) {
                if(!value){
                    return true;
                }else if(value&&(/^(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])$/).test(value)){
                    return true;
                }else{
                    return false;
                }
            },
            network:function (value) {
                var flag=false;
                var strs= new Array(); //定义一数组
                var regIp=/^((\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5]))?$/;
                var regNum=/^(1?[0-9]|2[0-4])$/;
                strs=value.split("/"); //字符分割
                if(strs.length==2){
                    if((regIp.test(strs[0]))&&(regNum.test(strs[1]))&&((/^\d{1,2}$/).test(strs[1]))) flag=true;
                }
                return flag;
            },
            //用户管理用户工号（只能是数字和字母，且必须包含数字）
            jobNumber:/^(?![A-Za-z]+$)[0-9A-Za-z]{1,16}$/,
            createAppName: /^[0-9a-z-]+$/,         //仅支持英文小写字母、中划线、数字
          number: /^\d+$/,       //整数
          //number2: /^\d{1,4}$/,         //输入四位数
          positiveNumber: /^[1-9][0-9]*$/,        //正整数
          positiveNumber2: /^(([0-9]+\.[0-9]*[1-9][0-9]*)|([0-9]*[1-9][0-9]*\.[0-9]+)|([0-9]*[1-9][0-9]*))$/,      //正浮点数
          positionNumber: /^[A-Za-z0-9\u4e00-\u9fa5]+\-[A-Za-z0-9\u4e00-\u9fa5]+\-[A-Za-z0-9\u4e00-\u9fa5]{1,}$/,          //楼号-楼层-房间号,中间以中划线隔开,数字、字母、汉字
          specNumber: /^[0-9]+\/[0-9]+\/[0-9]{1,}$/,
          snNumber: /^[0-9a-zA-Z- ]+$/,          //支持空格、数字、字母、中划线
          warrantyNumber: /^[0-9\u4e00-\u9fa5]+$/,       //支持数字、中文
          url: /((([A-Za-z]{3,9}:(?:\/\/)?)(?:[-;:&=\+\$,\w]+@)?[A-Za-z0-9.-]+|(?:www.|[-;:&=\+\$,\w]+@)[A-Za-z0-9.-]+)((?:\/[\+~%\/.\w]*)?\??(?:[-\+=&;%@.\w_]*)#?(?:[\w]*))?)/,
          email: /^([a-zA-Z0-9_-])+@([a-zA-Z0-9_-])+((\.(com|cn|org|net)){1,2})$/,
          englishname: /^[a-zA-Z ]+$/,
          chinaname: /^[\u4e00-\u9fa5]*$/,
          floatnum: /\d+\.?\d*$/,
          account: /^[a-zA-Z0-9_]*$/,
          password: /^[\x21-\x7ea-zA-Z0-9_]{6,18}$/,
          password2: /^[\x21-\x7ea-zA-Z0-9_]{8,30}$/,
          passwords: /^[\x21-\x7ea-zA-Z0-9_]{1,18}$/,
          cellphone: /^(13[0123456789]|14[57]|15[012356789]|18[0123456789]|17[067])[0-9]{8}$/,   //手机
          telphone: /^0\d{2,3}-?\d{7,8}$/,    //座机
          phone:/(^1[3|4|5|7|8]\d{9}$)|(^0\d{2,3}-?\d{7,8}$)/ ,     //手机、座机
          date: /^(\d{4})-(0\d{1}|1[0-2])-(0\d{1}|[12]\d{1}|3[01])$/,     //yyyy-mm-dd
          idcard: /^[1-9]\d{5}[1-9]\d{3}((0\d)|(1[0-2]))(([0|1|2]\d)|3[0-1])\d{3}([0-9]|X)$/,
          hostname: /^[\u4e00-\u9fa5\w-]{1,30}$/,
          name: /^[\u4e00-\u9fa5\w-.]*$/,
          appname:/^[a-z]([a-z0-9-]*[a-z0-9])*$/,
          appgridin:/^[A-Za-z0-9_]*$/,
          evn:/^[a-zA-Z_][A-Za-z0-9-_]*$/,
          //name: /^[\u4e00-\u9fa5]{1,32}$|^[\dA-Za-z_]{1,64}$/,
          ip: /^(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])$/,
          port: function(value) {
              if(!value){
                  return true;
              } else if(value&&( /^([1-9]|[1-9]\d|[1-9]\d{2}|[1-9]\d{3}|[1-5]\d{4}|6[0-4]\d{3}|65[0-4]\d{2}|655[0-2]\d|6553[0-5])$/).test(value)){
                  return true;
              }else{
                  return false;
              }
          },
          path: /^\/.*/,
          packagepath: /^\/.*\.(zip|tar|tar.gz)$/,
          minone:function(str) {
            var reg = /\d+\.?\d*$/;
            return reg.test(str) && parseInt(str) > 0
          },
          minlength: function(value, scope, element, attrs, param) {
            return value.length >= param;
          },
          maxlength: function(value, scope, element, attrs, param) {
            var result;
            var valueStr = value.toString();
            var length = valueStr.length;
            for(var i=0;i<length;i++){
               if(valueStr.charCodeAt(i)>255){
                result = valueStr.replace(/[\u4e00-\u9fa5]/g,"aa");
               }
            }
            if(typeof result!='undefined'){
              return result.length <= parseInt(attrs.maxlength);
            }
          },
          maxvalue: function(value, scope, element, attrs, param) {
            var result;
            var valueStr = parseInt(value);
            if(typeof result!='undefined'){
              return valueStr <= parseInt(attrs.maxvalue);
            }
          },
          name_opt: function(value){
            var reg = /^[\u4e00-\u9fa5\w-.]*$/;
            return reg.test(value||'');
          },
          no_chinese: function(value){   //禁止输入中文
            var reg = /([\u4E00-\u9FA5])+/;
            var reg2 = /^[\u4e00-\u9fa5\w-.]*$/;
            return (!reg.test(value))&&(reg2.test(value));
          },
          account_opt: function(value) {
            var reg = /^[a-zA-Z0-9_]*$/;
            return reg.test(value);
          },
          url_opt: function(value) {
            var reg = /^([a-zA-Z\d][a-zA-Z\d-_]+\.)+[a-zA-Z\d-_]*$/;
            return reg.test(value);
          },
          password_opt: function(value) {
            var reg = /^([\x21-\x7ea-zA-Z0-9_]{1,18})?$/;
            return reg.test(value || '');
          },
          evn_opt: function(value) {
            if(!value) return true;
            var reg = /^[a-zA-Z_][A-Za-z0-9-_]*$/;
            return reg.test(value);
          },
          port_opt: function(value) {
            var reg = /^([0-9]|[1-9]\d|[1-9]\d{2}|[1-9]\d{3}|[1-5]\d{4}|6[0-4]\d{3}|65[0-4]\d{2}|655[0-2]\d|6553[0-5])?$/;
            return reg.test(value || '');
          },
          ip_opt: function(value) {
            var reg = /^((\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5]))?$/;
            return reg.test(value || '');
          },
          phone_opt: function(value) {
            if(!value) return true;
            var reg = /(^1[3|4|5|7|8]\d{9}$)|(^0\d{2,3}-?\d{7,8}$)|(^[48]00-?\d{7}$)/ ;
            return reg.test(value);
          },
          number_opt: function(value) {
            if(!value) return true;
            var reg = /^\d+$/ ;
            return reg.test(value);
          },
          positiveNumber_opt: function(value) {
            if(!value&&value !=0) return true;
            var reg = /^[1-9][0-9]*$/;
            return reg.test(value);
          },
          name_opt: function(value) {
            var reg = /^[\u4e00-\u9fa5\w-]*$/;
            return reg.test(value || '');
          },
          positiveNumber_opt: function(value){
            var reg = /^[1-9][0-9]*$/;
            if(!value) return true;
            else return reg.test(value);
          },
          address_opt: function(value) {
            var b1 = /^(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])$/;
            var b2 = /((([A-Za-z]{3,9}:(?:\/\/)?)(?:[-;:&=\+\$,\w]+@)?[A-Za-z0-9.-]+|(?:www.|[-;:&=\+\$,\w]+@)[A-Za-z0-9.-]+)((?:\/[\+~%\/.\w]*)?\??(?:[-\+=&;%@.\w_]*)#?(?:[\w]*))?)/;
            return (b1.test(value))||(b2.test(value));
          },
          aliyun_password:function(value){
            var a1 = /[a-z]/,
                a2 = /[A-Z]/,
                a3 = /[0-9]/,
                a4 = /[-\._~`!@#$%^&*()+={}[]|:;"'<>?,]/;
            var result,result2;
            var str = value;
            if(a1.test(str) && a2.test(str) && (a3.test(str)||a4.test(str))) result=true;
            else if(a1.test(str) &&(a2.test(str) || a3.test(str))&& a4.test(str)) result=true;
            else if(a1.test(str) &&a3.test(str)&&(a2.test(str)||a4.test(str))) result = true;
            else if(a2.test(str) && a3.test(str)&&(a1.test(str)||a4.test(str))) result = true;
            else if(a2.test(str) && a4.test(str)&&(a3.test(str)||a4.test(str))) result = true;
            else if(a3.test(str) && a4.test(str)&&(a1.test(str)||a2.test(str))) result = true;
            else result = false;

            if(value.length<8||value.length>30) result2 = false;
            else result2 = true;
            return result&&result2;
          },
          aliyun_instance:function(value){
            var a = value.substr(0,1);
            var a1 = /[a-z]/,
                a2 = /[A-Z]/,
                a3 = /[0-9]/,
                a4 = /[-._]/,
                a5 = /^[0-9a-zA-Z-._\u4e00-\u9fa5]{2,128}$/;
            var result,result2,result3;
            if(a1.test(a)||a2.test(a)||a5.test(a)) result=true;
            else result=false;

            if(a5.test(value)) result3 = true;
            else result3=false;

            return result&&result3;
          },
            nameOrEmail:function (value) {
                if((/^([a-zA-Z0-9_-])+@([a-zA-Z0-9_-])+((\.(com|cn|org|net)){1,2})$/).test(value)) return true;
                else if((/^[\u4e00-\u9fa5\w-.]*$/).test(value)) return true;
                else return false;
            }
        };

        var defaultMsg = {
          required: {
            error: '请输入内容'
          },
        required_true: {
            error: '请输入内容'
        },
            required_false: {
                error: ''
            },
          notRequiredOrPort:{
            error:"若输入请选择正确的端口格式"
          },
          notRequiredOrIp:{
            error:"若输入,请选择正确的IP格式" 
          },
            ipPart:{
                error:"请输入IP地址的最后一段，0-255的整数，注意格式"
            },
            network:{
                error:"请选择正确的网络网段格式"
            },
            jobNumber:{
                error: "只能输入数字和字母，且必须包含数字"
            },
            createAppName:{
                error: '只能输入英文小写字母、中划线、数字'
            },
          warrantyNumber: {
              error: '请输入中文、数字'
          },
          url: {
            error: '请输入正确格式的网址',
            success: 'It\'s Url'
          },
          email: {
            error: '请输入正确格式的邮箱'
          },
          minone: {
            error: 'This should be a larger than one Number',
            success: 'It\'s Number'
          },
          number: {
            error: '请输入整数',
            success: 'It\'s Number'
          },
          number_opt: {
            error: '请输入整数',
            success: 'It\'s Number'
          },
          name_opt: {
            error: '请输入中文、字母、数字、下划线或中划线或点'
          },
          url_opt: {
            error: '请输入正确格式的域名',
            success: 'It\'s Url'
          },
          no_chinese: {
            error: '请输入字母、数字、下划线或中划线或点'
          },
          positiveNumber: {
              error: '请输入正整数'
          },
          positiveNumber_opt: {
            error: '请输入正整数'
          },
          positiveNumber2: {
            error: '请输入正数'
          },
          positionNumber:{
            error: '请输入楼号-楼层-房间号，中间以中划线隔开，可以是数字、中文、字母'
          },
          specNumber:{
            error: '请输入长/宽/高，中间以斜杠隔开，可以是数字'
          },
          snNumber:{
            error: '请输入空格、数字、字母、中划线'
          },
          floatnum:{
            error: 'This should be Number',
            success: 'It\'s Number'
          },
          minlength: {
            error: 'This should be longer',
            success: 'Long enough!'
          },
          maxlength: {
            error: 'This should be shorter'
          },
          maxvalue: {
            error: 'This should be shorter'
          },
          englishname: {
            error: 'Please enter valid name entry using letters only.'
          },
          account: {
            error: '请输入数字、字母、下划线'
          },
          account_opt: {
            error: '输入信息格式有误'
          },
          password: {
            error: '密码长度至少6位，最多不超过18位'
          },
          password2: {
            error: '密码长度至少8位，最多不超过30位'
          },
          passwords: {
            error: '密码格式不符合要求'
          },
          password_opt: {
            error: '密码格式不符合要求'
          },
          evn_opt: {
            error: '请输入字母、数字、下划线，第一个不能是数字'
          },
          cellphone: {
            error: '请输入手机号码'
          },
          telphone: {
            error: '请输入正确格式的座机号码，如010-8888888'
          },
          phone: {
              error: '请输入手机号码或座机'
          },
          phone_opt: {
            error: '请输入手机号码或座机'
          },
          chinaname: {
            error: '请输入汉字'
          },
          date: {
            error: '请输入正确的日期格式'
          },
          idcard: {
            error: '请输入正确的身份证号'
          },
          hostname: {
            error: '请输入少于30个字符的正确格式的名称'
          },
          name: {
          error: '请输入中文、字母、数字、下划线、中划线或点'
          },
          appname: {
          error: '请输入小写字母、数字、中划线,第一个必须是小写字母'
          },
          appgridin: {
          error: '请输入小写字母、数字、下划线'
          },
          evn: {
          error: '请输入字母、数字、下划线，第一个不能是数字'
          },
          ip: {
            error: '请输入正确的IP地址'
          },
          ip_opt: {
            error: '请输入正确的IP地址'
          },
          port: {
            error: '请输入正确的端口号'
          },
          path: {
          error: '请输入正确的路径'
          },
          packagepath: {
            error: '请输入后缀名为.tar、.zip或.tar.gz结尾的正确路径'
          },
          port_opt: {
            error: '请输入正确的端口号'
          },
          positiveNumber_opt: {
            error: '请输入正整数'
          },
          address_opt:{
            error: '请输入正确的IP地址或网址'
          },
          aliyun_password:{
            error: '请输入8-30个字符，必须同时包含三项（大写字母，小写字母，数字和特殊字符）'
          },
          aliyun_instance:{
            error:'请输入2-128个字符，以大小写字母或中文开头，可包含数字，"_"，"."，"-"'
          },
            nameOrEmail:{
                error:'请输入正确的名称格式（中文、字母、数字、下划线、中划线或点），或正确的邮箱格式'
            }
        };

        $validationProvider.setExpression(expression).setDefaultMsg(defaultMsg);

      }


    ]);

}).call(this);
