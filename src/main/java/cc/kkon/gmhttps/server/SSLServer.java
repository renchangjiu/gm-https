package cc.kkon.gmhttps.server;


import cc.kkon.gmhttps.client.TrustAllManager;
import cc.kkon.gmhttps.server.core.DefaultHttpServletRequest;
import cc.kkon.gmhttps.server.core.DefaultHttpServletResponse;
import cc.kkon.gmhttps.utils.ReadLine;
import cc.kkon.gmhttps.utils.Strings;
import cc.kkon.gmhttps.utils.Utils;
import cn.gmssl.jce.provider.GMJCE;
import cn.gmssl.jsse.provider.GMJSSE;
import org.apache.commons.io.IOUtils;

import javax.net.ServerSocketFactory;
import javax.net.ssl.*;
import javax.security.cert.X509Certificate;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.net.SocketException;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.Security;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 单向认证
 *
 * @author gmssl.cn
 */
public class SSLServer {

    private final int port;

    private final InputStream cert;

    private final String certPassword;

    private final ExecutorService threadPool;


    private final Map<String, HttpServlet> servlets;

    private final boolean twoWayAuth;


    private Runner0 runner0;

    private volatile boolean closed;


    public SSLServer(int port, InputStream cert, String certPassword, boolean twoWayAuth) {
        this.port = port;
        this.cert = cert;
        this.certPassword = certPassword;
        this.twoWayAuth = twoWayAuth;
        this.threadPool = Executors.newFixedThreadPool(20);
        this.servlets = new HashMap<>();
        this.runner0 = new Runner0();
    }

    public void listen() {
        Thread thread = new Thread(this.runner0);
        System.out.println("SSLServer started.");
        thread.start();
    }

    public void addServlet(String urlPattern, HttpServlet servlet) {
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

    private static SSLServerSocketFactory createServerSocketFactory(KeyStore keyStore, char[] pwd) throws Exception {
        TrustManager[] trust = {new TrustAllManager()};

        KeyManager[] kms = null;
        if (keyStore != null) {
            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            kmf.init(keyStore, pwd);
            kms = kmf.getKeyManagers();
        }

        SSLContext ctx = SSLContext.getInstance("GMSSLv1.1", "GMJSSE");
        SecureRandom secureRandom = new SecureRandom();
        ctx.init(kms, trust, secureRandom);

        ctx.getServerSessionContext().setSessionCacheSize(8192);
        ctx.getServerSessionContext().setSessionTimeout(3600);

        return ctx.getServerSocketFactory();
    }

    public void close() {
        this.closed = true;
        IOUtils.closeQuietly(this.cert);
        this.threadPool.shutdown();
        this.runner0.close();
        System.out.println("SSLServer closed.");
    }

    private class Runner0 implements Runnable {


        private SSLServerSocket sslServerSocket;

        @Override
        public void run() {
            try {
                this.listen();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        private void listen() throws Exception {
            Security.insertProviderAt(new GMJCE(), 1);
            Security.insertProviderAt(new GMJSSE(), 2);

            KeyStore pfx = KeyStore.getInstance("PKCS12", "GMJSSE");
            char[] certPwdBytes = certPassword.toCharArray();
            pfx.load(cert, certPwdBytes);

            ServerSocketFactory fact = createServerSocketFactory(pfx, certPwdBytes);
            sslServerSocket = (SSLServerSocket) fact.createServerSocket(port);

            sslServerSocket.setNeedClientAuth(twoWayAuth);

            System.out.println("listening...");

            while (!closed) {
                try {
                    SSLSocket socket = (SSLSocket) sslServerSocket.accept();
                    threadPool.execute(() -> {
                        this.processConnect(socket);
                    });
                } catch (SocketException e) {
                    System.out.println("SSLServerSocket closed, " + port + " released.");
                }
            }
        }

        private void processConnect(SSLSocket socket) {
            try {
                System.out.println("client comes");

                DataInputStream in = new DataInputStream(socket.getInputStream());
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());

                boolean get = false;
                LinkedList<String> reqHeadLines = new LinkedList<>();
                System.out.println("Request headers: ");
                while (true) {
                    byte[] lineBuf = ReadLine.read(in);
                    if (lineBuf == null || lineBuf.length == 0) {
                        break;
                    }
                    String line = new String(lineBuf);
                    System.out.println("\t" + line);
                    if (!get) {
                        get = line.startsWith("GET ");
                    }
                    reqHeadLines.add(line);
                }
                System.out.println();

                String contentLength = Utils.buildHeaders(reqHeadLines).get("Content-Length");

                byte[] buf = new byte[0];
                // 请求体
                if (!get && Strings.isNotEmpty(contentLength)) {
                    int len = Integer.parseInt(contentLength);
                    buf = new byte[len];
                    in.read(buf);
                    System.out.println(new String(buf, 0, len));
                }


                DefaultHttpServletRequest req = new DefaultHttpServletRequest(reqHeadLines, buf);
                DefaultHttpServletResponse res = new DefaultHttpServletResponse();

                String reqURI = req.getRequestURI();
                HttpServlet servlet = servlets.get(reqURI);
                if (servlet == null) {
                    // TODO: 404 servlet
                }
                servlet.service(req, res);

                byte[] message = res.buildResponseMessage();
                out.write(message);
                out.flush();
                if (twoWayAuth) {
                    X509Certificate[] cs = socket.getSession().getPeerCertificateChain();
                    System.out.println("client certs len=" + cs.length);
                    for (X509Certificate c : cs) {
                        System.out.println(c);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            } finally {
                IOUtils.closeQuietly(socket);
            }
        }

        public void close() {
            IOUtils.closeQuietly(sslServerSocket);
        }
    }
}

