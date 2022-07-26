package cn.bctools.teamwork.service.impl;

import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.teamwork.common.consts.CommonConst;
import cn.bctools.teamwork.dto.ProjectTaskWorkflowAddDTO;
import cn.bctools.teamwork.entity.ProjectTask;
import cn.bctools.teamwork.entity.ProjectTaskMember;
import cn.bctools.teamwork.entity.ProjectTaskWorkflow;
import cn.bctools.teamwork.entity.ProjectTaskWorkflowRule;
import cn.bctools.teamwork.mapper.ProjectTaskWorkflowMapper;
import cn.bctools.teamwork.service.ProjectTaskMemberService;
import cn.bctools.teamwork.service.ProjectTaskService;
import cn.bctools.teamwork.service.ProjectTaskWorkflowRuleService;
import cn.bctools.teamwork.service.ProjectTaskWorkflowService;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author Auto Generator
 */
@Service
public class ProjectTaskWorkflowServiceImpl extends ServiceImpl<ProjectTaskWorkflowMapper, ProjectTaskWorkflow> implements ProjectTaskWorkflowService {
    @Autowired
    ProjectTaskWorkflowRuleService projectTaskWorkflowRuleService;
    @Autowired
    StringRedisTemplate stringRedisTemplate;
    @Autowired
    ProjectTaskMemberService projectTaskMemberService;
    @Autowired
    ProjectTaskService projectTaskService;

