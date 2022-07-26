package cn.bctools.teamwork.service;

import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.utils.R;
import cn.bctools.teamwork.dto.ProjectMemberInvite;
import cn.bctools.teamwork.entity.ProjectMember;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Auto Generator
 */
public interface ProjectMemberService extends IService<ProjectMember> {

    /**
     * 根据项目id 和 成员id,获取该成员是否在项目中 存在返回 1
     * @param projectId
     * @param memberId
     * @return
     */
    Boolean isProjectMember(String projectId, String memberId);

    /**
     * 项目邀请新的成员
     * @param dto
     * @param currentUser
     * @return
     */
    R<ProjectMember> inviteMember(ProjectMemberInvite dto, UserDto currentUser);

    /**
     * 移除项目成员
     * @param dto
     * @param userDto
     * @param flag 是否保存动态
     * @return
     */
    Boolean removeMember(ProjectMemberInvite dto,UserDto userDto,Boolean flag);

    /**
     * 查询项目成员
     * @param projectId
     * @return
     */
    List<ProjectMember> listProjectMember(String projectId);

    /**
     * 根据项目成员查询成员拥有的项目id
     * @param memberIds
     * @return
     */
    List<ProjectMember> getProjectByMembers(ArrayList<String> memberIds);
}
