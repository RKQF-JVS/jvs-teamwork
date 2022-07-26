package cn.bctools.teamwork.vo;


import cn.bctools.teamwork.entity.ProjectTask;
import cn.bctools.teamwork.entity.ProjectTaskMould;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author admin
 */
@Data
@ApiModel("项目中任务列表与列表下的一层任务")
public class ProjectMouldAndTaskVO extends ProjectTaskMould {

    @ApiModelProperty("列表下面任务的集合")
    private List<ProjectTask> tasks;
}
