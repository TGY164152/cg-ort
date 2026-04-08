package com.ww.ort.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ww.ort.entity.DfOrtExperStandConfig;
import com.ww.ort.entity.DfOrtStandardConfig;
import com.ww.ort.service.DfOrtStandardConfigService;
import com.ww.ort.utils.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import org.springframework.stereotype.Controller;

import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author zhao
 * @since 2023-06-19
 */
@Controller
@RequestMapping("/dfOrtStandardConfig")
@Api(tags = "ORT标准配置")
@ResponseBody
@CrossOrigin
public class DfOrtStandardConfigController {

    @Autowired
    private DfOrtStandardConfigService dfOrtStandardConfigService;

    /**
     * 获取列表
     * @param page
     * @param limit
     * @param process
     * @param checkItem
     * @return
     */
    @GetMapping("/listBySearch")
    @ApiOperation("获取列表")
    public Result listBySearch(
            @ApiParam("页码")@RequestParam(value = "page", required = true) Integer page
            , @ApiParam("条数")@RequestParam(value = "limit", required = true) Integer limit
            , @ApiParam("型号")@RequestParam(value = "project", required = false) String project
            , @ApiParam("颜色")@RequestParam(value = "color", required = false) String color
            , @ApiParam("阶段")@RequestParam(value = "stage", required = false) String stage
            , @ApiParam("工序")@RequestParam(value = "process", required = false) String process
            , @ApiParam("测试项")@RequestParam(value = "checkItem", required = false) String checkItem
    ){
        IPage<DfOrtStandardConfig> pages = new Page<>(page,limit);

        QueryWrapper<DfOrtStandardConfig> qw = new QueryWrapper<>();

        qw
                .eq(StringUtils.isNotBlank(project),"project",project)
                .eq(StringUtils.isNotBlank(color),"color",color)
                .eq(StringUtils.isNotBlank(stage),"stage",stage)
                .eq(StringUtils.isNotBlank(process),"process",process)
                .eq(StringUtils.isNotBlank(checkItem),"check_item",checkItem)
                .orderByDesc("id");

        IPage<DfOrtStandardConfig> list = dfOrtStandardConfigService.page(pages,qw);
        return new Result(0,"查询成功",list.getRecords(),(int)list.getTotal());
    }

    /**
     * 添加或修改
     * @param data
     * @return
     */
    @PostMapping("/saveOrUpdate")
    @ApiOperation("添加或修改数据")
    public Result saveOrUpdate(@RequestBody DfOrtStandardConfig data){
        if (data.getId()!=null){
            if (dfOrtStandardConfigService.updateById(data)){
                return new Result(200,"修改成功");
            }
            return new Result(500,"修改失败");
        }else {
            if (dfOrtStandardConfigService.save(data)){
                return new Result(200,"添加成功");
            }
            return new Result(500,"添加失败");
        }
    }

    /**
     * 删除
     * @param id
     * @return
     */
    @GetMapping("/delete")
    @ApiOperation("删除")
    public Result delete(Integer id){
        if (dfOrtStandardConfigService.removeById(id)){
            return new Result(200,"删除成功");
        }
        return new Result(500,"删除失败");
    }

    /**
     * 获取列表
     * @param process
     * @param checkItem
     * @return
     */
    @GetMapping("/getCheckNameList")
    @ApiOperation("获取测试名称列表")
    public Result getCheckNameList(
            @ApiParam("工序")@RequestParam(required = true) String process
            , @ApiParam("测试项")@RequestParam(required = true) String checkItem
    ){
        QueryWrapper<DfOrtStandardConfig> qw = new QueryWrapper<>();

        qw
                .select("check_name")
                .eq(StringUtils.isNotBlank(process),"process",process)
                .eq(StringUtils.isNotBlank(checkItem),"check_item",checkItem)
                .groupBy("check_name")
                .orderByAsc("check_name");

        List<DfOrtStandardConfig> list = dfOrtStandardConfigService.list(qw);
        return new Result(0,"查询成功",list);
    }

}
