package com.ww.ort.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ww.ort.entity.DfOrtProcess;
import com.ww.ort.service.DfOrtProcessService;
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
 * ORT工序 前端控制器
 * </p>
 *
 * @author TGY
 * @since 2025-11-03
 */
@Controller
@RequestMapping("/dfOrtProcess")
@Api(tags = "ORT工序")
@ResponseBody
@CrossOrigin
public class DfOrtProcessController {

    @Autowired
    private DfOrtProcessService dfOrtProcessService;

    /**
     * 获取列表
     * @param page
     * @param limit
     * @param makeProcess
     * @param checkType
     * @param process
     * @return
     */
    @GetMapping("/listBySearch")
    @ApiOperation("获取列表")
    public Result listBySearch(
            @ApiParam("页码")@RequestParam(value = "page", required = true) Integer page
            , @ApiParam("条数")@RequestParam(value = "limit", required = true) Integer limit
            , @ApiParam("制程")@RequestParam(value = "makeProcess", required = false) String makeProcess
            , @ApiParam("类型")@RequestParam(value = "checkType", required = false) String checkType
            , @ApiParam("工序")@RequestParam(value = "process", required = false) String process
    ){
        IPage<DfOrtProcess> pages = new Page<>(page,limit);

        QueryWrapper<DfOrtProcess> qw = new QueryWrapper<>();

        qw
                .eq(StringUtils.isNotBlank(makeProcess),"make_process",makeProcess)
                .eq(StringUtils.isNotBlank(checkType),"check_type",checkType)
                .eq(StringUtils.isNotBlank(process),"process",process)
                .orderByDesc("id");

        IPage<DfOrtProcess> list = dfOrtProcessService.page(pages,qw);
        return new Result(0,"查询成功",list.getRecords(),(int)list.getTotal());
    }

    /**
     * 添加或修改
     * @param data
     * @return
     */
    @PostMapping("/saveOrUpdate")
    @ApiOperation("添加或修改数据")
    public Result saveOrUpdate(@RequestBody DfOrtProcess data){
        if (data.getId()!=null){
            if (dfOrtProcessService.updateById(data)){
                return new Result(200,"修改成功");
            }
            return new Result(500,"修改失败");
        }else {
            if (dfOrtProcessService.save(data)){
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
        if (dfOrtProcessService.removeById(id)){
            return new Result(200,"删除成功");
        }
        return new Result(500,"删除失败");
    }


    /**
     * 获取全部列表
     * @param makeProcess
     * @param checkType
     * @param process
     * @return
     */
    @GetMapping("/listAll")
    @ApiOperation("获取全部列表")
    public Result listAll(
            @ApiParam("制程")@RequestParam(value = "makeProcess", required = false) String makeProcess
            , @ApiParam("类型")@RequestParam(value = "checkType", required = false) String checkType
            , @ApiParam("工序")@RequestParam(value = "process", required = false) String process
    ){
        QueryWrapper<DfOrtProcess> qw = new QueryWrapper<>();

        qw
                .eq(StringUtils.isNotBlank(makeProcess),"make_process",makeProcess)
                .eq(StringUtils.isNotBlank(checkType),"check_type",checkType)
                .eq(StringUtils.isNotBlank(process),"process",process)
                .orderByAsc("sort");

        List<DfOrtProcess> list = dfOrtProcessService.list(qw);
        return new Result(0,"查询成功",list);
    }
}
