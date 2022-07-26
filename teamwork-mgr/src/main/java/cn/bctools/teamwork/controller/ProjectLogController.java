package cn.bctools.teamwork.controller;

import cn.bctools.auth.api.api.AuthUserServiceApi;
import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.utils.R;
import cn.bctools.log.annotation.Log;
import cn.bctools.teamwork.common.enums.LogActionTypeEnum;
import cn.bctools.teamwork.dto.ProjectLogInfoDTO;
import cn.bctools.teamwork.entity.ProjectLog;
import cn.bctools.teamwork.entity.ProjectTask;
import cn.bctools.teamwork.service.ProjectInfoService;
import cn.bctools.teamwork.service.ProjectLogService;
import cn.bctools.teamwork.service.ProjectTaskService;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 项目日志表
 * <p>
 * FIXME 需要注意，该类为自动生成，需结合真实业务进行修改
 *
 * @author Auto Generator
 */
@Api(tags = "项目日志表")
@RestController
@AllArgsConstructor
@RequestMapping("/ProjectLog")
public class ProjectLogController {

    ProjectLogService service;
    ProjectInfoService projectInfoService;
    ProjectTaskService projectTaskService;
    AuthUserServiceApi userServiceApi;
    ProjectLogService projectLogService;


    @Log
    @ApiOperation("项目和任务动态 分页查询 需要传递任务id与项目id其中一个,不可都传")
    @GetMapping("/listProjectLog")
    public R<ProjectLogInfoDTO> listProjectLog(ProjectLog dto) {
        LambdaQueryWrapper<ProjectLog> wrapper = new LambdaQueryWrapper<ProjectLog>()
                .orderByDesc(ProjectLog::getCreateTime);
        if (StrUtil.isNotEmpty(dto.getProjectId())) {
            wrapper.eq(ProjectLog::getProjectId, dto.getProjectId())
                    .eq(ProjectLog::getIsComment, 0);
        }
        if (StrUtil.isNotEmpty(dto.getTaskId())) {
            //获取任务的子级
            List<String> projectIds = projectTaskService.list(new LambdaQueryWrapper<ProjectTask>().eq(ProjectTask::getPid, dto.getTaskId()))
                    .stream().map(ProjectTask::getId).collect(Collectors.toList());
            projectIds.add(dto.getTaskId());
            wrapper.in(ProjectLog::getTaskId, projectIds)
                    .eq(ProjectLog::getActionType, LogActionTypeEnum.TASK.getCode())
                    .eq(BeanUtil.isNotEmpty(dto.getIsComment()), ProjectLog::getIsComment, dto.getIsComment());
        }
        //获取总条数
        int count = projectLogService.count(wrapper);
        if (dto.getIsSize()) {
            wrapper.last("limit 20");
        }
        List<ProjectLog> list = projectLogService.list(wrapper);
        if (list.isEmpty()) {
            return R.ok();
        }
        List<String> memberIds = list.stream().map(ProjectLog::getMemberId).collect(Collectors.toList());
        Map<String, UserDto> users = userServiceApi.getByIds(memberIds).getData().stream().collect(Collectors.toMap(UserDto::getId, Function.identity()));
        list.stream().peek(e -> {
            e.setMemberInfo(users.get(e.getMemberId()));
        }).collect(Collectors.toList());
        ProjectLogInfoDTO logInfoDTO = new ProjectLogInfoDTO().setTotal(count).setData(list);
        return R.ok(logInfoDTO);
    }


}
