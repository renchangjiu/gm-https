package cc.kkon;


import cc.kkon.gmhttps.client.Response0;
import cc.kkon.gmhttps.client.SSLRequests;
import cc.kkon.gmhttps.server.SSLServer;
import org.junit.Test;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Unit test for simple App.
 */
public class AppTest {

    @Test
    public void testGet() throws Exception {
        SSLServer server = this.startServer(false);
        SSLRequests requests = new SSLRequests();

        String url = "https://localhost:4430/get1";

        Map<String, String> params = new HashMap<>();
        Map<String, String> headers = new HashMap<>();

        params.put("ip", "192.168.1.1");
        params.put("pwd", "12345678");

        headers.put("token", UUID.randomUUID().toString());

        Response0 r0 = requests.get(url, params, headers);
        System.out.println("r0.getHeader(\"app-id\") = " + r0.getHeader("app-id"));
        System.out.println("r0.getContent() = " + r0.getContent());

        server.close();
    }

    @Test
    public void testPost() throws Exception {
        SSLServer server = this.startServer(false);
        SSLRequests requests = new SSLRequests();

        Map<String, String> params = new HashMap<>();
        Map<String, String> headers = new HashMap<>();

        params.put("ip", "192.168.1.1");
        params.put("pwd", "12345678");
        headers.put("token", UUID.randomUUID().toString());

        params.put("post1", "begin--abc--end");
        params.put("post2", UUID.randomUUID().toString());
        String url = "https://localhost:4430/post2";
        requests.post(url, params, headers);

        server.close();
    }

    @Test
    public void testPostJson() throws Exception {
        SSLServer server = this.startServer(false);
        SSLRequests requests = new SSLRequests();

        Map<String, String> params = new HashMap<>();
        Map<String, String> headers = new HashMap<>();

        params.put("post1", "begin--abc--end");
        params.put("post2", UUID.randomUUID().toString());

        headers.put("token", UUID.randomUUID().toString());
        String url = "https://localhost:4430/post2";

        String json = "{" +
                "\"a\": \"abc\"," +
                "\"b\": 123" +
                "}";
        Response0 r3 = requests.post4json(url, json, headers);

        server.close();
    }

    /**
     * 双向认证
     */
    @Test
    public void testTwoWayAuth() throws Exception {
        SSLServer server = this.startServer(true);
        String cert = "certs/sm2.user.both.pfx";
        String pwd = "12345678";
        SSLRequests requests = new SSLRequests(getClass().getClassLoader().getResourceAsStream(cert), pwd);
        String url = "https://localhost:4430/get1";

        Response0 r0 = requests.get(url);
        Thread.sleep(4000);

        server.close();
    }

    private SSLServer startServer(boolean twoWayAuth) throws InterruptedException {
        String cert = "certs/sm2.server.both.pfx";
        String pwd = "12345678";
        InputStream in = getClass().getClassLoader().getResourceAsStream(cert);
        SSLServer server = new SSLServer(4430, in, pwd, twoWayAuth);

        server.addServlet("/get1", new TestServlet1());
        server.addServlet(new TestServlet2());

        // 异步
        server.listen();
        Thread.sleep(4000);
        return server;
    }


}
