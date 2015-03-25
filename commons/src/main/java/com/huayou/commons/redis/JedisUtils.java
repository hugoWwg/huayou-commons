package com.huayou.commons.redis;

import org.apache.commons.beanutils.MethodUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;

import java.lang.reflect.Method;
import java.util.logging.Logger;

@Deprecated
/**
 * @author wwg
 * @deprecated use spring data redis instead
 */
public class JedisUtils {

    public static final Logger logger = Logger.getLogger(JedisUtils.class.getSimpleName());
    public static final String DEFAULT_HOST = "localhost";
    public static final int DEFAULT_PORT = Protocol.DEFAULT_PORT;
    public static final int DEFAULT_TIMEOUT = Protocol.DEFAULT_TIMEOUT;

    private static final String OK_CODE = "OK";
    private static final String OK_MULTI_CODE = "+OK";

    /**
     * 判断 是 OK 或 +OK.
     */
    public static boolean isStatusOk(String status) {
        return (status != null) && (OK_CODE.equals(status) || OK_MULTI_CODE.equals(status));
    }

    /**
     * 快速设置JedisPoolConfig, 不执行idle checking。
     */
    public static JedisPoolConfig createPoolConfig(int maxIdle, int maxActive) {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        //compatible redis v2.1
        Method method = MethodUtils.getAccessibleMethod(JedisPoolConfig.class, "setMaxTotal", int.class);
        if (null != method) {
            try {
                logger.info("invoke setMaxTotal method");
                MethodUtils.invokeExactMethod(poolConfig, "setMaxTotal", maxIdle);
            } catch (Exception ignored) {
            }
        }
        poolConfig.setTimeBetweenEvictionRunsMillis(-1);
        return poolConfig;
    }

    /**
     * 快速设置JedisPoolConfig, 设置执行idle checking的间隔和idle时间.
     */
    public static JedisPoolConfig createPoolConfig(int maxIdle, int maxActive, int checkingIntervalSecs,
                                                   int idleTimeSecs) {
        JedisPoolConfig poolConfig = createPoolConfig(maxIdle, maxActive);
        poolConfig.setTimeBetweenEvictionRunsMillis(checkingIntervalSecs * 1000);
        poolConfig.setMinEvictableIdleTimeMillis(idleTimeSecs * 1000);
        return poolConfig;
    }

    /**
     * 退出然后关闭Jedis。
     */
    public static void closeJedis(Jedis jedis) {
        if (jedis.isConnected()) {
            try {
                try {
                    jedis.quit();
                } catch (Exception ignored) {
                }
                jedis.disconnect();
            } catch (Exception ignored) {
            }
        }
    }
}
