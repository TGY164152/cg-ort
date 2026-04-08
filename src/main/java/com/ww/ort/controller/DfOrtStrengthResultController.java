package com.ww.ort.controller;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.gson.reflect.TypeToken;
import com.ww.ort.entity.*;
import com.ww.ort.service.*;
import com.ww.ort.utils.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import org.springframework.stereotype.Controller;

import java.io.File;
import java.io.IOException;
import java.sql.SQLOutput;
import java.util.*;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author zhao
 * @since 2023-05-06
 */
@Controller
@RequestMapping("/dfOrtStrengthResult")
@ResponseBody
@CrossOrigin
@Api(tags = "强度测试")
public class DfOrtStrengthResultController {
    @Autowired
    Environment env;

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private DfOrtTestDataService dfOrtTestDataService;

    @Autowired
    private DfOrtTestItemImportConfigService dfOrtTestItemImportConfigService;

    @Autowired
    private DfOrtStandardConfigService dfOrtStandardConfigService;

//    /**
//     * 获取ORT强度韦伯图
//     * @return
//     * @throws IOException
//     */
//    @GetMapping(value = "/getWeibullPlot")
//    @ApiOperation("获取ORT强度韦伯图")
//    public Result getWeibullPlot(
//            String factory //工厂
//            , @ApiParam("项目")@RequestParam String model //项目
//            , @ApiParam("颜色")@RequestParam String color //颜色
//            , @ApiParam("生成阶段")@RequestParam String productionPhase //生成阶段
//            , @ApiParam("阶段")@RequestParam String stage //阶段
//            , @ApiParam("测试项目")@RequestParam String testProject //测试项目
//            , String config1
//            , String config2
//            , @ApiParam("开始时间")@RequestParam String startDate //开始时间
//            , @ApiParam("结束时间")@RequestParam String endDate //结束时间
//    ) throws IOException {
//        try {
//            String redisKey = "ORT:强度:韦伯图:" + testProject + ":" + stage + ":" + model + ":" + color + ":" + startDate + ":" + endDate;
//
//            Object filename = redisUtils.get(redisKey);
//
//            if (filename != null){
//                return new Result(200, "获取图片成功", env.getProperty("imgUrl") + "/" + filename.toString());
//            }
//
//            QueryWrapper<DfOrtStrengthDetail> qw = new QueryWrapper<>();
//            qw
//                    .eq(StringUtils.isNotEmpty(model), "model", model)
//                    .eq(StringUtils.isNotEmpty(color), "color", color)
//                    .eq(StringUtils.isNotEmpty(productionPhase), "production_phase", productionPhase)
//                    .eq(StringUtils.isNotEmpty(testProject),"`type`",testProject)
//                    .eq(StringUtils.isNotEmpty(stage),"stage",stage)
//                    .between("test_time", startDate + " 00:00:00", endDate + " 23:59:59");
//
//            List<DfOrtStrengthDetail> list = dfOrtStrengthDetailService.getCheckNameDataList(qw);
//
//            if(list == null || list.size() == 0){
//                return new Result(500, "当前条件下没有相关数据");
//            }
//
//            //强度标准
//            QueryWrapper<DfOrtExperStandConfig> configQw = new QueryWrapper<>();
//            configQw
//                    .eq("project", model)
//                    .eq("color", color)
//                    .eq( "stage", stage)
//                    .eq( "experiment", testProject);
//            List<DfOrtExperStandConfig> configList = dfOrtExperStandConfigService.list(configQw);
//
//            if (configList == null || configList.size() == 0){
//                return new Result(500, "当前条件下没有相关标准数据");
//            }
//
//            //标准数据
//            Map<String,Double> configMap = new HashMap<>();
//
//            for (DfOrtExperStandConfig dfOrtExperStandConfig : configList) {
//                configMap.put(dfOrtExperStandConfig.getStandardItem(), dfOrtExperStandConfig.getStandardValue());
//            }
//
//            Map<String, String> replaceMap = new HashMap<>();
//            replaceMap.put("#JSON_DATA#", JSON.toJSONString(list));
//            replaceMap.put("#X#", "batch");
//            replaceMap.put("#Y#", "value");
//            replaceMap.put("#Char#",configMap.get("Char63.2").toString());
//            replaceMap.put("#B10#",configMap.get("B10").toString());
//            replaceMap.put("#B5#",configMap.get("B5").toString());
//            replaceMap.put("#Min#",configMap.get("Min").toString());
//            Double showMin = configMap.get("Min") - configMap.get("Min") * 0.1;
//            replaceMap.put("#showMin#",showMin.toString());
//
//            String jslFilePath = env.getProperty("jslPath") + "/加上表生成韦伯图多批.jsl";
//            String jslCreatePath = env.getProperty("jslCreatePath");
//            String imgPath = env.getProperty("imgPath");
//
//            Map<String,String> urlMap = testController.modifyFileContent(jslFilePath, jslCreatePath,imgPath, replaceMap);
//            CmdScript.runCmd(urlMap.get("runJsl"));
//
//
//            File imgFile=new File(urlMap.get("imageUrl"));
//
//            if (!imgFile.exists()|| null== imgFile) {
//                return new Result(500,"查询失败");
//            }
//
//            boolean redisFlag = redisUtils.set(redisKey,urlMap.get("imageName"));
//
//            return new Result(200,"查询成功",env.getProperty("imgUrl") + "/" + urlMap.get("imageName"));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return new Result(500,"查询失败");
//    }
    //
    //
    //    @ApiOperation("标准值分布")
    //    @GetMapping("/listBatchStatistics")
    //    public Result listStatistics(
    //            String factory //工厂
    //            , @ApiParam("项目")@RequestParam String model //项目
    //            , @ApiParam("颜色")@RequestParam String color //颜色
    //            , @ApiParam("生成阶段")@RequestParam String productionPhase //生成阶段
    //            , @ApiParam("阶段")@RequestParam String stage //阶段
    //            , @ApiParam("测试项目")@RequestParam String testProject //测试项目
    //            , String config1
    //            , String config2
    //            , @ApiParam("开始时间")@RequestParam String startDate //开始时间
    //            , @ApiParam("结束时间")@RequestParam String endDate //结束时间
    //    ) {
    //        //强度标准
    //        QueryWrapper<DfOrtExperStandConfig> configQw = new QueryWrapper<>();
    //        configQw
    //                    .eq("project", model)
    //                    .eq("color", color)
    //                    .eq( "stage", stage)
    //                    .eq( "experiment", testProject);
    //        List<DfOrtExperStandConfig> configList = dfOrtExperStandConfigService.list(configQw);
    //
    //        if (configList == null || configList.size() == 0){
    //            return new Result(500, "当前条件下没有相关标准数据");
    //        }
    //
    //        Statistics standard = new Statistics();
    //        standard.setName("标准");
    //        for (DfOrtExperStandConfig dfOrtExperStandConfig : configList) {
    //            switch (dfOrtExperStandConfig.getStandardItem()) {
    //                case "Char63.2": standard.setChar652(dfOrtExperStandConfig.getStandardValue()); break;
    //                case "B10": standard.setB10(dfOrtExperStandConfig.getStandardValue()); break;
    //                case "B5": standard.setB5(dfOrtExperStandConfig.getStandardValue()); break;
    //                case "Min": standard.setMin(dfOrtExperStandConfig.getStandardValue()); break;
    //            }
    //        }
    //
    //        QueryWrapper<DfOrtStrengthResult> qw = new QueryWrapper<>();
    //        qw
    //                    .eq(StringUtils.isNotEmpty(model), "model", model)
    //                    .eq(StringUtils.isNotEmpty(color), "color", color)
    //                    .eq(StringUtils.isNotEmpty(productionPhase), "production_phase", productionPhase)
    //                    .eq(StringUtils.isNotEmpty(testProject),"`type`",testProject)
    //                    .eq(StringUtils.isNotEmpty(stage),"stage",stage)
    //                .between( "test_time", startDate, endDate + " 23:59:59");
    //        List<Statistics> statisticsList = dfOrtStrengthResultService.listBatchStatistics(qw);
    //        statisticsList.add(0, standard);
    //
    //        return new Result(200, "查询成功", statisticsList);
    //    }
//
//    @ApiOperation("结果汇总")
//    @GetMapping("/listStatistics")
//    public Result listResultStatistics(
//            String factory //工厂
//            , @ApiParam("项目")@RequestParam String model //项目
//            , @ApiParam("颜色")@RequestParam String color //颜色
//            , @ApiParam("生成阶段")@RequestParam String productionPhase //生成阶段
//            , @ApiParam("阶段")@RequestParam String stage //阶段
//            , @ApiParam("测试项目")@RequestParam String testProject //测试项目
//            , String config1
//            , String config2
//            , @ApiParam("开始时间")@RequestParam String startDate //开始时间
//            , @ApiParam("结束时间")@RequestParam String endDate //结束时间
//    ) {
//        QueryWrapper<DfOrtStrengthResult> qw = new QueryWrapper<>();
//
//        qw
//                .eq(StringUtils.isNotEmpty(model), "model", model)
//                .eq(StringUtils.isNotEmpty(color), "color", color)
//                .eq(StringUtils.isNotEmpty(productionPhase), "production_phase", productionPhase)
//                .eq(StringUtils.isNotEmpty(testProject),"`type`",testProject)
//                .eq(StringUtils.isNotEmpty(stage),"stage",stage)
//                .between("test_time", startDate + " 00:00:00", endDate + " 23:59:59");
//
//        List<Statistics2> statisticsList = dfOrtStrengthResultService.listResultStatistics(qw);
//        if (null != factory && !"".equals(factory)) {
//            for (Statistics2 statistics2 : statisticsList) {
//                if (statistics2.getName().equals("汇总")) {
//                    statisticsList.remove(statistics2);
//                }
//            }
//        }
//
//        return new Result(200, "查询成功", statisticsList);
//    }
//
//    @ApiOperation("单批次分析")
//    @GetMapping("/oneBatchStrength")
//    public Result oneBatchStatistics(
//            @RequestParam String batch,
//            @RequestParam String testProject,
//            @RequestParam String stage, // 白片 或者 成品
//            String model, String color
//    ) {
//        QueryWrapper<DfOrtStrengthResult> qw = new QueryWrapper<>();
//        qw.eq(!"".equals(batch), "batch", batch)
//                .eq(!"".equals(testProject), "type", testProject)
//                .eq(!"".equals(stage), "stage", stage)
//                .eq(!"".equals(model), "model", model)
//                .eq(!"".equals(color), "color", color);
//
//        DfOrtStrengthResult one = dfOrtStrengthResultService.getOne(qw);
//        if (null == one) return new Result(200, "查无数据");
//        String mes = "";
//        if (one.getChar632() < one.getChar632Standard()) mes = mes + "Char63.2 NG\n";
//        if (one.getB10() < one.getB10Standard()) mes = mes + "B10 NG\n";
//        if (one.getB5() < one.getB5Standard()) mes = mes + "B5 NG\n";
//        if (one.getTreshold() < one.getTresholdStandard()) mes = mes + "Min NG\n";
//        if ("".equals(mes)) mes = "OK";
//        Map<String, Object> result = new HashMap<>();
//        result.put("mes", mes);
//        result.put("result", one);
//
//        return new Result(200, "查询成功", result);
//    }
//
//
//    /**
//     * 获取ORT强度均值分析图
//     * @return
//     * @throws IOException
//     */
//    @GetMapping(value = "/getMeanAnalysis")
//    @ApiOperation("获取ORT强度均值分析图")
//    public Result getMeanAnalysis(
//            String factory //工厂
//            , @ApiParam("项目")@RequestParam String model //项目
//            , @ApiParam("颜色")@RequestParam String color //颜色
//            , @ApiParam("生成阶段")@RequestParam String productionPhase //生成阶段
//            , @ApiParam("阶段")@RequestParam String stage //阶段
//            , @ApiParam("测试项目")@RequestParam String testProject //测试项目
//            , String config1
//            , String config2
//            , @ApiParam("开始时间")@RequestParam String startDate //开始时间
//            , @ApiParam("结束时间")@RequestParam String endDate //结束时间
//    ) throws IOException {
//        try {
//            String redisKey = "ORT:强度:均值分析图:" + testProject + ":" + stage + ":" + model + ":" + color + ":" + startDate + ":" + endDate;
//
//            Object filename = redisUtils.get(redisKey);
//
//            if (filename != null){
//                return new Result(200, "获取图片成功", env.getProperty("imgUrl") + "/" + filename.toString());
//            }
//
//            QueryWrapper<DfOrtStrengthDetail> qw=new QueryWrapper<>();
//            qw
//                    .eq(StringUtils.isNotEmpty(model), "model", model)
//                    .eq(StringUtils.isNotEmpty(color), "color", color)
//                    .eq(StringUtils.isNotEmpty(productionPhase), "production_phase", productionPhase)
//                    .eq(StringUtils.isNotEmpty(testProject),"`type`",testProject)
//                    .eq(StringUtils.isNotEmpty(stage),"stage",stage)
//                    .between("test_time", startDate + " 00:00:00", endDate + " 23:59:59");
//
//            List<DfOrtStrengthDetail> list = dfOrtStrengthDetailService.getCheckNameDataList(qw);
//
//            if(list == null || list.size() == 0){
//                return new Result(500, "当前条件下没有相关数据");
//            }
//
//            Map<String, String> replaceMap = new HashMap<>();
//            replaceMap.put("#JSON_DATA#", JSON.toJSONString(list));
//            replaceMap.put("#X#", "batch");
//            replaceMap.put("#Y#", "value");
//
//            String jslFilePath = env.getProperty("jslPath") + "/均值分析图.jsl";
//            String jslCreatePath = env.getProperty("jslCreatePath");
//            String imgPath = env.getProperty("imgPath");
//
//            Map<String,String> urlMap = testController.modifyFileContent(jslFilePath, jslCreatePath,imgPath, replaceMap);
//            CmdScript.runCmd(urlMap.get("runJsl"));
//
//
//            File imgFile=new File(urlMap.get("imageUrl"));
//
//            if (!imgFile.exists()|| null== imgFile) {
//                return new Result(500,"查询失败");
//            }
//
//            boolean redisFlag = redisUtils.set(redisKey,urlMap.get("imageName"));
//
//            return new Result(200,"查询成功",env.getProperty("imgUrl") + "/" + urlMap.get("imageName"));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return new Result(500,"查询失败");
//    }
//
//    /**
//     * 获取ORT强度方差分析图
//     * @return
//     * @throws IOException
//     */
//    @GetMapping(value = "/getVarianceAnalysis")
//    @ApiOperation("获取ORT强度方差分析图")
//    public Result getVarianceAnalysis(
//            String factory //工厂
//            , @ApiParam("项目")@RequestParam String model //项目
//            , @ApiParam("颜色")@RequestParam String color //颜色
//            , @ApiParam("生成阶段")@RequestParam String productionPhase //生成阶段
//            , @ApiParam("阶段")@RequestParam String stage //阶段
//            , @ApiParam("测试项目")@RequestParam String testProject //测试项目
//            , String config1
//            , String config2
//            , @ApiParam("开始时间")@RequestParam String startDate //开始时间
//            , @ApiParam("结束时间")@RequestParam String endDate //结束时间
//    ) throws IOException {
//        try {
//            String redisKey = "ORT:强度:方差分析图:" + testProject + ":" + stage + ":" + model + ":" + color + ":" + startDate + ":" + endDate;
//
//            Object filename = redisUtils.get(redisKey);
//
//            if (filename != null){
//                return new Result(200, "获取图片成功", env.getProperty("imgUrl") + "/" + filename.toString());
//            }
//
//            QueryWrapper<DfOrtStrengthDetail> qw=new QueryWrapper<>();
//            qw
//                    .eq(StringUtils.isNotEmpty(model), "model", model)
//                    .eq(StringUtils.isNotEmpty(color), "color", color)
//                    .eq(StringUtils.isNotEmpty(productionPhase), "production_phase", productionPhase)
//                    .eq(StringUtils.isNotEmpty(testProject),"`type`",testProject)
//                    .eq(StringUtils.isNotEmpty(stage),"stage",stage)
//                    .between("test_time", startDate + " 00:00:00", endDate + " 23:59:59");
//
//            List<DfOrtStrengthDetail> list = dfOrtStrengthDetailService.getCheckNameDataList(qw);
//
//            if(list == null || list.size() == 0){
//                return new Result(500, "当前条件下没有相关数据");
//            }
//
//            Map<String, String> replaceMap = new HashMap<>();
//            replaceMap.put("#JSON_DATA#", JSON.toJSONString(list));
//            replaceMap.put("#X#", "batch");
//            replaceMap.put("#Y#", "value");
//
//            String jslFilePath = env.getProperty("jslPath") + "/方差分析图.jsl";
//            String jslCreatePath = env.getProperty("jslCreatePath");
//            String imgPath = env.getProperty("imgPath");
//
//            Map<String,String> urlMap = testController.modifyFileContent(jslFilePath, jslCreatePath,imgPath, replaceMap);
//            CmdScript.runCmd(urlMap.get("runJsl"));
//
//
//            File imgFile=new File(urlMap.get("imageUrl"));
//
//            if (!imgFile.exists()|| null== imgFile) {
//                return new Result(500,"查询失败");
//            }
//
//            boolean redisFlag = redisUtils.set(redisKey,urlMap.get("imageName"));
//
//            return new Result(200,"查询成功",env.getProperty("imgUrl") + "/" + urlMap.get("imageName"));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return new Result(500,"查询失败");
//    }
//
//    /**
//     * 获取ORT强度非参数叠加图
//     * @return
//     * @throws IOException
//     */
//    @GetMapping(value = "/getNonparametric")
//    @ApiOperation("获取ORT强度非参数叠加图")
//    public Result getNonparametric(
//            String factory //工厂
//            , @ApiParam("项目")@RequestParam String model //项目
//            , @ApiParam("颜色")@RequestParam String color //颜色
//            , @ApiParam("生成阶段")@RequestParam String productionPhase //生成阶段
//            , @ApiParam("阶段")@RequestParam String stage //阶段
//            , @ApiParam("测试项目")@RequestParam String testProject //测试项目
//            , String config1
//            , String config2
//            , @ApiParam("开始时间")@RequestParam String startDate //开始时间
//            , @ApiParam("结束时间")@RequestParam String endDate //结束时间
//    ) throws IOException {
//        try {
//            String redisKey = "ORT:强度:非参数叠加图:" + testProject + ":" + stage + ":" + model + ":" + color + ":" + startDate + ":" + endDate;
//
//            Object filename = redisUtils.get(redisKey);
//
//            if (filename != null){
//                return new Result(200, "获取图片成功", env.getProperty("imgUrl") + "/" + filename.toString());
//            }
//
//            QueryWrapper<DfOrtStrengthDetail> qw=new QueryWrapper<>();
//            qw
//                    .eq(StringUtils.isNotEmpty(model), "model", model)
//                    .eq(StringUtils.isNotEmpty(color), "color", color)
//                    .eq(StringUtils.isNotEmpty(productionPhase), "production_phase", productionPhase)
//                    .eq(StringUtils.isNotEmpty(testProject),"`type`",testProject)
//                    .eq(StringUtils.isNotEmpty(stage),"stage",stage)
//                    .between("test_time", startDate + " 00:00:00", endDate + " 23:59:59");
//
//            List<DfOrtStrengthDetail> list = dfOrtStrengthDetailService.getCheckNameDataList(qw);
//
//            if(list == null || list.size() == 0){
//                return new Result(500, "当前条件下没有相关数据");
//            }
//
//            Map<String, String> replaceMap = new HashMap<>();
//            replaceMap.put("#JSON_DATA#", JSON.toJSONString(list));
//            replaceMap.put("#X#", "batch");
//            replaceMap.put("#Y#", "value");
//
//            String jslFilePath = env.getProperty("jslPath") + "/非参数叠加图.jsl";
//            String jslCreatePath = env.getProperty("jslCreatePath");
//            String imgPath = env.getProperty("imgPath");
//
//            Map<String,String> urlMap = testController.modifyFileContent(jslFilePath, jslCreatePath,imgPath, replaceMap);
//            CmdScript.runCmd(urlMap.get("runJsl"));
//
//
//            File imgFile=new File(urlMap.get("imageUrl"));
//
//            if (!imgFile.exists()|| null== imgFile) {
//                return new Result(500,"查询失败");
//            }
//
//            boolean redisFlag = redisUtils.set(redisKey,urlMap.get("imageName"));
//
//            return new Result(200,"查询成功",env.getProperty("imgUrl") + "/" + urlMap.get("imageName"));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return new Result(500,"查询失败");
//    }

