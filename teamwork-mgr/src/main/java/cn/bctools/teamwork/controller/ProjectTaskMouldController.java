package cn.bctools.teamwork.controller;

import cn.bctools.auth.api.api.AuthUserServiceApi;
import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.R;
import cn.bctools.log.annotation.Log;
import cn.bctools.oauth2.utils.UserCurrentUtils;
import cn.bctools.teamwork.common.consts.CommonConst;
import cn.bctools.teamwork.common.enums.CollectionTypeEnum;
import cn.bctools.teamwork.dto.req.ProjectTaskReq;
import cn.bctools.teamwork.entity.*;
import cn.bctools.teamwork.service.*;
import cn.bctools.teamwork.vo.ProjectTaskDetailVO;
import cn.bctools.teamwork.vo.ProjectTaskListVo;
import cn.bctools.teamwork.vo.ProjectTaskMouldListVo;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.ZoneOffset;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 任务列表 表
 * <p>
 * FIXME 需要注意，该类为自动生成，需结合真实业务进行修改
 *
 * @author Auto Generator
 */
@Api(tags = "任务列表 表")
@RestController
@AllArgsConstructor
@RequestMapping("/ProjectTaskMould")
public class ProjectTaskMouldController {
    /**
     * 展示方式默认类型
     */
    private final static Integer DEFAULT_TYPE = 1;
    /**
     * 返回给前端的key 完成
     */
    private final static String FINISH_TASK = "finish_task";
    /**
     * 返回给前端的key 未完成
     */
    private final static String UNFINISHED_TASK = "unfinished_task";
    private final ProjectTaskMouldService service;
    private final ProjectTaskService projectTaskService;
    private final AuthUserServiceApi userServiceApi;
    private final ProjectTaskMemberService projectTaskMemberService;
    private final ProjectTaskTagService projectTaskTagService;
    private final ProjectTaskToTagService projectTaskToTagService;
    private final ProjectTaskLikedService projectTaskLikedService;
    private final ProjectCollectionService projectCollectionService;

    @Log
    @ApiOperation("查询任务列表模板 列表")
    @PostMapping("/listMould")
    public R<List<ProjectTaskMouldListVo>> page(@RequestBody ProjectTaskReq req) {
        //获取任务列表数据
        List<ProjectTaskMould> list = service.lambdaQuery()
                .eq(ProjectTaskMould::getProjectId, req.getProjectId())
                .list();
        if (!list.isEmpty()) {
            List<ProjectTaskListVo> listVos;
            //不同的展示方式 调用不同的方法
            if (DEFAULT_TYPE.equals(req.getType())) {
                listVos = this.regroupDataTaskDefault(list, req);
            } else {
                listVos = this.regroupDataTaskList(list, req);
            }
            //重组模型数据
            List<ProjectTaskMouldListVo> collect = list.parallelStream().map(e -> new ProjectTaskMouldListVo().setSort(e.getSort()).setProjectId(e.getProjectId()).setId(e.getId()).setProjectMouldName(e.getName())).sorted(Comparator.comparing(ProjectTaskMouldListVo::getSort)).collect(Collectors.toList());

            List<ProjectTaskListVo> listVosCopy = listVos.stream().sorted(Comparator.comparing(ProjectTaskListVo::getSort)).collect(Collectors.toList());
            //模型关联任务
            collect = collect.parallelStream().peek(e -> {
                List<ProjectTaskListVo> listVos1 = listVosCopy.parallelStream().filter(v -> v.getProjectTaskMouldId().equals(e.getId())).collect(Collectors.toList());

                if (DEFAULT_TYPE.equals(req.getType())) {
                    HashMap<String, List<ProjectTaskListVo>> map = new HashMap<>(2);
                    //完成的任务
                    List<ProjectTaskListVo> finishTask = listVos1.stream().filter(v -> DEFAULT_TYPE.equals(v.getFinishStatus())).collect(Collectors.toList());
                    //未完成 的任务
                    List<ProjectTaskListVo> unfinishedTask = listVos1.stream().filter(v -> !DEFAULT_TYPE.equals(v.getFinishStatus())).collect(Collectors.toList());
                    map.put(FINISH_TASK, finishTask);
                    map.put(UNFINISHED_TASK, unfinishedTask);
                    e.setChildDefaultList(map);
                }
                e.setChildList(listVos1);
            }).sorted(Comparator.comparing(ProjectTaskMouldListVo::getSort)).collect(Collectors.toList());

            return R.ok(collect);
        }
        return R.ok(new ArrayList<>());
    }

