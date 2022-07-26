package cn.bctools.teamwork.service.impl;

import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.teamwork.common.consts.CommonConst;
import cn.bctools.teamwork.common.consts.ProjectVersionStatusConst;
import cn.bctools.teamwork.common.enums.ProjectVersionLogTypeEnum;
import cn.bctools.teamwork.dto.ProjectVersionEditDTO;
import cn.bctools.teamwork.dto.VersionTaskDTO;
import cn.bctools.teamwork.entity.ProjectFeatures;
import cn.bctools.teamwork.entity.ProjectTask;
import cn.bctools.teamwork.entity.ProjectVersion;
import cn.bctools.teamwork.entity.ProjectVersionLog;
import cn.bctools.teamwork.mapper.ProjectVersionMapper;
import cn.bctools.teamwork.service.ProjectFeaturesService;
import cn.bctools.teamwork.service.ProjectTaskService;
import cn.bctools.teamwork.service.ProjectVersionLogService;
import cn.bctools.teamwork.service.ProjectVersionService;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Auto Generator
 */
@Service
public class ProjectVersionServiceImpl extends ServiceImpl<ProjectVersionMapper, ProjectVersion> implements ProjectVersionService {

    private final String TIME_START="start";
    private final String TIME_PLAN="plan";

    @Autowired
    ProjectFeaturesService projectFeaturesService;
    @Autowired
    ProjectVersionLogService projectVersionLogService;
    @Autowired
    ProjectTaskService projectTaskService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProjectVersion saveVersion(ProjectVersion dto, UserDto login) {
        ProjectFeatures features = projectFeaturesService.getById(dto.getFeaturesId());
        if (BeanUtil.isEmpty(features)) {
            throw new BusinessException("该版本库已失效！");
        }
        String name = dto.getName();
        boolean check = checkVersionName(name, null, dto.getFeaturesId());
        if (check) {
            throw new BusinessException("该版本名称已存在！");
        }
        LocalDateTime now = LocalDateTime.now();
        if (BeanUtil.isEmpty(dto.getPlanPublishTime())) {
            dto.setPlanPublishTime(now);
        }
        if (BeanUtil.isEmpty(dto.getStartTime())) {
            dto.setStartTime(now);
        }
        dto.initSave(login);
        save(dto);
        ProjectVersionLog versionLog = new ProjectVersionLog();
        versionLog.setContent(name)
                .setMemberId(login.getId())
                .setProjectId(features.getProjectId())
                .setSourceId(dto.getId())
                .setRemark(ProjectVersionLogTypeEnum.CREATE.getName())
                .setType(ProjectVersionLogTypeEnum.CREATE.getCode());
        versionLog.initSave(login);
        projectVersionLogService.save(versionLog);
        return dto;
    }

