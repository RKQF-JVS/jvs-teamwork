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
@ApiModel("任务工作流流转规则表")
@EqualsAndHashCode(callSuper = false)
@TableName("project_task_workflow_rule")
public class ProjectTaskWorkflowRule extends BaseEntityPO implements Serializable {

    private static final long serialVersionUID = 1L;
    @ApiModelProperty("主键id")
    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;
    @ApiModelProperty("规则类型，0：任务分组，1：人员，2：条件 3：结果状态")
    @TableField("type")
    private Integer type;
    @ApiModelProperty("项目任务模板id或者指派的人员id")
    @TableField("object_id")
    private String objectId;
    @ApiModelProperty("场景。0：增加任务/任何条件，1：被完成，2：被重做，3：设置执行人，4：截止时间，5：优先级  /或 -1 -未修改 1-已完成 2-未完成")
    @TableField("action")
    private Integer action;
    @ApiModelProperty("规则信息id")
    @TableField("workflow_id")
    private String workflowId;
    @ApiModelProperty("排序")
    @TableField("sort")
    private Integer sort;
    @ApiModelProperty("所属租户id")
    @TableField("tenant_id")
    private String tenantId;
}
