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
* ORT导入配置
*/
@Data
@ApiModel("ORT导入配置")
public class DfOrtTestItemImportConfig {

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
    * 测试值名称
    */
    @TableField("check_item")
    @ApiModelProperty("测试值名称")
    private String checkItem;

    /**
     * 测试值注释
     */
    @TableField("check_name")
    @ApiModelProperty("测试值名称")
    private String checkName;

    /**
     * 测试值字段
     */
    @TableField("check_code")
    @ApiModelProperty("测试值字段")
    private String checkCode;

    /**
     * 字段类型
     */
    @TableField("field_type")
    @ApiModelProperty("字段类型")
    private String fieldType;

    /**
    * 行列
    */
    @TableField("row_col")
    @ApiModelProperty("行列")
    private String rowCol;

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