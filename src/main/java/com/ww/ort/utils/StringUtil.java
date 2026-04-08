package com.ww.ort.utils;

import org.apache.commons.lang.StringUtils;

/**
 * @author TGY
 * @date 2025-11-20
 */
public class StringUtil {

    /**
     * 格式化OK和NG批次
     * @param f
     * @param t
     * @return
     */
    public static String formatFT(int f, int t) {
        return f + "F/" + t + "T";
    }

    /**
     * 格式化OK和NG片数
     * @param f
     * @param t
     * @return
     */
    public static String formatNO(int f, int t) {
        return f + "pcs NG/" + t + "pcs OK";
    }

    /**
     * 获取批次号
     * @param lastBatch
     * @param checkDate
     * @param dayOrNight
     * @return
     */
    public static String getBatch(String lastBatch, String checkDate, String dayOrNight) {
        String testDataStr = TimeUtil.convertDateTimeFormat(checkDate, "yyyy-MM-dd", "yyyyMMdd");
        String batch = testDataStr + dayOrNight.toUpperCase() + "01";

        if (StringUtils.isNotBlank(lastBatch)){
            // 提取最后两位序号
            String indexStr = lastBatch.substring(lastBatch.length() - 2);
            int num = Integer.parseInt(indexStr);

            // 序号 + 1
            num++;

            // 格式化成两位，不足补0
            String numStr = String.format("%02d", num);
            batch = testDataStr + dayOrNight.toUpperCase() + numStr;
        }

        return batch;
    }
}