    /***
     * 功能描述: <br>
     * 〈重组任务数据 用于任务列表 看板展示方式〉
     * @Param: []
     * @Return: java.util.List<cn.bctools.teamwork.vo.ProjectTaskListVo>
     * @Author:
     * @Date: 2021/12/27 15:15
     */
    private List<ProjectTaskListVo> regroupDataTaskList(List<ProjectTaskMould> list, ProjectTaskReq req) {
        //获取所有任务
        List<String> ids = list.stream().map(ProjectTaskMould::getId).collect(Collectors.toList());

        LambdaQueryWrapper<ProjectTask> wrapper = new LambdaQueryWrapper<ProjectTask>()
                .in(ProjectTask::getMouldId, ids)
                .eq(BeanUtil.isNotEmpty(req.getFinishStatus()), ProjectTask::getFinishStatus, req.getFinishStatus())
                .like(StrUtil.isNotEmpty(req.getName()), ProjectTask::getName, req.getName())
                .eq(ProjectTask::getRecycleFlag, CommonConst.NO_DELETE_STATUS);
        wrapper = projectTaskService.fillTaskTreeWrapper(wrapper, req);
        if (BeanUtil.isEmpty(wrapper)) {
            return new ArrayList<>();
        }

        List<ProjectTask> projectTasks = projectTaskService.list(wrapper);
        if (!projectTasks.isEmpty()) {
            //用户数据
            Map<String, UserDto> userMap;
            //任务id
            List<String> projectTaskIds = projectTasks.stream().map(ProjectTask::getId).collect(Collectors.toList());
            //获取任务的执行人
            List<ProjectTaskMember> projectTaskMemberList = projectTaskMemberService.list(new LambdaQueryWrapper<ProjectTaskMember>().in(ProjectTaskMember::getTaskId, projectTaskIds).eq(ProjectTaskMember::getIsExecutor, BigDecimal.ROUND_DOWN));
            userMap = this.getExecutorsByIds(projectTaskMemberList);
            //获取数据
            List<ProjectTaskListVo> listVos = service.regroupDataTaskList("-1", userMap, projectTasks, projectTaskMemberList);
            return listVos;
        }
        return new ArrayList<>();
    }

    /**
     * 根据任务成员执行者集合，查询信息并转为id为key的map集合
     *
     * @param projectTaskMemberList
     * @return
     */
    private Map<String, UserDto> getExecutorsByIds(List<ProjectTaskMember> projectTaskMemberList) {
        if (!projectTaskMemberList.isEmpty()) {
            List<String> userIds = projectTaskMemberList.stream().map(ProjectTaskMember::getMemberId).collect(Collectors.toList());
            //获取用户数据
            return userServiceApi.getByIds(userIds).getData()
                    .stream()
                    .collect(Collectors.toMap(UserDto::getId, Function.identity()));
        }
        return null;
    }

