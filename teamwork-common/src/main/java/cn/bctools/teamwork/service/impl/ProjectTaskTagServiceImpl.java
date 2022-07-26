package cn.bctools.teamwork.service.impl;

import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.teamwork.entity.ProjectTaskTag;
import cn.bctools.teamwork.entity.ProjectTaskToTag;
import cn.bctools.teamwork.mapper.ProjectTaskTagMapper;
import cn.bctools.teamwork.service.ProjectInfoService;
import cn.bctools.teamwork.service.ProjectTaskTagService;
import cn.bctools.teamwork.service.ProjectTaskToTagService;
import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Auto Generator
 */
@Service
public class ProjectTaskTagServiceImpl extends ServiceImpl<ProjectTaskTagMapper, ProjectTaskTag> implements ProjectTaskTagService {

    @Autowired
    ProjectInfoService projectInfoService;
    @Autowired
    ProjectTaskToTagService projectTaskToTagService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProjectTaskTag saveTag(ProjectTaskTag dto, UserDto user) {
        ProjectTaskTag taskTag = this.lambdaQuery().eq(ProjectTaskTag::getProjectId, dto.getProjectId())
                .eq(ProjectTaskTag::getName, dto.getName()).one();
        if (BeanUtil.isNotEmpty(taskTag)) {
            throw new BusinessException("该标签已经存在！");
        }
        dto.initSave(user);
        this.save(dto);
        return dto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProjectTaskTag editTag(ProjectTaskTag dto, UserDto user) {
        ProjectTaskTag taskTag = this.lambdaQuery().eq(ProjectTaskTag::getProjectId, dto.getProjectId())
                .eq(ProjectTaskTag::getName, dto.getName())
                .ne(ProjectTaskTag::getId, dto.getId()).one();
        if (BeanUtil.isNotEmpty(taskTag)) {
            throw new BusinessException("该标签已经存在！");
        }
        dto.initUpdate(user);
        this.updateById(dto);
        return dto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean removeTagById(String id) {
        removeById(id);
        projectTaskToTagService.remove(Wrappers.<ProjectTaskToTag>lambdaQuery().eq(ProjectTaskToTag::getTagId, id));
        return true;
    }

}
