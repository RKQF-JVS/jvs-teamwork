package cn.bctools.teamwork.controller;

import cn.bctools.auth.api.api.AuthUserServiceApi;
import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.R;
import cn.bctools.log.annotation.Log;
import cn.bctools.oauth2.utils.UserCurrentUtils;
import cn.bctools.oss.dto.BaseFile;
import cn.bctools.oss.template.OssTemplate;
import cn.bctools.teamwork.common.common.config.CommonConfig;
import cn.bctools.teamwork.common.consts.CommonConst;
import cn.bctools.teamwork.common.enums.FileRelationTypeEnum;
import cn.bctools.teamwork.common.util.ExcelUtils;
import cn.bctools.teamwork.dto.*;
import cn.bctools.teamwork.dto.req.ProjectTaskReq;
import cn.bctools.teamwork.dto.req.TaskDateCountReq;
import cn.bctools.teamwork.entity.*;
import cn.bctools.teamwork.service.ProjectTaskMemberService;
import cn.bctools.teamwork.service.ProjectTaskService;
import cn.bctools.teamwork.vo.ProjectTaskCountForDateVO;
import cn.bctools.teamwork.vo.ProjectTaskDetailVO;
import cn.bctools.teamwork.vo.ProjectTaskTreeMouldVO;
import cn.bctools.teamwork.vo.ProjectTaskTreeVO;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 任务的基本信息
 * <p>
 * FIXME 需要注意，该类为自动生成，需结合真实业务进行修改
 *
 * @author Auto Generator
 */
@Api(tags = "任务的基本信息")
@RestController
@AllArgsConstructor
@RequestMapping("/ProjectTask")
public class ProjectTaskController {

    ProjectTaskService service;
    AuthUserServiceApi userServiceApi;
    OssTemplate ossTemplate;
    CommonConfig commonConfig;
    ProjectTaskMemberService projectTaskMemberService;


    /**
     * 验证是否能查看任务详情
     * @param taskId
     * @param currentUser
     */
    public void verifyLookAuth(String taskId, UserDto currentUser) {
        ProjectTask projectTask = service.getById(taskId);
        boolean empty = BeanUtil.isEmpty(projectTask);
        if (empty) {
            return;
        }
        //若开启了隐私模式 则匹配参与者
        if (projectTask.getIsPrivate() == 1) {
            ProjectTaskMember one = projectTaskMemberService.lambdaQuery().eq(ProjectTaskMember::getTaskId, taskId).eq(ProjectTaskMember::getMemberId, currentUser.getId()).one();
            if (BeanUtil.isEmpty(one)) {
                throw new BusinessException("该任务设置了隐私模式，你不是该任务的参与者！");
            }

        }
    }

    @Log
    @ApiOperation("详情")
    @GetMapping("/detail")
    public R<ProjectTaskDetailVO> detail(@RequestParam String taskId) {
        UserDto user = UserCurrentUtils.getCurrentUser();
        verifyLookAuth(taskId, user);
        ProjectTaskDetailVO detail = service.detail(taskId, user);
        List<ProjectTask> tasks = detail.getTasks();
        //设置执行者信息
        if (!tasks.isEmpty()) {
            List<String> projectIds = tasks.stream().filter(e -> StrUtil.isNotEmpty(e.getExecuteMemberId())).map(ProjectTask::getExecuteMemberId).collect(Collectors.toList());
            if (!projectIds.isEmpty()) {
                Map<String, UserDto> userDtoMap = userServiceApi.getByIds(projectIds).getData().stream().collect(Collectors.toMap(UserDto::getId, Function.identity()));
                detail.getTasks().stream().peek(e -> e.setExecutor(userDtoMap.get(e.getExecuteMemberId()))).collect(Collectors.toList());
            }
            //告诉前端下面是否有子任务没有完成
            List<String> ids = tasks.stream().map(ProjectTask::getId).collect(Collectors.toList());
            Map<String, List<ProjectTask>> map = service.list(new LambdaQueryWrapper<ProjectTask>().in(ProjectTask::getPid, ids)).stream().collect(Collectors.groupingBy(ProjectTask::getId, Collectors.toList()));
            detail.getTasks().stream().peek(e -> {
                e.setChildrenStatus(Boolean.TRUE);
                if (map.containsKey(e.getId())) {
                    long count = map.get(e.getId()).stream().filter(v -> BigDecimal.ROUND_UP == v.getFinishStatus()).count();
                    e.setChildrenStatus(count == 0);
                }
            }).collect(Collectors.toList());
        }
        if (StrUtil.isNotEmpty(detail.getExecuteMemberId())) {
            UserDto executor = userServiceApi.getById(detail.getExecuteMemberId()).getData();
            detail.setExecutor(executor);
        }
        return R.ok(detail);
    }

