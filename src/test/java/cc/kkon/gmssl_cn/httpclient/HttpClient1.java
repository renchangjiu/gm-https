package cc.kkon.gmssl_cn.httpclient;

import org.apache.commons.collections4.MapUtils;
import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 使用HttpClient访问国密https
 *
 * @author gmssl.cn
 */
public class HttpClient1 {
    private static final String ENCODING = "UTF-8";
    private static HttpClient client = null;


    // 创建SSL上下文---忽略服务端证书信任
    static SSLContext createIgnoreVerifySSL() throws NoSuchAlgorithmException, KeyManagementException, NoSuchProviderException {
        SSLContext sc = SSLContext.getInstance(cn.gmssl.jsse.provider.GMJSSE.GMSSLv11, cn.gmssl.jsse.provider.GMJSSE.NAME);

        // 实现一个X509TrustManager接口，用于绕过验证，不用修改里面的方法
        X509TrustManager trustManager = new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] paramArrayOfX509Certificate, String paramString) throws CertificateException {
                for (int i = 0; i < paramArrayOfX509Certificate.length; i++) {
                    System.out.println(paramArrayOfX509Certificate[i].getSubjectDN().getName());
                }
                System.out.println();
            }

            @Override
            public void checkServerTrusted(X509Certificate[] paramArrayOfX509Certificate, String paramString) throws CertificateException {
                for (int i = 0; i < paramArrayOfX509Certificate.length; i++) {
                    System.out.println(paramArrayOfX509Certificate[i].getSubjectDN().getName());
                }
                System.out.println();
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }
        };
        sc.init(null, new TrustManager[]{trustManager}, null);
        return sc;
    }

    private static void initGMSSL() {
        try {
            Security.insertProviderAt(new cn.gmssl.jce.provider.GMJCE(), 1);
            Security.insertProviderAt((Provider) Class.forName("cn.gmssl.jsse.provider.GMJSSE").newInstance(), 2);

            SSLContext sslContext = createIgnoreVerifySSL();

            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext,
                    new String[]{"GMSSLv1.1"}, new String[]{"ECC_SM4_CBC_SM3"},
                    SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

            Registry<ConnectionSocketFactory> socketFactoryRegistry =
                    RegistryBuilder.<ConnectionSocketFactory>create()
                            .register("https", sslsf).build();

            int timeout = 5;
            RequestConfig config = RequestConfig.custom()
                    .setConnectTimeout(timeout * 1000)
                    .setConnectionRequestTimeout(timeout * 1000)
                    .setSocketTimeout(timeout * 1000).build();

            HttpClientBuilder b = HttpClientBuilder.create()
                    .setConnectionManager(new BasicHttpClientConnectionManager(socketFactoryRegistry))
                    .setMaxConnPerRoute(20)
                    .setMaxConnTotal(400)
                    .setDefaultRequestConfig(config);


            client = b.build();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void post(String url, Map<String, String> paramMap, Map<String, String> headerMap) throws IOException {
        HttpPost httpPost = new HttpPost(url);
        httpPost.setProtocolVersion(HttpVersion.HTTP_1_1);

        /*
         * 处理参数
         */
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        if (MapUtils.isNotEmpty(paramMap)) {
            Set<String> keySet = paramMap.keySet();
            for (String key : keySet) {
                params.add(new BasicNameValuePair(key, paramMap.get(key)));
            }
        }

        /*
         * 设置头信息
         */
        if (MapUtils.isNotEmpty(headerMap)) {
            Set<String> keySet = headerMap.keySet();
            for (String key : keySet) {
                httpPost.addHeader(key, headerMap.get(key));
            }
        }

        httpPost.setEntity(new UrlEncodedFormEntity(params, ENCODING));

        HttpResponse response = client.execute(httpPost);

        StatusLine status = response.getStatusLine();
        System.out.println("Reponse status=" + status.getStatusCode());

        HttpEntity entity = response.getEntity();
        InputStream inputStream = entity.getContent();

        // 文件保存位置
        File saveDir = new File(".");
        if (!saveDir.exists()) {
            saveDir.mkdirs();
        }

        //输出
        File file = new File(saveDir + File.separator + "testssl.doc");
        FileOutputStream fos = new FileOutputStream(file);

        byte[] b = new byte[1024];
        int len = 0;
        while ((len = inputStream.read(b)) != -1) {
            fos.write(b, 0, len);
        }
        fos.close();

        inputStream.close();
        httpPost.abort();
    }

    public static void get(String url) throws IOException {
        HttpGet httpGet = new HttpGet(url);
        httpGet.setProtocolVersion(HttpVersion.HTTP_1_1);

        HttpResponse response = client.execute(httpGet);

        StatusLine status = response.getStatusLine();
        System.out.println("Reppnse status=" + status.getStatusCode());

        HttpEntity entity = response.getEntity();
        InputStream inputStream = entity.getContent();

        byte[] b = new byte[1024];
        int len = 0;
        while ((len = inputStream.read(b)) != -1) {
            System.out.print(new String(b, 0, len));
        }

        httpGet.abort();
    }

    public static void main(String[] args) {
        try {
            //初始化
            initGMSSL();

            // 测试GET
            String url;
            // url = "https://ebssec.boc.cn/";
              url = "https://localhost/echo";
            //   url = "https://localhost/a";
            // url = "https://www.baidu.com/";
            HttpClient1.get(url);

            HttpClient1.get(url);


            // HttpClient1.post();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

