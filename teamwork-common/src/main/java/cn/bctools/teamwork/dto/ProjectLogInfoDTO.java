package cn.bctools.teamwork.dto;

import cn.bctools.teamwork.entity.ProjectLog;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 这是一个项目动态信息的返回类
 *
 * @author admin
 */
@Data
@Accessors(chain = true)
@ApiModel("项目动态")
public class ProjectLogInfoDTO {
    @ApiModelProperty("数据")
    List<ProjectLog> data;
    @ApiModelProperty("总条数")
    private Integer total;
}
