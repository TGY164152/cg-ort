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
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * <p>
 * ORT颜色/雾都 前端控制器
 * </p>
 *
 * @author zhao
 * @since 2023-06-25
 */
@Controller
@RequestMapping("/dfOrtColorAndHaze")
@CrossOrigin
@Api(tags = "ORT颜色/雾都")
@ResponseBody
public class DfOrtColorAndHazeController {

    @Autowired
    private Environment env;

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private DfOrtTestDataService dfOrtTestDataService;

    @Autowired
    private DfOrtTestItemImportConfigService dfOrtTestItemImportConfigService;

    @Autowired
    private DfOrtStandardConfigService dfOrtStandardConfigService;

    /**
     * 获取ORT多点位箱线图（颜色）
     * @return
     * @throws IOException
     */
    @GetMapping(value = "/getBoxPlotMultiPointColor")
    @ApiOperation("ORT多点位箱线图（颜色）")
    public Result getBoxPlotMultiPointColor(
            @ApiParam("工厂")@RequestParam(value = "factory", required = false) String factory
            , @ApiParam("型号")@RequestParam(value = "project", required = false) String project
            , @ApiParam("颜色")@RequestParam(value = "color", required = false) String color
            , @ApiParam("阶段")@RequestParam(value = "stage", required = false) String stage
            , @ApiParam("工序")@RequestParam(value = "process", required = false) String process
            , @ApiParam("测试项目")@RequestParam(value = "checkItem", required = false) String checkItem
            , @ApiParam("测试类型")@RequestParam(value = "checkType", required = false) String checkType
            , @ApiParam("开始时间")@RequestParam String startDate
            , @ApiParam("结束时间")@RequestParam String endDate

    ) throws IOException {

        String startTime = startDate + " 00:00:00";
        String endTime = endDate + " 23:59:59";

        String redisKey = "ORT:Environment:BoxPlot:" + factory + ":" + project + ":" + color + ":" + stage + ":" + process + ":"+ checkItem + ":" + checkType + ":" + startDate + "_" + endDate;
        if (redisUtils.hasKey(redisKey)){
            String filename = (String) redisUtils.get(redisKey);

            String data = env.getProperty("imgUrl") + "/" + filename;
            return new Result(200,"获取ORT强度均值分析图成功",data);
        }

        QueryWrapper<DfOrtTestItemImportConfig> configQw = new QueryWrapper<>();
        configQw
                .eq("process", process)
                .eq("check_item", checkItem)
                .like("check_name", checkType);

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
                .like("check_name", checkType);
        List<DfOrtStandardConfig> standardConfigList = dfOrtStandardConfigService.list(standardConfigQw);

        if (CollectionUtils.isEmpty(standardConfigList)){
            return new Result(500, "当前条件下没有相关标准数据");
        }

        // 需要查询的sql集合
        List<String> selectSqlNameList = new ArrayList<>();
        for (DfOrtTestItemImportConfig dfOrtTestItemImportConfig : configList) {
            String checkNameItemType = dfOrtTestItemImportConfig.getCheckName();
            String[] nameArray = checkNameItemType.split("_");
            String checkName = nameArray[0];

            String selectSqlName = dfOrtTestItemImportConfig.getCheckCode() + " " + checkName;
            selectSqlNameList.add(selectSqlName);
        }

        //sql参数
        Map<String, Object> sqlParamMap = new HashMap<>();
        //查询结果
        List<Map<String,String>> list = null;
        //替换参数
        Map<String, String> replaceMap = new HashMap<>();

        Map<String, Double> showMap = new HashMap<>();
        List<String> checkNameList = new ArrayList<>();
        for (DfOrtStandardConfig standardConfig : standardConfigList){
            String checkNameItemType = standardConfig.getCheckName();
            String[] nameArray = checkNameItemType.split("_");
            String checkName = nameArray[0];
            Double max = standardConfig.getStandardMax();
            Double min = standardConfig.getStandardMin();
            Double USLShow = max + Math.abs(max) * 0.05;
            Double LSLShow = min - Math.abs(min) * 0.05;

            replaceMap.put("#" + checkName + "USL#", max.toString());
            replaceMap.put("#" + checkName + "LSL#", min.toString());

            showMap.put(checkName + "USLShow", USLShow);
            showMap.put(checkName + "LSLShow", LSLShow);
            checkNameList.add(checkName);
        }

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
            selectSqlNameList.add(0, "batch");
            String selectSql = String.join(",", selectSqlNameList);
            sqlParamMap.put("selectSql", selectSql);
            sqlParamMap.put("numSql", 10);
            sqlParamMap.put("orderSql", "batch asc");
            replaceMap.put("#XShow#", "batch");

            list = dfOrtTestDataService.getBatchDataList(qw, sqlParamMap);
        }else {
            //需要查询的sql
            sqlParamMap.put("rowSql", "check_date");
            selectSqlNameList.add(0, "check_date date");
            String selectSql = String.join(",", selectSqlNameList);
            sqlParamMap.put("selectSql", selectSql);
            sqlParamMap.put("numSql", 7);
            sqlParamMap.put("orderSql", "date asc");
            replaceMap.put("#XShow#", "date");

            list = dfOrtTestDataService.getBatchDataList(qw, sqlParamMap);
        }

