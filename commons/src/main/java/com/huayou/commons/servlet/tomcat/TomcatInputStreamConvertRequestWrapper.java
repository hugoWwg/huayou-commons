package com.huayou.commons.servlet.tomcat;

import org.apache.catalina.util.ParameterMap;
import org.apache.tomcat.util.buf.UDecoder;
import org.apache.tomcat.util.http.Parameters;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;

/**
 * 重新对request的输入流进行转换（如解密）
 * <p/>
 * Created by wuqiang on 14-12-23.
 *
 * @author wuqiang
 */
public class TomcatInputStreamConvertRequestWrapper implements HttpServletRequest {
    //org.apache.catalina.connector.Request
    private HttpServletRequest request;
    private javax.servlet.ServletInputStream decryptInputStream;
    protected int maxParameterCount = 10000;
    /**
     * Request parameters parsed flag.
     */
    protected boolean parametersParsed = false;
    private Parameters parameters = new Parameters();
    /**
     * URL decoder.
     */
    private UDecoder urlDecoder = new UDecoder();
    /**
     * Post data buffer.
     */
    protected static int CACHED_POST_LEN = 8192;
    protected byte[] postData = null;
    /**
     * Hash map used in the getParametersMap method.
     */
    protected ParameterMap<String, String[]> parameterMap = new ParameterMap<String, String[]>();

    public TomcatInputStreamConvertRequestWrapper(HttpServletRequest request, InputStream newInputStream, ServletInputStream originalInputStream) {
        this.request = request;
        this.decryptInputStream = new TomcatInputStream(newInputStream, originalInputStream);
        parameters.setURLDecoder(urlDecoder);
    }

    /**
     * Read post body in an array.
     */
    protected int readPostBody(byte body[], int len)
            throws IOException {

        int offset = 0;
        do {
            int inputLen = this.getInputStream().read(body, offset, len - offset);
            if (inputLen <= 0) {
                return offset;
            }
            offset += inputLen;
        } while ((len - offset) > 0);
        return len;

    }

    /**
     * Parse request parameters.
     */
    protected void parseParameters() {

        parametersParsed = true;

        boolean success = false;
        try {
            // Set this every time in case limit has been changed via JMX
            parameters.setLimit(maxParameterCount);
            String enc = getCharacterEncoding();
            if (enc != null) {
                parameters.setEncoding(enc);
            }
            String contentType = getContentType();
            if (contentType == null) {
                contentType = "";
            }
            int semicolon = contentType.indexOf(';');
            if (semicolon >= 0) {
                contentType = contentType.substring(0, semicolon).trim();
            } else {
                contentType = contentType.trim();
            }
            if (!("application/x-www-form-urlencoded".equals(contentType))) {
                success = true;
                return;
            }
            int len = getContentLength();
            if (len > 0) {
                byte[] formData = null;
                if (len < CACHED_POST_LEN) {
                    if (postData == null) {
                        postData = new byte[CACHED_POST_LEN];
                    }
                    formData = postData;
                } else {
                    formData = new byte[len];
                }
                try {
                    if (readPostBody(formData, len) != len) {
                        return;
                    }
                } catch (IOException e) {
                    // Client disconnect
                    return;
                }
                parameters.processParameters(formData, 0, len);
            }
            success = true;
        } finally {
            if (!success) {
                parameters.setParseFailed(true);
            }
        }

    }

    @Override
    public String getAuthType() {
        return this.request.getAuthType();
    }

    @Override
    public Cookie[] getCookies() {
        return this.request.getCookies();
    }

    @Override
    public long getDateHeader(String s) {
        return this.request.getDateHeader(s);
    }

    @Override
    public String getHeader(String s) {
        return this.request.getHeader(s);
    }

    @Override
    public Enumeration<String> getHeaders(String s) {
        return this.request.getHeaders(s);
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        return this.request.getHeaderNames();
    }

    @Override
    public int getIntHeader(String s) {
        return this.request.getIntHeader(s);
    }

    @Override
    public String getMethod() {
        return this.request.getMethod();
    }

    @Override
    public String getPathInfo() {
        return this.request.getPathInfo();
    }

    @Override
    public String getPathTranslated() {
        return this.request.getPathTranslated();
    }

    @Override
    public String getContextPath() {
        return this.request.getContextPath();
    }

    @Override
    public String getQueryString() {
        // TODO 不再支持queryString
        return "";
    }

    @Override
    public String getRemoteUser() {
        return this.request.getRemoteUser();
    }

    @Override
    public boolean isUserInRole(String s) {
        return this.request.isUserInRole(s);
    }

    @Override
    public Principal getUserPrincipal() {
        return this.request.getUserPrincipal();
    }

    @Override
    public String getRequestedSessionId() {
        return this.request.getRequestedSessionId();
    }

    @Override
    public String getRequestURI() {
        return this.request.getRequestURI();
    }

    @Override
    public StringBuffer getRequestURL() {
        return this.request.getRequestURL();
    }

    @Override
    public String getServletPath() {
        return this.request.getServletPath();
    }

    @Override
    public HttpSession getSession(boolean b) {
        return this.request.getSession(b);
    }

    @Override
    public HttpSession getSession() {
        return this.request.getSession();
    }


