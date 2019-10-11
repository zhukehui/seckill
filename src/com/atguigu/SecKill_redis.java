package com.atguigu;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.LoggerFactory;

import ch.qos.logback.core.rolling.helper.IntegerTokenConverter;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.ShardedJedisPool;
import redis.clients.jedis.Transaction;



public class SecKill_redis {
	
	private static final  org.slf4j.Logger logger =LoggerFactory.getLogger(SecKill_redis.class) ;

	public static void main(String[] args) {
 
 
		Jedis jedis =new Jedis("192.168.154.133",6379);
		
		System.out.println(jedis.ping());
	 
		jedis.close();
		
  
		
		
			
	}
	
	
	public static boolean doSecKill(String uid,String prodid) throws IOException {
		
		String qtkey = "sk:"+prodid+":qt" ;
		
		String usrkey = "sk:"+prodid+":usr";
		JedisPool jedisPool = JedisPoolUtil.getJedisPoolInstance();
		
		Jedis jedis = jedisPool.getResource();
		
		System.out.println("active:"+jedisPool.getNumActive()+"||waiter:"+jedisPool.getNumWaiters());
		
		//判断是否抢到
		if(jedis.sismember(usrkey, uid)) {
			System.err.println(uid+"已抢到！");
			jedis.close();
			return false;
		}
		
		jedis.watch(qtkey);
		//判断剩余库存
		String qtStr = jedis.get(qtkey);
		if(qtStr==null) {
			System.err.println("未初始化！");
			jedis.close();
			return false;
		}
		int qt = Integer.parseInt(qtStr);
		
		if(qt<=0) {
			System.err.println("已抢空！");
			jedis.close();
			return false;
			
		}
		
		//组队
		Transaction transaction = jedis.multi();
		
		//减库存
		transaction.decr(qtkey);
		
		//加人
		transaction.sadd(usrkey, uid);
		//执行
		List<Object> result = transaction.exec();
		
		if(result==null||result.size()==0) {
			System.err.println("秒杀失败！");
			jedis.close();
			return false;
		}
		
		jedis.close();
		
		System.out.println(uid+"秒杀成功！");
		return true;
	
		

	}
	

}
