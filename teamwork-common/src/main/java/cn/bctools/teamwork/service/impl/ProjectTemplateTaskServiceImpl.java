package cn.bctools.teamwork.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.teamwork.common.util.FieldFillUtil;
import cn.bctools.teamwork.entity.ProjectTemplate;
import cn.bctools.teamwork.entity.ProjectTemplateTask;
import cn.bctools.teamwork.mapper.ProjectTemplateTaskMapper;
import cn.bctools.teamwork.service.ProjectTemplateService;
import cn.bctools.teamwork.service.ProjectTemplateTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Auto Generator
 */
@Service
public class ProjectTemplateTaskServiceImpl extends ServiceImpl<ProjectTemplateTaskMapper, ProjectTemplateTask> implements ProjectTemplateTaskService {

    @Autowired
    ProjectTemplateService projectTemplateService;

    @Override
    public List<ProjectTemplateTask> initTaskList(ProjectTemplate template, UserDto login) {
        List<ProjectTemplateTask> listTst = new ArrayList() {{
            add(new ProjectTemplateTask()
                    .setSort(0)
                    .setName("待处理")
                    .setTemplateId(template.getId())
                    .initSave(login)
            );
            add(new ProjectTemplateTask().
                    setSort(0).
                    setName("进行中").
                    setTemplateId(template.getId())
                    .initSave(login)
            );
            add(new ProjectTemplateTask()
                    .setSort(0)
                    .setName("已完成")
                    .setTemplateId(template.getId())
                    .initSave(login)
            );
        }};
        return listTst;
    }

    @Override
    public List<ProjectTemplateTask> getTaskListByTemplate(String id) {
        return lambdaQuery().eq(ProjectTemplateTask::getTemplateId, id).orderByDesc(ProjectTemplateTask::getSort).list();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProjectTemplateTask saveTemplateTask(ProjectTemplateTask dto, UserDto login) {
        dto.initSave(login);
        this.save(dto);
        return dto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProjectTemplateTask editTemplateTask(ProjectTemplateTask dto, UserDto login) {
        ProjectTemplateTask templateTask = this.getById(dto.getId());
        if (BeanUtil.isEmpty(templateTask)) {
            throw new BusinessException("该任务已失效！");
        }
        dto.initUpdate(login);
        this.updateById(dto);
        return dto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean removeTask(ProjectTemplateTask dto) {
        ProjectTemplate template = projectTemplateService.getById(dto.getTemplateId());
        if (BeanUtil.isEmpty(template)) {
            throw new BusinessException("该模板不存在");
        }
        return this.removeById(dto.getId());
    }
}
