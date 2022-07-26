package cn.bctools.teamwork.entity;

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
 * @author admin
 */
@TableName("project_task_liked")
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@ApiModel("任务的基本信息")
@EqualsAndHashCode(callSuper = false)
public class ProjectTaskLiked extends BaseEntityPO implements Serializable {
    private static final long serialVersionUID = 1L;
    @ApiModelProperty("主键id")
    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;

    @ApiModelProperty("任务id")
    @TableField("task_id")
    private String taskId;

    @ApiModelProperty("成员id")
    @TableField("member_id")
    private String memberId;

    @ApiModelProperty("所属租户")
    @TableField("tenant_id")
    private String tenantId;

}
