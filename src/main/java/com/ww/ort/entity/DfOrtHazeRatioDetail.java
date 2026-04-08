package com.ww.ort.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.baomidou.mybatisplus.annotation.TableId;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 * 雾度测量详情
 * </p>
 *
 * @author zhao
 * @since 2024-12-24
 */
@Data
@ApiModel("雾度")
public class DfOrtHazeRatioDetail extends Model<DfOrtHazeRatioDetail> {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 工厂
     */
    @ApiModelProperty("工厂")
    private String factory;

    /**
     * 项目
     */
    @ApiModelProperty("项目")
    private String model;

    /**
     * 颜色
     */
    @ApiModelProperty("颜色")
    private String color;

    /**
     * 生产阶段
     */
    @ApiModelProperty("生产阶段")
    private String productionPhase;

    /**
     * 工序
     */
    @ApiModelProperty("工序")
    private String process;

    /**
     * 测试 IR雾度等测试
     */
    @ApiModelProperty("测试项目")
    private String testProject;

    /**
     * 测试时间，用来筛选
     */
    @ApiModelProperty("测试时间")
    private Timestamp testTime;

    /**
     * 白夜班
     */
    @ApiModelProperty("白夜班")
    private String dayOrNight;

    /**
     * 批次，一天为一批
     */
    @ApiModelProperty("批次")
    private String batch;

    /**
     * rx
     */
    @ApiModelProperty("rx")
    private Double rx;

    @ApiModelProperty("rx测试结果")
    private String rxResult;

    @ApiModelProperty("px")
    private Double px;

    @ApiModelProperty("px测试结果")
    private String pxResult;

    @ApiModelProperty("tx")
    private Double tx;

    @ApiModelProperty("tx测试结果")
    private String txResult;

    @ApiModelProperty("结果")
    private String checkResult;

    @ApiModelProperty("创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Timestamp createTime;


    @Override
    protected Serializable pkVal() {
        return this.id;
    }

    @Override
    public String toString() {
        return "DfOrtHazeRatioDetail{" +
                "id=" + id +
                ", factory='" + factory + '\'' +
                ", model='" + model + '\'' +
                ", color='" + color + '\'' +
                ", productionPhase='" + productionPhase + '\'' +
                ", process='" + process + '\'' +
                ", testProject='" + testProject + '\'' +
                ", testTime=" + testTime +
                ", dayOrNight='" + dayOrNight + '\'' +
                ", batch='" + batch + '\'' +
                ", rx=" + rx +
                ", rxResult='" + rxResult + '\'' +
                ", px=" + px +
                ", pxResult='" + pxResult + '\'' +
                ", tx=" + tx +
                ", txResult='" + txResult + '\'' +
                ", checkResult='" + checkResult + '\'' +
                ", createTime=" + createTime +
                '}';
    }
}
