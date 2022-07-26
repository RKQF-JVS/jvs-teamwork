package cn.bctools.teamwork.service.impl;

import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.teamwork.entity.ProjectTemplate;
import cn.bctools.teamwork.entity.ProjectTemplateTask;
import cn.bctools.teamwork.mapper.ProjectTemplateMapper;
import cn.bctools.teamwork.service.ProjectTemplateService;
import cn.bctools.teamwork.service.ProjectTemplateTaskService;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Auto Generator
 */
@Service
public class ProjectTemplateServiceImpl extends ServiceImpl<ProjectTemplateMapper, ProjectTemplate> implements ProjectTemplateService {

    @Autowired
    ProjectTemplateTaskService projectTemplateTaskService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProjectTemplate save(ProjectTemplate projectTemplate, UserDto login) {
        if (StrUtil.isEmpty(projectTemplate.getTemplateName())) {
            throw new BusinessException("模板名称必填！");
        }
        ProjectTemplate template = new ProjectTemplate();
        BeanUtil.copyProperties(projectTemplate, template);
        template.initSave(login);
        this.save(template);
        List<ProjectTemplateTask> tasks = projectTemplateTaskService.initTaskList(template, login);
        //保存初始化模板任务列表
        projectTemplateTaskService.saveBatch(tasks);
        return template;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProjectTemplate updateTemplate(ProjectTemplate projectTemplate, UserDto login) {
        ProjectTemplate template = new ProjectTemplate();
        BeanUtil.copyProperties(projectTemplate, template);
        template.initUpdate(login);
        this.updateById(template);
        return projectTemplate;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean removeTemplate(String id) {
        this.removeById(id);
        projectTemplateTaskService.remove(new LambdaQueryWrapper<ProjectTemplateTask>().eq(ProjectTemplateTask::getTemplateId, id));
        return true;
    }

    @Override
    public void getPage(Page<ProjectTemplate> page, ProjectTemplate dto, UserDto userDto) {
        //只能查看自己的模板数据
        this.page(page, new LambdaQueryWrapper<ProjectTemplate>().eq(ProjectTemplate::getCreateById, userDto.getId()));
        if (!page.getRecords().isEmpty()) {
            List<String> ids = page.getRecords().stream().map(ProjectTemplate::getId).collect(Collectors.toList());
            //获取模板任务
            Map<String, List<ProjectTemplateTask>> listMap = projectTemplateTaskService.list(new LambdaQueryWrapper<ProjectTemplateTask>().in(ProjectTemplateTask::getTemplateId, ids))
                    .stream().collect(Collectors.groupingBy(ProjectTemplateTask::getTemplateId));
            //数据重组
            page.getRecords().stream().peek(e -> {
                List<ProjectTemplateTask> orDefault = listMap.getOrDefault(e.getId(), new ArrayList<>());
                if (!orDefault.isEmpty()) {
                    orDefault = orDefault.stream().sorted(Comparator.comparing(ProjectTemplateTask::getSort).reversed()).collect(Collectors.toList());
                }
                e.setList(orDefault);
            }).collect(Collectors.toList());
        }
    }
}
