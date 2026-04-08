package com.ww.ort.utils;
import java.lang.reflect.Field;

public class FieldUtil {

    /**
     * 获取对象字段值
     * @param obj
     * @param fieldName
     * @return
     */
    public static Object getFieldValue(Object obj, String fieldName) {
        try {
            Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(obj);
        } catch (Exception e) {
            throw new RuntimeException("获取属性值失败: " + fieldName, e);
        }
    }
}
