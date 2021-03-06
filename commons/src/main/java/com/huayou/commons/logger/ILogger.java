package com.huayou.commons.logger;

import java.io.Serializable;

/**
 * 用于向一个线程绑定的Logger对象
 *
 * @author wu-qiang
 */
public interface ILogger extends Serializable {
    /**
     * 日志类型——Request请求
     */
    int LOG_TYPE_REQUEST = 1;
    /**
     * 日志类型——Task
     */
    int LOG_TYPE_TASK = 2;

    /**
     * 设置日志对象类型
     *
     * @param logType
     * @see <code>ILogger.LOG_TYPE_REQUEST</code>
     * <code>ILogger.LOG_TYPE_TASK</code>
     */
    void setLogType(int logType);

    /**
     * 返回日志对象类型
     *
     * @return
     * @see <code>ILogger.LOG_TYPE_REQUEST</code>
     * <code>ILogger.LOG_TYPE_TASK</code>
     */
    int getLogType();

    ILogger info(Object msg);

    ILogger debug(Object msg);

    ILogger warn(Object msg);

    /**
     * 也可以是Throwable对象
     *
     * @param msg
     * @return
     */
    ILogger error(Object msg);

    ILogger error(String msg, Throwable ex);

    /**
     * 保存日志
     *
     * @param isSuccess run方法是否执行成功，true：成功（未抛出异常），false：失败，run方法抛出异常
     */
    void close(boolean isSuccess);
}
