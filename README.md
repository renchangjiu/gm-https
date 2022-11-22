# gm-https
支持国密 https 的<b>低</b>性能 servlet 容器。支持单向认证及双向认证。

<b>有限</b>支持 servlet 规范。

<b>不建议</b>用在正式环境。

项目依赖的 gmssl_provider 来自 [https://gmssl.cn/gmssl](https://gmssl.cn/gmssl)。*官网说明：免费版本每年年底失效，程序会自动退出，需更新库，重新链接。请勿用于正式/生产环境，后果自负。*

#### 一、用法
##### 1. client 端
```java
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
```

##### 2. server 端
```java
    @Test
    public void testServer() throws Exception {
        String cert = "keystore/sm2.server1.both.pfx";
        cert = "sm2.auth1/sm2.auth1.both.pfx";
        InputStream in = getClass().getClassLoader().getResourceAsStream(cert);
        String pwd = "12345678";
        SSLServer server = new SSLServer(4430, in, pwd);

        server.addServlet("/get1", new TestServlet1());
        server.addServlet(new TestServlet2());
		
        // 异步
        server.listen();

        Thread.currentThread().join();
    }
```

##### 3. 详细用法参考测试用例


#### 二、maven 坐标

```xml
<groupId>cc.kkon</groupId>
<artifactId>gm-https</artifactId>
<version>0.2</version>
```