    @Override
    public List<ProjectVersion> getListByFeaturesId(String featuresId) {
        List<ProjectVersion> list = lambdaQuery().eq(ProjectVersion::getFeaturesId, featuresId).list();
        if (CollUtil.isNotEmpty(list)) {
            list.forEach(x -> {
                String str = ProjectVersionStatusConst.transNumToStr(x.getStatus());
                x.setStatusText(str);
            });
        }
        return list;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public VersionTaskDTO saveVersionTask(VersionTaskDTO dto, UserDto currentUser) {
        List<ProjectTask> tasks = dto.getTasks();
        ProjectVersion version = getById(dto.getId());
        if (BeanUtil.isEmpty(version)) {
            throw new BusinessException("该版本已经失效！");
        }
        StringBuffer relation = new StringBuffer();
        List<String> taskIds = tasks.stream().map(ProjectTask::getId).collect(Collectors.toList());
        List<ProjectTask> list = projectTaskService.lambdaQuery().in(ProjectTask::getId, taskIds).list();
        if (CollUtil.isNotEmpty(list)) {
            for (ProjectTask task : list) {
                if (StrUtil.isNotEmpty(task.getVersionId())) {
                    throw new BusinessException("有任务已被关联！任务名称是：" + task.getName());
                }
                relation.append(" " + task.getName());
            }
        } else {
            throw new BusinessException("全部任务已失效！");
        }
        String relationStr = relation.toString().trim();
        projectTaskService.lambdaUpdate().in(ProjectTask::getId, taskIds)
                .set(ProjectTask::getVersionId, version.getId())
                .set(ProjectTask::getFeaturesId, version.getFeaturesId())
                .set(ProjectTask::getUpdateBy, StrUtil.isEmpty(currentUser.getRealName()) ? currentUser.getAccountName() : currentUser.getRealName())
                .set(ProjectTask::getUpdateTime, LocalDateTime.now())
                .update();
        ProjectVersionLog versionLog = new ProjectVersionLog();
        versionLog.setMemberId(currentUser.getId())
                .setType(ProjectVersionLogTypeEnum.ADD_TASK.getCode())
                .setContent(relationStr)
                .setFeaturesId(version.getFeaturesId())
                .setSourceId(version.getId())
                .setRemark(String.format(ProjectVersionLogTypeEnum.ADD_TASK.getName(), list.size()));
        versionLog.initSave(currentUser);
        projectVersionLogService.save(versionLog);
        dto.setTasks(list);
        return dto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeVersionTask(ProjectTask dto, UserDto currentUser) {
        ProjectTask task = projectTaskService.getById(dto.getId());
        if (BeanUtil.isEmpty(task)) {
            throw new BusinessException("该任务已失效！");
        }
        String versionId = task.getVersionId();
        boolean update = projectTaskService.lambdaUpdate().eq(ProjectTask::getId, task.getId())
                .set(ProjectTask::getVersionId, null)
                .set(ProjectTask::getFeaturesId, null)
                .set(ProjectTask::getUpdateBy, StrUtil.isEmpty(currentUser.getRealName()) ? currentUser.getAccountName() : currentUser.getRealName())
                .set(ProjectTask::getUpdateTime, LocalDateTime.now()).update();

        if (StrUtil.isNotEmpty(versionId)) {
            //更新进度
            updateSchedule(versionId);
            //版本记录动态
            ProjectVersionLog log = new ProjectVersionLog();
            log.setMemberId(currentUser.getId())
                    .setSourceId(versionId)
                    .setType(ProjectVersionLogTypeEnum.REMOVE_TASK.getCode())
                    .setRemark(ProjectVersionLogTypeEnum.REMOVE_TASK.getName())
                    .setContent(task.getName())
                    .setProjectId(task.getProjectId())
                    .setFeaturesId(task.getFeaturesId());
            log.initSave(currentUser);
            projectVersionLogService.save(log);
        }
        return update;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSchedule(String versionId) {
        ProjectVersion version = getById(versionId);
        List<ProjectTask> tasks = projectTaskService.lambdaQuery().eq(ProjectTask::getVersionId, versionId)
                .eq(ProjectTask::getRecycleFlag, CommonConst.NO_DELETE_STATUS).list();
        Integer done = 0;
        if (CollUtil.isNotEmpty(tasks)) {
            for (ProjectTask task : tasks) {
                Integer status = task.getFinishStatus();
                if (BeanUtil.isNotEmpty(status) && status == 1) {
                    done++;
                }
            }
            int size = tasks.size();
            size = size > 0 ? size : 1;
            float schedule = ((float) done / (float) size) * 100;
            version.setSchedule((int) schedule);
            updateById(version);
        }

    }

    @Override
    public List<ProjectTask> getVersionTask(ProjectVersion dto) {
        String versionId = dto.getId();
        List<ProjectTask> list = projectTaskService.lambdaQuery().eq(ProjectTask::getVersionId, versionId)
                .eq(ProjectTask::getRecycleFlag, CommonConst.NO_DELETE_STATUS).list();
        return list;
    }

    /**
     * 检查数是版本库中否存在相同的版本名字
     *
     * @param name       *版本名称
     * @param featuresId * 版本库id
     * @param versionId  需要排除的版本id
     */
    private boolean checkVersionName(String name, String versionId, String featuresId) {
        LambdaQueryChainWrapper<ProjectVersion> wrapper = lambdaQuery().eq(ProjectVersion::getName, name)
                .eq(ProjectVersion::getFeaturesId, featuresId);
        if (StrUtil.isNotEmpty(versionId)) {
            wrapper.ne(ProjectVersion::getId, versionId);
        }
        wrapper.last(" LIMIT 1");
        ProjectVersion CHECK = wrapper.one();
        if (BeanUtil.isNotEmpty(CHECK)) {
            return true;
        }
        return false;
    }

    ;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProjectVersion edit(ProjectVersionEditDTO dto, UserDto currentUser) {
        String versionId = dto.getVersionId();
        ProjectVersion version = getById(versionId);
        if (BeanUtil.isEmpty(version)) {
            throw new BusinessException("此版本无效！");
        }
        boolean updateMark=false;
        String name = dto.getName();
        if (StrUtil.isNotEmpty(name)) {
            version.setName(name);
            boolean check = checkVersionName(name, version.getId(), version.getFeaturesId());
            if (check) {
                throw new BusinessException("该版本名称已存在!");
            }
        }
        ProjectFeatures features = projectFeaturesService.getById(version.getFeaturesId());
        ProjectVersionLog versionLog = new ProjectVersionLog()
                .setProjectId(features.getProjectId())
                .setMemberId(currentUser.getId())
                .setFeaturesId(features.getId())
                .setSourceId(version.getId());
        LambdaUpdateChainWrapper<ProjectVersion> updateChainWrapper = lambdaUpdate().set(ProjectVersion::getId, version.getId());
        if (StrUtil.isNotEmpty(name)) {
            updateChainWrapper.set(ProjectVersion::getName,name);
            versionLog.setType(ProjectVersionLogTypeEnum.NAME.getCode())
                    .setRemark(ProjectVersionLogTypeEnum.NAME.getName())
                    .setContent(name);
            updateMark=true;
        }
        String description = dto.getDescription();
        if (StrUtil.isNotEmpty(description)) {
            updateChainWrapper.set(ProjectVersion::getDescription,description);
            versionLog.setType(ProjectVersionLogTypeEnum.CONTENT.getCode())
                    .setRemark(ProjectVersionLogTypeEnum.CONTENT.getName())
                    .setContent(description);
            updateMark=true;
        }
        if (StrUtil.isEmpty(description) && StrUtil.isNotEmpty(version.getDescription())) {
            updateChainWrapper.set(ProjectVersion::getDescription,null);
            versionLog.setType(ProjectVersionLogTypeEnum.CLEAR_CONTENT.getCode())
                    .setRemark(ProjectVersionLogTypeEnum.CLEAR_CONTENT.getName());
            updateMark=true;
        }
        LocalDateTime startTime = dto.getStartTime();
        if (BeanUtil.isNotEmpty(startTime)) {
            updateChainWrapper.set(ProjectVersion::getStartTime,startTime);
            versionLog.setType(ProjectVersionLogTypeEnum.SET_START_TIME.getCode())
                    .setRemark(String.format(ProjectVersionLogTypeEnum.CONTENT.getName(), DateUtil.format(startTime, DatePattern.NORM_DATETIME_PATTERN)));
       updateMark=true;
        }

        LocalDateTime planPublishTime = dto.getPlanPublishTime();
        if (BeanUtil.isNotEmpty(planPublishTime)) {
            updateChainWrapper.set(ProjectVersion::getPlanPublishTime,planPublishTime);
            versionLog.setType(ProjectVersionLogTypeEnum.SET_PLAN_PUBLISH_TIME.getCode())
                    .setRemark(String.format(ProjectVersionLogTypeEnum.SET_PLAN_PUBLISH_TIME.getName(), DateUtil.format(planPublishTime, DatePattern.NORM_DATETIME_PATTERN)));
        updateMark=true;
        }
        //执行了其他修改 则不会进清除时间
        String timeType = dto.getTimeType();
        if (!updateMark) {
            boolean start = TIME_START.equals(timeType);
            if (BeanUtil.isEmpty(startTime) && BeanUtil.isNotEmpty(version.getStartTime()) && start) {
                updateChainWrapper.set(ProjectVersion::getStartTime,null);
                versionLog.setType(ProjectVersionLogTypeEnum.CLEAR_START_TIME.getCode())
                        .setRemark(ProjectVersionLogTypeEnum.CLEAR_START_TIME.getName());
            }
            boolean plan = TIME_PLAN.equals(timeType);
            if (BeanUtil.isEmpty(planPublishTime) && BeanUtil.isNotEmpty(version.getPlanPublishTime()) && plan) {
                updateChainWrapper.set(ProjectVersion::getPlanPublishTime,null);
                versionLog.setType(ProjectVersionLogTypeEnum.CLEAR_PLAN_PUBLISH_TIME.getCode())
                        .setRemark(ProjectVersionLogTypeEnum.CLEAR_PLAN_PUBLISH_TIME.getName());
            }
        }

        updateChainWrapper.update();
        versionLog.initSave(currentUser);
        projectVersionLogService.save(versionLog);
        return getById(versionId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean changeStatus(ProjectVersion dto, UserDto currentUser) {
        Integer status = dto.getStatus();
        String vid = dto.getId();
        if (BeanUtil.isEmpty(status)) {
            throw new BusinessException("请选择状态！");
        }
        Boolean flag;
        ProjectVersion version = getById(vid);
        ProjectFeatures features = projectFeaturesService.getById(version.getFeaturesId());
        String statusText = ProjectVersionStatusConst.transNumToStr(status);
        ProjectVersionLog SLog = new ProjectVersionLog();
        SLog.setProjectId(features.getProjectId())
                .setSourceId(vid)
                .setType(ProjectVersionLogTypeEnum.STATUS.getCode())
                .setRemark(String.format(ProjectVersionLogTypeEnum.STATUS.getName(), statusText))
                .setMemberId(currentUser.getId())
                .setFeaturesId(features.getId());
        SLog.initSave(currentUser);
        ProjectVersion upVersion = new ProjectVersion().setId(vid);
        upVersion.setStatus(status);
        if (status == ProjectVersionStatusConst.RELEASE) {
            LocalDateTime publishTime = dto.getPublishTime();
            if (BeanUtil.isEmpty(publishTime)) {
                throw new BusinessException("修改发布状态请填写实际发布时间！");
            }
            SLog.setType(ProjectVersionLogTypeEnum.COMPLETE.getCode())
                    .setRemark(String.format(ProjectVersionLogTypeEnum.COMPLETE.getName(), DateUtil.format(publishTime, DatePattern.NORM_DATETIME_PATTERN)));
        }
        flag = updateById(upVersion);
        projectVersionLogService.save(SLog);
        return flag;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean removeProjectVersion(ProjectVersion dto, UserDto currentUser) {
        //删除版本 并更新任务的版本
        String id = dto.getId();
        ProjectVersion version = getById(id);
        ProjectFeatures features = projectFeaturesService.getById(version.getFeaturesId());
        boolean remove = removeById(id);
        boolean update = projectTaskService.lambdaUpdate().eq(ProjectTask::getVersionId, id)
                .set(ProjectTask::getFeaturesId, null)
                .set(ProjectTask::getVersionId, null).update();
        //记录动态
        ProjectVersionLog versionLog = new ProjectVersionLog()
                .setMemberId(currentUser.getId())
                .setType(ProjectVersionLogTypeEnum.REMOVE_VERSION.getCode())
                .setRemark(String.format(ProjectVersionLogTypeEnum.REMOVE_VERSION.getName(), version.getName()))
                .setProjectId(features.getProjectId())
                .setSourceId(id);
        versionLog.initSave(currentUser);
        projectVersionLogService.save(versionLog);
        return remove;
    }
}
