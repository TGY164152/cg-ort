package com.ww.ort.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.io.Serializable;

/**
 * <p>
 * ORT-IR透过率
 * </p>
 *
 * @author zhao
 * @since 2024-12-31
 */
@Data
@ApiModel("ORT-IR透过率")
public class DfOrtIrThrough extends Model<DfOrtIrThrough> {

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
    @TableField("model")
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
     * 测试项目
     */
    @ApiModelProperty("测试项目")
    private String testProject;

    /**
     * 测试时间，用来筛选
     */
    @ApiModelProperty("测试时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Timestamp testTime;

    /**
     * 白夜班
     */
    @ApiModelProperty("白夜班")
    private String dayOrNight;

    /**
     * 批次，一天为一批
     */
    @ApiModelProperty("批次，一天为一批")
    private String batch;

    /**
     * Avg（400~680）nm
     */
    @ApiModelProperty("Avg（400~680）nm")
    private Double avgValue;

    /**
     * Avg（400~680）测试结果
     */
    @ApiModelProperty("Avg（400~680）测试结果")
    private String avgResult;

    /**
     * Max（680~730）nm
     */
    @ApiModelProperty("Max（680~730）nm")
    private Double maxValue;

    /**
     * Max（680~730）测试结果
     */
    @ApiModelProperty("Max（680~730）测试结果")
    private String maxResult;

    /**
     * Avg（920~960）nm
     */
    @ApiModelProperty("Avg（920~960）nm")
    private Double avgValueTwo;

    /**
     * Avg（920~960）nm测试结果
     */
    @ApiModelProperty("Avg（920~960）nm测试结果")
    private String avgResultTwo;

    /**
     * Min（920~960）nm
     */
    @ApiModelProperty("Min（920~960）nm")
    private Double minValue;

    /**
     * Min（920~960）nm测试结果
     */
    @ApiModelProperty("Min（920~960）nm测试结果")
    private String minResult;

    /**
     * 结果
     */
    @ApiModelProperty("结果")
    private String checkResult;

    /**
     * 添加时间
     */
    @ApiModelProperty("添加时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Timestamp createTime;

    @Override
    protected Serializable pkVal() {
        return this.id;
    }

    @Override
    public String toString() {
        return "DfOrtIrThrough{" +
            "id=" + id +
            ", factory=" + factory +
            ", model=" + model +
            ", color=" + color +
            ", productionPhase=" + productionPhase +
            ", process=" + process +
            ", testProject=" + testProject +
            ", testTime=" + testTime +
            ", dayOrNight=" + dayOrNight +
            ", batch=" + batch +
            ", avgValue=" + avgValue +
            ", avgResult=" + avgResult +
            ", maxValue=" + maxValue +
            ", maxResult=" + maxResult +
            ", avgValueTwo=" + avgValueTwo +
            ", avgResultTwo=" + avgResultTwo +
            ", minValue=" + minValue +
            ", minResult=" + minResult +
            ", checkResult=" + checkResult +
            ", createTime=" + createTime +
        "}";
    }
}
