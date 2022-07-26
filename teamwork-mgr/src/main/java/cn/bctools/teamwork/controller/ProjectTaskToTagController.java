package cn.bctools.teamwork.controller;

import cn.bctools.common.utils.R;
import cn.bctools.log.annotation.Log;
import cn.bctools.oauth2.utils.UserCurrentUtils;
import cn.bctools.teamwork.entity.ProjectTaskToTag;
import cn.bctools.teamwork.service.ProjectTaskToTagService;
import cn.bctools.teamwork.vo.ProjectTaskTagUpdateVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 任务标签映射表
 * <p>
 * FIXME 需要注意，该类为自动生成，需结合真实业务进行修改
 *
 * @author Auto Generator
 */
@Api(tags = "任务标签映射表")
@RestController
@AllArgsConstructor
@RequestMapping("/ProjectTaskToTag")
public class ProjectTaskToTagController {

    ProjectTaskToTagService service;

    @Log
    @ApiOperation("设置或取消标签")
    @PostMapping("/update")
    public R<ProjectTaskToTag> edit(@RequestBody ProjectTaskTagUpdateVo projectTaskTagUpdateVo) {
        if (projectTaskTagUpdateVo.getIsSave()) {
            ProjectTaskToTag projectTaskToTag = new ProjectTaskToTag().setTaskId(projectTaskTagUpdateVo.getTaskId()).setTagId(projectTaskTagUpdateVo.getTagId());
            projectTaskToTag.initSave(UserCurrentUtils.getCurrentUser());
            service.save(projectTaskToTag);
        } else {
            service.remove(new LambdaQueryWrapper<ProjectTaskToTag>().eq(ProjectTaskToTag::getTagId, projectTaskTagUpdateVo.getTaskId()).eq(ProjectTaskToTag::getTaskId, projectTaskTagUpdateVo.getTaskId()));
        }
        return R.ok();
    }
}