    @Log
    @ApiOperation("新增")
    @PostMapping("/save")
    public R<ProjectTask> save(@RequestBody ProjectTaskAddDTO projectTaskAddDTO) {
        //获取成员信息
        UserDto user = UserCurrentUtils.getCurrentUser();
        if (StrUtil.isNotEmpty(projectTaskAddDTO.getExecuteMemberId())) {
            //获取执行者
            projectTaskAddDTO.setExecuteMember(userServiceApi.getById(projectTaskAddDTO.getExecuteMemberId()).getData());
        }
        return R.ok(service.saveTask(projectTaskAddDTO, user, true));
    }

    @Log
    @ApiOperation("任务修改")
    @PutMapping("/edit")
    public R<ProjectTask> edit(@RequestBody ProjectTaskEditDTO dto) {
        UserDto user = UserCurrentUtils.getCurrentUser();
        return R.ok(service.editTask(dto, user));
    }

    @Log
    @ApiOperation("任务设置标签")
    @PostMapping("/setTag")
    public R<ProjectTaskToTag> setTag(@RequestBody ProjectTaskToTag dto) {
        if (StrUtil.isEmpty(dto.getTaskId()) || StrUtil.isEmpty(dto.getTagId())) {
            throw new BusinessException("数据有误！");
        }
        UserDto user = UserCurrentUtils.getCurrentUser();
        return R.ok(service.setTag(dto, user));
    }

    @Log
    @ApiOperation("任务详情 添加工时记录")
    @PostMapping("/saveTaskWorkTime")
    public R<ProjectTaskWorkTimeRecord> saveTaskWorkTime(@RequestBody ProjectTaskWorkTimeRecord dto) {
        if (StrUtil.isEmpty(dto.getTaskId()) || StrUtil.isEmpty(dto.getStartTime()) || BeanUtil.isEmpty(dto.getTimeConsuming())) {
            throw new BusinessException("数据有误！");
        }
        UserDto user = UserCurrentUtils.getCurrentUser();
        return R.ok(service.saveTaskWorkTime(dto, user));
    }

    @Log
    @ApiOperation("任务详情 编辑工时记录")
    @PutMapping("/editTaskWorkTime")
    public R<ProjectTaskWorkTimeRecord> editTaskWorkTime(@RequestBody ProjectTaskWorkTimeRecord dto) {
        if (StrUtil.isEmpty(dto.getId()) || StrUtil.isEmpty(dto.getStartTime()) || BeanUtil.isEmpty(dto.getTimeConsuming())) {
            throw new BusinessException("数据有误！");
        }
        UserDto user = UserCurrentUtils.getCurrentUser();
        return R.ok(service.editTaskWorkTime(dto, user));
    }

    @Log
    @ApiOperation("任务设置隐私模式 传递主键id")
    @PutMapping("/setPrivate")
    public R<Boolean> setPrivate(@RequestBody ProjectTask dto) {
        if (StrUtil.isEmpty(dto.getId())) {
            throw new BusinessException("请选择任务！");
        }
        UserDto user = UserCurrentUtils.getCurrentUser();
        return R.ok(service.setPrivate(dto, user));
    }


