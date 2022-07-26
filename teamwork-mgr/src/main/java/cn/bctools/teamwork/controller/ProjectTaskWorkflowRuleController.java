package cn.bctools.teamwork.controller;

import cn.bctools.common.utils.R;
import cn.bctools.log.annotation.Log;
import cn.bctools.teamwork.dto.ProjectTaskWorkflowRuleDTO;
import cn.bctools.teamwork.service.ProjectTaskWorkflowRuleService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 任务工作流流转规则表
 * <p>
 * FIXME 需要注意，该类为自动生成，需结合真实业务进行修改
 *
 * @author Auto Generator
 */
@Api(tags = "任务工作流流转规则表")
@RestController
@AllArgsConstructor
@RequestMapping("/ProjectTaskWorkflowRule")
public class ProjectTaskWorkflowRuleController {

    ProjectTaskWorkflowRuleService service;



    @Log
    @ApiOperation("根据任务流转规则id ，查询具体规则")
    @GetMapping("/getRuleByFlowId")
    public R<ProjectTaskWorkflowRuleDTO> detail(String workFlowId) {
        return R.ok(service.getRuleByFlowId(workFlowId));
    }





}
