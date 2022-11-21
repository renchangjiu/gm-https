package cc.kkon.gmssl_cn.client;

import javax.net.ssl.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.KeyStore;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.Security;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * 单向认证（使用HttpsURLConnection）
 *
 * @author gmssl.cn
 */
public class Client3 {
    public Client3() {
    }

    public static void main(String[] args) {
        SSLSocketFactory fact = null;
        HttpsURLConnection conn = null;

        System.out.println("Usage: java -cp GMExample.jar client.Client3 url");

        try {
            String urlStr = "https://ebssec.boc.cn/";

            Security.insertProviderAt((Provider) Class.forName("cn.gmssl.jce.provider.GMJCE").newInstance(), 1);
            Security.insertProviderAt((Provider) Class.forName("cn.gmssl.jsse.provider.GMJSSE").newInstance(), 2);

            fact = createSocketFactory(null, null);
            SSLSocketFactory fact2 = new PreferredCipherSuiteSSLSocketFactory(fact);

            URL url = new URL(urlStr);
            conn = (HttpsURLConnection) url.openConnection();
            conn.setInstanceFollowRedirects(true);
            conn.setSSLSocketFactory(fact2);
            conn.setHostnameVerifier(new HostnameVerifier() {
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });

            conn.connect();

            InputStream input = conn.getInputStream();
            byte[] buffer = new byte[1024 * 4];
            int length = 0;
            while ((length = input.read(buffer)) != -1) {
                System.out.println(new String(buffer, 0, length));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                conn.disconnect();
            } catch (Exception e) {
            }
        }
    }

    public static SSLSocketFactory createSocketFactory(KeyStore kepair, char[] pwd) throws Exception {
        TrustAllManager[] trust = {new TrustAllManager()};

        KeyManager[] kms = null;
        if (kepair != null) {
            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            kmf.init(kepair, pwd);
            kms = kmf.getKeyManagers();
        }

        SSLContext ctx = SSLContext.getInstance("GMSSLv1.1", "GMJSSE");
        SecureRandom secureRandom = new SecureRandom();
        ctx.init(kms, trust, secureRandom);

        ctx.getServerSessionContext().setSessionCacheSize(8192);
        ctx.getServerSessionContext().setSessionTimeout(3600);

        SSLSocketFactory factory = ctx.getSocketFactory();
        return factory;
    }
}

class PreferredCipherSuiteSSLSocketFactory extends SSLSocketFactory {

    private static final String PREFERRED_CIPHER_SUITE = "ECC_SM4_CBC_SM3";

    private final SSLSocketFactory delegate;

    public PreferredCipherSuiteSSLSocketFactory(SSLSocketFactory delegate) {

        this.delegate = delegate;
    }

    @Override
    public String[] getDefaultCipherSuites() {

        return setupPreferredDefaultCipherSuites(this.delegate);
    }

    @Override
    public String[] getSupportedCipherSuites() {

        return setupPreferredSupportedCipherSuites(this.delegate);
    }

    @Override
    public Socket createSocket(String arg0, int arg1) throws IOException {

        Socket socket = this.delegate.createSocket(arg0, arg1);
        String[] cipherSuites = setupPreferredDefaultCipherSuites(delegate);
        ((SSLSocket) socket).setEnabledCipherSuites(cipherSuites);

        return socket;
    }

    @Override
    public Socket createSocket(InetAddress arg0, int arg1) throws IOException {

        Socket socket = this.delegate.createSocket(arg0, arg1);
        String[] cipherSuites = setupPreferredDefaultCipherSuites(delegate);
        ((SSLSocket) socket).setEnabledCipherSuites(cipherSuites);

        return socket;
    }

    @Override
    public Socket createSocket(Socket arg0, String arg1, int arg2, boolean arg3) throws IOException {

        Socket socket = this.delegate.createSocket(arg0, arg1, arg2, arg3);
        String[] cipherSuites = setupPreferredDefaultCipherSuites(delegate);
        ((SSLSocket) socket).setEnabledCipherSuites(cipherSuites);

        return socket;
    }

    @Override
    public Socket createSocket(String arg0, int arg1, InetAddress arg2, int arg3) throws IOException {

        Socket socket = this.delegate.createSocket(arg0, arg1, arg2, arg3);
        String[] cipherSuites = setupPreferredDefaultCipherSuites(delegate);
        ((SSLSocket) socket).setEnabledCipherSuites(cipherSuites);

        return socket;
    }

    @Override
    public Socket createSocket(InetAddress arg0, int arg1, InetAddress arg2, int arg3) throws IOException {

        Socket socket = this.delegate.createSocket(arg0, arg1, arg2, arg3);
        String[] cipherSuites = setupPreferredDefaultCipherSuites(delegate);
        ((SSLSocket) socket).setEnabledCipherSuites(cipherSuites);

        return socket;
    }

    private static String[] setupPreferredDefaultCipherSuites(SSLSocketFactory sslSocketFactory) {
        ArrayList<String> suitesList = new ArrayList<String>(Arrays.asList(PREFERRED_CIPHER_SUITE));
        return suitesList.toArray(new String[suitesList.size()]);
    }

    private static String[] setupPreferredSupportedCipherSuites(SSLSocketFactory sslSocketFactory) {
        ArrayList<String> suitesList = new ArrayList<String>(Arrays.asList(PREFERRED_CIPHER_SUITE));
        return suitesList.toArray(new String[suitesList.size()]);
    }
}
