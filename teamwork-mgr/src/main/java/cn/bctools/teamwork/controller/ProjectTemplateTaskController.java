package cn.bctools.teamwork.controller;

import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.R;
import cn.bctools.log.annotation.Log;
import cn.bctools.oauth2.utils.UserCurrentUtils;
import cn.bctools.teamwork.entity.ProjectTemplateTask;
import cn.bctools.teamwork.service.ProjectTemplateTaskService;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 模板任务列表
 * <p>
 * FIXME 需要注意，该类为自动生成，需结合真实业务进行修改
 *
 * @author Auto Generator
 */
@Api(tags = "模板任务列表")
@RestController
@AllArgsConstructor
@RequestMapping("/ProjectTemplateTask")
public class ProjectTemplateTaskController {

    ProjectTemplateTaskService service;


    @Log
    @ApiOperation("模板任务列表")
    @GetMapping("/listAll")
    public R<List<ProjectTemplateTask>> listAll(String templateId) {
        if (StrUtil.isEmpty(templateId)) {
            throw new BusinessException("请选择模板！");
        }
        return R.ok(service.getTaskListByTemplate(templateId));
    }

    @Log
    @ApiOperation("详情")
    @GetMapping("/detail")
    public R<ProjectTemplateTask> detail(ProjectTemplateTask dto) {
        if (StrUtil.isEmpty(dto.getId())) {
            throw new BusinessException("请选择任务列表！");
        }
        return R.ok(service.getOne(Wrappers.lambdaQuery(dto)));
    }

    @Log
    @ApiOperation("新增")
    @PostMapping("/save")
    public R<ProjectTemplateTask> save(@RequestBody ProjectTemplateTask dto) {
        if (StrUtil.isEmpty(dto.getTemplateId())) {
            throw new BusinessException("请选择模板！");
        }
        if (StrUtil.isEmpty(dto.getName())) {
            throw new BusinessException("请填写任务名称！");
        }
        UserDto login = UserCurrentUtils.getCurrentUser();
        return R.ok(service.saveTemplateTask(dto, login));
    }

    @Log
    @ApiOperation("修改")
    @PutMapping("/edit")
    public R<ProjectTemplateTask> edit(@RequestBody ProjectTemplateTask dto) {
        if (StrUtil.isEmpty(dto.getTemplateId())) {
            throw new BusinessException("请选择任务！");
        }
        UserDto login = UserCurrentUtils.getCurrentUser();
        service.editTemplateTask(dto, login);
        return R.ok(dto);
    }

    @Log
    @ApiOperation("删除")
    @DeleteMapping("/del/{id}")
    public R<Boolean> remove(@PathVariable String id) {
        return R.ok(service.removeById(id));
    }

}
