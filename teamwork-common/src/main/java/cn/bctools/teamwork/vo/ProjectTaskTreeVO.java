package cn.bctools.teamwork.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 项目表格视图
 *
 * @author admin
 */
@Data
@ApiModel("项目表格视图 任务详情")
public class ProjectTaskTreeVO extends ProjectTaskDetailVO {

    @ApiModelProperty("项目表格视图 子任务")
   private List<ProjectTaskTreeVO>  childTask;


}
