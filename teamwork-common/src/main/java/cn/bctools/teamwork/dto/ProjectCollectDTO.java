package cn.bctools.teamwork.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
/**
 * @author admin
 */
@ApiModel("收藏和取消收藏数据")
@Data
public class ProjectCollectDTO {


    @ApiModelProperty(value = "项目主键id",required = true)
    private String projectId;

    @ApiModelProperty(value = "collect -收藏  cancel-取消收藏",required = true)
    private String type;
}
