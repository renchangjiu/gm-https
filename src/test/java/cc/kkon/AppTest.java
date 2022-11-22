package cc.kkon;


import cc.kkon.gmhttps.client.Response0;
import cc.kkon.gmhttps.client.SSLRequests;
import cc.kkon.gmhttps.server.SSLServer;
import cc.kkon.gmhttps.server.servelt.DefaultHttpServletRequest;
import cc.kkon.model.SysUser;
import cc.kkon.utils.Jsons;
import org.junit.Test;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.CountDownLatch;

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

        params.put("p1", "abc");
        params.put("p2", "def");
        params.put("p3", "中文");

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

        params.put("p1", "abc");
        params.put("p2", "def");
        params.put("p3", "中文");

        headers.put("token", UUID.randomUUID().toString());

        String url = "https://localhost:4430/post2";
        requests.post(url, params, headers);

        server.close();
    }

    @Test
    public void testPostJson() throws Exception {
        SSLServer server = this.startServer(false);
        server.addServlet("/post_json", new HttpServlet() {
            @Override
            protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
                // cast
                DefaultHttpServletRequest req1 = (DefaultHttpServletRequest) req;
                byte[] body = req1.getBody();
                SysUser user = Jsons.toBean(body, SysUser.class);
                System.out.println("user = " + user);
            }
        });
        SSLRequests requests = new SSLRequests();

        String url = "https://localhost:4430/post_json";

        SysUser user = new SysUser()
                .setStr1("abc")
                .setStr2("中文")
                .setInt1(123)
                .setDate1(new Date());
        String json = Jsons.toJson(user);
        requests.post4json(url, json, null);

        server.close();
    }

    @Test
    public void testConcurrency() throws Exception {
        SSLServer server = this.startServer(false);
        int threadNum = 40;
        CountDownLatch latch = new CountDownLatch(threadNum);
        server.addServlet("/concurrency", new HttpServlet() {
            @Override
            protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
                String threadName = Thread.currentThread().getName();
                String order = req.getParameter("order");
                String line1 = "latch.getCount() = " + latch.getCount();
                String line2 = "threadName = " + threadName;
                String line3 = "order = " + order;
                latch.countDown();
                System.out.println(line1 + ", " + line2 + ", " + line3);
            }
        });

        String url = "https://localhost:4430/concurrency";

        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < threadNum; i++) {
            int finalI = i;
            Thread t = new Thread(() -> {
                SSLRequests requests = new SSLRequests();

                Map<String, String> params = new HashMap<>();
                params.put("order", finalI + "");
                try {
                    requests.get(url, params, null);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            threads.add(t);
        }
        for (Thread thread : threads) {
            thread.start();
        }
        latch.await();

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

        String url = "https://localhost:4430/get1";

        SSLRequests requests = new SSLRequests(getClass().getClassLoader().getResourceAsStream(cert), pwd);
        requests.get(url);

        try {
            new SSLRequests().get(url);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
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
