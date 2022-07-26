package cn.bctools.teamwork.dto;

import cn.bctools.common.entity.dto.UserDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author admin
 */
@ApiModel("任务指派执行者")
@Data
public class ProjectTaskAssignExecutorDTO {
    @ApiModelProperty(value = "任务的id", required = true)
    private String taskId;
    @ApiModelProperty("执行人员的id 不传则变更为待认领")
    private String executorId;
    @ApiModelProperty(value = "执行人员的信息", hidden = true)
    private UserDto executorInfo;
}
