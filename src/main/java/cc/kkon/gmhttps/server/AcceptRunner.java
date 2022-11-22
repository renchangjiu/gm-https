package cc.kkon.gmhttps.server;

import cc.kkon.gmhttps.model.ServerConfig;
import cc.kkon.gmhttps.utils.Utils;
import cn.gmssl.jce.provider.GMJCE;
import cn.gmssl.jsse.provider.GMJSSE;
import org.apache.commons.io.IOUtils;

import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;
import javax.servlet.http.HttpServlet;
import java.io.Closeable;
import java.net.SocketException;
import java.security.KeyStore;
import java.security.Security;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AcceptRunner implements Runnable, Closeable {


    private final ServerConfig cfg;

    private final ExecutorService threadPool;

    private final Map<String, HttpServlet> servlets;


    private volatile boolean closed;

    private SSLServerSocket sslServerSocket;

    public AcceptRunner(ServerConfig cfg, Map<String, HttpServlet> servlets) {
        this.cfg = cfg;
        this.servlets = servlets;
        this.threadPool = Executors.newFixedThreadPool(cfg.threadCount);
    }

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
        char[] certPwdBytes = cfg.certPassword.toCharArray();
        pfx.load(cfg.cert, certPwdBytes);

        ServerSocketFactory fact = Utils.createServerSocketFactory(pfx, certPwdBytes);
        sslServerSocket = (SSLServerSocket) fact.createServerSocket(cfg.port);

        sslServerSocket.setNeedClientAuth(cfg.twoWayAuth);

        System.out.println("listening...");

        while (!closed) {
            try {
                System.out.println("client comes");
                SSLSocket socket = (SSLSocket) sslServerSocket.accept();
                ConnectRunner runner = new ConnectRunner(cfg, socket, this.servlets);
                threadPool.execute(runner);
            } catch (SocketException e) {
                System.out.println("SSLServerSocket closed, " + cfg.port + " released.");
            }
        }
    }

    @Override
    public void close() {
        this.closed = true;
        IOUtils.closeQuietly(cfg.cert);
        IOUtils.closeQuietly(sslServerSocket);
        this.threadPool.shutdown();
    }
}
