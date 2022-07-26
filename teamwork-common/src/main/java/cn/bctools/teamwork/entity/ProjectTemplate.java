package cn.bctools.teamwork.entity;

import cn.bctools.teamwork.common.po.BaseEntityPO;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * @author Auto Generator
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@ApiModel("项目-模板表")
@EqualsAndHashCode(callSuper = false)
@TableName("project_template")
public class ProjectTemplate extends BaseEntityPO implements Serializable {

    private static final long serialVersionUID = 1L;
    @ApiModelProperty("模板主键id")
    @TableField("id")
    private String id;
    @ApiModelProperty("模板名称")
    @TableField("template_name")
    private String templateName;
    @ApiModelProperty("模板说明")
    @TableField("description")
    private String description;
    @ApiModelProperty("封面图片文件名字")
    @TableField("template_cover")
    private String templateCover;
    @ApiModelProperty("模板编号")
    @TableField("template_code")
    private String templateCode;
    @ApiModelProperty("组织id")
    @TableField("organization_code")
    private String organizationCode;
    @ApiModelProperty("系统默认模板")
    @TableField("is_system")
    private Integer isSystem;
    @ApiModelProperty("所属租户id")
    @TableField("tenant_id")
    private String tenantId;
    @ApiModelProperty("逻辑删除标识 0-未删除 1-删除")
    @TableField("del_flag")
    private Integer delFlag;
    @ApiModelProperty("模板任务列表名字集合")
    @TableField(exist = false)
    List<ProjectTemplateTask> list;
    @ApiModelProperty("封面图片文件名字")
    @TableField(exist = false)
    private String templateCoverUrl;
}
