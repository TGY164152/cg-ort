package com.ww.ort.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.gson.reflect.TypeToken;
import com.ww.ort.entity.*;
import com.ww.ort.service.DfOrtStandardConfigService;
import com.ww.ort.service.DfOrtTestDataService;
import com.ww.ort.service.DfOrtTestItemImportConfigService;
import com.ww.ort.utils.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.util.*;

/**
 * <p>
 * 颜色检测表 前端控制器
 * </p>
 *
 * @author zhao
 * @since 2023-06-25
 */
@Controller
@RequestMapping("/dfOrtColorDetail")
@CrossOrigin
@Api(tags = "ORT颜色检验")
@ResponseBody
public class DfOrtColorDetailController {

    @Autowired
    private Environment env;

    @Autowired
    private DfOrtTestDataService dfOrtTestDataService;

    @Autowired
    private DfOrtTestItemImportConfigService dfOrtTestItemImportConfigService;

    @Autowired
    private DfOrtStandardConfigService dfOrtStandardConfigService;

//    /**
//     * 获取ORT颜色检验多点位箱线图
//     * @return
//     * @throws IOException
//     */
//    @GetMapping(value = "/getBoxPlotMultiPoint")
//    @ApiOperation("获取ORT颜色检验多点位箱线图")
//    public Result getWeibullPlot(
//            @ApiParam("工厂")@RequestParam(value = "factory", required = false) String factory
//            , @ApiParam("型号")@RequestParam(value = "project", required = false) String project
//            , @ApiParam("颜色")@RequestParam(value = "color", required = false) String color
//            , @ApiParam("阶段")@RequestParam(value = "stage", required = false) String stage
//            , @ApiParam("工序")@RequestParam(value = "process", required = false) String process
//            , @ApiParam("测试项目")@RequestParam(value = "checkItem", required = false) String checkItem
//            , @ApiParam("开始时间")@RequestParam String startDate
//            , @ApiParam("结束时间")@RequestParam String endDate
//            , @ApiParam("测试类型")@RequestParam(value = "checkType", required = false) String checkType
//    ) throws IOException {
//
//        String startTime = startDate + " 00:00:00";
//        String endTime = endDate + " 23:59:59";
//
//        try {
////            String redisKey = "ORT:强度:韦伯图:" + testProject + ":" + stage + ":" + model + ":" + color + ":" + startDate + ":" + endDate;
////
////            Object filename = redisUtils.get(redisKey);
////
////            if (filename != null){
////                return new Result(200, "获取图片成功", env.getProperty("imgUrl") + "/" + filename.toString());
////            }
//
//            QueryWrapper<DfOrtTestItemImportConfig> configQw = new QueryWrapper<>();
//            configQw
//                    .eq("process", process)
//                    .eq("check_item", checkItem)
//                    .eq("check_type", checkType);
//
//            List<DfOrtTestItemImportConfig> configList = dfOrtTestItemImportConfigService.list(configQw);
//            if (CollectionUtils.isEmpty(configList)){
//                return new Result(500, "当前条件下没有相关测试项配置");
//            }
//
//            //标准
//            QueryWrapper<DfOrtStandardConfig> standardConfigQw = new QueryWrapper<>();
//            standardConfigQw
//                    .eq("project", project)
//                    .eq("color", color)
//                    .eq( "stage", stage)
//                    .eq("process", process)
//                    .eq( "check_item", checkItem)
//                    .eq("check_type", checkType);
//            List<DfOrtStandardConfig> standardConfigList = dfOrtStandardConfigService.list(standardConfigQw);
//
//            if (CollectionUtils.isEmpty(standardConfigList)){
//                return new Result(500, "当前条件下没有相关标准数据");
//            }
//
//            // 需要查询的sql集合
//            List<String> selectSqlNameList = new ArrayList<>();
//            // y轴
//            List<String> yColList = new ArrayList<>();
//            // 上限集合
//            List<Double> uslList = new ArrayList<>();
//            // 下限集合
//            List<Double> lslList = new ArrayList<>();
//
//            for (DfOrtTestItemImportConfig dfOrtTestItemImportConfig : configList) {
//                String checkName = dfOrtTestItemImportConfig.getCheckName();
//                String selectSqlName = dfOrtTestItemImportConfig.getCheckCode() + " " + checkName;
//                selectSqlNameList.add(selectSqlName);
//
//                yColList.add(checkName);
//            }
//
//            for (DfOrtStandardConfig standardConfigData : standardConfigList){
//                uslList.add(standardConfigData.getStandardMax());
//                lslList.add(standardConfigData.getStandardMin());
//            }
//
//            //sql参数
//            Map<String, Object> sqlParamMap = new HashMap<>();
//            sqlParamMap.put("orderSql", "name asc");
//            //查询结果
//            List<Map<String,String>> list = null;
//            //替换参数
//            Map<String, String> replaceMap = new HashMap<>();
//            replaceMap.put("#X#", "name");
//            replaceMap.put("#yCols#", JsonUtil.convertBrackets(JsonUtil.toJson(yColList)));
//            replaceMap.put("#yMaxs#", JsonUtil.convertBrackets(JsonUtil.toJson(uslList)));
//            replaceMap.put("#yMins#", JsonUtil.convertBrackets(JsonUtil.toJson(lslList)));
//
//            QueryWrapper<DfOrtTestData> qw = new QueryWrapper<>();
//            qw
//                    .eq("factory", factory)
//                    .eq("project", project)
//                    .eq( "color", color)
//                    .eq("stage", stage)
//                    .eq("process", process)
//                    .eq("check_item", checkItem)
//                    .eq("string1", checkType)
//                    .between("check_time", startTime, endTime);
//
//            if (startDate.equals(endDate)){
//                //需要查询的sql
//                sqlParamMap.put("rowSql", "batch");
//                selectSqlNameList.add(0, "batch name");
//                String selectSql = String.join(",", selectSqlNameList);
//                sqlParamMap.put("selectSql", selectSql);
//                sqlParamMap.put("numSql", 10);
//
//                list = dfOrtTestDataService.getBatchDataList(qw, sqlParamMap);
//            }else {
//                //需要查询的sql
//                sqlParamMap.put("rowSql", "check_date");
//                selectSqlNameList.add(0, "check_date name");
//                String selectSql = String.join(",", selectSqlNameList);
//                sqlParamMap.put("selectSql", selectSql);
//                sqlParamMap.put("numSql", 7);
//
//                list = dfOrtTestDataService.getBatchDataList(qw, sqlParamMap);
//            }
//
//            if(CollectionUtils.isEmpty(list)){
//                return new Result(500, "当前条件下没有相关数据");
//            }
//
//            replaceMap.put("#JSON_DATA#", JsonUtil.toJson(list));
//
//            String jslFilePath = env.getProperty("jslPath") + "/多点位箱线图脚本.jsl";
//            String jslCreatePath = env.getProperty("jslCreatePath");
//            String imgPath = env.getProperty("imgPath");
//
//            Map<String,String> urlMap = testController.modifyFileContent(jslFilePath, jslCreatePath,imgPath, replaceMap);
//            CmdScript.runCmd(urlMap.get("runJsl"));
//
//            File imgFile=new File(urlMap.get("imageUrl"));
//
//            if (!imgFile.exists()|| null== imgFile) {
//                return new Result(500,"查询失败");
//            }
//
////            boolean redisFlag = redisUtils.set(redisKey,urlMap.get("imageName"));
//
//            return new Result(200,"查询成功",env.getProperty("imgUrl") + "/" + urlMap.get("imageName"));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return new Result(500,"查询失败");
//    }


//    @ApiOperation("结果汇总")
//    @GetMapping("/resultStatistics")
//    public Result resultStatistics(
//            @ApiParam("工厂")@RequestParam(value = "factory", required = false) String factory
//            , @ApiParam("型号")@RequestParam(value = "project", required = false) String project
//            , @ApiParam("颜色")@RequestParam(value = "color", required = false) String color
//            , @ApiParam("阶段")@RequestParam(value = "stage", required = false) String stage
//            , @ApiParam("工序")@RequestParam(value = "process", required = false) String process
////            , @ApiParam("测试项目")@RequestParam(value = "checkItem", required = false) String checkItem
//            , @ApiParam("开始时间")@RequestParam String startDate
//            , @ApiParam("结束时间")@RequestParam String endDate
//            , @ApiParam("测试类型")@RequestParam(value = "checkType", required = false) String checkType
//    ) {
//        String startTime = startDate + " 00:00:00";
//        String endTime = endDate + " 23:59:59";
//
//        //结果
//        List<Map<String, Object>> result = new ArrayList<>();
//
//        QueryWrapper<DfOrtTestItemImportConfig> configQw = new QueryWrapper<>();
//        configQw
//                .eq("process", process)
//                .eq("check_type", checkType);
//
//        List<DfOrtTestItemImportConfig> configList = dfOrtTestItemImportConfigService.list(configQw);
//        if (CollectionUtils.isEmpty(configList)){
//            return new Result(500, "当前条件下没有相关测试项配置");
//        }
//
//        //标准
//        QueryWrapper<DfOrtStandardConfig> standardConfigQw = new QueryWrapper<>();
//        standardConfigQw
//                .eq("project", project)
//                .eq("color", color)
//                .eq( "stage", stage)
//                .eq("process", process)
//                .eq("check_type", checkType)
//                .orderByAsc("check_item");
//        List<DfOrtStandardConfig> standardConfigList = dfOrtStandardConfigService.list(standardConfigQw);
//
//        if (CollectionUtils.isEmpty(standardConfigList)){
//            return new Result(500, "当前条件下没有相关标准数据");
//        }
//
//        // 需要查询的sql集合
//        List<String> selectSqlNameList = new ArrayList<>();
//        //查询名称
//        List<String> selectNameList = new ArrayList<>();
//        for (DfOrtTestItemImportConfig dfOrtTestItemImportConfig : configList) {
//            String checkName = dfOrtTestItemImportConfig.getCheckName();
//            String checkCode = dfOrtTestItemImportConfig.getCheckCode();
//
//            if (selectNameList.contains(checkName)){
//                continue;
//            }
//
//            String selectSqlName = "JSON_ARRAYAGG("+ checkCode +") " + checkName;
//            selectSqlNameList.add(selectSqlName);
//        }
//
//        Map<String,Map<String,Object>> configMap = new HashMap<>();
//        List<String> checkItemList = new ArrayList<>();
//        for (DfOrtStandardConfig standardConfigData : standardConfigList) {
//            String checkItem = standardConfigData.getCheckItem();
//            String checkName = standardConfigData.getCheckName();
//            Double min = standardConfigData.getStandardMin();
//            Double max = standardConfigData.getStandardMax();
//
//            if (!configMap.containsKey(checkItem)){
//                Map<String,Object> checkItemMap = new HashMap<>();
//                Map<String,List<Double>> standardMap = new HashMap<>();
//
//                standardMap.put(checkName, Arrays.asList(min, max));
//                checkItemMap.put("standardMap", standardMap);
//                checkItemMap.put("okNum", 0);
//                checkItemMap.put("ngNum", 0);
//                configMap.put(checkItem, checkItemMap);
//                checkItemList.add(checkItem);
//            }else {
//                Map<String,List<Double>> standardMap = (Map<String, List<Double>>) configMap.get(checkItem).get("standardMap");
//                standardMap.put(checkName, Arrays.asList(min, max));
//            }
//        }
//
//        Integer allOkNum = 0;
//        Integer allNgNum = 0;
//
//        //sql参数
//        Map<String, Object> sqlParamMap = new HashMap<>();
//        //需要查询的sql
//        sqlParamMap.put("rowSql", "check_date");
//        selectSqlNameList.add(0, "check_item, batch");
//        String selectSql = String.join(",", selectSqlNameList);
//        sqlParamMap.put("selectSql", selectSql);
//        sqlParamMap.put("numSql", 7);
//        sqlParamMap.put("groupSql", "check_item, batch");
//        sqlParamMap.put("orderSql", "check_item asc, batch asc");
//
//        QueryWrapper<DfOrtTestData> qw = new QueryWrapper<>();
//        qw
//                .eq("factory", factory)
//                .eq("project", project)
//                .eq( "color", color)
//                .eq("stage", stage)
//                .eq("process", process)
//                .between("check_time", startTime, endTime)
//                .eq("string1", checkType);
//
//        //查询结果
//        List<Map<String,String>> list = dfOrtTestDataService.getBatchArrayDataList(qw,sqlParamMap);
//
//        if(CollectionUtils.isEmpty(list)){
//            return new Result(500, "当前条件下没有相关数据");
//        }
//
//        for (Map<String, String> map : list){
//            String checkItem = map.get("check_item");
//
//            if (!configMap.containsKey(checkItem)){
//                continue;
//            }
//
//            Map<String,Object> checkItemMap = configMap.get(checkItem);
//            Map<String,List<Double>> standardMap = (Map<String, List<Double>>) checkItemMap.get("standardMap");
//
//            boolean flag = true;
//
//            //循环标准
//            outer:
//            for (Map.Entry<String, List<Double>> standardEntry : standardMap.entrySet()){
//                String checkName = standardEntry.getKey();
//                List<Double> standardList = standardEntry.getValue();
//
//                String dataStr = map.get(checkName);
//                List<Double> dataList = JsonUtil.toObject(dataStr, new TypeToken<List<Double>>(){});
//
//                for (Double data : dataList){
//                    if (data < standardList.get(0) || data > standardList.get(1)){
//                        flag = false;
//                        break outer;
//                    }
//                }
//            }
//
//            if (flag){
//                checkItemMap.put("okNum", (Integer) checkItemMap.get("okNum") + 1);
//            }else {
//                checkItemMap.put("ngNum", (Integer) checkItemMap.get("ngNum") + 1);
//            }
//        }
//
//        for (String checkItem : checkItemList){
//            Map<String,Object> checkItemData = new HashMap<>();
//
//            Map<String,Object> checkItemMap = configMap.get(checkItem);
//
//            int okNum = (Integer) checkItemMap.get("okNum");
//            int ngNum = (Integer) checkItemMap.get("ngNum");
//
//            Double okRate = MathUtils.round(okNum * 100.0 / (ngNum + okNum),2);
//
//            checkItemData.put("name", checkItem);
//            checkItemData.put("result", StringUtil.formatFT(ngNum, okNum));
//            checkItemData.put("okRate", okRate);
//
//            result.add(checkItemData);
//            allOkNum += okNum;
//            allNgNum += ngNum;
//        }
//
//        Map<String, Object> allData = new HashMap<>();
//        allData.put("name", "总良率");
//        allData.put("result", StringUtil.formatFT(allNgNum, allOkNum));
//        allData.put("okRate", MathUtils.round(allOkNum * 100.0 / (allNgNum + allOkNum),2));
//        result.add(allData);
//        return new Result(200, "查询成功", result);
//    }
//
//
//    @ApiOperation("结果汇总明细")
//    @GetMapping("/resultStatisticsDetail")
//    public Result resultStatisticsDetail(
//            @ApiParam("工厂")@RequestParam(value = "factory", required = false) String factory
//            , @ApiParam("型号")@RequestParam(value = "project", required = false) String project
//            , @ApiParam("颜色")@RequestParam(value = "color", required = false) String color
//            , @ApiParam("阶段")@RequestParam(value = "stage", required = false) String stage
//            , @ApiParam("工序")@RequestParam(value = "process", required = false) String process
//            , @ApiParam("开始时间")@RequestParam String startDate
//            , @ApiParam("结束时间")@RequestParam String endDate
//            , @ApiParam("测试类型")@RequestParam(value = "checkType", required = false) String checkType
//    ) {
//        String startTime = startDate + " 00:00:00";
//        String endTime = endDate + " 23:59:59";
//
//        //结果
//        List<Map<String, Object>> result = new ArrayList<>();
//        //班次
//        List<String> dayOrNightList = Arrays.asList("A", "B");
//
//        QueryWrapper<DfOrtTestItemImportConfig> configQw = new QueryWrapper<>();
//        configQw
//                .eq("process", process)
//                .eq("check_type", checkType)
//                .orderByAsc("check_item");
//
//        List<DfOrtTestItemImportConfig> configList = dfOrtTestItemImportConfigService.list(configQw);
//        if (CollectionUtils.isEmpty(configList)){
//            return new Result(500, "当前条件下没有相关测试项配置");
//        }
//
//        //标准
//        QueryWrapper<DfOrtStandardConfig> standardConfigQw = new QueryWrapper<>();
//        standardConfigQw
//                .eq("project", project)
//                .eq("color", color)
//                .eq( "stage", stage)
//                .eq("process", process)
//                .eq("check_type", checkType)
//                .orderByAsc("check_item");
//        List<DfOrtStandardConfig> standardConfigList = dfOrtStandardConfigService.list(standardConfigQw);
//
//        if (CollectionUtils.isEmpty(standardConfigList)){
//            return new Result(500, "当前条件下没有相关标准数据");
//        }
//
//        // 需要查询的sql集合
//        List<String> selectSqlNameList = new ArrayList<>();
//        //查询名称
//        List<String> selectNameList = new ArrayList<>();
//        for (DfOrtTestItemImportConfig dfOrtTestItemImportConfig : configList) {
//            String checkName = dfOrtTestItemImportConfig.getCheckName();
//            String checkCode = dfOrtTestItemImportConfig.getCheckCode();
//
//            if (selectNameList.contains(checkName) || "L".equals(checkName)){
//                continue;
//            }
//
//            String selectSqlName = "JSON_ARRAYAGG("+ checkCode +") " + checkName;
//            selectSqlNameList.add(selectSqlName);
//            selectNameList.add(checkName);
//        }
//
//        Map<String,Map<String,Object>> configMap = new HashMap<>();
//        List<String> checkItemList = new ArrayList<>();
//        for (DfOrtStandardConfig standardConfigData : standardConfigList) {
//            String checkItem = standardConfigData.getCheckItem();
//            String checkName = standardConfigData.getCheckName();
//            Double min = standardConfigData.getStandardMin();
//            Double max = standardConfigData.getStandardMax();
//
//            if (!configMap.containsKey(checkItem)){
//                Map<String,Object> checkItemMap = new HashMap<>();
//                Map<String,List<Double>> standardMap = new HashMap<>();
//                Map<String, Integer> aMap = new HashMap<>();
//                aMap.put("okNum", 0);
//                aMap.put("ngNum", 0);
//
//                Map<String, Integer> bMap = new HashMap<>();
//                bMap.put("okNum", 0);
//                bMap.put("ngNum", 0);
//
//                standardMap.put(checkName, Arrays.asList(min, max));
//                checkItemMap.put("A", aMap);
//                checkItemMap.put("B", bMap);
//                checkItemMap.put("standardMap", standardMap);
//                configMap.put(checkItem, checkItemMap);
//                checkItemList.add(checkItem);
//            }else {
//                Map<String,List<Double>> standardMap = (Map<String, List<Double>>) configMap.get(checkItem).get("standardMap");
//                standardMap.put(checkName, Arrays.asList(min, max));
//            }
//        }
//
//        //sql参数
//        Map<String, Object> sqlParamMap = new HashMap<>();
//        //需要查询的sql
//        sqlParamMap.put("rowSql", "check_date");
//        selectSqlNameList.add(0, "check_item, day_or_night");
//        String selectSql = String.join(",", selectSqlNameList);
//        sqlParamMap.put("selectSql", selectSql);
//        sqlParamMap.put("numSql", 7);
//        sqlParamMap.put("orderSql", "check_item asc, day_or_night asc");
//
//        QueryWrapper<DfOrtTestData> qw = new QueryWrapper<>();
//        qw
//                .eq("factory", factory)
//                .eq("project", project)
//                .eq( "color", color)
//                .eq("stage", stage)
//                .eq("process", process)
//                .between("check_time", startTime, endTime)
//                .eq("string1", checkType);
//
//        //查询结果
//        List<Map<String,String>> list = dfOrtTestDataService.getBatchDataList(qw,sqlParamMap);
//
//        if(CollectionUtils.isEmpty(list)){
//            return new Result(500, "当前条件下没有相关数据");
//        }
//
//        for (Map<String, String> map : list){
//            String checkItem = map.get("check_item");
//            String dayOrNight = map.get("day_or_night");
//
//            if (!configMap.containsKey(checkItem)){
//                continue;
//            }
//
//            //测试项目Map
//            Map<String,Object> checkItemMap = configMap.get(checkItem);
//
//            if (!checkItemMap.containsKey(dayOrNight)){
//                continue;
//            }
//
//            //班次OK和NG数
//            Map<String,Integer> dayOrNightMap  = (Map<String,Integer>) checkItemMap.get(dayOrNight);
//
//            Map<String,List<Double>> standardMap = (Map<String, List<Double>>) checkItemMap.get("standardMap");
//            boolean flag = true;
//
//            for (Map.Entry<String, List<Double>> standardEntry : standardMap.entrySet()){
//                String checkName = standardEntry.getKey();
//                List<Double> standardList = standardEntry.getValue();
//
//                Double checkValue = Double.valueOf(String.valueOf(map.get(checkName)));
//
//                if (checkValue < standardList.get(0) || checkValue > standardList.get(1)){
//                    flag = false;
//                    break;
//                }
//            }
//
//            if (flag){
//                dayOrNightMap.put("okNum", dayOrNightMap.get("okNum") + 1);
//            }else {
//                dayOrNightMap.put("ngNum", dayOrNightMap.get("ngNum") + 1);
//            }
//        }
//
//        for (String checkItem : checkItemList){
//            Map<String,Object> checkItemData = new HashMap<>();
//
//            Map<String,Object> checkItemMap = configMap.get(checkItem);
//            List<Map<String,Object>> dayOrNightDataList = new ArrayList<>();
//
//            for (String dayOrNight : dayOrNightList){
//                Map<String,Integer> dayOrNightMap  = (Map<String,Integer>) checkItemMap.get(dayOrNight);
//                Integer okNum = dayOrNightMap.get("okNum");
//                Integer ngNum = dayOrNightMap.get("ngNum");
//                Integer allNum = okNum + ngNum;
//                Double okRate = allNum == 0 ? 0 : MathUtils.round(okNum * 100.0 / allNum,2);
//
//                Map<String,Object> dayOrNightData = new HashMap<>();
//                dayOrNightData.put("dayOrNight", dayOrNight);
//                dayOrNightData.put("okNum", okNum);
//                dayOrNightData.put("ngNum", ngNum);
//                dayOrNightData.put("allNum", allNum);
//                dayOrNightData.put("okRate", okRate);
//                dayOrNightDataList.add(dayOrNightData);
//            }
//
//            checkItemData.put("name", checkItem);
//            checkItemData.put("dayOrNightDataList", dayOrNightDataList);
//            result.add(checkItemData);
//        }
//        return new Result(200, "查询成功", result);
//    }
//
//    /**
//     * 获取ORT颜色散点图
//     * @return
//     * @throws IOException
//     */
//    @GetMapping(value = "/getScatterPlot")
//    @ApiOperation("获取ORT颜色散点图")
//    public Result getScatterPlot(
//            @ApiParam("工厂")@RequestParam(value = "factory", required = false) String factory
//            , @ApiParam("型号")@RequestParam(value = "project", required = false) String project
//            , @ApiParam("颜色")@RequestParam(value = "color", required = false) String color
//            , @ApiParam("阶段")@RequestParam(value = "stage", required = false) String stage
//            , @ApiParam("工序")@RequestParam(value = "process", required = false) String process
////            , @ApiParam("测试项目")@RequestParam(value = "checkItem", required = false) String checkItem
//            , @ApiParam("开始时间")@RequestParam String startDate
//            , @ApiParam("结束时间")@RequestParam String endDate
//            , @ApiParam("测试类型")@RequestParam(value = "checkType", required = false) String checkType
//    ) throws IOException {
//
//        String startTime = startDate + " 00:00:00";
//        String endTime = endDate + " 23:59:59";
//
//        //结果
//        List<Map<String,Object>> result = new ArrayList<>();
//
//        QueryWrapper<DfOrtTestItemImportConfig> configQw = new QueryWrapper<>();
//        configQw
//                .eq("process", process)
//                .eq("check_type", checkType);
//
//        List<DfOrtTestItemImportConfig> configList = dfOrtTestItemImportConfigService.list(configQw);
//        if (CollectionUtils.isEmpty(configList)){
//            return new Result(500, "当前条件下没有相关测试项配置");
//        }
//
//        //标准
//        QueryWrapper<DfOrtStandardConfig> standardConfigQw = new QueryWrapper<>();
//        standardConfigQw
//                .eq("project", project)
//                .eq("color", color)
//                .eq( "stage", stage)
//                .eq("process", process)
//                .eq("check_type", checkType)
//                .orderByAsc("check_item");
//        List<DfOrtStandardConfig> standardConfigList = dfOrtStandardConfigService.list(standardConfigQw);
//
//        if (CollectionUtils.isEmpty(standardConfigList)){
//            return new Result(500, "当前条件下没有相关标准数据");
//        }
//
//        // 需要查询的sql集合
//        List<String> selectSqlNameList = new ArrayList<>();
//        //查询名称
//        List<String> selectNameList = new ArrayList<>();
//        for (DfOrtTestItemImportConfig dfOrtTestItemImportConfig : configList) {
//            String checkName = dfOrtTestItemImportConfig.getCheckName();
//            String checkCode = dfOrtTestItemImportConfig.getCheckCode();
//
//            if (selectNameList.contains(checkName) || "L".equals(checkName)){
//                continue;
//            }
//
//            String selectSqlName = checkCode + " `" + checkName + "`";
//            selectSqlNameList.add(selectSqlName);
//            selectNameList.add(checkName);
//        }
//
//        Map<String,Object> configMap = new HashMap<>();
//        List<String> checkItemList = new ArrayList<>();
//        for (DfOrtStandardConfig standardConfig : standardConfigList){
//            String checkItem = standardConfig.getCheckItem();
//            String checkName = standardConfig.getCheckName();
//
//            if ("Y".equals(checkName) || "L".equals(checkName)){
//                continue;
//            }
//
//            if (!configMap.containsKey(checkItem)){
//                Map<String,Object> checkItemData = new HashMap<>();
//                Map<String,Object> checkNameData = new HashMap<>();
//                checkNameData.put("name", checkName);
//                checkNameData.put("dataList", new ArrayList<List<Double>>());
//                checkItemData.put(checkName, checkNameData);
//                configMap.put(checkItem, checkItemData);
//                checkItemList.add(checkItem);
//            }else {
//                Map<String,Object> checkItemData = (Map<String,Object>) configMap.get(checkItem);
//                Map<String,Object> checkNameData = new HashMap<>();
//                checkNameData.put("name", checkName);
//                checkNameData.put("dataList", new ArrayList<List<Double>>());
//                checkItemData.put(checkName, checkNameData);
//            }
//        }
//
//        //sql参数
//        Map<String, Object> sqlParamMap = new HashMap<>();
//        //需要查询的sql
//        sqlParamMap.put("rowSql", "check_date");
//        selectSqlNameList.add(0, "check_item name");
//        String selectSql = String.join(",", selectSqlNameList);
//        sqlParamMap.put("selectSql", selectSql);
//        sqlParamMap.put("numSql", 7);
//        sqlParamMap.put("orderSql", "name asc");
//
//        QueryWrapper<DfOrtTestData> qw = new QueryWrapper<>();
//        qw
//                .eq("factory", factory)
//                .eq("project", project)
//                .eq( "color", color)
//                .eq("stage", stage)
//                .eq("process", process)
//                .eq("string1", checkType)
//                .between("check_time", startTime, endTime);
//
//        //查询结果
//        List<Map<String,String>> list = dfOrtTestDataService.getBatchDataList(qw, sqlParamMap);
//
//        if(CollectionUtils.isEmpty(list)){
//            return new Result(500, "当前条件下没有相关数据");
//        }
//
//        for (Map<String, String> map : list){
//            String checkItem = map.get("name");
//            Double Y = Double.valueOf(String.valueOf(map.get("Y")));
//
//            if (!configMap.containsKey(checkItem)){
//                continue;
//            }
//
//            Map<String,Object> checkItemData = (Map<String,Object>) configMap.get(checkItem);
//            for (Map.Entry<String,Object> entry : checkItemData.entrySet()){
//                String checkName = entry.getKey();
//                List<List<Double>> dataList = (List<List<Double>>) entry.getValue();
//
//                Double value = Double.valueOf(String.valueOf(map.get(checkName)));
//                dataList.add(Arrays.asList(value, Y));
//            }
//        }
//
//        for (String checkItem : checkItemList){
//            Map<String,Object> checkItemData = (Map<String,Object>) configMap.get(checkItem);
//
//            Map<String,Object> map = new HashMap<>();
//            map.put("name", checkItem);
//            map.put("checkItemData", checkItemData);
//            result.add(map);
//        }
//
//        return new Result(200,"查询成功",result);
//    }
//
//    @ApiOperation("班次良率")
//    @GetMapping("/dayOrNightOkRate")
//    public Result dayOrNightOkRate(
//            @ApiParam("工厂")@RequestParam(value = "factory", required = false) String factory
//            , @ApiParam("型号")@RequestParam(value = "project", required = false) String project
//            , @ApiParam("颜色")@RequestParam(value = "color", required = false) String color
//            , @ApiParam("阶段")@RequestParam(value = "stage", required = false) String stage
//            , @ApiParam("工序")@RequestParam(value = "process", required = false) String process
////            , @ApiParam("测试项目")@RequestParam(value = "checkItem", required = false) String checkItem
//            , @ApiParam("开始时间")@RequestParam String startDate
//            , @ApiParam("结束时间")@RequestParam String endDate
//            , @ApiParam("测试类型")@RequestParam(value = "checkType", required = false) String checkType
//    ) {
//        String startTime = startDate + " 00:00:00";
//        String endTime = endDate + " 23:59:59";
//
//        List<String> dayOrNightList = Arrays.asList("A", "B");
//
//        //结果
//        Map<String, Object> result = new HashMap<>();
//        //日期
//        List<String> dateList = new ArrayList<>();
//        //A班抽检数
//        List<Integer> aNumList = new ArrayList<>();
//        //B班抽检数
//        List<Integer> bNumList = new ArrayList<>();
//        //良率
//        List<Map<String,Object>> okRateMapList = new ArrayList<>();
//
//        QueryWrapper<DfOrtTestItemImportConfig> configQw = new QueryWrapper<>();
//        configQw
//                .eq("process", process);
//
//        List<DfOrtTestItemImportConfig> configList = dfOrtTestItemImportConfigService.list(configQw);
//        if (CollectionUtils.isEmpty(configList)){
//            return new Result(500, "当前条件下没有相关测试项配置");
//        }
//
//        //标准
//        QueryWrapper<DfOrtStandardConfig> standardConfigQw = new QueryWrapper<>();
//        standardConfigQw
//                .eq("project", project)
//                .eq("color", color)
//                .eq( "stage", stage)
//                .eq("process", process)
//                .orderByAsc("check_item");
//        List<DfOrtStandardConfig> standardConfigList = dfOrtStandardConfigService.list(standardConfigQw);
//
//        if (CollectionUtils.isEmpty(standardConfigList)){
//            return new Result(500, "当前条件下没有相关标准数据");
//        }
//
//        // 需要查询的sql集合
//        List<String> selectSqlNameList = new ArrayList<>();
//        //查询名称
//        List<String> selectNameList = new ArrayList<>();
//        for (DfOrtTestItemImportConfig dfOrtTestItemImportConfig : configList) {
//            //测试值字段
//            String checkCode = dfOrtTestItemImportConfig.getCheckCode();
//            // 测试名称
//            String checkName = dfOrtTestItemImportConfig.getCheckName();
//
//            if (selectNameList.contains(checkName) || "L".equals(checkName)){
//                continue;
//            }
//
//            String selectSqlName = checkCode + " `" + checkName + "`";
//            selectSqlNameList.add(selectSqlName);
//            selectNameList.add(checkName);
//        }
//
//        Map<String,Object> configMap = new HashMap<>();
//        List<String> checkItemList = new ArrayList<>();
//        for (DfOrtStandardConfig standardConfigData : standardConfigList) {
//            String checkItem = standardConfigData.getCheckItem();
//            String checkName = standardConfigData.getCheckName();
//            Double min = standardConfigData.getStandardMin();
//            Double max = standardConfigData.getStandardMax();
//
//            if (!configMap.containsKey(checkItem)){
//                Map<String,List<Double>> checkNameData = new HashMap<>();
//                checkNameData.put(checkName, Arrays.asList(min, max));
//                configMap.put(checkItem, checkNameData);
//                checkItemList.add(checkItem);
//            }else {
//                Map<String,List<Double>> checkNameData = (Map<String,List<Double>>) configMap.get(checkItem);
//                checkNameData.put(checkName, Arrays.asList(min, max));
//            }
//        }
//
//        //sql参数
//        Map<String, Object> sqlParamMap = new HashMap<>();
//        //需要查询的sql
//        sqlParamMap.put("rowSql", "check_date");
//        selectSqlNameList.add(0, "check_item, check_date, day_or_night");
//        String selectSql = String.join(",", selectSqlNameList);
//        sqlParamMap.put("selectSql", selectSql);
//        sqlParamMap.put("numSql", 7);
//        sqlParamMap.put("groupSql", " check_item, check_date, day_or_night ");
//
//        QueryWrapper<DfOrtTestData> qw = new QueryWrapper<>();
//        qw
//                .eq("factory", factory)
//                .eq("project", project)
//                .eq( "color", color)
//                .eq("stage", stage)
//                .eq("process", process)
//                .between("check_time", startTime, endTime);
//
//        //查询结果
//        List<Map<String,String>> list = dfOrtTestDataService.getBatchDataList(qw,sqlParamMap);
//
//        if(CollectionUtils.isEmpty(list)){
//            return new Result(500, "当前条件下没有相关数据");
//        }
//
//        //结果map
//        Map<String, Object> resultMap = new HashMap<>();
//
//        for (Map<String, String> map : list) {
//            String checkItem = map.get("check_item");
//            String checkDate = map.get("check_date");
//            String dayOrNight = map.get("day_or_night");
//            boolean flag = true;
//
//            //测试项标准数据
//            Map<String,Object> checkItemStandardData = (Map<String,Object>) configMap.get(checkItem);
//
//            for (Map.Entry<String, Object> entry : checkItemStandardData.entrySet()) {
//                String checkName = entry.getKey();
//                List<Double> standardList = (List<Double>) entry.getValue();
//
//                Double checkValue = Double.valueOf(String.valueOf(map.get(checkName)));
//
//                if (checkValue > standardList.get(1)) {
//                    flag = false;
//                    break;
//                }
//            }
//
//            //判断该日期是否存在
//            if (!resultMap.containsKey(checkDate)) {
//                Map<String, Map<String, Integer>> checkDateMap = new HashMap<>();
//                Map<String, Integer> aMap = new HashMap<>();
//                Map<String, Integer> bMap = new HashMap<>();
//
//                aMap.put("allNum", 0);
//                bMap.put("allNum", 0);
//                checkDateMap.put("A", aMap);
//                checkDateMap.put("B", bMap);
//
//                for (String checkItemStr : checkItemList){
//                    Map<String, Integer> checkItemMap = new HashMap<>();
//                    checkItemMap.put("okNum", 0);
//                    checkItemMap.put("allNum", 0);
//                    checkDateMap.put(checkItemStr, checkItemMap);
//
//                    Map<String,Object> okRateMap = new HashMap<>();
//                    okRateMap.put("name", checkItemStr);
//                    okRateMap.put("dataList", new ArrayList<>());
//                    configMap.put(checkItemStr,okRateMap);
//                }
//
//                checkDateMap.get(checkItem).put("allNum", 1);
//                checkDateMap.get(dayOrNight).put("allNum", 1);
//                if (flag) {
//                    checkDateMap.get(checkItem).put("okNum", 1);
//                }
//
//                resultMap.put(checkDate, checkDateMap);
//                dateList.add(checkDate);
//
//            } else {
//                Map<String, Object> checkDateMap = (Map<String, Object>) resultMap.get(checkDate);
//                Map<String, Integer> abMap = (Map<String, Integer>) checkDateMap.get(dayOrNight);
//                Map<String, Integer> checkItemMap = (Map<String, Integer>) checkDateMap.get(checkItem);
//
//                checkItemMap.put("allNum", checkItemMap.get("allNum") + 1);
//                abMap.put("allNum", abMap.get("allNum") + 1);
//                if (flag) {
//                    checkItemMap.put("okNum", checkItemMap.get("okNum") + 1);
//                }
//            }
//        }
//
//        //总良率
//        List<Double> allOKRateList = new ArrayList<>();
//
//        for (String checkDate : dateList){
//            Map<String, Object> checkDateMap = (Map<String, Object>) resultMap.get(checkDate);
//
//            //单日总投入数
//            Integer checkDataAllNum = 0;
//            //单日合格数
//            Integer checkDataOkNum = 0;
//
//            for (String checkItem : checkItemList){
//                Map<String, Integer> checkItemMap = (Map<String, Integer>) checkDateMap.get(checkItem);
//                Map<String, Object> okRateMap = (Map<String, Object>) configMap.get(checkItem);
//
//                Integer allNum = checkItemMap.get("allNum");
//                Integer okNum = checkItemMap.get("okNum");
//                Double okRate = allNum == 0 ? 0 : MathUtils.round(okNum * 100.0 / allNum, 2);
//
//                List<Double> dataList = (List<Double>) okRateMap.get("dataList");
//                dataList.add(okRate);
//
//                checkDataAllNum += allNum;
//                checkDataOkNum += okNum;
//            }
//
//            Double checkDataOkRate = checkDataAllNum == 0 ? 0 : MathUtils.round(checkDataOkNum * 100.0 / checkDataAllNum, 2);
//            allOKRateList.add(checkDataOkRate);
//
//            for (String dayOrNight : dayOrNightList){
//                Map<String, Integer> abMap = (Map<String, Integer>) checkDateMap.get(dayOrNight);
//                Integer allNum = abMap.get("allNum");
//
//                if ("A".equals(dayOrNight)){
//                    aNumList.add(allNum);
//                }else {
//                    bNumList.add(allNum);
//                }
//            }
//        }
//
//        for (String checkItem : checkItemList){
//            Map<String, Object> okRateMap = (Map<String, Object>) configMap.get(checkItem);
//            okRateMapList.add(okRateMap);
//        }
//
//        Map<String, Object> allOkRateMap = new HashMap<>();
//        allOkRateMap.put("name", "总良率");
//        allOkRateMap.put("dataList", allOKRateList);
//        okRateMapList.add(allOkRateMap);
//
//        result.put("dateList", dateList);
//        result.put("okRateMapList", okRateMapList);
//        result.put("aNumList", aNumList);
//        result.put("bNumList", bNumList);
//        return new Result(200, "查询成功", result);
//    }
//
//
//    @ApiOperation("良率统计")
//    @GetMapping("/okRateStatistics")
//    public Result okRateStatistics(
//            @ApiParam("工厂")@RequestParam(value = "factory", required = false) String factory
//            , @ApiParam("型号")@RequestParam(value = "project", required = false) String project
//            , @ApiParam("颜色")@RequestParam(value = "color", required = false) String color
//            , @ApiParam("阶段")@RequestParam(value = "stage", required = false) String stage
//            , @ApiParam("工序")@RequestParam(value = "process", required = false) String process
////            , @ApiParam("测试项目")@RequestParam(value = "checkItem", required = false) String checkItem
//            , @ApiParam("开始时间")@RequestParam String startDate
//            , @ApiParam("结束时间")@RequestParam String endDate
//            , @ApiParam("测试类型")@RequestParam(value = "checkType", required = false) String checkType
//    ) {
//        String startTime = startDate + " 00:00:00";
//        String endTime = endDate + " 23:59:59";
//
//        //结果
//        List<Map<String, Object>> result = new ArrayList<>();
//
//        QueryWrapper<DfOrtTestItemImportConfig> configQw = new QueryWrapper<>();
//        configQw
//                .eq("process", process)
//                .eq("check_type", checkType)
//                .orderByAsc("check_item");
//
//        List<DfOrtTestItemImportConfig> configList = dfOrtTestItemImportConfigService.list(configQw);
//        if (CollectionUtils.isEmpty(configList)){
//            return new Result(500, "当前条件下没有相关测试项配置");
//        }
//
//        //标准
//        QueryWrapper<DfOrtStandardConfig> standardConfigQw = new QueryWrapper<>();
//        standardConfigQw
//                .eq("project", project)
//                .eq("color", color)
//                .eq( "stage", stage)
//                .eq("process", process)
//                .eq("check_type", checkType)
//                .orderByAsc("check_item");
//        List<DfOrtStandardConfig> standardConfigList = dfOrtStandardConfigService.list(standardConfigQw);
//
//        if (CollectionUtils.isEmpty(standardConfigList)){
//            return new Result(500, "当前条件下没有相关标准数据");
//        }
//
//        // 需要查询的sql集合
//        List<String> selectSqlNameList = new ArrayList<>();
//        //查询名称
//        List<String> selectNameList = new ArrayList<>();
//        for (DfOrtTestItemImportConfig dfOrtTestItemImportConfig : configList) {
//            String checkName = dfOrtTestItemImportConfig.getCheckName();
//            String checkCode = dfOrtTestItemImportConfig.getCheckCode();
//
//            if (selectSqlNameList.contains(checkName) || "L".equals(checkName) ){
//                continue;
//            }
//
//            String selectSqlName = "JSON_ARRAYAGG("+ checkCode +") " + checkName;
//            selectSqlNameList.add(selectSqlName);
//            selectNameList.add(selectSqlName);
//        }
//
//        Map<String,Map<String,Object>> configMap = new HashMap<>();
//        List<String> checkItemList = new ArrayList<>();
//        for (DfOrtStandardConfig standardConfigData : standardConfigList) {
//            String checkItem = standardConfigData.getCheckItem();
//            String checkName = standardConfigData.getCheckName();
//            Double min = standardConfigData.getStandardMin();
//            Double max = standardConfigData.getStandardMax();
//
//            if (!configMap.containsKey(checkItem)){
//                Map<String,Object> checkItemMap = new HashMap<>();
//                Map<String,Object> checkNameMap = new HashMap<>();
//
//                checkNameMap.put("usl", max);
//                checkNameMap.put("lsl", min);
//                checkNameMap.put("name", checkName);
//
//                checkItemMap.put("checkNameMap", checkNameMap);
//                checkItemMap.put("okNum", 0);
//                checkItemMap.put("ngNum", 0);
//                configMap.put(checkItem, checkItemMap);
//                checkItemList.add(checkItem);
//            }else {
//                Map<String,Object> checkNameMap = (Map<String, Object>) configMap.get(checkItem).get("checkNameMap");
//                checkNameMap.put("usl", max);
//                checkNameMap.put("lsl", min);
//                checkNameMap.put("name", checkName);
//            }
//        }
//
//        Integer allOkNum = 0;
//        Integer allNgNum = 0;
//
//        //sql参数
//        Map<String, Object> sqlParamMap = new HashMap<>();
//        //需要查询的sql
//        sqlParamMap.put("rowSql", "check_date");
//        selectSqlNameList.add(0, "check_item, batch");
//        String selectSql = String.join(",", selectSqlNameList);
//        sqlParamMap.put("selectSql", selectSql);
//        sqlParamMap.put("numSql", 7);
//        sqlParamMap.put("groupSql", "check_item, batch");
//        sqlParamMap.put("orderSql", "check_item asc, batch asc");
//
//        QueryWrapper<DfOrtTestData> qw = new QueryWrapper<>();
//        qw
//                .eq("factory", factory)
//                .eq("project", project)
//                .eq( "color", color)
//                .eq("stage", stage)
//                .eq("process", process)
//                .between("check_time", startTime, endTime)
//                .eq("string1", checkType);
//
//        //查询结果
//        List<Map<String,String>> list = dfOrtTestDataService.getBatchArrayDataList(qw,sqlParamMap);
//
//        if(CollectionUtils.isEmpty(list)){
//            return new Result(500, "当前条件下没有相关数据");
//        }
//
//        for (Map<String, String> map : list){
//            String checkItem = map.get("check_item");
//
//            if (!configMap.containsKey(checkItem)){
//                continue;
//            }
//
//            Map<String,Object> checkItemMap = configMap.get(checkItem);
//            Map<String,Object> checkNameMap = (Map<String, Object>) checkItemMap.get("checkNameMap");
//
//            boolean flag = true;
//
//            for (Map.Entry<String, Object> checkNameEntry : checkNameMap.entrySet()){
//                String checkName = checkNameEntry.getKey();
//                Map<String, Object> checkNameData = (Map<String, Object>) checkNameMap.get(checkName);
//                Double max = (Double) checkNameData.get("usl");
//                Double min = (Double) checkNameData.get("lsl");
//
//                String dataStr = map.get(checkName);
//                List<Double> dataList = JsonUtil.toObject(dataStr, new TypeToken<List<Double>>(){});
//                // 均值
//                double mean = MathUtils.round(MathUtils.calculateMean(dataList), 3);
//                // 方差
//                double variance = MathUtils.round(MathUtils.calculateVariance(dataList, mean), 3);
//                // 标准差
//                double stdDev = MathUtils.round(MathUtils.calculateStandardDeviation(variance), 3);
//                // CPK
//                double cpk = MathUtils.round(MathUtils.calculateCPK(max, min, mean, stdDev), 3);
//
//                checkNameData.put("dataList", dataList);
//                checkNameData.put("cpk", cpk);
//
//                for (Double data : dataList){
//                    if (data < min || data > max){
//                        flag = false;
//                    }
//                }
//            }
//
//            if (flag){
//                checkItemMap.put("okNum", (Integer) checkItemMap.get("okNum") + 1);
//            }else {
//                checkItemMap.put("ngNum", (Integer) checkItemMap.get("ngNum") + 1);
//            }
//        }
//
//        for (String checkItem : checkItemList){
//            Map<String,Object> checkItemMap = configMap.get(checkItem);
//
//            int okNum = (Integer) checkItemMap.get("okNum");
//            int ngNum = (Integer) checkItemMap.get("ngNum");
//            Double okRate = MathUtils.round(okNum * 100.0 / (ngNum + okNum),2);
//
//            Map<String,Object> checkItemData = new HashMap<>();
//            checkItemData.put("name", checkItem);
//            checkItemData.put("checkNameMap", checkItemMap.get("checkNameMap"));
//            checkItemData.put("result", StringUtil.formatFT(ngNum, okNum));
//            checkItemData.put("okRate", okRate);
//
//            result.add(checkItemData);
//            allOkNum += okNum;
//            allNgNum += ngNum;
//
//        }
//
//        Map<String, Object> allData = new HashMap<>();
//        allData.put("name", "总良率");
//        allData.put("result", StringUtil.formatFT(allNgNum, allOkNum));
//        allData.put("okRate", MathUtils.round(allOkNum * 100.0 / (allNgNum + allOkNum),2));
//        result.add(allData);
//        return new Result(200, "查询成功", result);
//    }


