package cn.bctools.teamwork.service;

import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.utils.R;
import cn.bctools.teamwork.entity.ProjectFileInfo;
import cn.bctools.teamwork.entity.ProjectInfo;
import cn.bctools.teamwork.entity.ProjectLog;
import cn.bctools.teamwork.vo.BurnDownChartVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @author Auto Generator
 */
public interface ProjectInfoService extends IService<ProjectInfo> {

    /**
     * 保存项目的信息
     *
     * @param dto
     * @param userDto
     * @return
     */
    R<ProjectInfo> saveProject(ProjectInfo dto, UserDto userDto);

    /**
     * 根据项目id 查看项目的详情
     *
     * @param dto
     * @return
     */
    ProjectInfo detail(ProjectInfo dto);

    /**
     * 修改项目信息
     *
     * @param dto
     * @param userDto
     * @return
     */
    ProjectInfo edit(ProjectInfo dto, UserDto userDto);


    /**
     * 根据项目的主键id，删除项目（放入回收站）
     *
     * @param dto
     * @param userDto
     * @return
     */
    Boolean recycle(ProjectInfo dto, UserDto userDto);

    /**
     * 根据项目的主键id，从回收站恢复
     *
     * @param dto
     * @param userDto
     * @return
     */
    Boolean recovery(ProjectInfo dto, UserDto userDto);

    /**
     * 根据项目主键id 项目归档
     *
     * @param dto
     * @param userDto
     * @return
     */
    Boolean archive(ProjectInfo dto, UserDto userDto);

    /**
     * 根据项目主键id 取消项目归档
     *
     * @param dto
     * @param userDto
     * @return
     */
    Boolean recoveryArchive(ProjectInfo dto, UserDto userDto);

    /**
     * 根据id查询未删除的项目信息
     *
     * @param projectId
     * @return
     */
    ProjectInfo getNotDelById(String projectId);

    /**
     * 验证项目是否在回收站 不在则返回项目信息
     * 存在则抛出异常信息
     *
     * @param projectInfo 项目信息
     * @return 项目信息
     */
    ProjectInfo verifyDel(ProjectInfo projectInfo);

    /**
     * 验证项目是否存在  存在则返回项目信息
     * 不存在则抛出异常信息
     *
     * @param projectInfo 项目信息
     * @return 项目信息
     */
    ProjectInfo verifyExist(ProjectInfo projectInfo);

    /**
     * 根据项目id查询项目的动态，包括任务
     *
     * @param
     * @param dto
     * @return
     */
    List<ProjectLog> listProjectLog(ProjectLog dto);

    /**
     * 项目上传文件
     *
     * @param fileInfo
     * @param projectInfo
     * @param user
     * @return
     */
    Boolean uploadProjectFile(ProjectFileInfo fileInfo, ProjectInfo projectInfo, UserDto user);

    /**
     * 根据项目获取燃尽图数据
     *
     * @param projectId
     * @return
     */
    BurnDownChartVO burnDownChart(String projectId);

    /**
     * 根据项目id 更新进度
     *
     * @param projectId 项目id
     * @return
     */
    void updateSchedule(String projectId);
}
