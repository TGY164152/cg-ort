package com.ww.ort.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.gson.reflect.TypeToken;
import com.ww.ort.entity.DfOrtStandardConfig;
import com.ww.ort.entity.DfOrtTestData;
import com.ww.ort.entity.DfOrtTestItemImportConfig;
import com.ww.ort.service.DfOrtStandardConfigService;
import com.ww.ort.service.DfOrtTestDataService;
import com.ww.ort.service.DfOrtTestItemImportConfigService;
import com.ww.ort.utils.JsonUtil;
import com.ww.ort.utils.MathUtils;
import com.ww.ort.utils.Result;
import com.ww.ort.utils.StringUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>
 * ORT-IR透过率 前端控制器
 * </p>
 *
 * @author zhao
 * @since 2024-12-31
 */
@Controller
@RequestMapping("/dfOrtThroughRate")
@CrossOrigin
@ResponseBody
@Api(tags = "ORT透过率")
public class DfOrtThroughRateController {

    //波长名称
    String waveName = "wave";

    @Autowired
    private DfOrtTestDataService dfOrtTestDataService;

    @Autowired
    private DfOrtTestItemImportConfigService dfOrtTestItemImportConfigService;

    @Autowired
    private DfOrtStandardConfigService dfOrtStandardConfigService;

    /**
     * 获取控制图
     * @return
     * @throws IOException
     */
    @GetMapping(value = "/getBatchList")
    @ApiOperation("获取批次")
    public Result getBatchList(
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
        List<String> result = new ArrayList<>();

        // 需要查询的sql集合
        List<String> selectSqlNameList = new ArrayList<>();
        //sql参数
        Map<String, Object> sqlParamMap = new HashMap<>();
        //需要查询的sql
        sqlParamMap.put("rowSql", "check_date");
        selectSqlNameList.add(0, "batch name");
        String selectSql = String.join(",", selectSqlNameList);
        sqlParamMap.put("selectSql", selectSql);
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

        //查询结果
        List<Map<String,String>> list = dfOrtTestDataService.getBatchArrayDataList(qw, sqlParamMap);

        if(CollectionUtils.isEmpty(list)){
            return new Result(500, "当前条件下没有相关数据");
        }

        for (Map<String, String> map : list){
            String batch = map.get("name");
            result.add(batch);
        }

        return new Result(200,"查询成功",result);
    }

