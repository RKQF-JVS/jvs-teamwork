package cn.bctools.teamwork.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author admin
 */
@Data
@ApiModel("任务发表评论内容")
public class ProjectTaskCommentDTO {

    @ApiModelProperty("任务id")
    private String taskId;

    @ApiModelProperty("发表的评论内容")
    private String comment;

    @ApiModelProperty("提到的人的内容")
    private Mentions mentions;

    /**
     * 提到的人的内容信息
     */
    @ApiModel
    @Data
    public static class Mentions {
        @ApiModelProperty("提到的人的Id")
        private String memberId;
        @ApiModelProperty("提到的人的名字")
        private String name;
        @ApiModelProperty("提到的人的内容")
        private String comment;
    }
}
