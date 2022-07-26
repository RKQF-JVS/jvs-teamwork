package cn.bctools.teamwork.controller;

import cn.bctools.auth.api.api.AuthUserServiceApi;
import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.R;
import cn.bctools.log.annotation.Log;
import cn.bctools.oauth2.utils.UserCurrentUtils;
import cn.bctools.teamwork.common.consts.CommonConst;
import cn.bctools.teamwork.dto.ProjectVersionEditDTO;
import cn.bctools.teamwork.dto.VersionTaskDTO;
import cn.bctools.teamwork.entity.ProjectTask;
import cn.bctools.teamwork.entity.ProjectTaskMould;
import cn.bctools.teamwork.entity.ProjectVersion;
import cn.bctools.teamwork.service.ProjectTaskMouldService;
import cn.bctools.teamwork.service.ProjectTaskService;
import cn.bctools.teamwork.service.ProjectVersionService;
import cn.bctools.teamwork.vo.ProjectMouldAndTaskVO;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 版本库版本
 * <p>
 * FIXME 需要注意，该类为自动生成，需结合真实业务进行修改
 *
 * @author Auto Generator
 */
@Api(tags = "版本库版本")
@RestController
@AllArgsConstructor
@RequestMapping("/ProjectVersion")
public class ProjectVersionController {

    ProjectVersionService service;
    AuthUserServiceApi authUserServiceApi;
    ProjectTaskMouldService projectTaskMouldService;
    ProjectTaskService projectTaskService;

    @Log
    @ApiOperation("查询版本库版本的任务")
    @GetMapping("/getVersionTask")
    public R<List<ProjectTask>> getVersionTask(ProjectVersion dto) {
        String versionId = dto.getId();
        if (StrUtil.isEmpty(versionId)) {
            throw new BusinessException("请选择一个版本库版本！");
        }
        List<ProjectTask> result = service.getVersionTask(dto);
        fillExecutor(result);
        return R.ok(result);
    }

    @Log
    @ApiOperation("查询项目中可添加进发布的任务")
    @GetMapping("/getPublishTask")
    public R<List<ProjectMouldAndTaskVO>> getPublishTask(@RequestParam String projectId) {
        if (StrUtil.isEmpty(projectId)) {
            throw new BusinessException("请选择项目!");
        }
        List<ProjectMouldAndTaskVO> result = new ArrayList<>();
        List<ProjectTaskMould> taskMoulds = projectTaskMouldService.lambdaQuery()
                .eq(ProjectTaskMould::getProjectId, projectId)
                .list();
        if (CollUtil.isNotEmpty(taskMoulds)) {
            ProjectMouldAndTaskVO vo = null;
            for (ProjectTaskMould mould : taskMoulds) {
                vo = new ProjectMouldAndTaskVO();
                BeanUtil.copyProperties(mould, vo);
                List<ProjectTask> tasks = projectTaskService.lambdaQuery().eq(ProjectTask::getMouldId, mould.getId())
                        .eq(ProjectTask::getRecycleFlag, CommonConst.NO_DELETE_STATUS)
                        .isNull(ProjectTask::getVersionId).list();

                fillExecutor(tasks);
                vo.setTasks(tasks);
                result.add(vo);
            }
        }
        return R.ok(result);
    }

    private void fillExecutor(List<ProjectTask> tasks) {
        if (CollUtil.isNotEmpty(tasks)) {
            List<String> memIds = tasks.stream().filter(x->StrUtil.isNotEmpty(x.getExecuteMemberId())).map(ProjectTask::getExecuteMemberId).distinct().collect(Collectors.toList());
            List<UserDto> data = authUserServiceApi.getByIds(memIds).getData();
            Map<String, List<ProjectTask>> map = tasks.stream().collect(Collectors.groupingBy(task -> Optional.ofNullable(task.getExecuteMemberId()).orElse("0")));
            if (CollUtil.isNotEmpty(data)) {
                for (UserDto userDto : data) {
                    String id = userDto.getId();
                    List<ProjectTask> t = map.get(id);
                    if (CollUtil.isNotEmpty(t)) {
                        t.forEach(x -> {
                            x.setExecutor(userDto);
                        });
                    }
                }
            }
        }
    }

