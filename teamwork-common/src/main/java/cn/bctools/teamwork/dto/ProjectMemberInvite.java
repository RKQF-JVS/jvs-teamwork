package cn.bctools.teamwork.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 项目管理 我的项目 邀请新成员
 * @author admin
 */
@Data
public class ProjectMemberInvite {

    @ApiModelProperty(value = "项目id",required = true)
    private String projectId;

    @ApiModelProperty(value = "成员id",required = true)
    private String memberId;

    @ApiModelProperty(value = "成员名字",required = true)
    String memberName;

}
