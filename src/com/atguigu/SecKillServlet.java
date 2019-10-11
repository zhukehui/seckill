package com.atguigu;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.soap.AddressingFeature.Responses;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;


/**
 * Redis+LUA脚本解决 秒杀问题
 * 
 * 
 */
public class SecKillServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	String secKillScript ="local userid=KEYS[1];\r\n" + 
			"local prodid=KEYS[2];\r\n" + 
			"local qtkey='sk:'..prodid..\":qt\";\r\n" + 
			"local usersKey='sk:'..prodid..\":usr\";\r\n" + 
			"local userExists=redis.call(\"sismember\",usersKey,userid);\r\n" + 
			"if tonumber(userExists)==1 then \r\n" + 
			"   return 2;\r\n" + 
			"end\r\n" + 
			"local num= redis.call(\"get\" ,qtkey);\r\n" + 
			"if tonumber(num)<=0 then \r\n" + 
			"   return 0;\r\n" + 
			"else \r\n" + 
			"   redis.call(\"decr\",qtkey);\r\n" + 
			"   redis.call(\"sadd\",usersKey,userid);\r\n" + 
			"end\r\n" + 
			"return 1" ;
	//通过LUA脚本解决资源争夺问题
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		Jedis jedis = new Jedis("192.168.100.166", 6379);
		//1、获取请求参数
		String prodid = req.getParameter("prodid");
		//虚拟随机创建一个用户id
		String usrid = (int)(Math.random()*100000)+"";
		//加密脚本字符串
		String sha1 = jedis.scriptLoad(secKillScript);//jedis可以加载LUA 字符串脚本
		Object result = jedis.evalsha(sha1, 2, usrid , prodid);//当前行才会将多个指令发送给redis执行
		//解析LUA脚本执行后的返回结果
		String res = (long)result+"";
		if("0".equals(res)) {
			resp.getWriter().write("0");
			System.err.println("库存不足!!!!");
		}else if("1".equals(res)) {
			resp.getWriter().write("200");
			System.err.println("秒杀成功!!!!");
		}else {
			resp.getWriter().write("2");
			System.err.println("已经秒杀过!!!!");
		}
		jedis.close();
	}
	// http://192.168.100.1:8080/seckill/doseckill 
	//	请求方式必须是post , 必须提交请求参数  prodid = 0101
	/*protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//1、获取请求参数
		String prodid = request.getParameter("prodid");
		//虚拟随机创建一个用户id
		String usrid = (int)(Math.random()*100000)+"";
		
		//拼接去redis中查询数据的键：redis中的库存仍未初始化
		String prodqtKey = "sk:"+prodid+":qt"; // String来保存库存数量
		String usrsKey = "sk:"+prodid +":usr";//拼接必须使用商品id    key存的用户集合使用的是set 
		Jedis jedis = new Jedis("192.168.100.166", 6379);
		//2、在redis中判断用户是否秒杀过
		Boolean flag = jedis.sismember(usrsKey, usrid);
		if(flag) {//用户已存在
			response.getWriter().write("1");
			System.err.println("用户已经秒杀过!!!!");
			jedis.close();
			return;//结束当前方法
		}
		//使用乐观锁 观察库存值
		jedis.watch(prodqtKey);
		//3、判断库存是否足够
		String prodqtStr = jedis.get(prodqtKey);
		if(prodqtStr==null) {
			response.getWriter().write("2");
			System.err.println("秒杀尚未开始!!!!");
			jedis.close();
			return;
		}
		int prodqtNum = Integer.parseInt(prodqtStr);
		if(prodqtNum<=0) {
			response.getWriter().write("0");
			System.err.println("库存不足!!!!");
			jedis.close();
			return;
		}
		//以下两个操作使用事务组队
		Transaction transaction = jedis.multi();
		//可以执行开始秒杀业务逻辑
		//4、将用户存到秒杀集合中
		transaction.sadd(usrsKey, usrid);
		//5、减库存
		transaction.decr(prodqtKey);
		//如果是组队的事务，必须手动调用exec才能让队列执行
		List<Object> list = transaction.exec();
		if(list==null || list.size()==0) {
			System.err.println("组队失败!!!!");
			jedis.close();
			return;
		}
		System.err.println("秒杀成功!!!!");
		response.getWriter().write("200");
		jedis.close();
	}*/
	

}
