package cn.bctools.teamwork.service;

import com.baomidou.mybatisplus.extension.service.IService;
import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.teamwork.entity.ProjectTemplate;
import cn.bctools.teamwork.entity.ProjectTemplateTask;

import java.util.List;

/**
 *
 * @author Auto Generator
 */
public interface ProjectTemplateTaskService extends IService<ProjectTemplateTask> {

    /**
     * 获得模板初始化一个任务列表
     * @param template
     * @param login
     * @return
     */
    List<ProjectTemplateTask> initTaskList(ProjectTemplate template, UserDto login);

    /**
     * 根据模板id 查询模板的任务列表
     * @param id
     * @return
     */
    List<ProjectTemplateTask> getTaskListByTemplate(String id);

    /**
     * 保存模板任务信息
     * @param dto
     * @param login
     * @return
     */
    ProjectTemplateTask saveTemplateTask(ProjectTemplateTask dto, UserDto login);

    /**
     * 编辑模板任务信息
     * @param dto
     * @param login
     * @return
     */
    ProjectTemplateTask editTemplateTask(ProjectTemplateTask dto,UserDto login);

    /**
     * 删除模板任务
     * @param dto
     * @return
     */
    Boolean removeTask(ProjectTemplateTask dto);

}
