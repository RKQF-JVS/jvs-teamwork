package cn.bctools.teamwork.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 任务点赞
 * @author admin
 */
@Data
@ApiModel("任务点赞请求参数")
public class TaskLikeDTO {
    @ApiModelProperty(value = "任务ID",required = true)
    private String taskId;

    @ApiModelProperty(value = "点赞-1  ,取消点赞-0",required = true)
    private Integer like;
}
