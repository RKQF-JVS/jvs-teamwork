package cn.bctools.teamwork.controller;

import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.R;
import cn.bctools.log.annotation.Log;
import cn.bctools.oauth2.utils.UserCurrentUtils;
import cn.bctools.teamwork.dto.ProjectTaskWorkflowAddDTO;
import cn.bctools.teamwork.entity.ProjectTaskWorkflow;
import cn.bctools.teamwork.service.ProjectTaskWorkflowService;
import cn.hutool.core.util.StrUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 项目规则表
 * <p>
 * FIXME 需要注意，该类为自动生成，需结合真实业务进行修改
 *
 * @author Auto Generator
 */
@Api(tags = "项目规则表")
@RestController
@AllArgsConstructor
@RequestMapping("/ProjectTaskWorkflow")
public class ProjectTaskWorkflowController {

    ProjectTaskWorkflowService service;




    @Log
    @ApiOperation("根据项目查询该项目的规则")
    @GetMapping("/listWorkflow")
    public R<List<ProjectTaskWorkflow>> listWorkflow(ProjectTaskWorkflow dto) {
        return R.ok(service.listWorkflow(dto));
    }

    @Log
    @ApiOperation("查看规则信息")
    @GetMapping("/detail")
    public R<ProjectTaskWorkflow> detail(ProjectTaskWorkflow dto) {
        if (StrUtil.isEmpty(dto.getId())) {
            throw new BusinessException("请选择规则！");
        }
        return R.ok(service.getById(dto.getId()));
    }

    @Log
    @ApiOperation("新增")
    @PostMapping("/save")
    public R<Boolean> save(@RequestBody ProjectTaskWorkflowAddDTO dto) {
        if (StrUtil.isEmpty(dto.getProjectId())) {
            throw new BusinessException("请选择项目！");
        }
        if (StrUtil.isEmpty(dto.getName())) {
            throw new BusinessException("请填写规则名称！");
        }
        UserDto user = UserCurrentUtils.getCurrentUser();
        return R.ok(service.saveWorkFlow(dto, user));
    }

    @Log
    @ApiOperation("修改")
    @PutMapping("/edit")
    public R<Boolean> edit(@RequestBody ProjectTaskWorkflowAddDTO dto) {
        if (StrUtil.isEmpty(dto.getId())) {
            throw new BusinessException("请选择规则!");
        }
        UserDto user = UserCurrentUtils.getCurrentUser();
        return R.ok(service.updateFlow(dto, user));
    }

    @Log
    @ApiOperation("删除")
    @DeleteMapping("/del")
    public R<Boolean> remove(ProjectTaskWorkflow dto) {
        if (StrUtil.isEmpty(dto.getId())) {
            throw new BusinessException("请选择规则!");
        }
        return R.ok(service.delete(dto));
    }

}
