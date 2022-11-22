package cc.kkon.gmhttps.server;


import cc.kkon.gmhttps.model.ServerConfig;
import cc.kkon.gmhttps.utils.Strings;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import java.io.Closeable;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * 单向认证
 *
 * @author gmssl.cn
 */
public class SSLServer implements Closeable {

    private final Map<String, HttpServlet> servlets;

    private final AcceptRunner acceptRunner;


    public SSLServer(int port, InputStream cert, String certPassword, boolean twoWayAuth) {
        ServerConfig cfg = new ServerConfig(port, cert, certPassword, twoWayAuth, 20);
        this.servlets = new HashMap<>();
        this.acceptRunner = new AcceptRunner(cfg, servlets);
    }

    /**
     * li-sten~~
     */
    public void listen() {
        Thread thread = new Thread(this.acceptRunner);
        thread.start();
        System.out.println("SSLServer started.");
    }

    public synchronized void addServlet(String urlPattern, HttpServlet servlet) {
        this.check(urlPattern);
        this.servlets.put(urlPattern, servlet);
    }

    public void addServlet(HttpServlet servlet) {
        Class<? extends HttpServlet> clazz = servlet.getClass();
        WebServlet anno = clazz.getAnnotation(WebServlet.class);
        String[] value = anno.value();
        for (String val : value) {
            this.addServlet(val, servlet);
        }
    }

    private void check(String urlPattern) {
        if (Strings.isBlank(urlPattern)) {
            throw new RuntimeException("UrlPattern is blank.");
        }
        if (this.servlets.containsKey(urlPattern)) {
            throw new RuntimeException("UrlPattern existed.");
        }
    }

    @Override
    public void close() {
        this.acceptRunner.close();
        System.out.println("SSLServer closed.");
    }

}

