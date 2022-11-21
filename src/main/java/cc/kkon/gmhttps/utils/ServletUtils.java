package cc.kkon.gmhttps.utils;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ServletUtils {

    public static void write(HttpServletResponse resp, byte[] bytes) throws IOException {
        ServletOutputStream out = resp.getOutputStream();
        out.write(bytes);
        out.flush();
        out.close();
    }
}
