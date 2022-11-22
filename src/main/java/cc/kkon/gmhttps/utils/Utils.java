package cc.kkon.gmhttps.utils;

import cc.kkon.gmhttps.client.TrustAllManager;
import cc.kkon.gmhttps.model.FirstLine;

import javax.net.ssl.*;
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


}
