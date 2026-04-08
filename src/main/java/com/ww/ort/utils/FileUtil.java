package com.ww.ort.utils;

import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import java.io.*;
import java.util.Base64;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileUtil {

    /**
     *  base64 转 byte[]
     * @param base64
     * @return
     */
    public static byte[] base64ToBytes(String base64) {
        return Base64.getDecoder().decode(base64);
    }

    /**
     * 通用静态方法：将 File 文件转换成文件流 byte[]
     *
     * @param file 待转换的文件
     * @return 文件的字节数组（文件流）
     * @throws IOException 文件读取异常
     */
    public static byte[] toByteArray(File file) throws IOException {
        if (file == null || !file.exists()) {
            return null;
        }

        try (FileInputStream fis = new FileInputStream(file);
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[4096]; // 4KB buffer
            int len;
            while ((len = fis.read(buffer)) != -1) {
                bos.write(buffer, 0, len);
            }

            return bos.toByteArray();
        }
    }

    /**
     * 修改inputFile文件中的内容，放到outputFile文件中（没有会自动生成）
     * @param inputFile 原文件（全路径）
     * @param outputFile 修改后文件（全路径）
     * @param replacementMap 用map存储需要修改的内容，key为需要修改的内容，value为修改的内容。注意：会修改所有的key为value。
     */
    public static void modifyMapFile(String inputFile, String outputFile, Map<String, String> replacementMap) throws IOException {
        File input = new File(inputFile);
        BufferedReader reader = new BufferedReader(new FileReader(input));
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
        BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
        writer.write(builder.toString());
        writer.close();
    }

    /**
     * 保存 MultipartFile 到指定路径
     *
     * @param file 上传的文件
     * @param directory 保存文件的目录（如 D:/upload 或 /usr/local/upload）
     * @param filename 保存后的文件名（可自定义，例如原始名、UUID名等）
     * @return 保存后的完整路径
     * @throws IOException 当文件写入失败时抛出
     */
    public static String saveFile(MultipartFile file, String directory, String filename) throws IOException {
        // 1️⃣ 参数校验
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("上传文件为空");
        }

        // 2️⃣ 创建目标目录（不存在则自动创建）
        File dir = new File(directory);
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            if (!created) {
                throw new IOException("无法创建目录: " + directory);
            }
        }

        // 3️⃣ 获取文件扩展名（如 .jpg / .png / .xlsx）
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        // 4️⃣ 拼接最终保存路径
        String finalPath = directory + "/" + filename + extension;
        File dest = new File(finalPath);

        // 5️⃣ 保存文件（如果文件已存在，会被覆盖）
        file.transferTo(dest);

        // 6️⃣ 返回文件的绝对路径
        return finalPath;
    }


    /**
     * 将字节数组转换为 MultipartFile
     * @param bytes 二进制数据
     * @param filename 文件名
     * @param contentType 内容类型
     * @return MultipartFile
     */
    public static MultipartFile convertBytesToMultipartFile(
            byte[] bytes,
            String filename,
            String contentType) throws IOException {

        InputStream inputStream = new ByteArrayInputStream(bytes);

        return new MockMultipartFile(
                "file",           // 表单字段名（通常是"file"）
                filename,           // 原始文件名
                contentType,        // 内容类型，如 "image/png"
                inputStream         // 文件内容流
        );
    }
}
