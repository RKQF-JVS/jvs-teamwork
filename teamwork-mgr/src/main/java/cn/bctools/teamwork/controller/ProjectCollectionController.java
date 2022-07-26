package cn.bctools.teamwork.controller;

import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.R;
import cn.bctools.log.annotation.Log;
import cn.bctools.oauth2.utils.UserCurrentUtils;
import cn.bctools.teamwork.common.consts.CommonConst;
import cn.bctools.teamwork.dto.ProjectCollectDTO;
import cn.bctools.teamwork.service.ProjectCollectionService;
import cn.hutool.core.util.StrUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 项目-收藏表
 * <p>
 * FIXME 需要注意，该类为自动生成，需结合真实业务进行修改
 *
 * @author Auto Generator
 */
@Api(tags = "项目-收藏表")
@RestController
@AllArgsConstructor
@RequestMapping("/ProjectCollection")
public class ProjectCollectionController {

    ProjectCollectionService service;


    @Log
    @ApiOperation("项目 收藏与取消收藏")
    @DeleteMapping("/collect")
    public R<Boolean> collect(ProjectCollectDTO dto) {
        if (StrUtil.isEmpty(dto.getProjectId())) {
            throw new BusinessException("请先选择项目！");
        }
        UserDto loginUser = UserCurrentUtils.getCurrentUser();
        switch (dto.getType()) {
            case CommonConst.COLLECT:
                return R.ok(service.collect(dto.getProjectId(), loginUser));
            case CommonConst.CANCEL:
                return R.ok(service.cancel(dto.getProjectId(), loginUser));
            default:
                throw new BusinessException("未找到操作类型！");
        }
    }

}