    /**
     * 获取ORT强度韦伯图
     * @return
     * @throws IOException
     */
    @GetMapping(value = "/getWeibullPlot")
    @ApiOperation("获取ORT强度韦伯图")
    public Result getWeibullPlot(
            @ApiParam("工厂")@RequestParam(value = "factory", required = false) String factory
            , @ApiParam("型号")@RequestParam(value = "project", required = false) String project
            , @ApiParam("颜色")@RequestParam(value = "color", required = false) String color
            , @ApiParam("阶段")@RequestParam(value = "stage", required = false) String stage
            , @ApiParam("工序")@RequestParam(value = "process", required = false) String process
            , @ApiParam("测试项目")@RequestParam(value = "checkItem", required = false) String checkItem
            , @ApiParam("开始时间")@RequestParam String startDate
            , @ApiParam("结束时间")@RequestParam String endDate
    ) throws IOException {
        String redisKey = "ORT:Strength:WeibullPlot:" + factory + ":" + project + ":" + color + ":" + stage + ":" + process + ":"+ checkItem + ":" + startDate + "_" + endDate;
        if (redisUtils.hasKey(redisKey)){
            String filename = (String) redisUtils.get(redisKey);

            String data = env.getProperty("imgUrl") + "/" + filename;
            return new Result(200,"获取ORT强度韦伯图成功",data);
        }

        String startTime = startDate + " 00:00:00";
        String endTime = endDate + " 23:59:59";

        //标准
        QueryWrapper<DfOrtStandardConfig> standardConfigQw = new QueryWrapper<>();
        standardConfigQw
                .eq("project", project)
                .eq("color", color)
                .eq( "stage", stage)
                .eq("process", process)
                .eq( "check_item", checkItem);
        List<DfOrtStandardConfig> standardConfigList = dfOrtStandardConfigService.list(standardConfigQw);

        if (CollectionUtils.isEmpty(standardConfigList)){
            return new Result(500, "当前条件下没有相关标准数据");
        }

        //标准数据
        Map<String,Double> configMap = new HashMap<>();
        for (DfOrtStandardConfig standConfig : standardConfigList) {
            configMap.put(standConfig.getCheckName(), standConfig.getStandardMin());
        }

        //sql参数
        Map<String, Object> sqlParamMap = new HashMap<>();
        sqlParamMap.put("orderSql", "name asc");
        //查询结果
        List<Map<String,String>> list = null;
        //替换数据
        Map<String, String> replaceMap = new HashMap<>();
        replaceMap.put("#X#", "name");
        replaceMap.put("#Y#", "value");
        replaceMap.put("#showY#", "测试值");
        replaceMap.put("#Char#",configMap.get("Char63.2").toString());
        replaceMap.put("#B10#",configMap.get("B10").toString());
        replaceMap.put("#B5#",configMap.get("B5").toString());
        replaceMap.put("#Min#",configMap.get("Min").toString());
        Double showMin = configMap.get("Min") - configMap.get("Min") * 0.1;
        replaceMap.put("#showMin#",showMin.toString());

        QueryWrapper<DfOrtTestData> qw = new QueryWrapper<>();
        qw
                .eq("factory", factory)
                .eq("project", project)
                .eq( "color", color)
                .eq("stage", stage)
                .eq("process", process)
                .eq("check_item", checkItem)
                .between("check_time", startTime, endTime);

        if (startDate.equals(endDate)){
            //需要查询的sql
            sqlParamMap.put("rowSql", "batch");
            sqlParamMap.put("selectSql", "batch name, value1 value");
            sqlParamMap.put("numSql", 10);

            replaceMap.put("#showX#", "batch");

            list = dfOrtTestDataService.getBatchDataList(qw, sqlParamMap);
        }else {
            //需要查询的sql
            sqlParamMap.put("rowSql", "check_date");
            sqlParamMap.put("selectSql", "check_date name, value1 value");
            sqlParamMap.put("numSql", 7);

            replaceMap.put("#showX#", "date");

            list = dfOrtTestDataService.getBatchDataList(qw, sqlParamMap);
        }

        if(CollectionUtils.isEmpty(list)){
            return new Result(500, "当前条件下没有相关数据");
        }

        replaceMap.put("#JSON_DATA#", JSON.toJSONString(list));

        String jslFilePath = env.getProperty("jslPath") + "/加上表生成韦伯图多批.jsl";
        String jslCreatePath = env.getProperty("jslCreatePath");
        String imgPath = env.getProperty("imgPath");

        Map<String,String> urlMap = testController.modifyFileContent(jslFilePath, jslCreatePath,imgPath, replaceMap);
        CmdScript.runCmd(urlMap.get("runJsl"));

        File imgFile=new File(urlMap.get("imageUrl"));

        if (!imgFile.exists()|| null== imgFile) {
            return new Result(500,"查询失败");
        }

        //更新到缓存
        redisUtils.set(redisKey,urlMap.get("imageName"),  60 * 60 * 24 * 7);

        String data = env.getProperty("imgUrl") + "/" + urlMap.get("imageName");

        return new Result(200,"获取ORT强度韦伯图成功",data);
    }

