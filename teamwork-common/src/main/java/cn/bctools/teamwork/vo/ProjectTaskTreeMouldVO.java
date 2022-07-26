package cn.bctools.teamwork.vo;

import cn.bctools.teamwork.entity.ProjectTaskMould;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.util.List;

/**
 * * 项目表格视图 任务模板列表
 * @author admin
 */
@Data
@ApiModel("项目表格视图,任务模板列表")
public class ProjectTaskTreeMouldVO extends ProjectTaskMould {
   private List<ProjectTaskTreeVO> tasks;
}
