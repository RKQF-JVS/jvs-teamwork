package cn.bctools.teamwork.service;

import com.baomidou.mybatisplus.extension.service.IService;
import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.teamwork.dto.TaskMemberAddDTO;
import cn.bctools.teamwork.entity.ProjectTaskMember;

import java.util.List;

/**
 *
 * @author Auto Generator
 */
public interface ProjectTaskMemberService extends IService<ProjectTaskMember> {

    /**
     * 这是一个保存任务成员的方法
     * @param taskMember 任务成员
     * @param user 操作人信息
     * @param isLog  是否需要增加任务日志
     * @return
     */
    ProjectTaskMember inviteMember(TaskMemberAddDTO taskMember, UserDto user, boolean isLog);

    /**
     * 这是一个移除任务成员的方法
     * @param taskMember 任务成员
     * @param user 操作人信息
     * @param isLog  是否需要增加任务日志
     * @return
     */
    Boolean removeMember(TaskMemberAddDTO taskMember, UserDto user, boolean isLog);

    /**
     * 根据任务id和成员id 查询数据库是否存在 存在返回 true
     * @param memberId
     * @param taskId
     * @return
     */
    Boolean verifyExists(String memberId,String taskId);

    /**
     * 根据任务id 查询任务成员
     * @param taskId
     * @return
     */
    List<ProjectTaskMember> getMemberByTaskId(String taskId);
}
