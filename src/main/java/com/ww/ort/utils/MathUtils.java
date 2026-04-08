package com.ww.ort.utils;

import org.apache.commons.math3.distribution.NormalDistribution;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 计算类
 */
public class MathUtils {

    /**
     * 将 Double 类型的值保留指定的小数位数（四舍五入）
     *
     * @param value  需要格式化的值，可以为 null
     * @param scale  保留的小数位数（例如 2 表示保留两位）
     * @return 格式化后的 Double 值；如果 value 为 null，则返回 null
     */
    public static Double round(Double value, int scale) {
        if (value == null) {
            return null;
        }
        if (scale < 0) {
            throw new IllegalArgumentException("Scale must be non-negative");
        }

        BigDecimal bd = new BigDecimal(value.toString());
        bd = bd.setScale(scale, RoundingMode.HALF_UP); // 四舍五入
        return bd.doubleValue();
    }

    /**
     * 计算集合的平均值
     * @param data 集合
     * @return
     */
    public static Double calculateMean(List<Double> data) {
        return data.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    }

    /**
     * 计算集合的总体方差
     * @param data
     * @param mean
     * @return
     */
    public static Double calculateVariance(List<Double> data, Double mean) {
        return data.stream()
                .mapToDouble(x -> Math.pow(x - mean, 2))
                .average()
                .orElse(0.0);
    }

    /**
     * 计算集合的样本方差
     * @param data
     * @param mean
     * @return
     */
    public static Double calculateSampleVariance(List<Double> data, Double mean) {
        if (data == null) {
            return null;
        }

        // 方差（n - 1）
        Double varianceSum = data.stream()
                .mapToDouble(x -> {
                    Double diff = x - mean;
                    return diff * diff;
                })
                .sum();

        return varianceSum / (data.size() - 1);
    }

    /**
     * 计算标准差
     * @param variance 方差
     * @return
     */
    public static Double calculateStandardDeviation(Double variance) {
            return Math.sqrt(variance);
    }

    /**
     * 计算 CPK 值
     * @param usl 上限
     * @param lsl 下限
     * @param mean 均值
     * @param stdDev 标准差
     * @return
     */
    public static Double calculateCPK(Double usl, Double lsl, Double mean, Double stdDev) {
        Double cpk = null;

        if (stdDev == 0){
            return null;
        }

        if (usl != null && lsl == null){
            Double cpkUpper = (usl - mean) / (3 * stdDev);
            cpk = cpkUpper;
        }else if (lsl != null && usl == null){
            Double cpkLower = (mean - lsl) / (3 * stdDev);
            cpk = cpkLower;
        }else {
            Double cpkUpper = (usl - mean) / (3 * stdDev);
            Double cpkLower = (mean - lsl) / (3 * stdDev);
            cpk = Math.min(cpkUpper, cpkLower);
        }
        return cpk;
    }

    /**
     * 获取区间频次 直方图
     * @param interval
     * @param increment
     * @param min
     * @param data
     */
    private static void intervalFrequency(Integer[] interval, double increment, double min, List<Double> data) {  // 区间频次 直方图
        for (Double datum : data) {
            int v = (int)((datum - min) / increment);
            v = v < 0 ? 0 : Math.min(v, interval.length - 1);
            interval[v] = (interval[v] == null ? 0 : interval[v]) + 1;
        }
    }

    /**
     * 获取正态分布图
     * @param dataList 集合
     * @param mapData 存放数据
     */
    public static void normalDistribution(List<Double> dataList, Map<String, Object> mapData) {  // 将xy轴和密度曲线数据放到数组中
        if (dataList.size() == 0) return;
        Collections.sort(dataList);
//        if (data.length > 100) {
//            int removeCount = (int) Math.floor(data.length * 0.05); // 5%
//            double[] data2 = Arrays.copyOfRange(data, removeCount, data.length - removeCount);
//            data = data2;
//        }
//        if (dataList.size() > 10) {
//            List<Double> dataList2 = dataList.subList(5, dataList.size() - 5);
//            dataList = dataList2;
//        }
//
//        double min = dataList.stream().mapToDouble(Double::doubleValue).min().orElse(0.0);
//        double max = dataList.stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
//
//        if (mapData.containsKey("lsl") && mapData.containsKey("usl")){
//            min =(double)mapData.get("lsl");
//            max =(double)mapData.get("usl");
//        }

        double min =(double)mapData.get("lsl");
        double max =(double)mapData.get("usl");

        //标准值
        Double standardValue = calculateMean(Arrays.asList(min, max));
        // 均值
        Double mean = calculateMean(dataList);
        // 方差
        Double variance = calculateVariance(dataList, mean);
        // 标准差
        Double stdDev = calculateStandardDeviation(variance);
        // CPK
        Double cpk = calculateCPK(max, min, mean, stdDev);
        // OK数
        Integer okNum = (int) dataList.stream().filter(x -> x >= min && x <= max).count();
        // 良率
        Double okRate = okNum * 100.0 / dataList.size();

        NormalDistribution normalDist = new NormalDistribution(mean, stdDev);
        int groupNum = 20; // 组数
        Double[] xValues = new Double[groupNum];
        Integer[] yValues = new Integer[groupNum];
        Double[] normalData = new Double[groupNum];
        double increment = (max - min) / groupNum;
        double current = min;
        for (int i = 0; i < groupNum; i++) {  // 正态分布图的密度曲线
            normalData[i] = normalDist.density(current); // generate the normal distribution of each data point
            xValues[i] = current;
            current += increment;
        }
        // 获取直方图
        intervalFrequency(yValues, increment, min, dataList);
        mapData.put("x", Arrays.asList(xValues));
        mapData.put("y1", Arrays.asList(yValues));
        mapData.put("y2", Arrays.asList(normalData));
        mapData.put("standardValue", MathUtils.round(standardValue, 3));
        mapData.put("mean", MathUtils.round(mean, 3));
        mapData.put("stdDev", MathUtils.round(stdDev, 3));
        mapData.put("variance", MathUtils.round(variance, 3));
        mapData.put("cpk", MathUtils.round(cpk, 3));
        mapData.put("okRate", MathUtils.round(okRate, 3));
    }
}
