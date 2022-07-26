package cn.bctools.teamwork.service;

import cn.bctools.teamwork.dto.ProjectVersionEditDTO;
import cn.bctools.teamwork.dto.VersionTaskDTO;
import cn.bctools.teamwork.entity.ProjectTask;
import com.baomidou.mybatisplus.extension.service.IService;
import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.teamwork.entity.ProjectVersion;

import java.util.List;

/**
 *
 * @author Auto Generator
 */
public interface ProjectVersionService extends IService<ProjectVersion> {

    /**
     * 新增版本
     * @param dto
     * @param login
     * @return
     */
    ProjectVersion saveVersion(ProjectVersion dto, UserDto login);

    /**
     * 根据版本库id 查询下边所属的版本
     * @param featuresId
     * @return
     */
    List<ProjectVersion> getListByFeaturesId(String featuresId);

    /**
     * 版本发布关联任务
     * @param dto
     * @param currentUser
     * @return
     */
    VersionTaskDTO saveVersionTask(VersionTaskDTO dto, UserDto currentUser);

    /**
     * 根据任务id，取消关联版本信息
     * @param dto
     * @param currentUser
     * @return
     */
    boolean removeVersionTask(ProjectTask dto, UserDto currentUser);

    /**
     * 根据版本id 更新版本的完成进度
     * @param versionId
     */
    void updateSchedule(String versionId);

    /**
     * 根据版本查询所属版本的任务信息
     * @param dto
     * @return
     */
    List<ProjectTask> getVersionTask(ProjectVersion dto);

    /**
     * 更改版本库版本的信息
     * @param dto
     * @param currentUser
     * @return
     */
    ProjectVersion edit(ProjectVersionEditDTO dto, UserDto currentUser);

    /**
     * 更改版本的状态
     * @param dto
     * @param currentUser
     * @return
     */
    Boolean changeStatus(ProjectVersion dto, UserDto currentUser);

    /**
     * 根据版本id删除版本
     * @param dto
     * @param currentUser
     * @return
     */
    Boolean removeProjectVersion(ProjectVersion dto, UserDto currentUser);
}
