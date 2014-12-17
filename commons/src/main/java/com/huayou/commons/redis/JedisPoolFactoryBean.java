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
  private int threadCount;

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

  public int getThreadCount() {
    return threadCount;
  }

  public void setThreadCount(int threadCount) {
    this.threadCount = threadCount;
  }

  @Override
  public JedisPool getObject() throws Exception {
    // 设置Pool大小，设为与线程数等大，并屏蔽掉idle checking
    JedisPoolConfig poolConfig = JedisUtils.createPoolConfig(threadCount, threadCount);
    // create jedis pool
    JedisPool jedisPool = new JedisPool(poolConfig, host, port, timeout);
    logger.info("create jedisPool[{}] ,host[{}] port[{}] timeout[{}] threadCount[{}]",
                ToStringBuilder.reflectionToString(jedisPool),
                host,
                port,
                timeout,
                threadCount);
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
