package cn.bctools.teamwork.controller;

import cn.bctools.auth.api.api.AuthUserServiceApi;
import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.R;
import cn.bctools.log.annotation.Log;
import cn.bctools.teamwork.entity.ProjectVersionLog;
import cn.bctools.teamwork.service.ProjectVersionLogService;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 项目版本日志表
 * <p>
 * FIXME 需要注意，该类为自动生成，需结合真实业务进行修改
 *
 * @author Auto Generator
 */
@Api(tags = "项目版本日志表")
@RestController
@AllArgsConstructor
@RequestMapping("/ProjectVersionLog")
public class ProjectVersionLogController {

    ProjectVersionLogService service;
    AuthUserServiceApi authUserServiceApi;

    @Log
    @ApiOperation("分页 根据版本查询版本动态")
    @GetMapping("/page")
    public R<Page<ProjectVersionLog>> page(Page<ProjectVersionLog> page, ProjectVersionLog dto) {
        if(StrUtil.isEmpty(dto.getSourceId())){
            throw  new BusinessException("请选择一个版本！");
        }
        service.pageLog(page,dto);
        List<ProjectVersionLog> records = page.getRecords();
        if(CollUtil.isNotEmpty(records)){
            List<String> memIds = records.stream().map(ProjectVersionLog::getMemberId).distinct().collect(Collectors.toList());
           if(CollUtil.isNotEmpty(memIds)){
               Map<String, UserDto> dtoMap = authUserServiceApi.getByIds(memIds).getData().stream().collect(Collectors.toMap(UserDto::getId, Function.identity()));
                records.forEach(e ->{
                   e.setMember(dtoMap.get(e.getMemberId()));
                });
           }
        }

        return R.ok(page);
    }
}
