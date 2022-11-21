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
    public void testClient() throws Exception {
        String url = "";
        // url = "https://ebssec.boc.cn/";
        url = "https://localhost:4430/get1";

        Map<String, String> params = new HashMap<>();
        Map<String, String> headers = new HashMap<>();

        params.put("ip", "192.168.1.1");
        params.put("pwd", "12345678");
        headers.put("token", UUID.randomUUID().toString());

        Response0 r2 = SSLRequests.get(url, params, headers);
        System.out.println("r2.getHeader(\"app-id\") = " + r2.getHeader("app-id"));
        System.out.println("r2.getContent() = " + r2.getContent());

        params.put("post1", "begin--abc--end");
        params.put("post2", UUID.randomUUID().toString());
        url = "https://localhost:4430/post1";
        SSLRequests.post(url, params, headers);

        String json = "{" +
                "\"a\": \"abc\"," +
                "\"b\": 123" +
                "}";
        Response0 r3 = SSLRequests.post4json(url, json, headers);
        System.out.println();
    }

    @Test
    public void testServer() throws Exception {
        String cert = "keystore/sm2.server1.both.pfx";
        cert = "certs/sm2.auth1.both.pfx";
        InputStream in = getClass().getClassLoader().getResourceAsStream(cert);
        String pwd = "12345678";
        SSLServer server = new SSLServer(4430, in, pwd);

        server.addServlet("/get1", new TestServlet1());
        server.addServlet(new TestServlet2());

        server.listen();

        Thread.currentThread().join();
    }


}
