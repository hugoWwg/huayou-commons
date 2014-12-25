package com.huayou.commons.servlet.jetty;

import com.huayou.commons.servlet.RequestWrapperFactory;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;

/**
 * Created by wuqiang on 14-12-24.
 *
 * @author wuqiang
 */
public class JettyRequestWrapperFactory implements RequestWrapperFactory {
    @Override
    public HttpServletRequest getRequestWrapper(HttpServletRequest request, InputStream newInputStream, ServletInputStream originalInputStream) {
        return new JettyInputStreamConvertRequestWrapper(request,newInputStream,originalInputStream);
    }
}
