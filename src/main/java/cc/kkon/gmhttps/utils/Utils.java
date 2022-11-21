package cc.kkon.gmhttps.utils;

import cc.kkon.gmhttps.model.FirstLine;
import cc.kkon.gmhttps.utils.Strings;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Utils {

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
        fl.method = split[0];
        fl.url = split[1];
        fl.version = split[2];
        return fl;
    }

    public static Map<String, String> parseParams(String paramsStr) {
        Map<String, String> params = new HashMap<>();
        String[] ss = Strings.split(paramsStr, "&");
        for (String s : ss) {
            String[] split = Strings.split(s, "=");
            if (split.length == 2) {
                params.put(split[0], split[1]);
            }
        }
        return params;
    }
}
