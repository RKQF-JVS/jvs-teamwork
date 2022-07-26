package cn.bctools.teamwork.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 任务完成状态的改变
 * @author admin
 */
@Data
@ApiModel("任务完成状态")
public class ProjectTaskDoneDTO {

    @ApiModelProperty(value = "任务主键id",required = true)
    private String taskId;

    @ApiModelProperty(value = "任务的完成状态改变 0-未完成 1-已完成",required = true)
    private Integer finishStatus;

}
