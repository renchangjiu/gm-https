package cc.kkon.gmhttps.server;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class DefaultHttpServletResponse implements HttpServletResponse {

    private Map<String, String> headers;

    private DefaultServletOutputStream out;

    public DefaultHttpServletResponse() {
        this.headers = new HashMap<>();
        this.out = new DefaultServletOutputStream();
    }

    @Override
    public void addCookie(Cookie cookie) {

    }

    @Override
    public boolean containsHeader(String name) {
        return false;
    }

    @Override
    public String encodeURL(String url) {
        return null;
    }

    @Override
    public String encodeRedirectURL(String url) {
        return null;
    }

    @Override
    public String encodeUrl(String url) {
        return null;
    }

    @Override
    public String encodeRedirectUrl(String url) {
        return null;
    }

    @Override
    public void sendError(int sc, String msg) throws IOException {

    }

    @Override
    public void sendError(int sc) throws IOException {

    }

    @Override
    public void sendRedirect(String location) throws IOException {

    }

    @Override
    public void setDateHeader(String name, long date) {
        throw new RuntimeException();
    }

    @Override
    public void addDateHeader(String name, long date) {
        throw new RuntimeException();
    }

    @Override
    public void setHeader(String name, String value) {
        this.headers.put(name, value);
    }

    @Override
    public void addHeader(String name, String value) {
        throw new RuntimeException();
    }

    @Override
    public void setIntHeader(String name, int value) {
        throw new RuntimeException();
    }

    @Override
    public void addIntHeader(String name, int value) {
        throw new RuntimeException();
    }

    @Override
    public void setStatus(int sc) {

    }

    @Override
    public void setStatus(int sc, String sm) {

    }

    @Override
    public int getStatus() {
        return 0;
    }

    @Override
    public String getHeader(String name) {
        return this.headers.get(name);
    }

    @Override
    public Collection<String> getHeaders(String name) {
        throw new RuntimeException();
    }

    @Override
    public Collection<String> getHeaderNames() {
        return this.headers.keySet();
    }

    @Override
    public String getCharacterEncoding() {
        return null;
    }

    @Override
    public String getContentType() {
        return "text/plain";
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        return this.out;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        return null;
    }

    @Override
    public void setCharacterEncoding(String charset) {

    }

    @Override
    public void setContentLength(int len) {

    }

    @Override
    public void setContentLengthLong(long len) {

    }

    @Override
    public void setContentType(String type) {

    }

    @Override
    public void setBufferSize(int size) {

    }

    @Override
    public int getBufferSize() {
        return 0;
    }

    @Override
    public void flushBuffer() throws IOException {

    }

    @Override
    public void resetBuffer() {

    }

    @Override
    public boolean isCommitted() {
        return false;
    }

    @Override
    public void reset() {

    }

    @Override
    public void setLocale(Locale loc) {

    }

    @Override
    public Locale getLocale() {
        return null;
    }


    public byte[] buildResponseMessage() {
        byte[] bodyBytes = this.out.toByteArray();
        StringBuilder head = new StringBuilder("HTTP/1.1 200 OK\r\n" +
                "Server: GMSSL/1.0\r\n" +
                "Content-Length:" + bodyBytes.length + "\r\n" +
                "Content-Type: text/plain\r\n");
        for (Map.Entry<String, String> ent : headers.entrySet()) {
            head.append(ent.getKey()).append(": ").append(ent.getValue()).append("\r\n");
        }
        head.append("Connection: close\r\n\r\n");
        byte[] headBytes = head.toString().getBytes();
        byte[] bytes = ArrayUtils.addAll(headBytes, bodyBytes);
        IOUtils.closeQuietly(this.out);
        return bytes;
    }
}
