package cn.bctools.teamwork.service;

import com.baomidou.mybatisplus.extension.service.IService;
import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.teamwork.dto.ProjectTaskWorkflowAddDTO;
import cn.bctools.teamwork.entity.ProjectTask;
import cn.bctools.teamwork.entity.ProjectTaskWorkflow;

import java.util.List;

/**
 *
 * @author Auto Generator
 */
public interface ProjectTaskWorkflowService extends IService<ProjectTaskWorkflow> {
    /**
     * 根据项目查询项目的规则
     * @param dto
     * @return
     */
    List<ProjectTaskWorkflow> listWorkflow(ProjectTaskWorkflow dto);

    /**
     * 添加任务流转规则
     * @param dto
     * @param user
     * @return
     */
    Boolean saveWorkFlow(ProjectTaskWorkflowAddDTO dto, UserDto user);

    /**
     * 根据任务 查询项目定义的流转规则，根据场景进行流转
     * @param projectTask
     * @param action  场景操作
     * @param userDto 当前用户
     * @param memberId 指定执行人
     *
     */
    void queryRuleAndOperation(ProjectTask projectTask,String memberId,UserDto userDto,Integer action);

    /**
     * 删除规则
     * @param dto
     * @return
     */
    Boolean delete(ProjectTaskWorkflow dto);

    /**
     * 流转规则编辑
     * @param dto
     * @param login
     * @return
     */
    Boolean updateFlow(ProjectTaskWorkflowAddDTO dto,UserDto login);
}
