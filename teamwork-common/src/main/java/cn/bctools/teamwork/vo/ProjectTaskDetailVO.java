package cn.bctools.teamwork.vo;

import cn.bctools.teamwork.entity.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author admin
 */
@Data
@Accessors(chain = true)
@ApiModel("任务详情展示")
public class ProjectTaskDetailVO extends ProjectTask {

    @ApiModelProperty("子任务完成情况")
    private String taskCount;

    @ApiModelProperty("工时记录")
    private List<ProjectTaskWorkTimeRecord> projectTaskWorkTimeRecords;

    @ApiModelProperty("子任务")
    private List<ProjectTask> tasks;

    @ApiModelProperty("任务标签联系")
    private List<ProjectTaskTag> tags;

    @ApiModelProperty("我是否点赞 0-未点赞 1-已点赞")
    private Integer likeMine;

    @ApiModelProperty("我是否收藏 0-未收藏 1-已收藏")
    private Integer collectMine;

    @ApiModelProperty("所属任务列表信息")
    private ProjectTaskMould projectTaskMould;

    @ApiModelProperty("所属项目信息")
    private ProjectInfo projectInfo;

    @ApiModelProperty("所属父任务信息")
    private ProjectTask prentTask;

}
