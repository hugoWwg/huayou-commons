package com.huayou.commons.servlet.jetty;

import com.huayou.commons.logger.LogMgr;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpMethods;
import org.eclipse.jetty.http.MimeTypes;
import org.eclipse.jetty.util.LazyList;
import org.eclipse.jetty.util.MultiMap;
import org.eclipse.jetty.util.UrlEncoded;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.*;

/**
 * 重新对request的输入流进行转换（如解密）
 * <p/>
 * Created by wuqiang on 14-12-23.
 *
 * @author wuqiang
 */
public class JettyInputStreamConvertRequestWrapper implements HttpServletRequest {
    //    org.eclipse.jetty.server.Request
    private HttpServletRequest request;
    private ServletInputStream decryptInputStream;

    private MultiMap<String> _baseParameters;
    private MultiMap<String> _parameters;
    private boolean _paramsExtracted = false;
    private int maxFormKeys = 10000;

    public JettyInputStreamConvertRequestWrapper(HttpServletRequest request, InputStream newInputStream, ServletInputStream originalInputStream) {
        this.request = request;
        this.decryptInputStream = new JettyInputStream(newInputStream, originalInputStream);
    }

    /* ------------------------------------------------------------ */

    /**
     * Extract Parameters from query string and/or form _content.
     */
    public void extractParameters() {
        if (_baseParameters == null)
            _baseParameters = new MultiMap(16);

        if (_paramsExtracted) {
            if (_parameters == null)
                _parameters = _baseParameters;
            return;
        }

        _paramsExtracted = true;

        try {
            // handle any _content.
            String encoding = getCharacterEncoding();
            String content_type = getContentType();
            if (content_type != null && content_type.length() > 0) {
                content_type = HttpFields.valueParameters(content_type, null);

                if (MimeTypes.FORM_ENCODED.equalsIgnoreCase(content_type) && (HttpMethods.POST.equals(getMethod()) || HttpMethods.PUT.equals(getMethod()))) {
                    int content_length = getContentLength();
                    if (content_length != 0) {
                        try {
                            InputStream in = getInputStream();
                            // Add form params to query params
                            UrlEncoded.decodeTo(in, _baseParameters, encoding, -1, maxFormKeys);
                        } catch (IOException e) {
                            LogMgr.getLogger(this.getClass()).error(e);
                        }
                    }
                }

            }
            if (_parameters == null)
                _parameters = _baseParameters;
            else if (_parameters != _baseParameters) {
                // Merge parameters (needed if parameters extracted after a forward).
                Iterator iter = _baseParameters.entrySet().iterator();
                while (iter.hasNext()) {
                    Map.Entry entry = (Map.Entry) iter.next();
                    String name = (String) entry.getKey();
                    Object values = entry.getValue();
                    for (int i = 0; i < LazyList.size(values); i++)
                        _parameters.add(name, LazyList.get(values, i));
                }
            }
        } finally {
            // ensure params always set (even if empty) after extraction
            if (_parameters == null)
                _parameters = _baseParameters;
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
        return this.decryptInputStream;
    }

    @Override
    public String getParameter(String s) {
        if (!_paramsExtracted)
            extractParameters();
        return (String) _parameters.getValue(s, 0);
    }

    @Override
    public Enumeration<String> getParameterNames() {
        if (!_paramsExtracted)
            extractParameters();
        return Collections.enumeration(_parameters.keySet());
    }

    @Override
    public String[] getParameterValues(String s) {
        if (!_paramsExtracted)
            extractParameters();
        List<Object> vals = _parameters.getValues(s);
        if (vals == null)
            return null;
        return vals.toArray(new String[vals.size()]);
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        if (!_paramsExtracted)
            extractParameters();

        return Collections.unmodifiableMap(_parameters.toStringArrayMap());

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
