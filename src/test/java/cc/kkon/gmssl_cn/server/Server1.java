package cc.kkon.gmssl_cn.server;


import cc.kkon.gmssl_cn.client.TrustAllManager;

import javax.net.ServerSocketFactory;
import javax.net.ssl.*;
import java.io.*;
import java.net.Socket;
import java.security.KeyStore;
import java.security.Provider;
import java.security.Security;

/**
 * 单向认证
 *
 * @author gmssl.cn
 */
public class Server1 {
    public Server1() {
    }

    public static void main(String[] args) throws Exception {
        ServerSocketFactory fact = null;
        SSLServerSocket serversocket = null;

        System.out.println("Usage: java -cp GMExample.jar server.Server1 port");
        int port = 443;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        }

        System.out.println("Port=" + port);

        String pfxfile = "keystore/sm2.server1.both.pfx";
        String pwdpwd = "12345678";

        Security.insertProviderAt((Provider) Class.forName("cn.gmssl.jce.provider.GMJCE").newInstance(), 1);
        Security.insertProviderAt((Provider) Class.forName("cn.gmssl.jsse.provider.GMJSSE").newInstance(), 2);

        KeyStore pfx = KeyStore.getInstance("PKCS12", "GMJSSE");
        pfx.load(new FileInputStream(pfxfile), pwdpwd.toCharArray());

        fact = createServerSocketFactory(pfx, pwdpwd.toCharArray());
        serversocket = (SSLServerSocket) fact.createServerSocket(port);

        System.out.println("listening...");

        while (true) {
            Socket socket = null;
            try {
                socket = serversocket.accept();
                System.out.println("client comes");

                DataInputStream in = new DataInputStream(socket.getInputStream());
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());

                boolean get = false;
                while (true) {
                    byte[] lineBuf = ReadLine.read(in);
                    if (lineBuf == null || lineBuf.length == 0) {
                        break;
                    }
                    String line = new String(lineBuf);
                    System.out.println(line);
                    if (!get)
                        get = line.startsWith("GET ");
                }

                if (!get) {
                    byte[] buf = new byte[8192];
                    int len = in.read(buf);
                    System.out.println(new String(buf, 0, len));
                }

                byte[] body = "this is a gm server".getBytes();
                byte[] resp = ("HTTP/1.1 200 OK\r\nServer: GMSSL/1.0\r\nContent-Length:" + body.length + "\r\nContent-Type: text/plain\r\nConnection: close\r\n\r\n").getBytes();
                out.write(resp, 0, resp.length);
                out.write(body, 0, body.length);
                out.flush();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                } catch (Exception e) {
                }
            }
        }
    }

    public static SSLServerSocketFactory createServerSocketFactory(KeyStore kepair, char[] pwd) throws Exception {
        TrustManager[] trust = {new TrustAllManager()};

        KeyManager[] kms = null;
        if (kepair != null) {
            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            kmf.init(kepair, pwd);
            kms = kmf.getKeyManagers();
        }

        SSLContext ctx = SSLContext.getInstance("GMSSLv1.1", "GMJSSE");
        java.security.SecureRandom secureRandom = new java.security.SecureRandom();
        ctx.init(kms, trust, secureRandom);

        ctx.getServerSessionContext().setSessionCacheSize(8192);
        ctx.getServerSessionContext().setSessionTimeout(3600);

        SSLServerSocketFactory factory = ctx.getServerSocketFactory();
        return factory;
    }
}

class ReadLine {
    public static final byte[] CRLF = {'\r', '\n'};
    public static final byte CR = '\r';
    public static final byte LF = '\n';

    private static final int LINE_MAX_SIZE = 16384;

    public static byte[] read(DataInputStream in) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream s = new DataOutputStream(baos);
        boolean previousIsCR = false;

        int len = 0;
        byte b = 0;

        try {
            b = in.readByte();
            len++;
        } catch (EOFException e) {
            //2022.01.06
            //return new byte[0];
            return null;
        }

        while (true) {
            if (b == LF) {
                if (previousIsCR) {
                    s.flush();
                    byte[] rs = baos.toByteArray();
                    s.close();
                    return rs;
                } else {
                    /**
                     * 因为测试到java.sun.com网站，返回HTTP头的行结束符是"\n"，而不是FRC中规定的"\r\n"。
                     * IE可以正确解释，故修正为行结束判断为"\n"。
                     */
                    //s.write(b);

                    s.flush();
                    byte[] rs = baos.toByteArray();
                    s.close();
                    return rs;
                }
            } else if (b == CR) {
                if (previousIsCR) {
                    s.writeByte(CR);
                }
                previousIsCR = true;
            } else {
                if (previousIsCR) {
                    s.writeByte(CR);
                }
                previousIsCR = false;
                s.write(b);
            }

            if (len > LINE_MAX_SIZE) {
                s.close();
                throw new IOException("Reach line size limit");
            }

            try {
                b = in.readByte();
                len++;
            } catch (EOFException e) {
                s.flush();
                byte[] rs = baos.toByteArray();
                s.close();
                return rs;
            }
        }
    }
}