    @Log
    @ApiOperation("查询子任务")
    @GetMapping("/listChildTask")
    public R<List<ProjectTask>> listChildTask(@RequestParam String taskPId) {
        if (StrUtil.isEmpty(taskPId)) {
            throw new BusinessException("请选择任务！");
        }
        List<ProjectTask> projectTasks = service.listChildTask(taskPId);
        fillTaskExecutor(projectTasks);
        return R.ok(projectTasks);
    }

    private <T extends ProjectTask> List<T> fillTaskExecutor(List<T> list) {
        List<String> exeIds = list.stream().filter(x -> StrUtil.isNotEmpty(x.getExecuteMemberId())).map(ProjectTask::getExecuteMemberId).collect(Collectors.toList());
        if (CollUtil.isNotEmpty(exeIds)) {
            List<UserDto> exes = userServiceApi.getByIds(exeIds).getData();
            exes.forEach(e -> {
                for (ProjectTask task : list) {
                    String memberId = task.getExecuteMemberId();
                    if (StrUtil.isNotEmpty(memberId) && memberId.equals(e.getId())) {
                        task.setExecutor(e);
                        return;
                    }
                }
            });
        }
        return list;
    }

    @Log
    @ApiOperation("任务详情 指派任务执行者")
    @PostMapping("/assignExecutor")
    public R<Boolean> assignExecutor(@RequestBody ProjectTaskAssignExecutorDTO dto) {
        if (StrUtil.isEmpty(dto.getTaskId())) {
            throw new BusinessException("请选择任务！");
        }
        UserDto user = UserCurrentUtils.getCurrentUser();
        if (StrUtil.isNotEmpty(dto.getExecutorId())) {
            UserDto userDto = userServiceApi.getById(dto.getExecutorId()).getData();
            dto.setExecutorInfo(userDto);
        }
        return R.ok(service.assignExecutor(dto, user));
    }

    @Log
    @ApiOperation("任务列表 本列表批量指派任务执行者")
    @PostMapping("/assignExecutorBatch")
    public R<Boolean> assignExecutorBatch(@RequestBody ProjectTaskAssignExecutorBatchDTO dto) {
        if (CollUtil.isEmpty(dto.getTaskIds())) {
            return R.ok(false, "未选择任务！");
        }
        if (StrUtil.isEmpty(dto.getExecutorId())) {
            throw new BusinessException("请选择执行者！");
        }
        UserDto user = UserCurrentUtils.getCurrentUser();
        if (StrUtil.isNotEmpty(dto.getExecutorId())) {
            UserDto userDto = userServiceApi.getById(dto.getExecutorId()).getData();
            dto.setExecutorInfo(userDto);
        }
        return R.ok(service.assignExecutorBatch(dto, user));
    }

    @Log
    @ApiOperation("任务列表 本列表批量删除任务")
    @DeleteMapping("/taskRecycleBatch")
    public R<Boolean> taskRecycleBatch(@RequestParam String mouldId) {
        if (StrUtil.isEmpty(mouldId)) {
            throw new BusinessException("请选择任务列表！");
        }
        UserDto user = UserCurrentUtils.getCurrentUser();
        return R.ok(service.taskRecycleBatch(mouldId, user));
    }

    @Log
    @ApiOperation("任务详情 任务完成状态修改")
    @PostMapping("/taskDone")
    public R<Boolean> taskDone(@RequestBody ProjectTaskDoneDTO dto) {
        if (StrUtil.isEmpty(dto.getTaskId())) {
            throw new BusinessException("请选择任务！");
        }
        UserDto user = UserCurrentUtils.getCurrentUser();

        return R.ok(service.taskDone(dto, user));
    }

