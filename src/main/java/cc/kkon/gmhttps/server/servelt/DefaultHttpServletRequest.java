package cc.kkon.gmhttps.server.servelt;

import cc.kkon.gmhttps.model.CC;
import cc.kkon.gmhttps.model.FirstLine;
import cc.kkon.gmhttps.utils.Strings;
import cc.kkon.gmhttps.utils.Utils;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.*;

public class DefaultHttpServletRequest implements HttpServletRequest {

    private final FirstLine fl;

    private final Map<String, String> headers;

    private final Map<String, List<String>> params;

    private final byte[] body;


    public DefaultHttpServletRequest(List<String> headLines, byte[] body) {
        String firstLine = headLines.remove(0);
        this.fl = Utils.parse1stLine(firstLine);
        this.headers = Utils.buildHeaders(headLines);
        Map<String, List<String>> urlParams = null;
        Map<String, List<String>> bodyParams = null;
        this.body = body;
        String url = this.fl.url;
        if (url.contains("?")) {
            String paramsStr = url.split("\\?")[1];
            urlParams = Utils.parseParams(paramsStr);
        }
        String type = this.getContentType();
        if (body.length != 0 && Strings.isNotBlank(type)) {
            if (type.startsWith(CC.CONTENT_TYPE_URLENCODED)) {
                String con = new String(body, StandardCharsets.UTF_8);
                bodyParams = Utils.parseParams(con);
            }
        }
        this.params = Utils.merge(urlParams, bodyParams);
    }

    /**
     * 在 json 格式的请求中, 获取请求体
     */
    public byte[] getBody() {
        return body;
    }

    @Override
    public String getAuthType() {
        return null;
    }

    @Override
    public Cookie[] getCookies() {
        return new Cookie[0];
    }

    @Override
    public long getDateHeader(String s) {
        throw new RuntimeException("Not support now");
    }

    @Override
    public String getHeader(String s) {
        return this.headers.get(s);
    }

    @Override
    public Enumeration<String> getHeaders(String s) {
        throw new RuntimeException("Not support now");
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        return Collections.enumeration(this.headers.keySet());
    }

    @Override
    public int getIntHeader(String s) {
        throw new RuntimeException("Not support now");
    }

    /**
     * uppercase, like: GET,POST...
     */
    @Override
    public String getMethod() {
        return this.fl.method;
    }

    @Override
    public String getPathInfo() {
        throw new RuntimeException("Not support now");
    }

    @Override
    public String getPathTranslated() {
        return null;
    }

    @Override
    public String getContextPath() {
        return null;
    }

    @Override
    public String getQueryString() {
        return null;
    }

    @Override
    public String getRemoteUser() {
        return null;
    }

    @Override
    public boolean isUserInRole(String s) {
        return false;
    }

    @Override
    public Principal getUserPrincipal() {
        return null;
    }

    @Override
    public String getRequestedSessionId() {
        return null;
    }

    @Override
    public String getRequestURI() {
        return this.getRequestURL().toString();
    }

    @Override
    public StringBuffer getRequestURL() {
        String url = this.fl.url;
        int i = url.indexOf("?");
        if (i != -1) {
            url = url.substring(0, i);
        }
        return new StringBuffer(url);
    }

    @Override
    public String getServletPath() {
        return null;
    }

    @Override
    public HttpSession getSession(boolean b) {
        return null;
    }

    @Override
    public HttpSession getSession() {
        return null;
    }

    @Override
    public String changeSessionId() {
        return null;
    }

    @Override
    public boolean isRequestedSessionIdValid() {
        return false;
    }

    @Override
    public boolean isRequestedSessionIdFromCookie() {
        return false;
    }

    @Override
    public boolean isRequestedSessionIdFromURL() {
        return false;
    }

    @Override
    public boolean isRequestedSessionIdFromUrl() {
        return false;
    }

    @Override
    public boolean authenticate(HttpServletResponse httpServletResponse) throws IOException, ServletException {
        return false;
    }

    @Override
    public void login(String s, String s1) throws ServletException {

    }

    @Override
    public void logout() throws ServletException {

    }

    @Override
    public Collection<Part> getParts() throws IOException, ServletException {
        return null;
    }

    @Override
    public Part getPart(String s) throws IOException, ServletException {
        return null;
    }

    @Override
    public <T extends HttpUpgradeHandler> T upgrade(Class<T> aClass) throws IOException, ServletException {
        return null;
    }

    @Override
    public Object getAttribute(String s) {
        return null;
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return null;
    }

    @Override
    public String getCharacterEncoding() {
        return null;
    }

    @Override
    public void setCharacterEncoding(String s) throws UnsupportedEncodingException {

    }

    @Override
    public int getContentLength() {
        return (int) this.getContentLengthLong();
    }

    @Override
    public long getContentLengthLong() {
        String len = this.headers.getOrDefault("Content-Length", "0");
        return Long.parseLong(len);
    }

    @Override
    public String getContentType() {
        return this.headers.get("Content-Type");
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        return null;
    }

    @Override
    public String getParameter(String s) {
        if (this.params.containsKey(s)) {
            return this.params.get(s).get(0);
        }
        return null;
    }

    @Override
    public Enumeration<String> getParameterNames() {
        return Collections.enumeration(this.params.keySet());
    }

    @Override
    public String[] getParameterValues(String s) {
        List<String> val = this.params.get(s);
        if (val == null) {
            return null;
        }
        return val.toArray(new String[0]);
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        Map<String, String[]> res = new HashMap<>();
        for (Map.Entry<String, List<String>> ent : params.entrySet()) {
            res.put(ent.getKey(), ent.getValue().toArray(new String[0]));
        }
        return res;
    }

    @Override
    public String getProtocol() {
        return null;
    }

    @Override
    public String getScheme() {
        return null;
    }

    @Override
    public String getServerName() {
        return null;
    }

    @Override
    public int getServerPort() {
        return 0;
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return null;
    }

    @Override
    public String getRemoteAddr() {
        return null;
    }

    @Override
    public String getRemoteHost() {
        return null;
    }

    @Override
    public void setAttribute(String s, Object o) {

    }

    @Override
    public void removeAttribute(String s) {

    }

    @Override
    public Locale getLocale() {
        return null;
    }

    @Override
    public Enumeration<Locale> getLocales() {
        return null;
    }

    @Override
    public boolean isSecure() {
        return false;
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String s) {
        return null;
    }

    @Override
    public String getRealPath(String s) {
        return null;
    }

    @Override
    public int getRemotePort() {
        return 0;
    }

    @Override
    public String getLocalName() {
        return null;
    }

    @Override
    public String getLocalAddr() {
        return null;
    }

    @Override
    public int getLocalPort() {
        return 0;
    }

    @Override
    public ServletContext getServletContext() {
        return null;
    }

    @Override
    public AsyncContext startAsync() throws IllegalStateException {
        return null;
    }

    @Override
    public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) throws IllegalStateException {
        return null;
    }

    @Override
    public boolean isAsyncStarted() {
        return false;
    }

    @Override
    public boolean isAsyncSupported() {
        return false;
    }

    @Override
    public AsyncContext getAsyncContext() {
        return null;
    }

    @Override
    public DispatcherType getDispatcherType() {
        return null;
    }
}
