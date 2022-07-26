package cn.bctools.teamwork.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.R;
import cn.bctools.log.annotation.Log;
import cn.bctools.oauth2.utils.UserCurrentUtils;
import cn.bctools.teamwork.entity.ProjectFeatures;
import cn.bctools.teamwork.service.ProjectFeaturesService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 版本库表
 * <p>
 * FIXME 需要注意，该类为自动生成，需结合真实业务进行修改
 *
 * @author Auto Generator
 */
@Api(tags = "版本库表")
@RestController
@AllArgsConstructor
@RequestMapping("/ProjectFeatures")
public class ProjectFeaturesController {

    ProjectFeaturesService service;

    @Log
    @ApiOperation("版本库列表")
    @PostMapping("/list")
    public R<List<ProjectFeatures>> list(ProjectFeatures dto) {
        if (StrUtil.isEmpty(dto.getProjectId())) {
            throw new BusinessException("请选择一个项目！");
        }
        List<ProjectFeatures> result = service.lambdaQuery()
                .eq(ProjectFeatures::getProjectId, dto.getProjectId())
                .orderByAsc(ProjectFeatures::getCreateTime)
                .list();
        return R.ok(result);
    }

    @Log
    @ApiOperation("详情")
    @GetMapping("/detail")
    public R<ProjectFeatures> detail(ProjectFeatures dto) {
        if (StrUtil.isEmpty(dto.getId())) {
            throw new BusinessException("请选择版本库！");
        }
        return R.ok(service.getOne(Wrappers.lambdaQuery(dto)));
    }

    @Log
    @ApiOperation("新增版本库")
    @PostMapping("/save")
    public R<ProjectFeatures> save(@RequestBody ProjectFeatures dto) {
        if (StrUtil.isEmpty(dto.getProjectId()) || StrUtil.isEmpty(dto.getName())) {
            throw new BusinessException("参数错误！");
        }
        UserDto currentUser = UserCurrentUtils.getCurrentUser();
        service.saveFeatures(dto, currentUser);
        return R.ok(dto);
    }

    @Log
    @ApiOperation("编辑版本库")
    @PutMapping("/edit")
    public R<ProjectFeatures> edit(@RequestBody ProjectFeatures dto) {
        if (StrUtil.isEmpty(dto.getId()) || StrUtil.isEmpty(dto.getName())) {
            throw new BusinessException("参数错误！");
        }
        UserDto currentUser = UserCurrentUtils.getCurrentUser();
        service.updateFeatures(dto, currentUser);
        return R.ok(dto);
    }

    @Log
    @ApiOperation("删除版本库")
    @DeleteMapping("/del")
    public R<Boolean> remove(@RequestBody ProjectFeatures dto) {
        if (StrUtil.isEmpty(dto.getId())) {
            throw new BusinessException("请选择版本库！");
        }
        UserDto currentUser = UserCurrentUtils.getCurrentUser();
        return R.ok(service.removeFeatures(dto, currentUser));
    }

}
