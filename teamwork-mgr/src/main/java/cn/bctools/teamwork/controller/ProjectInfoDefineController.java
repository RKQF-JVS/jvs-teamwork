package cn.bctools.teamwork.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.R;
import cn.bctools.log.annotation.Log;
import cn.bctools.oauth2.utils.UserCurrentUtils;
import cn.bctools.teamwork.entity.ProjectInfoDefine;
import cn.bctools.teamwork.service.ProjectInfoDefineService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 项目自定义信息表
 * <p>
 * FIXME 需要注意，该类为自动生成，需结合真实业务进行修改
 *
 * @author Auto Generator
 */
@Api(tags = "项目自定义信息表")
@RestController
@AllArgsConstructor
@RequestMapping("/ProjectInfoDefine")
public class ProjectInfoDefineController {

    ProjectInfoDefineService service;

    @Log
    @ApiOperation("概览-项目信息-查询")
    @GetMapping("/list")
    public R<List<ProjectInfoDefine>> list(ProjectInfoDefine dto) {
        if (StrUtil.isEmpty(dto.getProjectId())) {
            throw new BusinessException("请选择项目！");
        }
        List<ProjectInfoDefine> list = service.list(Wrappers.lambdaQuery(dto));
        return R.ok(list);
    }

    @Log
    @ApiOperation("概览-项目信息-详情")
    @GetMapping("/detail")
    public R<ProjectInfoDefine> detail(ProjectInfoDefine dto) {
        if (StrUtil.isEmpty(dto.getId())) {
            throw new BusinessException("请选择项目信息！");
        }
        return R.ok(service.getOne(Wrappers.lambdaQuery(dto)));
    }

    @Log
    @ApiOperation("概览-项目信息-新增")
    @PostMapping("/save")
    @Transactional(rollbackFor = Exception.class)
    public R<ProjectInfoDefine> save(@RequestBody ProjectInfoDefine dto) {
        if (StrUtil.isEmpty(dto.getProjectId()) || StrUtil.isEmpty(dto.getName())) {
            throw new BusinessException("参数非法！");
        }
        UserDto user = UserCurrentUtils.getCurrentUser();
        service.saveProjectInfo(dto, user);
        return R.ok(dto);
    }

    @Log
    @ApiOperation("概览-项目信息-编辑")
    @PutMapping("/edit")
    public R<ProjectInfoDefine> edit(@RequestBody ProjectInfoDefine dto) {
        if (StrUtil.isEmpty(dto.getId())) {
            throw new BusinessException("请选择项目信息！");
        }
        if (StrUtil.isEmpty(dto.getName())) {
            throw new BusinessException("请填写项目信息名称！");
        }
        UserDto user = UserCurrentUtils.getCurrentUser();
        service.editProjectInfo(dto, user);
        return R.ok(dto);
    }

    @Log
    @ApiOperation("删除")
    @DeleteMapping("/del")
    @Transactional(rollbackFor = Exception.class)
    public R<Boolean> remove(ProjectInfoDefine dto) {
        if (StrUtil.isEmpty(dto.getId())) {
            throw new BusinessException("请选择项目信息！");
        }
        return R.ok(service.removeById(dto.getId()));
    }

}
