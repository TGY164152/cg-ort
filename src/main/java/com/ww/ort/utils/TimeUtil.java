package com.ww.ort.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


public class TimeUtil {
    private static final Logger logger = LoggerFactory.getLogger(TimeUtil.class);

    /**
     * 获取当前时间有下划线
     *
     * @return
     */
    public static String getNowTimeByNormal() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date now = new Date();
        return sdf.format(now);
    }

    /**
     * 获取当前时间,yyyy-MM-dd
     *
     * @return
     */
    public static String getNowTimeNoHour() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date now = new Date();
        return sdf.format(now);
    }

    /**
     * 获取当前时间,yyyyMMdd
     *
     * @return
     */
    public static String getNowTimeNoHour2() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        Date now = new Date();
        return sdf.format(now);
    }

    /**
     * 获取当前月yyyy-MM
     *
     * @return
     */
    public static String getNowMonth() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
        Date now = new Date();
        return sdf.format(now);
    }

    /**
     * 获取当前月yyyyMM
     *
     * @return
     */
    public static String getNowMonth2() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");
        Date now = new Date();
        return sdf.format(now);
    }

    public static String getFirstDay() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Calendar c = Calendar.getInstance();
        c.add(Calendar.MONTH, 0);
        c.set(Calendar.DAY_OF_MONTH, 1);// 设置为1号,当前日期既为本月第一天
        String first = format.format(c.getTime());
        return first + " 00:00:00";
    }

    public static String getLastDay() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Calendar ca = Calendar.getInstance();
        ca.set(Calendar.DAY_OF_MONTH, ca.getActualMaximum(Calendar.DAY_OF_MONTH));
        String last = format.format(ca.getTime());
        return last + " 23:59:59";
    }

    public static String getYear() {
        Calendar cal = Calendar.getInstance();
        return cal.get(Calendar.YEAR) + "";
    }

    public static String getMonth() {
        Calendar cal = Calendar.getInstance();
        return cal.get(Calendar.MONTH) + 1 + "";
    }

    public static String getDay() {
        Calendar cal = Calendar.getInstance();
        return cal.get(Calendar.DAY_OF_MONTH) + "";
    }


    /**
     * 获取昨天 格式:yyyy-MM-dd
     */
    public static String getYesterday() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -1);
        String last = df.format(calendar.getTime());
        return last;
    }

    /**
     * 获取两个月前 格式:yyyy-MM-dd
     */
    public static String getTwoMonthAgo() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, -2);
        String last = df.format(calendar.getTime());
        return last;
    }

    /**
     * 获取某个时间前的时间 格式:yyyy-MM-dd
     */
    public static String getBeforeTime(int hour) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR, hour);
        String last = df.format(calendar.getTime());
        return last;
    }

    /**
     * 在当前时间增加时间 格式:yyyy-MM-dd HH:mm:ss
     * @return
     */
    public static Date getValidTime(int minute) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, minute);
        String last = df.format(calendar.getTime());
        return calendar.getTime();
    }


    /**
     * 获取当前时间前一小时的时间
     * @param date
     * @return java.util.Date
     */
    public static String beforeOneHourToNowDate(Date date) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar calendar = Calendar.getInstance();
        /* HOUR_OF_DAY 指示一天中的小时 */
        calendar.setTime(date);
        calendar.add(Calendar.HOUR_OF_DAY, -1);
        return df.format(calendar.getTime());
    }

    /**
     * 获取当前时间后一小时的时间
     * @param date
     * @return java.util.Date
     */
    public static String afterOneHourToNowDate(Date date) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar calendar = Calendar.getInstance();
        /* HOUR_OF_DAY 指示一天中的小时 */
        calendar.setTime(date);
        calendar.add(Calendar.HOUR_OF_DAY, 1);
        return df.format(calendar.getTime());
    }

    /**
     * 获取当前时间后一小时的时间 且0分0秒
     * @param date
     * @return java.util.Date
     */
    public static String afterOneHour0_0ToNowDate(Date date) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar calendar = Calendar.getInstance();
        /* HOUR_OF_DAY 指示一天中的小时 */
        calendar.setTime(date);
        calendar.add(Calendar.HOUR_OF_DAY, 1);
        return df.format(calendar.getTime()).substring(0,14) + "00:00";
    }


    /**
     * 根据年,月获取该月开始时间
     */
    public static String getFirstDayByMonthAndYear(int year, int month) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Calendar ca = Calendar.getInstance();
        ca.set(year, month - 1, 1);
        String time = format.format(ca.getTime());
        return time + " 00:00:00";
    }

    /**
     * 根据年,月获取该月接收时间
     */
    public static String getLastDayByMonthAndYear(int year, int month) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Calendar ca = Calendar.getInstance();
        ca.set(year, month - 1, 1);
        ca.set(Calendar.DAY_OF_MONTH, ca.getActualMaximum(Calendar.DAY_OF_MONTH));
        String time = format.format(ca.getTime());
        return time + " 23:59:59";
    }

    /**
     * 获取上个月的第一天
     *
     * @return
     */
    public static String getBeforMonthFirstDay() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Calendar c = Calendar.getInstance();
        c.add(Calendar.MONTH, -1);
        c.set(Calendar.DAY_OF_MONTH, 1);// 设置为1号,当前日期既为本月第一天
        String first = format.format(c.getTime());
        return first + " 00:00:00";
    }

    /**
     * 获取上个月的最后一天
     *
     * @return
     */
    public static String getBeforMonthLastDay() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Calendar ca = Calendar.getInstance();
        ca.add(Calendar.MONTH, -1);
        ca.set(Calendar.DAY_OF_MONTH, ca.getActualMaximum(Calendar.DAY_OF_MONTH));
        String last = format.format(ca.getTime());
        return last + " 23:59:59";
    }


    /**
     * 获取上个月yyyy-MM
     *
     * @return yyyy-MM
     */
    public static String getBeforMonth() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM");
        Calendar c = Calendar.getInstance();
        c.add(Calendar.MONTH, -1);
        c.set(Calendar.DAY_OF_MONTH, 1);// 设置为1号,当前日期既为本月第一天
        String first = format.format(c.getTime());
        return first;
    }

    /**
     * 转换时间格式从yyyyMMddHHmmss转成yyyy-MM-dd HH:mm:ss
     *
     * @param time
     * @return
     */
    public static String changeTimeFormat(String time) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat oldFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        try {
            Date date = oldFormat.parse(time);
            return sdf.format(date);
        } catch (ParseException e) {

            logger.error("转换时间格式从yyyyMMddHHmmss转成yyyy-MM-dd HH:mm:ss", e);
        }
        return "";
    }


    /**
     * 流程信息使用的时间转换yyyyMMddHHmmssSSS
     *
     * @param time
     * @return
     */
    public static String changeTimeFormatByExp302(String time) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssSSS");
        SimpleDateFormat oldFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        try {
            Date date = oldFormat.parse(time);
            return sdf.format(date);
        } catch (ParseException e) {
            logger.error("流程信息使用的时间转换yyyyMMddHHmmssSSS", e);
        }
        return "";
    }

    public static String changeTimeFormat2(String time) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        SimpleDateFormat oldFormat = new SimpleDateFormat("yyyyMMddHHmm");
        try {
            Date date = oldFormat.parse(time);
            return sdf.format(date);
        } catch (ParseException e) {
            logger.error("changeTimeFormat2", e);
        }
        return "";
    }

    public static String changeTimeFormat3(String time) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat oldFormat = new SimpleDateFormat("yyyyMMdd");
        try {
            Date date = oldFormat.parse(time);
            return sdf.format(date);
        } catch (ParseException e) {
            logger.error("changeTimeFormat3", e);
        }
        return "";
    }

    /**
     * 转换时间格式从yyyy-MM-dd HH:mm:ss转成yyyy-MM-dd
     *
     * @param time
     * @return
     */
    public static String changeTimeFormat4(String time) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat oldFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date date = oldFormat.parse(time);
            return sdf.format(date);
        } catch (ParseException e) {
            logger.error("转换时间格式从yyyy-MM-dd HH:mm:ss转成yyyy-MM-dd", e);
        }
        return "";
    }

    public static String getLastDayOfMonth(String yearMonth) {
        int year = Integer.parseInt(yearMonth.split("-")[0]);  //年
        int month = Integer.parseInt(yearMonth.split("-")[1]); //月
        Calendar cal = Calendar.getInstance();
        // 设置年份
        cal.set(Calendar.YEAR, year);
        // 设置月份
        // cal.set(Calendar.MONTH, month - 1);
        cal.set(Calendar.MONTH, month); //设置当前月的上一个月
        // 获取某月最大天数
        //int lastDay = cal.getActualMaximum(Calendar.DATE);
        int lastDay = cal.getMinimum(Calendar.DATE); //获取月份中的最小值，即第一天
        // 设置日历中月份的最大天数
        //cal.set(Calendar.DAY_OF_MONTH, lastDay);
        cal.set(Calendar.DAY_OF_MONTH, lastDay - 1); //上月的第一天减去1就是当月的最后一天
        // 格式化日期
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(cal.getTime());
    }

    /**
     * 获取当前时间加长
     *
     * @return
     */
    public static String getNowTimeLong() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmsssss");
        Date now = new Date();
        return sdf.format(now);
    }

    /**
     * 指定日期加上天数后的日期
     *
     * @param num     为增加的天数
     * @param newDate 创建时间
     * @return
     * @throws ParseException
     */
    public static String plusDay(int num, String newDate) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date currdate = format.parse(newDate);
        System.out.println("现在的日期是：" + currdate);
        Calendar ca = Calendar.getInstance();
        ca.setTime(currdate);
        ca.add(Calendar.DATE, num);// num为增加的天数，可以改变的
        currdate = ca.getTime();
        String enddate = format.format(currdate);
        System.out.println("增加天数以后的日期：" + enddate);
        return enddate;
    }



    /**
     * 获取单双月0双1单
     *

     * @return
     * @throws ParseException
     */
    public static Integer getBimonthly()  {
        Calendar calendar = Calendar.getInstance();

        int month = calendar.get(Calendar.MONTH) + 1;

        return month%2;
    }



    /**
     * 复制文件夹
     *
     * @param resource 源路径
     * @param target   目标路径
     */
    public static void copyFolder(String resource, String target) throws Exception {

        File resourceFile = new File(resource);
        if (!resourceFile.exists()) {
            throw new Exception("源目标路径：[" + resource + "] 不存在...");
        }
//        System.out.println("target:"+target);
        File targetFile = new File(target);
        targetFile.mkdirs();
//        System.out.println("targetffff:"+targetFile.getPath());
        if (!targetFile.exists()) {
            throw new Exception("存放的目标路径：[" + target + "] 不存在...");
        } else {

        }

        // 获取源文件夹下的文件夹或文件
        File[] resourceFiles = resourceFile.listFiles();
        int i = 0;
        for (File file : resourceFiles) {

//        		System.out.println("fileNAME:"+file.getPath());
            File file1 = new File("D:\\ww工作资料\\桌面\\测试报文\\系统生成的测试报文第四十六批" + File.separator + resourceFile.getName());
//                System.out.println("文件夹" + file1.getPath());
//                System.out.println("文件夹" + targetFile.getAbsolutePath());
//                System.out.println("文件夹" + File.separator);
//                System.out.println("文件夹" + resourceFile.getName());
            // 复制文件
            if (file.isFile()) {
//                    System.out.println("文件" + file.getName());
                // 在 目标文件夹（B） 中 新建 源文件夹（A），然后将文件复制到 A 中
                // 这样 在 B 中 就存在 A
                if (!file1.exists()) {
                    file1.mkdirs();
                }
                File targetFile1 = new File(file1.getAbsolutePath() + File.separator + file.getName());
                copyFile(file, targetFile1);
            }
            // 复制文件夹
            if (file.isDirectory()) {// 复制源文件夹
                String dir1 = file.getAbsolutePath();
                // 目的文件夹
                String dir2 = file1.getAbsolutePath();
                copyFolder(dir1, dir2);
            }


        }

    }

    /*
    计算两个日期之间相差的天数
     */
    public static int between_days(String a, String b) {

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");// 自定义时间格式

        Calendar calendar_a = Calendar.getInstance();// 获取日历对象
        Calendar calendar_b = Calendar.getInstance();

        Date date_a = null;
        Date date_b = null;

        try {
            date_a = simpleDateFormat.parse(a);//字符串转Date
            date_b = simpleDateFormat.parse(b);
            calendar_a.setTime(date_a);// 设置日历
            calendar_b.setTime(date_b);
        } catch (ParseException e) {
            e.printStackTrace();//格式化异常
        }

        long time_a = calendar_a.getTimeInMillis();
        long time_b = calendar_b.getTimeInMillis();

        long between_days = (time_b - time_a) / (1000 * 3600 * 24);//计算相差天数
        Long data = between_days;
        return data.intValue();
    }

    /**
     * 复制文件
     *
     * @param resource
     * @param target
     */
    public static void copyFile(File resource, File target) throws IOException {
        // 输入流 --> 从一个目标读取数据
        // 输出流 --> 向一个目标写入数据


        // 文件输入流并进行缓冲
        FileInputStream inputStream = new FileInputStream(resource);
        BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);

        // 文件输出流并进行缓冲
        FileOutputStream outputStream = new FileOutputStream(target);
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);

        // 缓冲数组
        // 大文件 可将 1024 * 2 改大一些，但是 并不是越大就越快
        byte[] bytes = new byte[1024 * 2];
        int len = 0;
        while ((len = inputStream.read(bytes)) != -1) {
            bufferedOutputStream.write(bytes, 0, len);
        }
        // 刷新输出缓冲流
        bufferedOutputStream.flush();
        //关闭流
        bufferedInputStream.close();
        bufferedOutputStream.close();
        inputStream.close();
        outputStream.close();

        long end = System.currentTimeMillis();

