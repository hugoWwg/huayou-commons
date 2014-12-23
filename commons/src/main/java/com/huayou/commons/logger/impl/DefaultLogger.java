package com.huayou.commons.logger.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.huayou.commons.logger.ILogger;

/**
 * 默认的Logger对象，里面使用的是slf4j的Logger
 * 
 * @author wu-qiang
 * 
 */
public class DefaultLogger implements ILogger {
	private static final long serialVersionUID = 8366860838727228508L;
	private int logType = LOG_TYPE_REQUEST;
	protected Logger logger;

	public DefaultLogger(Object loggerMainUser) {
		super();
		if (loggerMainUser != null) {
			if (Class.class.isAssignableFrom(loggerMainUser.getClass())) {
				logger = LoggerFactory.getLogger((Class<?>) loggerMainUser);
			} else {
				logger = LoggerFactory.getLogger(loggerMainUser.getClass());
			}
		} else {
			logger = LoggerFactory.getLogger(this.getClass());
		}
	}

	@Override
	public ILogger info(Object msg) {
		logger.info(String.valueOf(msg));
		return this;
	}

	@Override
	public ILogger debug(Object msg) {
		logger.debug(String.valueOf(msg));
		return this;
	}

	@Override
	public ILogger warn(Object msg) {
		logger.warn(String.valueOf(msg));
		return this;
	}

	@Override
	public ILogger error(Object msg) {
		if (msg != null && msg instanceof Throwable) {
			this.error("", (Throwable) msg);
		} else {
			logger.error(String.valueOf(msg));
		}
		return this;
	}

	@Override
	public ILogger error(String msg, Throwable ex) {
		logger.error(msg, ex);
		return this;
	}

	@Override
	public void close(boolean isSuccess) {
		// TODO do nothing
	}

	@Override
	public void setLogType(int logType) {
		this.logType = logType;
	}

	@Override
	public int getLogType() {
		return this.logType;
	}

}
