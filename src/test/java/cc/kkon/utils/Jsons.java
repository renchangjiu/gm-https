package cc.kkon.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

/**
 * json utils
 *
 * @author yui
 */
public class Jsons {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    static {
        // 忽略未知属性
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * 将对象转换成json字符串
     *
     * @param data data
     * @return json string
     */
    public static String toJson(Object data) {
        try {
            return MAPPER.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 将对象转换成json 字节数组
     *
     * @param data data
     * @return json string
     */
    public static byte[] toJsonBytes(Object data) {
        try {
            return MAPPER.writeValueAsBytes(data);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 将json字符串转换成对象
     *
     * @param jsonData jsonData
     * @param beanType beanType
     * @return T
     */
    public static <T> T toBean(String jsonData, Class<T> beanType) {
        try {
            return MAPPER.readValue(jsonData, beanType);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 将json字节数组转换成对象
     *
     * @param jsonData jsonData
     * @param beanType beanType
     * @return T
     */
    public static <T> T toBean(byte[] jsonData, Class<T> beanType) {
        try {
            return MAPPER.readValue(jsonData, beanType);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 将json字符串转换成对象
     *
     * @param jsonData     jsonData
     * @param valueTypeRef valueTypeRef
     * @return T
     */
    public static <T> T toBean(String jsonData, TypeReference<T> valueTypeRef) {
        try {
            return (T) MAPPER.readValue(jsonData, valueTypeRef);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * 将json字符串转换成bean list
     *
     * @param jsonData jsonData
     * @param beanType beanType
     * @return T
     */
    public static <T> List<T> toList(String jsonData, Class<T> beanType) {
        JavaType javaType = MAPPER.getTypeFactory().constructParametricType(List.class, beanType);
        try {
            return MAPPER.readValue(jsonData, javaType);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
