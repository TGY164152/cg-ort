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
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.*;

/**
 * <p>
 * ORT汗液摩擦 前端控制器
 * </p>
 *
 * @author zhao
 * @since 2023-09-24
 */
@Controller
@RequestMapping("/dfOrtSweatRub")
@ResponseBody
@CrossOrigin
@Api(tags = "ORT汗液摩擦")
public class DfOrtSweatRubController {

    @Autowired
    private Environment env;

    @Autowired
    private DfOrtTestDataService dfOrtTestDataService;

    @Autowired
    private DfOrtTestItemImportConfigService dfOrtTestItemImportConfigService;

    @Autowired
    private DfOrtStandardConfigService dfOrtStandardConfigService;

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
            , @ApiParam("周期数")@RequestParam(required = false) String cycles
            , @ApiParam("测试名称")@RequestParam(required = false) String checkName
    ) throws IOException {

        String startTime = startDate + " 00:00:00";
        String endTime = endDate + " 23:59:59";

        //结果
        Map<String, Object> result = new HashMap<>();

        QueryWrapper<DfOrtTestItemImportConfig> configQw = new QueryWrapper<>();
        configQw
                .eq("process", process)
                .eq("check_item", checkItem)
                .eq("check_name",checkName)
                .last("limit 1");

        DfOrtTestItemImportConfig configData = dfOrtTestItemImportConfigService.getOne(configQw);
        if (configData == null){
            return new Result(500, "当前条件下没有相关测试项配置");
        }

        // 需要查询的sql集合
        List<String> selectSqlNameList = new ArrayList<>();
        String checkCode = configData.getCheckCode();
        String selectSqlName = "JSON_ARRAYAGG("+ checkCode +") `" + checkName + "`";
        selectSqlNameList.add(selectSqlName);

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
                .eq("string1", cycles)
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
        result.put("nameList", nameList);
        result.put("dataList", dataList);
        result.put("avgList", avgList);

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
            , @ApiParam("测试项目")@RequestParam(required = false) String checkItem
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
                .notIn("check_name", "cycles")
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
                .notIn("check_name", "cycles")
                .orderByAsc("check_name");
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

            selectSqlNameList.add(selectSqlName);
        }

        Map<String, Object> configMap = new HashMap<>();
        List<String> checkNameList = new ArrayList<>();
        for (DfOrtStandardConfig standardConfigData : standardConfigList) {
            String checkName = standardConfigData.getCheckName();
            Double min = standardConfigData.getStandardMin();
            Double max = standardConfigData.getStandardMax();

            Map<String,Object> checkNameMap = new HashMap<>();
            checkNameMap.put("name", checkName);
            checkNameMap.put("lsl",  min);
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
        selectSqlNameList.add(0, "batch");
        String selectSql = String.join(",", selectSqlNameList);
        sqlParamMap.put("selectSql", selectSql);
        sqlParamMap.put("numSql", 7);
        sqlParamMap.put("groupSql", "batch");
        sqlParamMap.put("orderSql", "batch asc");

        QueryWrapper<DfOrtTestData> qw = new QueryWrapper<>();
        qw
                .eq("factory", factory)
                .eq("project", project)
                .eq( "color", color)
                .eq("stage", stage)
                .eq("process", process)
                .eq( "check_item", checkItem)
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
                Double lsl = (Double) checkNameMap.get("lsl");
                Double usl = (Double) checkNameMap.get("usl");

                List<Double> dataList = JsonUtil.toObject(map.get(checkName), new TypeToken<List<Double>>() {});
                boolean checkNameFlag = true;

                for (Double data : dataList) {
                    if (data < lsl || data > usl) {
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

            checkNameMap.put("content", StringUtil.formatFT(ngNum, okNum));
            checkNameMap.put("okRate", okRate);
            result.add(checkNameMap);
        }

        Integer allTotalNum = allOkNum + allNgNum;
        Double allOkRate = allTotalNum == 0 ? 0 : MathUtils.round(allOkNum * 100.0 / allTotalNum,2);

        Map<String, Object> allData = new HashMap<>();
        allData.put("name", "总良率");
        allData.put("content", StringUtil.formatFT(allNgNum, allOkNum));
        allData.put("okRate", allOkRate);
        result.add(allData);
        return new Result(200, "查询成功", result);
    }


//    @ApiOperation("结果汇总明细")
//    @GetMapping("/resultStatisticsDetail")
//    public Result resultStatisticsDetail(
//            @ApiParam("工厂")@RequestParam(required = false) String factory
//            , @ApiParam("型号")@RequestParam(required = false) String project
//            , @ApiParam("颜色")@RequestParam(required = false) String color
//            , @ApiParam("阶段")@RequestParam(required = false) String stage
//            , @ApiParam("工序")@RequestParam(required = false) String process
//            , @ApiParam("测试项目")@RequestParam(required = false) String checkItem
//            , @ApiParam("开始时间")@RequestParam String startDate
//            , @ApiParam("结束时间")@RequestParam String endDate
//    ) {
//        String startTime = startDate + " 00:00:00";
//        String endTime = endDate + " 23:59:59";
//
//        //结果
//        List<Map<String, Object>> result = new ArrayList<>();
//        //测试名称集合
//        List<String> checkNameList = Arrays.asList("P1", "P2", "P3");
//        //测试类型集合
//        List<String> checkTypeList = Arrays.asList("摩前", "摩后");
//        //班次
//        List<String> dayOrNightList = Arrays.asList("A", "B");
//
//        QueryWrapper<DfOrtTestItemImportConfig> configQw = new QueryWrapper<>();
//        configQw
//                .eq("process", process)
//                .eq("check_item", checkItem)
//                .orderByAsc("check_item", "check_name");
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
//                .eq("check_item", checkItem)
//                .orderByAsc("check_item", "check_name");
//        List<DfOrtStandardConfig> standardConfigList = dfOrtStandardConfigService.list(standardConfigQw);
//
//        if (CollectionUtils.isEmpty(standardConfigList)){
//            return new Result(500, "当前条件下没有相关标准数据");
//        }
//
//        // 需要查询的sql集合
//        List<String> selectSqlNameList = new ArrayList<>();
//        for (DfOrtTestItemImportConfig dfOrtTestItemImportConfig : configList) {
//            // 测试名称
//            String checkName = dfOrtTestItemImportConfig.getCheckName();
//            //测试值字段
//            String checkCode = dfOrtTestItemImportConfig.getCheckCode();
//
//            String selectSqlName = checkCode + " `" + checkName + "`";
//
//            if (selectSqlNameList.contains(selectSqlName)){
//                continue;
//            }
//
//            selectSqlNameList.add(selectSqlName);
//        }
//
//        Map<String,Object> configMap = new HashMap<>();
//        List<String> checkItemTypeList = new ArrayList<>();
//        for (DfOrtStandardConfig standardConfigData : standardConfigList) {
//            String[] nameArray = standardConfigData.getCheckName().split("_");
//            String checkName = nameArray[0];
//            String checkType = nameArray[1];
//            Double min = standardConfigData.getStandardMin();
//
//            String checkItemType = checkItem + "-" + checkType;
//            if (!configMap.containsKey(checkItemType)){
//                Map<String,Object> checkItemTypeMap = new HashMap<>();
//
//                for (String dayOrNight : dayOrNightList){
//                    Map<String, Object> dayOrNightMap = new HashMap<>();
//                    dayOrNightMap.put("okNum", 0);
//                    dayOrNightMap.put("ngNum", 0);
//                    checkItemTypeMap.put(dayOrNight, dayOrNightMap);
//                }
//
//                Map<String, Object> checkNameMap = new HashMap<>();
//                checkNameMap.put("name", checkName);
//                checkNameMap.put("lsl",  min);
//
//                checkItemTypeMap.put(checkName, checkNameMap);
//                configMap.put(checkItemType, checkItemTypeMap);
//                checkItemTypeList.add(checkItemType);
//            }else {
//                Map<String,Object> checkItemTypeMap = (Map<String, Object>) configMap.get(checkItemType);
//                Map<String, Object> checkNameMap = new HashMap<>();
//                checkNameMap.put("name", checkName);
//                checkNameMap.put("lsl",  min);
//                checkItemTypeMap.put(checkName, checkNameMap);
//            }
//        }
//
//        //sql参数
//        Map<String, Object> sqlParamMap = new HashMap<>();
//        //需要查询的sql
//        sqlParamMap.put("rowSql", "check_date");
//        selectSqlNameList.add(0, "day_or_night");
//        String selectSql = String.join(",", selectSqlNameList);
//        sqlParamMap.put("selectSql", selectSql);
//        sqlParamMap.put("numSql", 7);
//        sqlParamMap.put("orderSql", "day_or_night asc");
//
//        QueryWrapper<DfOrtTestData> qw = new QueryWrapper<>();
//        qw
//                .eq("factory", factory)
//                .eq("project", project)
//                .eq( "color", color)
//                .eq("stage", stage)
//                .eq("process", process)
//                .eq( "check_item", checkItem)
//                .between("check_time", startTime, endTime);
//
//        //查询结果
//        List<Map<String,String>> list = dfOrtTestDataService.getBatchDataList(qw,sqlParamMap);
//
//        if(CollectionUtils.isEmpty(list)){
//            return new Result(500, "当前条件下没有相关数据");
//        }
//
//        for (Map<String, String> map : list){
//            String dayOrNight = map.get("day_or_night");
//
//            for (String checkType: checkTypeList){
//                String checkItemType = checkItem + "-" + checkType;
//
//                //测试项目类型
//                Map<String,Object> checkItemTypeMap = (Map<String, Object>) configMap.get(checkItemType);
//                //班次OK和NG数
//                Map<String,Integer> dayOrNightMap  = (Map<String,Integer>) checkItemTypeMap.get(dayOrNight);
//                //样品是否合格
//                boolean sampleFlag = true;
//
//                for (String checkName: checkNameList){
//                    Map<String, Object> checkNameMap = (Map<String, Object>) checkItemTypeMap.get(checkName);
//                    Double lsl = (Double) checkNameMap.get("lsl");
//
//                    Double checkValue = Double.valueOf(String.valueOf(map.get(checkName + "_" + checkType)));
//                    if (checkValue < lsl){
//                        sampleFlag = false;
//                        break;
//                    }
//                }
//
//                if (sampleFlag){
//                    dayOrNightMap.put("okNum", dayOrNightMap.get("okNum") + 1);
//                }else {
//                    dayOrNightMap.put("ngNum", dayOrNightMap.get("ngNum") + 1);
//                }
//            }
//        }
//
//        for (String checkItemType : checkItemTypeList){
//            Map<String,Object> checkItemData = new HashMap<>();
//
//            Map<String,Object> checkItemTypeMap = (Map<String, Object>) configMap.get(checkItemType);
//            List<Map<String,Object>> dayOrNightDataList = new ArrayList<>();
//
//            for (String dayOrNight : dayOrNightList){
//                Map<String,Integer> dayOrNightMap  = (Map<String,Integer>) checkItemTypeMap.get(dayOrNight);
//                Integer okNum = dayOrNightMap.get("okNum");
//                Integer ngNum = dayOrNightMap.get("ngNum");
//                Integer totalNum = okNum + ngNum;
//                Double okRate = totalNum == 0 ? 0 : MathUtils.round(okNum * 100.0 / totalNum,2);
//
//                Map<String,Object> dayOrNightData = new HashMap<>();
//                dayOrNightData.put("dayOrNight", dayOrNight);
//                dayOrNightData.put("okNum", okNum);
//                dayOrNightData.put("ngNum", ngNum);
//                dayOrNightData.put("totalNum", totalNum);
//                dayOrNightData.put("okRate", okRate);
//                dayOrNightDataList.add(dayOrNightData);
//            }
//
//            checkItemData.put("name", checkItemType);
//            checkItemData.put("dayOrNightDataList", dayOrNightDataList);
//            result.add(checkItemData);
//        }
//        return new Result(200, "查询成功", result);
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
//            , @ApiParam("测试项目")@RequestParam(value = "checkItem", required = false) String checkItem
//            , @ApiParam("开始时间")@RequestParam String startDate
//            , @ApiParam("结束时间")@RequestParam String endDate
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
//        //A班良率
//        List<Double> aOkRateList = new ArrayList<>();
//        //B班良率
//        List<Double> bOkRateList = new ArrayList<>();
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
//        //标准
//        QueryWrapper<DfOrtStandardConfig> standardConfigQw = new QueryWrapper<>();
//        standardConfigQw
//                .eq("project", project)
//                .eq("color", color)
//                .eq( "stage", stage)
//                .eq("process", process)
//                .eq("check_item", checkItem)
//                .orderByAsc("check_name");
//        List<DfOrtStandardConfig> standardConfigList = dfOrtStandardConfigService.list(standardConfigQw);
//
//        if (CollectionUtils.isEmpty(standardConfigList)){
//            return new Result(500, "当前条件下没有相关标准数据");
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
//            String selectSqlName = checkCode + " `" + checkName + "`";
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
//            Map<String,Object> checkNameTypeMap = new HashMap<>();
//            checkNameTypeMap.put("name", checkName);
//            checkNameTypeMap.put("lsl", min);
//            checkNameTypeMap.put("usl", max);
//            configMap.put(checkName, checkNameTypeMap);
//            checkNameList.add(checkName);
//        }
//
//        //sql参数
//        Map<String, Object> sqlParamMap = new HashMap<>();
//        //需要查询的sql
//        sqlParamMap.put("rowSql", "check_date");
//        selectSqlNameList.add(0, "check_date, day_or_night");
//        String selectSql = String.join(",", selectSqlNameList);
//        sqlParamMap.put("selectSql", selectSql);
//        sqlParamMap.put("numSql", 7);
//        sqlParamMap.put("groupSql", " check_date, day_or_night ");
//        sqlParamMap.put("orderSql", " check_date asc, day_or_night asc");
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
//        //查询结果
//        List<Map<String,String>> list = dfOrtTestDataService.getBatchDataList(qw,sqlParamMap);
//
//        if(CollectionUtils.isEmpty(list)){
//            return new Result(500, "当前条件下没有相关数据");
//        }
//
//        Map<String, Object> resultMap = new HashMap<>();
//
//        for (Map<String, String> map : list) {
//            String checkDate = map.get("check_date");
//            String dayOrNight = map.get("day_or_night");
//            boolean flag = true;
//
//            for (String checkNameType : checkNameList){
//                Map<String, Object> checkNameTypeData = (Map<String, Object>) configMap.get(checkNameType);
//                Double lsl = (Double) checkNameTypeData.get("lsl");
//
//                Double checkValue = Double.valueOf(String.valueOf(map.get(checkNameType)));
//
//                if (checkValue < lsl) {
//                    flag = false;
//                    break;
//                }
//            }
//
//            if (!resultMap.containsKey(checkDate)) {
//                Map<String, Map<String, Integer>> checkDateMap = new HashMap<>();
//                Map<String, Integer> aMap = new HashMap<>();
//                Map<String, Integer> bMap = new HashMap<>();
//
//                aMap.put("okNum", 0);
//                aMap.put("allNum", 0);
//                bMap.put("okNum", 0);
//                bMap.put("allNum", 0);
//                checkDateMap.put("A", aMap);
//                checkDateMap.put("B", bMap);
//
//                checkDateMap.get(dayOrNight).put("allNum", 1);
//                if (flag) {
//                    checkDateMap.get(dayOrNight).put("okNum", 1);
//                }
//
//                resultMap.put(checkDate, checkDateMap);
//                dateList.add(checkDate);
//            } else {
//                Map<String, Object> checkDateMap = (Map<String, Object>) resultMap.get(checkDate);
//                Map<String, Integer> abMap = (Map<String, Integer>) checkDateMap.get(dayOrNight);
//
//                if (flag) {
//                    abMap.put("okNum", abMap.get("okNum") + 1);
//                }
//
//                abMap.put("allNum", abMap.get("allNum") + 1);
//            }
//        }
//
//        for (String checkDate : dateList){
//            Map<String, Object> checkDateMap = (Map<String, Object>) resultMap.get(checkDate);
//
//            for (String dayOrNight : dayOrNightList){
//                Map<String, Integer> abMap = (Map<String, Integer>) checkDateMap.get(dayOrNight);
//                Integer okNum = abMap.get("okNum");
//                Integer allNum = abMap.get("allNum");
//                Double okRate = allNum == 0 ? 0 : MathUtils.round(okNum * 100.0 / allNum, 2) ;
//
//                if ("A".equals(dayOrNight)){
//                    aNumList.add(allNum);
//                    aOkRateList.add(okRate);
//                }else {
//                    bNumList.add(allNum);
//                    bOkRateList.add(okRate);
//                }
//            }
//        }
//
//        result.put("dateList", dateList);
//        result.put("aNumList", aNumList);
//        result.put("aOkRateList", aOkRateList);
//        result.put("bNumList", bNumList);
//        result.put("bOkRateList", bOkRateList);
//        return new Result(200, "查询成功", result);
//    }
}
