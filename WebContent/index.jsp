<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insert title here</title>
</head>
<body>
<h1>iPhone 11 !!!  1元秒杀！！！
</h1>


<form id="msform" action="${pageContext.request.contextPath}/doseckill">
	<input type="hidden" id="prodid" name="prodid" value="0101">
	<input type="button"  id="miaosha_btn" name="seckill_btn" value="秒杀点我"/>
</form>

</body>
<script  type="text/javascript" src="${pageContext.request.contextPath}/script/jquery/jquery-3.1.0.js"></script>
<script  type="text/javascript">
$(function(){
	$("#miaosha_btn").click(function(){	 
		var url=$("#msform").attr("action");
	     $.post(url,$("#msform").serialize(),function(data){
     		if(data=="0"){
    			alert("抢光了" );
    			$("#miaosha_btn").attr("disabled",true);
    		}else if(data=="1"){
    			alert("该用户已经秒杀过，请勿重复秒杀" );
    		}else if(data=="2"){
    			alert("秒杀尚未开始" );
    		}else if(data=="200"){
    			alert("秒杀成功!!!" );
    		}
		} );    
	})
})
</script>
</html>