package cn.bctools.teamwork.common.consts;

import cn.bctools.teamwork.entity.ProjectTemplateTask;

import java.util.ArrayList;
import java.util.List;

/**
 * 项目任务模板
 * @author admin
 */
public class ProjectTaskMouldConst {

    public static List<ProjectTemplateTask> init(){
        ProjectTemplateTask md1=new ProjectTemplateTask().setName("待处理");
        ProjectTemplateTask md2=new ProjectTemplateTask().setName("进行中");
        ProjectTemplateTask md3=new ProjectTemplateTask().setName("已完成");
        return new ArrayList<ProjectTemplateTask>(){{
            add(md1);add(md2);add(md3);
        }};
    }

}
