package cn.bctools.teamwork.service;

import com.baomidou.mybatisplus.extension.service.IService;
import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.teamwork.entity.ProjectInfoDefine;

/**
 *
 * @author Auto Generator
 */
public interface ProjectInfoDefineService extends IService<ProjectInfoDefine> {

    /**
     * 概览-项目信息保存
     * @param dto
     * @param user
     * @return
     */
    ProjectInfoDefine saveProjectInfo(ProjectInfoDefine dto, UserDto user);

    /**
     * 概览-项目信息编辑
     * @param dto
     * @param user
     * @return
     */
    ProjectInfoDefine editProjectInfo(ProjectInfoDefine dto,UserDto user);
}