    /**
     * 获取箱线图
     * @return
     * @throws IOException
     */
    @GetMapping(value = "/getBoxplot")
    @ApiOperation("获取箱线图")
    public Result getBoxplot(
            @ApiParam("工厂")@RequestParam(required = false) String factory
            , @ApiParam("型号")@RequestParam(required = false) String project
            , @ApiParam("颜色")@RequestParam(required = false) String color
            , @ApiParam("阶段")@RequestParam(required = false) String stage
            , @ApiParam("工序")@RequestParam(required = false) String process
            , @ApiParam("测试项目")@RequestParam(required = false) String checkItem
            , @ApiParam("开始时间")@RequestParam String startDate
            , @ApiParam("结束时间")@RequestParam String endDate
            , @ApiParam("测试类型")@RequestParam(required = false) String checkType
    ) throws IOException {

        String startTime = startDate + " 00:00:00";
        String endTime = endDate + " 23:59:59";

        //结果
        List<Map<String, Object>> result = new ArrayList<>();

        QueryWrapper<DfOrtTestItemImportConfig> configQw = new QueryWrapper<>();
        configQw
                .eq("process", process)
                .eq("check_item", checkItem)
                .like("check_name",checkType)
                .orderByAsc("check_name");

        List<DfOrtTestItemImportConfig> configList = dfOrtTestItemImportConfigService.list(configQw);
        if (CollectionUtils.isEmpty(configList)){
            return new Result(500, "当前条件下没有相关测试项配置");
        }

        //标准
        QueryWrapper<DfOrtStandardConfig> standardConfigQw = new QueryWrapper<>();
        standardConfigQw
                .eq("project", project)
                .eq("color", color)
                .eq( "stage", stage)
                .eq("process", process)
                .eq( "check_item", checkItem)
                .like("check_name", checkType)
                .orderByAsc("check_name");
        List<DfOrtStandardConfig> standardConfigList = dfOrtStandardConfigService.list(standardConfigQw);

        if (CollectionUtils.isEmpty(standardConfigList)){
            return new Result(500, "当前条件下没有相关标准数据");
        }

        // 需要查询的sql集合
        List<String> selectSqlNameList = new ArrayList<>();
        for (DfOrtTestItemImportConfig dfOrtTestItemImportConfig : configList) {
            String checkNameType = dfOrtTestItemImportConfig.getCheckName();
            String checkName = checkNameType.split("_")[0];
            String checkCode = dfOrtTestItemImportConfig.getCheckCode();

            String selectSqlName = "JSON_ARRAYAGG("+ checkCode +") `" + checkName + "`";
            selectSqlNameList.add(selectSqlName);
        }

        Map<String,Object> configMap = new HashMap<>();
        List<String> checkNameList = new ArrayList<>();
        for (DfOrtStandardConfig standardConfigData : standardConfigList){
            String checkNameType = standardConfigData.getCheckName();
            String checkName = checkNameType.split("_")[0];
            Double min = standardConfigData.getStandardMin();
            Double max = standardConfigData.getStandardMax();

            Map<String,Object> checkNameData = new HashMap<>();
            checkNameData.put("name", checkName);
            checkNameData.put("lsl", min);
            checkNameData.put("usl", max);
            checkNameData.put("nameList", new ArrayList<String>());
            checkNameData.put("dataList", new ArrayList<List<Double>>());
            checkNameData.put("avgList", new ArrayList<Double>());
            configMap.put(checkName, checkNameData);
            checkNameList.add(checkName);
        }

        //sql参数
        Map<String, Object> sqlParamMap = new HashMap<>();
        sqlParamMap.put("groupSql", "name");
        sqlParamMap.put("orderSql", "name asc");

        //查询结果
        List<Map<String,String>> list = null;

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
            selectSqlNameList.add(0, "batch name");
            String selectSql = String.join(",", selectSqlNameList);
            sqlParamMap.put("selectSql", selectSql);
            sqlParamMap.put("numSql", 10);

            list = dfOrtTestDataService.getBatchArrayDataList(qw, sqlParamMap);
        }else {
            //需要查询的sql
            sqlParamMap.put("rowSql", "check_date");
            selectSqlNameList.add(0, "check_date name");
            String selectSql = String.join(",", selectSqlNameList);
            sqlParamMap.put("selectSql", selectSql);
            sqlParamMap.put("numSql", 7);

            list = dfOrtTestDataService.getBatchArrayDataList(qw, sqlParamMap);
        }

