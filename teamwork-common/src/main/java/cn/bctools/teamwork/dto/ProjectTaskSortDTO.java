package cn.bctools.teamwork.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author admin
 */
@ApiModel("任务列表任务排序")
@Data
public class ProjectTaskSortDTO {
    @ApiModelProperty(value = "任务所属模板id",required = true)
    private String taskMouldId;
    @ApiModelProperty(value = "任务的所有主键id集合",required = true)
    private List<String> taskIds;
}
