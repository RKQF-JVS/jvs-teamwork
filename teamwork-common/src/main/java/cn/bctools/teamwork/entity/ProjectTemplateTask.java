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
@ApiModel("模板任务列表")
@EqualsAndHashCode(callSuper = false)
@TableName("project_template_task")
public class ProjectTemplateTask extends BaseEntityPO implements Serializable {

    private static final long serialVersionUID = 1L;
    @ApiModelProperty("模板任务id")
    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;
    @ApiModelProperty("模板任务名称")
    @TableField("name")
    private String name;
    @ApiModelProperty("模板任务排序")
    @TableField("sort")
    private Integer sort;
    @ApiModelProperty("所属模板id")
    @TableField("template_id")
    private String templateId;
    @ApiModelProperty("所属租户id")
    @TableField("tenant_id")
    private String tenantId;
}