        if(CollectionUtils.isEmpty(list)){
            return new Result(500, "当前条件下没有相关数据");
        }


        for (Map<String, String> map : list){
            for (String checkName : checkNameList){
                Double value = Double.valueOf(String.valueOf(map.get(checkName)));

                Double USLShow = showMap.get(checkName + "USLShow");
                Double LSLShow = showMap.get(checkName + "LSLShow");

                if (value > USLShow){
                    showMap.put(checkName + "USLShow", value);
                }

                if (value < LSLShow){
                    showMap.put(checkName + "LSLShow", value);
                }
            }
        }

        for (Map.Entry<String, Double> entry : showMap.entrySet()){
            replaceMap.put("#" + entry.getKey() + "#", entry.getValue().toString());
        }

        replaceMap.put("#JSON_DATA#", JsonUtil.toJson(list));

        String jslFilePath = env.getProperty("jslPath") + "/多点位（固定）箱线图脚本.jsl";
        String jslCreatePath = env.getProperty("jslCreatePath");
        String imgPath = env.getProperty("imgPath");

        Map<String,String> urlMap = testController.modifyFileContent(jslFilePath, jslCreatePath,imgPath, replaceMap);
        CmdScript.runCmd(urlMap.get("runJsl"));

        File imgFile=new File(urlMap.get("imageUrl"));

        if (!imgFile.exists()|| null== imgFile) {
            return new Result(500,"查询失败");
        }

        redisUtils.set(redisKey,urlMap.get("imageName"));

