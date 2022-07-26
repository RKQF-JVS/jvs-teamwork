package cn.bctools.teamwork.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author admin
 * @date ：Created in 2022/1/20 11:57
 * [description]：修改标签
 */
@Data
@Accessors(chain = true)
@ApiModel("任务 修改标签")
public class ProjectTaskTagUpdateVo {
    @ApiModelProperty("任务id")
    private String taskId;


    @ApiModelProperty("标签id")
    private String tagId;

    @ApiModelProperty("是否为新增")
    private Boolean isSave;

}
