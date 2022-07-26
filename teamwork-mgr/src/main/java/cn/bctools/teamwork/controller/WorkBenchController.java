package cn.bctools.teamwork.controller;


import cn.bctools.auth.api.api.AuthUserServiceApi;
import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.R;
import cn.bctools.log.annotation.Log;
import cn.bctools.oauth2.utils.UserCurrentUtils;
import cn.bctools.oss.template.OssTemplate;
import cn.bctools.teamwork.common.common.config.CommonConfig;
import cn.bctools.teamwork.common.consts.ProjectTaskConst;
import cn.bctools.teamwork.common.enums.CollectionTypeEnum;
import cn.bctools.teamwork.common.enums.LogActionTypeEnum;
import cn.bctools.teamwork.dto.req.WorkBenchTaskReq;
import cn.bctools.teamwork.entity.*;
import cn.bctools.teamwork.service.*;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 工作台操作
 *
 * @author admin
 */
@Api(tags = "工作台")
@RestController
@AllArgsConstructor
@RequestMapping("/workbench")
public class WorkBenchController {
    private final OssTemplate ossTemplate;
    private final CommonConfig commonConfig;
    private final ProjectInfoService projectInfoService;
    private final AuthUserServiceApi authUserServiceApi;
    private final ProjectMemberService projectMemberService;
    private final ProjectTaskService projectTaskService;
    private final ProjectCollectionService projectCollectionService;
    private final ProjectTaskMemberService projectTaskMemberService;
    private final ProjectLogService projectLogService;

    @Log
    @ApiOperation("工作台 我的项目")
    @PostMapping("/selfProjectList")
    public R<Page<ProjectInfo>> selfProjectList(@RequestBody Page page) {
        UserDto currentUser = UserCurrentUtils.getCurrentUser();
        String userId = currentUser.getId();
        //查出该成员拥有的项目
        List<ProjectMember> memberList = projectMemberService.getProjectByMembers(CollUtil.newArrayList(userId));
        if (CollUtil.isEmpty(memberList)) {
            return R.ok(page);
        }
        List<String> projectIds = memberList.stream().map(ProjectMember::getProjectId).collect(Collectors.toList());
        //分页查询查不在回收站和未归档的项目
        LambdaQueryWrapper<ProjectInfo> wrapper = Wrappers.lambdaQuery(ProjectInfo.class)
                .in(ProjectInfo::getId, projectIds)
                .eq(ProjectInfo::getRecycleFlag, BigDecimal.ROUND_UP)
                .eq(ProjectInfo::getIsArchive, BigDecimal.ROUND_UP);
        projectInfoService.page(page, wrapper);
        List<ProjectInfo> projectInfoList = page.getRecords();
        if (CollUtil.isEmpty(projectInfoList)) {
            return R.ok(page);
        }
        //项目拥有者信息
        List<String> ownerIds = projectInfoList.stream().map(ProjectInfo::getCreateById).collect(Collectors.toList());
        List<UserDto> ownerList = authUserServiceApi.getByIds(ownerIds).getData();
        Map<String, UserDto> ownerMap = ownerList.stream().collect(Collectors.toMap(UserDto::getId, Function.identity()));

        //查出我的收藏的
        List<ProjectCollection> collectionList = projectCollectionService.lambdaQuery().in(ProjectCollection::getMemberId, userId)
                .eq(ProjectCollection::getType, CollectionTypeEnum.PROJECT.getCode()).list();
        boolean collectEmpty = CollUtil.isNotEmpty(collectionList);
        List<String> collectContain = new ArrayList<>();
        if (collectEmpty) {
            collectContain = collectionList.stream().map(ProjectCollection::getObjectId).collect(Collectors.toList());
        }
        List<String> collectProjectIds = collectContain;
        //项目组装数据
        projectInfoList.stream().peek(e -> {
            e.setCollectStatus(Boolean.FALSE);
            if (collectEmpty) {
                if (collectProjectIds.contains(e.getId())) {
                    e.setCollectStatus(Boolean.TRUE);
                }
            }
            e.setOwner(ownerMap.get(e.getCreateById()));
            String cover = e.getProjectCover();
            if (StrUtil.isNotEmpty(cover)) {
                e.setProjectCoverUrl(ossTemplate.fileLink(cover, commonConfig.getBucketName()));
            }
        }).collect(Collectors.toList());
        page.setRecords(projectInfoList);
        return R.ok(page);
    }


