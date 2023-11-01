package cn.bctools.teamwork.service.impl;

import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.teamwork.common.consts.CommonConst;
import cn.bctools.teamwork.common.consts.ProjectTaskConst;
import cn.bctools.teamwork.common.enums.CollectionTypeEnum;
import cn.bctools.teamwork.common.enums.LogActionTypeEnum;
import cn.bctools.teamwork.common.enums.TaskLogTypeEnum;
import cn.bctools.teamwork.common.util.DateUtil;
import cn.bctools.teamwork.dto.*;
import cn.bctools.teamwork.dto.req.ProjectTaskReq;
import cn.bctools.teamwork.dto.req.TaskDateCountReq;
import cn.bctools.teamwork.entity.*;
import cn.bctools.teamwork.mapper.ProjectTaskMapper;
import cn.bctools.teamwork.service.*;
import cn.bctools.teamwork.vo.ProjectTaskCountForDateVO;
import cn.bctools.teamwork.vo.ProjectTaskDetailVO;
import cn.bctools.teamwork.vo.ProjectTaskTreeMouldVO;
import cn.bctools.teamwork.vo.ProjectTaskTreeVO;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author Auto Generator
 */
@Service
public class ProjectTaskServiceImpl extends ServiceImpl<ProjectTaskMapper, ProjectTask> implements ProjectTaskService {
    private final static String PID = "-1";
    private final static String TIME_TYPE_START = "start";
    private final static String TIME_TYPE_END = "end";
    @Autowired
    ProjectLogService projectLogService;
    @Autowired
    ProjectInfoService projectInfoService;
    @Autowired
    ProjectMemberService projectMemberService;
    @Autowired
    ProjectTaskMouldService projectTaskMouldService;
    @Autowired
    ProjectTaskMemberService projectTaskMemberService;
    @Autowired
    ProjectTaskToTagService projectTaskToTagService;
    @Autowired
    ProjectTaskWorkTimeRecordService projectTaskWorkTimeRecordService;
    @Autowired
    ProjectTaskTagService projectTaskTagService;
    @Autowired
    ProjectFileInfoService fileInfoService;
    @Autowired
    ProjectCollectionService projectCollectionService;
    @Autowired
    ProjectTaskLikedService projectTaskLikedService;
    @Autowired
    ProjectVersionService projectVersionService;
    @Autowired
    ProjectTaskWorkflowService projectTaskWorkflowService;
    @Autowired
    ProjectFileInfoService projectFileInfoService;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProjectTask saveTask(ProjectTaskAddDTO dto, UserDto user, boolean pLog) {
        UserDto userDto = dto.getExecuteMember();
        ProjectTask projectTask = new ProjectTask();
        BeanUtil.copyProperties(dto, projectTask);
        //初始化用户基本信息
        projectTask.initSave(user);
        //查询 是否开启新任务默认开启隐私模式 1-是 0-否
        Integer openTaskPrivate = projectInfoService.getById(dto.getProjectId()).getOpenTaskPrivate();
        projectTask.setIsPrivate(openTaskPrivate);
        this.save(projectTask);
        //添加任务的成员
        TaskMemberAddDTO create = new TaskMemberAddDTO()
                .setTaskId(projectTask.getId())
                .setMemberId(user.getId())
                .setIsExecutor(BigDecimal.ROUND_DOWN)
                .setIsOwner(BigDecimal.ROUND_DOWN);
        boolean isEquals = ObjectUtil.isNotEmpty(userDto) && !userDto.getId().equals(user.getId());
        if (isEquals) {
            create.setIsExecutor(0);
            TaskMemberAddDTO execute = new TaskMemberAddDTO() {{
                setTaskId(projectTask.getId());
                setMemberId(userDto.getId());
                setIsExecutor(1);
                setIsOwner(0);
            }};
            projectTaskMemberService.inviteMember(execute, user, false);
        }
        projectTaskMemberService.inviteMember(create, user, false);
        if (ObjectUtil.isNotEmpty(userDto)) {
            //创建的日志
            ProjectLog log1 = new ProjectLog() {{
                setRemark(TaskLogTypeEnum.CREATE.getRemark());
                setContent(projectTask.getName());
                setProjectId(projectTask.getProjectId());
                setTaskId(projectTask.getId());
                setType(TaskLogTypeEnum.CREATE.getCode());
                setIsComment(0);
                setMemberId(user.getId());
                setActionType(LogActionTypeEnum.TASK.getCode());
                setToMemberId(user.getId());
            }};
            this.assignOrClaim(projectTask, user, userDto);
            log1.initSave(user);
            projectLogService.runLog(log1);
            //若有父任务id，则是子任务 需要添加父任务的日志
            if (!PID.equals(dto.getPid()) && pLog) {
                this.saveParentLog(projectTask, user, dto);
            }
        }

        //项目是否有流转规则并匹配操作,创建
        projectTaskWorkflowService.queryRuleAndOperation(projectTask, null, user, 0);
        projectInfoService.updateSchedule(projectTask.getProjectId());
        return projectTask;
    }

    public void verifyTaskExistAndThrows(ProjectTask task) {
        if (BeanUtil.isEmpty(task)) {
            throw new BusinessException("该任务已失效！");
        }
    }

