package cn.bctools.teamwork.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 任务收藏
 * @author admin
 */
@Data
@ApiModel("任务收藏请求参数")
public class TaskCollectDTO {
    @ApiModelProperty(value = "任务ID",required = true)
    private String taskId;

    @ApiModelProperty(value = "收藏-1  ,取消收藏-0",required = true)
    private Integer collect;
}
