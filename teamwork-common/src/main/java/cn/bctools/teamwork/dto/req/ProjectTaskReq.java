package cn.bctools.teamwork.dto.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 查询任务列表的任务
 *
 * @author admin
 */
@Data
@ApiModel("查询任务列表的任务 参数")
public class ProjectTaskReq {
    @ApiModelProperty(value = "任务列表id")
    private String mouldId;

    @ApiModelProperty("任务标题")
    private String name;


    @ApiModelProperty("任务执行者 多选使用逗号隔开")
    private String executeMemberId;


    @ApiModelProperty("任务参与者ID 多选使用逗号隔开")
    private String taskMemberId;

    @ApiModelProperty("任务创建者ID 多选使用逗号隔开")
    private String createById;

    @ApiModelProperty("任务是否完成")
    private Integer finishStatus;

    @ApiModelProperty("优先级 使用逗号隔开 例如 1,2")
    private String priorityLevel;

    @ApiModelProperty("执行状态 使用逗号隔开 例如 1,2")
    private String executeStatus;

    @ApiModelProperty("截止日期 使用逗号隔开 例如 2020-11-11,2021-11-11")
    private String endTime;

    @ApiModelProperty("创建日期 使用逗号隔开 例如 2020-11-11,2021-11-11")
    private String createTime;

    @ApiModelProperty("完成日期 使用逗号隔开 例如 2020-11-11,2021-11-11")
    private String doneTime;

    @ApiModelProperty("开始日期 使用逗号隔开 例如 2020-11-11,2021-11-11")
    private String startTime;

    @ApiModelProperty(value = "项目id", required = true)
    private String projectId;

    @ApiModelProperty(value = "展示方式 1默认展示方式 2看板", required = true)
    private Integer type;
}
