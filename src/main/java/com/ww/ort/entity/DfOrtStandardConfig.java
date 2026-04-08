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
* 
*/
@Data
@ApiModel("ORT标准配置")
public class DfOrtStandardConfig {

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
    * 测试项名称
    */
    @TableField("check_name")
    @ApiModelProperty("测试项名称")
    private String checkName;

    /**
    * 标准-最小值
    */
    @TableField("standard_min")
    @ApiModelProperty("标准-最小值")
    private Double standardMin;

    /**
    * 标准-值
    */
    @TableField("standard_value")
    @ApiModelProperty("标准-值")
    private String standardValue;

    /**
    * 标准-最大值
    */
    @TableField("standard_max")
    @ApiModelProperty("标准-最大值")
    private Double standardMax;

    /**
    * 标准值1
    */
    @TableField("value1")
    @ApiModelProperty("标准值1")
    private Double value1;

    /**
     * 标准值2
     */
    @TableField("value2")
    @ApiModelProperty("标准值2")
    private Double value2;

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