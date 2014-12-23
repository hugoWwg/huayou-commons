package com.huayou.commons.logger;

import com.huayou.commons.logger.impl.DefaultLogger;

/**
 * 用于管理ILogger
 *
 * @author wu-qiang
 *
 */
public final class LogMgr {
    private LogMgr() {
    }

    private static final ThreadLocal<ILogger> loggerThreadLocal = new ThreadLocal<ILogger>();
    /**
     * 默认的全局Logger对象
     */
    private static final DefaultLogger globalDefaultLogger = new DefaultLogger(
            LogMgr.class);
    /**
     * 获取当前线程的Logger对象<br>
     * 如果线程没有绑定则返回null
     *
     * @return
     */
    public static ILogger getLoggerMaybeNull() {
        return loggerThreadLocal.get();
    }
    /**
     * 获取当前线程的Logger对象<br>
     * 如果线程没有绑定则返回默认的全局Logger对象
     *
     * @return
     */
    public static ILogger getLogger() {
        ILogger logger = loggerThreadLocal.get();
        if (logger == null) {
            return LogMgr.globalDefaultLogger;
        }
        return logger;
    }

    /**
     * 获取当前线程的Logger对象<br>
     * 如果线程没有绑定则返回defaultLogger
     *
     * @param defaultLogger
     * @return
     */
    public static ILogger getLogger(ILogger defaultLogger) {
        ILogger logger = getLogger();
        if (logger == null || logger == LogMgr.globalDefaultLogger) {
            return defaultLogger;
        }
        // Assert.notNull(logger,
        // "Cannot find ILogger for this thread, logger should be initialized first!");
        return logger;
    }

    /**
     *
     * 获取当前线程的Logger对象<br>
     * 如果线程没有绑定则返回创建一个new DefaultLogger(loggerMainUserClass)
     *
     * @param loggerMainUserClass
     * @return
     */
    public static ILogger getLogger(Class<?> loggerMainUserClass) {
        ILogger logger = getLogger();
        if (logger == null || logger == LogMgr.globalDefaultLogger) {
            return new DefaultLogger(loggerMainUserClass);
        }
        // Assert.notNull(logger,
        // "Cannot find ILogger for this thread, logger should be initialized first!");
        return logger;
    }

    /**
     * 向本线程设置/清空(设置为null)ILogger
     *
     * @param logger
     */
    static void setThreadLogger(ILogger logger) {
        loggerThreadLocal.set(logger);
    }

}