    public void verifyTaskDelAndThrows(ProjectTask task) {
        if (CommonConst.DELETE_STATUS.equals(task.getRecycleFlag())) {
            throw new BusinessException("该任务在回收站中无法编辑！");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProjectTask editTask(ProjectTaskEditDTO dto, UserDto user) {
        ProjectTask task = this.getById(dto.getId());
        boolean editMark = false;
        ProjectLog taskLog = new ProjectLog() {{
            setProjectId(task.getProjectId());
            setTaskId(task.getId());
            setIsComment(0);
            setMemberId(user.getId());
            setActionType(LogActionTypeEnum.TASK.getCode());
            setToMemberId(user.getId());
        }};
        LambdaUpdateChainWrapper<ProjectTask> update = lambdaUpdate().eq(ProjectTask::getId, dto.getId());
        if (StrUtil.isNotEmpty(dto.getName())) {
            update.set(ProjectTask::getName, dto.getName());
            taskLog.setRemark(TaskLogTypeEnum.NAME.getRemark())
                    .setType(TaskLogTypeEnum.NAME.getCode())
                    .setContent(dto.getName());
            editMark = true;
        }
        if (StrUtil.isEmpty(dto.getRemark()) && StrUtil.isNotEmpty(task.getRemark())) {
            update.set(ProjectTask::getRemark, null);
            taskLog.setRemark(TaskLogTypeEnum.CLEAR_CONTENT.getRemark())
                    .setType(TaskLogTypeEnum.CLEAR_CONTENT.getCode());
            editMark = true;
        }
        if (StrUtil.isNotEmpty(dto.getRemark())) {
            update.set(ProjectTask::getRemark, dto.getRemark());
            taskLog.setRemark(TaskLogTypeEnum.CONTENT.getRemark())
                    .setType(TaskLogTypeEnum.CONTENT.getCode())
                    .setContent(dto.getRemark());
            editMark = true;
        }
        //完成调用完成接口，暂不使用
        if (ObjectUtil.isNotEmpty(dto.getFinishStatus())) {
            TaskLogTypeEnum taskLogTypeEnum = dto.getFinishStatus() == BigDecimal.ROUND_UP ? TaskLogTypeEnum.REDO : TaskLogTypeEnum.DONE;
            update.set(ProjectTask::getFinishStatus, dto.getFinishStatus());
            taskLog.setRemark(taskLogTypeEnum.getRemark())
                    .setType(taskLogTypeEnum.getCode())
                    .setContent(dto.getRemark());
            editMark = true;
        }
        if (StrUtil.isNotEmpty(dto.getPriorityLevel())) {
            update.set(ProjectTask::getPriorityLevel, dto.getPriorityLevel());
            String pri = ProjectTaskConst.transPriNumToStr(dto.getPriorityLevel());
            taskLog.setRemark(String.format(TaskLogTypeEnum.PRI.getRemark(), pri))
                    .setType(TaskLogTypeEnum.PRI.getCode());
            editMark = true;
        }

        if (StrUtil.isNotEmpty(dto.getExecuteStatus())) {
            update.set(ProjectTask::getExecuteStatus, dto.getExecuteStatus());
            String executeNumToStr = ProjectTaskConst.transExecuteNumToStr(dto.getExecuteStatus());
            taskLog.setRemark(String.format(TaskLogTypeEnum.STATUS.getRemark(), executeNumToStr))
                    .setType(TaskLogTypeEnum.STATUS.getCode());
            editMark = true;
        }
        if (BeanUtil.isNotEmpty(dto.getInitWorkingHours())) {
            update.set(ProjectTask::getInitWorkingHours, dto.getInitWorkingHours());
            taskLog.setRemark(String.format(TaskLogTypeEnum.SET_WORK_TIME.getRemark(), dto.getInitWorkingHours()))
                    .setType(TaskLogTypeEnum.SET_WORK_TIME.getCode());
            editMark = true;
        }
        LocalDateTime taskEndTime = task.getEndTime();
        LocalDateTime dtoStartTime = dto.getStartTime();
        LocalDateTime taskStartTime = task.getStartTime();
        LocalDateTime dtoEndTime = dto.getEndTime();
        //开始时间和结束时间更新
        if (BeanUtil.isNotEmpty(dtoStartTime)) {
            //判断是否比结束时间小
            if (BeanUtil.isNotEmpty(taskEndTime)) {
                if (taskEndTime.isBefore(dtoStartTime) || taskEndTime.isEqual(dtoStartTime)) {
                    throw new BusinessException("截止时间应比开始时间大！");
                }
            }
            update.set(ProjectTask::getStartTime, dtoStartTime);
            taskLog.setRemark(String.format(TaskLogTypeEnum.SET_BEGIN_TIME.getRemark(), CommonConst.dateTimeToStr(dtoStartTime, CommonConst.DATE_TIME_MINUTE)))
                    .setType(TaskLogTypeEnum.SET_BEGIN_TIME.getCode());
            editMark = true;
        }
        if (BeanUtil.isNotEmpty(dtoEndTime)) {
            //判断是否比结束时间小
            if (BeanUtil.isNotEmpty(taskStartTime)) {
                if (dtoEndTime.isBefore(taskStartTime) || dtoEndTime.isEqual(taskStartTime)) {
                    throw new BusinessException("截止时间应比开始时间大！");
                }
            }
            update.set(ProjectTask::getEndTime, dtoEndTime);
            taskLog.setRemark(String.format(TaskLogTypeEnum.SET_END_TIME.getRemark(), CommonConst.dateTimeToStr(dtoEndTime, CommonConst.DATE_TIME_MINUTE)))
                    .setType(TaskLogTypeEnum.SET_END_TIME.getCode());
            editMark = true;
        }
        //开始时间和结束时间清除 放在最后的顺序才能判断 editMark 为true则进入了更新,则不用进入清除，
        //这里只对原来有，现在无的数据进行清除，但是存在开始时间和结束时间原来都有的情况，加一个参数 editTimeType 进行区分
        String timeType = dto.getEditTimeType();
        if (!editMark && StrUtil.isEmpty(timeType)) {
            boolean editStart = TIME_TYPE_START.equals(timeType);
            if (BeanUtil.isEmpty(dtoStartTime) && BeanUtil.isNotEmpty(taskStartTime) && editStart) {
                update.set(ProjectTask::getStartTime, null);
                taskLog.setRemark(TaskLogTypeEnum.CLEAR_BEGIN_TIME.getRemark())
                        .setType(TaskLogTypeEnum.CLEAR_BEGIN_TIME.getCode());
                editMark = true;
            }
            boolean editEnd = TIME_TYPE_END.equals(timeType);
            if (BeanUtil.isEmpty(dtoEndTime) && BeanUtil.isNotEmpty(taskEndTime) && editEnd) {
                update.set(ProjectTask::getEndTime, null);
                taskLog.setRemark(TaskLogTypeEnum.CLEAR_END_TIME.getRemark())
                        .setType(TaskLogTypeEnum.CLEAR_END_TIME.getCode());
                editMark = true;
            }
        }
        if (editMark) {
            update.set(ProjectTask::getUpdateBy, StrUtil.isEmpty(user.getRealName()) ? user.getAccountName() : user.getRealName());
            update.update();
            projectLogService.runLog(taskLog);
        }
        return task;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProjectTaskToTag setTag(ProjectTaskToTag dto, UserDto user) {
        //查询数据库是否已经存在标签，存在则是删除，不存在则添加
        ProjectTaskToTag toTag = projectTaskToTagService.lambdaQuery().eq(ProjectTaskToTag::getTagId, dto.getTagId())
                .eq(ProjectTaskToTag::getTaskId, dto.getTaskId()).one();
        if (BeanUtil.isEmpty(toTag)) {
            dto.initSave(user);
            projectTaskToTagService.save(dto);
        } else {
            projectTaskToTagService.removeById(toTag);
        }
        return dto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProjectTaskWorkTimeRecord saveTaskWorkTime(ProjectTaskWorkTimeRecord dto, UserDto user) {
        dto.initSave(user);
        projectTaskWorkTimeRecordService.save(dto);
        return dto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProjectTaskWorkTimeRecord editTaskWorkTime(ProjectTaskWorkTimeRecord dto, UserDto user) {
        dto.initUpdate(user);
        projectTaskWorkTimeRecordService.updateById(dto);
        return dto;
    }

    @Override
    public ProjectTaskDetailVO detail(String taskId, UserDto user) {
        ProjectTaskDetailVO ptdVo = new ProjectTaskDetailVO() {{
            setLikeMine(1);
            setCollectMine(1);
        }};
        ProjectTask task = this.getById(taskId);
        ProjectTaskMould mould = projectTaskMouldService.getById(task.getMouldId());
        ProjectInfo projectInfo = projectInfoService.getById(task.getProjectId());
        BeanUtil.copyProperties(task, ptdVo);
        //子任务
        ptdVo.setProjectInfo(projectInfo);
        ptdVo.setProjectTaskMould(mould);
        ptdVo.setTaskCount("0/0");
        ptdVo.setTasks(new ArrayList<>());
        ptdVo.setTags(new ArrayList<>());
        String taskPid = task.getPid();
        if (StrUtil.isNotEmpty(taskPid) && !ProjectTaskConst.TOP_TASK_PID.equals(taskPid)) {
            ptdVo.setPrentTask(getById(taskPid));
        }
        //子任务
        List<ProjectTask> child = this.listChildTask(taskId);
        if (CollUtil.isNotEmpty(child)) {
            ptdVo.setTasks(child);
            AtomicInteger doCount = new AtomicInteger(0);
            child.forEach(e -> {
                if (e.getFinishStatus() == 1) {
                    doCount.set(doCount.get() + 1);
                }
            });
            ptdVo.setTaskCount(doCount.get() + "/" + child.size());
        }
        //标签
        List<ProjectTaskToTag> list = projectTaskToTagService.lambdaQuery()
                .eq(ProjectTaskToTag::getTaskId, task.getId()).list();
        if (CollUtil.isNotEmpty(list)) {
            List<String> tagIds = list.stream().map(ProjectTaskToTag::getTagId).collect(Collectors.toList());
            List<ProjectTaskTag> taskTags = projectTaskTagService.lambdaQuery().in(ProjectTaskTag::getId, tagIds).list();
            ptdVo.setTags(taskTags);
        }
        //收藏
        ProjectCollection collection = projectCollectionService.lambdaQuery().eq(ProjectCollection::getObjectId, taskId)
                .eq(ProjectCollection::getMemberId, user.getId())
                .eq(ProjectCollection::getType, CollectionTypeEnum.TASK.getCode()).one();
        if (BeanUtil.isEmpty(collection)) {
            ptdVo.setCollectMine(0);
        }
        //点赞
        ProjectTaskLiked taskLiked = projectTaskLikedService.lambdaQuery().eq(ProjectTaskLiked::getTaskId, taskId)
                .eq(ProjectTaskLiked::getMemberId, user.getId()).one();
        if (BeanUtil.isEmpty(taskLiked)) {
            ptdVo.setLikeMine(0);
        }
        //工时
        List<ProjectTaskWorkTimeRecord> records = projectTaskWorkTimeRecordService.list(new LambdaQueryWrapper<ProjectTaskWorkTimeRecord>().eq(ProjectTaskWorkTimeRecord::getTaskId, taskId));
        ptdVo.setProjectTaskWorkTimeRecords(records);
        return ptdVo;
    }

    @Override
    public List<ProjectTask> listChildTask(String taskPId) {
        return this.lambdaQuery().eq(ProjectTask::getPid, taskPId)
                .eq(ProjectTask::getRecycleFlag, CommonConst.NO_DELETE_STATUS).list();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean setPrivate(ProjectTask dto, UserDto user) {
        ProjectTask task = this.getById(dto.getId());
        LambdaUpdateChainWrapper<ProjectTask> update = lambdaUpdate().eq(ProjectTask::getId, dto.getId());
        if (task.getIsPrivate() == 0) {
            update.set(ProjectTask::getIsPrivate, 1);
        } else {
            update.set(ProjectTask::getIsPrivate, 0);
        }
        return update.update();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean assignExecutor(ProjectTaskAssignExecutorDTO dto, UserDto user) {
        //查出任务信息
        String taskId = dto.getTaskId();
        ProjectTask task = this.getById(taskId);
        if (BeanUtil.isEmpty(task)) {
            throw new BusinessException("请选择任务！");
        }
        verifyTaskDelAndThrows(task);
        ProjectLog log = new ProjectLog() {{
            setProjectId(task.getProjectId());
            setIsComment(0);
            setMemberId(user.getId());
            setActionType(LogActionTypeEnum.TASK.getCode());
            setTaskId(task.getId());
            setType(TaskLogTypeEnum.REMOVE_EXECUTOR.getCode());
            setRemark(TaskLogTypeEnum.REMOVE_EXECUTOR.getRemark());
        }};
        String executorId = dto.getExecutorId();
        if (StrUtil.isNotEmpty(executorId)) {
            Boolean exists = projectTaskMemberService.verifyExists(executorId, taskId);
            //不存在则添加任务参与者
            if (!exists) {
                TaskMemberAddDTO taskMemberAddDTO = new TaskMemberAddDTO();
                taskMemberAddDTO.setTaskId(taskId).setMemberId(executorId).setIsOwner(0).setIsExecutor(1);
                projectTaskMemberService.inviteMember(taskMemberAddDTO, user, false);
            }
            log.setToMemberId(executorId)
                    .setType(TaskLogTypeEnum.ASSIGN.getCode())
                    .setRemark(String.format(TaskLogTypeEnum.ASSIGN.getRemark(), dto.getExecutorInfo().getRealName()));
            //是否是创建者
            if (executorId.equals(task.getCreateById())) {
                log.setType(TaskLogTypeEnum.CLAIM.getCode())
                        .setRemark(TaskLogTypeEnum.CLAIM.getRemark());
            }
        }

        boolean b = lambdaUpdate().eq(ProjectTask::getId, taskId)
                .set(ProjectTask::getExecuteMemberId, executorId)
                .set(ProjectTask::getUpdateTime, LocalDateTime.now())
                .set(ProjectTask::getUpdateBy, StrUtil.isEmpty(user.getRealName()) ? user.getAccountName() : user.getRealName()).update();
        //项目是否有流转规则并匹配操作,设置执行人
        projectTaskWorkflowService.queryRuleAndOperation(task, executorId, user, 3);
        projectLogService.runLog(log);
        return b;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean taskDone(ProjectTaskDoneDTO dto, UserDto user) {
        ProjectTask task = this.getById(dto.getTaskId());
        Integer action = 0;
        Integer finishStatus = dto.getFinishStatus();
        if (BeanUtil.isEmpty(task)) {
            throw new BusinessException("该任务已失效！");
        }
        verifyTaskDelAndThrows(task);
        //查看是否有父id,父任务已完成，子任务无法编辑
        if (!ProjectTaskConst.TOP_TASK_PID.equals(task.getPid())) {
            ProjectTask pTask = this.getById(task.getPid());
            if (pTask.getFinishStatus() == 1) {
                throw new BusinessException("父任务已完成,无法重做子任务！");
            }
            //增加父任务的动态
            ProjectLog pTaskLog = new ProjectLog() {{
                setProjectId(pTask.getProjectId());
                setIsComment(0);
                setMemberId(user.getId());
                setActionType(LogActionTypeEnum.TASK.getCode());
                setTaskId(pTask.getId());
                setToMemberId(user.getId());
            }};
            if (finishStatus == 1) {
                pTaskLog.setType(TaskLogTypeEnum.DONE_CHILD.getCode())
                        .setRemark(String.format(TaskLogTypeEnum.DONE_CHILD.getRemark(), task.getName()));

            } else {
                pTaskLog.setType(TaskLogTypeEnum.REDO_CHILD.getCode())
                        .setRemark(String.format(TaskLogTypeEnum.REDO_CHILD.getRemark(), task.getName()));
            }
            pTaskLog.initSave(user);
            projectLogService.runLog(pTaskLog);
        }
        ProjectLog taskLog = new ProjectLog() {{
            setProjectId(task.getProjectId());
            setIsComment(0);
            setMemberId(user.getId());
            setActionType(LogActionTypeEnum.TASK.getCode());
            setTaskId(task.getId());
            setToMemberId(user.getId());
        }};
        String versionId = task.getVersionId();
        if (finishStatus == 1) {
            taskLog.setType(TaskLogTypeEnum.DONE.getCode())
                    .setRemark(TaskLogTypeEnum.DONE.getRemark());
            //被完成的规则
            action = 1;
        } else {
            taskLog.setType(TaskLogTypeEnum.REDO.getCode())
                    .setRemark(TaskLogTypeEnum.REDO.getRemark());
            //被重做的规则
            action = 2;

        }
        boolean update = lambdaUpdate().eq(ProjectTask::getId, dto.getTaskId())
                .set(ProjectTask::getFinishStatus, finishStatus)
                .set(ProjectTask::getUpdateBy, StrUtil.isEmpty(user.getRealName()) ? user.getAccountName() : user.getRealName())
                .set(ProjectTask::getUpdateTime, LocalDateTime.now())
                .update();
        taskLog.initSave(user);
        projectLogService.runLog(taskLog);

        projectTaskWorkflowService.queryRuleAndOperation(task, null, user, action);
        if (StrUtil.isNotEmpty(versionId)) {
            projectVersionService.updateSchedule(versionId);
        }
        projectInfoService.updateSchedule(task.getProjectId());
        return update;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProjectFileInfo taskRelationFile(ProjectFileInfo fileInfo, ProjectTask task, UserDto user) {
        fileInfo.initSave(user);
        fileInfoService.save(fileInfo);
//        ProjectFileRelation relation = new ProjectFileRelation();
//        relation.setFileType(fileInfo.getFileType())
//                .setFileId(fileInfo.getId())
//                .setLinkId(task.getId())
//                .setLinkType(FileRelationTypeEnum.TASK_TYPE.getCode());
//        relation.initSave(user);
//        boolean save = projectFileRelationService.save(relation);
        ProjectLog fileLog = new ProjectLog() {{
            setProjectId(task.getProjectId());
            setIsComment(0);
            setMemberId(user.getId());
            setActionType(LogActionTypeEnum.TASK.getCode());
            setTaskId(task.getId());
            setType(TaskLogTypeEnum.LINK_FILE.getCode());
            setRemark(TaskLogTypeEnum.LINK_FILE.getRemark());
        }};
        fileLog.initSave(user);
        projectLogService.runLog(fileLog);
        return fileInfo;
    }

    @Override
    public List<ProjectFileInfo> listTaskFile(String taskId) {
//        List<ProjectFileRelation> relations = projectFileRelationService.lambdaQuery()
//                .eq(ProjectFileRelation::getLinkId, taskId)
//                .eq(ProjectFileRelation::getLinkType, FileRelationTypeEnum.TASK_TYPE).list();
//
//        if (CollUtil.isNotEmpty(relations)) {
//            List<String> fileIds = relations.stream().map(ProjectFileRelation::getFileId).collect(Collectors.toList());
//            List<ProjectFileInfo> fileInfoList = fileInfoService.lambdaQuery().in(ProjectFileInfo::getId, fileIds).list();
//            relations.forEach(x -> {
//                for (ProjectFileInfo fileInfo : fileInfoList) {
//                    if (fileInfo.getId().equals(x.getFileId()) && fileInfo.getDelFlag() == 0) {
//                        infoList.add(fileInfo);
//                        break;
//                    }
//                }
//            });
//        }
        List<ProjectFileInfo> list = projectFileInfoService.lambdaQuery().eq(ProjectFileInfo::getTaskId, taskId).eq(ProjectFileInfo::getRecycleFlag, BigDecimal.ROUND_UP).list();
        if (CollUtil.isEmpty(list)) {
            return new ArrayList<>();
        }
        return list;
    }

    @Override
    public Page<ProjectLog> listProjectLog(Page<ProjectLog> page, ProjectLog dto) {
        LambdaQueryWrapper<ProjectLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProjectLog::getTaskId, dto.getTaskId())
                .eq(ProjectLog::getActionType, LogActionTypeEnum.TASK.getCode())
                .eq(BeanUtil.isNotEmpty(dto.getIsComment()), ProjectLog::getIsComment, dto.getIsComment());
        return projectLogService.page(page, wrapper);
    }

    @Override
    public ProjectTaskCommentDTO taskComment(ProjectTaskCommentDTO dto, UserDto user) {
        ProjectTask task = this.getById(dto.getTaskId());
        if (BeanUtil.isEmpty(task)) {
            throw new BusinessException("任务已失效！");
        }
        ProjectLog clog = new ProjectLog() {{
            setProjectId(task.getProjectId());
            setIsComment(1);
            setMemberId(user.getId());
            setActionType(LogActionTypeEnum.TASK.getCode());
            setTaskId(task.getId());
            setType(TaskLogTypeEnum.COMMENT.getCode());
            setRemark(dto.getComment());
        }};
        clog.initSave(user);
        projectLogService.runLog(clog);
        return dto;
    }

    @Override
    public Boolean taskCollect(TaskCollectDTO dto, UserDto user) {
        ProjectTask task = this.getById(dto.getTaskId());
        verifyTaskExistAndThrows(task);
        verifyTaskDelAndThrows(task);
        Integer coll = task.getCollectionQuantity();
        ProjectCollection one = projectCollectionService.lambdaQuery().eq(ProjectCollection::getObjectId, task.getId())
                .eq(ProjectCollection::getMemberId, user.getId()).one();
        if (1 == dto.getCollect()) {
            coll = coll + 1;
            if (BeanUtil.isNotEmpty(one)) {
                throw new BusinessException("已经收藏该任务了！");
            }
            ProjectCollection collection = new ProjectCollection() {{
                setMemberId(user.getId());
                setObjectId(task.getId());
                setType(CollectionTypeEnum.TASK.getCode());
            }};
            collection.initSave(user);
            projectCollectionService.save(collection);

        } else {
            coll = coll - 1;
            if (BeanUtil.isEmpty(one)) {
                throw new BusinessException("未收藏该任务！");
            }
            projectCollectionService.removeById(one.getId());

        }
        boolean update = this.lambdaUpdate().eq(ProjectTask::getId, task.getId())
                .set(ProjectTask::getCollectionQuantity, coll)
                .set(ProjectTask::getUpdateBy, StrUtil.isEmpty(user.getRealName()) ? user.getAccountName() : user.getRealName()).update();
        return update;

    }

    @Override
    public Boolean taskLike(TaskLikeDTO dto, UserDto user) {
        ProjectTask task = this.getById(dto.getTaskId());
        verifyTaskExistAndThrows(task);
        verifyTaskDelAndThrows(task);
        Integer thumbs = task.getThumbsUpCount();
        ProjectTaskLiked one = projectTaskLikedService.lambdaQuery().eq(ProjectTaskLiked::getTaskId, task.getId())
                .eq(ProjectTaskLiked::getMemberId, user.getId()).one();
        if (1 == dto.getLike()) {
            thumbs = thumbs + 1;
            if (BeanUtil.isNotEmpty(one)) {
                throw new BusinessException("已经点赞，不能再点了哦！");
            }
            ProjectTaskLiked liked = new ProjectTaskLiked() {{
                setMemberId(user.getId());
                setTaskId(task.getId());
            }};
            liked.initSave(user);
            projectTaskLikedService.save(liked);
        } else {
            thumbs = thumbs - 1;
            if (BeanUtil.isEmpty(one)) {
                throw new BusinessException("您没有点赞！");
            }
            projectTaskLikedService.removeById(one.getId());
        }
        boolean update = this.lambdaUpdate().eq(ProjectTask::getId, task.getId())
                .set(ProjectTask::getThumbsUpCount, thumbs)
                .set(ProjectTask::getUpdateBy, StrUtil.isEmpty(user.getRealName()) ? user.getAccountName() : user.getRealName()).update();
        return update;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean taskRecycle(String taskId, UserDto user) {
        ProjectTask task = this.getById(taskId);
        boolean update = this.lambdaUpdate().eq(ProjectTask::getId, taskId)
                .set(ProjectTask::getRecycleFlag, CommonConst.DELETE_STATUS)
                .set(ProjectTask::getDelTime, LocalDateTime.now())
                .set(ProjectTask::getUpdateBy, StrUtil.isEmpty(user.getRealName()) ? user.getAccountName() : user.getRealName()).update();
        ProjectLog rLog = new ProjectLog() {{
            setProjectId(task.getProjectId());
            setIsComment(0);
            setMemberId(user.getId());
            setActionType(LogActionTypeEnum.TASK.getCode());
            setTaskId(taskId);
            setType(TaskLogTypeEnum.RECYCLE.getCode());
            setRemark(TaskLogTypeEnum.RECYCLE.getRemark());
        }};
        rLog.initSave(user);
        projectLogService.runLog(rLog);
        projectInfoService.updateSchedule(task.getProjectId());
        return update;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean taskRecovery(String taskId, UserDto user) {
        ProjectTask task = this.getById(taskId);
        verifyTaskExistAndThrows(task);
        if (CommonConst.NO_DELETE_STATUS.equals(task.getRecycleFlag())) {
            throw new BusinessException("任务已经恢复！");
        }
        boolean update = lambdaUpdate().eq(ProjectTask::getId, taskId)
                .set(ProjectTask::getRecycleFlag, CommonConst.NO_DELETE_STATUS)
                .update();
        //添加动态
        ProjectLog rLog = new ProjectLog() {{
            setProjectId(task.getProjectId());
            setIsComment(0);
            setMemberId(user.getId());
            setActionType(LogActionTypeEnum.TASK.getCode());
            setTaskId(taskId);
            setType(TaskLogTypeEnum.RECOVERY.getCode());
            setRemark(TaskLogTypeEnum.RECOVERY.getRemark());
        }};
        rLog.initSave(user);
        projectLogService.runLog(rLog);
        projectInfoService.updateSchedule(task.getProjectId());
        return update;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean del(String taskId, UserDto user) {
        ProjectTask task = getById(taskId);
        String versionId = task.getVersionId();
        boolean b = this.removeById(taskId);
        if (StrUtil.isNotEmpty(versionId)) {
            projectVersionService.updateSchedule(versionId);
        }
        projectInfoService.updateSchedule(task.getProjectId());
        return b;
    }

    @Override
    public Page<ProjectTask> listRecycle(Page<ProjectTask> page, String projectId) {
        return lambdaQuery().eq(ProjectTask::getProjectId, projectId)
                .eq(ProjectTask::getRecycleFlag, CommonConst.DELETE_STATUS).page(page);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean sort(ProjectTaskSortDTO dto, UserDto user) {
        List<String> taskIds = dto.getTaskIds();
        if (CollUtil.isEmpty(taskIds)) {
            return false;
        }
        //查询该列表的所有任务
        ProjectTaskMould taskMould = projectTaskMouldService.lambdaQuery().eq(ProjectTaskMould::getId, dto.getTaskMouldId()).one();
        for (int i = 0; i < taskIds.size(); i++) {
            ProjectTask task = getById(taskIds.get(i));
            lambdaUpdate()
                    .set(ProjectTask::getSort, i)
                    .set(ProjectTask::getMouldId, dto.getTaskMouldId()).
                    eq(ProjectTask::getId, taskIds.get(i)).update();
            if (!dto.getTaskMouldId().equals(task.getMouldId())) {
                //获取原来模型名称
                ProjectTaskMould byId = projectTaskMouldService.getById(task.getMouldId());
                //若任务列表不一样了 则添加动态 移动
                ProjectLog moveLog = new ProjectLog();
                moveLog.setContent(task.getName())
                        .setTaskId(taskIds.get(i))
                        .setType(TaskLogTypeEnum.MOVE.getCode())
                        .setRemark(String.format(TaskLogTypeEnum.MOVE.getRemark(), byId.getName(), taskMould.getName()))
                        .setIsComment(0)
                        .setMemberId(user.getId())
                        .setActionType(LogActionTypeEnum.TASK.getCode())
                        .setProjectId(taskMould.getProjectId());
                moveLog.initSave(user);
                projectLogService.runLog(moveLog);
            }
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean assignExecutorBatch(ProjectTaskAssignExecutorBatchDTO dto, UserDto user) {
        List<String> taskIds = dto.getTaskIds();
        ProjectTaskAssignExecutorDTO param;
        for (String taskId : taskIds) {
            param = new ProjectTaskAssignExecutorDTO();
            param.setExecutorInfo(dto.getExecutorInfo());
            param.setExecutorId(dto.getExecutorId());
            param.setTaskId(taskId);
            assignExecutor(param, user);
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean taskRecycleBatch(String mouldId, UserDto user) {
        ProjectTaskMould taskStage = projectTaskMouldService.lambdaQuery().eq(ProjectTaskMould::getId, mouldId).one();
        if (BeanUtil.isEmpty(taskStage)) {
            throw new BusinessException("任务列表不存在！");
        }
        List<ProjectTask> tasks = lambdaQuery().eq(ProjectTask::getMouldId, mouldId).eq(ProjectTask::getRecycleFlag, CommonConst.NO_DELETE_STATUS).list();
        if (CollUtil.isNotEmpty(tasks)) {
            ProjectLog delLog;
            for (ProjectTask task : tasks) {
                delLog = new ProjectLog();
                delLog.setProjectId(task.getProjectId())
                        .setMemberId(user.getId())
                        .setType(TaskLogTypeEnum.RECYCLE.getCode())
                        .setIsComment(0)
                        .setRemark(TaskLogTypeEnum.RECYCLE.getRemark())
                        .setTaskId(task.getId())
                        .setActionType(LogActionTypeEnum.TASK.getCode());
                delLog.initSave(user);
                projectLogService.runLog(delLog);
            }
        }
        lambdaUpdate().eq(ProjectTask::getMouldId, mouldId).eq(ProjectTask::getRecycleFlag, CommonConst.NO_DELETE_STATUS)
                .set(ProjectTask::getRecycleFlag, CommonConst.DELETE_STATUS).set(ProjectTask::getDelTime, LocalDateTime.now())
                .update();
        projectInfoService.updateSchedule(taskStage.getProjectId());
        return true;
    }

    @Override
    public List<ProjectTaskTreeMouldVO> taskTree(ProjectTaskReq req, UserDto user) {
        String projectId = req.getProjectId();
        List<ProjectTaskTreeMouldVO> result = Collections.synchronizedList(new ArrayList<>());
        //查询项目的任务列表
        List<ProjectTaskMould> moulds = projectTaskMouldService.lambdaQuery().eq(ProjectTaskMould::getProjectId, projectId)
                .orderByAsc(ProjectTaskMould::getSort).list();
        //根据任务列表id查询任务
        if (CollUtil.isNotEmpty(moulds)) {
            List<String> mouldIds = moulds.stream().map(ProjectTaskMould::getId).collect(Collectors.toList());
            list(Wrappers.<ProjectTask>lambdaQuery().in(ProjectTask::getMouldId, mouldIds).eq(ProjectTask::getRecycleFlag, CommonConst.NO_DELETE_STATUS));
            List<ProjectTaskTreeVO> taskTreeVOS;
            ProjectTaskTreeMouldVO mouldVO;
            ProjectTaskTreeVO treeVO;
            for (ProjectTaskMould x : moulds) {
                {
                    taskTreeVOS = new ArrayList<>();
                    mouldVO = new ProjectTaskTreeMouldVO();
                    BeanUtil.copyProperties(x, mouldVO);
                    //找顶层任务 填充
                    LambdaQueryWrapper<ProjectTask> wrapper = Wrappers.<ProjectTask>lambdaQuery();
                    wrapper.eq(ProjectTask::getMouldId, x.getId())
                            .eq(BeanUtil.isNotEmpty(req.getFinishStatus()), ProjectTask::getFinishStatus, req.getFinishStatus())
                            .like(StrUtil.isNotEmpty(x.getName()), ProjectTask::getName, req.getName())
                            .eq(ProjectTask::getRecycleFlag, CommonConst.NO_DELETE_STATUS)
                            .eq(ProjectTask::getPid, ProjectTaskConst.TOP_TASK_PID);
                    wrapper = fillTaskTreeWrapper(wrapper, req);
                    if (BeanUtil.isEmpty(wrapper)) {
                        continue;
                    }
                    List<ProjectTask> tasks = list(wrapper);
                    if (CollUtil.isNotEmpty(tasks)) {
                        for (ProjectTask t : tasks) {
                            //复制详情信息
                            treeVO = new ProjectTaskTreeVO();
                            ProjectTaskDetailVO detail = detail(t.getId(), user);
                            BeanUtil.copyProperties(detail, treeVO);
                            //添加子任务信息
                            fillChildTaskDetail(treeVO, user, req);
                            taskTreeVOS.add(treeVO);
                        }
                    }
                    //任务列表添加任务
                    mouldVO.setTasks(taskTreeVOS);
                    result.add(mouldVO);
                }
            }

        }
        return result;
    }

    /**
     * 填充条件
     *
     * @param wrapper
     * @param req
     * @return
     */
    @Override
    public LambdaQueryWrapper<ProjectTask> fillTaskTreeWrapper(LambdaQueryWrapper<ProjectTask> wrapper, ProjectTaskReq req) {
        if (getTreeWrapper(wrapper, req, projectTaskMemberService, projectLogService)) {
            return null;
        }
        return wrapper;
    }

    static boolean getTreeWrapper(LambdaQueryWrapper<ProjectTask> wrapper, ProjectTaskReq req, ProjectTaskMemberService projectTaskMemberService, ProjectLogService projectLogService) {
        if (StrUtil.isNotEmpty(req.getExecuteMemberId())) {
            String[] split = req.getExecuteMemberId().split(StrUtil.COMMA);
            wrapper.in(ProjectTask::getExecuteMemberId, split);
        }
        if (StrUtil.isNotEmpty(req.getCreateById())) {
            String[] split = req.getCreateById().split(StrUtil.COMMA);
            wrapper.in(ProjectTask::getCreateById, split);
        }
        if (StrUtil.isNotEmpty(req.getTaskMemberId())) {
            String[] split = req.getTaskMemberId().split(StrUtil.COMMA);
            //通过任务成员表查询任务
            List<ProjectTaskMember> list = projectTaskMemberService.lambdaQuery().in(ProjectTaskMember::getMemberId, split)
                    .select(ProjectTaskMember::getTaskId).list();
            List<String> collect = list.stream().map(ProjectTaskMember::getTaskId).collect(Collectors.toList());
            if (CollUtil.isEmpty(collect)) {
                return true;
            }
            wrapper.in(ProjectTask::getId, collect)
                    .eq(ProjectTask::getFinishStatus, 1);
        }
        if (StrUtil.isNotEmpty(req.getPriorityLevel())) {
            String[] split = req.getPriorityLevel().split(StrUtil.COMMA);
            wrapper.in(ProjectTask::getPriorityLevel, split);
        }
        if (StrUtil.isNotEmpty(req.getExecuteStatus())) {
            String[] split = req.getExecuteStatus().split(StrUtil.COMMA);
            wrapper.in(ProjectTask::getExecuteStatus, split);
        }
        if (StrUtil.isNotEmpty(req.getEndTime())) {
            String[] split = req.getEndTime().split(StrUtil.COMMA);
            wrapper.between(ProjectTask::getEndTime, split[0] + CommonConst.TIME_BEFORE, split[1] + CommonConst.TIME_AFTER);
        }
        if (StrUtil.isNotEmpty(req.getCreateTime())) {
            String[] split = req.getCreateTime().split(StrUtil.COMMA);
            wrapper.between(ProjectTask::getCreateTime, split[0] + CommonConst.TIME_BEFORE, split[1] + CommonConst.TIME_AFTER);
        }
        if (StrUtil.isNotEmpty(req.getDoneTime())) {
            //查日志中的完成时间
            String[] split = req.getDoneTime().split(StrUtil.COMMA);
            List<ProjectLog> list = projectLogService.lambdaQuery().between(ProjectLog::getCreateTime, split[0] + CommonConst.TIME_BEFORE, split[1] + CommonConst.TIME_AFTER)
                    .eq(ProjectLog::getActionType, LogActionTypeEnum.TASK)
                    .eq(ProjectLog::getType, TaskLogTypeEnum.DONE.getCode())
                    .select(ProjectLog::getTaskId).list();
            List<String> collect = list.stream().map(ProjectLog::getTaskId).collect(Collectors.toList());
            if (CollUtil.isEmpty(collect)) {
                return true;
            }
            wrapper.in(ProjectTask::getId, collect)
                    .eq(ProjectTask::getFinishStatus, 1);
        }
        if (StrUtil.isNotEmpty(req.getStartTime())) {
            String[] split = req.getStartTime().split(StrUtil.COMMA);
            wrapper.between(ProjectTask::getStartTime, split[0] + CommonConst.TIME_BEFORE, split[1] + CommonConst.TIME_AFTER);
        }
        return false;
    }

    @Override
    public List<ProjectTaskDetailVO> getListByTag(ProjectTaskTag dto, UserDto user) {
        List<ProjectTaskDetailVO> result = Collections.synchronizedList(new ArrayList<>());
        List<ProjectTaskToTag> taskIds = projectTaskToTagService.lambdaQuery().eq(ProjectTaskToTag::getTagId, dto.getId()).select(ProjectTaskToTag::getTaskId).list();
        if (CollUtil.isNotEmpty(taskIds)) {
            taskIds.forEach(x -> {
                ProjectTaskDetailVO detail = detail(x.getTaskId(), user);
                if (BeanUtil.isNotEmpty(detail)) {
                    result.add(detail);
                }
            });
        }
        return result;
    }

    private void fillChildTaskDetail(ProjectTaskTreeVO treeVO, UserDto user, ProjectTaskReq req) {
        //根据父任务 填充子任务
        LambdaQueryWrapper<ProjectTask> wrapper = Wrappers.<ProjectTask>lambdaQuery();
        wrapper.eq(BeanUtil.isNotEmpty(req.getFinishStatus()), ProjectTask::getFinishStatus, req.getFinishStatus())
                .like(StrUtil.isNotEmpty(req.getName()), ProjectTask::getName, req.getName())
                .eq(ProjectTask::getRecycleFlag, CommonConst.NO_DELETE_STATUS)
                .eq(ProjectTask::getPid, treeVO.getId())
                .eq(ProjectTask::getMouldId, treeVO.getId());
        wrapper = fillTaskTreeWrapper(wrapper, req);
        if (BeanUtil.isEmpty(wrapper)) {
            return;
        }
        List<ProjectTask> child = list(wrapper);
        if (CollUtil.isNotEmpty(child)) {
            List<ProjectTaskTreeVO> taskTreeVOS = new ArrayList<>();
            ProjectTaskTreeVO newTree;
            for (ProjectTask x : child) {
                //复制详情信息
                newTree = new ProjectTaskTreeVO();
                ProjectTaskDetailVO detail = detail(x.getId(), user);
                BeanUtil.copyProperties(detail, newTree);
                //添加子任务信息
                fillChildTaskDetail(newTree, user, req);
                taskTreeVOS.add(newTree);
            }
            treeVO.setChildTask(taskTreeVOS);
        }

    }

    /**
     * @param projectTask 任务信息
     * @param user        当前登录用户
     * @param dto         添加任务的参数
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveParentLog(ProjectTask projectTask, UserDto user, ProjectTaskAddDTO dto) {
        ProjectTask pTask = this.verifyParenTaskDeletedAndFinish(dto.getPid());

        ProjectLog pLog = new ProjectLog() {{
            setProjectId(pTask.getProjectId());
            setIsComment(0);
            setMemberId(user.getId());
            setActionType(LogActionTypeEnum.TASK.getCode());
            setTaskId(pTask.getId());
            setToMemberId(user.getId());
            setType(TaskLogTypeEnum.CREATE_CHILD.getCode());
            setContent(projectTask.getName());
            setRemark(String.format(TaskLogTypeEnum.CREATE_CHILD.getRemark(), dto.getName()));
        }};
        pLog.initSave(user);
        projectLogService.runLog(pLog);
    }

    /**
     * 保存指派或者认领的日志
     *
     * @param projectTask 任务信息
     * @param user        当前登录用户
     * @param userDto     执行人信息
     */
    @Transactional(rollbackFor = Exception.class)
    public void assignOrClaim(ProjectTask projectTask, UserDto user, UserDto userDto) {
        //执行人不为空则为指派，默认是认领 日志
        ProjectLog projectLog = new ProjectLog() {{
            setProjectId(projectTask.getProjectId());
            setIsComment(0);
            setMemberId(user.getId());
            setActionType(LogActionTypeEnum.TASK.getCode());
            setTaskId(projectTask.getId());
            setToMemberId(user.getId());
            setType(TaskLogTypeEnum.CLAIM.getCode());
            setContent(projectTask.getName());
            setRemark(TaskLogTypeEnum.CLAIM.getRemark());

        }};
        //若有执行者则为指派
        if (ObjectUtil.isNotEmpty(userDto) && userDto.getId().equals(user.getId())) {
            projectLog.setType(TaskLogTypeEnum.ASSIGN.getCode())
                    .setRemark(String.format(TaskLogTypeEnum.ASSIGN.getRemark(), userDto.getRealName()))
                    .setToMemberId(userDto.getId());
        }
        projectLog.initSave(user);
        projectLogService.runLog(projectLog);
    }


    /**
     * 这是一个验证项目信息的方法
     *
     * @param projectTask
     */
    public void verifyProject(ProjectTask projectTask) {
        ProjectInfo projectInfo = projectInfoService.getById(projectTask.getProjectId());
        projectInfoService.verifyExist(projectInfo);
        projectInfoService.verifyDel(projectInfo);
    }

    /**
     * 这是一个验证项目任务列表的方法
     *
     * @param projectTask
     */
    public void verifyTaskMould(ProjectTask projectTask) {
        ProjectTaskMould taskMould = projectTaskMouldService.lambdaQuery()
                .eq(ProjectTaskMould::getId, projectTask.getMouldId()).one();
        if (BeanUtil.isEmpty(taskMould)) {
            throw new BusinessException("该任务列表已删除或不存在！");
        }
    }

    /**
     * 这是一个验证执行者的方法
     *
     * @param projectTask
     */
    public void verifyExecuteMember(ProjectTask projectTask) {
        ProjectMember projectMember = projectMemberService.lambdaQuery()
                .eq(ProjectMember::getProjectId, projectTask.getProjectId())
                .eq(ProjectMember::getMemberId, projectTask.getExecuteMemberId())
                .one();
        if (BeanUtil.isEmpty(projectMember)) {
            throw new BusinessException("任务执行人有误！");
        }
    }

    /**
     * 这是一个验证父任务状态的方法，删除或者已完成等,并返回父任务信息
     *
     * @param pid
     */
    public ProjectTask verifyParenTaskDeletedAndFinish(String pid) {
        ProjectTask task = this.getById(pid);
        if (BeanUtil.isEmpty(task)) {
            throw new BusinessException("父任务不存在！");
        }
        if (CommonConst.DELETE_STATUS.equals(task.getRecycleFlag())) {
            throw new BusinessException("父任务在回收站中无法编辑！");
        }
        if (1 == task.getFinishStatus()) {
            throw new BusinessException("父任务已完成，无法添加新的子任务！");
        }
        return task;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveImportList(List<ProjectTask> taskList, UserDto login, String projectId) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern(CommonConst.DATE_TIME_MINUTE);
        //先留一个副本为
        List<ProjectTask> orin = taskList;
        Map<String, String> savedTask = new HashMap<>();
        //查询项目成员
        List<ProjectMember> projectMembers = projectMemberService.lambdaQuery().eq(ProjectMember::getProjectId, projectId).select(ProjectMember::getMemberId).list();
        //查询项目标签 id 和name
        List<ProjectTaskTag> tags = projectTaskTagService.lambdaQuery().eq(ProjectTaskTag::getProjectId, projectId).select(ProjectTaskTag::getId).select(ProjectTaskTag::getName).list();
        //查询项目任务列表 id 和 name
        List<ProjectTaskMould> moulds = projectTaskMouldService.lambdaQuery().eq(ProjectTaskMould::getProjectId, projectId).select(ProjectTaskMould::getId).select(ProjectTaskMould::getName).list();
        if (CollUtil.isEmpty(moulds)) {
            return;
        }

        ProjectTaskToTag toTag;
        ProjectTaskAddDTO addDTO;
        //进行保存
        for (ProjectTask x : taskList) {
            {
                String xName = x.getName();
                if (StrUtil.isNotEmpty(xName) || StrUtil.isNotEmpty(x.getMouldId())) {
                    boolean mouldChoose = true;
                    for (ProjectTaskMould mould : moulds) {
                        if (mould.getName().equals(x.getMouldId())) {
                            mouldChoose = false;
                            x.setMouldId(mould.getId());
                            break;
                        }
                    }
                    if (mouldChoose) {
                        return;
                    }
                    //若有父级任务  优先从查询列表中否存在父级任务(只查询 列表下最顶层任务)，若没有则从本次导入的任务中选择，若都没有 不导入
                    if (StrUtil.isNotEmpty(x.getPName())) {
                        List<ProjectTask> pTasks = lambdaQuery().eq(ProjectTask::getMouldId, x.getMouldId())
                                .eq(ProjectTask::getRecycleFlag, CommonConst.NO_DELETE_STATUS)
                                .eq(ProjectTask::getName, x.getPName())
                                .eq(ProjectTask::getPid, ProjectTaskConst.TOP_TASK_PID)
                                .eq(ProjectTask::getFinishStatus, 0).list();
                        if (CollUtil.isEmpty(pTasks)) {
                            if (savedTask.containsKey(x.getPName())) {
                                x.setPid(savedTask.get(x.getPName()));
                            } else {
                                return;
                            }
                        } else {
                            x.setPid(pTasks.get(0).getId());
                        }
                    }

                    //验证了列表 父任务 可以保存了
                    addDTO = new ProjectTaskAddDTO();
                    List<UserDto> importMember = x.getImportMember();
                    if (CollUtil.isNotEmpty(importMember)) {
                        confirm:
                        for (UserDto userDto : importMember) {
                            for (ProjectMember projectMember : projectMembers) {
                                if (userDto.getId().equals(projectMember.getMemberId())) {
                                    //匹配到指派人
                                    x.setExecuteMemberId(userDto.getId());
                                    addDTO.setExecuteMember(userDto);
                                    addDTO.setExecuteMemberId(userDto.getId());
                                    break confirm;
                                }
                            }
                        }
                    }
                    addDTO.setName(xName);
                    addDTO.setPid(x.getPid());
                    addDTO.setMouldId(x.getMouldId());
                    addDTO.setProjectId(projectId);
                    ProjectTask st = saveTask(addDTO, login, true);
                    savedTask.put(st.getName(), st.getId());
                    x.setId(st.getId());
                    //剩余的信息更新
                    LambdaUpdateChainWrapper<ProjectTask> updateWrapper = lambdaUpdate().eq(ProjectTask::getId, st.getId());

                    if (StrUtil.isNotEmpty(x.getStartTimeStr())) {
                        LocalDateTime time = LocalDateTime.parse(x.getStartTimeStr(), fmt);
                        updateWrapper.set(ProjectTask::getStartTime, time);
                        x.setStartTime(time);
                    }
                    if (StrUtil.isNotEmpty(x.getEndTimeStr())) {
                        LocalDateTime time = LocalDateTime.parse(x.getEndTimeStr(), fmt);
                        updateWrapper.set(ProjectTask::getEndTime, time);
                        x.setStartTime(time);
                    }
                    if (StrUtil.isNotEmpty(x.getPriorityLevel())) {
                        String lev = ProjectTaskConst.transPriStrToNum(x.getPriorityLevel());
                        x.setPriorityLevel(lev);
                        updateWrapper.set(ProjectTask::getPriorityLevel, lev);
                    }
                    updateWrapper.update();
                    //保存标签
                    if (CollUtil.isNotEmpty(tags)) {
                        if (StrUtil.isNotEmpty(x.getTaskTag())) {
                            String[] split = x.getTaskTag().split(";");
                            List<String> tagName = Arrays.stream(split).distinct().collect(Collectors.toList());
                            for (String name : tagName) {
                                for (ProjectTaskTag tag : tags) {
                                    if (name.equals(tag.getName())) {
                                        toTag = new ProjectTaskToTag();
                                        toTag.setTagId(tag.getId());
                                        toTag.setTaskId(st.getId());
                                        setTag(toTag, login);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        projectInfoService.updateSchedule(projectId);

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProjectTask taskCopy(TaskCopyDTO dto, UserDto currentUser) {
        UserDto member = dto.getExecuteMember();
        Integer copyExecutor = dto.getCopyExecutor();
        Integer copyChild = dto.getCopyChild();
        Integer copyMember = dto.getCopyMember();
        Integer copyWorkTime = dto.getCopyWorkTime();
        boolean copyMemberB = BeanUtil.isNotEmpty(copyMember) && (copyMember == 1);
        verifyCopyAndMoveProjectAndMould(dto.getProjectId(), dto.getMouldId());
        //思路：保存新的任务 根据复制内容进行相应操作
        ProjectTask orinTask = getById(dto.getTaskId());
        //目标项目中的成员
        List<ProjectMember> projectMembers = projectMemberService.listProjectMember(dto.getProjectId());
        //目标项目所有成员id
        List<String> memberIdList = projectMembers.stream().map(ProjectMember::getMemberId).collect(Collectors.toList());
        ProjectTaskAddDTO saveDto = new ProjectTaskAddDTO();
        saveDto.setProjectId(dto.getProjectId());
        saveDto.setName(dto.getName());
        if (BeanUtil.isNotEmpty(copyExecutor) && copyExecutor == 1) {
            if (BeanUtil.isNotEmpty(member)) {
                boolean b = memberIdList.contains(member.getId());
                if (b) {
                    saveDto.setExecuteMemberId(member.getId());
                    saveDto.setExecuteMember(member);
                }
            }
        }
        saveDto.setMouldId(dto.getMouldId());
        saveDto.setPid("-1");
        ProjectTask saveTask = saveTask(saveDto, currentUser, true);
        LambdaUpdateChainWrapper<ProjectTask> wrapper = lambdaUpdate().eq(ProjectTask::getId, saveTask.getId());
        if (copyMemberB) {
            List<ProjectTaskMember> orinMember = projectTaskMemberService.getMemberByTaskId(orinTask.getId());
            //跨项目 复制任务参与者 目标项目中是否存在，不存在会丢失,存在则添加任务成员
            if (dto.getProjectId().equals(orinTask.getProjectId())) {
                //不跨项目 直接复制
                TaskMemberAddDTO taskMemberAddDTO;
                for (ProjectTaskMember om : orinMember) {
                    //若为当前操作用户
                    boolean cur = currentUser.getId().equals(om.getMemberId());
                    if (cur) {
                        continue;
                    }
                    taskMemberAddDTO = new TaskMemberAddDTO();
                    taskMemberAddDTO.setTaskId(saveTask.getId());
                    taskMemberAddDTO.setMemberId(om.getMemberId());
                    taskMemberAddDTO.setIsOwner(0);
                    taskMemberAddDTO.setIsExecutor(0);
                    projectTaskMemberService.inviteMember(taskMemberAddDTO, currentUser, false);

                }
            } else {
                TaskMemberAddDTO taskMemberAddDTO;
                //跨项目
                for (ProjectTaskMember om : orinMember) {
                    //若为当前操作用户
                    boolean cur = currentUser.getId().equals(om.getMemberId());
                    if (cur) {
                        continue;
                    }
                    //任务成员中不存在且目标项目中存在 添加任务成员
                    boolean tex = projectTaskMemberService.verifyExists(om.getMemberId(), saveTask.getId());
                    boolean pex = memberIdList.contains(om.getMemberId());
                    if ((!tex) && pex) {
                        taskMemberAddDTO = new TaskMemberAddDTO();
                        taskMemberAddDTO.setTaskId(saveTask.getId());
                        taskMemberAddDTO.setMemberId(om.getMemberId());
                        taskMemberAddDTO.setIsOwner(0);
                        taskMemberAddDTO.setIsExecutor(0);
                        projectTaskMemberService.inviteMember(taskMemberAddDTO, currentUser, false);
                    }

                }
            }

        }

        if (BeanUtil.isNotEmpty(copyWorkTime) && copyWorkTime == 1) {
            wrapper.set(ProjectTask::getInitWorkingHours, orinTask.getInitWorkingHours()).update();
            saveTask.setInitWorkingHours(orinTask.getInitWorkingHours());
        }
        if (BeanUtil.isNotEmpty(copyChild) && copyChild == 1) {
            copyChildTask(orinTask, saveTask, currentUser);
        }
        return saveTask;
    }

    /**
     * 这是一个任务复制时候勾选子任务复制的实现方法
     *
     * @param oldPTask 原父任务
     * @param newPTask 现父任务
     * @param login
     */
    @Transactional(rollbackFor = Exception.class)
    public void copyChildTask(ProjectTask oldPTask, ProjectTask newPTask, UserDto login) {
        List<ProjectTask> childs = lambdaQuery().eq(ProjectTask::getPid, oldPTask.getId()).list();
        if (CollUtil.isNotEmpty(childs)) {
            ProjectTaskAddDTO dto;
            for (ProjectTask child : childs) {
                dto = new ProjectTaskAddDTO();
                dto.setName(child.getName());
                dto.setPid(newPTask.getId());
                dto.setProjectId(newPTask.getProjectId());
                dto.setMouldId(newPTask.getMouldId());
                ProjectTask task = saveTask(dto, login, false);
                copyChildTask(child, task, login);
            }
        }
    }

    /**
     * 任务移动复制 验证目标项目和任务列表
     *
     * @param projectId
     * @param mouldId
     */
    public void verifyCopyAndMoveProjectAndMould(String projectId, String mouldId) {
        ProjectInfo projectInfo = projectInfoService.getById(projectId);
        projectInfoService.verifyExist(projectInfo);
        projectInfoService.verifyDel(projectInfo);
        ProjectTaskMould taskMould = projectTaskMouldService.getById(mouldId);
        if (BeanUtil.isEmpty(taskMould) || taskMould.getRecycleFlag() == 1) {
            throw new BusinessException("目标任务列表在回收站或者已失效！");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProjectTask taskMove(TaskMoveDTO dto, UserDto login) {
        String projectId = dto.getProjectId();
        //1、查询项目 2、查询任务列表 3、查询任务 4、查询任务成员  验证相关信息
        verifyCopyAndMoveProjectAndMould(projectId, dto.getMouldId());
        ProjectTaskMould taskMould = projectTaskMouldService.getById(dto.getMouldId());
        ProjectTask orinTask = getById(dto.getTaskId());
        String orinTaskProjectId = orinTask.getProjectId();
        String orinTaskMouldId = orinTask.getMouldId();
        if (orinTaskProjectId.equals(projectId) && orinTaskMouldId.equals(dto.getMouldId())) {
            //未发生改变不做操作
            return orinTask;
        }
        ProjectTaskMould orinMould = projectTaskMouldService.getById(orinTaskMouldId);
        //跨项目和不跨项目
        //需要改变的任务
        List<String> taskIds = Collections.synchronizedList(new ArrayList<>());
        //该模板下的所有任务
        List<ProjectTask> tasks = lambdaQuery().eq(ProjectTask::getMouldId, orinTaskMouldId).list();
        collectTaskIdForSelfAndChild(tasks, orinTask.getId(), taskIds);
        if (!orinTaskProjectId.equals(projectId)) {
            orinTask.setProjectId(projectId)
                    .setMouldId(dto.getMouldId());
            //目标项目中的成员是否包含此任务以及子任务的执行者，不包含则任务对应的成员删除
            List<ProjectMember> projectMembers = projectMemberService.lambdaQuery().eq(ProjectMember::getProjectId, projectId).list();
            List<String> projectMembersId = projectMembers.stream().map(ProjectMember::getMemberId).collect(Collectors.toList());
            deleteSelfAndChildTaskMember(new HashSet<>(projectMembersId), orinTask);
            //任务需要改变的列表
            lambdaUpdate().in(ProjectTask::getId, taskIds).set(ProjectTask::getMouldId, dto.getMouldId()).update();
            //任务需要改变的项目
            lambdaUpdate().in(ProjectTask::getId, taskIds).set(ProjectTask::getProjectId, projectId).update();
            projectInfoService.updateSchedule(orinTaskProjectId);
            projectInfoService.updateSchedule(projectId);
        } else if (!orinTaskMouldId.equals(dto.getMouldId())) {
            orinTask.setMouldId(dto.getMouldId());
            lambdaUpdate().in(ProjectTask::getId, taskIds).set(ProjectTask::getMouldId, dto.getMouldId()).update();
        }
        //保存项目移动的日志
        ProjectLog projectLog = new ProjectLog();
        projectLog.setProjectId(projectId)
                .setTaskId(dto.getTaskId())
                .setActionType(LogActionTypeEnum.TASK.getCode())
                .setIsComment(0)
                .setRemark(String.format(TaskLogTypeEnum.MOVE.getRemark(), orinMould.getName(), taskMould.getName()))
                .setType(TaskLogTypeEnum.MOVE.getCode())
                .setMemberId(login.getId());
        projectLog.initSave(login);
        projectLogService.runLog(projectLog);
        return orinTask;
    }

    /**
     * 收集任务和所有子任务的id
     *
     * @param source  任务列表大全
     * @param selfId  任务自己的id
     * @param collect 收集id的容器
     */
    private void collectTaskIdForSelfAndChild(List<ProjectTask> source, String selfId, List<String> collect) {
        //收集自身
        collect.add(selfId);
        //找子任务
        List<ProjectTask> childs = source.stream().filter(x -> x.getPid().equals(selfId)).collect(Collectors.toList());
        if (CollUtil.isNotEmpty(childs)) {
            //子任务收集
            for (ProjectTask child : childs) {
                collectTaskIdForSelfAndChild(source, child.getId(), collect);
            }
        }
    }

    @Override
    public ProjectTaskCountForDateVO taskDateCount(TaskDateCountReq dto) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        ProjectTaskCountForDateVO result = new ProjectTaskCountForDateVO();
        ArrayList<ProjectTaskCountForDateVO.TaskDateAndCount> list = new ArrayList<>();
        String endTime = dto.getEndTime();
        String beginTime = dto.getBeginTime();
        LocalDate nowDate = LocalDate.now();
        if (StrUtil.isEmpty(beginTime)) {
            beginTime = nowDate.plusDays(-20).format(formatter);
        }
        if (StrUtil.isEmpty(endTime)) {
            endTime = nowDate.format(formatter);
        }
        List<String> dateList = DateUtil.getDateStr(beginTime, endTime);
        ProjectTaskCountForDateVO.TaskDateAndCount tdac;
        for (String date : dateList) {
            tdac = new ProjectTaskCountForDateVO.TaskDateAndCount();
            Integer count = lambdaQuery().eq(ProjectTask::getProjectId, dto.getProjectId())
                    .eq(ProjectTask::getRecycleFlag, CommonConst.NO_DELETE_STATUS)
                    .between(ProjectTask::getCreateTime, date + CommonConst.TIME_BEFORE, date + CommonConst.TIME_AFTER)
                    .count();
            tdac.setDate(date);
            tdac.setCount(count);
            list.add(tdac);
        }
        result.setResult(list);
        return result;
    }

    @Override
    public Map<String, Integer> getProjectStatus(ProjectTask dto) {
        ProjectInfo projectInfo = projectInfoService.getById(dto.getProjectId());
        projectInfoService.verifyExist(projectInfo);
        projectInfoService.verifyDel(projectInfo);
        List<ProjectTask> taskList = lambdaQuery().eq(ProjectTask::getProjectId, dto.getProjectId())
                .eq(ProjectTask::getRecycleFlag, CommonConst.NO_DELETE_STATUS).list();
        if (CollUtil.isEmpty(taskList)) {
            taskList = new ArrayList<>();
        }
        LocalDateTime nowTime = LocalDateTime.now();
        LocalDateTime today = nowTime;
        LocalDateTime tom = nowTime.plusDays(1);
        LocalDateTime now = nowTime;
//        AtomicInteger unDone = new AtomicInteger(0);
        final Integer[] unDone = {0};
        final Integer[] done = {0};
        final Integer[] overdue = {0};
        final Integer[] toBeAssign = {0};
        final Integer[] expireToday = {0};
        final Integer[] doneOverdue = {0};
        if (CollUtil.isNotEmpty(taskList)) {
            taskList.forEach(t -> {
                if (StrUtil.isEmpty(t.getExecuteMemberId())) {
                    toBeAssign[0]++;
                }
                if (BeanUtil.isNotEmpty(t.getFinishStatus()) && t.getFinishStatus() == 1) {
                    done[0]++;
                } else {
//                    unDone.set(unDone.get()+1);
                    unDone[0]++;
                }
                if (BeanUtil.isNotEmpty(t.getEndTime())) {
                    if (BeanUtil.isNotEmpty(t.getFinishStatus()) && t.getFinishStatus() == 0) {
                        //todo 今日未完成暂时放进逾期完成 后面有需求再改
                        if (t.getEndTime().isBefore(tom) && (t.getEndTime().isEqual(today)) || t.getEndTime().isAfter(today)) {
                            doneOverdue[0]++;
                        }
                        if (t.getEndTime().isBefore(now)) {
                            overdue[0]++;
                        }
                        LocalDate endDate = t.getEndTime().toLocalDate();
                        LocalDate nowDate = now.toLocalDate();
                        if (endDate.isEqual(nowDate)) {
                            expireToday[0]++;
                        }
                    } else {
                        List<ProjectLog> logList = projectLogService.lambdaQuery().eq(ProjectLog::getActionType, LogActionTypeEnum.TASK.getCode())
                                .eq(ProjectLog::getTaskId, t.getId()).eq(ProjectLog::getType, TaskLogTypeEnum.DONE.getCode()).list();
                        if (CollUtil.isNotEmpty(logList)) {
                            if (t.getEndTime().isBefore(logList.get(0).getCreateTime())) {
                                doneOverdue[0]++;
                            }
                        }
                    }
                }
            });
        }
        Map data = new HashMap(7);
        data.put("total", taskList.size());
        data.put("unDone", unDone[0]);
        data.put("done", done[0]);
        data.put("overdue", overdue[0]);
        data.put("toBeAssign", toBeAssign[0]);
        data.put("expireToday", expireToday[0]);
        data.put("doneOverdue", doneOverdue[0]);
        return data;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean cancelRelationFile(ProjectFileInfo fileInfo, UserDto currentUser) {
        String taskId = fileInfo.getTaskId();
        ProjectTask task = this.getById(taskId);
//        ProjectFileRelation fileRelation = projectFileRelationService.lambdaQuery().eq(ProjectFileRelation::getLinkId, taskId)
//                .eq(ProjectFileRelation::getLinkType, FileRelationTypeEnum.TASK_TYPE.getCode()).one();
//        if (BeanUtil.isEmpty(fileRelation)) {
//            throw new BusinessException("文件和任务未关联！");
//        }
//        boolean b = projectFileRelationService.removeById(fileRelation);

        projectFileInfoService.updateById(new ProjectFileInfo().setId(fileInfo.getId()).setRecycleFlag(1));
        ProjectLog projectLog = new ProjectLog();
        projectLog.setProjectId(task.getProjectId())
                .setRemark(TaskLogTypeEnum.UNLINK_FILE.getRemark())
                .setTaskId(taskId)
                .setType(TaskLogTypeEnum.UNLINK_FILE.getCode())
                .setActionType(LogActionTypeEnum.TASK.getCode())
                .setIsComment(0)
                .setIsRobot(0)
                .setMemberId(currentUser.getId());
        projectLog.initSave(currentUser);
        projectLogService.runLog(projectLog);
        return true;
    }

    /**
     * 设置任务执行人
     *
     * @param executeMemberId 成员id
     * @param taskId          任务id
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateExecuteMember(String executeMemberId, String taskId) {
        lambdaUpdate().eq(ProjectTask::getId, taskId)
                .set(ProjectTask::getExecuteMemberId, executeMemberId)
                .update();
    }

    /**
     * 设置任务执行人为null
     *
     * @param taskId 任务id
     */
    public void updateExecuteMember(String taskId) {
        updateExecuteMember(null, taskId);
    }

    /**
     * @param task             任务
     * @param projectMembersId 目标项目成员id
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteTaskMember(Set<String> projectMembersId, ProjectTask task) {
        String taskId = task.getId();
        List<ProjectTaskMember> taskMembers = projectTaskMemberService.lambdaQuery().eq(ProjectTaskMember::getTaskId, taskId).list();
        List<String> delMid = new ArrayList<>();
        for (ProjectTaskMember ptm : taskMembers) {
            //包含成员则跳过，不包含则添加进删除集合中
            if (projectMembersId.contains(ptm.getMemberId())) {
                continue;
            }
            delMid.add(ptm.getId());
        }
        if (CollUtil.isNotEmpty(delMid)) {
            //若删除的成员中有执行者 更新任务执行人
            if (delMid.contains(task.getExecuteMemberId())) {
                updateExecuteMember(taskId);
            }
            projectTaskMemberService.removeByIds(delMid);
        }
    }

    /**
     * 根据目标项目成员id 和 任务
     * 删除任务成员中，项目成员不存在的成员
     *
     * @param projectMembersId
     * @param task
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteSelfAndChildTaskMember(Set<String> projectMembersId, ProjectTask task) {
        deleteTaskMember(projectMembersId, task);
        //查询是否有子任务
        List<ProjectTask> childs = lambdaQuery().eq(ProjectTask::getPid, task.getId()).list();
        if (CollUtil.isNotEmpty(childs)) {
            for (ProjectTask child : childs) {
                deleteSelfAndChildTaskMember(projectMembersId, child);
            }
        }
    }
}
