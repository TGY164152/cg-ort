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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * ORT低温剪切
 * @author TGY
 * @date 2025-12-03
 */
@Controller
@RequestMapping("/dfOrtLowTemperatureShear")
@Api(tags = "ORT低温剪切")
@ResponseBody
@CrossOrigin
public class DfOrtLowTemperatureShearController {
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

        String redisKey = "ORT:Ink:VarianceAnalysis:" + factory + ":" + project + ":" + color + ":" + stage + ":" + process + ":"+ checkItem + ":" + checkName + ":" + startDate + "_" + endDate;
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
}
