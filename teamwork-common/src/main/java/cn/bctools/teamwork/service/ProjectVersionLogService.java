package cn.bctools.teamwork.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import cn.bctools.teamwork.entity.ProjectVersionLog;

/**
 *
 * @author Auto Generator
 */
public interface ProjectVersionLogService extends IService<ProjectVersionLog> {

    /**
     * 根据版本 查询版本的动态
     * @param page
     * @param dto
     * @return
     */
    Page<ProjectVersionLog> pageLog(Page<ProjectVersionLog> page, ProjectVersionLog dto);
}
