package cn.bctools.teamwork.controller;

import cn.bctools.common.utils.R;
import cn.bctools.log.annotation.Log;
import cn.bctools.teamwork.entity.ProjectInfo;
import cn.bctools.teamwork.entity.ProjectTaskWorkTimeRecord;
import cn.bctools.teamwork.service.ProjectTaskWorkTimeRecordService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 任务工时记录
 * <p>
 * FIXME 需要注意，该类为自动生成，需结合真实业务进行修改
 *
 * @author Auto Generator
 */
@Api(tags = "任务工时记录")
@RestController
@AllArgsConstructor
@RequestMapping("/ProjectTaskWorkTimeRecord")
public class ProjectTaskWorkTimeRecordController {

    ProjectTaskWorkTimeRecordService projectTaskWorkTimeRecordService;


    @Log
    @ApiOperation("删除工时记录")
    @DeleteMapping("/delWorkTimeRecord")
    @Transactional(rollbackFor = Exception.class)
    public R delWorkTimeRecord(@RequestBody ProjectTaskWorkTimeRecord dto) {
        boolean b = projectTaskWorkTimeRecordService.removeById(dto);
        return b ? R.ok() : R.failed();
    }


}
