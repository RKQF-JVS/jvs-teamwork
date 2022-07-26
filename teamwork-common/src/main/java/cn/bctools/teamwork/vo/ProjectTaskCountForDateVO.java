package cn.bctools.teamwork.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 项目新增任务趋势图
 * @author admin
 */
@Data
@ApiModel("项目新增任务趋势图")
public class ProjectTaskCountForDateVO {

    @ApiModelProperty("任务创建时间和对应任务数量集合")
    private List<TaskDateAndCount>  result;

    @Data
    @ApiModel("任务日期和数量")
    public static class TaskDateAndCount{

        @ApiModelProperty("任务的日期")
        private String date;

        @ApiModelProperty("任务数量")
        private Integer count;
    }
}
