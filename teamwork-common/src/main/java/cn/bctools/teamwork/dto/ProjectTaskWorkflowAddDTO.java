package cn.bctools.teamwork.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 *
 * @author admin
 */
@ApiModel("项目任务流程规则新增")
@Data
public class ProjectTaskWorkflowAddDTO {
    @ApiModelProperty("规则名称")
    private String name;

    @ApiModelProperty("组织id")
    private String organizationId;

    @ApiModelProperty("项目id")
    private String projectId;

    @ApiModelProperty("流转规则对象")
    private ProjectTaskWorkflowRuleDTO rule;

    @ApiModelProperty("规则主键id ,用于编辑")
    private String id;
}