    /***
     * 功能描述: <br>
     * 〈重组任务数据 用于任务列表 默认展示方式〉
     * @Param: []
     * @Return: java.util.List<cn.bctools.teamwork.vo.ProjectTaskListVo>
     * @Author:
     * @Date: 2021/12/27 15:15
     */
    private List<ProjectTaskListVo> regroupDataTaskDefault(List<ProjectTaskMould> list, ProjectTaskReq req) {
        //获取任务 只会获取顶级任务
        List<String> ids = list.stream().map(ProjectTaskMould::getId).collect(Collectors.toList());
        LambdaQueryWrapper<ProjectTask> wrapper = new LambdaQueryWrapper<ProjectTask>()
                .in(ProjectTask::getMouldId, ids)
                .eq(ProjectTask::getPid, -1)
                .eq(BeanUtil.isNotEmpty(req.getFinishStatus()), ProjectTask::getFinishStatus, req.getFinishStatus())
                .like(StrUtil.isNotEmpty(req.getName()), ProjectTask::getName, req.getName())
                .eq(ProjectTask::getRecycleFlag, CommonConst.NO_DELETE_STATUS);
        wrapper = projectTaskService.fillTaskTreeWrapper(wrapper, req);
        if (BeanUtil.isEmpty(wrapper)) {
            return new ArrayList<>();
        }
        List<ProjectTask> projectTasks = projectTaskService.list(wrapper);
        if (!projectTasks.isEmpty()) {
            UserDto currentUser = UserCurrentUtils.getCurrentUser();
            //用户数据
            Map<String, UserDto> userMap = new HashMap<>(1);
            //标签
            List<ProjectTaskTag> projectTaskTagList = new ArrayList<>();
            //任务id
            List<String> projectTaskIds = projectTasks.stream().map(ProjectTask::getId).collect(Collectors.toList());
            //其中我点赞的任务
            List<ProjectTaskLiked> likeds = projectTaskLikedService.list(new LambdaQueryWrapper<ProjectTaskLiked>().in(ProjectTaskLiked::getTaskId, projectTaskIds)
                    .eq(ProjectTaskLiked::getMemberId, currentUser.getId()));
            Map<String, ProjectTaskLiked> likedMap = new HashMap<>(1);
            if (CollUtil.isNotEmpty(likeds)) {
                likedMap = likeds.stream().collect(Collectors.toMap(ProjectTaskLiked::getTaskId, Function.identity()));
            }
            Map<String, ProjectTaskLiked> taskLikedMap = likedMap;
            //其中我收藏的任务
            List<ProjectCollection> collections = projectCollectionService.list(new LambdaQueryWrapper<ProjectCollection>().eq(ProjectCollection::getType, CollectionTypeEnum.TASK.getCode()).in(ProjectCollection::getObjectId, projectTaskIds)
                    .eq(ProjectCollection::getMemberId, currentUser.getId()));

            Map<String, ProjectCollection> collectionMap = new HashMap<>(1);
            if (CollUtil.isNotEmpty(collections)) {
                collectionMap = collections.stream().collect(Collectors.toMap(ProjectCollection::getObjectId, Function.identity()));
            }
            Map<String, ProjectCollection> collMap = collectionMap;
            //获取二级任务
            List<ProjectTask> secondLevelTaskList = projectTaskService.list(new LambdaQueryWrapper<ProjectTask>().in(ProjectTask::getPid, projectTaskIds));
            //获取任务的执行人
            List<ProjectTaskMember> projectTaskMemberList = projectTaskMemberService.list(new LambdaQueryWrapper<ProjectTaskMember>().in(ProjectTaskMember::getTaskId, projectTaskIds).eq(ProjectTaskMember::getIsExecutor, BigDecimal.ROUND_DOWN));
            userMap = getExecutorsByIds(projectTaskMemberList);
            //获取任务标签
            List<ProjectTaskToTag> projectTaskTags = projectTaskToTagService.list(new LambdaQueryWrapper<ProjectTaskToTag>().in(ProjectTaskToTag::getTaskId, projectTaskIds));
            if (!projectTaskTags.isEmpty()) {
                //获取标签信息
                List<String> projectTaskTagIds = projectTaskTags.stream().map(ProjectTaskToTag::getTagId).collect(Collectors.toList());
                projectTaskTagList = projectTaskTagService.list(new LambdaQueryWrapper<ProjectTaskTag>().in(ProjectTaskTag::getId, projectTaskTagIds));
            }
            //重组任务数据
            Map<String, UserDto> userMapCopy = userMap;
            List<ProjectTaskTag> projectTaskTagListCopy = projectTaskTagList;
            Function<ProjectTask, ProjectTaskListVo> function = e -> {
                //用户数据
                ProjectTaskMember taskMember = projectTaskMemberList.stream().filter(v -> e.getId().equals(v.getTaskId())).findFirst().orElseGet(ProjectTaskMember::new);
                UserDto userDto = userMapCopy.get(taskMember.getMemberId());
                //获取标签
                List<String> tagIds = projectTaskTags.stream().filter(v -> v.getTaskId().equals(e.getId())).map(ProjectTaskToTag::getTagId).collect(Collectors.toList());
                List<ProjectTaskTag> taskTags = projectTaskTagListCopy.stream().filter(v -> tagIds.contains(v.getId())).collect(Collectors.toList());
                //计算任务完成情况
                List<ProjectTask> collect = secondLevelTaskList.stream().filter(v -> v.getPid().equals(e.getId())).collect(Collectors.toList());
                Integer code = BigDecimal.ROUND_DOWN;
                long count = collect.stream().filter(v -> v.getFinishStatus()==BigDecimal.ROUND_DOWN).count();
                StringBuffer stringBuffer = new StringBuffer().append(count).append("/").append(collect.size());
                boolean remarkFlag=StrUtil.isNotEmpty(e.getRemark());
                return new ProjectTaskListVo()
                        .setIsRemark(remarkFlag)
                        .setThumbsUpCount(e.getThumbsUpCount())
                        .setTaskName(e.getName())
                        .setEndTime(e.getEndTime())
                        .setExecuteStatus(e.getExecuteStatus())
                        .setStartTime(e.getStartTime())
                        .setProjectTaskMouldId(e.getMouldId())
                        .setId(e.getId())
                        .setFinishStatus(e.getFinishStatus())
                        .setSort(e.getSort())
                        .setUserDto(userDto)
                        .setChildCount(stringBuffer.toString())
                        .setProjectTaskTags(taskTags)
                        .setLikeMine(taskLikedMap.containsKey(e.getId()))
                        .setCollectMine(collMap.containsKey(e.getId()))
                        .setPriorityLevel(e.getPriorityLevel());
            };
            List<ProjectTaskListVo> listVos = projectTasks.parallelStream().map(function).peek(e -> {
                //时间对比 注意只对比年月日
                if (ObjectUtil.isNotNull(e.getEndTime())) {
                    //注意这里的偏移量
                    long second = e.getEndTime().toEpochSecond(ZoneOffset.ofHours(0));
                    DateTime dateTime = DateUtil.date(second);
                    //时分秒制空
                    dateTime = DateUtil.beginOfDay(dateTime);
                    //当前时间
                    DateTime time = DateUtil.beginOfDay(DateUtil.date());
                    long l = DateUtil.betweenDay(dateTime, time, true);
                    e.setTimeStatus(l);
                }
            }).collect(Collectors.toList());
            return listVos;
        }
        return new ArrayList<>();
    }