    @ApiOperation("标准值分布")
    @GetMapping("/listBatchStatistics")
    public Result listStatistics(
            @ApiParam("工厂")@RequestParam(value = "factory", required = false) String factory
            , @ApiParam("型号")@RequestParam(value = "project", required = false) String project
            , @ApiParam("颜色")@RequestParam(value = "color", required = false) String color
            , @ApiParam("阶段")@RequestParam(value = "stage", required = false) String stage
            , @ApiParam("工序")@RequestParam(value = "process", required = false) String process
            , @ApiParam("测试项目")@RequestParam(value = "checkItem", required = false) String checkItem
            , @ApiParam("开始时间")@RequestParam String startDate
            , @ApiParam("结束时间")@RequestParam String endDate
    ) {
        String startTime = startDate + " 00:00:00";
        String endTime = endDate + " 23:59:59";

        //结果
        List<Map<String, Object>> result = new ArrayList<>();

        //标准
        QueryWrapper<DfOrtStandardConfig> standardConfigQw = new QueryWrapper<>();
        standardConfigQw
                .eq("project", project)
                .eq("color", color)
                .eq( "stage", stage)
                .eq("process", process)
                .eq( "check_item", checkItem);
        List<DfOrtStandardConfig> standardConfigList = dfOrtStandardConfigService.list(standardConfigQw);

        if (CollectionUtils.isEmpty(standardConfigList)){
            return new Result(500, "当前条件下没有相关标准数据");
        }

        //标准数据
        Map<String, Object> standard = new HashMap<>();
        standard.put("name", "标准（>）");
        for (DfOrtStandardConfig standConfig : standardConfigList) {
            standard.put(standConfig.getCheckName(), standConfig.getStandardMin());
        }
        result.add(standard);

        //sql参数
        Map<String, Object> sqlParamMap = new HashMap<>();
        sqlParamMap.put("rowSql", "check_date");
        sqlParamMap.put("selectSql", "batch name, JSON_ARRAYAGG(value1) value");
        sqlParamMap.put("numSql", 7);
        sqlParamMap.put("groupSql", "name");
        sqlParamMap.put("orderSql", "name asc");

        QueryWrapper<DfOrtTestData> qw = new QueryWrapper<>();
        qw
                .eq("factory", factory)
                .eq("project", project)
                .eq("color", color)
                .eq("stage", stage)
                .eq("process", process)
                .eq("check_item", checkItem)
                .between("check_time", startTime, endTime);
        List<Map<String, String>> list = dfOrtTestDataService.getBatchArrayDataList(qw, sqlParamMap);
        if (CollectionUtils.isEmpty(list)) {
            return new Result(500, "当前条件下没有相关数据");
        }

        for (Map<String, String> map : list) {
            String name = map.get("name");
            String value_array_str = map.get("value");
            List<Double> valueList = JsonUtil.toObject(value_array_str, new TypeToken<List<Double>>() {});

            Double char632 = MathUtils.round(DataUtil.percentile(valueList, 0.632), 3);
            Double b10 = MathUtils.round(DataUtil.percentile(valueList, 0.9), 3);
            Double b5 = MathUtils.round(DataUtil.percentile(valueList, 0.5), 3);
            Double min = MathUtils.round(Collections.min(valueList), 3);

            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("name", name);
            resultMap.put("Char63.2", char632);
            resultMap.put("B10", b10);
            resultMap.put("B5", b5);
            resultMap.put("Min", min);
            result.add(resultMap);
        }

        return new Result(200, "查询成功", result);
    }

