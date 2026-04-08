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
 * ORT水滴角 前端控制器
 * </p>
 *
 * @author zhao
 * @since 2023-09-24
 */
@Controller
@RequestMapping("/dfOrtContactAngle")
@ResponseBody
@CrossOrigin
@Api(tags = "ORT水滴角")
public class DfOrtContactAngleController {

    @Autowired
    private Environment env;

    @Autowired
    private DfOrtTestDataService dfOrtTestDataService;

    @Autowired
    private DfOrtTestItemImportConfigService dfOrtTestItemImportConfigService;

    @Autowired
    private DfOrtStandardConfigService dfOrtStandardConfigService;

    /**
     * 获取测试名称类型
     * @return
     * @throws IOException
     */
    @GetMapping(value = "/getCheckNameTypeList")
    @ApiOperation("获取测试名称类型")
    public Result getCheckNameTypeList(
            @ApiParam("工序")@RequestParam(required = false) String process
            , @ApiParam("测试项目")@RequestParam(required = false) String checkItem
            , @ApiParam("测试类型")@RequestParam(required = false) String checkType
    ) {

        List<Object> result = new ArrayList<>();

        QueryWrapper<DfOrtTestItemImportConfig> configQw = new QueryWrapper<>();
        configQw
                .eq("process", process)
                .eq("check_item", checkItem)
                .like("check_name",checkType)
                .orderByAsc("REGEXP_REPLACE(SUBSTRING_INDEX(check_name, '_', 1), '[^0-9]', '') + 0");

        List<DfOrtTestItemImportConfig> configList = dfOrtTestItemImportConfigService.list(configQw);

        for (DfOrtTestItemImportConfig config : configList){
            String checkNameType = config.getCheckName();
            String[] nameArray = checkNameType.split("_");
            String checkName = nameArray[0];

            Map<String, Object> map = new HashMap<>();
            map.put("name", checkName);
            map.put("value", checkNameType);
            result.add(map);
        }

        return new Result(200, "获取测试名称类型成功", result);
    }

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
            , @ApiParam("测试名称类型")@RequestParam(required = false) String checkNameType
    ) throws IOException {

        String startTime = startDate + " 00:00:00";
        String endTime = endDate + " 23:59:59";

        //结果
        Map<String, Object> result = new HashMap<>();

        QueryWrapper<DfOrtTestItemImportConfig> configQw = new QueryWrapper<>();
        configQw
                .eq("process", process)
                .eq("check_item", checkItem)
                .eq("check_name",checkNameType)
                .last("limit 1");

        DfOrtTestItemImportConfig configData = dfOrtTestItemImportConfigService.getOne(configQw);
        if (configData == null){
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
                .eq("check_name", checkNameType)
                .last("limit 1");
        DfOrtStandardConfig standardConfigData = dfOrtStandardConfigService.getOne(standardConfigQw);

        if (standardConfigData == null){
            return new Result(500, "当前条件下没有相关标准数据");
        }

        // 需要查询的sql集合
        List<String> selectSqlNameList = new ArrayList<>();
        String checkCode = configData.getCheckCode();
        String selectSqlName = "JSON_ARRAYAGG("+ checkCode +") `" + checkNameType + "`";
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
            return new Result(500, "当前条件下没有相关数据");
        }

        List<String> nameList = new ArrayList<>();
        List<List<Double>> dataList = new ArrayList<>();
        List<Double> avgList = new ArrayList<>();

        for (Map<String, String> map : list) {
            String name = map.get("name");

            List<Double> valueList = JsonUtil.toObject(map.get(checkNameType), new TypeToken<List<Double>>(){});
            Double avg = valueList.stream().mapToDouble(Double::doubleValue).average().orElse(0);

            nameList.add(name);
            dataList.add(valueList);
            avgList.add(MathUtils.round(avg, 2));
        }

        result.put("name", checkNameType);
        result.put("usl", max);
        result.put("lsl", min);
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
                .eq("check_item", checkItem)
                .orderByAsc("check_item", "check_name");
        List<DfOrtStandardConfig> standardConfigList = dfOrtStandardConfigService.list(standardConfigQw);

        if (CollectionUtils.isEmpty(standardConfigList)){
            return new Result(500, "当前条件下没有相关标准数据");
        }

        // 需要查询的sql集合
        List<String> selectSqlNameList = new ArrayList<>();
        for (DfOrtTestItemImportConfig dfOrtTestItemImportConfig : configList) {
            // 测试名称类型
            String checkNameType = dfOrtTestItemImportConfig.getCheckName();
            //测试值字段
            String checkCode = dfOrtTestItemImportConfig.getCheckCode();

            String selectSqlName = "JSON_ARRAYAGG("+ checkCode +") `" + checkNameType + "`";

            selectSqlNameList.add(selectSqlName);
        }

        Map<String,Object> configMap = new HashMap<>();
        //测试类型集合
        List<String> checkTypeList = new ArrayList<>();
        for (DfOrtStandardConfig standardConfigData : standardConfigList) {
            String[] nameArray = standardConfigData.getCheckName().split("_");
            String checkName = nameArray[0];
            String checkType = nameArray[1];
            Double min = standardConfigData.getStandardMin();
            Double max = standardConfigData.getStandardMax();

            if (!configMap.containsKey(checkType)){
                Map<String,Object> checkTypeMap = new HashMap<>();
                //测试名称集合
                List<String> checkNameList = new ArrayList<>();
                checkNameList.add(checkName);

                checkTypeMap.put("okNum", 0);
                checkTypeMap.put("ngNum", 0);
                checkTypeMap.put("checkNameList", checkNameList);

                Map<String,Object> checkNameMap = new HashMap<>();
                checkNameMap.put("name", checkName);
                checkNameMap.put("lsl", min);
                checkNameMap.put("usl", max);

                checkTypeMap.put(checkName, checkNameMap);
                configMap.put(checkType, checkTypeMap);
                checkTypeList.add(checkType);
            }else {
                Map<String,Object> checkTypeMap = (Map<String, Object>) configMap.get(checkType);
                List<String> checkNameList = (List<String>) checkTypeMap.get("checkNameList");
                checkNameList.add(checkName);
                Map<String, Object> checkNameMap = new HashMap<>();
                checkNameMap.put("name", checkName);
                checkNameMap.put("usl", max);
                checkNameMap.put("lsl", min);
                checkTypeMap.put(checkName, checkNameMap);
            }
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
            boolean flag = true;

            for (String checkType : checkTypeList) {
                boolean checkTypeFlag = true;

                Map<String, Object> checkTypeMap = (Map<String, Object>) configMap.get(checkType);
                List<String> checkNameList = (List<String>) checkTypeMap.get("checkNameList");

                outer:
                for (String checkName : checkNameList) {
                    Map<String, Object> checkNameMap = (Map<String, Object>) checkTypeMap.get(checkName);
                    Double lsl = (Double) checkNameMap.get("lsl");
                    Double usl = (Double) checkNameMap.get("usl");
                    List<Double> dataList = JsonUtil.toObject(map.get(checkName + "_" + checkType), new TypeToken<List<Double>>() {});

                    for (Double data : dataList) {
                        if (lsl != null && data < lsl) {
                            checkTypeFlag = false;
                            flag = false;
                            break outer;
                        }

                        if (usl != null && data > usl){
                            checkTypeFlag = false;
                            flag = false;
                            break outer;
                        }
                    }
                }

                //更新测试类型批次的ok数和ng数
                if (checkTypeFlag) {
                    checkTypeMap.put("okNum", (Integer) checkTypeMap.get("okNum") + 1);
                } else {
                    checkTypeMap.put("ngNum", (Integer) checkTypeMap.get("ngNum") + 1);
                }
            }

            //更新该批次的ok数和ng数
            if (flag) {
                allOkNum += 1;
            } else {
                allNgNum += 1;
            }
        }

        for (String checkType : checkTypeList){
            Map<String, Object> resultMap = new HashMap<>();
            Map<String, Object> checkItemTypeData = (Map<String, Object>) configMap.get(checkType);
            Integer okNum = (Integer) checkItemTypeData.get("okNum");
            Integer ngNum = (Integer) checkItemTypeData.get("ngNum");
            Integer totalNum = okNum + ngNum;
            Double okRate = totalNum == 0 ? 0 : MathUtils.round(okNum * 100.0 / totalNum, 2);

            resultMap.put("name", checkType);
            resultMap.put("content", StringUtil.formatFT(ngNum, okNum));
            resultMap.put("okRate", okRate);
            result.add(resultMap);
        }

        Integer allTotalNum = allOkNum + allNgNum;
        Double allOkRate = allTotalNum == 0 ? 0 : MathUtils.round(allOkNum * 100.0 / allTotalNum,2);

        Map<String, Object> allData = new HashMap<>();
        allData.put("name", "总良率");
        allData.put("content", StringUtil.formatFT(allNgNum, allOkNum));
        allData.put("okRate",allOkRate);
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
            , @ApiParam("测试项目")@RequestParam(required = false) String checkItem
            , @ApiParam("开始时间")@RequestParam String startDate
            , @ApiParam("结束时间")@RequestParam String endDate
    ) {
        String startTime = startDate + " 00:00:00";
        String endTime = endDate + " 23:59:59";

        //结果
        List<Map<String, Object>> result = new ArrayList<>();

        //班次
        List<String> dayOrNightList = Arrays.asList("A", "B");

        QueryWrapper<DfOrtTestItemImportConfig> configQw = new QueryWrapper<>();
        configQw
                .eq("process", process)
                .eq("check_item", checkItem)
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
                .eq("check_item", checkItem)
                .orderByAsc("check_item", "check_name");
        List<DfOrtStandardConfig> standardConfigList = dfOrtStandardConfigService.list(standardConfigQw);

        if (CollectionUtils.isEmpty(standardConfigList)){
            return new Result(500, "当前条件下没有相关标准数据");
        }

        // 需要查询的sql集合
        List<String> selectSqlNameList = new ArrayList<>();
        for (DfOrtTestItemImportConfig dfOrtTestItemImportConfig : configList) {
            // 测试名称类型
            String checkNameType = dfOrtTestItemImportConfig.getCheckName();

            //测试值字段
            String checkCode = dfOrtTestItemImportConfig.getCheckCode();

            String selectSqlName = checkCode + " " + checkNameType;

            selectSqlNameList.add(selectSqlName);
        }

        Map<String,Object> configMap = new HashMap<>();
        //测试类型集合
        List<String> checkTypeList = new ArrayList<>();
        for (DfOrtStandardConfig standardConfigData : standardConfigList) {
            String[] nameArray = standardConfigData.getCheckName().split("_");
            String checkName = nameArray[0];
            String checkType = nameArray[1];
            Double min = standardConfigData.getStandardMin();
            Double max = standardConfigData.getStandardMax();

            if (!configMap.containsKey(checkType)){
                Map<String,Object> checkTypeMap = new HashMap<>();
                //测试名称集合
                List<String> checkNameList = new ArrayList<>();
                checkNameList.add(checkName);

                for (String dayOrNight : dayOrNightList){
                    Map<String, Object> dayOrNightMap = new HashMap<>();
                    dayOrNightMap.put("okNum", 0);
                    dayOrNightMap.put("ngNum", 0);
                    checkTypeMap.put(dayOrNight, dayOrNightMap);
                }
                checkTypeMap.put("checkNameList", checkNameList);

                Map<String, Object> checkNameMap = new HashMap<>();
                checkNameMap.put("name", checkName);
                checkNameMap.put("lsl",  min);
                checkNameMap.put("usl",  max);

                checkTypeMap.put(checkName, checkNameMap);
                configMap.put(checkType, checkTypeMap);
                checkTypeList.add(checkType);
            }else {
                Map<String,Object> checkTypeMap = (Map<String, Object>) configMap.get(checkType);
                List<String> checkNameList = (List<String>) checkTypeMap.get("checkNameList");
                checkNameList.add(checkName);
                Map<String, Object> checkNameMap = new HashMap<>();
                checkNameMap.put("name", checkName);
                checkNameMap.put("lsl",  min);
                checkNameMap.put("usl",  max);
                checkTypeMap.put(checkName, checkNameMap);
            }
        }

        //sql参数
        Map<String, Object> sqlParamMap = new HashMap<>();
        //需要查询的sql
        sqlParamMap.put("rowSql", "check_date");
        selectSqlNameList.add(0, "day_or_night");
        String selectSql = String.join(",", selectSqlNameList);
        sqlParamMap.put("selectSql", selectSql);
        sqlParamMap.put("numSql", 7);
        sqlParamMap.put("orderSql", "day_or_night asc");

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
        List<Map<String,String>> list = dfOrtTestDataService.getBatchDataList(qw,sqlParamMap);

        if(CollectionUtils.isEmpty(list)){
            return new Result(500, "当前条件下没有相关数据");
        }

        for (Map<String, String> map : list){
            String dayOrNight = map.get("day_or_night");

            for (String checkType: checkTypeList){

                //测试项目类型
                Map<String,Object> checkTypeMap = (Map<String, Object>) configMap.get(checkType);

                List<String> checkNameList = (List<String>) checkTypeMap.get("checkNameList");

                //班次OK和NG数
                Map<String,Integer> dayOrNightMap  = (Map<String,Integer>) checkTypeMap.get(dayOrNight);
                //样品是否合格
                boolean sampleFlag = true;

                for (String checkName: checkNameList){
                    Map<String, Object> checkNameMap = (Map<String, Object>) checkTypeMap.get(checkName);
                    Double lsl = (Double) checkNameMap.get("lsl");
                    Double usl = (Double) checkNameMap.get("usl");

                    Double checkValue = Double.valueOf(String.valueOf(map.get(checkName + "_" + checkType)));
                    if (lsl != null && checkValue < lsl){
                        sampleFlag = false;
                        break;
                    }

                    if (usl != null && checkValue > usl){
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

        for (String checkType : checkTypeList){
            Map<String,Object> checkItemData = new HashMap<>();

            Map<String,Object> checkTypeMap = (Map<String, Object>) configMap.get(checkType);
            List<Map<String,Object>> dayOrNightDataList = new ArrayList<>();

            for (String dayOrNight : dayOrNightList){
                Map<String,Integer> dayOrNightMap  = (Map<String,Integer>) checkTypeMap.get(dayOrNight);
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

            checkItemData.put("name", checkType);
            checkItemData.put("dayOrNightDataList", dayOrNightDataList);
            result.add(checkItemData);
        }
        return new Result(200, "查询成功", result);
    }
}
