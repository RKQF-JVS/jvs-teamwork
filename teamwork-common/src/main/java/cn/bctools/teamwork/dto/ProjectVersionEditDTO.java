package cn.bctools.teamwork.dto;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 这是一个版本改的一些字段
 *
 * @author admin
 */
@Data
@ApiModel("项目版本的字段")
public class ProjectVersionEditDTO {

    @ApiModelProperty(value = "版本id", required = true)
    private String versionId;

    @ApiModelProperty("开始时间")
    private LocalDateTime startTime;

    @ApiModelProperty("预计发布时间")
    private LocalDateTime planPublishTime;

    @ApiModelProperty("描述备注")
    private String description;

    @ApiModelProperty("名称")
    private String name;

    @ApiModelProperty("为了区分发布时间和开始时间标记 start 或者 plan")
    private String timeType;

}
