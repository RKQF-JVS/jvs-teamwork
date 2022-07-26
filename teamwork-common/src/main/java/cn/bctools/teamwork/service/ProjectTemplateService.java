package cn.bctools.teamwork.service;

import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.teamwork.entity.ProjectTemplate;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author Auto Generator
 */
public interface ProjectTemplateService extends IService<ProjectTemplate> {

    /**
     * 新增保存模板信息
     *
     * @param dto
     * @param login
     * @return
     */
    ProjectTemplate save(ProjectTemplate projectTemplate, UserDto login);

    /**
     * 修改模板信息
     *
     * @param dto
     * @param login
     * @return
     */
    ProjectTemplate updateTemplate(ProjectTemplate projectTemplate, UserDto login);

    /**
     * 删除模板并清除模板任务列表
     *
     * @param dto
     * @return
     */
    Boolean removeTemplate(String id);

    /**
     * 查询模板分页信息
     *
     * @param page
     * @param dto
     * @return
     */
    void getPage(Page<ProjectTemplate> page, ProjectTemplate dto,UserDto userDto);
}
