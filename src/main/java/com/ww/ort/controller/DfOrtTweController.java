package com.ww.ort.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.gson.reflect.TypeToken;
import com.ww.ort.entity.DfOrtStandardConfig;
import com.ww.ort.entity.DfOrtTestData;
import com.ww.ort.entity.DfOrtTestItemImportConfig;
import com.ww.ort.service.DfOrtStandardConfigService;
import com.ww.ort.service.DfOrtTestDataService;
import com.ww.ort.service.DfOrtTestItemImportConfigService;
import com.ww.ort.utils.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.*;

/**
 * TWE
 * @author TGY
 * @date 2025-12-04
 */
@Controller
@RequestMapping("/dfOrtTwe")
@Api(tags = "ORT TWE")
@ResponseBody
@CrossOrigin
public class DfOrtTweController {

    @Autowired
    private DfOrtTestDataService dfOrtTestDataService;

    @Autowired
    private DfOrtTestItemImportConfigService dfOrtTestItemImportConfigService;

    @Autowired
    private DfOrtStandardConfigService dfOrtStandardConfigService;


    /**
     * 获取ORT应力正太分布图
     * @return
     * @throws IOException
     */
    @GetMapping(value = "/getNormalDistribution")
    @ApiOperation("获取ORT应力正太分布图")
    public Result getNormalDistribution(
            @ApiParam("工厂")@RequestParam(value = "factory", required = false) String factory
            , @ApiParam("型号")@RequestParam(value = "project", required = false) String project
            , @ApiParam("颜色")@RequestParam(value = "color", required = false) String color
            , @ApiParam("阶段")@RequestParam(value = "stage", required = false) String stage
            , @ApiParam("工序")@RequestParam(value = "process", required = false) String process
            , @ApiParam("测试项目")@RequestParam(value = "checkItem", required = false) String checkItem
            , @ApiParam("开始时间")@RequestParam String startDate
            , @ApiParam("结束时间")@RequestParam String endDate
    ) throws IOException {

        String startTime = startDate + " 00:00:00";
        String endTime = endDate + " 23:59:59";

        Map<String, Map<String, Object>> resultMap = new HashMap<>();

        QueryWrapper<DfOrtTestItemImportConfig> configQw = new QueryWrapper<>();
        configQw
                .eq("process", process)
                .eq("check_item", checkItem)
                .orderByAsc("check_name");

        List<DfOrtTestItemImportConfig> configList = dfOrtTestItemImportConfigService.list(configQw);
        if (CollectionUtils.isEmpty(configList)){
            return new Result(500, "当前条件下没有相关测试项配置");
        }

        QueryWrapper<DfOrtStandardConfig> standardConfigQw = new QueryWrapper<>();
        standardConfigQw
                .eq("project", project)
                .eq("color", color)
                .eq("stage", stage)
                .eq("process", process)
                .eq("check_item", checkItem)
                .orderByAsc("check_name");
        List<DfOrtStandardConfig> standardConfigList = dfOrtStandardConfigService.list(standardConfigQw);
        if (CollectionUtils.isEmpty(standardConfigList)){
            return new Result(500, "当前条件下没有相关测试项标准配置");
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
            Double max = standardConfigData.getStandardMax();

            Map<String,Object> checkNameMap = new HashMap<>();
            checkNameMap.put("name", checkName);
            checkNameMap.put("usl", max);
            checkNameMap.put("dataList", new ArrayList<Double>());
            configMap.put(checkName, checkNameMap);
            checkNameList.add(checkName);
        }

        //sql参数
        Map<String, Object> sqlParamMap = new HashMap<>();
        sqlParamMap.put("rowSql", "check_date");
        selectSqlNameList.add(0, "batch name");
        String selectSql = String.join(",", selectSqlNameList);
        sqlParamMap.put("selectSql", selectSql);
        sqlParamMap.put("numSql", 7);
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

        //查询结果
        List<Map<String,String>> list = dfOrtTestDataService.getBatchDataList(qw, sqlParamMap);

        if(CollectionUtils.isEmpty(list)){
            return new Result(500, "当前条件下没有相关测试项数据");
        }

        for (String checkName : checkNameList){
            Map<String,Object> checkNameData = (Map<String, Object>) configMap.get(checkName);
            Double usl = (Double) checkNameData.get("usl");
            List<Double> dataList = (List<Double>) checkNameData.get("dataList");

            for (Map<String, String> data : list) {
                Double value = Double.valueOf(String.valueOf(data.get(checkName)));
                dataList.add(value);
            }

            Map<String,Object> checkNameMap = new HashMap<>();
            checkNameMap.put("lsl", dataList.stream().min(Comparator.comparing(Double::doubleValue)).get());
            checkNameMap.put("usl", usl);
            MathUtils.normalDistribution(dataList, checkNameMap);

            resultMap.put(checkName, checkNameMap);
        }

        return new Result(200,"获取ORT应力正太分布图成功", resultMap);
    }


