package cn.bctools.teamwork.service.impl;

import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.R;
import cn.bctools.teamwork.common.consts.CommonConst;
import cn.bctools.teamwork.common.consts.ProjectTaskMouldConst;
import cn.bctools.teamwork.common.enums.FileRelationTypeEnum;
import cn.bctools.teamwork.common.enums.LogActionTypeEnum;
import cn.bctools.teamwork.common.enums.ProjectLogTypeEnum;
import cn.bctools.teamwork.common.enums.TaskLogTypeEnum;
import cn.bctools.teamwork.common.util.DateUtil;
import cn.bctools.teamwork.dto.ProjectLogAddBO;
import cn.bctools.teamwork.entity.*;
import cn.bctools.teamwork.mapper.ProjectInfoMapper;
import cn.bctools.teamwork.service.*;
import cn.bctools.teamwork.vo.BurnDownChartVO;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author Auto Generator
 */
@Service
public class ProjectInfoServiceImpl extends ServiceImpl<ProjectInfoMapper, ProjectInfo> implements ProjectInfoService {

    @Autowired
    ProjectTemplateTaskService projectTemplateTaskService;

    @Autowired
    ProjectMemberService projectMemberService;

    @Autowired
    ProjectTaskMouldService projectTaskMouldService;

    @Autowired
    ProjectLogService projectLogService;

    @Autowired
    ProjectCollectionService projectCollectionService;

    @Autowired
    ProjectFileInfoService projectFileInfoService;

    @Autowired
    ProjectTaskService projectTaskService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<ProjectInfo> saveProject(ProjectInfo dto, UserDto userDto) {
        dto.initSave(userDto);
        this.save(dto);
        String projectId = dto.getId();
        //1、模板 的任务列表增加到 项目的任务列表，若未选择模板，采用默认模板
        List<ProjectTemplateTask> tts = projectTemplateTaskService.list(Wrappers.lambdaQuery(ProjectTemplateTask.class)
                .eq(ProjectTemplateTask::getTemplateId, dto.getTemplateId()).orderByDesc(ProjectTemplateTask::getSort));
        if (CollUtil.isEmpty(tts)) {
            tts = ProjectTaskMouldConst.init();
        }
        AtomicInteger sort = new AtomicInteger(0);
        ProjectTaskMould pm;
        for (ProjectTemplateTask tt : tts) {
            pm = new ProjectTaskMould();
            pm.setProjectId(projectId)
                    .setName(tt.getName())
                    .setSort(sort.get());
            pm.initSave(userDto);
            projectTaskMouldService.save(pm);
            sort.set(sort.get() + 1);
        }


        //2、增加项目成员
        ProjectMember projectMember = new ProjectMember();
        projectMember.setProjectId(projectId)
                .setOwnerId(dto.getCreateById())
                .setJoinTime(LocalDateTime.now())
                .setMemberId(userDto.getId());
        projectMember.initSave(userDto);
        projectMemberService.save(projectMember);

        //保存项目创建日志
        ProjectLogAddBO createLog = new ProjectLogAddBO();
        createLog.setProjectLogTypeEnum(ProjectLogTypeEnum.CREATE)
                .setLogActionTypeEnum(LogActionTypeEnum.PROJECT)
                .setProjectId(projectId)
                .setContent(dto.getProjectName());
        projectLogService.runLog(createLog, userDto);
        //保存项目成员添加日志
        ProjectLogAddBO addMemberLog = new ProjectLogAddBO();
        addMemberLog.setProjectLogTypeEnum(ProjectLogTypeEnum.INVITE_MEMBER)
                .setLogActionTypeEnum(LogActionTypeEnum.PROJECT)
                .setProjectId(projectId)
                .setContent(dto.getProjectName())
                .setFormatContent(new String[]{userDto.getRealName()})
                .setToMemberId(userDto.getId());
        projectLogService.runLog(addMemberLog, userDto);
        return R.ok(dto);
    }

