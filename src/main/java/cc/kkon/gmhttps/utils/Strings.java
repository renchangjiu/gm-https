package cc.kkon.gmhttps.utils;


import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author yui
 */
public final class Strings extends StringUtils {
    public static final String COMMA = ",";

    public static void ifEmpty(String source, Runnable r) {
        if (isEmpty(source)) {
            r.run();
        }
    }

    public static void ifNotEmpty(String source, Consumer<String> consumer) {
        if (isNotEmpty(source)) {
            consumer.accept(source);
        }
    }

    public static void ifContains(String source, String sub, Consumer<Integer> consumer) {
        if (isNotEmpty(source)) {
            int idx = source.indexOf(sub);
            if (idx != -1) {
                consumer.accept(idx);
            }
        }
    }


    public static String htmlSpecialCharsEncode(String str) {
        str = str.replaceAll("&", "&amp;");
        str = str.replaceAll("<", "&lt;");
        str = str.replaceAll(">", "&gt;");
        return str;
    }

    /**
     * <p>单词首字母转大写</p>
     *
     * <pre>
     * StringUtil.firstLetterToUppercase(null)      = null
     * StringUtil.firstLetterToUppercase("")        = ""
     * StringUtil.firstLetterToUppercase(" ")       = " "
     * StringUtil.firstLetterToUppercase("bob")     = "Bob"
     * StringUtil.firstLetterToUppercase("1 bob  ") = "1  bob  "
     * </pre>
     */
    public static String firstLetterToUpper(String str) {
        return (str != null && str.length() >= 1) ? Character.toUpperCase(str.charAt(0)) + str.substring(1) : str;

    }

    private static final Pattern PATTERN = Pattern.compile("\\+");

    public static String encodeUri(String source) {
        if (isEmpty(source)) {
            return "";
        }
        try {
            String res = URLEncoder.encode(source, StandardCharsets.UTF_8.displayName());
            return PATTERN.matcher(res).replaceAll("%20");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static String decodeUri(String source) {
        try {
            return isEmpty(source) ? "" : URLDecoder.decode(source, StandardCharsets.UTF_8.displayName());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static String toStr(Object o) {
        return o == null ? "" : o.toString();
    }

    public static Integer toInteger(Object o) {
        try {
            if (o == null || "".equals(o.toString().trim())) {
                return null;
            }
            return Integer.parseInt(o.toString().trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static <T> List<T> trans(Object list) {
        return (List<T>) list;
    }

    public static String[] split(String str, String sep) {
        if (isEmpty(str)) {
            return new String[0];
        }
        return Arrays.stream(str.split(sep))
                .filter(StringUtils::isNotEmpty)
                .toArray(String[]::new);

    }

    public static String[] split(String str) {
        return split(str, COMMA);
    }

    public static List<String> split2list(String str, String sep) {
        List<String> list = new LinkedList<>();
        if (isEmpty(str)) {
            return list;
        }
        String[] split = split(str, sep);
        list.addAll(Arrays.asList(split));
        return list;
    }

    public static List<String> split2list(String str) {
        return split2list(str, COMMA);
    }

    public static String join(int[] ints, String sep) {
        return Arrays.stream(ints).boxed().map(String::valueOf).collect(Collectors.joining(sep));
    }

    /**
     * 返回两参数中不为空的参数. 优先返回第一个参数.
     */
    public static String getNotEmpty(String v1, String v2) {
        return isNotEmpty(v1) ? v1 : v2;
    }


    /**
     * 检查 CharSequence 是否等于给定字符集中的任何字符
     *
     * <pre>
     * equalsAny("a", "a", "c")     = true
     * equalsAny("a", "b", "c")     = false
     * equalsAny("a")               = false
     * equalsAny(null)              = false
     * equalsAny(null, null)        = false
     * </pre>
     *
     * @param cs  要检查的 CharSequence，可能为 null
     * @param css 要判断的字符，可能为空
     * @return true 如果等于任何字符, false 如果没有匹配或空输入
     */
    public static boolean equalsAny(CharSequence cs, CharSequence... css) {
        if (isEmpty(cs) || ArrayUtils.isEmpty(css)) {
            return false;
        }
        for (CharSequence ce : css) {
            if (cs.equals(ce)) {
                return true;
            }
        }
        return false;
    }

    private static final Pattern LINE_PATTERN = Pattern.compile("_(\\w)");
    private static final Pattern HUMP_PATTERN = Pattern.compile("[A-Z]");

    /**
     * 驼峰转下划线
     */
    public static String hump2line(String str) {
        if (isEmpty(str)) {
            return "";
        }
        Matcher matcher = HUMP_PATTERN.matcher(str);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, "_" + matcher.group(0).toLowerCase());
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    /**
     * 下划线转驼峰
     */
    public static String line2hump(String str) {
        if (isEmpty(str)) {
            return "";
        }
        str = str.toLowerCase();
        Matcher matcher = LINE_PATTERN.matcher(str);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, matcher.group(1).toUpperCase());
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    /**
     * 若 str 的长度不足 length, 则左侧补零
     */
    public static String addZero(String str, int length) {
        if (str == null) {
            str = "";
        }
        StringBuilder sb = new StringBuilder();
        int diff = length - str.length();
        for (int i = 0; i < diff; i++) {
            sb.append("0");
        }
        return sb + str;
    }

    /**
     * 若 i 的长度不足 length, 则左侧补零
     */
    public static String addZero(int i, int length) {
        return addZero(i + "", length);
    }

    public static String getAbsolutePath(String root, String relativePath) {
        String sep = "/";
        String path1 = root.replaceAll("\\\\", sep);
        String path2 = relativePath.replaceAll("\\\\", sep);

        if (path1.endsWith(sep)) {
            path1 = path1.substring(0, path1.length() - 1);
        }
        String[] split2 = Strings.split(path2, sep);
        for (String s : split2) {
            if (s.equals("..")) {
                path1 = path1.substring(0, path1.lastIndexOf(sep));
                path2 = path2.substring(path2.indexOf(sep) + 1);
            } else if (s.equals(".")) {
                path2 = path2.substring(path2.indexOf(sep) + 1);
            } else {
                return path1 + sep + path2;
            }
        }
        throw new RuntimeException("ERR");
    }
}
