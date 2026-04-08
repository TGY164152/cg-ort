package com.ww.ort.controller;

import com.alibaba.fastjson.JSON;
import com.ww.ort.utils.CmdScript;
import com.ww.ort.utils.RedisUtils;
import com.ww.ort.utils.Result;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.core.env.Environment;
import org.apache.poi.xssf.usermodel.XSSFPictureData;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.*;

import static java.lang.Thread.sleep;

@Controller
@RequestMapping("/test")
@CrossOrigin
@Api(tags = "测试运行")
@ResponseBody
public class testController {

    @Autowired
    private RedisUtils redisUtil;

    @Autowired
    private Environment env;

    @GetMapping("/runCmdTest")
    public String runCmdTest() {
        String runUrl = "C:\\Users\\acer\\Desktop\\JMP\\加上表生成韦伯图.jsl";
        int i = CmdScript.runCmd(runUrl);
        if (i == 0) {
            return "成功运行";
        } else {
            return "运行异常";
        }
    }

    @GetMapping("/replaceText")
    public String replaceText() {

        long start = System.currentTimeMillis();

        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
        String f = format.format(new Date());

        final String inputFile = "C:\\Users\\acer\\Desktop\\加上表生成韦伯图.jsl";
        //final String backupFile = "C:\\Users\\acer\\Desktop\\backup\\加上表生成韦伯图.jsl";
        final String runJsl = "C:\\Users\\acer\\Desktop\\加上表生成韦伯图" + f + ".jsl";
        int result = 1;

        try {
            Map<String, String> map = new HashMap<>();
            map.put("startDate", "'2023-04-28'");
            map.put("endDate", "'2023-04-29'");

            // 修改文件内容
            modifyMapFile(inputFile, runJsl, map);

            result = CmdScript.runCmd(runJsl);


        } catch (IOException e) {
            e.printStackTrace();
        }

        long end = System.currentTimeMillis();

        System.out.println("运行时间为" + (end - start) + "ms");
        if (result == 0) {
            return "成功运行";
        } else {
            return "运行异常";
        }
    }

    private static void backupFile(String inputFile, String backupFile) throws IOException {
        File input = new File(inputFile);
        File backup = new File(backupFile);
        BufferedReader reader = new BufferedReader(new FileReader(input));
        BufferedWriter writer = new BufferedWriter(new FileWriter(backup));
        String line;
        while ((line = reader.readLine()) != null) {
            writer.write(line);
            writer.newLine();
        }
        reader.close();
        writer.close();
    }

    private static void restoreFile(String inputFile, String backupFile) throws IOException {
        File input = new File(inputFile);
        File backup = new File(backupFile);
        BufferedReader reader = new BufferedReader(new FileReader(backup));
        BufferedWriter writer = new BufferedWriter(new FileWriter(input));
        String line;
        while ((line = reader.readLine()) != null) {
            writer.write(line);
            writer.newLine();
        }
        reader.close();
        writer.close();
    }