    @Log
    @ApiOperation("新增任务列表模板")
    @PostMapping("/save")
    public R<ProjectTaskMould> save(@RequestBody ProjectTaskMould dto) {
        String name = dto.getName();
        if (StrUtil.isEmpty(name)) {
            throw new BusinessException("请填写列表名称！");
        }
        if (StrUtil.isEmpty(dto.getProjectId())) {
            throw new BusinessException("缺少项目信息！");
        }
        UserDto user = UserCurrentUtils.getCurrentUser();
        return service.saveTaskMould(dto, user);
    }

    @Log
    @ApiOperation("修改任务列表模板")
    @PutMapping("/edit")
    @Transactional(rollbackFor = Exception.class)
    public R<ProjectTaskMould> edit(@RequestBody ProjectTaskMould dto) {
        String name = dto.getName();
        if (StrUtil.isEmpty(name)) {
            throw new BusinessException("请填写列表名称！");
        }
        if (StrUtil.isEmpty(dto.getId())) {
            throw new BusinessException("未找到列表信息！");
        }
        service.updateById(dto);
        return R.ok(dto);
    }


    @Log
    @ApiOperation("删除任务模板列表")
    @DeleteMapping("/del")
    @Transactional(rollbackFor = Exception.class)
    public R<Boolean> remove(ProjectTaskMould dto) {
        if (StrUtil.isEmpty(dto.getId())) {
            throw new BusinessException("未找到列表信息!");
        }
        //查看该列表下是否有任务
        List<ProjectTask> tasks = projectTaskService.lambdaQuery().eq(ProjectTask::getMouldId, dto.getId()).list();
        if (CollUtil.isNotEmpty(tasks)) {
            throw new BusinessException("请先清空该列表的任务!");
        }
        return R.ok(service.removeById(dto.getId()));
    }

