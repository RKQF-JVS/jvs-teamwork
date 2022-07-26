package cn.bctools.teamwork.entity;

import cn.bctools.common.entity.dto.UserDto;
import com.baomidou.mybatisplus.annotation.*;
import cn.bctools.teamwork.common.po.BaseEntityPO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author Auto Generator
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@ApiModel("项目信息表")
@EqualsAndHashCode(callSuper = false)
@TableName("project_info")
public class ProjectInfo extends BaseEntityPO implements Serializable {

    private static final long serialVersionUID = 1L;
    @ApiModelProperty("主键id")
    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;
    @ApiModelProperty("封面图片的名称")
    @TableField("project_cover")
    private String projectCover;
    @ApiModelProperty("项目名称")
    @TableField("project_name")
    private String projectName;
    @ApiModelProperty("项目编号")
    @TableField("project_code")
    private String projectCode;
    @ApiModelProperty("项目简介")
    @TableField("project_description")
    private String projectDescription;
    @ApiModelProperty("是否私有 1-私有项目 0-公开项目")
    @TableField("is_private")
    private Integer isPrivate;
    @ApiModelProperty("项目编号前缀")
    @TableField("project_prefix")
    private String projectPrefix;
    @ApiModelProperty("是否开启项目前缀 1-是 0-否")
    @TableField("is_open_prefix")
    private Integer isOpenPrefix;
    @ApiModelProperty("项目进度")
    @TableField("project_schedule")
    private Double projectSchedule;
    @ApiModelProperty("删除标记 0-未删除 1-已删除")
    @TableField("del_flag")
    private Integer delFlag;
    @ApiModelProperty("回收站标记 0-不在回收站 1-在回收站")
    @TableField("recycle_flag")
    private Integer recycleFlag;
    @ApiModelProperty("是否开启新任务默认开启隐私模式 1-是 0-否")
    @TableField("open_task_private")
    private Integer openTaskPrivate;
    @ApiModelProperty("是否开启任务开始时间 1-是 0-否")
    @TableField("is_open_begin_time")
    private Integer isOpenBeginTime;
    @ApiModelProperty("项目开始日期")
    @TableField("begin_time")
    private LocalDateTime beginTime;
    @ApiModelProperty("项目模板id")
    @TableField("template_id")
    private String templateId;
    @ApiModelProperty("组织id 没有用到")
    @TableField("organization_id")
    private String organizationId;
    @ApiModelProperty("删除时间")
    @TableField("del_time")
    private LocalDateTime delTime;
    @ApiModelProperty("是否归档 1-是 0-否")
    @TableField("is_archive")
    private Integer isArchive;
    @ApiModelProperty("归档时间")
    @TableField("archive_time")
    private LocalDateTime archiveTime;
    @ApiModelProperty("看板风格")
    @TableField("task_board_theme")
    private String taskBoardTheme;
    @ApiModelProperty("项目截止日期")
    @TableField("end_time")
    private LocalDateTime endTime;
    @ApiModelProperty("自动更新项目进度 1-选择 0-未选择")
    @TableField("auto_update_schedule")
    private Integer autoUpdateSchedule;
    @ApiModelProperty("所属租户id")
    @TableField("tenant_id")
    private String tenantId;

    @ApiModelProperty("是否被收藏 0-否 1-是")
    @TableField(exist = false)
    private Boolean collectStatus;
    @ApiModelProperty("封面外链")
    @TableField(exist = false)
    private String projectCoverUrl;

    @ApiModelProperty("成员昵称 或者邮箱 查询")
    @TableField(exist = false)
    private String memberNickEmail;

    @ApiModelProperty("成员分页查询 第几页默认1")
    @TableField(exist = false)
    private Long current = 1L;

    @ApiModelProperty("成员分页查询 一页条数默认10")
    @TableField(exist = false)
    private Long size = 10L;

    @ApiModelProperty("项目拥有者信息")
    @TableField(exist = false)
    private UserDto owner;

}