    @Log
    @ApiOperation("任务详情 关联文件")
    @PostMapping("/uploadRelationFile")
    public R<ProjectFileInfo> taskRelationFile(@RequestBody ProjectTaskFileDTO dto) {
        if (StrUtil.isEmpty(dto.getId())) {
            throw new BusinessException("请选择任务！");
        }
        ProjectTask projectTask = service.getById(dto.getId());
        if (BeanUtil.isEmpty(projectTask)) {
            throw new BusinessException("任务已失效！");
        }
        if (CommonConst.DELETE_STATUS.equals(projectTask.getRecycleFlag())) {
            throw new BusinessException("任务在回收站，无法编辑！");
        }

        BaseFile baseFile = dto.getBaseFile();
        ProjectFileInfo fileInfo = new ProjectFileInfo();
        fileInfo.setFileName(baseFile.getFileName())
                .setFileType(baseFile.getFileType())
                .setTaskId(projectTask.getId())
                .setProjectId(projectTask.getProjectId())
                .setOriginName(baseFile.getOriginalName())
                .setExtension(baseFile.getFileType())
                .setSize(baseFile.getSize())
                .setObjectType(FileRelationTypeEnum.TASK_TYPE.getCode());
        UserDto user = UserCurrentUtils.getCurrentUser();
        return R.ok(service.taskRelationFile(fileInfo, projectTask, user));
    }

    @Log
    @ApiOperation("任务详情 取消关联文件")
    @PostMapping("/cancelRelationFile")
    public R<Boolean> cancelRelationFile(@RequestBody ProjectFileInfo fileInfo) {

        UserDto currentUser = UserCurrentUtils.getCurrentUser();
        Boolean result = service.cancelRelationFile(fileInfo, currentUser);
        return R.ok(result);
    }

    @Log
    @ApiOperation("任务详情 关联文件列表")
    @GetMapping("/listTaskFile")
    public R<List<ProjectFileInfo>> listTaskFile(String taskId) {
        if (StrUtil.isEmpty(taskId)) {
            throw new BusinessException("请选择任务！");
        }
        List<ProjectFileInfo> fileInfos = service.listTaskFile(taskId);
        for (ProjectFileInfo fileInfo : fileInfos) {
            fileInfo.setFileUrl(ossTemplate.fileLink(fileInfo.getFileName(), commonConfig.getBucketName()));
        }
        return R.ok(fileInfos);
    }


    @Log
    @ApiOperation("任务详情 任务评论")
    @PostMapping("/taskComment")
    public R<ProjectTaskCommentDTO> taskComment(@RequestBody ProjectTaskCommentDTO dto) {
        if (StrUtil.isEmpty(dto.getTaskId())) {
            throw new BusinessException("请选择任务！");
        }
        UserDto user = UserCurrentUtils.getCurrentUser();
        return R.ok(service.taskComment(dto, user));
    }

    @Log
    @ApiOperation("任务收藏")
    @PostMapping("/taskCollect")
    public R<Boolean> taskCollect(@RequestBody TaskCollectDTO dto) {
        if (StrUtil.isEmpty(dto.getTaskId()) || BeanUtil.isEmpty(dto.getCollect())) {
            throw new BusinessException("请选择任务和操作！");
        }
        UserDto user = UserCurrentUtils.getCurrentUser();
        return R.ok(service.taskCollect(dto, user));
    }

    @Log
    @ApiOperation("任务点赞")
    @PostMapping("/taskLike")
    public R<Boolean> taskLike(@RequestBody TaskLikeDTO dto) {
        if (StrUtil.isEmpty(dto.getTaskId()) || BeanUtil.isEmpty(dto.getLike())) {
            throw new BusinessException("请选择任务和操作！");
        }
        UserDto user = UserCurrentUtils.getCurrentUser();
        return R.ok(service.taskLike(dto, user));
    }

    @Log
    @ApiOperation("任务移到回收站")
    @DeleteMapping("/taskRecycle")
    public R<Boolean> taskRecycle(@RequestParam String taskId) {
        if (StrUtil.isEmpty(taskId)) {
            throw new BusinessException("请选择任务！");
        }
        UserDto user = UserCurrentUtils.getCurrentUser();
        return R.ok(service.taskRecycle(taskId, user));
    }

    @Log
    @ApiOperation("查询回收站的任务 分页查询")
    @GetMapping("/listRecycle")
    public R<Page<ProjectTask>> listRecycle(Page<ProjectTask> page, String projectId) {
        if (StrUtil.isEmpty(projectId)) {
            throw new BusinessException("请选择项目！");
        }
        return R.ok(service.listRecycle(page, projectId));
    }

