package cc.kkon.gmhttps.server;

import cc.kkon.gmhttps.model.ServerConfig;
import cc.kkon.gmhttps.server.servelt.DefaultHttpServletRequest;
import cc.kkon.gmhttps.server.servelt.DefaultHttpServletResponse;
import cc.kkon.gmhttps.utils.ReadLine;
import cc.kkon.gmhttps.utils.Strings;
import cc.kkon.gmhttps.utils.Utils;

import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLSocket;
import javax.security.cert.X509Certificate;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import java.io.Closeable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Map;

public class ConnectRunner implements Runnable, Closeable {

    private final ServerConfig cfg;

    private final Map<String, HttpServlet> servlets;


    private final SSLSocket socket;
    private DataOutputStream out;

    public ConnectRunner(ServerConfig cfg, SSLSocket socket, Map<String, HttpServlet> servlets) {
        this.cfg = cfg;
        this.servlets = servlets;
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            this.doConnect(socket);
        } catch (SSLHandshakeException e) {
            System.out.println("Bad request: " + e.getMessage());
        } catch (ServletException | IOException e) {
            throw new RuntimeException(e);
        } finally {
            Utils.closeQuietly(socket);
        }
    }

    private void doConnect(SSLSocket socket) throws ServletException, IOException {
        DataInputStream in = new DataInputStream(socket.getInputStream());
        this.out = new DataOutputStream(socket.getOutputStream());

        boolean get = false;
        LinkedList<String> reqHeadLines = new LinkedList<>();
        while (true) {
            byte[] lineBuf = ReadLine.read(in);
            if (lineBuf == null || lineBuf.length == 0) {
                break;
            }

            String line = new String(lineBuf);
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
            in.read(buf);
        }

        DefaultHttpServletRequest req = new DefaultHttpServletRequest(reqHeadLines, buf);
        DefaultHttpServletResponse resp = new DefaultHttpServletResponse();

        String reqURI = req.getRequestURI();
        HttpServlet servlet = servlets.get(reqURI);
        if (servlet == null) {
            // TODO: 404 servlet
        }
        servlet.service(req, resp);
        this.writeResponseMessage(resp);
        if (cfg.twoWayAuth) {
            X509Certificate[] cs = socket.getSession().getPeerCertificateChain();
            System.out.println("client certs len=" + cs.length);
            for (X509Certificate c : cs) {
                System.out.println(c);
            }
        }
    }

    private void writeResponseMessage(DefaultHttpServletResponse resp) {
        try {
            byte[] bytes = resp.buildResponseMessage();
            this.out.write(bytes);
            this.out.flush();
            Utils.closeQuietly(this.out);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {

    }
}