    @Override
    public ProjectInfo detail(ProjectInfo dto) {
        return this.getById(dto.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProjectInfo edit(ProjectInfo dto, UserDto userDto) {
        ProjectInfo projectInfo = getById(dto.getId());
        verifyExist(projectInfo);
        verifyDel(projectInfo);
        //清除项目周期时间
        if (BeanUtil.isEmpty(dto.getEndTime()) && BeanUtil.isNotEmpty(projectInfo.getEndTime())) {
            lambdaUpdate().eq(ProjectInfo::getId, dto.getId())
                    .set(ProjectInfo::getBeginTime, null)
                    .set(ProjectInfo::getEndTime, null).update();
        }
        dto.initUpdate(userDto);
        this.updateById(dto);
        //保存修改日志
        ProjectLogAddBO editLog = new ProjectLogAddBO();
        editLog.setProjectId(dto.getId())
                .setProjectLogTypeEnum(ProjectLogTypeEnum.EDIT)
                .setContent(dto.getProjectName())
                .setLogActionTypeEnum(LogActionTypeEnum.PROJECT);
        projectLogService.runLog(editLog, userDto);
        return dto;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean recycle(ProjectInfo dto, UserDto userDto) {
        ProjectInfo projectInfo = this.getById(dto.getId());
        //delFlag 是删除标记 加入回收站实则是逻辑删除
        ProjectInfo setDelFlag = new ProjectInfo()
                .setId(dto.getId())
                .setDelTime(LocalDateTime.now())
                .setRecycleFlag(1);
        setDelFlag.initUpdate(userDto);

        //保存项目进入回收站的日志
        ProjectLogAddBO recycleLog = new ProjectLogAddBO();
        recycleLog.setProjectId(dto.getId())
                .setProjectLogTypeEnum(ProjectLogTypeEnum.RECYCLE)
                .setLogActionTypeEnum(LogActionTypeEnum.PROJECT)
                .setContent(projectInfo.getProjectName());
        projectLogService.runLog(recycleLog, userDto);
        return this.updateById(setDelFlag);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean recovery(ProjectInfo dto, UserDto userDto) {
        ProjectInfo projectInfo = this.getById(dto.getId());
        // 是删除标记 加入回收站
        ProjectInfo recycle = new ProjectInfo()
                .setId(dto.getId())
                .setRecycleFlag(0);
        recycle.initUpdate(userDto);
        //保存项目恢复的日志
        ProjectLogAddBO recoveryLog = new ProjectLogAddBO();
        recoveryLog.setProjectId(dto.getId())
                .setProjectLogTypeEnum(ProjectLogTypeEnum.RECOVERY)
                .setLogActionTypeEnum(LogActionTypeEnum.PROJECT)
                .setContent(projectInfo.getProjectName());
        projectLogService.runLog(recoveryLog, userDto);
        return this.updateById(recycle);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean archive(ProjectInfo dto, UserDto userDto) {
        ProjectInfo projectInfo = this.getById(dto.getId());
        //archive 改变归档状态
        ProjectInfo archive = new ProjectInfo()
                .setId(dto.getId())
                .setArchiveTime(LocalDateTime.now())
                .setIsArchive(1);
        archive.initUpdate(userDto);
        //保存项目归档的日志
        ProjectLogAddBO archiveLog = new ProjectLogAddBO();
        archiveLog.setProjectId(dto.getId())
                .setProjectLogTypeEnum(ProjectLogTypeEnum.ARCHIVE)
                .setLogActionTypeEnum(LogActionTypeEnum.PROJECT)
                .setContent(projectInfo.getProjectName());
        projectLogService.runLog(archiveLog, userDto);
        return this.updateById(archive);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean recoveryArchive(ProjectInfo dto, UserDto userDto) {
        ProjectInfo projectInfo = this.getById(dto.getId());
        //archive 改变归档状态
        ProjectInfo archive = new ProjectInfo()
                .setId(dto.getId())
                .setArchiveTime(LocalDateTime.now())
                .setIsArchive(0);
        archive.initUpdate(userDto);
        //保存项目取消归档的日志
        ProjectLogAddBO recoveryArchiveLog = new ProjectLogAddBO();
        recoveryArchiveLog.setProjectId(dto.getId())
                .setProjectLogTypeEnum(ProjectLogTypeEnum.RECOVERY_ARCHIVE)
                .setLogActionTypeEnum(LogActionTypeEnum.PROJECT)
                .setContent(projectInfo.getProjectName());
        projectLogService.runLog(recoveryArchiveLog, userDto);
        return this.updateById(archive);
    }

    @Override
    public ProjectInfo getNotDelById(String projectId) {
        ProjectInfo projectInfo = getById(projectId);
        if (BeanUtil.isNotEmpty(projectInfo) && projectInfo.getRecycleFlag() == 0) {
            return projectInfo;
        }
        return null;
    }

    @Override
    public ProjectInfo verifyDel(ProjectInfo projectInfo) {
        if (projectInfo.getRecycleFlag() == 1) {
            throw new BusinessException("项目在回收站！");
        }
        return projectInfo;
    }

    @Override
    public ProjectInfo verifyExist(ProjectInfo projectInfo) {
        if (BeanUtil.isEmpty(projectInfo)) {
            throw new BusinessException("项目已失效！");
        }
        return projectInfo;
    }

    @Override
    public List<ProjectLog> listProjectLog(ProjectLog dto) {
        LambdaQueryWrapper<ProjectLog> wrapper = new LambdaQueryWrapper<ProjectLog>()
                .eq(ProjectLog::getProjectId, dto.getProjectId()).eq(ProjectLog::getIsComment, 0)
                .orderByDesc(ProjectLog::getCreateTime);
        if (dto.getIsSize()){
            wrapper.last("limit 20");
        }
        return projectLogService.list(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean uploadProjectFile(ProjectFileInfo fileInfo, ProjectInfo projectInfo, UserDto user) {
        fileInfo.initSave(user);
        projectFileInfoService.save(fileInfo);

        ProjectLogAddBO fileLog = new ProjectLogAddBO();
        fileLog.setProjectId(projectInfo.getId())
                .setProjectLogTypeEnum(ProjectLogTypeEnum.UPLOAD_FILE)
                .setLogActionTypeEnum(LogActionTypeEnum.PROJECT)
                .setContent(fileInfo.getOriginName());
        projectLogService.runLog(fileLog, user);
        return true;
    }

    @Override
    public BurnDownChartVO burnDownChart(String projectId) {
        BurnDownChartVO burn = new BurnDownChartVO();
        ProjectInfo projectInfo = getById(projectId);
        DateTimeFormatter ymd = DateTimeFormatter.ofPattern(DatePattern.NORM_DATE_PATTERN);
        String starStr;
        String endStr;
        //标志填充0
        boolean cycle = true;
        LocalDate now = LocalDate.now();
        //相差多少天
        int day = 0;
        //展示的开始时间过去了多少天
        int beforeDay = 0;
        LocalDateTime beginOrin = projectInfo.getBeginTime();
        LocalDateTime endOrin = projectInfo.getEndTime();
        if (BeanUtil.isEmpty(endOrin)) {
            //若没有周期时间 时间为现在时间往前10天 条数全部设置为0
            LocalDate end = now.plusDays(-1);
            LocalDate star = now.plusDays(-10);
            endStr = end.format(ymd);
            starStr = star.format(ymd);
            cycle = false;
        } else {
            LocalDate begin = beginOrin.toLocalDate();
            LocalDate end = endOrin.toLocalDate();
            burn.setEndTime(end.format(ymd));
            burn.setStartTime(begin.format(ymd));
            long endDay = end.toEpochDay();
            long beginDay = begin.toEpochDay();
            long c = endDay - beginDay;
            day = (int) c;
            long ten = 9;
            if (end.isEqual(begin)) {
                cycle = false;
            } else if (end.isAfter(now) || end.isEqual(now)) {
                end = now.plusDays(-1);
            } else if (end.isBefore(now) && c > ten) {
                begin = end.plusDays(-9);
            }
            endStr = end.format(ymd);
            starStr = begin.format(ymd);
            //若开始时间变化 计算与原开始时间相差天数
            long l = begin.toEpochDay() - beginDay;
            beforeDay = (int) l;
        }
        burn.setViewStartTime(starStr);
        burn.setViewEndTime(endStr);
        List<String> dateStr = DateUtil.getDateStr(starStr, endStr);
        //查询该项目下所有的项目任务
        List<ProjectTask> tasks = projectTaskService.lambdaQuery()
                .eq(ProjectTask::getRecycleFlag, CommonConst.NO_DELETE_STATUS)
                .eq(ProjectTask::getProjectId, projectId).list();


        return fillBurnDownChart(burn, dateStr, cycle, tasks, day, beforeDay);
    }

    @Override
    public void updateSchedule(String projectId) {
        if (StrUtil.isEmpty(projectId)) {
            return;
        }
        ProjectInfo projectInfo = this.getById(projectId);
        if (projectInfo.getAutoUpdateSchedule() == 0) {
            return;
        }
        updateScheduleById(projectId);
    }

    /**
     * 根据项目id查询项目下的任务计算完成情况,改变项目进度
     *
     * @param projectId
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateScheduleById(String projectId) {
        ProjectInfo projectInfo = new ProjectInfo().setId(projectId);
        List<ProjectTask> projectTasks = projectTaskService.lambdaQuery().eq(ProjectTask::getProjectId, projectId).list();
        Double sch = 0.0;
        if (CollUtil.isNotEmpty(projectTasks)) {
            double total = projectTasks.size();
            double done = projectTasks.parallelStream().filter(x -> x.getFinishStatus() == 1).count();
            sch = done / total;
        }
        projectInfo.setProjectSchedule(sch);
        updateById(projectInfo);

    }

    /**
     * 填充任务燃尽图的预测和实际 的显示日期和条数
     *
     * @param vo        需要填充的实体类
     * @param dateStr   燃尽图展示时间
     * @param cycle     项目是否设置了周期或者周期为同一天
     * @param tasks     项目任务
     * @param day       相差多少天
     * @param beforeDay 已经过去多少天
     * @return 填充后的
     */
    public BurnDownChartVO fillBurnDownChart(BurnDownChartVO vo, List<String> dateStr,
                                             boolean cycle,
                                             List<ProjectTask> tasks,
                                             int day,
                                             int beforeDay) {
        DateTimeFormatter ymd = DateTimeFormatter.ofPattern(DatePattern.NORM_DATE_PATTERN);
        BurnDownChartVO.ForecastBurnDownChartVO forecast;
        BurnDownChartVO.RealBurnDownChartVO real;
        List<BurnDownChartVO.ForecastBurnDownChartVO> forecastList = new ArrayList<>();
        List<BurnDownChartVO.RealBurnDownChartVO> realList = new ArrayList<>();
        boolean empty = CollUtil.isEmpty(tasks);
        int size = !empty ? tasks.size() : 0;
        double step = (double) size / (double) day;
        Map<String, List<ProjectLog>> map = new HashMap<>(size);
        //已完成的任务日志
        List<ProjectLog> doneLogs = new ArrayList<>();
        if (!empty) {
            //查询任务完成的日志
            List<String> doneIds = tasks.stream().filter(x -> x.getFinishStatus() == 1).map(ProjectTask::getId).collect(Collectors.toList());
            doneLogs = projectLogService.lambdaQuery().in(ProjectLog::getTaskId, doneIds)
                    .eq(ProjectLog::getActionType, LogActionTypeEnum.TASK.getCode())
                    .eq(ProjectLog::getType, TaskLogTypeEnum.DONE).list();
            for (ProjectLog doneLog : doneLogs) {
                LocalDateTime createTime = doneLog.getCreateTime();
                LocalDate cDate = createTime.toLocalDate();
                String cDateStr = cDate.format(ymd);
                doneLog.setCreateDateStr(cDateStr);
            }
            //任务完成日志按照时间分类
            map = doneLogs.stream().collect(Collectors.groupingBy(ProjectLog::getCreateDateStr));
        }
        String firstDate = dateStr.get(0);
        //在日期之前完成的
        List<String> alDoneIds = doneLogs.stream().filter(x -> {
            LocalDateTime createTime = x.getCreateTime();
            LocalDate cDate = createTime.toLocalDate();
            LocalDate parse = LocalDate.parse(firstDate, ymd);
            return cDate.isBefore(parse);
        }).map(ProjectLog::getTaskId).distinct().collect(Collectors.toList());
        //标记已完成
        int already = 0;
        if (CollUtil.isNotEmpty(alDoneIds)) {
            already = alDoneIds.size();
        }

        for (String date : dateStr) {
            forecast = new BurnDownChartVO.ForecastBurnDownChartVO();
            real = new BurnDownChartVO.RealBurnDownChartVO();
            forecast.setDate(date);
            real.setDate(date);
            if (!cycle || empty) {
                forecast.setCount(0.0);
                real.setCount(0);
            } else {
                double count = step * beforeDay;
                List<ProjectLog> doneList = map.get(date);
                if (CollUtil.isNotEmpty(doneList)) {
                    long doneSize = doneList.stream().map(ProjectLog::getTaskId).distinct().count();
                    already += (int) doneSize;
                }
                real.setCount(size - already);
                forecast.setCount(count);
            }
            forecastList.add(forecast);
            realList.add(real);
            beforeDay++;
        }
        vo.setReal(realList);
        vo.setForecast(forecastList);
        return vo;
    }


}
