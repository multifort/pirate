<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en" data-ng-app="app">
<head>
	<base href="/">
	<meta charset="utf-8">
	<title>云运维管理平台</title>
	<meta name="description" content="">
	<meta name="keywords" content="">
	<style>

		body{
			background:url('/statics/img/bg.png') no-repeat center center #1d1d1d;
			color:#eee;
			font-family:Corbel,Arial,Helvetica,sans-serif;
			font-size:13px;
		}

		#rocket{
			width:275px;
			height:375px;
			background:url('/statics/img/rocket.png') no-repeat;
			margin:60px auto 10px;
			position:relative;
		}

		/*	Two steam classes. */

		.steam1,
		.steam2{
			position:absolute;
			bottom:78px;
			left:50px;
			width:80px;
			height:80px;
			background:url('/statics/img/steam.png') no-repeat;
			opacity:0.8;
		}

		.steam2{

			/*	.steam2 shows the bottom part (dark version)
					 *	of the background image.
					 */

			background-position:left bottom;
		}

		hgroup{

			/* Using the HTML4 hgroup element */

			display:block;
			margin:0 auto;
			width:850px;
			font-family:'Century Gothic',Calibri,'Myriad Pro',Arial,Helvetica,sans-serif;
			text-align:center;
		}

		h1{
			color:#76D7FB;
			font-size:40px;
			text-shadow:3px 3px 0 #3D606D;
			white-space:nowrap;
		}

		h2{
			color:#9FE3FC;
			font-size:18px;
			font-weight:normal;
			padding-bottom:15px;
		}

		/* Only Needed For The Demo Page <!-- ×ªï¿½ï¿½ï¿½ï¿½×¢ï¿½ï¿½Ò»ï¿½ï¿½ï¿½Ø²ï¿½ï¿½ï¿½ï¿½Õ¼ï¿½ï¿½ï¿½ï¿½ï¿½:www.16sucai.com -->    */

		p.createdBy{
			font-size:15px;
			font-weight:normal;
			margin:50px;
			text-align:center;
			text-shadow:none;
		}

		a, a:visited {
			text-decoration:none;
			outline:none;
			border-bottom:1px dotted #97cae6;
			color:#97cae6;
		}

		a:hover{
			border-bottom:1px dashed transparent;
		}
	</style>
</head>
<body>
<div id="rocket"></div>
<hgroup>
	<h1>404您请求的页面去火星了！</h1>
	<h2>@苏州博纳讯动软件有限公司。</h2>
</hgroup>
<script src="/statics/libs/jquery/jquery/dist/jquery.min.js"></script>
<script>
	$(window).load(function(){

		// We are listening for the window load event instead of the regular document ready.

		function animSteam(){

			// Create a new span with the steam1, or steam2 class:

			$('<span>',{
				className:'steam'+Math.floor(Math.random()*2 + 1),
				css:{
					// Apply a random offset from 10px to the left to 10px to the right
					marginLeft	: -10 + Math.floor(Math.random()*20)
				}
			}).appendTo('#rocket').animate({
				left:'-=58',
				bottom:'-=100'
			}, 120,function(){

				// When the animation completes, remove the span and
				// set the function to be run again in 10 milliseconds

				$(this).remove();
				setTimeout(animSteam,10);
			});
		}

		function moveRocket(){
			$('#rocket').animate({'left':'+=100'},5000).delay(1000)
					.animate({'left':'-=100'},5000,function(){
						setTimeout(moveRocket,1000);
					});
		}

		// Run the functions when the document and all images have been loaded.

		moveRocket();
		animSteam();
	});
</script>
</body>
</html>