    /**
     * 获取控制图
     * @return
     * @throws IOException
     */
    @GetMapping(value = "/getControlChart")
    @ApiOperation("获取控制图")
    public Result getControlChart(
            @ApiParam("工厂")@RequestParam(value = "factory", required = false) String factory
            , @ApiParam("型号")@RequestParam(value = "project", required = false) String project
            , @ApiParam("颜色")@RequestParam(value = "color", required = false) String color
            , @ApiParam("阶段")@RequestParam(value = "stage", required = false) String stage
            , @ApiParam("工序")@RequestParam(value = "process", required = false) String process
            , @ApiParam("测试项目")@RequestParam(value = "checkItem", required = false) String checkItem
            , @ApiParam("批次")@RequestParam(value = "batch", required = false) String batch
    ) throws IOException {
        //结果
        Map<String, Object> result = new HashMap<>();
        //波长集合
        List<String> waveList = new ArrayList<>();
        //数据集合
        List<Object> dataList = new ArrayList<>();
        //测试名称集合
        List<String> checkNameList = new ArrayList<>();

        QueryWrapper<DfOrtTestItemImportConfig> configQw = new QueryWrapper<>();
        configQw
                .eq("process", process)
                .eq("check_item", checkItem)
                .ne("check_name","wave")
                .orderByAsc("CAST(check_name AS UNSIGNED)");

        List<DfOrtTestItemImportConfig> configList = dfOrtTestItemImportConfigService.list(configQw);
        if (CollectionUtils.isEmpty(configList)){
            return new Result(500, "当前条件下没有相关测试项配置");
        }

        // 需要查询的sql集合
        List<String> selectSqlNameList = new ArrayList<>();
        Map<String,Object> configMap = new HashMap<>();
        for (DfOrtTestItemImportConfig dfOrtTestItemImportConfig : configList) {
            String checkName = dfOrtTestItemImportConfig.getCheckName();
            String checkCode = dfOrtTestItemImportConfig.getCheckCode();

            String selectSqlName =  checkCode + " `" + checkName + "`";
            selectSqlNameList.add(selectSqlName);

            Map<String, Object> checkNameMap = new HashMap<>();
            checkNameMap.put("name", checkName);
            checkNameMap.put("valueList", new ArrayList<Double>());
            configMap.put(checkName, checkNameMap);
            checkNameList.add(checkName);
        }

        //sql参数
        Map<String, Object> sqlParamMap = new HashMap<>();
        sqlParamMap.put("rowSql", "check_date");
        selectSqlNameList.add(0, "string1 wave");
        String selectSql = String.join(",", selectSqlNameList);
        sqlParamMap.put("selectSql", selectSql);
        sqlParamMap.put("numSql", 7);
        sqlParamMap.put("orderSql", "id asc");

        QueryWrapper<DfOrtTestData> qw = new QueryWrapper<>();
        qw
                .eq("factory", factory)
                .eq("project", project)
                .eq( "color", color)
                .eq("stage", stage)
                .eq("process", process)
                .eq("check_item", checkItem)
                .eq("batch", batch);

        //查询结果
        List<Map<String,String>> list = dfOrtTestDataService.getBatchDataList(qw, sqlParamMap);

        if(CollectionUtils.isEmpty(list)){
            return new Result(500, "当前条件下没有相关数据");
        }

        for (Map<String, String> map : list){
            String wave = map.get("wave");

            for (String checkName : checkNameList){
                Map<String, Object> configData = (Map<String, Object>) configMap.get(checkName);
                List<Double> valueList = (List<Double>) configData.get("valueList");
                Double value = JsonUtil.toObject(map.get(checkName), new TypeToken<Double>(){});
                valueList.add(MathUtils.round(value, 2));
            }
            waveList.add(wave);
        }

        for (String checkName : checkNameList){
            Map<String, Object> configData = (Map<String, Object>) configMap.get(checkName);
            dataList.add(configData);
        }

        result.put("nameList", waveList);
        result.put("dataList", dataList);

        return new Result(200,"获取控制图成功",result);
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
                .ne("check_name",waveName)
                .orderByAsc("CAST(check_name AS UNSIGNED)");

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
                .eq("check_item", checkItem);
        List<DfOrtStandardConfig> standardConfigList = dfOrtStandardConfigService.list(standardConfigQw);

        if (CollectionUtils.isEmpty(standardConfigList)){
            return new Result(500, "当前条件下没有相关标准数据");
        }

        Map<String,Object> configMap = new HashMap<>();
        List<String> standardNameList = new ArrayList<>();
        Pattern pattern = Pattern.compile("(\\w+)\\((\\d+)~(\\d+)\\)");
        for (DfOrtStandardConfig standardConfigData : standardConfigList) {
            String checkName = standardConfigData.getCheckName();
            Matcher standardMatcher = pattern.matcher(checkName);
            standardMatcher.matches();
            String standardName = standardMatcher.group(1) + "_" + standardMatcher.group(2) + "_" + standardMatcher.group(3);
            Double min = standardConfigData.getStandardMin();
            Double max = standardConfigData.getStandardMax();

            Map<String,Object> standardNameMap = new HashMap<>();
            standardNameMap.put("name", standardName);
            standardNameMap.put("lsl", min);
            standardNameMap.put("usl", max);
            configMap.put(standardName, standardNameMap);
            standardNameList.add(standardName);
        }

        // 需要查询的sql集合
        List<String> selectSqlNameList = new ArrayList<>();
        List<String> pieceNameList = new ArrayList<>();
        for (DfOrtTestItemImportConfig dfOrtTestItemImportConfig : configList) {
            //测试值字段
            String checkCode = dfOrtTestItemImportConfig.getCheckCode();
            // 测试名称
            String pieceName = dfOrtTestItemImportConfig.getCheckName();

            for (String standardName : standardNameList){
                String[] standardNameArray = standardName.split("_");
                String grammarName = standardNameArray[0];
                String startWave = standardNameArray[1];
                String endWave = standardNameArray[2];

                String templateStr = "{0}(case when {1} between {2} and {3} then {4} end) {0}_{2}_{3}_{5}";
                String selectSqlName = MessageFormat.format(templateStr, grammarName, waveName, startWave,endWave, checkCode, pieceName);

                selectSqlNameList.add(selectSqlName);
            }
            pieceNameList.add(pieceName);
        }

        //sql参数
        Map<String, Object> sqlParamMap = new HashMap<>();
        //需要查询的sql
        sqlParamMap.put("rowSql", "check_date");
        selectSqlNameList.add(0, "check_date, batch, day_or_night");
        String selectSql = String.join(",", selectSqlNameList);
        sqlParamMap.put("selectSql", selectSql);
        sqlParamMap.put("numSql", 7);
        sqlParamMap.put("groupSql", "check_date, day_or_night, batch");
        sqlParamMap.put("orderSql", "check_date asc, day_or_night asc, batch asc");

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
        List<Map<String,String>> list = dfOrtTestDataService.getThroughBatchDataList(qw,sqlParamMap);

        if(CollectionUtils.isEmpty(list)){
            return new Result(500, "当前条件下没有相关数据");
        }

        Map<String, Object> resultMap = new HashMap<>();

        for (Map<String, String> map : list) {
            String checkDate = map.get("check_date");
            String batch = map.get("batch");
            String dayOrNight = map.get("day_or_night");

            for (String pieceName : pieceNameList){
                boolean flag = true;

                for (String standardName : standardNameList){
                    Map<String, Object> standardNameMap = (Map<String, Object>) configMap.get(standardName);
                    Double lsl = (Double) standardNameMap.get("lsl");
                    Double max = (Double) standardNameMap.get("usl");

                    Double checkValue = MathUtils.round(JsonUtil.toObject(map.get(standardName + "_" + pieceName), new TypeToken<Double>(){}), 2) ;

                    if (lsl != null && checkValue < lsl){
                        flag = false;
                        break;
                    }

                    if (max != null && checkValue > max){
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
        return new Result(200, "查询班次良率成功", result);
    }


    /**
     * 获取波段列表
     * @return
     * @throws IOException
     */
    @GetMapping(value = "/getWaveBandList")
    @ApiOperation("获取波段列表")
    public Result getWaveBandList(
            @ApiParam("工厂")@RequestParam(required = false) String factory
            , @ApiParam("型号")@RequestParam(required = false) String project
            , @ApiParam("颜色")@RequestParam(required = false) String color
            , @ApiParam("阶段")@RequestParam(required = false) String stage
            , @ApiParam("工序")@RequestParam(required = false) String process
            , @ApiParam("测试项目")@RequestParam(required = false) String checkItem
    ) throws IOException {
        QueryWrapper<DfOrtStandardConfig> qw = new QueryWrapper<>();
        qw
                .eq("project", project)
                .eq( "color", color)
                .eq("stage", stage)
                .eq("process", process)
                .eq("check_item", checkItem);

        List<DfOrtStandardConfig> list = dfOrtStandardConfigService.list(qw);

        return new Result(200,"查询成功",list);
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
            , @ApiParam("开始时间")@RequestParam String startDate
            , @ApiParam("结束时间")@RequestParam String endDate
            , @ApiParam("波段")@RequestParam String waveBand
    ) throws IOException {

        String startTime = startDate + " 00:00:00";
        String endTime = endDate + " 23:59:59";

        //结果
        Map<String, Object> result = new HashMap<>();
        //名称集合
        List<String> nameList = new ArrayList<>();
        //箱线数据
        List<List<Double>> dataList = new ArrayList<>();
        //平均值
        List<Double> avgList = new ArrayList<>();

        QueryWrapper<DfOrtTestItemImportConfig> configQw = new QueryWrapper<>();
        configQw
                .eq("process", process)
                .eq("check_item", checkItem)
                .ne("check_name","wave")
                .orderByAsc("CAST(check_name AS UNSIGNED)");

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
                .eq("check_name", waveBand)
                .last("limit 1");
        DfOrtStandardConfig standardConfigData = dfOrtStandardConfigService.getOne(standardConfigQw);
        if (standardConfigData == null){
            return new Result(500, "当前条件下没有相关测试项标准配置");
        }

        Double min = standardConfigData.getStandardMin();
        Double max = standardConfigData.getStandardMax();
        Pattern pattern = Pattern.compile("(\\w+)\\((\\d+)~(\\d+)\\)");
        String standardName = standardConfigData.getCheckName();
        Matcher standardMatcher = pattern.matcher(standardName);
        standardMatcher.matches();
        String grammarName = standardMatcher.group(1);
        String startWave = standardMatcher.group(2);
        String endWave = standardMatcher.group(3);

        //需要查询的sql集合
        List<String> selectSqlNameList = new ArrayList<>();
        // 需要查询的sql集合new
        List<String> selectSqlNameNewList = new ArrayList<>();
        List<String> pieceNameList = new ArrayList<>();
        for (DfOrtTestItemImportConfig dfOrtTestItemImportConfig : configList) {
            //测试值字段
            String checkCode = dfOrtTestItemImportConfig.getCheckCode();
            // 测试名称
            String pieceName = dfOrtTestItemImportConfig.getCheckName();

            String templateNewStr = "{0}(case when {1} between {2} and {3} then {4} end) {0}_{2}_{3}_{5}";
            String selectSqlNameNew = MessageFormat.format(templateNewStr, grammarName, waveName, startWave,endWave, checkCode, pieceName);

            String templateStr = "JSON_ARRAYAGG({0}_{1}_{2}_{3}) {0}_{1}_{2}_{3}";
            String selectSqlName = MessageFormat.format(templateStr, grammarName, startWave, endWave, pieceName);

            selectSqlNameNewList.add(selectSqlNameNew);
            selectSqlNameList.add(selectSqlName);
            pieceNameList.add(pieceName);
        }

        //sql参数
        Map<String, Object> sqlParamMap = new HashMap<>();
        selectSqlNameNewList.add(0, "check_date, batch");
        String selectSqlNew = String.join(",", selectSqlNameNewList);
        sqlParamMap.put("selectSqlNew", selectSqlNew);
        sqlParamMap.put("groupSqlNew", "check_date, batch");
        sqlParamMap.put("orderSqlNew", "check_date asc, batch asc");
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

            list = dfOrtTestDataService.getThroughBatchArrayDataList(qw, sqlParamMap);
        }else {
            //需要查询的sql
            sqlParamMap.put("rowSql", "check_date");
            selectSqlNameList.add(0, "check_date name");
            String selectSql = String.join(",", selectSqlNameList);
            sqlParamMap.put("selectSql", selectSql);
            sqlParamMap.put("numSql", 7);

            list = dfOrtTestDataService.getThroughBatchArrayDataList(qw, sqlParamMap);
        }

        if(CollectionUtils.isEmpty(list)){
            return new Result(500, "当前条件下没有相关测试项数据");
        }


        for (Map<String, String> map : list) {
            String name = map.get("name");

            List<Double> nameValueList = new ArrayList<>();

            for (String pieceName : pieceNameList){
                String checkName = grammarName + "_" + startWave + "_" + endWave + "_" + pieceName;
                List<Double> valueList = JsonUtil.toObject(map.get(checkName), new TypeToken<List<Double>>(){});
                valueList.stream().forEach(value -> value = MathUtils.round(value, 2));
                nameValueList.addAll(valueList);
            }

            Double avg = nameValueList.stream().mapToDouble(Double::doubleValue).average().orElse(0);

            nameList.add(name);
            dataList.add(nameValueList);
            avgList.add(MathUtils.round(avg, 2));
        }

        result.put("lsl", min);
        result.put("usl", max);
        result.put("nameList", nameList);
        result.put("dataList", dataList);
        result.put("avgList", avgList);
        return new Result(200,"获取箱线图成功", result);
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

        QueryWrapper<DfOrtTestItemImportConfig> configQw = new QueryWrapper<>();
        configQw
                .eq("process", process)
                .eq("check_item", checkItem)
                .ne("check_name","wave")
                .orderByAsc("CAST(check_name AS UNSIGNED)");

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
                .eq("check_item", checkItem);
        List<DfOrtStandardConfig> standardConfigList = dfOrtStandardConfigService.list(standardConfigQw);

        if (CollectionUtils.isEmpty(standardConfigList)){
            return new Result(500, "当前条件下没有相关标准数据");
        }

        Map<String,Object> configMap = new HashMap<>();
        List<String> standardNameList = new ArrayList<>();
        Pattern pattern = Pattern.compile("(\\w+)\\((\\d+)~(\\d+)\\)");
        for (DfOrtStandardConfig standardConfigData : standardConfigList) {
            String checkName = standardConfigData.getCheckName();
            Matcher standardMatcher = pattern.matcher(checkName);
            standardMatcher.matches();
            String standardName = standardMatcher.group(1) + "_" + standardMatcher.group(2) + "_" + standardMatcher.group(3);
            Double min = standardConfigData.getStandardMin();
            Double max = standardConfigData.getStandardMax();

            Map<String,Object> standardNameMap = new HashMap<>();
            standardNameMap.put("name", standardName);
            standardNameMap.put("checkName", checkName);
            standardNameMap.put("lsl", min);
            standardNameMap.put("usl", max);
            configMap.put(standardName, standardNameMap);
            standardNameList.add(standardName);
        }

        // 需要查询的sql集合
        List<String> selectSqlNameList = new ArrayList<>();
        List<String> pieceNameList = new ArrayList<>();
        for (DfOrtTestItemImportConfig dfOrtTestItemImportConfig : configList) {
            //测试值字段
            String checkCode = dfOrtTestItemImportConfig.getCheckCode();
            // 测试名称
            String pieceName = dfOrtTestItemImportConfig.getCheckName();

            for (String standardName : standardNameList){
                String[] standardNameArray = standardName.split("_");
                String grammarName = standardNameArray[0];
                String startWave = standardNameArray[1];
                String endWave = standardNameArray[2];

                String templateStr = "{0}(case when {1} between {2} and {3} then {4} end) {0}_{2}_{3}_{5}";
                String selectSqlName = MessageFormat.format(templateStr, grammarName, waveName, startWave,endWave, checkCode, pieceName);

                selectSqlNameList.add(selectSqlName);
            }
            pieceNameList.add(pieceName);
        }

        //sql参数
        Map<String, Object> sqlParamMap = new HashMap<>();
        //需要查询的sql
        sqlParamMap.put("rowSql", "check_date");
        selectSqlNameList.add(0, "check_date, batch, day_or_night");
        String selectSql = String.join(",", selectSqlNameList);
        sqlParamMap.put("selectSql", selectSql);
        sqlParamMap.put("numSql", 7);
        sqlParamMap.put("groupSql", "check_date, day_or_night, batch");
        sqlParamMap.put("orderSql", "check_date asc, day_or_night asc, batch asc");

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
        List<Map<String,String>> list = dfOrtTestDataService.getThroughBatchDataList(qw,sqlParamMap);

        if(CollectionUtils.isEmpty(list)){
            return new Result(500, "当前条件下没有相关数据");
        }


        for (String standardName : standardNameList){
            Map<String, Object> standardNameMap = (Map<String, Object>) configMap.get(standardName);
            String checkName = (String) standardNameMap.get("checkName");
            Double lsl = (Double) standardNameMap.get("lsl");
            Double usl = (Double) standardNameMap.get("usl");
            String standard = "";
            if (lsl != null && usl == null){
                standard = "≥" + lsl;
            }else if (lsl == null && usl != null){
                standard = "≤" + usl;
            }

            Integer okNum = 0;
            Integer totalNum = 0;

            List<Double> dataList = new ArrayList<>();

            for (Map<String, String> map : list){

                for (String pieceName : pieceNameList){
                    Double checkValue = MathUtils.round(JsonUtil.toObject(map.get(standardName + "_" + pieceName), new TypeToken<Double>(){}), 2) ;

                    if ((lsl != null && checkValue >= lsl) || (usl != null && checkValue <= usl)){
                        okNum += 1;
                    }

                    totalNum += 1;
                    dataList.add(checkValue);
                }
            }

            //良率
            Double okRate = totalNum == 0 ? 0 : MathUtils.round(okNum * 100.0 / totalNum,2);
            //最大值
            Double max = MathUtils.round(Collections.max(dataList),2);
            //最小值
            Double min = MathUtils.round(Collections.min(dataList),2);
            //平均值
            Double mean = MathUtils.calculateMean(dataList);
            //样本方差
            Double variance = MathUtils.calculateSampleVariance(dataList, mean);
            //标准差
            Double standardDeviation = MathUtils.calculateStandardDeviation(variance);
            //CPK
            Double cpk = MathUtils.round(MathUtils.calculateCPK(usl, lsl, mean, standardDeviation),2);

            Map<String, Object> standardNameData = new HashMap<>();
            standardNameData.put("name", checkName);
            standardNameData.put("standard", standard);
            standardNameData.put("okRate", okRate);
            standardNameData.put("max", max);
            standardNameData.put("min", min);
            standardNameData.put("mean", MathUtils.round(mean,2));
            standardNameData.put("sigma", MathUtils.round(standardDeviation,2));
            standardNameData.put("cpk", cpk);
            result.add(standardNameData);
        }

        return new Result(200, "查询结果汇总成功", result);
    }

}
