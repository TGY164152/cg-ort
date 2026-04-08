package com.ww.ort.controller;


import com.alibaba.fastjson.JSON;
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
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * <p>
 * ORT应力详情 前端控制器
 * </p>
 *
 * @author zhao
 * @since 2023-06-27
 */
@Controller
@RequestMapping("/dfOrtStress")
@ResponseBody
@CrossOrigin
@Api(tags = "ORT应力详情")
public class DfOrtStressController {
    @Autowired
    private Environment env;

    @Autowired
    private DfOrtTestDataService dfOrtTestDataService;

    @Autowired
    private DfOrtTestItemImportConfigService dfOrtTestItemImportConfigService;

    @Autowired
    private DfOrtStandardConfigService dfOrtStandardConfigService;


    /**
     * 获取ORT应力箱线图
     * @return
     * @throws IOException
     */
    @GetMapping(value = "/getBoxplot")
    @ApiOperation("获取ORT应力箱线图")
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
        List<Map<String,Object>> result = new ArrayList<>();

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

//            if (checkName.contains("|") || checkName.contains("/") || checkName.contains("*")){
//                checkName = "`" + checkName + "`";
//            }

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
            checkNameMap.put("name", checkName);
            checkNameMap.put("lsl", min);
            checkNameMap.put("usl", max);
            checkNameMap.put("nameList", new ArrayList<String>());
            checkNameMap.put("dataList", new ArrayList<List<Double>>());
            configMap.put(checkName, checkNameMap);
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

        for (Map<String, String> map : list) {
            String name = map.get("name");

            for (String checkName : checkNameList){
                List<Double> valueList = JsonUtil.toObject(map.get(checkName), new TypeToken<List<Double>>(){});

                Map<String, Object> checkNameMap = (Map<String, Object>) configMap.get(checkName);
                List<String> nameList = (List<String>) checkNameMap.get("nameList");
                List<List<Double>> dataList = (List<List<Double>>) checkNameMap.get("dataList");

                nameList.add(name);
                dataList.add(valueList);
            }
        }

        for (String checkName : checkNameList){
            Map<String, Object> checkNameMap = (Map<String, Object>) configMap.get(checkName);
            result.add(checkNameMap);
        }
        return new Result(200,"获取ORT应力箱线图成功", result);
    }


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