        return new Result(200,"获取ORT多点位（固定）箱线图（颜色）数据成功", env.getProperty("imgUrl") + "/" + urlMap.get("imageName"));
    }


    @ApiOperation("结果汇总（颜色）")
    @GetMapping("/resultStatisticsColor")
    public Result resultStatisticsColor(
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
        //测试项目集合
        List<String> checkItemCodeList = Arrays.asList("BM区", "BM0 SD区", "视窗区", "IR区(Rx)");
        //测试名称集合
        List<String> checkNameList = Arrays.asList("Y", "a", "b");

        QueryWrapper<DfOrtTestItemImportConfig> configQw = new QueryWrapper<>();
        configQw
                .eq("process", process)
                .eq("check_item", checkItem)
                .or(item ->item.like("check_name", "BM区_24H")
                        .like("check_name", "BM0 SD区_24H")
                        .like("check_name", "视窗区_24H")
                        .like("check_name", "IR区(Rx)_24H")
                );

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
                .or(item ->item.like("check_name", "BM区_24H")
                        .like("check_name", "BM0 SD区_24H")
                        .like("check_name", "视窗区_24H")
                        .like("check_name", "IR区(Rx)_24H")
                );
        List<DfOrtStandardConfig> standardConfigList = dfOrtStandardConfigService.list(standardConfigQw);

        if (CollectionUtils.isEmpty(standardConfigList)){
            return new Result(500, "当前条件下没有相关标准数据");
        }

        // 需要查询的sql集合
        List<String> selectSqlNameList = new ArrayList<>();
        for (DfOrtTestItemImportConfig dfOrtTestItemImportConfig : configList) {
            String[] nameArray = dfOrtTestItemImportConfig.getCheckName().split("_");
            String checkNameItemCode = nameArray[0] + "_" + nameArray[1];

            //测试值字段
            String checkCode = dfOrtTestItemImportConfig.getCheckCode();

            String selectSqlName = "JSON_ARRAYAGG("+ checkCode +") `" + checkNameItemCode + "`";

            selectSqlNameList.add(selectSqlName);
        }

        Map<String,Object> configMap = new HashMap<>();
        for (DfOrtStandardConfig standardConfigData : standardConfigList) {
            String[] nameArray = standardConfigData.getCheckName().split("_");
            String checkName = nameArray[0];
            String checkItemCode = nameArray[1];
            Double min = standardConfigData.getStandardMin();
            Double max = standardConfigData.getStandardMax();

            if (!configMap.containsKey(checkItemCode)){
                Map<String,Object> checkItemCodeMap = new HashMap<>();

                checkItemCodeMap.put("okNum", 0);
                checkItemCodeMap.put("ngNum", 0);

                Map<String,Object> checkNameMap = new HashMap<>();
                checkNameMap.put("name", checkName);
                checkNameMap.put("lsl", min);
                checkNameMap.put("usl", max);
                checkNameMap.put("okNum", 0);
                checkNameMap.put("ngNum", 0);

                checkItemCodeMap.put(checkName, checkNameMap);
                configMap.put(checkItemCode, checkItemCodeMap);
            }else {
                Map<String,Object> checkItemTypeMap = (Map<String, Object>) configMap.get(checkItemCode);
                Map<String, Object> checkNameMap = new HashMap<>();
                checkNameMap.put("name", checkName);
                checkNameMap.put("usl", max);
                checkNameMap.put("lsl", min);
                checkNameMap.put("okNum", 0);
                checkNameMap.put("ngNum", 0);
                checkItemTypeMap.put(checkName, checkNameMap);
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
                .eq("check_item", checkItem)
                .between("check_time", startTime, endTime);

        //查询结果
        List<Map<String,String>> list = dfOrtTestDataService.getBatchArrayDataList(qw,sqlParamMap);

        if(CollectionUtils.isEmpty(list)){
            return new Result(500, "当前条件下没有相关数据");
        }

        Map<String,Object> dataMap = new HashMap<>();
        List<String> batchList = new ArrayList<>();
        for (Map<String, String> map : list) {
            String batch = map.get("batch");
            Map<String, Object> batchMap = new HashMap<>();

            for (String checkItemCode : checkItemCodeList){
                Map<String, Object> checkItemCodeMap = new HashMap<>();

                for (String checkName : checkNameList) {
                    List<Double> dataList = JsonUtil.toObject(map.get(checkName + "_" + checkItemCode), new TypeToken<List<Double>>() {});
                    checkItemCodeMap.put(checkName, dataList);
                }

                batchMap.put(checkItemCode, checkItemCodeMap);
            }

            batchList.add(batch);
            dataMap.put(batch, batchMap);
        }

        //总ok数
        Integer allOkNum = 0;
        //总ng数
        Integer allNgNum = 0;

        for (String batch : batchList) {
            Map<String, Object> batchMap = (Map<String, Object>) dataMap.get(batch);

            //批次是否合格
            boolean batchFlag = true;

            for (String checkItemCode : checkItemCodeList) {
                Map<String, Object> checkItemCodeMap = (Map<String, Object>) batchMap.get(checkItemCode);
                Map<String, Object> checkItemCodeConfigMap = (Map<String, Object>) configMap.get(checkItemCode);
                boolean checkItemCodeFlag = true;

                for (String checkName : checkNameList) {
                    Map<String, Object> checkNameConfigMap = (Map<String, Object>) checkItemCodeConfigMap.get(checkName);
                    Double lsl = (Double) checkNameConfigMap.get("lsl");
                    Double usl = (Double) checkNameConfigMap.get("usl");
                    boolean checkNameFlag = true;

                    List<Double> valueList = (List<Double>) checkItemCodeMap.get(checkName);

                    for (Double value : valueList) {
                        if (lsl != null && value < lsl) {
                            checkNameFlag = false;
                            checkItemCodeFlag = false;
                            batchFlag = false;
                            break;
                        }

                        if (usl != null && value > usl) {
                            checkNameFlag = false;
                            checkItemCodeFlag = false;
                            batchFlag = false;
                            break;
                        }
                    }

                    if (checkNameFlag) {
                        checkNameConfigMap.put("okNum", (Integer) checkNameConfigMap.get("okNum") + 1);
                    } else {
                        checkNameConfigMap.put("ngNum", (Integer) checkNameConfigMap.get("ngNum") + 1);
                    }
                }

                if (checkItemCodeFlag) {
                    checkItemCodeConfigMap.put("okNum", (Integer) checkItemCodeConfigMap.get("okNum") + 1);
                } else {
                    checkItemCodeConfigMap.put("ngNum", (Integer) checkItemCodeConfigMap.get("ngNum") + 1);
                }
            }

            if (batchFlag) {
                allOkNum += 1;
            } else {
                allNgNum += 1;
            }
        }

        for (String checkItemCode : checkItemCodeList){
            Map<String, Object> resultMap = new HashMap<>();
            List<Object> checkNameDataList = new ArrayList<>();

            Map<String, Object> checkItemCodeConfigMap = (Map<String, Object>) configMap.get(checkItemCode);
            Integer okNumCheckItemCode = (Integer) checkItemCodeConfigMap.get("okNum");
            Integer ngNumCheckItemCode = (Integer) checkItemCodeConfigMap.get("ngNum");
            Integer totalNumCheckItemCode = okNumCheckItemCode + ngNumCheckItemCode;
            Double okRateCheckItemCode = totalNumCheckItemCode == 0 ? 0 : MathUtils.round(okNumCheckItemCode * 100.0 / totalNumCheckItemCode, 2);

            for (String checkName : checkNameList){
                Map<String, Object> checkNameConfigMap = (Map<String, Object>) checkItemCodeConfigMap.get(checkName);
                Integer okNumCheckName = (Integer) checkNameConfigMap.get("okNum");
                Integer ngNumCheckName = (Integer) checkNameConfigMap.get("ngNum");
                Integer totalNumCheckName = okNumCheckName + ngNumCheckName;
                Double okRateCheckName = totalNumCheckName == 0 ? 0 : MathUtils.round(okNumCheckName * 100.0 / totalNumCheckName, 2);

                Map<String, Object> checkNameData = new HashMap<>();
                checkNameData.put("name", checkName);
                checkNameData.put("content", StringUtil.formatFT(ngNumCheckName, okNumCheckName));
                checkNameData.put("okRate", okRateCheckName);
                checkNameDataList.add(checkNameData);
            }

            Map<String, Object> allCheckNameData = new HashMap<>();
            allCheckNameData.put("name", "总良率");
            allCheckNameData.put("content", StringUtil.formatFT(ngNumCheckItemCode, okNumCheckItemCode));
            allCheckNameData.put("okRate", okRateCheckItemCode);
            checkNameDataList.add(allCheckNameData);

            resultMap.put("name", checkItemCode);
            resultMap.put("checkNameDataList", checkNameDataList);
            result.add(resultMap);
        }

        Map<String, Object> allData = new HashMap<>();
        allData.put("name", "总良率");
        allData.put("content", StringUtil.formatFT(allNgNum, allOkNum));
        allData.put("okRate", MathUtils.round(allOkNum * 100.0 / (allNgNum + allOkNum),2));
        result.add(allData);
        return new Result(200, "查询成功", result);
    }

    /**
     * 获取箱线图（雾度）
     * @return
     * @throws IOException
     */
    @GetMapping(value = "/getBoxplotHaze")
    @ApiOperation("获取箱线图（雾度）")
    public Result getBoxplotHaze(
            @ApiParam("工厂")@RequestParam(required = false) String factory
            , @ApiParam("型号")@RequestParam(required = false) String project
            , @ApiParam("颜色")@RequestParam(required = false) String color
            , @ApiParam("阶段")@RequestParam(required = false) String stage
            , @ApiParam("工序")@RequestParam(required = false) String process
            , @ApiParam("测试项目")@RequestParam(required = false) String checkItem
            , @ApiParam("测试项目编号")@RequestParam(required = false) String checkItemCode
            , @ApiParam("开始时间")@RequestParam String startDate
            , @ApiParam("结束时间")@RequestParam String endDate

    ) throws IOException {

        String startTime = startDate + " 00:00:00";
        String endTime = endDate + " 23:59:59";

        //测试类型集合
        List<String> checkTypeList = Arrays.asList("正常", "24H");

        //结果
        List<Map<String, Object>> result = new ArrayList<>();

        QueryWrapper<DfOrtTestItemImportConfig> configQw = new QueryWrapper<>();
        configQw
                .eq("process", process)
                .eq("check_item", checkItem)
                .like("check_name",checkItemCode)
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
                .like("check_name", checkItemCode)
                .orderByAsc("check_name");
        List<DfOrtStandardConfig> standardConfigList = dfOrtStandardConfigService.list(standardConfigQw);

        if (CollectionUtils.isEmpty(standardConfigList)){
            return new Result(500, "当前条件下没有相关标准数据");
        }

        // 需要查询的sql集合
        List<String> selectSqlNameList = new ArrayList<>();
        for (DfOrtTestItemImportConfig dfOrtTestItemImportConfig : configList) {
            String[] nameArray = dfOrtTestItemImportConfig.getCheckName().split("_");
            String checkNameType = nameArray[0] + "_" + nameArray[2];
            String checkCode = dfOrtTestItemImportConfig.getCheckCode();

            String selectSqlName = "JSON_ARRAYAGG("+ checkCode +") `" + checkNameType + "`";
            selectSqlNameList.add(selectSqlName);
        }

        Map<String,Object> configMap = new HashMap<>();
        List<String> checkNameList = new ArrayList<>();
        for (DfOrtStandardConfig standardConfigData : standardConfigList){
            String[] nameArray = standardConfigData.getCheckName().split("_");
            String checkName = nameArray[0];
            String checkType = nameArray[2];
            Double min = standardConfigData.getStandardMin();
            Double max = standardConfigData.getStandardMax();

            if (!configMap.containsKey(checkName)){
                Map<String,Object> checkNameMap = new HashMap<>();
                Map<String,Object> checkTypeMap = new HashMap<>();
                checkTypeMap.put("name", checkType);
                checkTypeMap.put("lsl", min);
                checkTypeMap.put("usl", max);
                checkTypeMap.put("nameList", new ArrayList<String>());
                checkTypeMap.put("dataList", new ArrayList<List<Double>>());
                checkTypeMap.put("avgList", new ArrayList<Double>());

                checkNameMap.put("name", checkName);
                checkNameMap.put(checkType, checkTypeMap);
                configMap.put(checkName, checkNameMap);
                checkNameList.add(checkName);
            }else {
                Map<String, Object> checkNameMap = (Map<String, Object>) configMap.get(checkName);
                Map<String,Object> checkTypeMap = new HashMap<>();
                checkTypeMap.put("name", checkType);
                checkTypeMap.put("lsl", min);
                checkTypeMap.put("usl", max);
                checkTypeMap.put("nameList", new ArrayList<String>());
                checkTypeMap.put("dataList", new ArrayList<List<Double>>());
                checkTypeMap.put("avgList", new ArrayList<Double>());
                checkNameMap.put(checkType, checkTypeMap);
            }
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
            Map<String,Object> checkNameMap = (Map<String, Object>) configMap.get(checkName);

            for (String checkType : checkTypeList){
                Map<String,Object> checkTypeMap = (Map<String, Object>) checkNameMap.get(checkType);

                List<String> nameList = (List<String>) checkTypeMap.get("nameList");
                List<List<Double>> dataList = (List<List<Double>>) checkTypeMap.get("dataList");
                List<Double> avgList = (List<Double>) checkTypeMap.get("avgList");

                for (Map<String, String> map : list) {
                    String name = map.get("name");

                    List<Double> valueList = JsonUtil.toObject(map.get(checkName + "_" + checkType), new TypeToken<List<Double>>(){});
                    Double avg = valueList.stream().mapToDouble(Double::doubleValue).average().orElse(0);

                    nameList.add(name);
                    dataList.add(valueList);
                    avgList.add(MathUtils.round(avg, 2));
                }
            }

            result.add(checkNameMap);
        }

        return new Result(200,"查询成功",result);
    }

    @ApiOperation("结果汇总（雾度）")
    @GetMapping("/resultStatisticsHaze")
    public Result resultStatisticsHaze(
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
        //测试项目集合
        List<String> checkItemCodeList = Arrays.asList("IR 雾度", "视窗区 雾度");
        //测试名称IR集合
        List<String> checkNameIRList = Arrays.asList("Px", "Rx", "Tx");
        //视窗雾度测试项目
        String checkItemHaze = "视窗区 雾度";
        //测试名称Haze集合
        List<String> checkNameHazeList = Arrays.asList("Haze");

        QueryWrapper<DfOrtTestItemImportConfig> configQw = new QueryWrapper<>();
        configQw
                .eq("process", process)
                .eq("check_item", checkItem)
                .or(item ->item.like("check_name", "IR 雾度_24H")
                        .like("check_name", "视窗区 雾度_24H")
                );

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
                .or(item ->item.like("check_name", "IR 雾度_24H")
                        .like("check_name", "视窗区 雾度_24H")
                );
        List<DfOrtStandardConfig> standardConfigList = dfOrtStandardConfigService.list(standardConfigQw);

        if (CollectionUtils.isEmpty(standardConfigList)){
            return new Result(500, "当前条件下没有相关标准数据");
        }

        // 需要查询的sql集合
        List<String> selectSqlNameList = new ArrayList<>();
        for (DfOrtTestItemImportConfig dfOrtTestItemImportConfig : configList) {
            String[] nameArray = dfOrtTestItemImportConfig.getCheckName().split("_");
            String checkNameItemCode = nameArray[0] + "_" + nameArray[1];

            //测试值字段
            String checkCode = dfOrtTestItemImportConfig.getCheckCode();

            String selectSqlName = "JSON_ARRAYAGG("+ checkCode +") `" + checkNameItemCode + "`";

            selectSqlNameList.add(selectSqlName);
        }

        Map<String,Object> configMap = new HashMap<>();
        for (DfOrtStandardConfig standardConfigData : standardConfigList) {
            String[] nameArray = standardConfigData.getCheckName().split("_");
            String checkName = nameArray[0];
            String checkItemCode = nameArray[1];
            Double min = standardConfigData.getStandardMin();
            Double max = standardConfigData.getStandardMax();

            if (!configMap.containsKey(checkItemCode)){
                Map<String,Object> checkItemCodeMap = new HashMap<>();

                checkItemCodeMap.put("okNum", 0);
                checkItemCodeMap.put("ngNum", 0);

                Map<String,Object> checkNameMap = new HashMap<>();
                checkNameMap.put("name", checkName);
                checkNameMap.put("lsl", min);
                checkNameMap.put("usl", max);
                checkNameMap.put("okNum", 0);
                checkNameMap.put("ngNum", 0);

                checkItemCodeMap.put(checkName, checkNameMap);
                configMap.put(checkItemCode, checkItemCodeMap);
            }else {
                Map<String,Object> checkItemTypeMap = (Map<String, Object>) configMap.get(checkItemCode);
                Map<String, Object> checkNameMap = new HashMap<>();
                checkNameMap.put("name", checkName);
                checkNameMap.put("usl", max);
                checkNameMap.put("lsl", min);
                checkNameMap.put("okNum", 0);
                checkNameMap.put("ngNum", 0);
                checkItemTypeMap.put(checkName, checkNameMap);
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
                .eq("check_item", checkItem)
                .between("check_time", startTime, endTime);

        //查询结果
        List<Map<String,String>> list = dfOrtTestDataService.getBatchArrayDataList(qw,sqlParamMap);

        if(CollectionUtils.isEmpty(list)){
            return new Result(500, "当前条件下没有相关数据");
        }

        Map<String,Object> dataMap = new HashMap<>();
        List<String> batchList = new ArrayList<>();
        List<String> checkNameList = new ArrayList<>();
        for (Map<String, String> map : list) {
            String batch = map.get("batch");
            Map<String, Object> batchMap = new HashMap<>();

            for (String checkItemCode : checkItemCodeList){
                Map<String, Object> checkItemCodeMap = new HashMap<>();

                if (checkItemHaze.equals(checkItemCode)){
                    checkNameList = checkNameHazeList;
                }else {
                    checkNameList = checkNameIRList;
                }

                for (String checkName : checkNameList) {
                    List<Double> dataList = JsonUtil.toObject(map.get(checkName + "_" + checkItemCode), new TypeToken<List<Double>>() {});
                    checkItemCodeMap.put(checkName, dataList);
                }

                batchMap.put(checkItemCode, checkItemCodeMap);
            }

            batchList.add(batch);
            dataMap.put(batch, batchMap);
        }

        //总ok数
        Integer allOkNum = 0;
        //总ng数
        Integer allNgNum = 0;

        for (String batch : batchList) {
            Map<String, Object> batchMap = (Map<String, Object>) dataMap.get(batch);

            //批次是否合格
            boolean batchFlag = true;

            for (String checkItemCode : checkItemCodeList) {
                Map<String, Object> checkItemCodeMap = (Map<String, Object>) batchMap.get(checkItemCode);
                Map<String, Object> checkItemCodeConfigMap = (Map<String, Object>) configMap.get(checkItemCode);
                boolean checkItemCodeFlag = true;

                if (checkItemHaze.equals(checkItemCode)){
                    checkNameList = checkNameHazeList;
                }else {
                    checkNameList = checkNameIRList;
                }

                for (String checkName : checkNameList) {
                    Map<String, Object> checkNameConfigMap = (Map<String, Object>) checkItemCodeConfigMap.get(checkName);
                    Double lsl = (Double) checkNameConfigMap.get("lsl");
                    Double usl = (Double) checkNameConfigMap.get("usl");
                    boolean checkNameFlag = true;

                    List<Double> valueList = (List<Double>) checkItemCodeMap.get(checkName);

                    for (Double value : valueList) {
                        if (lsl != null && value < lsl) {
                            checkNameFlag = false;
                            checkItemCodeFlag = false;
                            batchFlag = false;
                            break;
                        }

                        if (usl != null && value > usl) {
                            checkNameFlag = false;
                            checkItemCodeFlag = false;
                            batchFlag = false;
                            break;
                        }
                    }

                    if (checkNameFlag) {
                        checkNameConfigMap.put("okNum", (Integer) checkNameConfigMap.get("okNum") + 1);
                    } else {
                        checkNameConfigMap.put("ngNum", (Integer) checkNameConfigMap.get("ngNum") + 1);
                    }
                }

                if (checkItemCodeFlag) {
                    checkItemCodeConfigMap.put("okNum", (Integer) checkItemCodeConfigMap.get("okNum") + 1);
                } else {
                    checkItemCodeConfigMap.put("ngNum", (Integer) checkItemCodeConfigMap.get("ngNum") + 1);
                }
            }

            if (batchFlag) {
                allOkNum += 1;
            } else {
                allNgNum += 1;
            }
        }

        for (String checkItemCode : checkItemCodeList){
            Map<String, Object> resultMap = new HashMap<>();
            List<Object> checkNameDataList = new ArrayList<>();

            Map<String, Object> checkItemCodeConfigMap = (Map<String, Object>) configMap.get(checkItemCode);
            Integer okNumCheckItemCode = (Integer) checkItemCodeConfigMap.get("okNum");
            Integer ngNumCheckItemCode = (Integer) checkItemCodeConfigMap.get("ngNum");
            Integer totalNumCheckItemCode = okNumCheckItemCode + ngNumCheckItemCode;
            Double okRateCheckItemCode = totalNumCheckItemCode == 0 ? 0 : MathUtils.round(okNumCheckItemCode * 100.0 / totalNumCheckItemCode, 2);

            if (checkItemHaze.equals(checkItemCode)){
                checkNameList = checkNameHazeList;
            }else {
                checkNameList = checkNameIRList;
            }

            for (String checkName : checkNameList){
                Map<String, Object> checkNameConfigMap = (Map<String, Object>) checkItemCodeConfigMap.get(checkName);
                Integer okNumCheckName = (Integer) checkNameConfigMap.get("okNum");
                Integer ngNumCheckName = (Integer) checkNameConfigMap.get("ngNum");
                Integer totalNumCheckName = okNumCheckName + ngNumCheckName;
                Double okRateCheckName = totalNumCheckName == 0 ? 0 : MathUtils.round(okNumCheckName * 100.0 / totalNumCheckName, 2);

                Map<String, Object> checkNameData = new HashMap<>();
                checkNameData.put("name", checkName);
                checkNameData.put("content", StringUtil.formatFT(ngNumCheckName, okNumCheckName));
                checkNameData.put("okRate", okRateCheckName);
                checkNameDataList.add(checkNameData);
            }

            if (!checkItemHaze.equals(checkItemCode)){
                Map<String, Object> allCheckNameData = new HashMap<>();
                allCheckNameData.put("name", "总良率");
                allCheckNameData.put("content", StringUtil.formatFT(ngNumCheckItemCode, okNumCheckItemCode));
                allCheckNameData.put("okRate", okRateCheckItemCode);
                checkNameDataList.add(allCheckNameData);
            }

            resultMap.put("name", checkItemCode);
            resultMap.put("checkNameDataList", checkNameDataList);
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
