package com.ww.ort.controller;


import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.gson.reflect.TypeToken;
import com.ww.ort.entity.*;
import com.ww.ort.service.DfOrtHazeRatioDetailService;
import com.ww.ort.service.DfOrtStandardConfigService;
import com.ww.ort.service.DfOrtTestDataService;
import com.ww.ort.service.DfOrtTestItemImportConfigService;
import com.ww.ort.utils.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import org.springframework.stereotype.Controller;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * <p>
 * 雾度测量详情 前端控制器
 * </p>
 *
 * @author zhao
 * @since 2024-12-24
 */
@Controller
@RequestMapping("/dfOrtHaze")
@ResponseBody
@CrossOrigin
@Api(tags = "ORT IR雾度")
public class DfOrtHazeController {

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

    /**
     * 获取测试名称
     * @return
     * @throws IOException
     */
    @GetMapping(value = "/getCheckNameList")
    @ApiOperation("获取测试名称")
    public Result getCheckNameTypeList(
            @ApiParam("工序")@RequestParam(required = false) String process
            , @ApiParam("测试项目")@RequestParam(required = false) String checkItem
    ) throws IOException {

        List<Object> result = new ArrayList<>();

        QueryWrapper<DfOrtTestItemImportConfig> configQw = new QueryWrapper<>();
        configQw
                .eq("process", process)
                .eq("check_item", checkItem)
                .orderByAsc("check_name");

        List<DfOrtTestItemImportConfig> configList = dfOrtTestItemImportConfigService.list(configQw);

        for (DfOrtTestItemImportConfig config : configList){
            String checkName = config.getCheckName();

            Map<String, Object> map = new HashMap<>();
            map.put("name", checkName);
            map.put("value", checkName);
            result.add(map);
        }

        return new Result(200, "获取测试名称成功", result);
    }

    /**
     * 获取箱线图
     * @return
     * @throws IOException
     */
    @GetMapping(value = "/getBoxplot")
    @ApiOperation("获取箱线图")
    public Result getBoxplot(
            @ApiParam("工厂")@RequestParam(value = "factory", required = false) String factory
            , @ApiParam("型号")@RequestParam(value = "project", required = false) String project
            , @ApiParam("颜色")@RequestParam(value = "color", required = false) String color
            , @ApiParam("阶段")@RequestParam(value = "stage", required = false) String stage
            , @ApiParam("工序")@RequestParam(value = "process", required = false) String process
            , @ApiParam("测试项目")@RequestParam(value = "checkItem", required = false) String checkItem
            , @ApiParam("测试名称")@RequestParam(value = "checkName", required = false) String checkName
            , @ApiParam("开始时间")@RequestParam String startDate
            , @ApiParam("结束时间")@RequestParam String endDate
    ) throws IOException {

        String startTime = startDate + " 00:00:00";
        String endTime = endDate + " 23:59:59";

        //结果
        Map<String, Object> result = new HashMap<>();

        QueryWrapper<DfOrtTestItemImportConfig> configQw = new QueryWrapper<>();
        configQw
                .eq("process", process)
                .eq("check_item", checkItem)
                .eq("check_name", checkName)
                .last("limit 1");

        DfOrtTestItemImportConfig configData = dfOrtTestItemImportConfigService.getOne(configQw);
        if (configData == null){
            return new Result(500, "当前条件下没有相关测试项配置");
        }

        QueryWrapper<DfOrtStandardConfig> standardConfigQw = new QueryWrapper<>();
        standardConfigQw
                .eq("project", project)
                .eq("color", color)
                .eq("stage", stage)
                .eq("process", process)
                .eq("check_item", checkItem)
                .eq("check_name", checkName)
                .last("limit 1");
        DfOrtStandardConfig standardConfigData = dfOrtStandardConfigService.getOne(standardConfigQw);
        if (standardConfigData == null){
            return new Result(500, "当前条件下没有相关测试项标准配置");
        }

        // 需要查询的sql集合
        List<String> selectSqlNameList = new ArrayList<>();
        //测试值字段
        String checkCode = configData.getCheckCode();

        String selectSqlName = "JSON_ARRAYAGG("+ checkCode +") `" + checkName + "`";
        selectSqlNameList.add(selectSqlName);

        Double min = standardConfigData.getStandardMin();
        Double max = standardConfigData.getStandardMax();

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
            return new Result(500, "当前条件下没有相关测试项数据");
        }

