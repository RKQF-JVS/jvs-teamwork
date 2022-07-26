package cn.bctools.teamwork.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 任务移动参数
 * @author admin
 */
@Data
@ApiModel("任务移动所需参数")
public class TaskMoveDTO {
    @ApiModelProperty(value = "项目id",required = true)
    private String projectId;

    @ApiModelProperty(value = "列表id",required = true)
    private String mouldId;

    @ApiModelProperty(value = "任务id",required = true)
    private String taskId;
}
