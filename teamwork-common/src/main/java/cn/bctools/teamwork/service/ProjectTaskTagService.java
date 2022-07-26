package cn.bctools.teamwork.service;

import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.teamwork.entity.ProjectTaskTag;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 *
 * @author Auto Generator
 */
public interface ProjectTaskTagService extends IService<ProjectTaskTag> {

    /**
     * 保存标签
     * @param dto
     * @param userDto
     * @return
     */
    ProjectTaskTag saveTag(ProjectTaskTag dto, UserDto userDto);

    /**
     * 修改标签
     * @param dto
     * @param user
     * @return
     */
    ProjectTaskTag editTag(ProjectTaskTag dto, UserDto user);

    /**
     * 根据标签id删除标签并清除任务绑定的标签
     * @param id
     * @return
     */
    Boolean removeTagById(String id);
}
