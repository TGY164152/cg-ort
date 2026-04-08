package com.ww.ort.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.gson.reflect.TypeToken;
import com.ww.ort.entity.*;
import com.ww.ort.service.DfOrtStandardConfigService;
import com.ww.ort.service.DfOrtTestDataService;
import com.ww.ort.utils.JsonUtil;
import com.ww.ort.utils.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.*;

import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * ORT百格 前端控制器
 * </p>
 *
 * @author zhao
 * @since 2024-12-26
 */
@Controller
@RequestMapping("/dfOrtBaige")
@ResponseBody
@CrossOrigin
@Api(tags = "ORT百格")
public class DfOrtBaigeController {
    @Autowired
    private Environment env;

    @Autowired
    private DfOrtTestDataService dfOrtTestDataService;

    @Autowired
    private DfOrtStandardConfigService dfOrtStandardConfigService;

    @GetMapping(value = "/getBaigeOkRate")
    @ApiOperation("获取ORT百格良率")
    public Result getBaigeOkRate(
            @ApiParam("项目")@RequestParam(value = "project", required = true) String project
            , @ApiParam("工厂")@RequestParam(value = "factory", required = true) String factory
            , @ApiParam("型号")@RequestParam(value = "model", required = true) String model
            , @ApiParam("颜色")@RequestParam(value = "color", required = true) String color
            , @ApiParam("阶段")@RequestParam(value = "stage", required = true) String stage
            , @ApiParam("工序")@RequestParam(value = "process", required = true) String process
            , @ApiParam("测试项目")@RequestParam(value = "checkItem", required = true) String checkItem
            , @ApiParam("开始时间")@RequestParam String startDate
            , @ApiParam("结束时间")@RequestParam String endDate
    ){
        String startTime = startDate + " 00:00:00";
        String endTime = endDate + " 23:59:59";

        //结果
        List<Map<String, Object>> reslutList = new ArrayList<>();

        QueryWrapper<DfOrtStandardConfig> configQw = new QueryWrapper<>();
        configQw
                .eq("project", project)
                .eq("model", model)
                .eq("color", color)
                .eq( "stage", stage)
                .eq("process", process)
                .eq( "check_item", checkItem);

        List<DfOrtStandardConfig> configList = dfOrtStandardConfigService.list(configQw);

        if (configList == null || configList.size() == 0){
            return new Result(500, "当前条件下没有相关标准数据");
        }

        Map<String, String> configMap = new HashMap<>();
        for (DfOrtStandardConfig configData : configList) {
            configMap.put(configData.getCheckName(), configData.getStandardValue());
        }

        //sql参数
        Map<String, Object> sqlParamMap = new HashMap<>();
        String selectSql = "batch,JSON_ARRAYAGG(string2) string2_array_str,JSON_ARRAYAGG(string3) string3_array_str,JSON_ARRAYAGG(string4) string4_array_str,JSON_ARRAYAGG(string5) string5_array_str";
        String groupSql = "batch";
        QueryWrapper<DfOrtTestData> qw = new QueryWrapper<>();
        qw
                .eq("project", project)
                .eq("factory", factory)
                .eq("model", model)
                .eq( "color", color)
                .eq("stage", stage)
                .eq("process", process)
                .eq("check_item", checkItem)
                .between("check_time", startTime, endTime);
        List<Map<String,String>> list = dfOrtTestDataService.getBatchArrayDataList(qw,sqlParamMap);

        if(list == null || list.size() == 0){
            return new Result(500, "当前条件下没有相关数据");
        }

        for (Map<String, String> map : list){
            String batch = map.get("batch");
            String string2_array_str = map.get("string2_array_str");
            String string3_array_str = map.get("string3_array_str");
            String string4_array_str = map.get("string4_array_str");
            String string5_array_str = map.get("string5_array_str");

            List<String> string2List= JsonUtil.toObject(string2_array_str, new TypeToken<List<String>>(){});
            List<String> string3List= JsonUtil.toObject(string3_array_str, new TypeToken<List<String>>(){});
            List<String> string4List= JsonUtil.toObject(string4_array_str, new TypeToken<List<String>>(){});
            List<String> string5List= JsonUtil.toObject(string5_array_str, new TypeToken<List<String>>(){});

            int allNum = string2List.size();
            int okNum = 0;

            for (int i = 0; i < allNum; i++){
                if (configMap.get("Cosmetic").equalsIgnoreCase(string2List.get(i))
                    && configMap.get("Mask area").compareTo(string3List.get(i)) <= 0
                    && configMap.get("AR arera").compareTo(string4List.get(i)) <= 0
                    && configMap.get("IR+BS-AR area").compareTo(string5List.get(i)) <= 0){
                    okNum++;
                }
            }

            Map<String,Object> result = new HashMap<>();
            result.put("batch", batch);
            result.put("okRate", Math.round(okNum  * 10000.0 / allNum) / 100.0);
            reslutList.add(result);
        }
        return new Result(200, "查询成功", reslutList);
    }


