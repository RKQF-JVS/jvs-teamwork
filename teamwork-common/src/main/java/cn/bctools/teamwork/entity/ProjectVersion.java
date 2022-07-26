package cn.bctools.teamwork.entity;

import cn.bctools.teamwork.common.po.BaseEntityPO;
import cn.hutool.core.date.DatePattern;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author Auto Generator
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@ApiModel("版本库版本")
@EqualsAndHashCode(callSuper = false)
@TableName("project_version")
public class ProjectVersion extends BaseEntityPO implements Serializable {

    private static final long serialVersionUID = 1L;
    @ApiModelProperty("主键id")
    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;
    @ApiModelProperty("版本库名称")
    @TableField("name")
    private String name;
    @ApiModelProperty("描述")
    @TableField("description")
    private String description;
    @ApiModelProperty("组织id")
    @TableField("organization_id")
    private String organizationId;
    @ApiModelProperty("实际发布时间")
    @TableField("publish_time")
    @DateTimeFormat(pattern = DatePattern.NORM_DATETIME_PATTERN)
    @JsonFormat(pattern = DatePattern.NORM_DATETIME_PATTERN, timezone = "GMT+8")
    private LocalDateTime publishTime;
    @ApiModelProperty("开始时间")
    @TableField("start_time")
    @DateTimeFormat(pattern = DatePattern.NORM_DATETIME_PATTERN)
    @JsonFormat(pattern = DatePattern.NORM_DATETIME_PATTERN, timezone = "GMT+8")
    private LocalDateTime startTime;
    @ApiModelProperty("状态。0：未开始，1：进行中，2：延期发布，3：已发布")
    @TableField("status")
    private Integer status;
    @ApiModelProperty("进度百分比")
    @TableField("schedule")
    private Integer schedule;
    @ApiModelProperty("预计发布时间")
    @TableField("plan_publish_time")
    @DateTimeFormat(pattern = DatePattern.NORM_DATETIME_PATTERN)
    @JsonFormat(pattern = DatePattern.NORM_DATETIME_PATTERN, timezone = "GMT+8")
    private LocalDateTime planPublishTime;
    @ApiModelProperty("版本库id")
    @TableField("features_id")
    private String featuresId;
    @ApiModelProperty("所属租户id")
    @TableField("tenant_id")
    private String tenantId;

    @ApiModelProperty("状态 文字展示")
    @TableField(exist = false)
    private String statusText;

}
