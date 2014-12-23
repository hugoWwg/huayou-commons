package com.huayou.commons.logger;


/**
 * 
 * @author wu-qiang
 * 
 */
public final class _LogMgrPackageAccesser {
	/**
	 * 向本线程设置/清空(设置为null)ILogger
	 * 
	 * @param logger
	 */
	public static void setThreadLogger(ILogger logger) {
		LogMgr.setThreadLogger(logger);
	}
}