    @Log
    @ApiOperation("详情")
    @GetMapping("/detail")
    public R<ProjectTaskMould> detail(String id) {
        if (StrUtil.isEmpty(id)) {
            throw new BusinessException("请选择任务列表!");
        }
        return R.ok(service.getById(id));
    }

    @Log
    @ApiOperation("项目的任务列表")
    @GetMapping("/listAll")
    public R<List<ProjectTaskMould>> listAll(@RequestParam String projectId) {
        List<ProjectTaskMould> list = service.list(
                Wrappers.<ProjectTaskMould>lambdaQuery()
                        .eq(ProjectTaskMould::getProjectId, projectId));
        return R.ok(list);
    }

    @Log
    @ApiOperation("修改-排序")
    @PostMapping("/sort")
    public R<List<ProjectTaskMould>> sort(@RequestBody List<String> ids) {
        List<ProjectTaskMould> projectTaskMoulds = service.listByIds(ids);
        Map<String, Integer> map = new LinkedHashMap<>(ids.size());
        for (int i = 0; i < ids.size(); i++) {
            map.put(ids.get(i), i);
        }
        List<ProjectTaskMould> collect = projectTaskMoulds.stream().peek(e -> e.setSort(map.get(e.getId()))).collect(Collectors.toList());
        service.updateBatchById(collect);
        return R.ok();
    }


    @Log
    @ApiOperation("查看任务列表下面的任务 最大的一层任务")
    @GetMapping("/tasks")
    public R<List<ProjectTaskDetailVO>> tasks(ProjectTaskReq req) {
        if (StrUtil.isEmpty(req.getMouldId())) {
            throw new BusinessException("请选择任务列表!");
        }
        UserDto currentUser = UserCurrentUtils.getCurrentUser();
        List<ProjectTask> tasks = service.tasks(req);
        List<ProjectTaskDetailVO> result = new ArrayList<>();
        if (CollUtil.isNotEmpty(tasks)) {
            List<String> memIdS = tasks.stream().filter(x -> StrUtil.isNotEmpty(x.getExecuteMemberId())).map(ProjectTask::getExecuteMemberId).collect(Collectors.toList());
            List<UserDto> users = userServiceApi.getByIds(memIdS).getData();
            Map<String, UserDto> dtoMap = users.stream().collect(Collectors.toMap(UserDto::getId, Function.identity()));
            tasks.forEach(x -> {
                ProjectTaskDetailVO detail = projectTaskService.detail(x.getId(), currentUser);
                if (StrUtil.isNotEmpty(x.getExecuteMemberId())) {
                    UserDto executor = dtoMap.get(detail.getExecuteMemberId());
                    detail.setExecutor(executor);
                }
                result.add(detail);
            });
        }
        return R.ok(result);
    }

}
