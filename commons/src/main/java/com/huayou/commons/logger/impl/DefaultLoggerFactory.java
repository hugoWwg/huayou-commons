package com.huayou.commons.logger.impl;

import com.huayou.commons.logger.ILogger;
import com.huayou.commons.logger.ILoggerFactory;

/**
 * 默认的LoggerFactory
 * 
 * @author wu-qiang
 * 
 */
public class DefaultLoggerFactory implements ILoggerFactory<Object, ILogger> {

	@Override
	public ILogger getLogger(Object loggerMainUser) {
		return new DefaultLogger(loggerMainUser);
	}

}
