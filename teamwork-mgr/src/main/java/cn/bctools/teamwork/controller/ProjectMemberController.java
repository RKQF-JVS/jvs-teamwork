package cn.bctools.teamwork.controller;

import cn.bctools.auth.api.api.AuthUserServiceApi;
import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.R;
import cn.bctools.log.annotation.Log;
import cn.bctools.oauth2.utils.UserCurrentUtils;
import cn.bctools.teamwork.common.util.PageUtil;
import cn.bctools.teamwork.dto.ProjectMemberInfoDTO;
import cn.bctools.teamwork.dto.ProjectMemberInvite;
import cn.bctools.teamwork.entity.ProjectInfo;
import cn.bctools.teamwork.entity.ProjectMember;
import cn.bctools.teamwork.entity.ProjectTaskMember;
import cn.bctools.teamwork.resp.ProjectMemberInviteDTO;
import cn.bctools.teamwork.service.ProjectInfoService;
import cn.bctools.teamwork.service.ProjectMemberService;
import cn.bctools.teamwork.service.ProjectTaskMemberService;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 项目-成员表
 * <p>
 * FIXME 需要注意，该类为自动生成，需结合真实业务进行修改
 *
 * @author Auto Generator
 */
@Api(tags = "项目-成员表")
@RestController
@AllArgsConstructor
@RequestMapping("/ProjectMember")
public class ProjectMemberController {

    ProjectMemberService service;
    AuthUserServiceApi authUserServiceApi;
    ProjectInfoService projectInfoService;
    ProjectTaskMemberService projectTaskMemberService;


    @Log
    @ApiOperation("项目管理 我的项目 邀请新成员页面初始化")
    @PostMapping("/listForInvite")
    public R<Page<ProjectMemberInviteDTO>> listForInvite(@RequestBody ProjectInfo dto) {
        Long current = dto.getCurrent();
        Long size = dto.getSize();
        Page<ProjectMemberInviteDTO> page = new Page<>(current, size);
        String projectId = dto.getId();
        if (StrUtil.isEmpty(projectId)) {
            throw new BusinessException("请先选择项目");
        }
        Map<String, ProjectMember> memberMap = service.listProjectMember(projectId).stream().collect(Collectors.toMap(ProjectMember::getMemberId, Function.identity()));
        List<ProjectMemberInviteDTO> result = new ArrayList<>();
        List<UserDto> listMembers = authUserServiceApi.users().getData();
        //总条数
        long total = listMembers.size();
        String nickEmail = dto.getMemberNickEmail();
        boolean notEmpty = StrUtil.isNotEmpty(nickEmail);
        String realName;
        String email;

        if (CollUtil.isNotEmpty(listMembers)) {
            ProjectMemberInviteDTO pmi;
            //若有条件 则过滤
            if (notEmpty) {
                listMembers = listMembers.parallelStream().filter(e -> {
                    String eRealName = e.getRealName();
                    String eEmail = e.getEmail();
                    boolean condition = (StrUtil.isNotEmpty(eRealName) && eRealName.contains(nickEmail)) || (StrUtil.isNotEmpty(eEmail) && eEmail.contains(nickEmail));
                    return condition;
                }).collect(Collectors.toList());
                //过滤后的总条数
                total = listMembers.size();
            }
            //过滤完后组装数据
            if (CollUtil.isNotEmpty(listMembers)) {
                //分页
                listMembers = PageUtil.startPage(listMembers, current, size);
                for (UserDto user : listMembers) {
                    realName = user.getRealName();
                    email = user.getEmail();
                    pmi = new ProjectMemberInviteDTO();
                    pmi.setMemberId(user.getId())
                            .setName(realName)
                            .setAvatar(user.getHeadImg())
                            .setEmail(email)
                            .setStatus(user.getCancelFlag() ? "1" : "0")
                            .setIsJoin(memberMap.containsKey(user.getId()));
                    result.add(pmi);
                }
            }
        }
        page.setTotal(total);
        page.setRecords(result);
        return R.ok(page);
    }

    @Log
    @ApiOperation("项目管理 我的项目 邀请新成员邀请操作")
    @PostMapping("/inviteMember")
    public R<ProjectMember> inviteMember(@RequestBody ProjectMemberInvite dto) {
        UserDto currentUser = UserCurrentUtils.getCurrentUser();
        Boolean exist = service.isProjectMember(dto.getProjectId(), dto.getMemberId());
        if (exist) {
            throw new BusinessException("该项目中已存在该成员！");
        }
        return service.inviteMember(dto, currentUser);
    }


    @Log
    @ApiOperation("移除项目成员")
    @DeleteMapping("/removeMember")
    public R<Boolean> removeMember(ProjectMemberInvite dto) {
        String projectId = dto.getProjectId();
        String memberId = dto.getMemberId();
        if (StrUtil.isEmpty(memberId) || StrUtil.isEmpty(projectId)) {
            throw new BusinessException("数据异常！");
        }
        ProjectInfo project = projectInfoService.getNotDelById(projectId);
        if (memberId.equals(project.getCreateById())) {
            throw new BusinessException("不能移除项目拥有者！");
        }

        if (ObjectUtils.isEmpty(project)) {
            throw new BusinessException("该项目已失效或不存在！");
        }
        Boolean exist = service.isProjectMember(projectId, memberId);
        if (!exist) {
            return R.ok(true);
        }
        UserDto userDto = UserCurrentUtils.getCurrentUser();
        return R.ok(service.removeMember(dto, userDto, true));
    }

