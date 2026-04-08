package com.ww.ort.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ww.ort.entity.DfOrtTestItemImportConfig;
import com.ww.ort.entity.DfOrtTestItemUrl;
import com.ww.ort.service.DfOrtTestItemUrlService;
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
 * ORT大屏页面配置 前端控制器
 * </p>
 *
 * @author zhao
 * @since 2023-06-28
 */
@Controller
@RequestMapping("/dfOrtTestItemUrl")
@Api(tags = "ORT大屏页面配置")
@ResponseBody
@CrossOrigin
public class DfOrtTestItemUrlController {

    @Autowired
    private DfOrtTestItemUrlService dfOrtTestItemUrlService;

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
            , @ApiParam("工序")@RequestParam(value = "process", required = false) String process
            , @ApiParam("测试项")@RequestParam(value = "checkItem", required = false) String checkItem
    ){
        IPage<DfOrtTestItemUrl> pages = new Page<>(page,limit);

        QueryWrapper<DfOrtTestItemUrl> qw = new QueryWrapper<>();

        qw
                .eq(StringUtils.isNotBlank(process),"process",process)
                .eq(StringUtils.isNotBlank(checkItem),"check_item",checkItem)
                .orderByDesc("id");

        IPage<DfOrtTestItemUrl> list = dfOrtTestItemUrlService.page(pages,qw);
        return new Result(0,"查询成功",list.getRecords(),(int)list.getTotal());
    }

    /**
     * 添加或修改
     * @param data
     * @return
     */
    @PostMapping("/saveOrUpdate")
    @ApiOperation("添加或修改数据")
    public Result saveOrUpdate(@RequestBody DfOrtTestItemUrl data){
        if (data.getId()!=null){
            if (dfOrtTestItemUrlService.updateById(data)){
                return new Result(200,"修改成功");
            }
            return new Result(500,"修改失败");
        }else {
            if (dfOrtTestItemUrlService.save(data)){
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
        if (dfOrtTestItemUrlService.removeById(id)){
            return new Result(200,"删除成功");
        }
        return new Result(500,"删除失败");
    }

    @GetMapping("getBigScreenProcess")
    @ApiOperation("获取大屏工序")
    public Result getBigScreenProcess() {
        QueryWrapper<DfOrtTestItemUrl> qw = new QueryWrapper<>();
        qw
                .eq("is_use", 1);
        List<DfOrtTestItemUrl> list = dfOrtTestItemUrlService.getBigScreenProcess(qw);
        return new Result(200, "查询成功", list);
    }

    @GetMapping("getBigScreenTestItem")
    @ApiOperation("获取大屏测试项目")
    public Result getBigScreenTestItem(String process) {
        QueryWrapper<DfOrtTestItemUrl> qw = new QueryWrapper<>();
        qw
                .eq("process", process)
                .eq("is_use", 1)
                .orderByAsc("sort");
        List<DfOrtTestItemUrl> list = dfOrtTestItemUrlService.list(qw);
        return new Result(200, "查询成功", list);
    }
}
