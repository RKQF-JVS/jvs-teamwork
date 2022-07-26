package cn.bctools.teamwork.dto;

import cn.bctools.teamwork.common.enums.LogActionTypeEnum;
import cn.bctools.teamwork.common.enums.ProjectLogTypeEnum;
import cn.bctools.teamwork.common.enums.TaskLogTypeEnum;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 项目动态和任务动态
 *
 * @author admin
 */
@Data
@Accessors(chain = true)
public class ProjectLogAddBO {
    @ApiModelProperty("动态类型")
    LogActionTypeEnum logActionTypeEnum;

    @ApiModelProperty("任务动态类型")
    TaskLogTypeEnum taskLogTypeEnum;

    @ApiModelProperty("项目动态类型")
    ProjectLogTypeEnum projectLogTypeEnum;

    @ApiModelProperty("格式化需要的内容")
    String[] formatContent;

    @ApiModelProperty("操作内容")
    @TableField("content")
    private String content;

    @ApiModelProperty("任务id")
    private String taskId;

    @ApiModelProperty("被操作人id，例如指派给，被认领...")
    private String toMemberId;

    @ApiModelProperty("是否评论，0：否 默认0")
    private Integer isComment;

    @ApiModelProperty("关联项目id")
    private String projectId;

    @ApiModelProperty("图标")
    private String icon;

    @ApiModelProperty("是否机器人 0:否 默认0")
    private Integer isRobot;
}
