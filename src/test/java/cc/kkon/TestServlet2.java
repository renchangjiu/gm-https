package cc.kkon;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

@WebServlet("/post2")
public class TestServlet2 extends HttpServlet {


    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("Thread.currentThread().getName() = " + Thread.currentThread().getName());
        System.out.println("------post request------");

        Map<String, String[]> parameterMap = req.getParameterMap();
        for (Map.Entry<String, String[]> ent : parameterMap.entrySet()) {
            System.out.println("ent.getKey() = " + ent.getKey());
            System.out.println("ent.getValue() = " + Arrays.toString(ent.getValue()));
        }
        System.out.println();

        ArrayList<String> headerNames = Collections.list(req.getHeaderNames());
        for (String headerName : headerNames) {
            System.out.println("headerName = " + headerName);
            System.out.println("req.getHeader(headerName) = " + req.getHeader(headerName));
        }
        System.out.println();

        resp.setHeader("app-id", UUID.randomUUID().toString());
        ServletOutputStream out = resp.getOutputStream();
        out.write("Hello 世界!".getBytes(StandardCharsets.UTF_8));
        out.flush();
    }
}
