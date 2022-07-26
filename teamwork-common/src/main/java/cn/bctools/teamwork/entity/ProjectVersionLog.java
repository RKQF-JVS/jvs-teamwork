package cn.bctools.teamwork.entity;

import cn.bctools.common.entity.dto.UserDto;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import cn.bctools.teamwork.common.po.BaseEntityPO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author Auto Generator
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@ApiModel("项目版本日志表")
@EqualsAndHashCode(callSuper = false)
@TableName("project_version_log")
public class ProjectVersionLog extends BaseEntityPO implements Serializable {

    private static final long serialVersionUID = 1L;
    @ApiModelProperty("主键id")
    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;
    @ApiModelProperty("操作人id")
    @TableField("member_id")
    private String memberId;
    @ApiModelProperty("操作内容")
    @TableField("content")
    private String content;
    @ApiModelProperty("日志描述")
    @TableField("remark")
    private String remark;
    @ApiModelProperty("版本库版本id")
    @TableField("source_id")
    private String sourceId;
    @ApiModelProperty("项目id")
    @TableField("project_id")
    private String projectId;
    @ApiModelProperty("图标")
    @TableField("icon")
    private String icon;
    @ApiModelProperty("版本库id")
    @TableField("features_id")
    private String featuresId;
    @ApiModelProperty("动态类型")
    @TableField("type")
    private String type;
    @ApiModelProperty("所属租户id")
    @TableField("tenant_id")
    private String tenantId;

    @ApiModelProperty("操作人")
    @TableField(exist = false)
    private UserDto member;
}
