package com.huayou.commons.servlet;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;

/**
 * Created by wuqiang on 14-12-24.
 * 在spring中定义这个接口的具体实现
 * @author wuqiang
 */
public interface RequestWrapperFactory {
    public HttpServletRequest getRequestWrapper(HttpServletRequest request, InputStream newInputStream, ServletInputStream originalInputStream);
}
