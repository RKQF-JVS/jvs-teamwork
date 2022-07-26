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
@ApiModel("版本库表")
@EqualsAndHashCode(callSuper = false)
@TableName("project_features")
public class ProjectFeatures extends BaseEntityPO implements Serializable {

    private static final long serialVersionUID = 1L;
    @ApiModelProperty("主键")
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
    @ApiModelProperty("项目id")
    @TableField("project_id")
    private String projectId;
    @TableField("tenant_id")
    private String tenantId;
}
