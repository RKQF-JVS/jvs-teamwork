package cn.bctools.teamwork.controller;

import cn.bctools.auth.api.api.AuthUserServiceApi;
import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.R;
import cn.bctools.log.annotation.Log;
import cn.bctools.oauth2.utils.UserCurrentUtils;
import cn.bctools.teamwork.entity.ProjectTaskTag;
import cn.bctools.teamwork.service.ProjectTaskTagService;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 任务标签表
 * <p>
 * FIXME 需要注意，该类为自动生成，需结合真实业务进行修改
 *
 * @author Auto Generator
 */
@Api(tags = "任务标签表")
@RestController
@AllArgsConstructor
@RequestMapping("/ProjectTaskTag")
public class ProjectTaskTagController {

    ProjectTaskTagService service;

    AuthUserServiceApi authUserServiceApi;

    @Log
    @ApiOperation("查询项目所有的标签")
    @GetMapping("/listTag")
    public R<List<ProjectTaskTag>> page(ProjectTaskTag dto) {
        return R.ok(service.lambdaQuery().eq(ProjectTaskTag::getProjectId, dto.getProjectId()).list());
    }


    @Log
    @ApiOperation("分页 查询项目标签")
    @GetMapping("/listTagPage")
    public R<Page<ProjectTaskTag>> listTagPage(Page<ProjectTaskTag> page, ProjectTaskTag dto) {
        if (StrUtil.isEmpty(dto.getProjectId())) {
            throw new BusinessException("数据有误!");
        }
        return R.ok(service.lambdaQuery().eq(ProjectTaskTag::getProjectId, dto.getProjectId()).page(page));
    }

    @Log
    @ApiOperation("新增标签")
    @PostMapping("/save")
    public R<ProjectTaskTag> save(@RequestBody ProjectTaskTag dto) {
        if (StrUtil.isEmpty(dto.getProjectId()) || StrUtil.isEmpty(dto.getName()) || StrUtil.isEmpty(dto.getColor())) {
            throw new BusinessException("数据有误！");
        }
        UserDto user = UserCurrentUtils.getCurrentUser();
        return R.ok(service.saveTag(dto, user));
    }

    @Log
    @ApiOperation("修改")
    @PutMapping("/edit")
    public R<ProjectTaskTag> edit(@RequestBody ProjectTaskTag dto) {
        UserDto user = UserCurrentUtils.getCurrentUser();
        return R.ok(service.editTag(dto, user));
    }


    @Log
    @ApiOperation("删除")
    @DeleteMapping("/del")
    @Transactional(rollbackFor = Exception.class)
    public R<Boolean> remove(@RequestBody ProjectTaskTag dto) {
        return R.ok(service.removeTagById(dto.getId()));
    }

}
