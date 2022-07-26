package cn.bctools.teamwork.service;

import com.baomidou.mybatisplus.extension.service.IService;
import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.teamwork.entity.ProjectFeatures;

/**
 *
 * @author Auto Generator
 */
public interface ProjectFeaturesService extends IService<ProjectFeatures> {
    /**
     * 新增版本库
     * @param dto
     * @param currentUser
     * @return
     */
    ProjectFeatures saveFeatures(ProjectFeatures dto, UserDto currentUser);

    /**
     * 编辑版本库
     * @param dto
     * @param currentUser
     * @return
     */
    ProjectFeatures updateFeatures(ProjectFeatures dto, UserDto currentUser);

    /**
     * 根据id 删除版本库
     * @param dto
     * @param currentUser
     * @return
     */
    Boolean removeFeatures(ProjectFeatures dto, UserDto currentUser);
}

