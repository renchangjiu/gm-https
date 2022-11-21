package cc.kkon.gmhttps.server;


import cc.kkon.gmhttps.client.TrustAllManager;
import cc.kkon.gmhttps.utils.Strings;
import cc.kkon.gmhttps.utils.Utils;
import org.apache.commons.io.IOUtils;

import javax.net.ServerSocketFactory;
import javax.net.ssl.*;
import javax.servlet.annotation.WebServlet;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.net.Socket;
import java.security.KeyStore;
import java.security.Provider;
import java.security.Security;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * 单向认证
 *
 * @author gmssl.cn
 */
public class SSLServer {

    private int port;

    private InputStream cert;

    private String certPassword;

    private Map<String, DefaultHttpServlet> servlets;

    private volatile boolean closed;


    public SSLServer(int port, InputStream cert, String certPassword) {
        this.port = port;
        this.cert = cert;
        this.certPassword = certPassword;
        this.servlets = new HashMap<>();
    }

    public void listen() throws Exception {
        Thread thread = new Thread(new Runner0());
        System.out.println("SSLServer started.");
        thread.start();
    }

    public void addServlet(String urlPattern, DefaultHttpServlet servlet) {
        this.check(urlPattern);
        this.servlets.put(urlPattern, servlet);
    }

    public void addServlet(DefaultHttpServlet servlet) {
        Class<? extends DefaultHttpServlet> clazz = servlet.getClass();
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

    private static SSLServerSocketFactory createServerSocketFactory(KeyStore keyStore, char[] pwd) throws Exception {
        TrustManager[] trust = {new TrustAllManager()};

        KeyManager[] kms = null;
        if (keyStore != null) {
            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            kmf.init(keyStore, pwd);
            kms = kmf.getKeyManagers();
        }

        SSLContext ctx = SSLContext.getInstance("GMSSLv1.1", "GMJSSE");
        java.security.SecureRandom secureRandom = new java.security.SecureRandom();
        ctx.init(kms, trust, secureRandom);

        ctx.getServerSessionContext().setSessionCacheSize(8192);
        ctx.getServerSessionContext().setSessionTimeout(3600);

        return ctx.getServerSocketFactory();
    }

    public void close() {
        this.closed = true;
        System.out.println("SSLServer closed.");
    }

    private class Runner0 implements Runnable {

        @Override
        public void run() {
            try {
                this.listen();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        private void listen() throws Exception {
            ServerSocketFactory fact = null;
            SSLServerSocket sslServerSocket = null;

            System.out.println("Usage: java -cp GMExample.jar server.Server1 port");

            System.out.println("Port=" + port);

            Security.insertProviderAt((Provider) Class.forName("cn.gmssl.jce.provider.GMJCE").newInstance(), 1);
            Security.insertProviderAt((Provider) Class.forName("cn.gmssl.jsse.provider.GMJSSE").newInstance(), 2);

            KeyStore pfx = KeyStore.getInstance("PKCS12", "GMJSSE");
            char[] certPwdBytes = certPassword.toCharArray();
            pfx.load(cert, certPwdBytes);

            fact = createServerSocketFactory(pfx, certPwdBytes);
            sslServerSocket = (SSLServerSocket) fact.createServerSocket(port);

            System.out.println("listening...");

            while (!closed) {
                Socket socket = null;
                try {
                    socket = sslServerSocket.accept();
                    System.out.println("client comes");

                    DataInputStream in = new DataInputStream(socket.getInputStream());
                    DataOutputStream out = new DataOutputStream(socket.getOutputStream());

                    boolean get = false;
                    LinkedList<String> reqHeadLines = new LinkedList<>();
                    while (true) {
                        byte[] lineBuf = ReadLine.read(in);
                        if (lineBuf == null || lineBuf.length == 0) {
                            break;
                        }
                        String line = new String(lineBuf);
                        System.out.println(line);
                        if (!get) {
                            get = line.startsWith("GET ");
                        }
                        reqHeadLines.add(line);
                    }
                    String contentLength = Utils.buildHeaders(reqHeadLines).get("Content-Length");

                    byte[] buf = new byte[0];
                    // 请求体
                    if (!get && Strings.isNotEmpty(contentLength)) {
                        int len = Integer.parseInt(contentLength);
                        buf = new byte[len];
                        int readLen = in.read(buf);
                        System.out.println(new String(buf, 0, len));
                    }


                    DefaultHttpServletRequest req = new DefaultHttpServletRequest(reqHeadLines, buf);
                    DefaultHttpServletResponse res = new DefaultHttpServletResponse();

                    String reqURI = req.getRequestURI();
                    DefaultHttpServlet servlet = servlets.get(reqURI);
                    if (servlet == null) {
                        // TODO: 404 servlet
                    }
                    servlet.service(req, res);

                    byte[] message = res.buildResponseMessage();
                    out.write(message);
                    out.flush();
                    System.out.println("\n\n\n");
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    IOUtils.close(socket);
                }
            }
        }
    }
}

