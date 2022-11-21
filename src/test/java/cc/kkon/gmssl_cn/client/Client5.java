package cc.kkon.gmssl_cn.client;

import javax.net.SocketFactory;
import javax.net.ssl.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.net.InetSocketAddress;
import java.security.KeyStore;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

/**
 * 双向认证（验证服务端证书）
 *
 * @author gmssl.cn
 */
public class Client5 {
    public Client5() {
    }

    public static void main(String[] args) {
        SocketFactory fact = null;
        SSLSocket socket = null;

        System.out.println("Usage: java -cp GMExample.jar client.Client5 addr port");

        try {
            String addr = "demo.gmssl.cn";
            int port = 1443;
            String uri = "/";
            if (args.length > 0) {
                addr = args[0];
                port = Integer.parseInt(args[1]);
            }

            Security.insertProviderAt((Provider) Class.forName("cn.gmssl.jce.provider.GMJCE").newInstance(), 1);
            Security.insertProviderAt((Provider) Class.forName("cn.gmssl.jsse.provider.GMJSSE").newInstance(), 2);

            // 客户端密钥对
            String pfxfile = "keystore/sm2.user1.both.pfx";
            String pwd = "12345678";
            KeyStore pfx = KeyStore.getInstance("PKCS12", "GMJSSE");
            pfx.load(new FileInputStream(pfxfile), pwd.toCharArray());

            // 加载可信证书
            KeyStore trust = KeyStore.getInstance("PKCS12");
            trust.load(null);
            FileInputStream fin = new FileInputStream("keystore/sm2.oca.pem");
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            X509Certificate oca = (X509Certificate) cf.generateCertificate(fin);
            trust.setCertificateEntry("oca", oca);
            fin = new FileInputStream("keystore/sm2.rca.pem");
            X509Certificate rca = (X509Certificate) cf.generateCertificate(fin);
            trust.setCertificateEntry("rca", rca);

            // 创建Factory
            fact = createSocketFactory(pfx, pwd.toCharArray(), trust);
            socket = (SSLSocket) fact.createSocket();
            socket.setEnabledCipherSuites(new String[]{"ECC_SM4_CBC_SM3"});
            socket.setTcpNoDelay(true);

            socket.connect(new InetSocketAddress(addr, port), 2000);
            socket.setTcpNoDelay(true);
            socket.startHandshake();

            DataInputStream in = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());

            String s = "GET " + uri + " HTTP/1.1\r\n";
            s += "Accept: */*\r\n";
            s += "User-Agent: Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.1; Trident/4.0)\r\n";
            s += "Host: " + addr + (port == 443 ? "" : ":" + port) + "\r\n";
            s += "Connection: Close\r\n";
            s += "\r\n";
            out.write(s.getBytes());
            out.flush();

            System.out.println(socket.getSession().getCipherSuite());

            byte[] buf = new byte[8192];
            int len = in.read(buf);
            if (len == -1) {
                System.out.println("eof");
                return;
            }
            System.out.println(new String(buf, 0, len));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (Exception e) {
            }
        }
    }

    public static SSLSocketFactory createSocketFactory(KeyStore kepair, char[] pwd, KeyStore trustStore) throws Exception {
        KeyManager[] kms = null;
        if (kepair != null) {
            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            kmf.init(kepair, pwd);
            kms = kmf.getKeyManagers();
        }

        TrustManager[] tms = null;
        if (trustStore != null) {
            // 指定指定的证书验证
            TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
            tmf.init(trustStore);
            tms = tmf.getTrustManagers();
        } else {
            // 不验证(信任全部)
            tms = new TrustManager[1];
            tms[0] = new TrustAllManager();
        }

        SSLContext ctx = SSLContext.getInstance("GMSSLv1.1", "GMJSSE");
        SecureRandom secureRandom = new SecureRandom();
        ctx.init(kms, tms, secureRandom);

        ctx.getServerSessionContext().setSessionCacheSize(8192);
        ctx.getServerSessionContext().setSessionTimeout(3600);

        SSLSocketFactory factory = ctx.getSocketFactory();
        return factory;
    }
}
