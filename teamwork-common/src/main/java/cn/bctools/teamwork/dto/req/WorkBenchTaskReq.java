package cn.bctools.teamwork.dto.req;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 工作台查询任务需要的参数
 * @author admin
 */
@Data
@ApiModel("工作台查询任务需要的参数")
public class WorkBenchTaskReq {
    @ApiModelProperty(value = "0-未完成 1-已完成",required = true)
    private Integer finishStatus;
    @ApiModelProperty(value = "我执行的-1 我参与的-2 我创建的-3",required = true)
    private Integer taskType;
}