    private static void modifyFile(String inputFile, String oldString, String newString) throws IOException {
        //String runUrl = "C:\\Users\\acer\\Desktop\\test.txt";
        // 打开文件并读取内容
        //File inputFile = new File(inputFile);
        BufferedReader reader = new BufferedReader(new FileReader(inputFile));

        // 找到需要修改的行并进行修改
        StringBuilder contentBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.contains(oldString)) {
                line = line.replace(oldString, newString);
            }
            contentBuilder.append(line);
            contentBuilder.append(System.lineSeparator());
        }
        reader.close();

        // 写入修改后的内容
        File outputFile = new File(inputFile);
        BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
        writer.write(contentBuilder.toString());
        writer.close();
    }

    public static void modifyMapFile(String inputFile, String outputFile, Map<String, String> replacementMap) throws IOException {
        /*File input = new File(inputFile);
        BufferedReader reader = new BufferedReader(new FileReader(input));*/
        InputStreamReader isr = new InputStreamReader(Files.newInputStream(new File(inputFile).toPath()), "UTF-8");  // 解决中文乱码问题
        BufferedReader reader = new BufferedReader(isr);
        Pattern pattern = Pattern.compile(
                "(" + String.join("|", replacementMap.keySet()) + ")");
        StringBuilder builder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            Matcher matcher = pattern.matcher(line);
            while (matcher.find()) {
                String match = matcher.group();
                line = line.replace(match, replacementMap.get(match));
            }
            builder.append(line);
            builder.append(System.lineSeparator());
        }
        reader.close();
        /*BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));*/
        BufferedWriter writer = new BufferedWriter (new OutputStreamWriter (new FileOutputStream (outputFile,true),"UTF-8"));  // 解决中文乱码问题
        writer.write(builder.toString());
        writer.close();
    }


    /**
     * 计算文件内容的 MD5 值
     * @param input
     * @return
     */
    private static String getMD5Hash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hashBytes = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 替换文件内容
     * @param inputFilePath
     * @param outputDirectory
     * @throws IOException
     */
    public static Map<String, String> modifyFileContent(String inputFilePath, String outputDirectory,String imgPath, Map<String, String> replaceMap) throws IOException {
        Map<String,String> urlMap = new HashMap<>();

        // 读取文件内容
        String content = new String(Files.readAllBytes(Paths.get(inputFilePath)), "UTF-8");

        for (Map.Entry<String, String> entry : replaceMap.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            content = content.replace(key, value);
        }

        // 计算替换后内容的 MD5 值
        String md5Hash = getMD5Hash(content);

        // 生成新的文件名（MD5值）
        String newFileName = md5Hash + ".jsl";
        Path runJsl = Paths.get(outputDirectory, newFileName);

        String imageName = md5Hash + ".png";
        Path imageUrl = Paths.get(imgPath, imageName);
        content = content.replace("#IMAGE_URL#", imageUrl.toString());

        // 将修改后的内容写入新的文件
        Files.write(runJsl, content.getBytes("UTF-8"));

        urlMap.put("runJsl", runJsl.toString());
        urlMap.put("imageName", imageName.toString());
        urlMap.put("imageUrl", imageUrl.toString());
        return urlMap;
    }

    /**
     * 生成堆叠箱线图
     */
    public <T> String generateStackBoxplot(
            List<T> dataList,  // 数据列表
            String xFieldName,  // X轴名称
            String[] yFieldNames,  // Y轴名称（多个）
            Double[][] yStandard  // 每个Y轴的标准，格式[[1,2], [4,5], [2,8]]
            ) throws IOException, InterruptedException {

        String inputFilePath = env.getProperty("jslPath") + "/堆叠箱线图脚本.jsl";
        String jslCreatePath = env.getProperty("jslCreatePath");
        String imgPath = env.getProperty("imgPath");
        // 读取文件内容
        String content = new String(Files.readAllBytes(Paths.get(inputFilePath)), "UTF-8");

        // 定义X Y轴
        String subVariables = "";
        for (String yFieldName : yFieldNames) {
            subVariables += ", Y(:\"" + yFieldName + "\")";
        }
        String variables = "Variables( X( :" + xFieldName + " )" + subVariables + "),";

        // 添加堆叠箱子的个数
        String boxplots = "";
        for (int i = 0; i < yFieldNames.length; i++) {
            String boxplot = "Dispatch(\n" +
                    "\t\t\t{},\n" +
                    "\t\t\t\""+ yFieldNames[i] +"\",\n" +
                    "\t\t\tScaleBox,\n" +
                    "\t\t\t{Add Ref Line( "+ yStandard[i][0] +", \"Dashed\", \"Blue\", \"Min\", 2 ),\n" +
                    "\t\t\tAdd Ref Line( "+ yStandard[i][1] +", \"Dashed\", \"Blue\", \"Max\", 2 )}\n" +
                    "\t\t),";
            boxplots += boxplot;
        }

        // 替换jsl脚本
        content = content.replace("#JSON_DATA#", JSON.toJSONString(dataList));
        content = content.replace("#VARIABLES#", variables);
        content = content.replace("#BOXPLOTS#", boxplots);

        // 计算替换后内容的 MD5 值
        String md5Hash = getMD5Hash(content);

        // 生成新的文件名（MD5值）
        String newFileName = md5Hash + ".jsl";
        Path runJsl = Paths.get(jslCreatePath, newFileName);

        String imageName = md5Hash + ".png";
        Path imageUrl = Paths.get(imgPath, imageName);
        content = content.replace("#IMAGE_URL#", imageUrl.toString());

        // 将修改后的内容写入新的文件
        Files.write(runJsl, content.getBytes("UTF-8"));

        // 查看是否已经生成过图片，没有的话就直接运行文件，然后返回图片地址
        File imageFile = new File(imageUrl.toString());
        if (!imageFile.exists()) {
            CmdScript.runCmd(runJsl.toString());
            // 停一秒，等待图片生成
            sleep(1000);
        }

        return imageUrl.toString();
    }

    /**
     * 计算集合的平均值
     * @param data
     * @return
     */
    public static double calculateMean(List<Double> data) {
        return data.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    }

    /**
     * 计算集合的方差
     * @param data
     * @param mean
     * @return
     */
    public static double calculateVariance(List<Double> data, double mean) {
        return data.stream()
                .mapToDouble(x -> Math.pow(x - mean, 2))
                .average()
                .orElse(0.0);
    }

    /**
     * 计算标准差
     * @param variance
     * @return
     */
    public static double calculateStandardDeviation(double variance) {
        return Math.sqrt(variance);
    }

    /**
     * 计算 CPK 值
     * @param usl
     * @param lsl
     * @param mean
     * @param stdDev
     * @return
     */
    public static double calculateCPK(double usl, double lsl, double mean, double stdDev) {
        double cpkUpper = (usl - mean) / (3 * stdDev);
        double cpkLower = (mean - lsl) / (3 * stdDev);
        return Math.min(cpkUpper, cpkLower);
    }

    /**
     * 保存Excel单元格图片
     * @param xssfPictureData
     * @param imgUrl
     * @return
     * @throws Exception
     */
    public static void saveExcelImg(XSSFPictureData xssfPictureData,String imgUrl) throws Exception {
        byte[] imgByte = xssfPictureData.getData();
        FileOutputStream out = new FileOutputStream(imgUrl);
        out.write(imgByte);
        out.close();
    }
}