    @GetMapping(value = "/getBaigeSummary")
    @ApiOperation("获取ORT百格汇总")
    public Result getBaigeSummary(
            @ApiParam("项目")@RequestParam(value = "project", required = true) String project
            , @ApiParam("工厂")@RequestParam(value = "factory", required = true) String factory
            , @ApiParam("型号")@RequestParam(value = "model", required = true) String model
            , @ApiParam("颜色")@RequestParam(value = "color", required = true) String color
            , @ApiParam("阶段")@RequestParam(value = "stage", required = true) String stage
            , @ApiParam("工序")@RequestParam(value = "process", required = true) String process
            , @ApiParam("测试项目")@RequestParam(value = "checkItem", required = true) String checkItem
            , @ApiParam("开始时间")@RequestParam String startDate
            , @ApiParam("结束时间")@RequestParam String endDate
    ){

        String startTime = startDate + " 00:00:00";
        String endTime = endDate + " 23:59:59";
        //结果
        Map<String,Object> result = new HashMap<>();

        List<Map<String,String>> resultList = new ArrayList<>();
        Map<String,String> configResult = new HashMap<>();

        QueryWrapper<DfOrtStandardConfig> configQw = new QueryWrapper<>();
        configQw
                .eq("project", project)
                .eq("model", model)
                .eq("color", color)
                .eq( "stage", stage)
                .eq("process", process)
                .eq( "check_item", checkItem);

        List<DfOrtStandardConfig> configList = dfOrtStandardConfigService.list(configQw);

        if (configList == null || configList.size() == 0){
            return new Result(500, "当前条件下没有相关标准数据", result);
        }

        Map<String, String> configMap = new HashMap<>();
        for (DfOrtStandardConfig configData : configList) {
            String checkName = configData.getCheckName();
            String standardValue = configData.getStandardValue();
            configMap.put(checkName, standardValue);

            if ("Pass".equals(standardValue)){
                standardValue = "Pass/Fail";
            }else{
                standardValue = "≥ " + standardValue;
            }
            configResult.put(checkName, standardValue);
        }
        configResult.put("Summary", "OK/NG");

        //sql参数
        Map<String, Object> sqlParamMap = new HashMap<>();
        String selectSql = "batch, * ";
        String orderSql = "batch, id";



        QueryWrapper<DfOrtTestData> qw = new QueryWrapper<>();
        qw
                .eq("project", project)
                .eq("factory", factory)
                .eq("model", model)
                .eq( "color", color)
                .eq("stage", stage)
                .eq("process", process)
                .eq("check_item", checkItem)
                .between("check_time", startTime, endTime);

        List<Map<String, String>> list = dfOrtTestDataService.getBatchDataList(qw, sqlParamMap);
        if (list == null || list.size() == 0){
            return new Result(500, "当前条件下没有相关数据");
        }

        String cosmeticResult = "OK";
        String maskAreaResult = "OK";
        String arAreaResult = "OK";
        String irBsArAreaResult = "OK";
        String summaryResult = "OK";

        for (Map<String,String> data : list){
            String sample = data.get("string1");
            String cosmetic = data.get("string2");
            String maskArea = data.get("string3");
            String arArea = data.get("string4");
            String irBsArArea = data.get("string5");
            String summary = "OK";

            if (!configMap.get("Cosmetic").equalsIgnoreCase(cosmetic)){
                cosmeticResult = "NG";
                summary = "NG";
            }

            if (configMap.get("Mask area").compareTo(maskArea) > 0){
                maskAreaResult = "NG";
                summary = "NG";
            }

            if (configMap.get("AR area").compareTo(arArea) > 0){
                arAreaResult = "NG";
                summary = "NG";
            }

            if (configMap.get("IR+BS-AR area").compareTo(irBsArArea) > 0){
                irBsArAreaResult = "NG";
                summary = "NG";
            }

            if ("NG".equals(summary)){
                summaryResult = "NG";
            }

            Map<String,String> resultData = new HashMap<>();
            resultData.put("sample", sample);
            resultData.put("Cosmetic", cosmetic);
            resultData.put("Mask area", maskArea);
            resultData.put("AR area", arArea);
            resultData.put("IR+BS-AR area", irBsArArea);
            resultData.put("Summary", summary);

            resultList.add(resultData);
        }

        Map<String,String> resultCount = new HashMap<>();
        resultCount.put("sample", "Conclusion");
        resultCount.put("Cosmetic", cosmeticResult);
        resultCount.put("Mask area", maskAreaResult);
        resultCount.put("AR area", arAreaResult);
        resultCount.put("IR+BS-AR area", irBsArAreaResult);
        resultCount.put("Summary", summaryResult);
        resultList.add(resultCount);

        return new Result(200, "查询成功", resultList);
    }


