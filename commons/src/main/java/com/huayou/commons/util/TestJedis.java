package com.huayou.commons.util;

import com.google.common.base.Stopwatch;
import com.huayou.commons.redis.JedisUtils;
import com.google.common.collect.Maps;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @Author : hugo
 * @Date : 15/3/6 上午9:53.
 */
public class TestJedis {

    public static void main(String[] args) {
        new TestJedis().test();
    }

    public void test() {
        // 设置Pool大小，设为与线程数等大，并屏蔽掉idle checking
        JedisPoolConfig poolConfig = JedisUtils.createPoolConfig(50, 50);
        JedisPool jedisPool = new JedisPool(poolConfig, "127.0.0.1", 6379, 10000, "hy", 0);
        Jedis js = null;
        boolean broken = false;
        try {
            js = jedisPool.getResource();

//            aboutSet(js);
//            aboutZset(js);
//            aboutHash(js);
            aboutPipelining(js);
        } catch (JedisConnectionException e) {
            broken = true;
            throw e;
        } finally {
            if (js != null) {
                try {
                    if (broken) {
                        jedisPool.returnBrokenResource(js);
                    } else {
                        jedisPool.returnResource(js);
                    }
                } catch (Exception e) {
                    JedisUtils.closeJedis(js);
                }
            }
        }

    }

    /**
     * 关于set 无须唯一
     */
    public void aboutSet(Jedis js) {
        js.sadd("s1", "顺序3");
        js.sadd("s1", "a");
        js.sadd("s1", "b");
        js.sadd("s1", "1");
        js.sadd("s1", "蛤蛤蛤");
        js.sadd("s1", "2");
        js.sadd("s1", "so waht？");
        js.sadd("s1", "%^");
        js.sadd("s1", "顺序1");
        js.sadd("s1", "乱码吗？");
        js.sadd("s1", "顺序2");
        //移除元素
        js.srem("s1", "蛤蛤蛤");

        Set<String> s = js.smembers("s1");
        for (String str : s) {
            System.out.println(str);
        }
    }

    /**
     * zset(sorted set 有序集合)
     * 有2中编码类型:ziplist,skiplist,当zset中数据较多时,将会被重构为skiplist
     * skiplist JAVA里对应实现为concurrentskiplistmap
     * skiplist这个数据结构好处就是来解决HASH冲突的时候HASH表效率降低（如key一样时）
     */
    public void aboutZset(Jedis js) {
        js.zadd("zs", 92, "张三1");
        js.zadd("zs", 93, "张三7");
        js.zadd("zs", 94, "张三5");
        js.zadd("zs", 87, "张三9");
        js.zadd("zs", 66, "张三");
        js.zadd("zs", 19, "张三0");
        //返回key名称为zs的zset（元素已按score从小到大排序）中的index从start到end的所有元素
        Set<String> sets = js.zrange("zs", 0, -1);
        System.out.println(sets);
    }

    public void aboutHash(Jedis js) {
        Map m = Maps.newHashMap();
        m.put("1", "t");
        m.put("2", "ttt");
        m.put("username", "老王");
        m.put("password", "123456");
        m.put("age", "79");
        m.put("sex", "man");
        js.hmset("m", m);
        List<String> v = js.hmget("m", "username", "age");//返回名称为key的hash中field i对应的value
        List<String> v1 = js.hmget("m", "sex");
        System.out.println(v);
        System.out.println(v1);
        js.hdel("m", "username");//删除map中的某一个键的键值对
    }

    /**
     * 管道(Pipelining)
     * 有时，我们需要采用异步方式，一次发送多个指令，不同步等待其返回结果。
     * 这样可以取得非常好的执行效率。这就是管道
     */
    public void aboutPipelining(Jedis js) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        Pipeline pipeline = js.pipelined();
        for (int i = 0; i < 9999; i++) {
            pipeline.set("key" + i, "value" + i);
        }
        List<Object> list = pipeline.syncAndReturnAll();
        System.out.println(list.size());
        System.out.println(stopwatch.elapsed(TimeUnit.MILLISECONDS));
        js.disconnect();
    }


}
