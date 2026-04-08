package com.ww.ort.utils;

import java.io.*;
import java.lang.reflect.Method;
import java.util.Map;

public class JSLTemplate {



    /**
     * 修改文件中的内容
     * @param file 文件
     * @param replacements 替换字符串(key目标,value 替换的内容)
     * @param saveFile 生成文件存放位置s
     * 返回修改的成功的数量
     */
    public static int modifyFileContent(File file, Map<String,String>replacements, File saveFile) {
        //记录修改的行数
        int cnt = 0;
        if(!file.exists()){
            return cnt;
        }
        StringBuilder sb = new StringBuilder();

        //记录替换所在的行
        int rowLine = 0;
        //换行符
        String enter = System.getProperty("line.separator");

        //printWriter原本也想放在 try-with 中，少写点代码，
        //但是一个文件不能同时读写，pw 和 br 对同一个文件操作的结果时，文件的内容被清空！！！
        //不妨试下，将 pw 申明在 try-with 中，看下运行结果。
        PrintWriter pw = null;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            for (line = br.readLine(); line != null; line = br.readLine()) {
                rowLine++;
                for(Map.Entry<String,String> entry:replacements.entrySet()){
                    if (line.contains(entry.getKey())) {
                        line = line.replace(entry.getKey(), entry.getValue());
                        cnt++;
                    }
                };
                //数据暂存在 StringBuilder 中
                if (rowLine == 1) {
                    sb.append(line);
                } else {
                    sb.append(enter).append(line);
                }
            }
            pw = new PrintWriter(new FileWriter(saveFile));
            pw.print(sb);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
//            System.exit(1);
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (pw != null) {
                pw.close();
            }
        }

        return cnt;
    }




    /**
     * 递归利用反射获取字段的值
     *
     * 调用示例请参考第二段单元测试代码
     *
     * @param fieldName 字段名称，支持多级，如：name person.student.className
     * @param data 需要从里面提前字段值的对象
     */
    public static Object recursionGetFieldValueByReflect(String fieldName, Object data) throws Exception {
        // fieldName 是否包含 ,
        boolean fieldNameContainsDot = fieldName.contains(".");
        int indexOfDotInFieldName = fieldName.indexOf(".");

        // temp field name 会根据 fieldName 是否包含 , 而不同
        String tmpFieldName = fieldName;
        if (fieldNameContainsDot) {
            tmpFieldName = fieldName.substring(0, indexOfDotInFieldName);
        }

        // 通过字符串拼接的方式，拼接出实体类相对应的get方法;
        String methodName = "get" + tmpFieldName.substring(0, 1).toUpperCase() + tmpFieldName.substring(1);
        // 通过反射拿到类对象，再获取类对象的 methodName 这个方法
        Method declaredMethod = data.getClass().getDeclaredMethod(methodName, null);
        // 通过invoke提交对象，执行declaredMethod这个方法
        Object result = declaredMethod.invoke(data);

        // 包含 , 并且 result 不为 null 需要继续递归
        if (fieldNameContainsDot && result != null) {
            return recursionGetFieldValueByReflect(fieldName.substring(indexOfDotInFieldName + 1), result);
        } else {
            return result;
        }
    }
}
