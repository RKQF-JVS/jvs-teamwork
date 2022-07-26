package cn.bctools.teamwork.service.impl;

import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.teamwork.entity.ProjectFeatures;
import cn.bctools.teamwork.entity.ProjectTask;
import cn.bctools.teamwork.mapper.ProjectFeaturesMapper;
import cn.bctools.teamwork.service.ProjectFeaturesService;
import cn.bctools.teamwork.service.ProjectTaskService;
import cn.bctools.teamwork.service.ProjectVersionLogService;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Auto Generator
 */
@Service
public class ProjectFeaturesServiceImpl extends ServiceImpl<ProjectFeaturesMapper, ProjectFeatures> implements ProjectFeaturesService {
    @Autowired
    ProjectVersionLogService projectVersionLogService;
    @Autowired
    ProjectTaskService projectTaskService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProjectFeatures saveFeatures(ProjectFeatures dto, UserDto currentUser) {
        boolean b = verifyRepeatName(dto.getName(), dto.getProjectId());
        throwExistException(b);
        dto.initSave(currentUser);
        save(dto);
        return dto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProjectFeatures updateFeatures(ProjectFeatures dto, UserDto currentUser) {
        boolean b = verifyRepeatName(dto.getName(), dto.getProjectId(), dto.getId());
        throwExistException(b);
        dto.initUpdate(currentUser);
        updateById(dto);
        return dto;
    }

    public void throwExistException(boolean b){
        if(b){
            throw new BusinessException("该版本库名称已存在！");
        }
    }

    public boolean verifyRepeatName(String name, String projectId) {
        return verifyRepeatName(name, projectId, null);
    }

    public boolean verifyRepeatName(String name, String projectId, String featuresId) {
        List<ProjectFeatures> featuresByName = getFeaturesByName(name,
                new LambdaQueryWrapper<ProjectFeatures>()
                        .eq(ProjectFeatures::getProjectId, projectId)
                        .ne(StrUtil.isNotEmpty(featuresId), ProjectFeatures::getId, featuresId));
        return CollUtil.isNotEmpty(featuresByName);
    }


    public List<ProjectFeatures> getFeaturesByName(String name, LambdaQueryWrapper<ProjectFeatures> wrapper) {
        List<ProjectFeatures> list = list(wrapper.eq(ProjectFeatures::getName, name));
        return list;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean removeFeatures(ProjectFeatures dto, UserDto user) {
        boolean b = removeById(dto.getId());
        projectTaskService.lambdaUpdate().eq(ProjectTask::getFeaturesId, dto.getId())
                .set(ProjectTask::getFeaturesId, null)
                .set(ProjectTask::getVersionId, null)
                .set(ProjectTask::getUpdateBy, StrUtil.isEmpty(user.getRealName()) ? user.getAccountName() : user.getRealName())
                .set(ProjectTask::getUpdateTime, LocalDateTime.now()).update();
        return b;
    }
}