    @Log
    @ApiOperation("恢复任务")
    @PutMapping("/taskRecovery")
    public R<Boolean> taskRecovery(String taskId) {
        if (StrUtil.isEmpty(taskId)) {
            throw new BusinessException("请选择任务！");
        }
        UserDto user = UserCurrentUtils.getCurrentUser();
        return R.ok(service.taskRecovery(taskId, user));
    }

    @Log
    @ApiOperation("任务彻底删除")
    @DeleteMapping("/del")
    public R<Boolean> del(@RequestParam String taskId) {
        if (StrUtil.isEmpty(taskId)) {
            throw new BusinessException("请选择任务！");
        }
        UserDto user = UserCurrentUtils.getCurrentUser();
        return R.ok(service.del(taskId, user));
    }

    @Log
    @ApiOperation("任务排序 改变")
    @PostMapping("/sort")
    public R<Boolean> sort(@RequestBody ProjectTaskSortDTO dto) {
        if (StrUtil.isEmpty(dto.getTaskMouldId())) {
            throw new BusinessException("请指明任务移动的的任务列表！");
        }
        UserDto user = UserCurrentUtils.getCurrentUser();
        return R.ok(service.sort(dto, user));
    }


    @Log
    @ApiOperation("根据标签查询该标签的任务信息")
    @PostMapping("/getListByTag")
    public R<List<ProjectTaskDetailVO>> getListByTag(@RequestBody ProjectTaskTag dto) {
        if (StrUtil.isEmpty(dto.getId())) {
            throw new BusinessException("请选择标签！");
        }
        UserDto user = UserCurrentUtils.getCurrentUser();
        List<ProjectTaskDetailVO> list = service.getListByTag(dto, user);
        fillTaskExecutor(list);
        return R.ok(list);
    }


    /**
     * 填充每个的执行者信息
     *
     * @param tasks
     */
    private void fillExecutorSelfAndChild(List<ProjectTaskTreeVO> tasks) {
        if (CollUtil.isNotEmpty(tasks)) {
            fillTaskExecutor(tasks);
            tasks.forEach(x -> {
                //填充子任务的
                if (CollUtil.isNotEmpty(x.getChildTask())) {
                    fillExecutorSelfAndChild(x.getChildTask());
                }
            });
        }
    }


    @Log
    @ApiOperation("任务模板批量导入任务")
    @PostMapping("/importTask")
    public R<Boolean> importTask(@RequestParam("projectId") String projectId, @RequestParam("file") MultipartFile file) {
        List<List<Object>> importList = ExcelUtils.getListInfo(file);
        UserDto login = UserCurrentUtils.getCurrentUser();
        if (CollUtil.isNotEmpty(importList)) {
            Function<List<Object>, ProjectTask> function = obj -> {
                String memBerName = (String) obj.get(3);
                ProjectTask task = new ProjectTask();
                task.setProjectId(projectId)
                        .setName((String) obj.get(0))
                        .setPName((String) obj.get(1))
                        .setMouldId((String) obj.get(2))
                        .setStartTimeStr((String) obj.get(4))
                        .setEndTimeStr((String) obj.get(5))
                        .setRemark((String) obj.get(6))
                        .setPriorityLevel((String) obj.get(7))
                        .setTaskTag((String) obj.get(8));
                if (StrUtil.isNotEmpty(memBerName)) {
                    List<UserDto> users = userServiceApi.getByRealName(memBerName).getData();
                    task.setImportMember(users);
                }
                return task;
            };
            List<ProjectTask> collect = importList.parallelStream().map(function).collect(Collectors.toList());
            service.saveImportList(collect, login, projectId);
            return R.ok(true);
        } else {
            return R.failed(false, "文件格式错误或内容为空");
        }
    }

    @Log
    @ApiOperation("任务导入模板下载 返回文件外链")
    @PostMapping("/downTaskTemplate")
    public R<String> downTaskTemplate() {
        String link = ossTemplate.fileLink(commonConfig.getTaskImportTemplate(), commonConfig.getBucketName());
        return R.ok(link);
    }

