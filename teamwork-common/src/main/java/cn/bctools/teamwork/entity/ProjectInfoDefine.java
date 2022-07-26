package cn.bctools.teamwork.entity;

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
@ApiModel("项目自定义信息表")
@EqualsAndHashCode(callSuper = false)
@TableName("project_info_define")
public class ProjectInfoDefine extends BaseEntityPO implements Serializable {

    private static final long serialVersionUID = 1L;
    @ApiModelProperty("id")
    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;
    @ApiModelProperty("名称")
    @TableField("name")
    private String name;
    @ApiModelProperty("内容")
    @TableField("value")
    private String value;
    @ApiModelProperty("描述")
    @TableField("description")
    private String description;
    @ApiModelProperty("组织id")
    @TableField("organization_id")
    private String organizationId;
    @ApiModelProperty("项目id")
    @TableField("project_id")
    private String projectId;
    @ApiModelProperty("所属租户id")
    @TableField("tenant_id")
    private String tenantId;
}
