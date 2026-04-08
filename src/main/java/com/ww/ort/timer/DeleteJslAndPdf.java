package com.ww.ort.timer;

import com.ww.ort.utils.TimeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

@Component
public class DeleteJslAndPdf {
    @Autowired
    Environment env;

    @Scheduled(cron = "0 5 0 * * ?")
//    @Scheduled(fixedDelay = 30000)
    public void autoGenInverDatas() {
        System.out.println("定时删除创建的脚步和PDF文件");
        String nowTime = TimeUtil.getNowTimeNoHour() + " 00:00:00";
        File fl = new File(env.getProperty("jslCreatePath"));
        if (fl.isDirectory()) {
            File[] files = fl.listFiles();

            for (File f : files) {

                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                FileTime t = null;
                try {
                    t = Files.readAttributes(Paths.get(f.getPath()), BasicFileAttributes.class).creationTime();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (Timestamp.valueOf(nowTime).after(Timestamp.valueOf(dateFormat.format(t.toMillis())))) {
                    f.delete();
                }
            }
        }
        File fl2 = new File(env.getProperty("pdfCachePath"));
        if (fl2.isDirectory()) {
            File[] files = fl2.listFiles();

            for (File f : files) {

                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                FileTime t = null;
                try {
                    t = Files.readAttributes(Paths.get(f.getPath()), BasicFileAttributes.class).creationTime();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (Timestamp.valueOf(nowTime).after(Timestamp.valueOf(dateFormat.format(t.toMillis())))) {
                    f.delete();
                }
            }
        }


    }
}