    @ApiOperation("结果汇总")
    @GetMapping("/resultStatistics")
    public Result resultStatistics(
            @ApiParam("工厂")@RequestParam(value = "factory", required = false) String factory
            , @ApiParam("型号")@RequestParam(value = "project", required = false) String project
            , @ApiParam("颜色")@RequestParam(value = "color", required = false) String color
            , @ApiParam("阶段")@RequestParam(value = "stage", required = false) String stage
            , @ApiParam("工序")@RequestParam(value = "process", required = false) String process
            , @ApiParam("测试项目")@RequestParam(value = "checkItem", required = false) String checkItem
            , @ApiParam("开始时间")@RequestParam String startDate
            , @ApiParam("结束时间")@RequestParam String endDate
    ) {
        String startTime = startDate + " 00:00:00";
        String endTime = endDate + " 23:59:59";

        //结果
        List<Map<String, Object>> result = new ArrayList<>();

        //标准
        QueryWrapper<DfOrtStandardConfig> standardConfigQw = new QueryWrapper<>();
        standardConfigQw
                .eq("project", project)
                .eq("color", color)
                .eq( "stage", stage)
                .eq("process", process)
                .eq( "check_item", checkItem);
        List<DfOrtStandardConfig> standardConfigList = dfOrtStandardConfigService.list(standardConfigQw);

        if (CollectionUtils.isEmpty(standardConfigList)){
            return new Result(500, "当前条件下没有相关标准数据");
        }

        //标准数据
        Map<String,Double> configMap = new HashMap<>();
        for (DfOrtStandardConfig standConfig : standardConfigList) {
            configMap.put(standConfig.getCheckName(), standConfig.getStandardMin());
        }

        Map<String, Object> sqlParamMap = new HashMap<>();
        sqlParamMap.put("rowSql", "check_date");
        sqlParamMap.put("selectSql", "batch name, JSON_ARRAYAGG(value1) value");
        sqlParamMap.put("numSql", 7);
        sqlParamMap.put("groupSql", "name");
        sqlParamMap.put("orderSql", "name asc");

        QueryWrapper<DfOrtTestData> qw = new QueryWrapper<>();
        qw
                .eq("factory", factory)
                .eq("project", project)
                .eq( "color", color)
                .eq("stage", stage)
                .eq("process", process)
                .eq("check_item", checkItem)
                .between("check_time", startTime, endTime);
        List<Map<String,String>> list = dfOrtTestDataService.getBatchArrayDataList(qw,sqlParamMap);

        if(list == null || list.size() == 0){
            return new Result(500, "当前条件下没有相关数据");
        }

        //总数
        Integer allNum = list.size();
        List<String> nameList = Arrays.asList("Char63.2", "B10", "B5", "Min", "总良率");
        Map<String,Integer> okNumMap = new HashMap<>();
        for (String name : nameList){
            okNumMap.put(name, 0);
        }

        for (Map<String, String> map : list){
            String name = map.get("name");
            String value_array_str = map.get("value");
            List<Double> valueList= JsonUtil.toObject(value_array_str, new TypeToken<List<Double>>(){});

            Double char632 = Math.round(DataUtil.percentile(valueList, 0.632) * 1000.0) / 1000.0;
            Double b10 = Math.round(DataUtil.percentile(valueList, 0.9) * 1000.0) / 1000.0;
            Double b5 = Math.round(DataUtil.percentile(valueList, 0.5) * 1000.0) / 1000.0;
            Double min = Math.round(Collections.min(valueList) * 1000.0) / 1000.0;

            if (char632 >= configMap.get("Char63.2")){
                okNumMap.put("Char63.2", okNumMap.get("Char63.2")+1);
            }

            if (b10 >= configMap.get("B10")){
                okNumMap.put("B10", okNumMap.get("B10")+1);
            }

            if (b5 >= configMap.get("B5")){
                okNumMap.put("B5", okNumMap.get("B5")+1);
            }

            if (min >= configMap.get("Min")){
                okNumMap.put("Min", okNumMap.get("Min")+1);
            }

            if (char632 >= configMap.get("Char63.2") && b10 >= configMap.get("B10") && b5 >= configMap.get("B5") && min >= configMap.get("Min")){
                okNumMap.put("总良率", okNumMap.get("总良率")+1);
            }
        }

        for (String name : nameList){
            String content = StringUtil.formatFT(allNum - okNumMap.get(name), okNumMap.get(name));
            Double okRate = MathUtils.round(okNumMap.get(name) * 100.0 / allNum,3);
            Map<String, Object> map = new HashMap<>();
            map.put("name", name);
            map.put("content", content);
            map.put("okRate", okRate);
            result.add(map);
        }
        return new Result(200, "查询成功", result);
    }