    @Log
    @ApiOperation("任务复制")
    @PostMapping("/taskCopy")
    public R<ProjectTask> taskCopy(@RequestBody TaskCopyDTO dto) {
        if (StrUtil.isEmpty(dto.getMouldId()) || StrUtil.isEmpty(dto.getProjectId()) || StrUtil.isEmpty(dto.getTaskId()) || StrUtil.isEmpty(dto.getName())) {
            throw new BusinessException("参数错误！");
        }
        UserDto currentUser = UserCurrentUtils.getCurrentUser();
        if (BeanUtil.isNotEmpty(dto.getCopyExecutor()) && dto.getCopyExecutor() == 1) {
            ProjectTask task = service.getById(dto.getTaskId());
            if (BeanUtil.isNotEmpty(task) && CommonConst.DELETE_STATUS.equals(task.getRecycleFlag())) {
                throw new BusinessException("任务已失效！");
            }
            String executeMemberId = task.getExecuteMemberId();
            if (StrUtil.isNotEmpty(executeMemberId)) {
                UserDto data = userServiceApi.getById(executeMemberId).getData();
                dto.setExecuteMember(data);
            }
        }
        return R.ok(service.taskCopy(dto, currentUser));
    }

    @Log
    @ApiOperation("任务移动")
    @PostMapping("/taskMove")
    public R<ProjectTask> taskMove(@RequestBody TaskMoveDTO dto) {
        if (StrUtil.isEmpty(dto.getMouldId()) || StrUtil.isEmpty(dto.getProjectId()) || StrUtil.isEmpty(dto.getTaskId())) {
            throw new BusinessException("请求参数错误！");
        }
        UserDto currentUser = UserCurrentUtils.getCurrentUser();
        return R.ok(service.taskMove(dto, currentUser));
    }

    @Log
    @ApiOperation("任务趋势图")
    @PostMapping("/taskDateCount")
    public R<ProjectTaskCountForDateVO> taskDateCount(@RequestBody TaskDateCountReq dto) {
        if (StrUtil.isNotEmpty(dto.getEndTime()) && StrUtil.isNotEmpty(dto.getBeginTime())) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate star = LocalDate.parse(dto.getBeginTime(), formatter);
            LocalDate end = LocalDate.parse(dto.getEndTime(), formatter);
            if (end.isBefore(star)) {
                throw new BusinessException("结束时间不能比开始时间小");
            }
        }
        return R.ok(service.taskDateCount(dto));
    }

    @Log
    @ApiOperation("项目id必传，项目任务状态 {键-任务状态:值-任务数量} done-已完成 unDone-未完成 overdue-已逾期  doneOverdue-逾期完成 expireToday-今日到期 toBeAssign-待认领 total-总共任务")
    @PostMapping("/getProjectStatus")
    public R<Map<String, Integer>> getProjectStatus(@RequestBody ProjectTask dto) {
        if (StrUtil.isEmpty(dto.getProjectId())) {
            throw new BusinessException("请选择项目！");
        }
        return R.ok(service.getProjectStatus(dto));
    }


    @Log
    @ApiOperation("甘特图数据")
    @PostMapping("/ganttTask")
    public R<List<ProjectTaskTreeMouldVO>> ganttTask(@RequestBody ProjectTaskReq req) {
        //todo 暂时查询任务的表格视图
        String projectId = req.getProjectId();
        if (StrUtil.isEmpty(projectId)) {
            throw new BusinessException("请选择项目！");
        }
        UserDto user = UserCurrentUtils.getCurrentUser();
        List<ProjectTaskTreeMouldVO> vos = service.taskTree(req, user);
        //填充每个任务的执行者
        if (CollUtil.isNotEmpty(vos)) {
            vos.forEach(x -> {
                List<ProjectTaskTreeVO> tasks = x.getTasks();
                fillExecutorSelfAndChild(tasks);
            });
        }
        return R.ok(vos);
    }


}
