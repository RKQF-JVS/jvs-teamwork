package cn.bctools.teamwork.service.impl;

import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.teamwork.common.enums.CollectionTypeEnum;
import cn.bctools.teamwork.entity.ProjectCollection;
import cn.bctools.teamwork.entity.ProjectInfo;
import cn.bctools.teamwork.mapper.ProjectCollectionMapper;
import cn.bctools.teamwork.service.ProjectCollectionService;
import cn.bctools.teamwork.service.ProjectInfoService;
import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Auto Generator
 */
@Service
public class ProjectCollectionServiceImpl extends ServiceImpl<ProjectCollectionMapper, ProjectCollection> implements ProjectCollectionService {
    @Autowired
    ProjectInfoService projectInfoService;

    @Override
    public Boolean collect(String projectId, UserDto loginUser) {
        boolean result;
        ProjectInfo projectInfo = projectInfoService.getNotDelById(projectId);
        projectInfoService.verifyExist(projectInfo);
        ProjectCollection collection = this.lambdaQuery().eq(ProjectCollection::getObjectId, projectId)
                .eq(ProjectCollection::getMemberId, loginUser.getId()).one();
        if (BeanUtil.isEmpty(collection)) {
            ProjectCollection projectCollection = new ProjectCollection() {{
                setObjectId(projectId);
                setMemberId(loginUser.getId())
                        .setType(CollectionTypeEnum.PROJECT.getCode());
            }};
            projectCollection.initSave(loginUser);
            result = this.save(projectCollection);
        } else {
            throw new BusinessException("该项目已收藏！");
        }


        return result;
    }

    @Override
    public Boolean cancel(String projectId, UserDto loginUser) {
        boolean result;
        ProjectCollection collection = this.lambdaQuery().eq(ProjectCollection::getObjectId, projectId)
                .eq(ProjectCollection::getMemberId, loginUser.getId())
                .eq(ProjectCollection::getType, CollectionTypeEnum.PROJECT).one();
        if (BeanUtil.isNotEmpty(collection)) {
            result = this.removeById(collection.getId());
        } else {
            throw new BusinessException("该项目尚未收藏！");
        }

        return result;
    }
}
