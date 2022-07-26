package cn.bctools.teamwork.dto;


import cn.bctools.common.entity.dto.UserDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author admin
 */
@ApiModel("任务指派执行者 批量")
@Data
public class ProjectTaskAssignExecutorBatchDTO {
    @ApiModelProperty(value = "任务的id", required = true)
    private List<String> taskIds;
    @ApiModelProperty(value = "执行人员的id", required = true)
    private String executorId;
    @ApiModelProperty(value = "执行人员的信息", hidden = true)
    private UserDto executorInfo;
}
