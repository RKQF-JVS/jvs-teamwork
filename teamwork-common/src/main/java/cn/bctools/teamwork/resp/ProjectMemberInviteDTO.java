package cn.bctools.teamwork.resp;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 项目管理 我的项目 邀请新成员界面
 *
 * @author admin
 */
@Data
@Accessors(chain = true)
public class ProjectMemberInviteDTO {
    @ApiModelProperty("成员id")
    private String memberId;
    @ApiModelProperty("成员名字")
    private String name;
    @ApiModelProperty("成员邮箱")
    private String email;
    @ApiModelProperty("成员是否加入了项目 1-是 0-否")
    private Boolean isJoin;
    @ApiModelProperty("头像")
    private String avatar;
    @ApiModelProperty("状态 0-停用 1-启用")
    private String status;
}
