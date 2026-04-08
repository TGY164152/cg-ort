package com.ww.ort.entity;

import lombok.Data;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.IdType;
import java.sql.Timestamp;

/**
* ORT工序-测试项
*/
@Data
@ApiModel("ORT工序-测试项")
public class DfOrtProcessItem {

    /**
    * id
    */
    @TableId(value = "id", type = IdType.AUTO)
    @ApiModelProperty("id")
    private Integer id;

    /**
    * 工序
    */
    @TableField("process")
    @ApiModelProperty("工序")
    private String process;

    /**
    * 测试项
    */
    @TableField("check_item")
    @ApiModelProperty("测试项")
    private String checkItem;

    /**
    * 排序
    */
    @TableField("sort")
    @ApiModelProperty("排序")
    private Integer sort;

    /**
    * 0关 1开
    */
    @TableField("is_use")
    @ApiModelProperty("0关 1开")
    private Integer isUse;

    /**
    * 添加时间
    */
    @TableField("create_time")
    @ApiModelProperty("添加时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Timestamp createTime;

    /**
    * 修改时间
    */
    @TableField("update_time")
    @ApiModelProperty("修改时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Timestamp updateTime;

}