    @Log
    @ApiOperation("根据版本库id 查询版本信息")
    @GetMapping("/listVersion")
    public R<List<ProjectVersion>> listVersion(ProjectVersion dto) {
        String featuresId = dto.getFeaturesId();
        if (StrUtil.isEmpty(featuresId)) {
            throw new BusinessException("请选择一个版本库！");
        }
        List<ProjectVersion> result = service.getListByFeaturesId(featuresId);
        return R.ok(result);
    }

    @Log
    @ApiOperation("详情")
    @GetMapping("/detail")
    public R<ProjectVersion> detail(ProjectVersion dto) {
        if (StrUtil.isEmpty(dto.getId())) {
            throw new BusinessException("请选择版本！");
        }
        return R.ok(service.getOne(Wrappers.lambdaQuery(dto)));
    }

    @Log
    @ApiOperation("新增版本")
    @PostMapping("/save")
    public R<ProjectVersion> save(@RequestBody ProjectVersion dto) {
        if (StrUtil.isEmpty(dto.getFeaturesId())) {
            throw new BusinessException("请选择一个版本库！");
        }
        if (StrUtil.isEmpty(dto.getName())) {
            throw new BusinessException("请填写版本名称！");
        }
        UserDto currentUser = UserCurrentUtils.getCurrentUser();
        service.saveVersion(dto, currentUser);
        return R.ok(dto);
    }

    @Log
    @ApiOperation("版本新增发布内容")
    @PostMapping("/saveVersionTask")
    public R<VersionTaskDTO> saveVersionTask(@RequestBody VersionTaskDTO dto) {
        if (StrUtil.isEmpty(dto.getId())) {
            throw new BusinessException("请选择一个版本！");
        }
        if (CollUtil.isEmpty(dto.getTasks())) {
            throw new BusinessException("请选择关联的任务！");
        }
        UserDto currentUser = UserCurrentUtils.getCurrentUser();
        service.saveVersionTask(dto, currentUser);
        return R.ok(dto);
    }

    @Log
    @ApiOperation("版本界面更改状态信息")
    @PostMapping("/changeStatus")
    public R<Boolean> changeStatus(@RequestBody ProjectVersion dto) {
        if (StrUtil.isEmpty(dto.getId())) {
            throw new BusinessException("请选择一个版本！");
        }
        UserDto currentUser = UserCurrentUtils.getCurrentUser();
       Boolean result= service.changeStatus(dto, currentUser);
        return R.ok(result);
    }

    @Log
    @ApiOperation("修改")
    @PutMapping("/edit")
    public R<ProjectVersion> edit(@RequestBody ProjectVersionEditDTO dto) {
        if(StrUtil.isEmpty(dto.getVersionId())){
            throw new BusinessException("请选择版本!");
        }
        UserDto currentUser = UserCurrentUtils.getCurrentUser();
        ProjectVersion result=service.edit(dto,currentUser);
        return R.ok(result);
    }

    @Log
    @ApiOperation("删除")
    @DeleteMapping("/del")
    public R<Boolean> remove(@RequestBody ProjectVersion dto) {
        if(StrUtil.isEmpty(dto.getId())){
            throw new BusinessException("请选择一个版本！");
        }
        UserDto currentUser = UserCurrentUtils.getCurrentUser();
        Boolean result =service.removeProjectVersion(dto,currentUser);
        return R.ok(result);
    }

    @Log
    @ApiOperation("版本移除关联任务")
    @DeleteMapping("/removeVersionTask")
    public R<Boolean> removeVersionTask(@RequestBody ProjectTask dto) {
        if (StrUtil.isEmpty(dto.getId())) {
            throw new BusinessException("请选择任务！");
        }
        UserDto currentUser = UserCurrentUtils.getCurrentUser();
        boolean result = service.removeVersionTask(dto, currentUser);
        return R.ok(result);
    }


}
