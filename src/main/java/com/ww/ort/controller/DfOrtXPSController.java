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
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * XPS_物质成分分析
 * </p>
 *
 * @author TGY
 * @since 2025-11-12
 */
@Controller
@RequestMapping("/dfOrtXPS")
@Api(tags = "ORTXPS")
@ResponseBody
@CrossOrigin
public class DfOrtXPSController {

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