    @Log
    @ApiOperation("项目获取项目所有成员")
    @PostMapping("/projectMember")
    public R<Page<ProjectMemberInfoDTO>> projectMember(@RequestBody ProjectInfo dto) {
        Long size = dto.getSize();
        Long current = dto.getCurrent();
        Page<ProjectMemberInfoDTO> page = new Page<>(current, size);
        //过滤条件
        String memberNickEmail = dto.getMemberNickEmail();
        List<ProjectMember> list = service.lambdaQuery().eq(ProjectMember::getProjectId, dto.getId()).list();
        List<String> memberIds = list.stream().map(ProjectMember::getMemberId).collect(Collectors.toList());
        Map<String, UserDto> userDtoMap = authUserServiceApi.getByIds(memberIds).getData().stream().collect(Collectors.toMap(UserDto::getId, Function.identity()));
        Function<ProjectMember, ProjectMemberInfoDTO> function = (e) -> {
            ProjectMemberInfoDTO projectMemberInfoDTO = new ProjectMemberInfoDTO();
            BeanUtil.copyProperties(e, projectMemberInfoDTO);
            projectMemberInfoDTO.setMemberInfo(userDtoMap.get(e.getMemberId()));
            projectMemberInfoDTO.setIsOwner(Boolean.FALSE);
            if (e.getMemberId().equals(e.getOwnerId())) {
                projectMemberInfoDTO.setIsOwner(Boolean.TRUE);
            }
            return projectMemberInfoDTO;
        };
        List<ProjectMemberInfoDTO> collect = list.stream().map(function).collect(Collectors.toList());
        //过滤
       collect= this.filterNickAndEmail(collect,memberNickEmail);
        //todo 暂时取消分页
//        collect = PageUtil.startPage(collect, current, size);
        page.setRecords(collect);
        page.setTotal(collect.size());
        page.setSize(collect.size());
        page.setCurrent(1);
        return R.ok(page);
    }

    private List<ProjectMemberInfoDTO> filterNickAndEmail(List<ProjectMemberInfoDTO> data, String nickEmail) {
        if (StrUtil.isNotEmpty(nickEmail)) {
            data = data.stream().filter(e -> {
                UserDto memberInfo = e.getMemberInfo();
                String email = memberInfo.getEmail();
                String realName = memberInfo.getRealName();
                boolean condition = (StrUtil.isNotEmpty(email) && email.contains(nickEmail)) || (StrUtil.isNotEmpty(realName) && realName.contains(nickEmail));
                return condition;
            }).collect(Collectors.toList());
        }
        return data;
    }

    @Log
    @ApiOperation("任务获取项目所有成员")
    @GetMapping("/listProjectMember")
    public R<List<ProjectMemberInfoDTO>> listProjectMember(@RequestParam String projectId, @RequestParam(required = false) String taskId, @RequestParam(required = false) String userId) {
        List<ProjectMember> memberList = service.listProjectMember(projectId);
        if (StrUtil.isNotEmpty(userId)) {
            List<String> list = Arrays.asList(userId.split(","));
            memberList = memberList.stream().filter(e -> !list.contains(e.getMemberId())).collect(Collectors.toList());
        }
        List<ProjectMemberInfoDTO> list = fillMemberInfo(memberList, taskId);
        return R.ok(list);
    }

    private List<ProjectMemberInfoDTO> fillMemberInfo(List<ProjectMember> memberList, String taskId) {
        //如果存在任务id 就需要排除已经存在的 参与者
        if (StrUtil.isNotEmpty(taskId)) {
            List<String> taskMemberId = projectTaskMemberService.list(new LambdaQueryWrapper<ProjectTaskMember>().eq(ProjectTaskMember::getTaskId, taskId)).stream().map(ProjectTaskMember::getMemberId).collect(Collectors.toList());
            memberList = memberList.stream().filter(e -> !taskMemberId.contains(e.getMemberId())).collect(Collectors.toList());
        }
        List<String> memIds = memberList.stream().map(ProjectMember::getMemberId).collect(Collectors.toList());
        List<UserDto> users = authUserServiceApi.getByIds(memIds).getData();
        Map<String, UserDto> usersMap = users.stream().collect(Collectors.toMap(UserDto::getId, Function.identity()));
        List<ProjectMemberInfoDTO> memberInfoDTOS = memberList.stream().map(e -> {
            ProjectMemberInfoDTO infoDTO = new ProjectMemberInfoDTO();
            BeanUtil.copyProperties(e, infoDTO);
            infoDTO.setMemberInfo(usersMap.get(e.getMemberId()));
            infoDTO.setIsOwner(Boolean.FALSE);
            //是否为拥有者
            if (e.getOwnerId().equals(e.getMemberId())) {
                infoDTO.setIsOwner(Boolean.TRUE);
            }
            return infoDTO;
        }).collect(Collectors.toList());
        return memberInfoDTOS;
    }
}