    /**
     * 获取ORT强度均值分析图
     * @return
     * @throws IOException
     */
    @GetMapping(value = "/getMeanAnalysis")
    @ApiOperation("获取ORT强度均值分析图")
    public Result getMeanAnalysis(
            @ApiParam("工厂")@RequestParam(value = "factory", required = false) String factory
            , @ApiParam("型号")@RequestParam(value = "project", required = false) String project
            , @ApiParam("颜色")@RequestParam(value = "color", required = false) String color
            , @ApiParam("阶段")@RequestParam(value = "stage", required = false) String stage
            , @ApiParam("工序")@RequestParam(value = "process", required = false) String process
            , @ApiParam("测试项目")@RequestParam(value = "checkItem", required = false) String checkItem
            , @ApiParam("开始时间")@RequestParam String startDate
            , @ApiParam("结束时间")@RequestParam String endDate
    ) throws IOException {

        String redisKey = "ORT:Strength:MeanAnalysis:" + factory + ":" + project + ":" + color + ":" + stage + ":" + process + ":"+ checkItem + ":" + startDate + "_" + endDate;
        if (redisUtils.hasKey(redisKey)){
            String filename = (String) redisUtils.get(redisKey);

            String data = env.getProperty("imgUrl") + "/" + filename;
            return new Result(200,"获取ORT强度均值分析图成功",data);
        }

        String startTime = startDate + " 00:00:00";
        String endTime = endDate + " 23:59:59";

        //sql参数
        Map<String, Object> sqlParamMap = new HashMap<>();
        sqlParamMap.put("orderSql", "name asc");
        //查询结果
        List<Map<String,String>> list = null;
        //替换参数
        Map<String, String> replaceMap = new HashMap<>();
        replaceMap.put("#X#", "name");
        replaceMap.put("#Y#", "value");
        replaceMap.put("#showY#", "测试值");

        QueryWrapper<DfOrtTestData> qw = new QueryWrapper<>();
        qw
                .eq("factory", factory)
                .eq("project", project)
                .eq( "color", color)
                .eq("stage", stage)
                .eq("process", process)
                .eq("check_item", checkItem)
                .between("check_time", startTime, endTime);

        if (startDate.equals(endDate)){
            //需要查询的sql
            sqlParamMap.put("rowSql", "batch");
            sqlParamMap.put("selectSql", "batch name, value1 value");
            sqlParamMap.put("numSql", 10);

            replaceMap.put("#showX#", "batch");

            list = dfOrtTestDataService.getBatchDataList(qw, sqlParamMap);
        }else {
            //需要查询的sql
            sqlParamMap.put("rowSql", "check_date");
            sqlParamMap.put("selectSql", "check_date name, value1 value");
            sqlParamMap.put("numSql", 7);

            replaceMap.put("#showX#", "date");

            list = dfOrtTestDataService.getBatchDataList(qw, sqlParamMap);
        }

        if(CollectionUtils.isEmpty(list)){
            return new Result(500, "当前条件下没有相关数据");
        }

        replaceMap.put("#JSON_DATA#", JSON.toJSONString(list));

        String jslFilePath = env.getProperty("jslPath") + "/均值分析图.jsl";
        String jslCreatePath = env.getProperty("jslCreatePath");
        String imgPath = env.getProperty("imgPath");

        Map<String,String> urlMap = testController.modifyFileContent(jslFilePath, jslCreatePath,imgPath, replaceMap);
        CmdScript.runCmd(urlMap.get("runJsl"));


        File imgFile=new File(urlMap.get("imageUrl"));

        if (!imgFile.exists()|| null== imgFile) {
            return new Result(500,"查询失败");
        }

        //更新缓存图片
        redisUtils.set(redisKey,urlMap.get("imageName"),  60 * 60 * 24 * 7);

        return new Result(200,"获取ORT强度均值分析图成功",env.getProperty("imgUrl") + "/" + urlMap.get("imageName"));
    }

