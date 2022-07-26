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
@ApiModel("项目-收藏表")
@EqualsAndHashCode(callSuper = false)
@TableName("project_collection")
public class ProjectCollection extends BaseEntityPO implements  Serializable {

    private static final long serialVersionUID = 1L;
    @ApiModelProperty("主键id")
    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;
    @ApiModelProperty("对象id ;项目、任务等id")
    @TableField("object_id")
    private String objectId;
    @ApiModelProperty("成员id")
    @TableField("member_id")
    private String memberId;
    @ApiModelProperty("所属租户id")
    @TableField("tenant_id")
    private String tenantId;

    @ApiModelProperty("收藏类型 项目、任务等")
    @TableField("type")
    private String type;
}
