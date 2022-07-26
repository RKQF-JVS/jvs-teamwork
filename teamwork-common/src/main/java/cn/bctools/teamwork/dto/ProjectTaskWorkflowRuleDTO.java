package cn.bctools.teamwork.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 *
 * @author admin
 */
@ApiModel("具体的规则")
@Data
public class ProjectTaskWorkflowRuleDTO {

    @ApiModelProperty(value = "模板任务id",required = true)
     private  String mouldId;

    @ApiModelProperty(value = "第一次场景 设置执行人有结果，其他无结果value",required = true)
    private  RuleActionResult first;


    @ApiModelProperty(value = "第二次场景 指派或者流转 有value",required = true)
    private  RuleActionResult second;

    @ApiModelProperty("第三次场景 第二次场景为指派，第三场景可选流转或指派")
    private  RuleActionResult third;

    @ApiModelProperty(value = "修改任务状态 -1未改变 1-完成 2-未完成",required = true)
    private  String state;
    @Data
    public static class RuleActionResult{

        @ApiModelProperty("场景")
        private Integer action;

        @ApiModelProperty("结果")
        private String value;

    }


}
