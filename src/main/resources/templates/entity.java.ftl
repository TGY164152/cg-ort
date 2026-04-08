package ${package.Entity};

<#if entityLombokModel>
import lombok.Data;
</#if>
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.IdType;
import java.sql.Timestamp;

/**
* ${table.comment}
*/
<#if entityLombokModel>
@Data
</#if>
@ApiModel("${table.comment}")
public class ${entity} {

<#list table.fields as field>
    <#if field.comment??>
    /**
    * ${field.comment}
    */
    </#if>
    <#-- 如果是主键id，添加@TableId注解 -->
    <#if field.name == "id">
    @TableId(value = "id", type = IdType.AUTO)
    <#else>
    @TableField("${field.name}")
    </#if>
    @ApiModelProperty("${field.comment!field.name}")
    <#-- 判断如果是datetime字段，添加@JsonFormat -->
    <#if field.propertyType == "LocalDateTime">
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Timestamp ${field.propertyName};
    <#else>
    private ${field.propertyType} ${field.propertyName};
    </#if>

</#list>
}