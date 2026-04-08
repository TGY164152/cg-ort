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

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * <p>
 * 电阻
 * </p>
 *
 * @author TGY
 * @since 2025-11-12
 */
@Controller
@RequestMapping("/dfOrtResistance")
@Api(tags = "ORT电阻")
@ResponseBody
@CrossOrigin
public class DfOrtResistanceController {

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
    @GetMapping(value = "/getControlChart")
    @ApiOperation("获取控制图")
    public Result getWeibullPlot(
            @ApiParam("工厂")@RequestParam(value = "factory", required = false) String factory
            , @ApiParam("型号")@RequestParam(value = "project", required = false) String project
            , @ApiParam("颜色")@RequestParam(value = "color", required = false) String color
            , @ApiParam("阶段")@RequestParam(value = "stage", required = false) String stage
            , @ApiParam("工序")@RequestParam(value = "process", required = false) String process
            , @ApiParam("测试项目")@RequestParam(value = "checkItem", required = false) String checkItem
            , @ApiParam("测试名称")@RequestParam(value = "checkName", required = false) String checkName
            , @ApiParam("开始时间")@RequestParam(value = "startDate", required = false) String startDate
            , @ApiParam("结束时间")@RequestParam(value = "endDate", required = false) String endDate
    ) throws IOException {

        String startTime = startDate + " 00:00:00";
        String endTime = endDate + " 23:59:59";

        //结果
        Map<String, Object> result = new HashMap<>();
        //名称集合
        List<String> nameList = new ArrayList<>();
        //数据集合
        List<Object> dataList = new ArrayList<>();

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

        //标准
        QueryWrapper<DfOrtStandardConfig> standardConfigQw = new QueryWrapper<>();
        standardConfigQw
                .eq("project", project)
                .eq("color", color)
                .eq( "stage", stage)
                .eq("process", process)
                .eq( "check_item", checkItem)
                .eq("check_name", checkName)
                .last("limit 1");
        DfOrtStandardConfig standardConfigData = dfOrtStandardConfigService.getOne(standardConfigQw);

        if (standardConfigData == null){
            return new Result(500, "当前条件下没有相关标准数据");
        }

        // 需要查询的sql集合
        List<String> selectSqlNameList = new ArrayList<>();
        String checkCode = configData.getCheckCode();
        String selectSqlName = "JSON_ARRAYAGG("+ checkCode +") `" + checkName + "`";
        selectSqlNameList.add(selectSqlName);

        //标准
        Double min = standardConfigData.getStandardMin();
        Double max = standardConfigData.getStandardMax();

        //sql参数
        Map<String, Object> sqlParamMap = new HashMap<>();
        sqlParamMap.put("groupSql", "name");
        sqlParamMap.put("orderSql", "name asc");

        //筛选条件
        QueryWrapper<DfOrtTestData> qw = new QueryWrapper<>();
        qw
                .eq("factory", factory)
                .eq("project", project)
                .eq( "color", color)
                .eq("stage", stage)
                .eq("process", process)
                .eq("check_item", checkItem)
                .between("check_time", startTime, endTime);

        //单天
        if (startDate.equals(endDate)){
            //需要查询的sql
            sqlParamMap.put("rowSql", "batch");
            selectSqlNameList.add(0, "batch name");
            String selectSql = String.join(",", selectSqlNameList);
            sqlParamMap.put("selectSql", selectSql);
            sqlParamMap.put("numSql", 10);

            List<Map<String, String>> list = dfOrtTestDataService.getBatchArrayDataList(qw, sqlParamMap);

            if(list == null || list.size() == 0){
                return new Result(500, "当前条件下没有相关数据");
            }

            for (Map<String, String> map : list){
                String name = map.get("name");

                //当前批次，测试名称的数据
                List<Double> valueList = JsonUtil.toObject(map.get(checkName), new TypeToken<List<Double>>(){});
                nameList.add(name);
                dataList.add(valueList);
            }
        }else {
            //需要查询的sql
            sqlParamMap.put("rowSql", "check_date");
            selectSqlNameList.add(0, "batch name");
            String selectSql = String.join(",", selectSqlNameList);
            sqlParamMap.put("selectSql", selectSql);
            sqlParamMap.put("numSql", 7);

            List<Map<String, String>> list = dfOrtTestDataService.getBatchArrayDataList(qw, sqlParamMap);

            if(list == null || list.size() == 0){
                return new Result(500, "当前条件下没有相关数据");
            }

            for (Map<String, String> map : list){
                String name = map.get("name");

                //当前批次，测试名称的数据
                List<Double> valueList = JsonUtil.toObject(map.get(checkName), new TypeToken<List<Double>>(){});
                Double avg = valueList.stream().mapToDouble(Double::doubleValue).average().orElse(0);

                nameList.add(name);
                dataList.add(MathUtils.round(avg, 2));
            }
        }

        result.put("lsl", min);
        result.put("usl", max);
        result.put("nameList", nameList);
        result.put("dataList", dataList);

        return new Result(200,"获取控制图数据成功", result);
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
        selectSqlNameList.add(0, "batch name");
        String selectSql = String.join(",", selectSqlNameList);
        sqlParamMap.put("selectSql", selectSql);
        sqlParamMap.put("numSql", 7);
        sqlParamMap.put("groupSql", " batch ");
        sqlParamMap.put("orderSql", " batch asc");

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
                Double lsl = (Double) checkNameMap.get("lsl");
                Double usl = (Double) checkNameMap.get("usl");

                List<Double> dataList = JsonUtil.toObject(map.get(checkName), new TypeToken<List<Double>>() {});
                boolean checkNameFlag = true;

                for (Double data : dataList) {
                    if ( lsl != null && data < lsl) {
                        flag = false;
                        checkNameFlag = false;
                        break;
                    }

                    if (usl != null && data > usl) {
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

}