    @GetMapping(value = "/getBaigeDetail")
    @ApiOperation("获取ORT百格明细")
    public Result getBaigeDetail(
            @ApiParam("项目")@RequestParam(value = "project", required = true) String project
            , @ApiParam("工厂")@RequestParam(value = "factory", required = true) String factory
            , @ApiParam("型号")@RequestParam(value = "model", required = true) String model
            , @ApiParam("颜色")@RequestParam(value = "color", required = true) String color
            , @ApiParam("阶段")@RequestParam(value = "stage", required = true) String stage
            , @ApiParam("工序")@RequestParam(value = "process", required = true) String process
            , @ApiParam("测试项目")@RequestParam(value = "checkItem", required = true) String checkItem
            , @ApiParam("开始时间")@RequestParam String startDate
            , @ApiParam("结束时间")@RequestParam String endDate
            , @ApiParam("样品号")@RequestParam(value = "sample", required = true) String sample
    ){
        String startTime = startDate + " 00:00:00";
        String endTime = endDate + " 23:59:59";

        QueryWrapper<DfOrtTestData> qw = new QueryWrapper<>();
        qw
                .eq("project", project)
                .eq("factory", factory)
                .eq("model", model)
                .eq( "color", color)
                .eq("stage", stage)
                .eq("process", process)
                .eq("check_item", checkItem)
                .between("check_time", startTime, endTime)
                .eq("string1", sample);

        DfOrtTestData data = dfOrtTestDataService.getOne(qw);
        if (data == null){
            return new Result(500, "当前条件下没有相关数据");
        }

        Map<String,String> result = new HashMap<>();
        result.put("sample", data.getString1());
        result.put("Cosmetic", data.getString2());
        result.put("Mask area", data.getString3());
        result.put("AR area", data.getString4());
        result.put("IR+BS-AR area", data.getString5());
        result.put("Cosmetic_img", data.getString6() != null && !"".equals(data.getString6()) ? data.getString6().replace("#imgPath#", env.getProperty("imgUrl")): "/");
        result.put("Mask area_img", data.getString7() != null && !"".equals(data.getString7()) ? data.getString7().replace("#imgPath#", env.getProperty("imgUrl")): "/");
        result.put("AR area_img", data.getString8() != null && !"".equals(data.getString8()) ? data.getString8().replace("#imgPath#", env.getProperty("imgUrl")): "/");
        result.put("IR+BS-AR area_img", data.getString9() != null && !"".equals(data.getString9()) ? data.getString9().replace("#imgPath#", env.getProperty("imgUrl")): "/");

        return new Result(200, "查询成功", result);
    }


}