        if(CollectionUtils.isEmpty(list)){
            return new Result(500, "当前条件下没有相关数据");
        }

        for (String checkName : checkNameList){
            Map<String, Object> checkNameData = (Map<String, Object>) configMap.get(checkName);

            for (Map<String, String> map : list) {
                String name = map.get("name");

                List<String> nameList = (List<String>) checkNameData.get("nameList");
                List<List<Double>> dataList = (List<List<Double>>) checkNameData.get("dataList");
                List<Double> avgList = (List<Double>) checkNameData.get("avgList");

                List<Double> valueList = JsonUtil.toObject(map.get(checkName), new TypeToken<List<Double>>(){});
                Double avg = valueList.stream().mapToDouble(Double::doubleValue).average().orElse(0);

                nameList.add(name);
                dataList.add(valueList);
                avgList.add(MathUtils.round(avg, 2));
            }

            result.add(checkNameData);
        }

        return new Result(200,"查询成功",result);
    }

    @ApiOperation("结果汇总")
    @GetMapping("/resultStatistics")
    public Result resultStatistics(
            @ApiParam("工厂")@RequestParam(required = false) String factory
            , @ApiParam("型号")@RequestParam(required = false) String project
            , @ApiParam("颜色")@RequestParam(required = false) String color
            , @ApiParam("阶段")@RequestParam(required = false) String stage
            , @ApiParam("工序")@RequestParam(required = false) String process
            , @ApiParam("开始时间")@RequestParam String startDate
            , @ApiParam("结束时间")@RequestParam String endDate
    ) {
        String startTime = startDate + " 00:00:00";
        String endTime = endDate + " 23:59:59";

        //结果
        List<Map<String, Object>> result = new ArrayList<>();
        //测试项目集合
        List<String> checkItemList = Arrays.asList("BM", "BM0", "IR");
        //测试名称集合
        List<String> checkNameList = Arrays.asList("a", "b");
        //测试类型集合
        List<String> checkTypeList = Arrays.asList("D65", "F2");

        QueryWrapper<DfOrtTestItemImportConfig> configQw = new QueryWrapper<>();
        configQw
                .eq("process", process)
                .in("check_item", checkItemList)
                .orderByAsc("check_item", "check_name");

        List<DfOrtTestItemImportConfig> configList = dfOrtTestItemImportConfigService.list(configQw);
        if (CollectionUtils.isEmpty(configList)){
            return new Result(500, "当前条件下没有相关测试项配置");
        }

        //标准
        QueryWrapper<DfOrtStandardConfig> standardConfigQw = new QueryWrapper<>();
        standardConfigQw
                .eq("project", project)
                .eq("color", color)
                .eq( "stage", stage)
                .eq("process", process)
                .in("check_item", checkItemList)
                .orderByAsc("check_item", "check_name");
        List<DfOrtStandardConfig> standardConfigList = dfOrtStandardConfigService.list(standardConfigQw);

        if (CollectionUtils.isEmpty(standardConfigList)){
            return new Result(500, "当前条件下没有相关标准数据");
        }

        // 需要查询的sql集合
        List<String> selectSqlNameList = new ArrayList<>();
        for (DfOrtTestItemImportConfig dfOrtTestItemImportConfig : configList) {
            // 测试名称
            String checkName = dfOrtTestItemImportConfig.getCheckName();
            //测试值字段
            String checkCode = dfOrtTestItemImportConfig.getCheckCode();

            String selectSqlName = "JSON_ARRAYAGG("+ checkCode +") `" + checkName + "`";

            if (selectSqlNameList.contains(selectSqlName)){
                continue;
            }

            selectSqlNameList.add(selectSqlName);
        }

        Map<String,Object> configMap = new HashMap<>();
        List<String> checkItemTypeList = new ArrayList<>();
        for (DfOrtStandardConfig standardConfigData : standardConfigList) {
            String checkItem = standardConfigData.getCheckItem();
            String[] nameArray = standardConfigData.getCheckName().split("_");
            String checkName = nameArray[0];
            String checkType = nameArray[1];
            Double min = standardConfigData.getStandardMin();
            Double max = standardConfigData.getStandardMax();

            String checkItemType = checkItem + "-" + checkType;
            if (!configMap.containsKey(checkItemType)){
                Map<String,Object> checkItemTypeMap = new HashMap<>();

                checkItemTypeMap.put("okNum", 0);
                checkItemTypeMap.put("ngNum", 0);

                Map<String,Object> checkNameMap = new HashMap<>();
                checkNameMap.put("name", checkName);
                checkNameMap.put("lsl", min);
                checkNameMap.put("usl", max);
                checkNameMap.put("dataList", new ArrayList<Double>());

                checkItemTypeMap.put(checkName, checkNameMap);
                configMap.put(checkItemType, checkItemTypeMap);
                checkItemTypeList.add(checkItemType);
            }else {
                Map<String,Object> checkItemTypeMap = (Map<String, Object>) configMap.get(checkItemType);
                Map<String, Object> checkNameMap = new HashMap<>();
                checkNameMap.put("name", checkName);
                checkNameMap.put("usl", max);
                checkNameMap.put("lsl", min);
                checkNameMap.put("dataList", new ArrayList<Double>());
                checkItemTypeMap.put(checkName, checkNameMap);
            }
        }

        //sql参数
        Map<String, Object> sqlParamMap = new HashMap<>();
        //需要查询的sql
        sqlParamMap.put("rowSql", "check_date");
        selectSqlNameList.add(0, "check_item, batch");
        String selectSql = String.join(",", selectSqlNameList);
        sqlParamMap.put("selectSql", selectSql);
        sqlParamMap.put("numSql", 7);
        sqlParamMap.put("groupSql", "check_item, batch");
        sqlParamMap.put("orderSql", "check_item asc, batch asc");

        QueryWrapper<DfOrtTestData> qw = new QueryWrapper<>();
        qw
                .eq("factory", factory)
                .eq("project", project)
                .eq( "color", color)
                .eq("stage", stage)
                .eq("process", process)
                .in("check_item", checkItemList)
                .between("check_time", startTime, endTime);

        //查询结果
        List<Map<String,String>> list = dfOrtTestDataService.getBatchArrayDataList(qw,sqlParamMap);

        if(CollectionUtils.isEmpty(list)){
            return new Result(500, "当前条件下没有相关数据");
        }

        Map<String,Object> dataMap = new HashMap<>();
        for (Map<String, String> map : list) {
            String checkItem = map.get("check_item");
            String batch = map.get("batch");

            if (!dataMap.containsKey(checkItem)) {
                Map<String, Object> checkItemMap = new HashMap<>();
                List<String> batchList = new ArrayList<>();
                Map<String, Object> batchMap = new HashMap<>();

                for (String checkType : checkTypeList){
                    Map<String, Object> checkTypeMap = new HashMap<>();

                    for (String checkName : checkNameList) {
                        List<Double> dataList = JsonUtil.toObject(map.get(checkName + "_" + checkType), new TypeToken<List<Double>>() {});
                        checkTypeMap.put(checkName, dataList);
                    }
                    batchMap.put(checkType, checkTypeMap);
                }

                batchList.add(batch);
                checkItemMap.put(batch, batchMap);
                checkItemMap.put("batchList", batchList);
                checkItemMap.put("okNum", 0);
                checkItemMap.put("ngNum", 0);
                dataMap.put(checkItem, checkItemMap);
            } else {
                Map<String, Object> checkItemMap = (Map<String, Object>) dataMap.get(checkItem);
                List<String> batchList = (List<String>) checkItemMap.get("batchList");
                if (!checkItemMap.containsKey(batch)) {
                    Map<String, Object> batchMap = new HashMap<>();

                    for (String checkType : checkTypeList){
                        Map<String, Object> checkTypeMap = new HashMap<>();

                        for (String checkName : checkNameList) {
                            List<Double> dataList = JsonUtil.toObject(map.get(checkName + "_" + checkType), new TypeToken<List<Double>>() {});
                            checkTypeMap.put(checkName, dataList);
                        }
                        batchMap.put(checkType, checkTypeMap);
                    }

                    batchList.add(batch);
                    checkItemMap.put(batch, batchMap);
                } else {
                    Map<String, Object> batchMap = (Map<String, Object>) checkItemMap.get(batch);

                    for (String checkType : checkTypeList){
                        Map<String, Object> checkTypeMap = new HashMap<>();

                        for (String checkName : checkNameList) {
                            List<Double> dataList = JsonUtil.toObject(map.get(checkName + "_" + checkType), new TypeToken<List<Double>>() {});
                            checkTypeMap.put(checkName, dataList);
                        }
                        batchMap.put(checkType, checkTypeMap);
                    }
                }
            }
        }

        //总ok数
        Integer allOkNum = 0;
        //总ng数
        Integer allNgNum = 0;

        for (Map.Entry<String, Object> entry : dataMap.entrySet()) {
            String checkItem = entry.getKey();
            Map<String, Object> checkItemMap = (Map<String, Object>) entry.getValue();
            List<String> batchList = (List<String>) checkItemMap.get("batchList");

            for (String batch : batchList) {
                Map<String, Object> batchMap = (Map<String, Object>) checkItemMap.get(batch);
                //测试项目批次是否合格
                boolean checkItemBatchFlag = true;

                for(String checkType : checkTypeList){
                    //测试项目类型
                    String checkItemType = checkItem + "-" + checkType;
                    //测试项目类型对象
                    Map<String,Object> checkItemTypeData = (Map<String, Object>) configMap.get(checkItemType);
                    //测试类型批次是否
                    Boolean checkTypeBatchFlag = true;
                    //测试类型数据
                    Map<String, Object> checkTypeMap = (Map<String, Object>) batchMap.get(checkType);

                    if (CollectionUtils.isEmpty(checkTypeMap)){
                        continue;
                    }

                    for (String checkName : checkNameList){
                        //测试名称map
                        Map<String,Object> checkNameMap = (Map<String,Object>) checkItemTypeData.get(checkName);
                        Double lsl = (Double) checkNameMap.get("lsl");
                        Double usl = (Double) checkNameMap.get("usl");
                        //测试名称数据
                        List<Double> checkNameDataList = (List<Double>) checkNameMap.get("dataList");
                        List<Double> dataList = (List<Double>) checkTypeMap.get(checkName);
                        if (!CollectionUtils.isEmpty(dataList)){
                            checkNameDataList.addAll(dataList);
                        }

                        //测试类型批次不合格，则表示这一批不合格，不需要再判断后续的测试类型
                        if (!checkTypeBatchFlag){
                            break;
                        }
                        for (Double data : dataList){

                            if (lsl != null && data < lsl){
                                checkItemBatchFlag = false;
                                checkTypeBatchFlag = false;
                                break;
                            }

                            if (usl != null || data > usl){
                                checkItemBatchFlag = false;
                                checkTypeBatchFlag = false;
                                break;
                            }
                        }
                    }

                    //更新测试类型批次的ok数和ng数
                    if (checkTypeBatchFlag){
                        checkItemTypeData.put("okNum", (Integer) checkItemTypeData.get("okNum") + 1);
                    }else {
                        checkItemTypeData.put("ngNum", (Integer) checkItemTypeData.get("ngNum") + 1);
                    }
                }

                //更新测试类型批次的ok数和ng数
                if (checkItemBatchFlag){
                    allOkNum += 1;
                }else {
                    allNgNum += 1;
                }
            }
        }

        for (String checkItemType : checkItemTypeList){
            Map<String, Object> resultMap = new HashMap<>();
            List<Object> checkNameDataList = new ArrayList<Object>();

            Map<String, Object> checkItemTypeData = (Map<String, Object>) configMap.get(checkItemType);
            Integer okNum = (Integer) checkItemTypeData.get("okNum");
            Integer ngNum = (Integer) checkItemTypeData.get("ngNum");
            Integer totalNum = okNum + ngNum;
            Double okRate = totalNum == 0 ? 0 : MathUtils.round(okNum * 100.0 / totalNum, 2);

            for (String checkName : checkNameList){
                Map<String, Object> checkNameMap = (Map<String, Object>) checkItemTypeData.get(checkName);
                Double lsl = ( Double) checkNameMap.get("lsl");
                Double usl = ( Double) checkNameMap.get("usl");
                List<Double> dataList = (List<Double>) checkNameMap.get("dataList");
                // 均值
                Double mean = MathUtils.round(MathUtils.calculateMean(dataList), 3);
                // 方差
                Double variance = MathUtils.round(MathUtils.calculateVariance(dataList, mean), 3);
                // 标准差
                Double stdDev = MathUtils.round(MathUtils.calculateStandardDeviation(variance), 3);
                // CPK
                Double cpk = MathUtils.round(MathUtils.calculateCPK(usl, lsl, mean, stdDev), 3);

                Map<String, Object> checkNameData = new HashMap<>();
                checkNameData.put("name", checkName);
                checkNameData.put("cpk", cpk);
                checkNameDataList.add(checkNameData);
            }

            resultMap.put("name", checkItemType);
            resultMap.put("checkNameDataList", checkNameDataList);
            resultMap.put("content", StringUtil.formatFT(ngNum, okNum));
            resultMap.put("okRate", okRate);
            result.add(resultMap);
        }

        Map<String, Object> allData = new HashMap<>();
        allData.put("name", "总良率");
        allData.put("content", StringUtil.formatFT(allNgNum, allOkNum));
        allData.put("okRate", MathUtils.round(allOkNum * 100.0 / (allNgNum + allOkNum),2));
        result.add(allData);
        return new Result(200, "查询成功", result);
    }


    @ApiOperation("结果汇总明细")
    @GetMapping("/resultStatisticsDetail")
    public Result resultStatisticsDetail(
            @ApiParam("工厂")@RequestParam(required = false) String factory
            , @ApiParam("型号")@RequestParam(required = false) String project
            , @ApiParam("颜色")@RequestParam(required = false) String color
            , @ApiParam("阶段")@RequestParam(required = false) String stage
            , @ApiParam("工序")@RequestParam(required = false) String process
            , @ApiParam("开始时间")@RequestParam String startDate
            , @ApiParam("结束时间")@RequestParam String endDate
    ) {
        String startTime = startDate + " 00:00:00";
        String endTime = endDate + " 23:59:59";

        //结果
        List<Map<String, Object>> result = new ArrayList<>();
        //测试项目集合
        List<String> checkItemList = Arrays.asList("BM", "BM0", "IR");
        //测试名称集合
        List<String> checkNameList = Arrays.asList("a", "b");
        //测试类型集合
        List<String> checkTypeList = Arrays.asList("D65", "F2");
        //班次
        List<String> dayOrNightList = Arrays.asList("A", "B");

        QueryWrapper<DfOrtTestItemImportConfig> configQw = new QueryWrapper<>();
        configQw
                .eq("process", process)
                .in("check_item", checkItemList)
                .orderByAsc("check_item", "check_name");

        List<DfOrtTestItemImportConfig> configList = dfOrtTestItemImportConfigService.list(configQw);
        if (CollectionUtils.isEmpty(configList)){
            return new Result(500, "当前条件下没有相关测试项配置");
        }

        //标准
        QueryWrapper<DfOrtStandardConfig> standardConfigQw = new QueryWrapper<>();
        standardConfigQw
                .eq("project", project)
                .eq("color", color)
                .eq( "stage", stage)
                .eq("process", process)
                .in("check_item", checkItemList)
                .orderByAsc("check_item", "check_name");
        List<DfOrtStandardConfig> standardConfigList = dfOrtStandardConfigService.list(standardConfigQw);

        if (CollectionUtils.isEmpty(standardConfigList)){
            return new Result(500, "当前条件下没有相关标准数据");
        }

        // 需要查询的sql集合
        List<String> selectSqlNameList = new ArrayList<>();
        for (DfOrtTestItemImportConfig dfOrtTestItemImportConfig : configList) {
            // 测试名称
            String checkName = dfOrtTestItemImportConfig.getCheckName();
            //测试值字段
            String checkCode = dfOrtTestItemImportConfig.getCheckCode();

            String selectSqlName = checkCode + " `" + checkName + "`";

            if (selectSqlNameList.contains(selectSqlName)){
                continue;
            }

            selectSqlNameList.add(selectSqlName);
        }

        Map<String,Object> configMap = new HashMap<>();
        List<String> checkItemTypeList = new ArrayList<>();
        for (DfOrtStandardConfig standardConfigData : standardConfigList) {
            String checkItem = standardConfigData.getCheckItem();
            String[] nameArray = standardConfigData.getCheckName().split("_");
            String checkName = nameArray[0];
            String checkType = nameArray[1];
            Double min = standardConfigData.getStandardMin();
            Double max = standardConfigData.getStandardMax();

            String checkItemType = checkItem + "-" + checkType;
            if (!configMap.containsKey(checkItemType)){
                Map<String,Object> checkItemTypeMap = new HashMap<>();

                for (String dayOrNight : dayOrNightList){
                    Map<String, Object> dayOrNightMap = new HashMap<>();
                    dayOrNightMap.put("okNum", 0);
                    dayOrNightMap.put("ngNum", 0);
                    checkItemTypeMap.put(dayOrNight, dayOrNightMap);
                }

                Map<String, Object> checkNameMap = new HashMap<>();
                checkNameMap.put("name", checkName);
                checkNameMap.put("usl",  max);
                checkNameMap.put("lsl",  min);

                checkItemTypeMap.put(checkName, checkNameMap);
                configMap.put(checkItemType, checkItemTypeMap);
                checkItemTypeList.add(checkItemType);
            }else {
                Map<String,Object> checkItemTypeMap = (Map<String, Object>) configMap.get(checkItemType);
                Map<String, Object> checkNameMap = new HashMap<>();
                checkNameMap.put("name", checkName);
                checkNameMap.put("usl",  max);
                checkNameMap.put("lsl",  min);
                checkItemTypeMap.put(checkName, checkNameMap);
            }
        }

        //sql参数
        Map<String, Object> sqlParamMap = new HashMap<>();
        //需要查询的sql
        sqlParamMap.put("rowSql", "check_date");
        selectSqlNameList.add(0, "check_item, day_or_night");
        String selectSql = String.join(",", selectSqlNameList);
        sqlParamMap.put("selectSql", selectSql);
        sqlParamMap.put("numSql", 7);
        sqlParamMap.put("orderSql", "check_item asc, day_or_night asc");

        QueryWrapper<DfOrtTestData> qw = new QueryWrapper<>();
        qw
                .eq("factory", factory)
                .eq("project", project)
                .eq( "color", color)
                .eq("stage", stage)
                .eq("process", process)
                .in("check_item", checkItemList)
                .between("check_time", startTime, endTime);

        //查询结果
        List<Map<String,String>> list = dfOrtTestDataService.getBatchDataList(qw,sqlParamMap);

        if(CollectionUtils.isEmpty(list)){
            return new Result(500, "当前条件下没有相关数据");
        }

        for (Map<String, String> map : list){
            String checkItem = map.get("check_item");
            String dayOrNight = map.get("day_or_night");

            for (String checkType: checkTypeList){
                String checkItemType = checkItem + "-" + checkType;

                //测试项目类型
                Map<String,Object> checkItemTypeMap = (Map<String, Object>) configMap.get(checkItemType);
                //班次OK和NG数
                Map<String,Integer> dayOrNightMap  = (Map<String,Integer>) checkItemTypeMap.get(dayOrNight);
                //样品是否合格
                boolean sampleFlag = true;

                for (String checkName: checkNameList){
                    Map<String, Object> checkNameMap = (Map<String, Object>) checkItemTypeMap.get(checkName);
                    Double usl = (Double) checkNameMap.get("usl");
                    Double lsl = (Double) checkNameMap.get("lsl");

                    Double checkValue = Double.valueOf(String.valueOf(map.get(checkName + "_" + checkType)));
                    if (checkValue < lsl || checkValue > usl){
                        sampleFlag = false;
                        break;
                    }
                }

                if (sampleFlag){
                    dayOrNightMap.put("okNum", dayOrNightMap.get("okNum") + 1);
                }else {
                    dayOrNightMap.put("ngNum", dayOrNightMap.get("ngNum") + 1);
                }
            }
        }

        for (String checkItemType : checkItemTypeList){
            Map<String,Object> checkItemData = new HashMap<>();

            Map<String,Object> checkItemTypeMap = (Map<String, Object>) configMap.get(checkItemType);
            List<Map<String,Object>> dayOrNightDataList = new ArrayList<>();

            for (String dayOrNight : dayOrNightList){
                Map<String,Integer> dayOrNightMap  = (Map<String,Integer>) checkItemTypeMap.get(dayOrNight);
                Integer okNum = dayOrNightMap.get("okNum");
                Integer ngNum = dayOrNightMap.get("ngNum");
                Integer totalNum = okNum + ngNum;
                Double okRate = totalNum == 0 ? 0 : MathUtils.round(okNum * 100.0 / totalNum,2);

                Map<String,Object> dayOrNightData = new HashMap<>();
                dayOrNightData.put("dayOrNight", dayOrNight);
                dayOrNightData.put("okNum", okNum);
                dayOrNightData.put("ngNum", ngNum);
                dayOrNightData.put("totalNum", totalNum);
                dayOrNightData.put("okRate", okRate);
                dayOrNightDataList.add(dayOrNightData);
            }

            checkItemData.put("name", checkItemType);
            checkItemData.put("dayOrNightDataList", dayOrNightDataList);
            result.add(checkItemData);
        }
        return new Result(200, "查询成功", result);
    }


    @ApiOperation("班次良率")
    @GetMapping("/dayOrNightOkRate")
    public Result dayOrNightOkRate(
            @ApiParam("工厂")@RequestParam(required = false) String factory
            , @ApiParam("型号")@RequestParam(required = false) String project
            , @ApiParam("颜色")@RequestParam(required = false) String color
            , @ApiParam("阶段")@RequestParam(required = false) String stage
            , @ApiParam("工序")@RequestParam(required = false) String process
            , @ApiParam("测试项目")@RequestParam(required = false) String checkItem
            , @ApiParam("开始时间")@RequestParam String startDate
            , @ApiParam("结束时间")@RequestParam String endDate
            , @ApiParam("测试类型")@RequestParam( required = false) String checkType
    ) {
        String startTime = startDate + " 00:00:00";
        String endTime = endDate + " 23:59:59";

        List<String> dayOrNightList = Arrays.asList("A", "B");

        //结果
        Map<String, Object> result = new HashMap<>();
        //日期
        List<String> dateList = new ArrayList<>();
        //A班抽检数
        List<Integer> aNumList = new ArrayList<>();
        //B班抽检数
        List<Integer> bNumList = new ArrayList<>();
        //良率
        List<Map<String,Object>> okRateMapList = new ArrayList<>();

        QueryWrapper<DfOrtTestItemImportConfig> configQw = new QueryWrapper<>();
        configQw
                .eq("process", process)
                .eq("check_item", checkItem)
                .like("check_name",checkType)
                .orderByAsc("check_name");

        List<DfOrtTestItemImportConfig> configList = dfOrtTestItemImportConfigService.list(configQw);
        if (CollectionUtils.isEmpty(configList)){
            return new Result(500, "当前条件下没有相关测试项配置");
        }

        //标准
        QueryWrapper<DfOrtStandardConfig> standardConfigQw = new QueryWrapper<>();
        standardConfigQw
                .eq("project", project)
                .eq("color", color)
                .eq( "stage", stage)
                .eq("process", process)
                .eq( "check_item", checkItem)
                .like("check_name", checkType)
                .orderByAsc("check_name");
        List<DfOrtStandardConfig> standardConfigList = dfOrtStandardConfigService.list(standardConfigQw);

        if (CollectionUtils.isEmpty(standardConfigList)){
            return new Result(500, "当前条件下没有相关标准数据");
        }

        // 需要查询的sql集合
        List<String> selectSqlNameList = new ArrayList<>();
        for (DfOrtTestItemImportConfig dfOrtTestItemImportConfig : configList) {
            //测试值字段
            String checkCode = dfOrtTestItemImportConfig.getCheckCode();
            String checkNameType = dfOrtTestItemImportConfig.getCheckName();
            // 测试名称
            String checkName = checkNameType.split("_")[0];

            String selectSqlName = checkCode + " `" + checkName + "`";
            selectSqlNameList.add(selectSqlName);
        }

        Map<String,Object> configMap = new HashMap<>();
        List<String> checkNameList = new ArrayList<>();
        for (DfOrtStandardConfig standardConfigData : standardConfigList) {
            String checkNameType = standardConfigData.getCheckName();
            String checkName = checkNameType.split("_")[0];
            Double min = standardConfigData.getStandardMin();
            Double max = standardConfigData.getStandardMax();

            Map<String,Object> checkNameData = new HashMap<>();
            checkNameData.put("name", checkName);
            checkNameData.put("lsl", min);
            checkNameData.put("usl", max);
            checkNameData.put("dataList", new ArrayList<Double>());
            configMap.put(checkName, checkNameData);
            checkNameList.add(checkName);
        }

        //sql参数
        Map<String, Object> sqlParamMap = new HashMap<>();
        //需要查询的sql
        sqlParamMap.put("rowSql", "check_date");
        selectSqlNameList.add(0, "check_date, day_or_night");
        String selectSql = String.join(",", selectSqlNameList);
        sqlParamMap.put("selectSql", selectSql);
        sqlParamMap.put("numSql", 7);
        sqlParamMap.put("orderSql", "check_date, day_or_night ");

        QueryWrapper<DfOrtTestData> qw = new QueryWrapper<>();
        qw
                .eq("factory", factory)
                .eq("project", project)
                .eq( "color", color)
                .eq("stage", stage)
                .eq("process", process)
                .between("check_time", startTime, endTime);

        //查询结果
        List<Map<String,String>> list = dfOrtTestDataService.getBatchDataList(qw,sqlParamMap);

        if(CollectionUtils.isEmpty(list)){
            return new Result(500, "当前条件下没有相关数据");
        }

        //结果map
        Map<String, Object> resultMap = new HashMap<>();

        for (Map<String, String> map : list) {
            String checkDate = map.get("check_date");
            String dayOrNight = map.get("day_or_night");
            boolean flag = true;

            //判断该日期是否存在
            if (!resultMap.containsKey(checkDate)) {
                Map<String, Object> checkDateMap = new HashMap<>();

                checkDateMap.put("A", 0);
                checkDateMap.put("B", 0);

                for (String checkName : checkNameList){
                    checkDateMap.put(checkName, 0);
                }

                checkDateMap.put("okNum", 0);
                checkDateMap.put("allNum", 0);
                resultMap.put(checkDate, checkDateMap);
                dateList.add(checkDate);
            }

            Map<String, Object> checkDateMap = (Map<String, Object>) resultMap.get(checkDate);
            Integer allNum = (Integer) checkDateMap.get("allNum");
            Integer okNum = (Integer) checkDateMap.get("okNum");
            Integer abNum = (Integer) checkDateMap.get(dayOrNight);

            for (String checkName : checkNameList){
                Map<String, Object> checkNameMap = (Map<String, Object>) configMap.get(checkName);
                Double usl = (Double) checkNameMap.get("usl");

                Integer checkNameOkNum = (Integer) checkDateMap.get(checkName);

                Double checkValue = Double.valueOf(String.valueOf(map.get(checkName)));

                if (checkValue > usl) {
                    flag = false;
                }else {
                    checkDateMap.put(checkName, checkNameOkNum + 1);
                }
            }

            checkDateMap.put("allNum", allNum + 1);
            if (flag) {
                checkDateMap.put("okNum", okNum + 1);
            }
            checkDateMap.put(dayOrNight, abNum + 1);
        }

        //总良率
        List<Double> allOKRateList = new ArrayList<>();

        for (String checkDate : dateList){
            Map<String, Object> checkDateMap = (Map<String, Object>) resultMap.get(checkDate);
            Integer okNum = (Integer) checkDateMap.get("okNum");
            Integer allNum = (Integer) checkDateMap.get("allNum");

            for (String checkName : checkNameList){
                Integer checkNameOkNum = (Integer) checkDateMap.get(checkName);
                Map<String, Object> okRateMap = (Map<String, Object>) configMap.get(checkName);

                Double okRate = allNum == 0 ? 0 : MathUtils.round(checkNameOkNum * 100.0 / allNum, 2);

                List<Double> dataList = (List<Double>) okRateMap.get("dataList");
                dataList.add(okRate);
            }

            Double checkDataOkRate = allNum == 0 ? 0 : MathUtils.round(okNum * 100.0 / allNum, 2);
            allOKRateList.add(checkDataOkRate);

            for (String dayOrNight : dayOrNightList){
                Integer abNum = (Integer) checkDateMap.get(dayOrNight);

                if ("A".equals(dayOrNight)){
                    aNumList.add(abNum);
                }else {
                    bNumList.add(abNum);
                }
            }
        }

        for (String checkName : checkNameList){
            Map<String, Object> okRateMap = (Map<String, Object>) configMap.get(checkName);
            okRateMapList.add(okRateMap);
        }

        Map<String, Object> allOkRateMap = new HashMap<>();
        allOkRateMap.put("name", "总良率");
        allOkRateMap.put("dataList", allOKRateList);
        okRateMapList.add(allOkRateMap);

        result.put("dateList", dateList);
        result.put("okRateMapList", okRateMapList);
        result.put("aNumList", aNumList);
        result.put("bNumList", bNumList);
        return new Result(200, "查询成功", result);
    }


    /**
     * 获取箱线图（点位）
     * @return
     * @throws IOException
     */
    @GetMapping(value = "/getBoxplotPoint")
    @ApiOperation("获取箱线图（点位）")
    public Result getBoxplotPoint(
            @ApiParam("工厂")@RequestParam(required = false) String factory
            , @ApiParam("型号")@RequestParam(required = false) String project
            , @ApiParam("颜色")@RequestParam(required = false) String color
            , @ApiParam("阶段")@RequestParam(required = false) String stage
            , @ApiParam("工序")@RequestParam(required = false) String process
            , @ApiParam("测试项目")@RequestParam(required = false) String checkItem
            , @ApiParam("开始时间")@RequestParam String startDate
            , @ApiParam("结束时间")@RequestParam String endDate
    ) throws IOException {

        String startTime = startDate + " 00:00:00";
        String endTime = endDate + " 23:59:59";

        //结果
        List<Map<String, Object>> result = new ArrayList<>();

        QueryWrapper<DfOrtTestItemImportConfig> configQw = new QueryWrapper<>();
        configQw
                .eq("process", process)
                .eq("check_item", checkItem)
                .orderByAsc("check_name");

        List<DfOrtTestItemImportConfig> configList = dfOrtTestItemImportConfigService.list(configQw);
        if (CollectionUtils.isEmpty(configList)){
            return new Result(500, "当前条件下没有相关测试项配置");
        }

        //标准
        QueryWrapper<DfOrtStandardConfig> standardConfigQw = new QueryWrapper<>();
        standardConfigQw
                .eq("project", project)
                .eq("color", color)
                .eq( "stage", stage)
                .eq("process", process)
                .eq( "check_item", checkItem)
                .orderByAsc("check_name");
        List<DfOrtStandardConfig> standardConfigList = dfOrtStandardConfigService.list(standardConfigQw);

        if (CollectionUtils.isEmpty(standardConfigList)){
            return new Result(500, "当前条件下没有相关标准数据");
        }

        // 需要查询的sql集合
        List<String> selectSqlNameList = new ArrayList<>();
        for (DfOrtTestItemImportConfig dfOrtTestItemImportConfig : configList) {
            String checkName = dfOrtTestItemImportConfig.getCheckName();
            String checkCode = dfOrtTestItemImportConfig.getCheckCode();

            String selectSqlName = "JSON_ARRAYAGG("+ checkCode +") `" + checkName + "`";
            selectSqlNameList.add(selectSqlName);
        }

        Map<String,Object> configMap = new HashMap<>();
        List<String> checkNameList = new ArrayList<>();
        for (DfOrtStandardConfig standardConfigData : standardConfigList) {
            String checkNameType = standardConfigData.getCheckName();
            String checkName = checkNameType.split("_")[0];
            Double min = standardConfigData.getStandardMin();
            Double max = standardConfigData.getStandardMax();

            Map<String,Object> checkNameData = new HashMap<>();
            checkNameData.put("name", checkName);
            checkNameData.put("lsl", min);
            checkNameData.put("usl", max);
            checkNameData.put("nameList", new ArrayList<String>());
            checkNameData.put("dataList", new ArrayList<List<Double>>());
            checkNameData.put("avgList", new ArrayList<Double>());
            configMap.put(checkName, checkNameData);
            checkNameList.add(checkName);
        }

        //sql参数
        Map<String, Object> sqlParamMap = new HashMap<>();
        sqlParamMap.put("groupSql", "name");
        sqlParamMap.put("orderSql", "name asc");

        //查询结果
        List<Map<String,String>> list = null;

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
            selectSqlNameList.add(0, "batch name");
            String selectSql = String.join(",", selectSqlNameList);
            sqlParamMap.put("selectSql", selectSql);
            sqlParamMap.put("numSql", 10);

            list = dfOrtTestDataService.getBatchArrayDataList(qw, sqlParamMap);
        }else {
            //需要查询的sql
            sqlParamMap.put("rowSql", "check_date");
            selectSqlNameList.add(0, "check_date name");
            String selectSql = String.join(",", selectSqlNameList);
            sqlParamMap.put("selectSql", selectSql);
            sqlParamMap.put("numSql", 7);

            list = dfOrtTestDataService.getBatchArrayDataList(qw, sqlParamMap);
        }

        if(CollectionUtils.isEmpty(list)){
            return new Result(500, "当前条件下没有相关数据");
        }

        for (String checkName : checkNameList){
            Map<String, Object> checkNameData = (Map<String, Object>) configMap.get(checkName);

            for (Map<String, String> map : list) {
                String name = map.get("name");

                List<String> nameList = (List<String>) checkNameData.get("nameList");
                List<List<Double>> dataList = (List<List<Double>>) checkNameData.get("dataList");
                List<Double> avgList = (List<Double>) checkNameData.get("avgList");

                List<Double> valueList = JsonUtil.toObject(map.get(checkName), new TypeToken<List<Double>>(){});
                Double avg = valueList.stream().mapToDouble(Double::doubleValue).average().orElse(0);

                nameList.add(name);
                dataList.add(valueList);
                avgList.add(MathUtils.round(avg, 2));
            }

            result.add(checkNameData);
        }

        return new Result(200,"查询成功",result);
    }


    @ApiOperation("结果汇总（OQC）")
    @GetMapping("/resultStatisticsOQC")
    public Result resultStatisticsOQC(
            @ApiParam("工厂")@RequestParam(required = false) String factory
            , @ApiParam("型号")@RequestParam(required = false) String project
            , @ApiParam("颜色")@RequestParam(required = false) String color
            , @ApiParam("阶段")@RequestParam(required = false) String stage
            , @ApiParam("工序")@RequestParam(required = false) String process
            , @ApiParam("开始时间")@RequestParam String startDate
            , @ApiParam("结束时间")@RequestParam String endDate
    ) {
        String startTime = startDate + " 00:00:00";
        String endTime = endDate + " 23:59:59";

        //结果
        List<Map<String, Object>> result = new ArrayList<>();
        //测试项目集合
        List<String> checkItemList = new ArrayList<>(Arrays.asList("LCM 透过颜色", "BM LAB", "PVD LAB", "AR 反射颜色"));
        //测试名称集合
        List<String> checkNameList = Arrays.asList("a", "b");
        //测试类型集合
        List<String> checkTypeList = Arrays.asList("D65", "F2");
        //总ok数
        Integer allOkNum = 0;
        //总ng数
        Integer allNgNum = 0;
        //LCM测试项目
        String checkItemLCM = "LCM 透过颜色";

        QueryWrapper<DfOrtTestItemImportConfig> configQw = new QueryWrapper<>();
        configQw
                .eq("process", process)
                .in("check_item", checkItemList)
                .orderByAsc("check_item", "check_name");

        List<DfOrtTestItemImportConfig> configList = dfOrtTestItemImportConfigService.list(configQw);
        if (CollectionUtils.isEmpty(configList)){
            return new Result(500, "当前条件下没有相关测试项配置");
        }

        //标准
        QueryWrapper<DfOrtStandardConfig> standardConfigQw = new QueryWrapper<>();
        standardConfigQw
                .eq("project", project)
                .eq("color", color)
                .eq( "stage", stage)
                .eq("process", process)
                .in("check_item", checkItemList)
                .orderByAsc("check_item", "check_name");
        List<DfOrtStandardConfig> standardConfigList = dfOrtStandardConfigService.list(standardConfigQw);

        if (CollectionUtils.isEmpty(standardConfigList)){
            return new Result(500, "当前条件下没有相关标准数据");
        }

        // 需要查询的sql集合
        List<String> selectSqlNameList = new ArrayList<>();
        List<String> selectSqlNameListLCM = new ArrayList<>();
        for (DfOrtTestItemImportConfig dfOrtTestItemImportConfig : configList) {
            //测试项目
            String checkItem = dfOrtTestItemImportConfig.getCheckItem();
            // 测试名称
            String checkName = dfOrtTestItemImportConfig.getCheckName();
            //测试值字段
            String checkCode = dfOrtTestItemImportConfig.getCheckCode();

            if (checkItemLCM.equals(checkItem)){
                selectSqlNameListLCM.add("JSON_ARRAYAGG("+ checkCode +") `" + checkName + "`");
                continue;
            }

            String selectSqlName = "JSON_ARRAYAGG("+ checkCode +") `" + checkName + "`";

            if (selectSqlNameList.contains(selectSqlName)){
                continue;
            }

            selectSqlNameList.add(selectSqlName);
        }

        Map<String,Object> configMap = new HashMap<>();
        List<String> checkItemTypeList = new ArrayList<>();
        for (DfOrtStandardConfig standardConfigData : standardConfigList) {
            String checkItem = standardConfigData.getCheckItem();
            Double min = standardConfigData.getStandardMin();
            Double max = standardConfigData.getStandardMax();

            if (checkItemLCM.equals(checkItem)){
                Map<String,Object> checkNameMap = new HashMap<>();
                checkNameMap.put("name", standardConfigData.getCheckName());
                checkNameMap.put("lsl", min);
                checkNameMap.put("usl", max);
                configMap.put(checkItem, checkNameMap);
                continue;
            }

            String[] nameArray = standardConfigData.getCheckName().split("_");
            String checkName = nameArray[0];
            String checkType = nameArray[1];

            String checkItemType = checkItem + "-" + checkType;
            if (!configMap.containsKey(checkItemType)){
                Map<String,Object> checkItemTypeMap = new HashMap<>();

                checkItemTypeMap.put("okNum", 0);
                checkItemTypeMap.put("ngNum", 0);

                Map<String,Object> checkNameMap = new HashMap<>();
                checkNameMap.put("name", checkName);
                checkNameMap.put("lsl", min);
                checkNameMap.put("usl", max);
                checkNameMap.put("dataList", new ArrayList<Double>());

                checkItemTypeMap.put(checkName, checkNameMap);
                configMap.put(checkItemType, checkItemTypeMap);
                checkItemTypeList.add(checkItemType);
            }else {
                Map<String,Object> checkItemTypeMap = (Map<String, Object>) configMap.get(checkItemType);
                Map<String, Object> checkNameMap = new HashMap<>();
                checkNameMap.put("name", checkName);
                checkNameMap.put("usl", max);
                checkNameMap.put("lsl", min);
                checkNameMap.put("dataList", new ArrayList<Double>());
                checkItemTypeMap.put(checkName, checkNameMap);
            }
        }

        //sql参数
        Map<String, Object> sqlParamMap = new HashMap<>();
        //需要查询的sql
        sqlParamMap.put("rowSql", "check_date");
        selectSqlNameListLCM.add(0, "check_item, batch");
        String selectSql = String.join(",", selectSqlNameListLCM);
        sqlParamMap.put("selectSql", selectSql);
        sqlParamMap.put("numSql", 7);
        sqlParamMap.put("groupSql", "check_item, batch");
        sqlParamMap.put("orderSql", "check_item asc, batch asc");

        //LCM 透过颜色
        QueryWrapper<DfOrtTestData> qwLCM = new QueryWrapper<>();
        qwLCM
                .eq("factory", factory)
                .eq("project", project)
                .eq( "color", color)
                .eq("stage", stage)
                .eq("process", process)
                .eq("check_item", checkItemLCM)
                .between("check_time", startTime, endTime);

        //查询结果
        List<Map<String,String>> listLCM = dfOrtTestDataService.getBatchArrayDataList(qwLCM,sqlParamMap);

        if(CollectionUtils.isEmpty(listLCM)){
            return new Result(500, "当前条件下没有相关数据");
        }

        Map<String,Object> resultMapLCM = new HashMap<>();

        Map<String,Object> checkItemMapLCM = (Map<String, Object>) configMap.get(checkItemLCM);
        String checkNameLCM = (String) checkItemMapLCM.get("name");
        Double uslLCM = (Double) checkItemMapLCM.get("usl");
        Double lslLCM = (Double) checkItemMapLCM.get("lsl");
        Integer okNumLCM = 0;
        Integer ngNumLCM = 0;

        List<Double> dataListLCM = new ArrayList<>();

        for (Map<String, String> map : listLCM){
            List<Double> dataList = JsonUtil.toObject(map.get(checkNameLCM), new TypeToken<List<Double>>() {});
            dataListLCM.addAll(dataList);
            //测试项目批次是否合格
            boolean checkItemBatchFlag = true;
            //测试类型批次是否
            Boolean checkTypeBatchFlag = true;

            for (Double data : dataList){
                if (data < lslLCM || data > uslLCM){
                    checkItemBatchFlag = false;
                    checkTypeBatchFlag = false;
                    break;
                }
            }

            //更新测试类型批次的ok数和ng数
            if (checkTypeBatchFlag){
                okNumLCM += 1;
            }else {
                ngNumLCM += 1;
            }

            //更新测试类型批次的ok数和ng数
            if (checkItemBatchFlag){
                allOkNum += 1;
            }else {
                allNgNum += 1;
            }
        }

        Integer totalNumLCM = okNumLCM + ngNumLCM;
        Double okRateLCM = totalNumLCM == 0 ? 0 : MathUtils.round(okNumLCM * 100.0 / totalNumLCM, 2);
        // 均值
        Double meanLCM = MathUtils.round(MathUtils.calculateMean(dataListLCM), 3);
        // 方差
        Double varianceLCM = MathUtils.round(MathUtils.calculateVariance(dataListLCM, meanLCM), 3);
        // 标准差
        Double stdDevLCM = MathUtils.round(MathUtils.calculateStandardDeviation(varianceLCM), 3);
        // CPK
        Double cpkLCM = MathUtils.round(MathUtils.calculateCPK(uslLCM, lslLCM, meanLCM, stdDevLCM), 3);

        List<Object> checkNameDataListLCM = new ArrayList<Object>();
        Map<String, Object> checkNameDataLCM = new HashMap<>();
        checkNameDataLCM.put("name", checkNameLCM);
        checkNameDataLCM.put("cpk", cpkLCM);
        checkNameDataListLCM.add(checkNameDataLCM);

        resultMapLCM.put("name", checkItemLCM);
        resultMapLCM.put("checkNameDataList", checkNameDataListLCM);
        resultMapLCM.put("content", StringUtil.formatFT(ngNumLCM, okNumLCM));
        resultMapLCM.put("okRate", okRateLCM);
        result.add(resultMapLCM);

        //BM、PVD、AR
        selectSqlNameList.add(0, "check_item, batch");
        selectSql = String.join(",", selectSqlNameList);
        sqlParamMap.put("selectSql", selectSql);

        checkItemList.remove(checkItemLCM);
        QueryWrapper<DfOrtTestData> qw = new QueryWrapper<>();
        qw
                .eq("factory", factory)
                .eq("project", project)
                .eq( "color", color)
                .eq("stage", stage)
                .eq("process", process)
                .in("check_item", checkItemList)
                .between("check_time", startTime, endTime);

        //查询结果
        List<Map<String,String>> list = dfOrtTestDataService.getBatchArrayDataList(qw,sqlParamMap);

        if(CollectionUtils.isEmpty(list)){
            return new Result(500, "当前条件下没有相关数据");
        }

        Map<String,Object> dataMap = new HashMap<>();
        for (Map<String, String> map : list) {
            String checkItem = map.get("check_item");
            String batch = map.get("batch");

            //判断测试项目是否存在
            if (!dataMap.containsKey(checkItem)) {
                Map<String, Object> checkItemMap = new HashMap<>();
                List<String> batchList = new ArrayList<>();
                Map<String, Object> batchMap = new HashMap<>();

                for (String checkType : checkTypeList){
                    Map<String, Object> checkTypeMap = new HashMap<>();

                    for (String checkName : checkNameList) {
                        List<Double> dataList = JsonUtil.toObject(map.get(checkName + "_" + checkType), new TypeToken<List<Double>>() {});
                        checkTypeMap.put(checkName, dataList);
                    }
                    batchMap.put(checkType, checkTypeMap);
                }

                batchList.add(batch);
                checkItemMap.put(batch, batchMap);
                checkItemMap.put("batchList", batchList);
                checkItemMap.put("okNum", 0);
                checkItemMap.put("ngNum", 0);
                dataMap.put(checkItem, checkItemMap);
            } else {
                Map<String, Object> checkItemMap = (Map<String, Object>) dataMap.get(checkItem);
                List<String> batchList = (List<String>) checkItemMap.get("batchList");
                if (!checkItemMap.containsKey(batch)) {
                    Map<String, Object> batchMap = new HashMap<>();

                    for (String checkType : checkTypeList){
                        Map<String, Object> checkTypeMap = new HashMap<>();

                        for (String checkName : checkNameList) {
                            List<Double> dataList = JsonUtil.toObject(map.get(checkName + "_" + checkType), new TypeToken<List<Double>>() {});
                            checkTypeMap.put(checkName, dataList);
                        }
                        batchMap.put(checkType, checkTypeMap);
                    }

                    batchList.add(batch);
                    checkItemMap.put(batch, batchMap);
                } else {
                    Map<String, Object> batchMap = (Map<String, Object>) checkItemMap.get(batch);

                    for (String checkType : checkTypeList){
                        Map<String, Object> checkTypeMap = new HashMap<>();

                        for (String checkName : checkNameList) {
                            List<Double> dataList = JsonUtil.toObject(map.get(checkName + "_" + checkType), new TypeToken<List<Double>>() {});
                            checkTypeMap.put(checkName, dataList);
                        }
                        batchMap.put(checkType, checkTypeMap);
                    }
                }
            }
        }

        for (Map.Entry<String, Object> entry : dataMap.entrySet()) {
            String checkItem = entry.getKey();
            Map<String, Object> checkItemMap = (Map<String, Object>) entry.getValue();
            List<String> batchList = (List<String>) checkItemMap.get("batchList");

            for (String batch : batchList) {
                Map<String, Object> batchMap = (Map<String, Object>) checkItemMap.get(batch);
                //测试项目批次是否合格
                boolean checkItemBatchFlag = true;

                for(String checkType : checkTypeList){
                    //测试项目类型
                    String checkItemType = checkItem + "-" + checkType;
                    //测试项目类型对象
                    Map<String,Object> checkItemTypeData = (Map<String, Object>) configMap.get(checkItemType);
                    //测试类型批次是否
                    Boolean checkTypeBatchFlag = true;
                    //测试类型数据
                    Map<String, Object> checkTypeMap = (Map<String, Object>) batchMap.get(checkType);

                    if (CollectionUtils.isEmpty(checkTypeMap)){
                        continue;
                    }

                    for (String checkName : checkNameList){
                        //测试名称map
                        Map<String,Object> checkNameMap = (Map<String,Object>) checkItemTypeData.get(checkName);
                        Double lsl = (Double) checkNameMap.get("lsl");
                        Double usl = (Double) checkNameMap.get("usl");
                        //测试名称数据
                        List<Double> checkNameDataList = (List<Double>) checkNameMap.get("dataList");
                        List<Double> dataList = (List<Double>) checkTypeMap.get(checkName);
                        if (!CollectionUtils.isEmpty(dataList)){
                            checkNameDataList.addAll(dataList);
                        }

                        //测试类型批次不合格，则表示这一批不合格，不需要再判断后续的测试类型
                        if (!checkTypeBatchFlag){
                            break;
                        }
                        for (Double data : dataList){
                            if (data < lsl || data > usl){
                                checkItemBatchFlag = false;
                                checkTypeBatchFlag = false;
                                break;
                            }
                        }
                    }

                    //更新测试类型批次的ok数和ng数
                    if (checkTypeBatchFlag){
                        checkItemTypeData.put("okNum", (Integer) checkItemTypeData.get("okNum") + 1);
                    }else {
                        checkItemTypeData.put("ngNum", (Integer) checkItemTypeData.get("ngNum") + 1);
                    }
                }

                //更新测试类型批次的ok数和ng数
                if (checkItemBatchFlag){
                    allOkNum += 1;
                }else {
                    allNgNum += 1;
                }
            }
        }

        for (String checkItemType : checkItemTypeList){
            Map<String, Object> resultMap = new HashMap<>();
            List<Object> checkNameDataList = new ArrayList<Object>();

            Map<String, Object> checkItemTypeData = (Map<String, Object>) configMap.get(checkItemType);
            Integer okNum = (Integer) checkItemTypeData.get("okNum");
            Integer ngNum = (Integer) checkItemTypeData.get("ngNum");
            Integer totalNum = okNum + ngNum;
            Double okRate = totalNum == 0 ? 0 : MathUtils.round(okNum * 100.0 / totalNum, 2);

            for (String checkName : checkNameList){
                Map<String, Object> checkNameMap = (Map<String, Object>) checkItemTypeData.get(checkName);
                Double lsl = ( Double) checkNameMap.get("lsl");
                Double usl = ( Double) checkNameMap.get("usl");
                List<Double> dataList = (List<Double>) checkNameMap.get("dataList");
                // 均值
                Double mean = MathUtils.round(MathUtils.calculateMean(dataList), 3);
                // 方差
                Double variance = MathUtils.round(MathUtils.calculateVariance(dataList, mean), 3);
                // 标准差
                Double stdDev = MathUtils.round(MathUtils.calculateStandardDeviation(variance), 3);
                // CPK
                Double cpk = MathUtils.round(MathUtils.calculateCPK(usl, lsl, mean, stdDev), 3);

                Map<String, Object> checkNameData = new HashMap<>();
                checkNameData.put("name", checkName);
                checkNameData.put("cpk", cpk);
                checkNameDataList.add(checkNameData);
            }

            resultMap.put("name", checkItemType);
            resultMap.put("checkNameDataList", checkNameDataList);
            resultMap.put("content", StringUtil.formatFT(ngNum, okNum));
            resultMap.put("okRate", okRate);
            result.add(resultMap);
        }

        Map<String, Object> allData = new HashMap<>();
        allData.put("name", "总良率");
        allData.put("content", StringUtil.formatFT(allNgNum, allOkNum));
        allData.put("okRate", MathUtils.round(allOkNum * 100.0 / (allNgNum + allOkNum),2));
        result.add(allData);
        return new Result(200, "查询成功", result);
    }
}
