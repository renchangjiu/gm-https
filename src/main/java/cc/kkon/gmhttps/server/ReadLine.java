package cc.kkon.gmhttps.server;

import java.io.*;

class ReadLine {
    public static final byte[] CRLF = {'\r', '\n'};
    public static final byte CR = '\r';
    public static final byte LF = '\n';

    private static final int LINE_MAX_SIZE = 16384;

    public static byte[] read(DataInputStream in) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream s = new DataOutputStream(baos);
        boolean previousIsCR = false;

        int len = 0;
        byte b = 0;

        try {
            b = in.readByte();
            len++;
        } catch (EOFException e) {
            //2022.01.06
            //return new byte[0];
            return null;
        }

        while (true) {
            if (b == LF) {
                if (previousIsCR) {
                    s.flush();
                    byte[] rs = baos.toByteArray();
                    s.close();
                    return rs;
                } else {
                    /**
                     * 因为测试到java.sun.com网站，返回HTTP头的行结束符是"\n"，而不是FRC中规定的"\r\n"。
                     * IE可以正确解释，故修正为行结束判断为"\n"。
                     */
                    //s.write(b);

                    s.flush();
                    byte[] rs = baos.toByteArray();
                    s.close();
                    return rs;
                }
            } else if (b == CR) {
                if (previousIsCR) {
                    s.writeByte(CR);
                }
                previousIsCR = true;
            } else {
                if (previousIsCR) {
                    s.writeByte(CR);
                }
                previousIsCR = false;
                s.write(b);
            }

            if (len > LINE_MAX_SIZE) {
                s.close();
                throw new IOException("Reach line size limit");
            }

            try {
                b = in.readByte();
                len++;
            } catch (EOFException e) {
                s.flush();
                byte[] rs = baos.toByteArray();
                s.close();
                return rs;
            }
        }
    }
}
