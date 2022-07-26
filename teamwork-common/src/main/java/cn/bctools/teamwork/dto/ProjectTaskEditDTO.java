package cn.bctools.teamwork.dto;

import cn.hutool.core.date.DatePattern;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 这是一个任务修改的一些字段
 * @author admin
 */
@Data
@ApiModel("任务修改的字段")
public class ProjectTaskEditDTO {

    @ApiModelProperty(value = "任务主键id",required = true)
    private String id;

    @ApiModelProperty("任务名称")
    private String name;

    @ApiModelProperty("任务执行状态  0-未开始 1-已完成 2-进行中 3-挂起 4-测试中")
    private String executeStatus;

    @ApiModelProperty("任务完成状态 0-未完成 1-已完成")
    private Integer finishStatus;

    @DateTimeFormat(pattern = DatePattern.NORM_DATETIME_PATTERN)
    @JsonFormat(pattern = DatePattern.NORM_DATETIME_PATTERN, timezone = "GMT+8")
    @ApiModelProperty("任务开始时间")
    private LocalDateTime startTime;

    @DateTimeFormat(pattern = DatePattern.NORM_DATETIME_PATTERN)
    @JsonFormat(pattern = DatePattern.NORM_DATETIME_PATTERN, timezone = "GMT+8")
    @ApiModelProperty("任务结束时间")
    private LocalDateTime endTime;

    @ApiModelProperty("任务备注")
    private String remark;

    @ApiModelProperty("优先级 0-普通 1-紧急 3-非常紧急")
    private String priorityLevel;

    @ApiModelProperty("设置预估工时")
    private Integer initWorkingHours;

    @ApiModelProperty("设置任务开始时间和结束时间的标记 end 或者 start")
    private String editTimeType;

}
