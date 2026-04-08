package com.ww.ort.utils;

import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import static java.lang.Thread.sleep;

@Component
public class GenerateJmp {

    @Autowired
    private Environment env;

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
}
