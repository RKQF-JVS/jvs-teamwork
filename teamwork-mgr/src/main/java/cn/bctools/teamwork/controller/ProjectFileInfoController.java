package cn.bctools.teamwork.controller;

import cn.bctools.oss.template.OssTemplate;
import cn.bctools.teamwork.common.common.config.CommonConfig;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.R;
import cn.bctools.log.annotation.Log;
import cn.bctools.oauth2.utils.UserCurrentUtils;
import cn.bctools.teamwork.entity.ProjectFileInfo;
import cn.bctools.teamwork.service.ProjectFileInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 项目文件信息表
 * <p>
 * FIXME 需要注意，该类为自动生成，需结合真实业务进行修改
 *
 * @author Auto Generator
 */
@Api(tags = "项目文件信息表")
@RestController
@AllArgsConstructor
@RequestMapping("/ProjectFileInfo")
public class ProjectFileInfoController {

    ProjectFileInfoService service;
    OssTemplate ossTemplate;
    CommonConfig commonConfig;


    @Log
    @ApiOperation("文件移到回收站")
    @DeleteMapping("/fileRecycle")
    public R<Boolean> fileRecycle(@RequestParam String fileId) {
        if (StrUtil.isEmpty(fileId)) {
            throw new BusinessException("请选择任务！");
        }
        UserDto user = UserCurrentUtils.getCurrentUser();
        return R.ok(service.fileRecycle(fileId, user));
    }

    @Log
    @ApiOperation("查询项目中回收站的文件 分页查询")
    @GetMapping("/listRecycle")
    public R<Page<ProjectFileInfo>> listRecycle(Page<ProjectFileInfo> page, String projectId) {
        if (StrUtil.isEmpty(projectId)) {
            throw new BusinessException("请选择项目！");
        }
        return R.ok(service.listRecycle(page, projectId));
    }

    @Log
    @ApiOperation("查询项目的文件 分页查询")
    @GetMapping("/listAll")
    public R<Page<ProjectFileInfo>> listAll(Page<ProjectFileInfo> page, String projectId) {
        if (StrUtil.isEmpty(projectId)) {
            throw new BusinessException("请选择项目！");
        }
        service.page(page,Wrappers.lambdaQuery(ProjectFileInfo.class).eq(ProjectFileInfo::getProjectId,projectId)
                .eq(ProjectFileInfo::getRecycleFlag,0));
        List<ProjectFileInfo> records = page.getRecords();
        if(CollUtil.isNotEmpty(records)){
            for (ProjectFileInfo record : records) {
                record.setFileUrl(ossTemplate.fileLink(record.getFileName(),commonConfig.getBucketName()));
            }
        }

        return R.ok(page);
    }

    @Log
    @ApiOperation("恢复文件")
    @PutMapping("/fileRecovery")
    public R<Boolean> fileRecovery(@RequestParam String fileId) {
        if (StrUtil.isEmpty(fileId)) {
            throw new BusinessException("请选择文件！");
        }
        UserDto user = UserCurrentUtils.getCurrentUser();
        return R.ok(service.fileRecovery(fileId, user));
    }

    @Log
    @ApiOperation("文件彻底删除")
    @DeleteMapping("/del")
    public R<Boolean> del(String fileId) {
        if (StrUtil.isEmpty(fileId)) {
            throw new BusinessException("请选择文件！");
        }
        UserDto user = UserCurrentUtils.getCurrentUser();
        return R.ok(service.del(fileId, user));
    }


}
