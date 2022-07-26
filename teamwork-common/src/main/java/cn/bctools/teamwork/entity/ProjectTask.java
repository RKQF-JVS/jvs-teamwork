package cn.bctools.teamwork.entity;

import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.teamwork.common.po.BaseEntityPO;
import cn.hutool.core.date.DatePattern;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Auto Generator
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@ApiModel("任务的基本信息")
@EqualsAndHashCode(callSuper = false)
@TableName("project_task")
public class ProjectTask extends BaseEntityPO implements Serializable {

    private static final long serialVersionUID = 1L;
    @ApiModelProperty("主键id")
    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;
    @ApiModelProperty("任务名称")
    @TableField("name")
    private String name;
    @ApiModelProperty("父任务id")
    @TableField("pid")
    private String pid;
    @ApiModelProperty("任务开始时间")
    @TableField("start_time")
    @DateTimeFormat(pattern = DatePattern.NORM_DATETIME_PATTERN)
    @JsonFormat(pattern = DatePattern.NORM_DATETIME_PATTERN, timezone = "GMT+8")
    private LocalDateTime startTime;
    @ApiModelProperty("任务结束截止时间")
    @TableField("end_time")
    @DateTimeFormat(pattern = DatePattern.NORM_DATETIME_PATTERN)
    @JsonFormat(pattern = DatePattern.NORM_DATETIME_PATTERN, timezone = "GMT+8")
    private LocalDateTime endTime;
    @ApiModelProperty("任务执行者")
    @TableField("execute_member_id")
    private String executeMemberId;
    @ApiModelProperty("任务完成状态 0-未完成 1-已完成")
    @TableField("finish_status")
    private Integer finishStatus;
    @ApiModelProperty("任务执行状态 0-未开始 1-已完成 2-进行中 3-挂起 4-测试中")
    @TableField("execute_status")
    private String executeStatus;
    @ApiModelProperty("任务备注")
    @TableField("remark")
    private String remark;
    @ApiModelProperty("优先级 0-普通 1-紧急 3-非常紧急")
    @TableField("priority_level")
    private String priorityLevel;
    @ApiModelProperty("设置预估工时")
    @TableField("init_working_hours")
    private Integer initWorkingHours;
    @ApiModelProperty("所属租户")
    @TableField("tenant_id")
    private String tenantId;
    @ApiModelProperty("任务点赞数量")
    @TableField("thumbs_up_count")
    private Integer thumbsUpCount;
    @ApiModelProperty("项目id")
    @TableField("project_id")
    private String projectId;
    @ApiModelProperty("是否隐私模式 1-是")
    @TableField("is_private")
    private Integer isPrivate;
    @ApiModelProperty("排序")
    @TableField("sort")
    private Integer sort;
    @ApiModelProperty("版本库版本id")
    @TableField("version_id")
    private String versionId;
    @ApiModelProperty("版本库id")
    @TableField("features_id")
    private String featuresId;
    @ApiModelProperty("任务列表id")
    @TableField("mould_id")
    private String mouldId;
    @ApiModelProperty("任务收藏数量")
    @TableField("collection_quantity")
    private Integer collectionQuantity;
    @ApiModelProperty("删除时间")
    @TableField("del_time")
    private LocalDateTime delTime;
    @ApiModelProperty(" 1-已删除")
    @TableField("del_flag")
    private Integer delFlag;
    @ApiModelProperty("回收站标记 0-不在回收站 1-在回收站")
    @TableField("recycle_flag")
    private String recycleFlag;

    @ApiModelProperty("父任务名字")
    @TableField(exist = false)
    private String pName;

    @ApiModelProperty("开始时间字符串")
    @TableField(exist = false)
    private String startTimeStr;

    @ApiModelProperty("截止时间字符串")
    @TableField(exist = false)
    private String endTimeStr;
    @ApiModelProperty("任务标签 英文 ; 隔开")
    @TableField(exist = false)
    private String taskTag;

    @ApiModelProperty("导入时 根据姓名查询出的执行者集合")
    @TableField(exist = false)
    private List<UserDto> importMember;


    @ApiModelProperty("执行者信息")
    @TableField(exist = false)
    private UserDto executor;

    @ApiModelProperty("子任务是否完成")
    @TableField(exist = false)
    private Boolean childrenStatus;

    @ApiModelProperty("任务是否是子任务")
    @TableField(exist = false)
    private Boolean isChildren;

    @ApiModelProperty("所属项目信息")
    @TableField(exist = false)
    private ProjectInfo projectInfo;




}
