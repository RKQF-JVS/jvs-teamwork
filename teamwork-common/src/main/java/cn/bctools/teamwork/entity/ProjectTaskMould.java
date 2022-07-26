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
 * @author Auto Generator
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@ApiModel("任务列表 表")
@EqualsAndHashCode(callSuper = false)
@TableName("project_task_mould")
public class ProjectTaskMould extends BaseEntityPO implements Serializable {

    private static final long serialVersionUID = 1L;
    @ApiModelProperty("主键id")
    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;
    @ApiModelProperty("任务名称")
    @TableField("name")
    private String name;
    @ApiModelProperty("项目id")
    @TableField("project_id")
    private String projectId;
    @ApiModelProperty("排序")
    @TableField("sort")
    private Integer sort;
    @ApiModelProperty("任务描述")
    @TableField("description")
    private String description;
    @ApiModelProperty("删除标记")
    @TableField("del_flag")
    private Integer delFlag;

    @ApiModelProperty("回收站标记 0-不在回收站 1-在回收站")
    @TableField("recycle_flag")
    private Integer recycleFlag;

    @ApiModelProperty("所属租户id")
    @TableField("tenant_id")
    private String tenantId;

}
