package cn.bctools.teamwork.service.impl;

import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.teamwork.common.enums.LogActionTypeEnum;
import cn.bctools.teamwork.common.enums.TaskLogTypeEnum;
import cn.bctools.teamwork.dto.TaskMemberAddDTO;
import cn.bctools.teamwork.entity.ProjectLog;
import cn.bctools.teamwork.entity.ProjectTaskMember;
import cn.bctools.teamwork.mapper.ProjectTaskMemberMapper;
import cn.bctools.teamwork.service.ProjectLogService;
import cn.bctools.teamwork.service.ProjectTaskMemberService;
import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 *
 * @author Auto Generator
 */
@Service

public class ProjectTaskMemberServiceImpl extends ServiceImpl<ProjectTaskMemberMapper, ProjectTaskMember> implements ProjectTaskMemberService {
    @Autowired
    ProjectLogService projectLogService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProjectTaskMember inviteMember(TaskMemberAddDTO taskMember, UserDto user, boolean isLog) {
        UserDto member = taskMember.getMemberInfo();
        ProjectTaskMember ptm = new ProjectTaskMember();
        BeanUtil.copyProperties(taskMember,ptm);
        ptm.initSave(user);

        this.save(ptm);
        if(isLog){
            //保存添加成员的日志
            ProjectLog log1 = new ProjectLog() {{
                setRemark(String.format(TaskLogTypeEnum.INVITE_MEMBER.getRemark(),member.getRealName()));
                setContent(member.getRealName());
                setTaskId(taskMember.getTaskId());
                setType(TaskLogTypeEnum.INVITE_MEMBER.getCode());
                setIsComment(0);
                setMemberId(user.getId());
                setActionType(LogActionTypeEnum.TASK.getCode());
                setToMemberId(user.getId());
            }};
            log1.initSave(user);
            projectLogService.save(log1);
        }
        return ptm;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean removeMember(TaskMemberAddDTO taskMember, UserDto user, boolean isLog) {
        UserDto member = taskMember.getMemberInfo();
        LambdaQueryWrapper<ProjectTaskMember> wrapper = new LambdaQueryWrapper<>();
        //查询被删除的成员是否是执行者,若是，则将执行者替换为拥有者
        wrapper.eq(ProjectTaskMember::getMemberId,member.getId())
                .eq(ProjectTaskMember::getTaskId,taskMember.getTaskId());
        ProjectTaskMember tm = this.getOne(wrapper);
        //若为拥有者 不执行
        if(tm.getIsOwner()==1){
            return false;
        }
        if(tm.getIsExecutor()==1){
            ProjectTaskMember owner = this.lambdaQuery().eq(ProjectTaskMember::getTaskId, taskMember.getTaskId())
                    .eq(ProjectTaskMember::getIsOwner, 1).one();
            owner.setIsExecutor(1);
            owner.initSave(user);
            this.updateById(owner);
        }
        boolean b = this.removeById(tm);
        if(isLog){
            //保存添加成员的日志
            ProjectLog log1 = new ProjectLog() {{
                setRemark(String.format(TaskLogTypeEnum.REMOVE_MEMBER.getRemark(),member.getRealName()));
                setContent(member.getRealName());
                setTaskId(taskMember.getTaskId());
                setType(TaskLogTypeEnum.REMOVE_MEMBER.getCode());
                setIsComment(0);
                setMemberId(user.getId());
                setActionType(LogActionTypeEnum.TASK.getCode());
                setToMemberId(user.getId());
            }};
            log1.initSave(user);
            projectLogService.save(log1);
        }
        return b;
    }

    @Override
    public Boolean verifyExists(String memberId, String taskId) {
        ProjectTaskMember one = this.lambdaQuery().eq(ProjectTaskMember::getTaskId, taskId)
                .eq(ProjectTaskMember::getMemberId, memberId).one();
        return BeanUtil.isNotEmpty(one);
    }

    @Override
    public List<ProjectTaskMember> getMemberByTaskId(String taskId) {
        return lambdaQuery().eq(ProjectTaskMember::getTaskId,taskId).list();
    }
}
