package cn.bctools.teamwork.dto.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 *任务趋势图请求参数
 * @author admin
 */
@Data
@ApiModel("任务趋势图请求参数")
public class TaskDateCountReq {
    @ApiModelProperty(value = "项目id",required = true)
    private String projectId;

    @ApiModelProperty("任务开始时间选择 格式 'yyyy-MM-dd' 不选择 默认取当前系统时间 -20天")
    private String beginTime;

    @ApiModelProperty("任务结束时间选择 格式 'yyyy-MM-dd' 不选择 默认取当前系统时间")
    private String endTime;
}
