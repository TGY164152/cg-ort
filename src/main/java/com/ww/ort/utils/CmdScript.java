package com.ww.ort.utils;

import java.io.IOException;

public class CmdScript {

    /**
     * 运行脚本
     * @param jslPath 脚本文件位置
     * @return 0(运行成功),1(运行失败)
     */
    public static int runCmd(String jslPath) {
        try {
            Runtime runtime = Runtime.getRuntime();
            // 打开任务管理器，exec方法调用后返回 Process 进程对象
            Process process = runtime.exec("cmd.exe /c " + jslPath);
            // 等待进程对象执行完成，并返回“退出值”，0 为正常，其他为异常
            int exitValue = process.waitFor();
            Thread.sleep(1000);
            System.out.println(jslPath + " 运行结果: " + exitValue);
            // 销毁process对象
            process.destroy();
            return exitValue;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return 1;
    }

}
