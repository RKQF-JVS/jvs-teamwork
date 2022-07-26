package cn.bctools.teamwork.service.impl;

import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.oss.template.OssTemplate;
import cn.bctools.teamwork.common.common.config.CommonConfig;
import cn.bctools.teamwork.common.enums.LogActionTypeEnum;
import cn.bctools.teamwork.common.enums.ProjectLogTypeEnum;
import cn.bctools.teamwork.dto.ProjectLogAddBO;
import cn.bctools.teamwork.entity.ProjectFileInfo;
import cn.bctools.teamwork.entity.ProjectLog;
import cn.bctools.teamwork.mapper.ProjectFileInfoMapper;
import cn.bctools.teamwork.service.ProjectFileInfoService;
import cn.bctools.teamwork.service.ProjectLogService;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * @author Auto Generator
 */
@Service
public class ProjectFileInfoServiceImpl extends ServiceImpl<ProjectFileInfoMapper, ProjectFileInfo> implements ProjectFileInfoService {
    @Autowired
    ProjectLogService projectLogService;
    @Autowired
    OssTemplate ossTemplate;
    @Autowired
    CommonConfig commonConfig;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean fileRecycle(String fileId, UserDto user) {
        ProjectFileInfo projectFileInfo = this.getById(fileId);
        throwExistException(projectFileInfo);
        if (projectFileInfo.getRecycleFlag() == 1) {
            throw new BusinessException("文件已在回收站");
        }
        boolean b = lambdaUpdate().eq(ProjectFileInfo::getId, fileId)
                .set(ProjectFileInfo::getRecycleFlag, 1)
                .set(ProjectFileInfo::getDeletedTime, LocalDateTime.now())
                .set(ProjectFileInfo::getUpdateBy, StrUtil.isEmpty(user.getRealName()) ? user.getAccountName() : user.getRealName()).update();
        return b;
    }

    @Override
    public Page<ProjectFileInfo> listRecycle(Page<ProjectFileInfo> page, String projectId) {
        page = lambdaQuery().eq(ProjectFileInfo::getProjectId, projectId)
                .eq(ProjectFileInfo::getRecycleFlag, BigDecimal.ROUND_DOWN).page(page);
        //获取文件外链
        page.getRecords().stream().peek(e -> {
            String s = ossTemplate.fileLink(e.getFileName(), commonConfig.getBucketName());
            e.setFileUrl(s);
        }).collect(Collectors.toList());
        return page;
    }

    public void throwExistException(ProjectFileInfo fileInfo) {
        if (BeanUtil.isEmpty(fileInfo)) {
            throw new BusinessException("文件不存在！");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean fileRecovery(String fileId, UserDto user) {
        ProjectFileInfo projectFileInfo = this.getById(fileId);
        throwExistException(projectFileInfo);
        if (projectFileInfo.getRecycleFlag() == 0) {
            throw new BusinessException("文件已经恢复");
        }
        boolean b = lambdaUpdate().eq(ProjectFileInfo::getId, fileId)
                .set(ProjectFileInfo::getRecycleFlag, 0)
                .set(ProjectFileInfo::getUpdateBy, StrUtil.isEmpty(user.getRealName()) ? user.getAccountName() : user.getRealName()).update();
        return b;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean del(String fileId, UserDto user) {
        ProjectFileInfo fileInfo = this.getById(fileId);
        if (BeanUtil.isEmpty(fileInfo)) {
            throw new BusinessException("文件不存在！");
        }
        boolean b = this.removeById(fileId);

        ProjectLogAddBO projectLogAddBO = new ProjectLogAddBO();
        projectLogAddBO.setProjectId(fileInfo.getProjectId())
                .setLogActionTypeEnum(LogActionTypeEnum.PROJECT)
                .setProjectLogTypeEnum(ProjectLogTypeEnum.DELETE_FILE);
        projectLogService.runLog(projectLogAddBO,user);
        return b;
    }
}
