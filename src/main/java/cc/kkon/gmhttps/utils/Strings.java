package cc.kkon.gmhttps.utils;


import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.regex.Pattern;

/**
 * @author yui
 */
public final class Strings {
    public static final String COMMA = ",";

    public static boolean isEmpty(final CharSequence cs) {
        return cs == null || cs.length() == 0;
    }

    public static boolean isNotEmpty(final CharSequence cs) {
        return !isEmpty(cs);
    }

    public static boolean isNotBlank(final CharSequence cs) {
        return !isBlank(cs);
    }

    public static boolean isBlank(final CharSequence cs) {
        final int strLen = length(cs);
        if (strLen == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(cs.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static int length(final CharSequence cs) {
        return cs == null ? 0 : cs.length();
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


    public static String[] split(String str, String sep) {
        if (isEmpty(str)) {
            return new String[0];
        }
        return Arrays.stream(str.split(sep))
                .filter(Strings::isNotEmpty)
                .toArray(String[]::new);

    }

    public static String[] split(String str) {
        return split(str, COMMA);
    }


}
