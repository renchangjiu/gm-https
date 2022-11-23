package cc.kkon.gmhttps.client;

import cc.kkon.gmhttps.utils.Utils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 请求工具类, 线程不安全
 */
public class SSLRequests {

    private final HttpClient client;

    private static final Charset CHARSET = StandardCharsets.UTF_8;


    private static final int timeout = 30 * 1000;


    /**
     * 单向认证
     */
    public SSLRequests() {
        this.client = ClientBuilder.initGMSSL();
    }

    /**
     * 双向认证
     *
     * @param cert 证书
     * @param pwd  证书密码
     */
    public SSLRequests(InputStream cert, String pwd) {
        this.client = ClientBuilder.initGMSSL(cert, pwd);
    }

    public Response0 post(String url, Map<String, String> params, Map<String, String> headers) throws IOException {
        HttpPost httpPost = new HttpPost(url);
        httpPost.setProtocolVersion(HttpVersion.HTTP_1_1);
        putHeaders(httpPost, headers);
        /*
         * 处理参数
         */
        List<NameValuePair> list = new ArrayList<>();
        if (Utils.isNotEmpty(params)) {
            Set<String> keySet = params.keySet();
            for (String key : keySet) {
                list.add(new BasicNameValuePair(key, params.get(key)));
            }
        }

        httpPost.setEntity(new UrlEncodedFormEntity(list, CHARSET));

        HttpResponse response = client.execute(httpPost);
        Response0 res0 = new Response0(response);
        httpPost.abort();
        return res0;
    }

    public Response0 post4json(String url, String json, Map<String, String> headers) throws IOException {
        HttpPost httpPost = new HttpPost(url);
        httpPost.setProtocolVersion(HttpVersion.HTTP_1_1);
        putHeaders(httpPost, headers);

        StringEntity entity = new StringEntity(json, ContentType.APPLICATION_JSON);

        httpPost.setEntity(entity);

        HttpResponse response = client.execute(httpPost);
        Response0 res0 = new Response0(response);
        httpPost.abort();
        return res0;
    }

    public Response0 get(String url) throws IOException {
        return get(url, null, null);
    }

    public Response0 get(String url, Map<String, String> params, Map<String, String> headers) throws IOException {
        HttpGet httpGet;
        try {
            URIBuilder uriBuilder = new URIBuilder(url);
            // 设置请求参数
            if (params != null && params.size() != 0) {
                List<NameValuePair> list = new LinkedList<>();
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    list.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
                }
                uriBuilder.setParameters(list);
            }

            httpGet = new HttpGet(uriBuilder.build());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        // 设置超时时间
        if (timeout > 0) {
            RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(timeout).setConnectTimeout(timeout).build();
            httpGet.setConfig(requestConfig);
        }

        httpGet.setProtocolVersion(HttpVersion.HTTP_1_1);

        putHeaders(httpGet, headers);
        HttpResponse response = client.execute(httpGet);

        Response0 res0 = new Response0(response);
        httpGet.abort();
        return res0;
    }

    /**
     * 设置请求头
     */
    private static void putHeaders(HttpRequestBase httpMethod, Map<String, String> headers) {
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                httpMethod.setHeader(entry.getKey(), entry.getValue());
            }
        }
    }
}