    @Override
    public List<ProjectTaskWorkflow> listWorkflow(ProjectTaskWorkflow dto) {
        LambdaQueryWrapper<ProjectTaskWorkflow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProjectTaskWorkflow::getProjectId, dto.getProjectId())
                .eq(ProjectTaskWorkflow::getOrganizationId, dto.getOrganizationId())
                .orderByAsc(ProjectTaskWorkflow::getCreateTime);
        return list(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveWorkFlow(ProjectTaskWorkflowAddDTO dto, UserDto user) {

        ProjectTaskWorkflow workflow = new ProjectTaskWorkflow().setProjectId(dto.getProjectId())
                .setName(dto.getName())
                .setOrganizationId(dto.getOrganizationId());
        workflow.initSave(user);
        boolean save = this.save(workflow);
        boolean rule = projectTaskWorkflowRuleService.saveRule(workflow, dto.getRule(), user);
        //删除缓存中的规则
        stringRedisTemplate.delete(CommonConst.PROJECT_RULE + dto.getProjectId());
        return save && rule;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void queryRuleAndOperation(ProjectTask task, String memberId, UserDto loginUser, Integer action) {
        //此项目的所有规则
        List<ProjectTaskWorkflow> flowList = getTaskRule(task.getProjectId());
        //匹配规则
        List<ProjectTaskWorkflow> workFlow = getMatchingRule(flowList, task, memberId, action);
        //遍历规则，任务修改
        LambdaUpdateWrapper<ProjectTask> taskWrapper = Wrappers.<ProjectTask>lambdaUpdate().eq(ProjectTask::getId, task.getId());
        if (CollUtil.isNotEmpty(workFlow)) {
            workFlow.forEach(x -> {
                List<ProjectTaskWorkflowRule> ruleList = x.getWorkflowRuleList();
                //第三场景
                ProjectTaskWorkflowRule rule03 = ruleList.stream().filter(o1 -> o1.getSort() == 3).findFirst().orElse(null);
                //分为指派 和 流转
                if (BeanUtil.isNotEmpty(rule03) && rule03.getAction() == 3) {
                    taskWrapper.set(ProjectTask::getExecuteMemberId, rule03.getObjectId());
                    if (StrUtil.isNotEmpty(rule03.getObjectId())) {
                        //若不为空 则更改执行人
                        updateTaskMember(task.getId(), rule03.getObjectId());
                    }
                } else {
                    taskWrapper.set(ProjectTask::getMouldId, rule03.getObjectId());
                }
                //第四场景和第五场景 （指派|流转） 、  改变状态
                ProjectTaskWorkflowRule rule04 = ruleList.stream().filter(o1 -> o1.getSort() == 4).findFirst().orElse(null);
                ProjectTaskWorkflowRule rule05 = ruleList.stream().filter(o1 -> o1.getSort() == 5).findFirst().orElse(null);
                //第三场景若为指派 第四场景就为流转 第四场景的值可为null
                if (BeanUtil.isNotEmpty(rule03) && BeanUtil.isNotEmpty(rule04) && StrUtil.isNotEmpty(rule04.getObjectId())) {
                    if (rule03.getAction() == 3) {
                        taskWrapper.set(ProjectTask::getMouldId, rule04.getObjectId());
                    }
                    if (rule03.getAction() == 0) {
                        taskWrapper.set(ProjectTask::getExecuteMemberId, rule04.getObjectId());
                        if (StrUtil.isNotEmpty(rule04.getObjectId())) {
                            updateTaskMember(task.getId(), rule04.getObjectId());
                        }
                    }
                }
                if (BeanUtil.isNotEmpty(rule05)) {
                    //第五场景 改变状态
                    if (rule05.getAction() == 1) {
                        taskWrapper.set(ProjectTask::getFinishStatus, 1);
                    }
                    if (rule05.getAction() == 2) {
                        taskWrapper.set(ProjectTask::getFinishStatus, 0);
                    }
                }
                projectTaskService.update(taskWrapper);
            });
        }

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(ProjectTaskWorkflow dto) {
        boolean remove = remove(Wrappers.<ProjectTaskWorkflow>lambdaQuery().eq(ProjectTaskWorkflow::getId, dto.getId()));
        boolean removeRule = projectTaskWorkflowRuleService.remove(Wrappers.<ProjectTaskWorkflowRule>lambdaQuery().eq(ProjectTaskWorkflowRule::getWorkflowId, dto.getId()));
        ProjectTaskWorkflow one = lambdaQuery().select(ProjectTaskWorkflow::getProjectId).eq(ProjectTaskWorkflow::getId, dto.getId()).one();
        if (BeanUtil.isNotEmpty(one)) {
            //删除缓存中的规则
            stringRedisTemplate.delete(CommonConst.PROJECT_RULE + one.getProjectId());
        }
        if (remove && removeRule) {
            return true;
        } else {
            throw new BusinessException("删除失败!");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateFlow(ProjectTaskWorkflowAddDTO dto, UserDto login) {

        ProjectTaskWorkflow workflow = new ProjectTaskWorkflow().setProjectId(dto.getProjectId())
                .setName(dto.getName())
                .setOrganizationId(dto.getOrganizationId())
                .setId(dto.getId());
        boolean update = lambdaUpdate().set(ProjectTaskWorkflow::getName, dto.getName())
                .set(ProjectTaskWorkflow::getUpdateTime, LocalDateTime.now())
                .set(ProjectTaskWorkflow::getUpdateBy, StrUtil.isEmpty(login.getRealName()) ? login.getAccountName() : login.getRealName())
                .eq(ProjectTaskWorkflow::getId, dto.getId()).update();
        boolean remove = projectTaskWorkflowRuleService.remove(Wrappers.<ProjectTaskWorkflowRule>lambdaQuery().eq(ProjectTaskWorkflowRule::getWorkflowId, dto.getId()));
        boolean saveRules = projectTaskWorkflowRuleService.saveRule(workflow, dto.getRule(), login);
        ProjectTaskWorkflow one = lambdaQuery().select(ProjectTaskWorkflow::getProjectId).eq(ProjectTaskWorkflow::getId, dto.getId()).one();
        if (BeanUtil.isNotEmpty(one)) {
            stringRedisTemplate.delete(CommonConst.PROJECT_RULE + one.getProjectId());
        }

        if (update && remove && saveRules) {
            return true;
        } else {
            throw new BusinessException("修改失败!");
        }
    }

    /**
     * 修改任务的执行人
     */
    private void updateTaskMember(String taskId, String memberId) {
        ProjectTaskMember one = projectTaskMemberService.lambdaQuery().eq(ProjectTaskMember::getTaskId, taskId).eq(ProjectTaskMember::getMemberId, memberId).one();
        //清除原执行人
        projectTaskMemberService.lambdaUpdate().set(ProjectTaskMember::getIsExecutor, 0).eq(ProjectTaskMember::getTaskId, taskId).update();
        //若已经存在执行人 改变执行人，若没有 则添加
        if (BeanUtil.isEmpty(one)) {
            ProjectTaskMember tm = new ProjectTaskMember() {{
                setTaskId(taskId);
                setMemberId(memberId);
                setIsExecutor(1);
            }};
            projectTaskMemberService.save(tm);
        } else {
            projectTaskMemberService.lambdaUpdate().set(ProjectTaskMember::getIsExecutor, 1).eq(ProjectTaskMember::getTaskId, taskId).eq(ProjectTaskMember::getMemberId, memberId).update();
        }
    }

    private List<ProjectTaskWorkflow> getMatchingRule(List<ProjectTaskWorkflow> flowList, ProjectTask task, String memberId, Integer action) {
        List<String> flowIds = flowList.stream().filter(x -> {
            boolean flag = false;
            List<ProjectTaskWorkflowRule> ruleList = x.getWorkflowRuleList();
            for (ProjectTaskWorkflowRule rule : ruleList) {
                //找到第一场景
                if (rule.getSort() == 1) {
                    if (rule.getObjectId().equals(task.getMouldId())) {
                        flag = true;
                    } else {
                        flag = false;
                        break;
                    }
                }
                //找到第二场景 匹配场景 ，并判断是否设置执行人
                if (rule.getSort() == 2) {
                    if (action.equals(rule.getAction())) {
                        if (action == 3) {
                            //选择的执行人是否与规则匹配
                            if (rule.getObjectId().equals(memberId)) {
                                flag = true;
                            } else {
                                flag = false;
                            }
                        } else {
                            flag = true;
                        }
                    }
                }
            }
            return flag;
        }).map(ProjectTaskWorkflow::getId).collect(Collectors.toList());
        if (CollUtil.isNotEmpty(flowIds)) {
            return flowList.stream().filter(x ->
                    flowIds.contains(x.getId())
            ).collect(Collectors.toList());
        }
        return null;
    }

    /**
     * 根据项目的id 获取项目的任务流转规则
     */

    private List<ProjectTaskWorkflow> getTaskRule(String projectId) {
        List<ProjectTaskWorkflow> list;
        String ruleJson = stringRedisTemplate.opsForValue().get(CommonConst.PROJECT_RULE + projectId);
        if (StrUtil.isEmpty(ruleJson)) {
            list = lambdaQuery().eq(ProjectTaskWorkflow::getProjectId, projectId).list();
            if (CollUtil.isNotEmpty(list)) {
                //所有的流转规则id
                List<String> flowIds = list.stream().map(ProjectTaskWorkflow::getId).collect(Collectors.toList());
                List<ProjectTaskWorkflowRule> ruleList = projectTaskWorkflowRuleService.lambdaQuery().in(ProjectTaskWorkflowRule::getWorkflowId, flowIds).
                        orderByAsc(ProjectTaskWorkflowRule::getSort).list();
                if (CollUtil.isNotEmpty(ruleList)) {
                    //根据任务规则id 分组
                    Map<String, List<ProjectTaskWorkflowRule>> group = ruleList.stream().collect(Collectors.groupingBy(ProjectTaskWorkflowRule::getWorkflowId));
                    list.forEach(x -> {
                        x.setWorkflowRuleList(group.get(x.getId()));
                    });
                }
            }
            String ruleStr = JSON.toJSONString(list);
            //保存到缓存
            stringRedisTemplate.opsForValue().set(CommonConst.PROJECT_RULE + projectId, ruleStr, 60, TimeUnit.MINUTES);
        } else {
            list = JSON.parseArray(ruleJson, ProjectTaskWorkflow.class);
        }
        return list;
    }
}
