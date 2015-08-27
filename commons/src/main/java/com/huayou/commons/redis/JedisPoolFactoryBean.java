package com.huayou.commons.redis;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

@Deprecated
/**
 * @author wwg
 * use spring data redis instead
 */
public class JedisPoolFactoryBean implements FactoryBean<JedisPool> {

    private static Logger logger = LoggerFactory.getLogger(JedisPoolFactoryBean.class);
    private String host;
    private int port;
    private int timeout;
    private String password;
    private int database;
    private int threadCount;
    private int checkingIntervalSecs;
    private int idleTimeSecs;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getDatabase() {
        return database;
    }

    public void setDatabase(int database) {
        this.database = database;
    }

    public int getThreadCount() {
        return threadCount;
    }

    public void setThreadCount(int threadCount) {
        this.threadCount = threadCount;
    }

    public int getCheckingIntervalSecs() {
        return checkingIntervalSecs;
    }

    public void setCheckingIntervalSecs(int checkingIntervalSecs) {
        this.checkingIntervalSecs = checkingIntervalSecs;
    }

    public int getIdleTimeSecs() {
        return idleTimeSecs;
    }

    public void setIdleTimeSecs(int idleTimeSecs) {
        this.idleTimeSecs = idleTimeSecs;
    }

    @Override
    public JedisPool getObject() throws Exception {
        // 设置Pool大小，设为与线程数等大，执行idle checking
        JedisPoolConfig poolConfig = JedisUtils.createPoolConfig(threadCount, threadCount,
                                                                 checkingIntervalSecs,
                                                                 idleTimeSecs);

        // 在获取连接的时候检查有效性, make sure borrow a jedis available
        poolConfig.setTestOnBorrow(true);
        // 扫描idle，如果validate失败，则会在pool中drop。
        poolConfig.setTestWhileIdle(true);
        // create jedis pool
        JedisPool jedisPool = new JedisPool(poolConfig, host, port, timeout, password, database);
        logger.info(
            "create jedisPool[{}] ,host[{}] port[{}] timeout[{}] threadCount[{}] password[{}] database[{}]",
            ToStringBuilder.reflectionToString(jedisPool),
            host,
            port,
            timeout,
            threadCount,
            password,
            database,
            checkingIntervalSecs,
            idleTimeSecs);
        return jedisPool;
    }

    @Override
    public Class<?> getObjectType() {
        return JedisPool.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }


}
