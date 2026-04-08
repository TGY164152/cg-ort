package com.ww.ort.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ww.ort.entity.DfOrtStandardConfig;
import com.ww.ort.entity.DfOrtTestItemImportConfig;
import com.ww.ort.service.DfOrtTestItemImportConfigService;
import com.ww.ort.utils.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import org.springframework.stereotype.Controller;

/**
 * <p>
 * ORT导入配置 前端控制器
 * </p>
 *
 * @author TGY
 * @since 2025-10-11
 */
@Controller
@RequestMapping("/dfOrtTestItemImportConfig")
@Api(tags = "ORT导入配置")
@ResponseBody
@CrossOrigin
public class DfOrtTestItemImportConfigController {

    @Autowired
    private DfOrtTestItemImportConfigService dfOrtTestItemImportConfigService;

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
        IPage<DfOrtTestItemImportConfig> pages = new Page<>(page,limit);
        QueryWrapper<DfOrtTestItemImportConfig> qw = new QueryWrapper<>();

        qw
                .eq(StringUtils.isNotBlank(process),"process",process)
                .eq(StringUtils.isNotBlank(checkItem),"check_item",checkItem)
                .orderByDesc("id");

        IPage<DfOrtTestItemImportConfig> list = dfOrtTestItemImportConfigService.page(pages,qw);
        return new Result(0,"查询成功",list.getRecords(),(int)list.getTotal());
    }

    /**
     * 添加或修改
     * @param data
     * @return
     */
    @PostMapping("/saveOrUpdate")
    @ApiOperation("添加或修改数据")
    public Result saveOrUpdate(@RequestBody DfOrtTestItemImportConfig data){
        if (data.getId()!=null){
            if (dfOrtTestItemImportConfigService.updateById(data)){
                return new Result(200,"修改成功");
            }
            return new Result(500,"修改失败");
        }else {
            if (dfOrtTestItemImportConfigService.save(data)){
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
        if (dfOrtTestItemImportConfigService.removeById(id)){
            return new Result(200,"删除成功");
        }
        return new Result(500,"删除失败");
    }
}
