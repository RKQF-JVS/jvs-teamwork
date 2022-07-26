package cn.bctools.teamwork.service;

import com.baomidou.mybatisplus.extension.service.IService;
import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.teamwork.dto.ProjectTaskWorkflowRuleDTO;
import cn.bctools.teamwork.entity.ProjectTaskWorkflow;
import cn.bctools.teamwork.entity.ProjectTaskWorkflowRule;

/**
 * @author Auto Generator
 */
public interface ProjectTaskWorkflowRuleService extends IService<ProjectTaskWorkflowRule> {

    /**
     * 保存任务流转规则具体实现
     *
     * @param workflow
     * @param rule
     * @param user
     * @return
     */
    boolean saveRule(ProjectTaskWorkflow workflow, ProjectTaskWorkflowRuleDTO rule, UserDto user);

    /**
     * 根据规则id 查询具体实现
     * @param workFlowId
     * @return
     */
    ProjectTaskWorkflowRuleDTO getRuleByFlowId(String workFlowId);
}