    @Override
    public boolean isRequestedSessionIdValid() {
        return this.request.isRequestedSessionIdValid();
    }

    @Override
    public boolean isRequestedSessionIdFromCookie() {
        return this.request.isRequestedSessionIdFromCookie();
    }

    @Override
    public boolean isRequestedSessionIdFromURL() {
        return this.request.isRequestedSessionIdFromURL();
    }

    @Override
    public boolean isRequestedSessionIdFromUrl() {
        return this.request.isRequestedSessionIdFromUrl();
    }

    @Override
    public boolean authenticate(HttpServletResponse httpServletResponse) throws IOException, ServletException {
        return this.request.authenticate(httpServletResponse);
    }

    @Override
    public void login(String s, String s2) throws ServletException {
        this.request.login(s, s2);
    }

    @Override
    public void logout() throws ServletException {
        this.request.logout();
    }

    @Override
    public Collection<Part> getParts() throws IOException, ServletException {
        return this.request.getParts();
    }

    @Override
    public Part getPart(String s) throws IOException, ServletException {
        return this.request.getPart(s);
    }

    @Override
    public Object getAttribute(String s) {
        return this.request.getAttribute(s);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return this.request.getAttributeNames();
    }

    @Override
    public String getCharacterEncoding() {
        return this.request.getCharacterEncoding();
    }

    @Override
    public void setCharacterEncoding(String s) throws UnsupportedEncodingException {
        this.request.setCharacterEncoding(s);
    }

    @Override
    public int getContentLength() {
        try {
            return this.decryptInputStream.available();
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
    }

    @Override
    public String getContentType() {
        return this.request.getContentType();
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        // TODO
        return this.decryptInputStream;
    }

    @Override
    public String getParameter(String s) {
        if (!parametersParsed) {
            parseParameters();
        }
        return this.parameters.getParameter(s);
    }

    @Override
    public Enumeration<String> getParameterNames() {
        if (!parametersParsed) {
            parseParameters();
        }
        return this.parameters.getParameterNames();
    }

    @Override
    public String[] getParameterValues(String s) {
        if (!parametersParsed) {
            parseParameters();
        }
        return this.parameters.getParameterValues(s);
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        if (parameterMap.isLocked()) {
            return parameterMap;
        }

        Enumeration<String> enumeration = getParameterNames();
        while (enumeration.hasMoreElements()) {
            String name = enumeration.nextElement();
            String[] values = getParameterValues(name);
            parameterMap.put(name, values);
        }

        parameterMap.setLocked(true);

        return parameterMap;

    }

    @Override
    public String getProtocol() {
        return this.request.getProtocol();
    }

    @Override
    public String getScheme() {
        return this.request.getScheme();
    }

    @Override
    public String getServerName() {
        return this.request.getServerName();
    }

    @Override
    public int getServerPort() {
        return this.request.getServerPort();
    }

    @Override
    public BufferedReader getReader() throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getRemoteAddr() {
        return this.request.getRemoteAddr();
    }

    @Override
    public String getRemoteHost() {
        return this.request.getRemoteHost();
    }

    @Override
    public void setAttribute(String s, Object o) {
        this.request.setAttribute(s, o);
    }

    @Override
    public void removeAttribute(String s) {
        this.request.removeAttribute(s);
    }

    @Override
    public Locale getLocale() {
        return this.request.getLocale();
    }

    @Override
    public Enumeration<Locale> getLocales() {
        return this.request.getLocales();
    }

    @Override
    public boolean isSecure() {
        return this.request.isSecure();
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String s) {
        return this.request.getRequestDispatcher(s);
    }

    @Override
    public String getRealPath(String s) {
        return this.request.getRealPath(s);
    }

    @Override
    public int getRemotePort() {
        return this.request.getRemotePort();
    }

    @Override
    public String getLocalName() {
        return this.request.getLocalName();
    }

    @Override
    public String getLocalAddr() {
        return this.request.getLocalAddr();
    }

    @Override
    public int getLocalPort() {
        return this.request.getLocalPort();
    }

    @Override
    public ServletContext getServletContext() {
        return this.request.getServletContext();
    }

    @Override
    public AsyncContext startAsync() throws IllegalStateException {
        return this.request.startAsync();
    }

    @Override
    public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) throws IllegalStateException {
        return this.request.startAsync(servletRequest, servletResponse);
    }

    @Override
    public boolean isAsyncStarted() {
        return this.request.isAsyncStarted();
    }

    @Override
    public boolean isAsyncSupported() {
        return this.request.isAsyncSupported();
    }

    @Override
    public AsyncContext getAsyncContext() {
        return this.request.getAsyncContext();
    }

    @Override
    public DispatcherType getDispatcherType() {
        return this.request.getDispatcherType();
    }

    /*servlet-api-3.1.0才有的方法*/
    public String changeSessionId() {
        throw new UnsupportedOperationException();
    }

    /*servlet-api-3.1.0才有的方法*/
    public <T extends HttpUpgradeHandler> T upgrade(Class<T> handlerClass) throws IOException, ServletException {
        throw new UnsupportedOperationException();
    }

    /*servlet-api-3.1.0才有的方法*/
    public long getContentLengthLong() {
        return getContentLength();
    }
}
