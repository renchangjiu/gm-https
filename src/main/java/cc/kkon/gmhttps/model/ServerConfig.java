package cc.kkon.gmhttps.model;

import java.io.InputStream;

/**
 * @author yui
 */
public class ServerConfig {

    public final int port;

    public final InputStream cert;

    public final String certPassword;

    public final boolean twoWayAuth;

    public final int threadCount;

    public ServerConfig(int port, InputStream cert, String certPassword, boolean twoWayAuth, int threadCount) {
        this.port = port;
        this.cert = cert;
        this.certPassword = certPassword;
        this.twoWayAuth = twoWayAuth;
        this.threadCount = threadCount;
    }
}