    /**
     * 获取ORT应力箱线图
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

        QueryWrapper<DfOrtStandardConfig> standardConfigQw = new QueryWrapper<>();
        standardConfigQw
                .eq("project", project)
                .eq("color", color)
                .eq("stage", stage)
                .eq("process", process)
                .eq("check_item", checkItem)
                .orderByAsc("check_name");
        List<DfOrtStandardConfig> standardConfigList = dfOrtStandardConfigService.list(standardConfigQw);
        if (CollectionUtils.isEmpty(standardConfigList)){
            return new Result(500, "当前条件下没有相关测试项标准配置");
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
            return new Result(500, "当前条件下没有相关测试项数据");
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

        return new Result(200,"获取ORT应力箱线图成功", result);
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
        Map<String, Object> result = new HashMap<>();

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

            String selectSqlName = "JSON_ARRAYAGG("+ checkCode +") `" + checkName + "`";
            selectSqlNameList.add(selectSqlName);
        }

        Map<String,Object> configMap = new HashMap<>();
        List<String> checkNameList = new ArrayList<>();
        for (DfOrtStandardConfig standardConfigData : standardConfigList) {
            String checkName = standardConfigData.getCheckName();
            Double max = standardConfigData.getStandardMax();
            Double B99 = standardConfigData.getValue1();
            Double B95 = standardConfigData.getValue2();

            Map<String,Object> checkNameMap = new HashMap<>();
            checkNameMap.put("name", checkName);
            checkNameMap.put("usl", max);
            checkNameMap.put("B99", B99);
            checkNameMap.put("B95", B95);
            checkNameMap.put("dataList", new ArrayList<>());
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
                Double usl = (Double) checkNameMap.get("usl");
                Double B99 = (Double) checkNameMap.get("B99");
                Double B95 = (Double) checkNameMap.get("B95");
                List<Object> dataList = (List<Object>) checkNameMap.get("dataList");

                List<Double> valueList = JsonUtil.toObject(map.get(checkName), new TypeToken<List<Double>>() {});

                Double maxValue = MathUtils.round(Collections.max(valueList), 3);
                Double B99Value = MathUtils.round(DataUtil.percentile(valueList,0.99), 3);
                Double B95Value = MathUtils.round(DataUtil.percentile(valueList,0.95), 3);

                if (maxValue > usl || B99Value > B99 || B95Value > B95) {
                    flag = false;
                    checkNameMap.put("ngNum", (Integer) checkNameMap.get("ngNum") + 1);
                }else {
                    checkNameMap.put("okNum", (Integer) checkNameMap.get("okNum") + 1);
                }

                dataList.addAll(valueList);
            }

            if (flag) {
                allOkNum = allOkNum + 1;
            } else {
                allNgNum = allNgNum + 1;
            }
        }

        List<Object> checkNameDataList = new ArrayList<>();

        for (String checkName : checkNameList){
            Map<String,Object> checkNameData = (Map<String,Object>) configMap.get(checkName);
            List<Double> dataList = (List<Double>) checkNameData.get("dataList");
            Integer okNum = (Integer) checkNameData.get("okNum");
            Integer ngNum = (Integer) checkNameData.get("ngNum");
            Integer totalNum = okNum + ngNum;

            Double max = MathUtils.round(Collections.max(dataList), 3);
            Double B99 = MathUtils.round(DataUtil.percentile(dataList,0.99), 3);
            Double B95 = MathUtils.round(DataUtil.percentile(dataList,0.95), 3);
            Double mean = MathUtils.round(MathUtils.calculateMean(dataList), 3);
            Double variance = MathUtils.round(MathUtils.calculateVariance(dataList, mean), 3);
            Double okRate = totalNum == 0 ? 0 : MathUtils.round(okNum * 100.0 / totalNum,2);

            checkNameData.put("name", checkName);
            checkNameData.put("max", max);
            checkNameData.put("B99", B99);
            checkNameData.put("B95", B95);
            checkNameData.put("mean", mean);
            checkNameData.put("variance", variance);
            checkNameData.put("content", StringUtil.formatFT(ngNum, okNum));
            checkNameData.put("okRate", okRate);
            checkNameDataList.add(checkNameData);
        }

        Integer allTotalNum = allOkNum + allNgNum;
        Double allOkRate = allTotalNum == 0 ? 0 : MathUtils.round(allOkNum * 100.0 / allTotalNum,2);
        result.put("allOkRate", allOkRate);
        result.put("dataList", checkNameDataList);
        return new Result(200, "查询成功", result);
    }
}