    @Log
    @ApiOperation("工作台 我的任务")
    @GetMapping("/selfTaskList")
    public R<Page<ProjectTask>> selfTaskList(Page page, WorkBenchTaskReq req) {
        UserDto currentUser = UserCurrentUtils.getCurrentUser();
        LambdaQueryWrapper<ProjectTask> wrapper = Wrappers.<ProjectTask>lambdaQuery().eq(ProjectTask::getRecycleFlag, BigDecimal.ROUND_UP);
        wrapper = getSelfTaskListWrapper(wrapper, req, currentUser);
        if (Objects.isNull(wrapper)) {
            return R.ok(page);
        }
        //获取任务
        projectTaskService.page(page, wrapper);
        List<ProjectTask> records = page.getRecords();
        if (CollUtil.isEmpty(records)) {
            return R.ok(page);
        }
        //获取项目信息
        List<String> projectIds = records.stream().map(ProjectTask::getProjectId).collect(Collectors.toList());
        List<ProjectInfo> projectInfos = projectInfoService.list(Wrappers.lambdaQuery(ProjectInfo.class).in(ProjectInfo::getId, projectIds));
        Map<String, ProjectInfo> projectInfoMap = projectInfos.stream().collect(Collectors.toMap(ProjectInfo::getId, Function.identity()));
        //获取执行者信息
        List<String> execute = records.stream().filter(e -> StrUtil.isNotEmpty(e.getExecuteMemberId())).map(ProjectTask::getExecuteMemberId).collect(Collectors.toList());
        boolean executeNotEmpty = CollUtil.isNotEmpty(execute);
        List<UserDto> userDtos = executeNotEmpty ? authUserServiceApi.getByIds(execute).getData() : new ArrayList<>();
        Map<String, UserDto> executeMap = executeNotEmpty ? userDtos.stream().collect(Collectors.toMap(UserDto::getId, Function.identity())) : new HashMap<>();
        //组装数据
        records.stream().peek(e -> {
            e.setIsChildren(Boolean.FALSE);
            if (!ProjectTaskConst.TOP_TASK_PID.equals(e.getPid())) {
                e.setIsChildren(Boolean.TRUE);
            }
            e.setProjectInfo(projectInfoMap.get(e.getProjectId()));
            String executeMemberId = e.getExecuteMemberId();
            if (executeNotEmpty && StrUtil.isNotEmpty(executeMemberId)) {
                e.setExecutor(executeMap.get(executeMemberId));
            }
        }).collect(Collectors.toList());

        return R.ok(page);
    }

    private LambdaQueryWrapper<ProjectTask> getSelfTaskListWrapper(LambdaQueryWrapper<ProjectTask> wrapper, WorkBenchTaskReq req, UserDto current) {
        wrapper.eq(ProjectTask::getFinishStatus, req.getFinishStatus());
        Integer taskType = req.getTaskType();
        String userId = current.getId();
        switch (taskType) {
            case BigDecimal.ROUND_DOWN:
                wrapper.eq(ProjectTask::getExecuteMemberId, userId);
                break;
            case BigDecimal.ROUND_CEILING:
                //我参与的，查询任务成员表
                List<ProjectTaskMember> list = projectTaskMemberService.lambdaQuery().eq(ProjectTaskMember::getMemberId, userId).select(ProjectTaskMember::getTaskId).list();
                if (CollUtil.isEmpty(list)) {
                    return null;
                }
                //获取任务id列表
                List<String> taskIds = list.stream().map(ProjectTaskMember::getTaskId).collect(Collectors.toList());
                wrapper.in(ProjectTask::getId, taskIds);
                break;
            case BigDecimal.ROUND_FLOOR:
                wrapper.eq(ProjectTask::getCreateById, userId);
                break;
            default:
                throw new BusinessException("未发现该任务类型！");
        }


        return wrapper;
    }

    @Log
    @ApiOperation("工作台 获取我的项目任务操作日志")
    @PostMapping("/getLogBySelfProject")
    public R getLogBySelfProject(@RequestBody Page page) {
        UserDto currentUser = UserCurrentUtils.getCurrentUser();
        //分页查询任务日志数据-我操作的
        projectLogService.page(page, Wrappers.lambdaQuery(ProjectLog.class)
                .eq(ProjectLog::getActionType, LogActionTypeEnum.TASK.getCode())
                .eq(ProjectLog::getMemberId,currentUser.getId())
                .orderByDesc(ProjectLog::getCreateTime));
        List<ProjectLog> records = page.getRecords();
        if (CollUtil.isEmpty(records)) {
            return R.ok(page);
        }
        //获取任务信息和项目信息
        List<String> taskIds = records.stream().map(ProjectLog::getTaskId).collect(Collectors.toList());
        List<String> projectIds = records.stream().map(ProjectLog::getProjectId).collect(Collectors.toList());
        List<ProjectInfo> projectInfoList = projectInfoService.lambdaQuery().in(ProjectInfo::getId, projectIds).list();
        Map<String, ProjectInfo> projectInfoMap = projectInfoList.stream().collect(Collectors.toMap(ProjectInfo::getId, Function.identity()));
        List<ProjectTask> projectTaskList = projectTaskService.lambdaQuery().in(ProjectTask::getId, taskIds).list();
        Map<String, ProjectTask> projectTaskMap = projectTaskList.stream().collect(Collectors.toMap(ProjectTask::getId, Function.identity()));
        records.stream().peek(e->{
            e.setProjectInfo(projectInfoMap.get(e.getProjectId()));
            e.setProjectTask(projectTaskMap.get(e.getTaskId()));
        }).collect(Collectors.toList());
        return R.ok(page);
    }


    @Log
    @ApiOperation(value = "工作台 获取我的日程", hidden = true)
    @PostMapping("/getSchedule")
    public R getSchedule() {
        return R.ok();
    }


    @Log
    @ApiOperation(value = "工作台 获取团队成员 ", hidden = true)
    @PostMapping("/getTeamMembers")
    public R getTeamMembers() {
        return R.ok();
    }


}
