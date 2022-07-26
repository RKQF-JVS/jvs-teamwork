package cn.bctools.teamwork.entity;

import cn.bctools.common.entity.dto.UserDto;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import cn.bctools.teamwork.common.po.BaseEntityPO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author Auto Generator
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@ApiModel("项目日志表")
@EqualsAndHashCode(callSuper = false)
@TableName("project_log")
public class ProjectLog extends BaseEntityPO implements Serializable {

    private static final long serialVersionUID = 1L;
    @ApiModelProperty("主键id")
    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;
    @ApiModelProperty("操作人id")
    @TableField("member_id")
    private String memberId;
    @ApiModelProperty("操作内容")
    @TableField("content")
    private String content;
    @ApiModelProperty("评论操作内容")
    @TableField("remark")
    private String remark;
    @ApiModelProperty("操作类型(create,done等)")
    @TableField("type")
    private String type;
    @ApiModelProperty("任务id")
    @TableField("task_id")
    private String taskId;
    @ApiModelProperty("场景类型 项目或者任务")
    @TableField("action_type")
    private String actionType;
    @ApiModelProperty("被操作人id，例如指派给，被认领...")
    @TableField("to_member_id")
    private String toMemberId;
    @ApiModelProperty("是否评论，默认 0：否")
    @TableField("is_comment")
    private Integer isComment;
    @ApiModelProperty("关联项目id")
    @TableField("project_id")
    private String projectId;
    @ApiModelProperty("图标")
    @TableField("icon")
    private String icon;
    @ApiModelProperty("是否机器人 默认0 ：否")
    @TableField("is_robot")
    private Integer isRobot;
    @ApiModelProperty("所属租户id")
    @TableField("tenant_id")
    private String tenantId;
    @ApiModelProperty("是否查询20条")
    @TableField(exist = false)
    private Boolean isSize;
    @ApiModelProperty("项目动态操作人的信息")
    @TableField(exist = false)
    private UserDto memberInfo;

    @ApiModelProperty("日志的创建日期 yyyy-MM-dd 字符串")
    @TableField(exist = false)
    private String createDateStr;

    @ApiModelProperty("项目信息")
    @TableField(exist = false)
    private ProjectInfo projectInfo;

    @ApiModelProperty("任务信息")
    @TableField(exist = false)
    private ProjectTask projectTask;
}
