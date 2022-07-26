package cn.bctools.teamwork.dto;

import cn.bctools.teamwork.entity.ProjectTask;
import cn.bctools.teamwork.entity.ProjectVersion;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 版本和任务关联
 * @author admin
 */
@Data
@ApiModel("版本和任务关联")
public class VersionTaskDTO extends ProjectVersion{
    @ApiModelProperty("版本关联的任务")
    private List<ProjectTask> tasks;


}
