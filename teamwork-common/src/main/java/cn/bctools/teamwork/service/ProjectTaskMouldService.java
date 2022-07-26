package cn.bctools.teamwork.service;

import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.utils.R;
import cn.bctools.teamwork.dto.req.ProjectTaskReq;
import cn.bctools.teamwork.entity.ProjectTask;
import cn.bctools.teamwork.entity.ProjectTaskMember;
import cn.bctools.teamwork.entity.ProjectTaskMould;
import cn.bctools.teamwork.vo.ProjectTaskListVo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * @author Auto Generator
 */
public interface ProjectTaskMouldService extends IService<ProjectTaskMould> {
    /***
     * 功能描述: <br>
     * 〈递归生成数据〉
     * @param list 查询的模型所有任务集合
     * @param userMap 用户数据
     * @param projectTaskMemberList 任务执行人 数据
     * @param id 需要查询的任务id
     * @return java.util.List<cn.bctools.teamwork.vo.ProjectTaskListVo>
     */
    List<ProjectTaskListVo> regroupDataTaskList(String id,Map<String, UserDto> userMap, List<ProjectTask> list,List<ProjectTaskMember> projectTaskMemberList);

    /**
     * 保存项目任务列表模板
     *
     * @param dto
     * @param user
     * @return
     */
    R<ProjectTaskMould> saveTaskMould(ProjectTaskMould dto, UserDto user);

    /**
     * 查看任务列表下面的任务 最上层
     *
     * @param req
     * @return
     */
    List<ProjectTask> tasks(ProjectTaskReq req);
}
