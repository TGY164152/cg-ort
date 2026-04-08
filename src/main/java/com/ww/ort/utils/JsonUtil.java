package com.ww.ort.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.Set;

public class JsonUtil {

    private static final Logger log = LoggerFactory.getLogger(JsonUtil.class);

    private static final Gson gson;

    static {
        gson = new GsonBuilder()
                // 自定义命名策略：驼峰转下划线
//                .setFieldNamingStrategy(f -> {
//                    String name = f.getName();
//                    StringBuilder sb = new StringBuilder();
//                    for (int i = 0; i < name.length(); i++) {
//                        char c = name.charAt(i);
//                        if (Character.isUpperCase(c)) {
//                            sb.append('_').append(Character.toLowerCase(c));
//                        } else {
//                            sb.append(c);
//                        }
//                    }
//                    return sb.toString();
//                })

//                // 设置日期格式
//                .setDateFormat("yyyy-MM-dd HH:mm:ss")

                // 禁用时间戳，GSON 默认就是日期转字符串
                //.disableHtmlEscaping() // 视需求，可选

                .create();
    }

    /**
     * 将任意对象（单个对象或集合）转换为 JSON 字符串（字段转下划线、时间格式化）
     */
    public static String toJson(Object obj) {
        return gson.toJson(obj);
    }

    /**
     * 将任意对象（单个对象或集合）转换为目标对象（字段转下划线、时间格式化）
     * @param value
     * @param clazz
     * @return
     * @param <T>
     */
    public static <T> T toObject(Object value, Class<T> clazz) {
        if (value instanceof String) {
            // value 是 JSON 字符串，反序列化
            return gson.fromJson((String) value, clazz);
        } else {
            // GSON 不支持直接转换 Java 对象成另一个对象
            // 这里先转成 JsonElement，再转成目标对象
            JsonElement jsonElement = gson.toJsonTree(value);
            return gson.fromJson(jsonElement, clazz);
        }
    }

    /**
     * 将任意对象（单个对象或集合）转换为 JSON 对象（字段转下划线、时间格式化）
     * @param value
     * @param typeToken
     * @return
     * @param <T>
     */
    public static <T> T toObject(Object value, TypeToken<T> typeToken) {
        if (value instanceof String) {
            return gson.fromJson((String) value, typeToken.getType());
        } else {
            JsonElement jsonElement = gson.toJsonTree(value);
            return gson.fromJson(jsonElement, typeToken.getType());
        }
    }

    /**
     * 将[]转为{}
     */
    public static String convertBrackets(String jsonArrayStr) {
        if (jsonArrayStr == null) {
            return "{}";
        }

        String trimmed = jsonArrayStr.trim();

        // 处理空数组
        if ("[]".equals(trimmed)) {
            return "{}";
        }

        // 验证格式
        if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
            String content = trimmed.substring(1, trimmed.length() - 1).trim();
            return "{" + content + "}";
        }

        throw new IllegalArgumentException("无效的 JSON 数组格式: " + jsonArrayStr);
    }

    /**
     *  将ASCII码字符串转换为JSON字符串
     * @param msg
     * @return
     */
    public static String convertAsciiStrToJson(String msg) {
        if (msg == null) return null;

        if (!msg.contains(",")) return msg;

        String[] nums = msg.split(",");
        for (String num : nums) {
            if (!num.matches("\\d+")) {
                return msg;
            }
        }

        try {
            StringBuilder sb = new StringBuilder();
            for (String num : nums) {
                int ascii = Integer.parseInt(num);
                if (ascii < 32 || ascii > 126) {
                    throw new IllegalArgumentException("ASCII 编码中存在非法字符: " + ascii);
                }
                sb.append((char) ascii);
            }

            String jsonStr = sb.toString();

            // JSON 验证
            ObjectMapper mapper = new ObjectMapper();
            mapper.readTree(jsonStr);

            return jsonStr;

        } catch (Exception e) {
            log.error("ASCII 转换失败或结果不是合法 JSON，原始消息简略：{}",
                    msg != null ? msg.substring(0, Math.min(msg.length(), 100)) : "null", e);
            throw new RuntimeException("转换失败：无效的 ASCII 或非 JSON 格式", e);
        }
    }
}