    /**
     * 获取ORT强度方差分析图
     * @return
     * @throws IOException
     */
    @GetMapping(value = "/getVarianceAnalysis")
    @ApiOperation("获取方差分析图")
    public Result getVarianceAnalysis(
            @ApiParam("工厂")@RequestParam(value = "factory", required = false) String factory
            , @ApiParam("型号")@RequestParam(value = "project", required = false) String project
            , @ApiParam("颜色")@RequestParam(value = "color", required = false) String color
            , @ApiParam("阶段")@RequestParam(value = "stage", required = false) String stage
            , @ApiParam("工序")@RequestParam(value = "process", required = false) String process
            , @ApiParam("测试项目")@RequestParam(value = "checkItem", required = false) String checkItem
            , @ApiParam("开始时间")@RequestParam String startDate
            , @ApiParam("结束时间")@RequestParam String endDate
    ) throws IOException {

        String redisKey = "ORT:Strength:VarianceAnalysis:" + factory + ":" + project + ":" + color + ":" + stage + ":" + process + ":"+ checkItem + ":" + startDate + "_" + endDate;
        if (redisUtils.hasKey(redisKey)){
            String filename = (String) redisUtils.get(redisKey);

            String data = env.getProperty("imgUrl") + "/" + filename;
            return new Result(200,"获取ORT强度方差分析图",data);
        }

        String startTime = startDate + " 00:00:00";
        String endTime = endDate + " 23:59:59";

        //sql参数
        Map<String, Object> sqlParamMap = new HashMap<>();
        sqlParamMap.put("orderSql", "name asc");
        //查询结果
        List<Map<String,String>> list = null;
        //替换内容
        Map<String, String> replaceMap = new HashMap<>();
        replaceMap.put("#X#", "name");
        replaceMap.put("#Y#", "value");
        replaceMap.put("#showY#", "测试值");

        QueryWrapper<DfOrtTestData> qw = new QueryWrapper<>();
        qw
                .eq("factory", factory)
                .eq("project", project)
                .eq( "color", color)
                .eq("stage", stage)
                .eq("process", process)
                .eq("check_item", checkItem)
                .between("check_time", startTime, endTime);

        if (startDate.equals(endDate)){
            //需要查询的sql
            sqlParamMap.put("rowSql", "batch");
            sqlParamMap.put("selectSql", "batch name, value1 value");
            sqlParamMap.put("numSql", 10);

            replaceMap.put("#showX#", "batch");

            list = dfOrtTestDataService.getBatchDataList(qw, sqlParamMap);
        }else {
            //需要查询的sql
            sqlParamMap.put("rowSql", "check_date");
            sqlParamMap.put("selectSql", "check_date name, value1 value");
            sqlParamMap.put("numSql", 7);

            replaceMap.put("#showX#", "date");

            list = dfOrtTestDataService.getBatchDataList(qw, sqlParamMap);
        }

        if(CollectionUtils.isEmpty(list)){
            return new Result(500, "当前条件下没有相关数据");
        }

        replaceMap.put("#JSON_DATA#", JSON.toJSONString(list));

        String jslFilePath = env.getProperty("jslPath") + "/方差分析图.jsl";
        String jslCreatePath = env.getProperty("jslCreatePath");
        String imgPath = env.getProperty("imgPath");

        Map<String,String> urlMap = testController.modifyFileContent(jslFilePath, jslCreatePath,imgPath, replaceMap);
        CmdScript.runCmd(urlMap.get("runJsl"));

        File imgFile = new File(urlMap.get("imageUrl"));

        if (!imgFile.exists()|| null== imgFile) {
            return new Result(500,"查询失败");
        }

        // 更新缓存图片
        redisUtils.set(redisKey,urlMap.get("imageName"),  60 * 60 * 24 * 7);

        return new Result(200,"获取ORT强度方差分析图",env.getProperty("imgUrl") + "/" + urlMap.get("imageName"));
    }

