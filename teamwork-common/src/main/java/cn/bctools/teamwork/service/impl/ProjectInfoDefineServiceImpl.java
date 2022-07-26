package cn.bctools.teamwork.service.impl;

import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.teamwork.entity.ProjectInfo;
import cn.bctools.teamwork.entity.ProjectInfoDefine;
import cn.bctools.teamwork.mapper.ProjectInfoDefineMapper;
import cn.bctools.teamwork.service.ProjectInfoDefineService;
import cn.bctools.teamwork.service.ProjectInfoService;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author Auto Generator
 */
@Service
public class ProjectInfoDefineServiceImpl extends ServiceImpl<ProjectInfoDefineMapper, ProjectInfoDefine> implements ProjectInfoDefineService {
    @Autowired
    ProjectInfoService projectInfoService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProjectInfoDefine saveProjectInfo(ProjectInfoDefine dto, UserDto user) {

        ProjectInfo info = projectInfoService.getById(dto.getProjectId());
        projectInfoService.verifyExist(info);
        projectInfoService.verifyDel(info);
        dto.initSave(user);
        save(dto);
        return dto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProjectInfoDefine editProjectInfo(ProjectInfoDefine dto, UserDto user) {
        List<ProjectInfoDefine> defines = lambdaQuery().eq(ProjectInfoDefine::getName, dto.getName()).ne(ProjectInfoDefine::getId, dto.getId()).list();
        ProjectInfo info = projectInfoService.getById(dto.getProjectId());
        projectInfoService.verifyExist(info);
        projectInfoService.verifyDel(info);
        ProjectInfoDefine define = getById(dto.getId());
        if (BeanUtil.isEmpty(define)) {
            throw new BusinessException("该项目信息已失效！");
        }
        if (CollUtil.isNotEmpty(defines)) {
            throw new BusinessException("该项目信息名称已存在！");
        }
        dto.initUpdate(user);
        updateById(dto);
        return dto;
    }
}