        List<String> nameList = new ArrayList<>();
        List<List<Double>> dataList = new ArrayList<>();
        List<Double> avgList = new ArrayList<>();

        for (Map<String, String> map : list) {
            String name = map.get("name");

            List<Double> valueList = JsonUtil.toObject(map.get(checkName), new TypeToken<List<Double>>(){});
            Double avg = valueList.stream().mapToDouble(Double::doubleValue).average().orElse(0);

            nameList.add(name);
            dataList.add(valueList);
            avgList.add(MathUtils.round(avg, 2));
        }

        result.put("name", checkName);
        result.put("lsl", min);
        result.put("usl", max);
        result.put("nameList", nameList);
        result.put("dataList", dataList);
        result.put("avgList", avgList);

        return new Result(200,"获取ORT应力箱线图（OQC）成功", result);
    }

//    /**
//     * 获取箱线图
//     * @return
//     * @throws IOException
//     */
//    @GetMapping(value = "/getBoxplot")
//    @ApiOperation("获取箱线图")
//    public Result getBoxplot(
//            @ApiParam("工厂")@RequestParam(value = "factory", required = false) String factory
//            , @ApiParam("型号")@RequestParam(value = "project", required = false) String project
//            , @ApiParam("颜色")@RequestParam(value = "color", required = false) String color
//            , @ApiParam("阶段")@RequestParam(value = "stage", required = false) String stage
//            , @ApiParam("工序")@RequestParam(value = "process", required = false) String process
//            , @ApiParam("测试项目")@RequestParam(value = "checkItem", required = false) String checkItem
//            , @ApiParam("开始时间")@RequestParam String startDate
//            , @ApiParam("结束时间")@RequestParam String endDate
//    ) throws IOException {
//
//        String startTime = startDate + " 00:00:00";
//        String endTime = endDate + " 23:59:59";
//
//        //结果
//        List<Map<String, Object>> result = new ArrayList<>();
//
//        QueryWrapper<DfOrtTestItemImportConfig> configQw = new QueryWrapper<>();
//        configQw
//                .eq("process", process)
//                .eq("check_item", checkItem)
//                .orderByAsc("check_name");
//
//        List<DfOrtTestItemImportConfig> configList = dfOrtTestItemImportConfigService.list(configQw);
//        if (CollectionUtils.isEmpty(configList)){
//            return new Result(500, "当前条件下没有相关测试项配置");
//        }
//
//        QueryWrapper<DfOrtStandardConfig> standardConfigQw = new QueryWrapper<>();
//        standardConfigQw
//                .eq("project", project)
//                .eq("color", color)
//                .eq("stage", stage)
//                .eq("process", process)
//                .eq("check_item", checkItem)
//                .orderByAsc("check_name");
//        List<DfOrtStandardConfig> standardConfigList = dfOrtStandardConfigService.list(standardConfigQw);
//        if (CollectionUtils.isEmpty(standardConfigList)){
//            return new Result(500, "当前条件下没有相关测试项标准配置");
//        }
//
//        // 需要查询的sql集合
//        List<String> selectSqlNameList = new ArrayList<>();
//        for (DfOrtTestItemImportConfig dfOrtTestItemImportConfig : configList) {
//            //测试值字段
//            String checkCode = dfOrtTestItemImportConfig.getCheckCode();
//            // 测试名称
//            String checkName = dfOrtTestItemImportConfig.getCheckName();
//
//            String selectSqlName = "JSON_ARRAYAGG("+ checkCode +") " + checkName;
//            selectSqlNameList.add(selectSqlName);
//        }
//
//        Map<String,Object> configMap = new HashMap<>();
//        List<String> checkNameList = new ArrayList<>();
//        for (DfOrtStandardConfig standardConfigData : standardConfigList) {
//            String checkName = standardConfigData.getCheckName();
//            Double min = standardConfigData.getStandardMin();
//            Double max = standardConfigData.getStandardMax();
//
//            Map<String,Object> checkNameData = new HashMap<>();
//            checkNameData.put("name", checkName);
//            checkNameData.put("lsl", min);
//            checkNameData.put("usl", max);
//            checkNameData.put("nameList", new ArrayList<String>());
//            checkNameData.put("dataList", new ArrayList<List<Double>>());
//            checkNameData.put("avgList", new ArrayList<Double>());
//            configMap.put(checkName, checkNameData);
//            checkNameList.add(checkName);
//        }
//
//        //sql参数
//        Map<String, Object> sqlParamMap = new HashMap<>();
//        sqlParamMap.put("groupSql", "name");
//        sqlParamMap.put("orderSql", "name asc");
//
//        //查询结果
//        List<Map<String,String>> list = null;
//
//        QueryWrapper<DfOrtTestData> qw = new QueryWrapper<>();
//        qw
//                .eq("factory", factory)
//                .eq("project", project)
//                .eq( "color", color)
//                .eq("stage", stage)
//                .eq("process", process)
//                .eq("check_item", checkItem)
//                .between("check_time", startTime, endTime);
//
//        if (startDate.equals(endDate)){
//            //需要查询的sql
//            sqlParamMap.put("rowSql", "batch");
//            selectSqlNameList.add(0, "batch name");
//            String selectSql = String.join(",", selectSqlNameList);
//            sqlParamMap.put("selectSql", selectSql);
//            sqlParamMap.put("numSql", 10);
//
//            list = dfOrtTestDataService.getBatchArrayDataList(qw, sqlParamMap);
//        }else {
//            //需要查询的sql
//            sqlParamMap.put("rowSql", "check_date");
//            selectSqlNameList.add(0, "check_date name");
//            String selectSql = String.join(",", selectSqlNameList);
//            sqlParamMap.put("selectSql", selectSql);
//            sqlParamMap.put("numSql", 7);
//
//            list = dfOrtTestDataService.getBatchArrayDataList(qw, sqlParamMap);
//        }
//
//        if(CollectionUtils.isEmpty(list)){
//            return new Result(500, "当前条件下没有相关测试项数据");
//        }
//
//        for (String checkName : checkNameList){
//            Map<String, Object> checkNameData = (Map<String, Object>) configMap.get(checkName);
//
//            for (Map<String, String> map : list) {
//                String name = map.get("name");
//
//                List<String> nameList = (List<String>) checkNameData.get("nameList");
//                List<List<Double>> dataList = (List<List<Double>>) checkNameData.get("dataList");
//                List<Double> avgList = (List<Double>) checkNameData.get("avgList");
//
//                List<Double> valueList = JsonUtil.toObject(map.get(checkName), new TypeToken<List<Double>>(){});
//                Double avg = valueList.stream().mapToDouble(Double::doubleValue).average().orElse(0);
//
//                nameList.add(name);
//                dataList.add(valueList);
//                avgList.add(MathUtils.round(avg, 2));
//            }
//
//            result.add(checkNameData);
//        }
//
//        return new Result(200,"获取ORT应力箱线图成功", result);
//    }

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

        QueryWrapper<DfOrtTestItemImportConfig> configQw = new QueryWrapper<>();
        configQw
                .eq("process", process)
                .eq("check_item", checkItem);

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
                .eq("check_item", checkItem)
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
            // 测试名称
            String checkName = dfOrtTestItemImportConfig.getCheckName();

            String selectSqlName = "JSON_ARRAYAGG("+ checkCode +") `" + checkName + "`";
            selectSqlNameList.add(selectSqlName);
        }


        Map<String,Object> configMap = new HashMap<>();
        List<String> checkNameList = new ArrayList<>();
        for (DfOrtStandardConfig standardConfigData : standardConfigList) {
            String checkName = standardConfigData.getCheckName();
            Double min = standardConfigData.getStandardMin();
            Double max = standardConfigData.getStandardMax();

            Map<String,Object> checkNameMap = new HashMap<>();
            checkNameMap.put("lsl", min);
            checkNameMap.put("usl", max);
            checkNameMap.put("okNum", 0);
            checkNameMap.put("ngNum", 0);
            configMap.put(checkName, checkNameMap);
            checkNameList.add(checkName);
        }

        //sql参数
        Map<String, Object> sqlParamMap = new HashMap<>();
        //需要查询的sql
        sqlParamMap.put("rowSql", "check_date");
        selectSqlNameList.add(0, "batch name");
        String selectSql = String.join(",", selectSqlNameList);
        sqlParamMap.put("selectSql", selectSql);
        sqlParamMap.put("numSql", 7);
        sqlParamMap.put("groupSql", " name ");
        sqlParamMap.put("orderSql", " name asc");

        QueryWrapper<DfOrtTestData> qw = new QueryWrapper<>();
        qw
                .eq("factory", factory)
                .eq("project", project)
                .eq( "color", color)
                .eq("stage", stage)
                .eq("process", process)
                .eq("check_item", checkItem)
                .between("check_time", startTime, endTime);

        //查询结果
        List<Map<String,String>> list = dfOrtTestDataService.getBatchArrayDataList(qw,sqlParamMap);

        if(CollectionUtils.isEmpty(list)){
            return new Result(500, "当前条件下没有相关数据");
        }

        Integer allOkNum = 0;
        Integer allNgNum = 0;

        for (Map<String, String> map : list) {
            String name = map.get("name");
            boolean flag = true;

            for (String checkName : checkNameList){
                Map<String, Object> checkNameMap = (Map<String, Object>) configMap.get(checkName);
                boolean checkNameFlag = true;

                String dataStr = map.get(checkName);
                List<Double> dataList = JsonUtil.toObject(dataStr, new TypeToken<List<Double>>() {});
                Double usl = (Double) checkNameMap.get("usl");

                for (Double data : dataList) {
                    if (data > usl) {
                        flag = false;
                        checkNameFlag = false;
                        break;
                    }
                }

                if (checkNameFlag) {
                    checkNameMap.put("okNum", (Integer) checkNameMap.get("okNum") + 1);
                } else {
                    checkNameMap.put("ngNum", (Integer) checkNameMap.get("ngNum") + 1);
                }

            }

            if (flag) {
                allOkNum = allOkNum + 1;
            } else {
                allNgNum = allNgNum + 1;
            }
        }

        for (String checkName : checkNameList){
            Map<String,Object> checkNameMap = (Map<String,Object>) configMap.get(checkName);

            Integer okNum = (Integer) checkNameMap.get("okNum");
            Integer ngNum = (Integer) checkNameMap.get("ngNum");
            Integer totalNum = okNum + ngNum;

            Double okRate = totalNum == 0 ? 0 : MathUtils.round(okNum * 100.0 / totalNum,2);

            Map<String,Object> checkNameData = new HashMap<>();
            checkNameData.put("name", checkName);
            checkNameData.put("content", StringUtil.formatFT(ngNum, okNum));
            checkNameData.put("okRate", okRate);
            result.add(checkNameData);
        }

        Map<String, Object> allData = new HashMap<>();
        allData.put("name", "总良率");
        allData.put("content", StringUtil.formatFT(allNgNum, allOkNum));
        allData.put("okRate", MathUtils.round(allOkNum * 100.0 / (allNgNum + allOkNum),2));
        result.add(allData);
        return new Result(200, "查询成功", result);
    }

    @ApiOperation("班次良率")
    @GetMapping("/dayOrNightOkRate")
    public Result dayOrNightOkRate(
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

        List<String> dayOrNightList = Arrays.asList("A", "B");

        //结果
        Map<String, Object> result = new HashMap<>();
        //日期
        List<String> dateList = new ArrayList<>();
        //A班抽检数
        List<Integer> aNumList = new ArrayList<>();
        //B班抽检数
        List<Integer> bNumList = new ArrayList<>();
        //A班良率
        List<Double> aOkRateList = new ArrayList<>();
        //B班良率
        List<Double> bOkRateList = new ArrayList<>();

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
                .eq("check_item", checkItem)
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
            // 测试名称
            String checkName = dfOrtTestItemImportConfig.getCheckName();

            String selectSqlName = checkCode + " `" + checkName + "`";
            selectSqlNameList.add(selectSqlName);
        }

        Map<String,Object> configMap = new HashMap<>();
        List<String> checkNameList = new ArrayList<>();
        for (DfOrtStandardConfig standardConfigData : standardConfigList) {
            String checkName = standardConfigData.getCheckName();
            Double min = standardConfigData.getStandardMin();
            Double max = standardConfigData.getStandardMax();

            Map<String,Object> checkNameMap = new HashMap<>();
            checkNameMap.put("name", checkName);
            checkNameMap.put("lsl", min);
            checkNameMap.put("usl", max);
            configMap.put(checkName, checkNameMap);
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
        sqlParamMap.put("groupSql", " check_date, day_or_night ");
        sqlParamMap.put("orderSql", " check_date asc, day_or_night asc");

        QueryWrapper<DfOrtTestData> qw = new QueryWrapper<>();
        qw
                .eq("factory", factory)
                .eq("project", project)
                .eq( "color", color)
                .eq("stage", stage)
                .eq("process", process)
                .eq("check_item", checkItem)
                .between("check_time", startTime, endTime);

        //查询结果
        List<Map<String,String>> list = dfOrtTestDataService.getBatchDataList(qw,sqlParamMap);

        if(CollectionUtils.isEmpty(list)){
            return new Result(500, "当前条件下没有相关数据");
        }

        Map<String, Object> resultMap = new HashMap<>();

        for (Map<String, String> map : list) {
            String checkDate = map.get("check_date");
            String dayOrNight = map.get("day_or_night");
            boolean flag = true;

            for (String checkName : checkNameList){
                Map<String, Object> checkNameMap = (Map<String, Object>) configMap.get(checkName);
                Double usl = (Double) checkNameMap.get("usl");

                Double checkValue = Double.valueOf(String.valueOf(map.get(checkName)));

                if (checkValue > usl) {
                    flag = false;
                    break;
                }
            }

            if (!resultMap.containsKey(checkDate)) {
                Map<String, Map<String, Integer>> checkDateMap = new HashMap<>();
                Map<String, Integer> aMap = new HashMap<>();
                Map<String, Integer> bMap = new HashMap<>();

                aMap.put("okNum", 0);
                aMap.put("allNum", 0);
                bMap.put("okNum", 0);
                bMap.put("allNum", 0);
                checkDateMap.put("A", aMap);
                checkDateMap.put("B", bMap);

                checkDateMap.get(dayOrNight).put("allNum", 1);
                if (flag) {
                    checkDateMap.get(dayOrNight).put("okNum", 1);
                }

                resultMap.put(checkDate, checkDateMap);
                dateList.add(checkDate);
            } else {
                Map<String, Object> checkDateMap = (Map<String, Object>) resultMap.get(checkDate);
                Map<String, Integer> abMap = (Map<String, Integer>) checkDateMap.get(dayOrNight);

                if (flag) {
                    abMap.put("okNum", abMap.get("okNum") + 1);
                }

                abMap.put("allNum", abMap.get("allNum") + 1);
            }
        }

        for (String checkDate : dateList){
            Map<String, Object> checkDateMap = (Map<String, Object>) resultMap.get(checkDate);

            for (String dayOrNight : dayOrNightList){
                Map<String, Integer> abMap = (Map<String, Integer>) checkDateMap.get(dayOrNight);
                Integer okNum = abMap.get("okNum");
                Integer allNum = abMap.get("allNum");
                Double okRate = allNum == 0 ? 0 : MathUtils.round(okNum * 100.0 / allNum, 2) ;

                if ("A".equals(dayOrNight)){
                    aNumList.add(allNum);
                    aOkRateList.add(okRate);
                }else {
                    bNumList.add(allNum);
                    bOkRateList.add(okRate);
                }
            }
        }

        result.put("dateList", dateList);
        result.put("aNumList", aNumList);
        result.put("aOkRateList", aOkRateList);
        result.put("bNumList", bNumList);
        result.put("bOkRateList", bOkRateList);
        return new Result(200, "查询成功", result);
    }

    /**
     * 获取均值分析图
     * @return
     * @throws IOException
     */
    @GetMapping(value = "/getMeanAnalysis")
    @ApiOperation("获取均值分析图")
    public Result getMeanAnalysis(
            @ApiParam("工厂")@RequestParam(value = "factory", required = false) String factory
            , @ApiParam("型号")@RequestParam(value = "project", required = false) String project
            , @ApiParam("颜色")@RequestParam(value = "color", required = false) String color
            , @ApiParam("阶段")@RequestParam(value = "stage", required = false) String stage
            , @ApiParam("工序")@RequestParam(value = "process", required = false) String process
            , @ApiParam("测试项目")@RequestParam(value = "checkItem", required = false) String checkItem
            , @ApiParam("测试名称")@RequestParam(value = "checkName", required = false) String checkName
            , @ApiParam("开始时间")@RequestParam String startDate
            , @ApiParam("结束时间")@RequestParam String endDate
    ) throws IOException {

        String redisKey = "ORT:optics:MeanAnalysis:" + factory + ":" + project + ":" + color + ":" + stage + ":" + process + ":"+ checkItem + ":" + checkName + ":" + startDate + "_" + endDate;
        if (redisUtils.hasKey(redisKey)){
            String filename = (String) redisUtils.get(redisKey);

            String data = env.getProperty("imgUrl") + "/" + filename;
            return new Result(200,"获取均值分析图成功",data);
        }

        String startTime = startDate + " 00:00:00";
        String endTime = endDate + " 23:59:59";

        QueryWrapper<DfOrtTestItemImportConfig> configQw = new QueryWrapper<>();
        configQw
                .eq("process", process)
                .eq("check_item", checkItem)
                .eq("check_name", checkName)
                .last("limit 1");

        DfOrtTestItemImportConfig configData = dfOrtTestItemImportConfigService.getOne(configQw);
        if (configData == null){
            return new Result(500, "当前条件下没有相关测试项配置");
        }

        // 需要查询的sql集合
        List<String> selectSqlNameList = new ArrayList<>();
        //测试值字段
        String checkCode = configData.getCheckCode();

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
            sqlParamMap.put("selectSql", " batch name, " + checkCode + " value");
            sqlParamMap.put("numSql", 10);

            replaceMap.put("#showX#", "batch");

            list = dfOrtTestDataService.getBatchDataList(qw, sqlParamMap);
        }else {
            //需要查询的sql
            sqlParamMap.put("rowSql", "check_date");
            sqlParamMap.put("selectSql", " check_date name, " + checkCode + " value");
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

        return new Result(200,"获取均值分析图成功",env.getProperty("imgUrl") + "/" + urlMap.get("imageName"));
    }

    /**
     * 获取方差分析图
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
            , @ApiParam("测试项")@RequestParam(value = "checkName", required = false) String checkName
            , @ApiParam("开始时间")@RequestParam String startDate
            , @ApiParam("结束时间")@RequestParam String endDate
    ) throws IOException {

        String redisKey = "ORT:Optics:VarianceAnalysis:" + factory + ":" + project + ":" + color + ":" + stage + ":" + process + ":"+ checkItem + ":" + checkName + ":" + startDate + "_" + endDate;
        if (redisUtils.hasKey(redisKey)){
            String filename = (String) redisUtils.get(redisKey);

            String data = env.getProperty("imgUrl") + "/" + filename;
            return new Result(200,"获取ORT强度方差分析图",data);
        }

        String startTime = startDate + " 00:00:00";
        String endTime = endDate + " 23:59:59";

        QueryWrapper<DfOrtTestItemImportConfig> configQw = new QueryWrapper<>();
        configQw
                .eq("process", process)
                .eq("check_item", checkItem)
                .eq("check_name", checkName)
                .last("limit 1");

        DfOrtTestItemImportConfig configData = dfOrtTestItemImportConfigService.getOne(configQw);
        if (configData == null){
            return new Result(500, "当前条件下没有相关测试项配置");
        }

        // 需要查询的sql集合
        List<String> selectSqlNameList = new ArrayList<>();
        //测试值字段
        String checkCode = configData.getCheckCode();

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
            sqlParamMap.put("selectSql", " batch name, " + checkCode + " value");
            sqlParamMap.put("numSql", 10);

            replaceMap.put("#showX#", "batch");

            list = dfOrtTestDataService.getBatchDataList(qw, sqlParamMap);
        }else {
            //需要查询的sql
            sqlParamMap.put("rowSql", "check_date");
            sqlParamMap.put("selectSql", " check_date name, " + checkCode + " value");
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

    @ApiOperation("结果汇总（OQC）")
    @GetMapping("/resultStatisticsOQC")
    public Result resultStatisticsOQC(
            @ApiParam("工厂")@RequestParam(value = "factory", required = false) String factory
            , @ApiParam("型号")@RequestParam(value = "project", required = false) String project
            , @ApiParam("颜色")@RequestParam(value = "color", required = false) String color
            , @ApiParam("阶段")@RequestParam(value = "stage", required = false) String stage
            , @ApiParam("工序")@RequestParam(value = "process", required = false) String process
            , @ApiParam("开始时间")@RequestParam String startDate
            , @ApiParam("结束时间")@RequestParam String endDate
    ) {
        String startTime = startDate + " 00:00:00";
        String endTime = endDate + " 23:59:59";

        //结果
        List<Map<String, Object>> result = new ArrayList<>();
        //测试项目集合
        List<String> checkItemList = Arrays.asList("LCM Haze", "PVD Haze");

        QueryWrapper<DfOrtTestItemImportConfig> configQw = new QueryWrapper<>();
        configQw
                .eq("process", process)
                .in("check_item", checkItemList)
                .orderByAsc("check_item, check_name");

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
                .orderByAsc("check_item, check_name");
        List<DfOrtStandardConfig> standardConfigList = dfOrtStandardConfigService.list(standardConfigQw);

        if (CollectionUtils.isEmpty(standardConfigList)){
            return new Result(500, "当前条件下没有相关标准数据");
        }

        String checkName = "Haze";
        // 需要查询的sql集合
        List<String> selectSqlNameList = new ArrayList<>();
        for (DfOrtTestItemImportConfig dfOrtTestItemImportConfig : configList) {
            // 测试名称
            checkName = dfOrtTestItemImportConfig.getCheckName();
            //测试值字段
            String checkCode = dfOrtTestItemImportConfig.getCheckCode();

            String selectSqlName = "JSON_ARRAYAGG("+ checkCode +") `" + checkName + "`";

            if(selectSqlNameList.contains(selectSqlName)){
                continue;
            }

            selectSqlNameList.add(selectSqlName);
        }


        Map<String,Object> configMap = new HashMap<>();
        for (DfOrtStandardConfig standardConfigData : standardConfigList) {
            String checkItem = standardConfigData.getCheckItem();
            Double min = standardConfigData.getStandardMin();
            Double max = standardConfigData.getStandardMax();

            Map<String,Object> checkItemMap = new HashMap<>();
            checkItemMap.put("name", checkItem);
            checkItemMap.put("lsl", min);
            checkItemMap.put("usl", max);
            checkItemMap.put("okNum", 0);
            checkItemMap.put("ngNum", 0);
            configMap.put(checkItem, checkItemMap);
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
        sqlParamMap.put("orderSql", " check_item asc, batch asc");

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

        for (Map<String, String> map : list) {
            String checkItem = map.get("check_item");

            Map<String, Object> checkItemMap = (Map<String, Object>) configMap.get(checkItem);
            boolean checkItemFlag = true;

            String dataStr = map.get(checkName);
            List<Double> dataList = JsonUtil.toObject(dataStr, new TypeToken<List<Double>>() {});
            Double usl = (Double) checkItemMap.get("usl");
            Double lsl = (Double) checkItemMap.get("lsl");

            for (Double data : dataList) {
                if (usl != null && data > usl) {
                    checkItemFlag = false;
                    break;
                }

                if (lsl != null && data < lsl) {
                    checkItemFlag = false;
                    break;
                }
            }

            if (checkItemFlag) {
                checkItemMap.put("okNum", (Integer) checkItemMap.get("okNum") + 1);
            } else {
                checkItemMap.put("ngNum", (Integer) checkItemMap.get("ngNum") + 1);
            }
        }

        for (String checkItem : checkItemList){
            Map<String,Object> checkItemMap = (Map<String,Object>) configMap.get(checkItem);

            Integer okNum = (Integer) checkItemMap.get("okNum");
            Integer ngNum = (Integer) checkItemMap.get("ngNum");
            Integer totalNum = okNum + ngNum;

            Double okRate = totalNum == 0 ? 0 : MathUtils.round(okNum * 100.0 / totalNum,2);

            Map<String,Object> checkNameData = new HashMap<>();
            checkNameData.put("name", checkItem);
            checkNameData.put("content", StringUtil.formatFT(ngNum, okNum));
            checkNameData.put("okRate", okRate);
            result.add(checkNameData);
        }
        return new Result(200, "查询结果汇总（OQC）数据成功", result);
    }
}
