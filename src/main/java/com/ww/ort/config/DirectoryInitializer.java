package com.ww.ort.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * 启动项目生产配置文件中文件目录
 */
@Component
public class DirectoryInitializer implements CommandLineRunner {

    @Autowired
    Environment env;

    @Override
    public void run(String... args) throws Exception {
        if (!Files.exists(Paths.get(env.getProperty("jslPath")))){
            Files.createDirectories(Paths.get(env.getProperty("jslPath")));
        }
        if (!Files.exists(Paths.get(env.getProperty("jslCreatePath")))){
            Files.createDirectories(Paths.get(env.getProperty("jslCreatePath")));
        }
        if (!Files.exists(Paths.get(env.getProperty("imgPath")))){
            Files.createDirectories(Paths.get(env.getProperty("imgPath")));
        }
        if (!Files.exists(Paths.get(env.getProperty("pdfCachePath")))){
            Files.createDirectories(Paths.get(env.getProperty("pdfCachePath")));
        }
        if (!Files.exists(Paths.get(env.getProperty("experimentPath")))){
            Files.createDirectories(Paths.get(env.getProperty("experimentPath")));
        }
    }
}
