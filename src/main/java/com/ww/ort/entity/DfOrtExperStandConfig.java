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
* ORT强度标准值配置
*/
@Data
@ApiModel("ORT强度标准值配置")
public class DfOrtExperStandConfig {

    /**
    * id
    */
    @TableId(value = "id", type = IdType.AUTO)
    @ApiModelProperty("id")
    private Integer id;

    /**
    * 型号
    */
    @TableField("project")
    @ApiModelProperty("型号")
    private String project;

    /**
    * 颜色
    */
    @TableField("color")
    @ApiModelProperty("颜色")
    private String color;

    /**
    * 成品 / 白片
    */
    @TableField("stage")
    @ApiModelProperty("阶段")
    private String stage;

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
    * 标准项
    */
    @TableField("standard_item")
    @ApiModelProperty("标准项")
    private String standardItem;

    /**
    * 标准值
    */
    @TableField("standard_value")
    @ApiModelProperty("标准值")
    private Double standardValue;

    /**
    * 创建时间
    */
    @TableField("create_time")
    @ApiModelProperty("创建时间")
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