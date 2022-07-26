package cn.bctools.teamwork.controller;

import cn.bctools.auth.api.api.AuthUserServiceApi;
import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.utils.R;
import cn.bctools.log.annotation.Log;
import cn.bctools.oauth2.utils.UserCurrentUtils;
import cn.bctools.teamwork.dto.TaskMemberAddDTO;
import cn.bctools.teamwork.entity.ProjectMember;
import cn.bctools.teamwork.entity.ProjectTask;
import cn.bctools.teamwork.entity.ProjectTaskMember;
import cn.bctools.teamwork.service.ProjectMemberService;
import cn.bctools.teamwork.service.ProjectTaskMemberService;
import cn.bctools.teamwork.service.ProjectTaskService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 任务-成员表
 * <p>
 * FIXME 需要注意，该类为自动生成，需结合真实业务进行修改
 *
 * @author Auto Generator
 */
@Api(tags = "任务-成员表")
@RestController
@AllArgsConstructor
@RequestMapping("/ProjectTaskMember")
public class ProjectTaskMemberController {

    ProjectTaskMemberService projectTaskMemberService;

    AuthUserServiceApi userServiceApi;

    ProjectTaskService projectTaskService;

    ProjectMemberService projectMemberService;

    @Log
    @ApiOperation("获取参与者")
    @GetMapping
    public R<List<ProjectTaskMember>> getInviteMember(@RequestParam String taskId) {
        List<ProjectTaskMember> list = projectTaskMemberService.list(new LambdaQueryWrapper<ProjectTaskMember>().eq(ProjectTaskMember::getTaskId, taskId));
        if (list.isEmpty()){
            return R.ok(new ArrayList<>());
        }
        List<String> ids = list.stream().map(ProjectTaskMember::getMemberId).collect(Collectors.toList());
        Map<String, UserDto> userDtoMap = userServiceApi.getByIds(ids).getData().stream().collect(Collectors.toMap(UserDto::getId, Function.identity()));
        list.stream().peek(e->e.setUserDto(userDtoMap.get(e.getMemberId()))).collect(Collectors.toList());
        return R.ok(list);
    }
    @Log
    @ApiOperation("任务添加参与者")
    @PostMapping("/inviteMember")
    @Transactional(rollbackFor = Exception.class)
    public R inviteMember(@RequestBody TaskMemberAddDTO dto) {
        UserDto user = UserCurrentUtils.getCurrentUser();
        //获取用户信息
        ProjectTask projectTask = projectTaskService.getById(dto.getTaskId());
        List<String> memberIds = projectMemberService.lambdaQuery()
                .eq(ProjectMember::getProjectId, projectTask.getProjectId())
                .select(ProjectMember::getMemberId)
                .list()
                .stream()
                .map(ProjectMember::getMemberId)
                .collect(Collectors.toList());
        //获取所有用户信息
        Map<String, UserDto> userDtoMap = userServiceApi.getByIds(memberIds)
                .getData()
                .stream()
                .collect(Collectors.toMap(UserDto::getId, Function.identity()));
        //先获取所有成员
        List<String> projectTaskMemberIds = projectTaskMemberService.list(new LambdaQueryWrapper<ProjectTaskMember>()
                .eq(ProjectTaskMember::getTaskId, dto.getTaskId()))
                .stream()
                .map(ProjectTaskMember::getMemberId)
                .collect(Collectors.toList());

        //这是新增任务成员id
        List<String> addMembersIdS = dto.getMemberIds();
        if (dto.getIsAll()) {
            //若选择所有成员，新增成员为项目所有成员
            addMembersIdS = memberIds;
        }
        List<String> addMembersIdCopy = addMembersIdS;
        //排除已经存在的 找出新增中不同成员 新增
        List<String> saveList = addMembersIdCopy.stream().filter(e -> !projectTaskMemberIds.contains(e)).collect(Collectors.toList());
        //找到需要删除的数据 找出任务中存在不同成员 删除
        List<String> removerIds = projectTaskMemberIds.stream().filter(e -> !addMembersIdCopy.contains(e)).collect(Collectors.toList());
        //删除数据
        removerIds.forEach(e -> {
            TaskMemberAddDTO re = new TaskMemberAddDTO() {{
                setTaskId(dto.getTaskId());
                setMemberInfo(userDtoMap.get(e));
            }};
            projectTaskMemberService.removeMember(re, user, true);
        });
        saveList.forEach(e -> {
            TaskMemberAddDTO add = new TaskMemberAddDTO() {{
                setMemberId(e);
                setTaskId(dto.getTaskId());
                setMemberInfo(userDtoMap.get(e));
                setIsOwner(0);
                setIsExecutor(0);
            }};
            projectTaskMemberService.inviteMember(add, user, true);
        });
        return R.ok();
    }
}
