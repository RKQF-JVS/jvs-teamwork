package cn.bctools.teamwork.dto;

import cn.bctools.common.entity.dto.UserDto;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 项目任务添加类
 *
 * @author admin
 */
@Data
public class ProjectTaskAddDTO {

    @ApiModelProperty(value = "任务名称", required = true)
    private String name;

    @ApiModelProperty(value = "项目id", required = true)
    private String projectId;

    @ApiModelProperty(value = "父任务id 默认请传-1", required = true)
    private String pid;

    @ApiModelProperty(value = "任务执行者id", required = true)
    private String executeMemberId;

    @ApiModelProperty(value = "任务列表id", required = true)
    private String mouldId;

    @ApiModelProperty(value = "任务执行者信息", hidden = true)
    private UserDto executeMember;
}
