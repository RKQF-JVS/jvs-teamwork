package cn.bctools.teamwork.vo;

import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.teamwork.entity.ProjectTaskTag;
import cn.hutool.core.date.DatePattern;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author ：xh
 * @date ：Created in 2021/12/22 15:17
 * [description]：任务列表 任务基本信息
 * @modified By：
 * @version: $
 */
@Data
@Accessors(chain = true)
@ApiModel("任务列表 任务基本信息")
public class ProjectTaskListVo {
    @ApiModelProperty("任务名称")
    private String taskName;
    @ApiModelProperty("任务与当前时间对比结果 0当天 小于0过去 大于0未来")
    private Long timeStatus;
    @ApiModelProperty("任务id")
    private String id;
    @ApiModelProperty("任务点赞数量")
    private Integer thumbsUpCount;
    @ApiModelProperty("任务模型id")
    private String projectTaskMouldId;
    @ApiModelProperty("任务完成情况 例如 1/3")
    private String childCount;
    @ApiModelProperty("标签名称")
    private List<ProjectTaskTag> projectTaskTags;
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
    @ApiModelProperty("任务负责人信息")
    private UserDto userDto;
    @ApiModelProperty("任务执行状态 0-未开始 1-已完成 2-进行中 3-挂起 4-测试中")
    private String executeStatus;
    @ApiModelProperty("任务完成状态 0-未完成 1-已完成")
    private Integer finishStatus;
    @ApiModelProperty("排序")
    private Integer sort;
    @ApiModelProperty("子任务")
    private List<ProjectTaskListVo> childTask;
    @ApiModelProperty("我是否点赞 0-未点赞 1-已点赞")
    private Boolean likeMine;
    @ApiModelProperty("我是否收藏 0-未收藏 1-已收藏")
    private Boolean collectMine;
    @ApiModelProperty("是否有备注")
    private Boolean isRemark;
    @ApiModelProperty("优先级 0-普通 1-紧急 3-非常紧急")
    private String priorityLevel;

}
