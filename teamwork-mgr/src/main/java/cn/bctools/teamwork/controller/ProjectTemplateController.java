package cn.bctools.teamwork.controller;

import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.R;
import cn.bctools.log.annotation.Log;
import cn.bctools.oauth2.utils.UserCurrentUtils;
import cn.bctools.oss.template.OssTemplate;
import cn.bctools.teamwork.common.common.config.CommonConfig;
import cn.bctools.teamwork.entity.ProjectTemplate;
import cn.bctools.teamwork.service.ProjectTemplateService;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 项目-模板表
 * <p>
 * FIXME 需要注意，该类为自动生成，需结合真实业务进行修改
 *
 * @author Auto Generator
 */
@Api(tags = "项目-模板表")
@RestController
@AllArgsConstructor
@RequestMapping("/ProjectTemplate")
public class ProjectTemplateController {

    ProjectTemplateService service;
    OssTemplate ossTemplate;
    CommonConfig commonConfig;

    @Log
    @ApiOperation("分页")
    @GetMapping("/page")
    public R<Page<ProjectTemplate>> page(Page<ProjectTemplate> page, ProjectTemplate dto) {
        UserDto currentUser = UserCurrentUtils.getCurrentUser();
        service.getPage(page, dto, currentUser);
        return R.ok(page);
    }

    @Log
    @ApiOperation("全部模板")
    @GetMapping("/listAll")
    public R<List<ProjectTemplate>> listAll() {
        List<ProjectTemplate> list = service.list(new LambdaQueryWrapper<ProjectTemplate>().eq(ProjectTemplate::getCreateById, UserCurrentUtils.getUserId()));
        return R.ok(list);
    }

    @Log
    @ApiOperation("模板新增")
    @PostMapping("/save")
    public R<ProjectTemplate> save(@RequestBody ProjectTemplate projectTemplate) {
        UserDto login = UserCurrentUtils.getCurrentUser();
        return R.ok(service.save(projectTemplate, login));
    }

    @Log
    @ApiOperation("修改")
    @PutMapping("/edit")
    public R<ProjectTemplate> edit(@RequestBody ProjectTemplate projectTemplate) {
        if (StrUtil.isEmpty(projectTemplate.getId())) {
            throw new BusinessException("请选择模板！");
        }
        UserDto login = UserCurrentUtils.getCurrentUser();
        return R.ok(service.updateTemplate(projectTemplate, login));
    }

    @Log
    @ApiOperation("详情")
    @GetMapping("/detail")
    public R<ProjectTemplate> detail(ProjectTemplate dto) {
        if (StrUtil.isEmpty(dto.getId())) {
            throw new BusinessException("请选择模板！");
        }
        ProjectTemplate projectTemplate = service.getOne(Wrappers.lambdaQuery(dto));
        //获取图片外链
        if (StrUtil.isNotEmpty(projectTemplate.getTemplateCover())) {
            String s = ossTemplate.fileLink(projectTemplate.getTemplateCover(), commonConfig.getBucketName());
            projectTemplate.setTemplateCoverUrl(s);
        }
        return R.ok(projectTemplate);
    }

    @Log
    @ApiOperation("删除模板")
    @DeleteMapping("/del")
    public R<Boolean> remove(@RequestParam String id) {
        return R.ok(service.removeTemplate(id));
    }

}
