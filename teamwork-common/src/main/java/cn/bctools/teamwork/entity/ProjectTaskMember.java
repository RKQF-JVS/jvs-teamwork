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
@ApiModel("任务-成员表")
@EqualsAndHashCode(callSuper = false)
@TableName("project_task_member")
public class ProjectTaskMember extends BaseEntityPO implements Serializable {

    private static final long serialVersionUID = 1L;
    @ApiModelProperty("主键id")
    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;
    @ApiModelProperty("任务ID")
    @TableField("task_id")
    private String taskId;
    @ApiModelProperty("是否执行者 1-是 0-否")
    @TableField("is_executor")
    private Integer isExecutor;
    @ApiModelProperty("成员id")
    @TableField("member_id")
    private String memberId;
    @ApiModelProperty("是否创建人 1-是 0-否")
    @TableField("is_owner")
    private Integer isOwner;
    @ApiModelProperty("成员信息")
    @TableField(exist = false)
    private UserDto userDto;
    @ApiModelProperty("所属租户")
    @TableField("tenant_id")
    private String tenantId;

}
