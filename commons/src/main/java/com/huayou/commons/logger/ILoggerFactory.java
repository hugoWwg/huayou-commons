package com.huayou.commons.logger;

public interface ILoggerFactory<T, L extends ILogger> {
	/**
	 * 创建logger
	 * 
	 * @param loggerMainUser
	 *            这个logger对象的主使用者
	 * @return
	 */
	public L getLogger(T loggerMainUser);
}
