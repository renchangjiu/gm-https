package cc.kkon;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

@WebServlet("get1")
public class TestServlet1 extends HttpServlet {


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("Thread.currentThread().getName() = " + Thread.currentThread().getName());
        System.out.println("get request......");
        Map<String, String[]> parameterMap = req.getParameterMap();

        this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("post");
        String ip = req.getParameter("ip");
        String pwd = req.getParameter("pwd");
        String post1 = req.getParameter("post1");
        String post2 = req.getParameter("post2");
        String token = req.getHeader("token");

        resp.setHeader("app-id", UUID.randomUUID().toString());
        ServletOutputStream out = resp.getOutputStream();
        out.write("Hello 世界!".getBytes(StandardCharsets.UTF_8));
        out.flush();
        System.out.println();
    }
}
