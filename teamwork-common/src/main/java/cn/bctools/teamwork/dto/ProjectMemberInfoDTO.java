package cn.bctools.teamwork.dto;

import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.teamwork.entity.ProjectMember;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 成员信息返回
 * @author admin
 */
@Data
@ApiModel("成员信息返回")
public class ProjectMemberInfoDTO extends ProjectMember {

    @ApiModelProperty("成员的详细信息")
    private UserDto memberInfo;
    @ApiModelProperty("是否为拥有者")
    private Boolean isOwner;
}
