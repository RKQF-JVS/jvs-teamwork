package cn.bctools.teamwork.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author ：xh
 * @date ：Created in 2021/12/22 15:13
 * [description]：列表Vo
 * @modified By：
 * @version: $
 */
@Data
@Accessors(chain = true)
@ApiModel("任务列表")
public class ProjectTaskMouldListVo {
    @ApiModelProperty(value = "项目id")
    private String projectId;
    @ApiModelProperty(value = "排序")
    private Integer sort;
    @ApiModelProperty("任务列表名称")
    private String projectMouldName;
    @ApiModelProperty("模型id")
    private String id;
    @ApiModelProperty("任务 默认空数组")
    private List<ProjectTaskListVo> childList = new ArrayList<>();
    @ApiModelProperty("任务 看板模式下才会存在这个值 默认空map")
    private Map<String, List<ProjectTaskListVo>> childDefaultList = new HashMap<>(1);

}