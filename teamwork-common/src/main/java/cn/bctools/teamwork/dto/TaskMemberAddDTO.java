package cn.bctools.teamwork.dto;

import cn.bctools.common.entity.dto.UserDto;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 任务成员添加
 *
 * @author admin
 */
@Data
@Accessors(chain = true)
public class TaskMemberAddDTO {

    @ApiModelProperty(value = "成员id", hidden = true)
    private String memberId;

    @ApiModelProperty(value = "成员id 数组 若选择所有")
    private List<String> memberIds;

    @ApiModelProperty(value = "任务ID", required = true)
    private String taskId;

    @ApiModelProperty(value = "成员信息", hidden = true)
    private UserDto memberInfo;

    @ApiModelProperty("是否创建人 1-是 0-否")
    private Integer isOwner;

    @ApiModelProperty("是否执行者 1-是 0-否")
    private Integer isExecutor;

    @ApiModelProperty(value = "是否选择项目全部人员", required = true)
    private Boolean isAll;
}
