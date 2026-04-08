package com.ww.ort.entity;

import io.swagger.annotations.Api;
import lombok.Data;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.IdType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ResponseBody;

import java.sql.Timestamp;

/**
* ORT文件路径
*/
@Data
@ApiModel("ORT文件路径")
public class DfOrtFileUrl {

    /**
    * id
    */
    @TableId(value = "id", type = IdType.AUTO)
    @ApiModelProperty("id")
    private Integer id;

    /**
    * 项目
    */
    @TableField("big_project")
    @ApiModelProperty("项目")
    private String bigProject;

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

    @TableField("check_date")
    @ApiModelProperty("测试日期")
    private String checkDate;


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
    * 文件路径
    */
    @TableField("file_url")
    @ApiModelProperty("文件路径")
    private String fileUrl;

    /**
    * 创建人
    */
    @TableField("create_user")
    @ApiModelProperty("创建人")
    private String createUser;

    /**
    * 创建人工号
    */
    @TableField("create_username")
    @ApiModelProperty("创建人工号")
    private String createUsername;

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