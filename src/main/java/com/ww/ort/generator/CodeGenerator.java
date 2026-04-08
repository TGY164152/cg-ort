package com.ww.ort.generator;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.baomidou.mybatisplus.generator.AutoGenerator;
import com.baomidou.mybatisplus.generator.InjectionConfig;
import com.baomidou.mybatisplus.generator.config.*;
import com.baomidou.mybatisplus.generator.config.po.TableFill;
import com.baomidou.mybatisplus.generator.config.po.TableInfo;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;

import java.util.ArrayList;
import java.util.List;


public class CodeGenerator {
    /**
     * RUN THIS
     */
    //生成文件所在项目路径
//    private static String baseProjectPath = "D:\\java\\boengongye";
    private static String baseProjectPath = "D:\\boen\\boen-cg-ort-service";

    //基础包名
    private static String basePackage="com.ww.ort";
    //设置作者
    private static String authorName="TGY";
    //这里是要生成的表名（如果全部要生成的话，这里注释掉）
    //private static String[] tables= {"t_role","t_resource","t_role_resource","t_user_role"};
    private static String[] tables= {"df_ort_test_item_send_config"};
    //可以设置table前缀
    private static String prefix="t_";
//    private static String prefix="t_";

    //数据库配置四要素
    private static String driverName = "com.mysql.cj.jdbc.Driver";
    private static String url = "jdbc:mysql://192.168.110.20:3306/boen_cg_hb_qms?serverTimezone=UTC";
    private static String username = "remote_user";
    private static String password = "123456";

    public static void main(String[] args) {
        // 代码生成器
        AutoGenerator mpg = new AutoGenerator();

        // 全局配置
        GlobalConfig gc = new GlobalConfig();
        String projectPath = System.getProperty("user.dir");
        gc.setOutputDir(baseProjectPath + "/src/main/java");
        // TODO 设置用户名
        gc.setAuthor("TGY");
        gc.setOpen(true);
        // service 命名方式
        gc.setServiceName("%sService");
        // service impl 命名方式
        gc.setServiceImplName("%sServiceImpl");
        // 自定义文件命名，注意 %s 会自动填充表实体属性！
        gc.setMapperName("%sMapper");
        gc.setXmlName("%sMapper");
        gc.setFileOverride(true);
        gc.setActiveRecord(true);
        // XML 二级缓存
        gc.setEnableCache(false);
        // XML ResultMap
        gc.setBaseResultMap(true);
        // XML columList
        gc.setBaseColumnList(false);
        mpg.setGlobalConfig(gc);

        // TODO 数据源配置
        DataSourceConfig dsc = new DataSourceConfig();
        dsc.setUrl(url);
        dsc.setDriverName(driverName);
        dsc.setUsername(username);
        dsc.setPassword(password);
        mpg.setDataSource(dsc);

        // TODO 包配置
        PackageConfig pc = new PackageConfig();
        //pc.setModuleName(scanner("模块名"));
        pc.setParent("com.ww.ort");
        pc.setEntity("entity");
        pc.setService("service");
        pc.setServiceImpl("service.impl");
        mpg.setPackageInfo(pc);

        // 自定义需要填充的字段
        List<TableFill> tableFillList = new ArrayList<>();

        // 自定义配置
        InjectionConfig cfg = new InjectionConfig() {
            @Override
            public void initMap() {
                // to do nothing
            }
        };

        //指定 Mapper XML 输出路径和文件名
        List<FileOutConfig> focList = new ArrayList<>();
        focList.add(new FileOutConfig("/templates/mapper.xml.ftl") {
            @Override
            public String outputFile(TableInfo tableInfo) {
                // 自定义输入文件名称
                return projectPath + "/src/main/resources/mapper/"
                        + "/" + tableInfo.getEntityName() + "Mapper" + StringPool.DOT_XML;
            }
        });
        cfg.setFileOutConfigList(focList);
        mpg.setCfg(cfg);

        //指定自定义模板路径
        TemplateConfig templateConfig = new TemplateConfig();
        templateConfig.setEntity("templates/entity.java"); // 注意只写一次 .ftl
        mpg.setTemplate(templateConfig);

        // 策略配置
        StrategyConfig strategy = new StrategyConfig();
        strategy
                //underline_to_camel = 下划线转驼峰。
                //表名生成规则：数据库表名 → Java 实体类名。
                .setNaming(NamingStrategy.underline_to_camel)
                //字段名生成规则：数据库字段名 → Java 属性名。
                .setColumnNaming(NamingStrategy.underline_to_camel)
                //使用 @Data
                .setEntityLombokModel(true)
                // 设置逻辑删除键（这个是逻辑删除的操作）
                .setLogicDeleteFieldName("deleted")
                // TODO 指定生成的bean的数据库表名（如果全部生成，这里要注释掉）
                .setInclude(tables)
                // 驼峰转连字符
                .setControllerMappingHyphenStyle(true);
        mpg.setStrategy(strategy);
        // 选择 freemarker 引擎需要指定如下加，注意 pom 依赖必须有！
        mpg.setTemplateEngine(new FreemarkerTemplateEngine());
        mpg.execute();
    }

}
