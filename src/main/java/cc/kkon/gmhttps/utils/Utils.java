package cc.kkon.gmhttps.utils;

import cc.kkon.gmhttps.client.TrustAllManager;
import cc.kkon.gmhttps.model.FirstLine;

import javax.net.ssl.*;
import java.io.*;
import java.nio.charset.Charset;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Utils {

    public static SSLServerSocketFactory createServerSocketFactory(KeyStore keyStore, char[] pwd) throws Exception {
        TrustManager[] trust = {new TrustAllManager()};

        KeyManager[] kms = null;
        if (keyStore != null) {
            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            kmf.init(keyStore, pwd);
            kms = kmf.getKeyManagers();
        }

        SSLContext ctx = SSLContext.getInstance("GMSSLv1.1", "GMJSSE");
        SecureRandom secureRandom = new SecureRandom();
        ctx.init(kms, trust, secureRandom);

        ctx.getServerSessionContext().setSessionCacheSize(8192);
        ctx.getServerSessionContext().setSessionTimeout(3600);

        return ctx.getServerSocketFactory();
    }


    public static Map<String, String> buildHeaders(List<String> headLine) {
        Map<String, String> res = new HashMap<>();
        for (String line : headLine) {
            String[] split = line.split(":");
            if (split.length == 2) {
                res.put(split[0], split[1].trim());
            }
        }
        return res;
    }

    public static FirstLine parse1stLine(String firstLine) {
        String[] split = Strings.split(firstLine, " ");
        FirstLine fl = new FirstLine();
        fl.method = split[0].toUpperCase();
        fl.url = split[1];
        fl.version = split[2];
        return fl;
    }

    public static Map<String, List<String>> parseParams(String paramsStr) {
        Map<String, List<String>> res = new HashMap<>();
        String[] ss = Strings.split(paramsStr, "&");
        for (String s : ss) {
            String[] split = s.split("=");
            String key = split[0];
            String val;
            if (split.length < 2) {
                val = "";
            } else {
                val = split[1];
            }
            val = Strings.decodeUri(val);
            if (res.containsKey(key)) {
                res.get(key).add(val);
            } else {
                List<String> vals = new LinkedList<>();
                vals.add(val);
                res.put(key, vals);
            }
        }
        return res;
    }

    public static Map<String, List<String>> merge(Map<String, List<String>> p1, Map<String, List<String>> p2) {
        if (p1 == null) {
            p1 = new HashMap<>();
        }
        if (p2 == null) {
            p2 = new HashMap<>();
        }
        for (Map.Entry<String, List<String>> ent : p1.entrySet()) {
            String key = ent.getKey();
            List<String> val = ent.getValue();
            if (p2.containsKey(key)) {
                val.addAll(p2.get(key));
            }
        }
        return p1;
    }


    public static void closeQuietly(final Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static String toString(final InputStream input, final Charset charset) throws IOException {
        BufferedInputStream bi = new BufferedInputStream(input);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int len;
        byte[] bytes = new byte[1024 * 8];
        while ((len = bi.read(bytes)) != -1) {
            out.write(bytes, 0, len);
        }
        String res = out.toString(charset.name());
        bi.close();
        out.close();
        return res;
    }

    public static boolean isNotEmpty(final Map<?, ?> map) {
        return !isEmpty(map);
    }

    public static boolean isEmpty(final Map<?, ?> map) {
        return map == null || map.isEmpty();
    }

    public static byte[] addAll(byte[] array1, byte[] array2) {
        if (array1 == null) {
            array1 = new byte[0];
        }
        if (array2 == null) {
            array2 = new byte[0];
        }
        final byte[] joinedArray = new byte[array1.length + array2.length];
        System.arraycopy(array1, 0, joinedArray, 0, array1.length);
        System.arraycopy(array2, 0, joinedArray, array1.length, array2.length);
        return joinedArray;
    }
}
