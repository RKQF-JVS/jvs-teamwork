package cn.bctools.teamwork.service.impl;

import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.teamwork.dto.ProjectTaskWorkflowRuleDTO;
import cn.bctools.teamwork.entity.ProjectTaskWorkflow;
import cn.bctools.teamwork.entity.ProjectTaskWorkflowRule;
import cn.bctools.teamwork.mapper.ProjectTaskWorkflowRuleMapper;
import cn.bctools.teamwork.service.ProjectTaskWorkflowRuleService;
import cn.bctools.teamwork.service.ProjectTaskWorkflowService;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Auto Generator
 */
@Service
public class ProjectTaskWorkflowRuleServiceImpl extends ServiceImpl<ProjectTaskWorkflowRuleMapper, ProjectTaskWorkflowRule> implements ProjectTaskWorkflowRuleService {

    @Autowired
    ProjectTaskWorkflowService projectTaskWorkflowService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveRule(ProjectTaskWorkflow workflow, ProjectTaskWorkflowRuleDTO rule, UserDto user) {
        //若有id 则是编辑 先删除规则
        if (StrUtil.isNotEmpty(workflow.getId())) {
            remove(Wrappers.<ProjectTaskWorkflowRule>lambdaQuery().eq(ProjectTaskWorkflowRule::getWorkflowId, workflow.getId()));
        }
        ArrayList<ProjectTaskWorkflowRule> list = new ArrayList<>();
        ProjectTaskWorkflowRule rule1 = new ProjectTaskWorkflowRule().setObjectId(rule.getMouldId());
        rule1.setAction(0)
                .setType(0)
                .setWorkflowId(workflow.getId())
                .setSort(1);
        rule1.initSave(user);
        list.add(rule1);
        ProjectTaskWorkflowRule rule2 = new ProjectTaskWorkflowRule();
        ProjectTaskWorkflowRuleDTO.RuleActionResult first = rule.getFirst();
        rule2.setAction(first.getAction())
                .setType(first.getAction() == 3 ? 1 : 0)
                .setObjectId(first.getValue())
                .setSort(2)
                .setWorkflowId(workflow.getId());
        rule2.initSave(user);
        list.add(rule2);
        ProjectTaskWorkflowRule rule3 = new ProjectTaskWorkflowRule();
        ProjectTaskWorkflowRuleDTO.RuleActionResult second = rule.getSecond();
        rule3.setAction(second.getAction())
                .setType(second.getAction() == 3 ? 1 : 0)
                .setSort(3)
                .setObjectId(second.getValue())
                .setWorkflowId(workflow.getId());
        rule3.initSave(user);
        list.add(rule3);
        ProjectTaskWorkflowRule rule4 = new ProjectTaskWorkflowRule();
        ProjectTaskWorkflowRuleDTO.RuleActionResult third = rule.getThird();
        rule4.setWorkflowId(workflow.getId())
                .setAction(third.getAction())
                .setType(second.getAction() == 3 ? 1 : 0)
                .setObjectId(third.getValue())
                .setSort(4);
        rule4.initSave(user);
        list.add(rule4);
        ProjectTaskWorkflowRule rule5 = new ProjectTaskWorkflowRule();
        String state = rule.getState();
        int anInt = Integer.parseInt(state);
        rule5.setWorkflowId(workflow.getId())
                .setAction(anInt)
                .setType(anInt)
                .setObjectId(state)
                .setSort(5);
        rule5.initSave(user);
        list.add(rule5);
        return this.saveBatch(list);
    }

    @Override
    public ProjectTaskWorkflowRuleDTO getRuleByFlowId(String workFlowId) {
        ProjectTaskWorkflowRuleDTO dto = new ProjectTaskWorkflowRuleDTO();
        List<ProjectTaskWorkflowRule> list = lambdaQuery().eq(ProjectTaskWorkflowRule::getWorkflowId, workFlowId).list();
        if (CollUtil.isNotEmpty(list)) {
            ProjectTaskWorkflowRuleDTO.RuleActionResult result;
            for (ProjectTaskWorkflowRule x : list) {

                result = new ProjectTaskWorkflowRuleDTO.RuleActionResult();
                result.setAction(x.getAction());
                result.setValue(x.getObjectId());
                Integer sort = x.getSort();
                if (sort == 1) {
                    dto.setMouldId(x.getObjectId());
                }
                if (sort == 2) {
                    dto.setFirst(result);
                }
                if (sort == 3) {
                    dto.setSecond(result);
                }
                if (sort == 4) {
                    dto.setThird(result);
                }
                if (sort == 5) {
                    dto.setState(x.getObjectId());
                }

            }

        }

        return dto;
    }
}
