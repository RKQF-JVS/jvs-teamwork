package cn.bctools.teamwork.dto;

import cn.bctools.teamwork.common.enums.SelectTypeEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author admin
 */
@Data
@ApiModel("项目列表或项目下拉框搜索条件")
public class ProjectQueryDTO {
    @ApiModelProperty("主键id")
    private String id;
    @ApiModelProperty("项目名称")
    private String projectName;
    @ApiModelProperty(value = "查询类型 注意如果是全部项目就不用传 就是 MY 类型永远都用不到", required = true)
    private SelectTypeEnum selectType;

}
