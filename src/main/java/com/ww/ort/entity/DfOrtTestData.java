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
* ORT测试数据
*/
@Data
@ApiModel("ORT测试数据")
public class DfOrtTestData {

    /**
    * id
    */
    @TableId(value = "id", type = IdType.AUTO)
    @ApiModelProperty("id")
    private Integer id;

    /**
    * 文件id
    */
    @TableField("file_id")
    @ApiModelProperty("文件id")
    private Integer fileId;

    /**
    * 工厂
    */
    @TableField("factory")
    @ApiModelProperty("工厂")
    private String factory;

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
    * 阶段
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
    * 测试时间
    */
    @TableField("check_time")
    @ApiModelProperty("测试时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Timestamp checkTime;

    /**
    * 测试日期
    */
    @TableField("check_date")
    @ApiModelProperty("测试日期")
    private String checkDate;

    /**
    * AB班
    */
    @TableField("day_or_night")
    @ApiModelProperty("AB班")
    private String dayOrNight;

    /**
    * 批次
    */
    @TableField("batch")
    @ApiModelProperty("批次")
    private String batch;

    /**
    * 字符串1
    */
    @TableField("string1")
    @ApiModelProperty("字符串1")
    private String string1;

    /**
    * 字符串2
    */
    @TableField("string2")
    @ApiModelProperty("字符串2")
    private String string2;

    /**
    * 字符串3
    */
    @TableField("string3")
    @ApiModelProperty("字符串3")
    private String string3;

    /**
    * 字符串4
    */
    @TableField("string4")
    @ApiModelProperty("字符串4")
    private String string4;

    /**
    * 字符串5
    */
    @TableField("string5")
    @ApiModelProperty("字符串5")
    private String string5;

    /**
    * 字符串6
    */
    @TableField("string6")
    @ApiModelProperty("字符串6")
    private String string6;

    /**
    * 字符串7
    */
    @TableField("string7")
    @ApiModelProperty("字符串7")
    private String string7;

    /**
    * 字符串8
    */
    @TableField("string8")
    @ApiModelProperty("字符串8")
    private String string8;

    /**
    * 字符串9
    */
    @TableField("string9")
    @ApiModelProperty("字符串9")
    private String string9;

    /**
    * 字符串10
    */
    @TableField("string10")
    @ApiModelProperty("字符串10")
    private String string10;

    /**
    * 测试值1
    */
    @TableField("value1")
    @ApiModelProperty("测试值1")
    private Double value1;

    /**
    * 测试值2
    */
    @TableField("value2")
    @ApiModelProperty("测试值2")
    private Double value2;

    /**
    * 测试值3
    */
    @TableField("value3")
    @ApiModelProperty("测试值3")
    private Double value3;

    /**
    * 测试值4
    */
    @TableField("value4")
    @ApiModelProperty("测试值4")
    private Double value4;

    /**
    * 测试值5
    */
    @TableField("value5")
    @ApiModelProperty("测试值5")
    private Double value5;

    /**
    * 测试值6
    */
    @TableField("value6")
    @ApiModelProperty("测试值6")
    private Double value6;

    /**
    * 测试值7
    */
    @TableField("value7")
    @ApiModelProperty("测试值7")
    private Double value7;

    /**
    * 测试值8
    */
    @TableField("value8")
    @ApiModelProperty("测试值8")
    private Double value8;

    /**
    * 测试值9
    */
    @TableField("value9")
    @ApiModelProperty("测试值9")
    private Double value9;

    /**
    * 测试值10
    */
    @TableField("value10")
    @ApiModelProperty("测试值10")
    private Double value10;

    /**
    * 测试值11
    */
    @TableField("value11")
    @ApiModelProperty("测试值11")
    private Double value11;

    /**
    * 测试值12
    */
    @TableField("value12")
    @ApiModelProperty("测试值12")
    private Double value12;

    /**
    * 测试值13
    */
    @TableField("value13")
    @ApiModelProperty("测试值13")
    private Double value13;

    /**
    * 测试值14
    */
    @TableField("value14")
    @ApiModelProperty("测试值14")
    private Double value14;

    /**
    * 测试值15
    */
    @TableField("value15")
    @ApiModelProperty("测试值15")
    private Double value15;

    /**
    * 测试值16
    */
    @TableField("value16")
    @ApiModelProperty("测试值16")
    private Double value16;

    /**
    * 测试值17
    */
    @TableField("value17")
    @ApiModelProperty("测试值17")
    private Double value17;

    /**
    * 测试值18
    */
    @TableField("value18")
    @ApiModelProperty("测试值18")
    private Double value18;

    /**
    * 测试值19
    */
    @TableField("value19")
    @ApiModelProperty("测试值19")
    private Double value19;

    /**
    * 测试值20
    */
    @TableField("value20")
    @ApiModelProperty("测试值20")
    private Double value20;

    /**
    * 测试值21
    */
    @TableField("value21")
    @ApiModelProperty("测试值21")
    private Double value21;

    /**
    * 测试值22
    */
    @TableField("value22")
    @ApiModelProperty("测试值22")
    private Double value22;

    /**
    * 测试值23
    */
    @TableField("value23")
    @ApiModelProperty("测试值23")
    private Double value23;

    /**
    * 测试值24
    */
    @TableField("value24")
    @ApiModelProperty("测试值24")
    private Double value24;

    /**
    * 测试值25
    */
    @TableField("value25")
    @ApiModelProperty("测试值25")
    private Double value25;

    /**
    * 测试值26
    */
    @TableField("value26")
    @ApiModelProperty("测试值26")
    private Double value26;

    /**
    * 测试值27
    */
    @TableField("value27")
    @ApiModelProperty("测试值27")
    private Double value27;

    /**
    * 测试值28
    */
    @TableField("value28")
    @ApiModelProperty("测试值28")
    private Double value28;

    /**
    * 测试值29
    */
    @TableField("value29")
    @ApiModelProperty("测试值29")
    private Double value29;

    /**
    * 测试值30
    */
    @TableField("value30")
    @ApiModelProperty("测试值30")
    private Double value30;

    /**
    * 测试值31
    */
    @TableField("value31")
    @ApiModelProperty("测试值31")
    private Double value31;

    /**
    * 测试值32
    */
    @TableField("value32")
    @ApiModelProperty("测试值32")
    private Double value32;

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