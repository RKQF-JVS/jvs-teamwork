package cn.bctools.teamwork.service.impl;

import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.utils.R;
import cn.bctools.teamwork.common.enums.LogActionTypeEnum;
import cn.bctools.teamwork.common.enums.ProjectLogTypeEnum;
import cn.bctools.teamwork.dto.ProjectLogAddBO;
import cn.bctools.teamwork.dto.ProjectMemberInvite;
import cn.bctools.teamwork.entity.ProjectMember;
import cn.bctools.teamwork.mapper.ProjectMemberMapper;
import cn.bctools.teamwork.service.ProjectLogService;
import cn.bctools.teamwork.service.ProjectMemberService;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Auto Generator
 */
@Service
@RequiredArgsConstructor
public class ProjectMemberServiceImpl extends ServiceImpl<ProjectMemberMapper, ProjectMember> implements ProjectMemberService {
    private final ProjectLogService projectLogService;

    @Override
    public Boolean isProjectMember(String projectId, String memberId) {
        LambdaQueryWrapper<ProjectMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProjectMember::getProjectId, projectId).eq(ProjectMember::getMemberId, memberId);
        ProjectMember projectMember = this.getOne(wrapper);
        return BeanUtil.isNotEmpty(projectMember);

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<ProjectMember> inviteMember(ProjectMemberInvite dto, UserDto userDto) {
        //增加项目成员
        ProjectMember projectMember = new ProjectMember();
        projectMember.setProjectId(dto.getProjectId())
                .setJoinTime(LocalDateTime.now())
                .setMemberId(dto.getMemberId());
        projectMember.initSave(userDto);
        this.save(projectMember);
        //增加日志
        ProjectLogAddBO inviteMemberLog = new ProjectLogAddBO();
        inviteMemberLog.setProjectId(dto.getProjectId())
                .setLogActionTypeEnum(LogActionTypeEnum.PROJECT)
                .setProjectLogTypeEnum(ProjectLogTypeEnum.INVITE_MEMBER)
                .setToMemberId(dto.getMemberId())
                .setContent(dto.getMemberName())
                .setFormatContent(new String[]{dto.getMemberName()});
        projectLogService.runLog(inviteMemberLog,userDto);
        return R.ok(projectMember);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean removeMember(ProjectMemberInvite dto, UserDto userDto,Boolean flag) {

        LambdaQueryWrapper<ProjectMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProjectMember::getMemberId, dto.getMemberId())
                .eq(ProjectMember::getProjectId, dto.getProjectId());
        boolean result = this.remove(wrapper);
        // 成功增加日志
        if (result && flag) {

            ProjectLogAddBO removeMemberLog = new ProjectLogAddBO();
            removeMemberLog.setProjectId(dto.getProjectId())
                    .setLogActionTypeEnum(LogActionTypeEnum.PROJECT)
                    .setProjectLogTypeEnum(ProjectLogTypeEnum.REMOVE_MEMBER)
                    .setToMemberId(dto.getMemberId())
                    .setContent(dto.getMemberName())
                    .setFormatContent(new String[]{dto.getMemberName()});
            projectLogService.runLog(removeMemberLog,userDto);
        }
        return result;
    }

    @Override
    public List<ProjectMember> listProjectMember(String projectId) {
        List<ProjectMember> list = this.lambdaQuery()
                .eq(ProjectMember::getProjectId, projectId).list();
        return list;
    }

    @Override
    public List<ProjectMember> getProjectByMembers(ArrayList<String> memberIds) {
        LambdaQueryWrapper<ProjectMember> wrapper = Wrappers.lambdaQuery(ProjectMember.class)
                .in(ProjectMember::getMemberId, memberIds)
                .orderByDesc(ProjectMember::getCreateTime);
        List<ProjectMember> list = list(wrapper);
        if(CollUtil.isNotEmpty(list)){
            return list;
        }
        return new ArrayList<>();
    }


}
