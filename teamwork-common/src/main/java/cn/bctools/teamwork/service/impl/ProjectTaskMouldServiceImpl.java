package cn.bctools.teamwork.service.impl;

import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.utils.R;
import cn.bctools.teamwork.common.consts.CommonConst;
import cn.bctools.teamwork.common.consts.ProjectTaskConst;
import cn.bctools.teamwork.dto.req.ProjectTaskReq;
import cn.bctools.teamwork.entity.ProjectInfo;
import cn.bctools.teamwork.entity.ProjectTask;
import cn.bctools.teamwork.entity.ProjectTaskMember;
import cn.bctools.teamwork.entity.ProjectTaskMould;
import cn.bctools.teamwork.mapper.ProjectTaskMouldMapper;
import cn.bctools.teamwork.service.*;
import cn.bctools.teamwork.vo.ProjectTaskListVo;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Auto Generator
 */
@Service
public class ProjectTaskMouldServiceImpl extends ServiceImpl<ProjectTaskMouldMapper, ProjectTaskMould> implements ProjectTaskMouldService {
    @Autowired
    ProjectInfoService projectInfoService;

    @Autowired
    ProjectTaskService projectTaskService;

    @Autowired
    ProjectTaskMemberService projectTaskMemberService;
    @Autowired
    ProjectLogService projectLogService;

    @Override
    public List<ProjectTaskListVo> regroupDataTaskList(String id, Map<String, UserDto> userMap, List<ProjectTask> list, List<ProjectTaskMember> projectTaskMemberList) {
        //过滤任务数据
        List<ProjectTask> tasks = list.stream().filter(e -> e.getPid().equals(id)).collect(Collectors.toList());
        if (tasks.isEmpty()) {
            return new ArrayList<>();
        }
        List<ProjectTaskListVo> vos = tasks.parallelStream().map(e -> {
            //用户数据
            ProjectTaskMember taskMember = projectTaskMemberList.stream().filter(v -> e.getId().equals(v.getMemberId())).findFirst().orElseGet(ProjectTaskMember::new);
            UserDto userDto = userMap.get(taskMember.getMemberId());
            //获取子任务
            List<ProjectTaskListVo> listVos = this.regroupDataTaskList(e.getId(), userMap, list, projectTaskMemberList);
            return new ProjectTaskListVo()
                    .setThumbsUpCount(e.getThumbsUpCount())
                    .setTaskName(e.getName())
                    .setEndTime(e.getEndTime())
                    .setExecuteStatus(e.getExecuteStatus())
                    .setStartTime(e.getStartTime())
                    .setProjectTaskMouldId(e.getMouldId())
                    .setId(e.getId())
                    .setSort(e.getSort())
                    .setChildTask(listVos)
                    .setUserDto(userDto)
                    .setPriorityLevel(e.getPriorityLevel());
        }).collect(Collectors.toList());
        return vos;
    }

    @Override
    public R<ProjectTaskMould> saveTaskMould(ProjectTaskMould dto, UserDto user) {
        ProjectInfo project = projectInfoService.getNotDelById(dto.getProjectId());
        projectInfoService.verifyExist(project);
        //排序 原最小的排序-1
        this.sort(dto);
        dto.initSave(user);
        this.save(dto);
        return R.ok(dto);
    }

    @Override
    public List<ProjectTask> tasks(ProjectTaskReq req) {
        LambdaQueryWrapper<ProjectTask> wrapper = Wrappers.<ProjectTask>lambdaQuery();
        wrapper.eq(ProjectTask::getMouldId, req.getMouldId())
                .eq(BeanUtil.isNotEmpty(req.getFinishStatus()), ProjectTask::getFinishStatus, req.getFinishStatus())
                .like(StrUtil.isNotEmpty(req.getName()), ProjectTask::getName, req.getName())
                .eq(ProjectTask::getRecycleFlag, CommonConst.NO_DELETE_STATUS)
                .eq(ProjectTask::getPid, ProjectTaskConst.TOP_TASK_PID);
        if (ProjectTaskServiceImpl.getTreeWrapper(wrapper, req, projectTaskMemberService, projectLogService)) {
            return null;
        }

        return projectTaskService.list(wrapper);
    }

    /**
     * 查询最小的排序，存在则-1，不存在初始为0
     *
     * @param mould
     */
    public void sort(ProjectTaskMould mould) {
        ProjectTaskMould taskMould = this.lambdaQuery()
                .eq(ProjectTaskMould::getProjectId, mould.getProjectId())
                .orderByAsc(ProjectTaskMould::getSort)
                .last(" LIMIT 1")
                .one();
        if (BeanUtil.isEmpty(taskMould)) {
            mould.setSort(0);
        } else {
            mould.setSort(taskMould.getSort() - 1);
        }
    }
}
