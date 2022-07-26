package cn.bctools.teamwork.service;

import com.baomidou.mybatisplus.extension.service.IService;
import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.teamwork.entity.ProjectCollection;

/**
 *
 * @author Auto Generator
 */
public interface ProjectCollectionService extends IService<ProjectCollection> {

    /**
     * 项目收藏与取消收藏
     * @param projectId
     * @param loginUser
     * @return
     */
    Boolean collect(String projectId, UserDto loginUser);

    /**
     * 项目取消收藏
     * @param projectId
     * @param loginUser
     * @return
     */
    Boolean cancel(String projectId, UserDto loginUser);
}