    /**
     * 获取ORT强度非参数叠加图
     * @return
     * @throws IOException
     */
    @GetMapping(value = "/getNonparametric")
    @ApiOperation("获取ORT强度非参数叠加图")
    public Result getNonparametric(
            @ApiParam("工厂")@RequestParam(value = "factory", required = false) String factory
            , @ApiParam("型号")@RequestParam(value = "project", required = false) String project
            , @ApiParam("颜色")@RequestParam(value = "color", required = false) String color
            , @ApiParam("阶段")@RequestParam(value = "stage", required = false) String stage
            , @ApiParam("工序")@RequestParam(value = "process", required = false) String process
            , @ApiParam("测试项目")@RequestParam(value = "checkItem", required = false) String checkItem
            , @ApiParam("开始时间")@RequestParam String startDate
            , @ApiParam("结束时间")@RequestParam String endDate
    ) throws IOException {

        String redisKey = "ORT:Strength:Nonparametric:" + factory + ":" + project + ":" + color + ":" + stage + ":" + process + ":"+ checkItem + ":" + startDate + "_" + endDate;
        if (redisUtils.hasKey(redisKey)){
            String filename = (String) redisUtils.get(redisKey);

            String data = env.getProperty("imgUrl") + "/" + filename;
            return new Result(200,"获取ORT强度非参数叠加图成功",data);
        }

        String startTime = startDate + " 00:00:00";
        String endTime = endDate + " 23:59:59";

        //sql参数
        Map<String, Object> sqlParamMap = new HashMap<>();
        sqlParamMap.put("orderSql", "name asc");
        //查询结果
        List<Map<String,String>> list = null;
        //替换内容
        Map<String, String> replaceMap = new HashMap<>();
        replaceMap.put("#X#", "name");
        replaceMap.put("#Y#", "value");
        replaceMap.put("#showY#", checkItem);

        QueryWrapper<DfOrtTestData> qw = new QueryWrapper<>();
        qw
                .eq("factory", factory)
                .eq("project", project)
                .eq( "color", color)
                .eq("stage", stage)
                .eq("process", process)
                .eq("check_item", checkItem)
                .between("check_time", startTime, endTime);

        if (startDate.equals(endDate)){
            //需要查询的sql
            sqlParamMap.put("rowSql", "batch");
            sqlParamMap.put("selectSql", "batch name, value1 value");
            sqlParamMap.put("numSql", 10);

            replaceMap.put("#showX#", "batch");

            list = dfOrtTestDataService.getBatchDataList(qw, sqlParamMap);
        }else {
            //需要查询的sql
            sqlParamMap.put("rowSql", "check_date");
            sqlParamMap.put("selectSql", "check_date name, value1 value");
            sqlParamMap.put("numSql", 7);

            replaceMap.put("#showX#", "date");

            list = dfOrtTestDataService.getBatchDataList(qw, sqlParamMap);
        }

        if(list == null || list.size() == 0){
            return new Result(500, "当前条件下没有相关数据");
        }

        replaceMap.put("#JSON_DATA#", JSON.toJSONString(list));

        String jslFilePath = env.getProperty("jslPath") + "/非参数叠加图.jsl";
        String jslCreatePath = env.getProperty("jslCreatePath");
        String imgPath = env.getProperty("imgPath");

        Map<String,String> urlMap = testController.modifyFileContent(jslFilePath, jslCreatePath,imgPath, replaceMap);
        CmdScript.runCmd(urlMap.get("runJsl"));

        File imgFile=new File(urlMap.get("imageUrl"));

        if (!imgFile.exists()|| null== imgFile) {
            return new Result(500,"查询失败");
        }

        //  更新缓存图片
        redisUtils.set(redisKey,urlMap.get("imageName"),  60 * 60 * 24 * 7);

        return new Result(200,"获取ORT强度非参数叠加图成功",env.getProperty("imgUrl") + "/" + urlMap.get("imageName"));
    }

}