//            if (checkName.contains("|") || checkName.contains("/") || checkName.contains("*")){
//                checkName = "`" + checkName + "`";
//            }

            String selectSqlName = checkCode + " `" + checkName + "`";
            selectSqlNameList.add(selectSqlName);
        }

        //sql参数
        Map<String, Object> sqlParamMap = new HashMap<>();
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

            list = dfOrtTestDataService.getBatchDataList(qw, sqlParamMap);
        }else {
            //需要查询的sql
            sqlParamMap.put("rowSql", "check_date");
            selectSqlNameList.add(0, "check_date name");
            String selectSql = String.join(",", selectSqlNameList);
            sqlParamMap.put("selectSql", selectSql);
            sqlParamMap.put("numSql", 7);

            list = dfOrtTestDataService.getBatchDataList(qw, sqlParamMap);
        }

        if(CollectionUtils.isEmpty(list)){
            return new Result(500, "当前条件下没有相关测试项数据");
        }


        for (DfOrtStandardConfig standardData : standardConfigList ){
            String checkName = standardData.getCheckName();
            Double min = standardData.getStandardMin();
            Double max = standardData.getStandardMax();
            List<Double> dataList = new ArrayList<>();

            for (Map<String, String> data : list) {
                Double value = Double.valueOf(String.valueOf(data.get(checkName)));
                dataList.add(value);
            }

            Map<String,Object> checkNameMap = new HashMap<>();
            checkNameMap.put("name", checkName);
            checkNameMap.put("lsl", min);
            checkNameMap.put("usl", max);
            MathUtils.normalDistribution(dataList, checkNameMap);

            result.add(checkNameMap);
        }

        return new Result(200,"获取ORT应力正太分布图成功", result);
    }

    /**
     * 获取ORT应力数据详情
     * @return
     * @throws IOException
     */
    @GetMapping(value = "/getDataDetail")
    @ApiOperation("获取ORT应力数据详情")
    public Result getDataDetail(
            @ApiParam("工厂")@RequestParam(value = "factory", required = false) String factory
            , @ApiParam("型号")@RequestParam(value = "project", required = false) String project
            , @ApiParam("颜色")@RequestParam(value = "color", required = false) String color
            , @ApiParam("阶段")@RequestParam(value = "stage", required = false) String stage
            , @ApiParam("工序")@RequestParam(value = "process", required = false) String process
            , @ApiParam("测试项目")@RequestParam(value = "checkItem", required = false) String checkItem
            , @ApiParam("开始时间")@RequestParam String startDate
            , @ApiParam("结束时间")@RequestParam String endDate
    ){
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
            String checkName = dfOrtTestItemImportConfig.getCheckName();
            String checkCode = dfOrtTestItemImportConfig.getCheckCode();
//            if (checkName.contains("|") || checkName.contains("/") || checkName.contains("*")){
//                checkName = "`" + checkName + "`";
//            }

            String selectSqlName = "JSON_ARRAYAGG("+ checkCode +") `" + checkName + "`";
            selectSqlNameList.add(selectSqlName);
        }

        Map<String, Object> configMap = new HashMap<>();
        List<String> checkNameList = new ArrayList<>();
        for (DfOrtStandardConfig standardConfigData : standardConfigList) {
            String checkName = standardConfigData.getCheckName();
            Double min = standardConfigData.getStandardMin();
            Double max = standardConfigData.getStandardMax();
            //标准值
            double standardValue = MathUtils.round(MathUtils.calculateMean(Arrays.asList(min, max)), 3) ;

            Map<String,Object> checkNameMap = new HashMap<>();
            checkNameMap.put("name", checkName);
            checkNameMap.put("nameList", new ArrayList<String>());
            checkNameMap.put("standardValue", standardValue);
            checkNameMap.put("usl", max);
            checkNameMap.put("lsl", min);
            checkNameMap.put("meanList", new ArrayList<Double>());
            checkNameMap.put("stdDevList", new ArrayList<Double>());
            checkNameMap.put("cpkList", new ArrayList<Double>());
            checkNameMap.put("okRateList", new ArrayList<Double>());
            configMap.put(checkName, checkNameMap);
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
            Map<String, Object> checkNameMap = (Map<String, Object>) configMap.get(checkName);
            Double lsl = (Double) checkNameMap.get("lsl");
            Double usl = (Double) checkNameMap.get("usl");
            List<String> nameList = (List<String>) checkNameMap.get("nameList");
            List<Double> meanList = (List<Double>) checkNameMap.get("meanList");
            List<Double> stdDevList = (List<Double>) checkNameMap.get("stdDevList");
            List<Double> cpkList = (List<Double>) checkNameMap.get("cpkList");
            List<Double> okRateList = (List<Double>) checkNameMap.get("okRateList");

            for (Map<String, String> map : list) {
                String name = map.get("name");
                //当前批次，测试名称的数据
                List<Double> dataList = JsonUtil.toObject(map.get(checkName), new TypeToken<List<Double>>() {});
                // 均值
                Double mean = MathUtils.round(MathUtils.calculateMean(dataList), 3);
                // 方差
                Double variance = MathUtils.round(MathUtils.calculateVariance(dataList, mean), 3);
                // 标准差
                Double stdDev = MathUtils.round(MathUtils.calculateStandardDeviation(variance), 3);
                // CPK
                Double cpk = MathUtils.round(MathUtils.calculateCPK(usl, lsl, mean, stdDev), 3);
                // OK数
                Integer okNum = (int) dataList.stream().filter(x -> x >= lsl && x <= usl).count();
                // 良率
                Double okRate = MathUtils.round(okNum * 100.0 / dataList.size(), 3);

                nameList.add(name);
                meanList.add(mean);
                stdDevList.add(stdDev);
                cpkList.add(cpk);
                okRateList.add(okRate);
            }

            result.add(checkNameMap);
        }
        return new Result(200,"获取ORT应力数据详情",result);
    }
}
