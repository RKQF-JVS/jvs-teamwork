package cn.bctools.teamwork.dto;

import cn.bctools.common.entity.dto.UserDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 任务复制参数
 * @author admin
 */
@Data
@ApiModel("任务复制所需参数")
public class TaskCopyDTO {

    @ApiModelProperty(value = "任务的名称",required = true)
    private String name;
    @ApiModelProperty(value ="目标项目的id",required = true)
    private String projectId;
    @ApiModelProperty(value ="目标项目中的任务列表id",required = true)
    private String mouldId;
    @ApiModelProperty(value ="复制的任务id",required = true)
    private String taskId;
    @ApiModelProperty("复制内容-是否选择执行者 1-是")
    private Integer copyExecutor;
    @ApiModelProperty("复制内容-是否选择子任务 1-是")
    private Integer copyChild;
    @ApiModelProperty("复制内容-是否选择参与者 1-是")
    private Integer copyMember;
    @ApiModelProperty("复制内容-是否选择工时 1-是")
    private Integer copyWorkTime;

    @ApiModelProperty(value = "任务执行者信息",hidden = true )
    private UserDto executeMember;
}