//        System.out.println("耗时：" + (end - start) / 1000 + " s");

    }

    /**
     * 通过时间秒毫秒数判断两个时间的间隔
     *
     * @param date1
     * @param date2
     * @return
     */
    public static int differentDaysByMillisecond(Date date1, Date date2) {
        int days = (int) ((date2.getTime() - date1.getTime()) / (1000 * 3600 * 24));
        return days;
    }

    /**
     * 通过时间获取该月的天数，并返回天的字符串数组 1日，2日
     * 类型为 yyyy-MM
     *
     */
    public static List<String> theMonthDays(String theMonth) {
        Integer year  = Integer.parseInt(theMonth.substring(0,4));   // 获取年份
        Integer month = Integer.parseInt(theMonth.substring(5));     // 获取月份
        Integer days = 0;   //该月天数
        List<String> returnDay = new ArrayList<>();
        if ((year%4 == 0 && year%100 != 0) || year%400 == 0){
            if (month == 2){
                days = 29;
            }
        }else {
            if (month == 2){
                days = 28;
            }
        }
        if (month==1 ||month==3 ||month==5 ||month==7 ||month==8 ||month==10 ||month==12){
            days = 31;
        }else if (month==4 ||month==6 ||month==9 ||month==11){
            days = 30;
        }
        for (int i = 1; i <= days; i++) {
            returnDay.add(i + "日");
        }
        return returnDay;
    }

    // 获取某月的上个月
    public static String getLastMonth(String date){
        int year = Integer.parseInt(date.substring(0, 4));
        int month = Integer.parseInt(date.substring(5, 7));
        if (month > 1){
            month--;
        }else {
            month = 12;
            year--;
        }
        if (month >= 10){
            return year + "-" + month;
        }else{
            return year + "-0" + month;
        }
    }

    // 获取某月的下个月
    public static String getNextMonth(String date){
        int year = Integer.parseInt(date.substring(0, 4));
        int month = Integer.parseInt(date.substring(5, 7));
        if (month < 12){
            month++;
        }else {
            month = 1;
            year++;
        }
        if (month >= 10){
            return year + "-" + month;
        }else{
            return year + "-0" + month;
        }
    }

    // 获取某一天的第二天
    public static String getNextDay(String date) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date parse = format.parse(date);
        long time = parse.getTime();
        long addTime = 1 * 24 * 60 * 60 * 1000;
        Date date1 = new Date(time + addTime);
        String result = format.format(date1);
        return result;
    }

    // 获取某一天的上一天
    public static String getLastDay(String date) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date parse = format.parse(date);
        long time = parse.getTime();
        long addTime = -1 * 24 * 60 * 60 * 1000;
        Date date1 = new Date(time + addTime);
        String result = format.format(date1);
        return result;
    }

    // 获取某一天的前几天
    public static String getLastSomeDay(String date, int day_num) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date parse = format.parse(date);
        long time = parse.getTime();
        long addTime = -1 * 24 * 60 * 60 * 1000 * day_num;
        Date date1 = new Date(time + addTime);
        String result = format.format(date1);
        return result;
    }

    // 获取上一年该月
    public static String getLastYearMonth(String date){
        String substring = date.substring(0, 4);
        String month = date.substring(5, 7);
        int year = Integer.parseInt(substring);
        year--;
        return year + "-" + month;
    }

    // 获取某月的天数
    public static long howManyDayOfTheMonth(String date) throws ParseException {
        String nextMonth = getNextMonth(date);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM");
        Date date1 = format.parse(date);
        Date date2 = format.parse(nextMonth);
        Long gap = date2.getTime() - date1.getTime();
        return gap / 1000 / 60 / 60 / 24;
    }

    // 获取某月的天数
    public static long howManyDayOfTheYear(String date) throws ParseException {
        String nextYear = (Integer.parseInt(date) + 1) + "";
        SimpleDateFormat format = new SimpleDateFormat("yyyy");
        Date date1 = format.parse(date);
        Date date2 = format.parse(nextYear);
        Long gap = date2.getTime() - date1.getTime();
        return gap / 1000 / 60 / 60 / 24;
    }



    public static String timeStamp2Date(String seconds) {
        if(seconds == null || seconds.isEmpty() || seconds.equals("null")){
            return "";
        }
        String format = "yyyy-MM-dd HH:mm:ss";
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(new Date(Long.valueOf(seconds+"000")));
    }

    public static Date timeStamp2Date3(String seconds) {
        if(seconds == null || seconds.isEmpty() || seconds.equals("null")){
            return null;
        }
        String format = "yyyy-MM-dd HH:mm:ss";
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return new Date(Long.valueOf(seconds+"000"));
    }

    public static Timestamp stringToTimestamp(String dateString, String formatString) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat(formatString);
        Date date = format.parse(dateString);
        return new Timestamp(date.getTime());
    }

    /**
     * 通用方法：将日期时间字符串从一种格式转换为另一种格式
     *
     * @param inputDateTime  原始日期时间字符串
     * @param inputPattern   输入格式（如 "yyyy-MM-dd HH:mm"）
     * @param outputPattern  输出格式（如 "H:mm"）
     * @return 转换后的时间字符串
     */
    public static String convertDateTimeFormat(String inputDateTime, String inputPattern, String outputPattern) {
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern(inputPattern);
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern(outputPattern);

        try {
            // 先尝试解析为 LocalDateTime（带时间）
            LocalDateTime dateTime = LocalDateTime.parse(inputDateTime, inputFormatter);
            return dateTime.format(outputFormatter);
        } catch (Exception e) {
            // 如果解析失败，尝试解析为 LocalDate（仅日期）
            LocalDate date = LocalDate.parse(inputDateTime, inputFormatter);
            return date.format(outputFormatter);
        }
    }

    public static void main(String[] args) throws ParseException {
        Timestamp timestamp = stringToTimestamp("8/8", "MM/dd");
        System.out.println(timestamp);

    }







}
