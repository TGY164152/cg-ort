package com.ww.ort.utils;

import java.lang.reflect.Field;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DataUtil {

    // 正则预编译（性能更好）
    private static final Pattern INT_PATTERN = Pattern.compile("\\d+");
    private static final Pattern DOUBLE_PATTERN = Pattern.compile("\\d+(\\.\\d+)?");

    /**
     * 获取百分点位的值
     * @param data 数组
     * @param percentile 百分点位，0.1表示10%点位的值
     * @return 数组中该点位的值，这里是求数组中的值
     */
    public static double findPercentileValue(double[] data, double percentile) {
        // 将数组按升序排序
        Arrays.sort(data);

        // 计算指定百分位的索引位置
        int index = (int) Math.floor(data.length * percentile);

        // 返回在排序后数组中的值
        return data[index];
    }

    /**
     * 获取百分点位的值
     * @param data 数组
     * @param percentile 百分点位，0.1表示10%点位的值
     * @return 数组中该点位的值，这里是求数组中的值
     */
    public static Double findPercentileValue(Double[] data, Double percentile) {
        // 将数组按升序排序
        Arrays.sort(data);

        // 计算指定百分位的索引位置
        int index = (int) Math.floor(data.length * percentile);

        // 返回在排序后数组中的值
        return data[index];
    }

    // 查看是否是数字 是为true
    public static boolean isNumeric(String str) {try {Double.parseDouble(str);return true;} catch(Exception e){return false;}}

    public static void main(String[] args) {
        //Double[] d1 = new Double[10];
        double[] d2 = new double[10];

        d2[0] = 10d;
        d2[1] = 10d;
        d2[2] = 10d;
        d2[3] = 10d;
        d2[4] = 10d;
        d2[5] = 10d;
        d2[6] = 10d;
        d2[7] = 10d;
        d2[8] = 10d;
        d2[9] = 1d;

        //System.out.println(findPercentileValue(d1, 0.2));
        System.out.println(findPercentileValue(d2, 0.0));
    }


    /**
     * 将各个字段列表的Map转换为对象列表
     * @param map
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> List<T> mapToObjects(Map<String, List<Object>> map, Class<T> clazz) {
        List<T> result = new ArrayList<>();

        // 获取任意一个key的长度
        int size = map.values().stream().findFirst().map(List::size).orElse(0);

        try {
            for (int i = 0; i < size; i++) {
                T obj = clazz.getDeclaredConstructor().newInstance();

                for (Map.Entry<String, List<Object>> entry : map.entrySet()) {
                    String fieldName = entry.getKey();
                    List<Object> values = entry.getValue();

                    if (i < values.size()) {
                        Object value = values.get(i);
                        try {
                            Field field = clazz.getDeclaredField(fieldName);
                            field.setAccessible(true);
                            field.set(obj, value);
                        } catch (NoSuchFieldException e) {
                            // 如果对象中没有这个字段，忽略
                        }
                    }
                }

                result.add(obj);
            }
        } catch (Exception e) {
            throw new RuntimeException("转换失败", e);
        }

        return result;
    }

    /**
     * 根据字段类型转换值
     */
    public static Object convertValue(String str, String fieldType) {
        switch (fieldType) {
            case "String":
                return str;

            case "Integer":
                return extractInteger(str);

            case "Long":
                return extractLong(str);

            case "Double":
                return extractDouble(str);

            default:
                return str;
        }
    }

    /**
     * 获取整数
     */
    private static Integer extractInteger(String str) {
        Matcher matcher = INT_PATTERN.matcher(str);
        if (matcher.find()) {
            return Integer.valueOf(matcher.group());
        }
        return null;
    }

    /**
     * 获取长整型
     */
    private static Long extractLong(String str) {
        Matcher matcher = INT_PATTERN.matcher(str);
        if (matcher.find()) {
            return Long.valueOf(matcher.group());
        }
        return null;
    }

    /**
     * 获取小数
     */
    private static Double extractDouble(String str) {
        Matcher matcher = DOUBLE_PATTERN.matcher(str);
        if (matcher.find()) {
            return Double.valueOf(matcher.group());
        }
        return null;
    }

    /**
     * 模拟 Excel 的 PERCENTILE(array, p) 函数
     * @param values 数值集合，例如 E19:E50 对应的数据
     * @param p 百分位（0.0 ~ 1.0），例如 0.632, 0.1, 0.05
     * @return 对应百分位的数值
     */
    public static double percentile(List<Double> values, double p) {
        if (values == null || values.isEmpty()) {
            throw new IllegalArgumentException("数据集合不能为空");
        }
        if (p < 0 || p > 1) {
            throw new IllegalArgumentException("百分位 p 必须在 0 到 1 之间");
        }

        // 1. 排序（升序）
        Collections.sort(values);

        int n = values.size();
        // 2. 计算位置（Excel 算法）
        double pos = (n - 1) * p;
        int k = (int) Math.floor(pos);
        double d = pos - k;

        // 3. 边界条件
        if (k >= n - 1) {
            return values.get(n - 1);
        }
        if (k < 0) {
            return values.get(0);
        }

        // 4. 线性插值计算
        return values.get(k) + d * (values.get(k + 1) - values.get(k));
    }
}
