package cc.kkon.gmhttps.server.servelt;

import cc.kkon.gmhttps.utils.Utils;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @author yui
 */
public class DefaultServletOutputStream extends ServletOutputStream {

    private ByteArrayOutputStream out;


    public DefaultServletOutputStream() {
        this.out = new ByteArrayOutputStream();
    }

    @Override
    public boolean isReady() {
        throw new RuntimeException();
    }

    @Override
    public void setWriteListener(WriteListener listener) {
        throw new RuntimeException();
    }

    @Override
    public void write(int b) throws IOException {
        this.out.write(b);
    }

    @Override
    public void close() throws IOException {
        Utils.closeQuietly(this.out);
    }

    public byte[] toByteArray() {
        try {
            this.out.flush();
            return this.out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}
