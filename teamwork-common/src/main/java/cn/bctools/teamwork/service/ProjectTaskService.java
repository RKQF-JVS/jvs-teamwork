package cn.bctools.teamwork.service;

import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.teamwork.dto.*;
import cn.bctools.teamwork.dto.req.ProjectTaskReq;
import cn.bctools.teamwork.dto.req.TaskDateCountReq;
import cn.bctools.teamwork.entity.*;
import cn.bctools.teamwork.vo.ProjectTaskCountForDateVO;
import cn.bctools.teamwork.vo.ProjectTaskDetailVO;
import cn.bctools.teamwork.vo.ProjectTaskTreeMouldVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * @author Auto Generator
 */
public interface ProjectTaskService extends IService<ProjectTask> {

    /**
     * 添加任务
     *
     * @param dto
     * @param user
     * @param pLog 添加子任务是否添加父任务动态
     * @return
     */
    ProjectTask saveTask(ProjectTaskAddDTO dto, UserDto user, boolean pLog);

    /**
     * 修改任务的信息
     *
     * @param dto
     * @param user
     * @return
     */
    ProjectTask editTask(ProjectTaskEditDTO dto, UserDto user);

    /**
     * 根据任务id 和 标签id
     * 为任务添加标签
     *
     * @param dto
     * @param user
     * @return
     */
    ProjectTaskToTag setTag(ProjectTaskToTag dto, UserDto user);

    /**
     * 任务详情添加工时记录
     *
     * @param dto
     * @param user
     * @return
     */
    ProjectTaskWorkTimeRecord saveTaskWorkTime(ProjectTaskWorkTimeRecord dto, UserDto user);

    /**
     * 任务详情编辑工时记录
     *
     * @param dto
     * @param user
     * @return
     */
    ProjectTaskWorkTimeRecord editTaskWorkTime(ProjectTaskWorkTimeRecord dto, UserDto user);

    /**
     * 根据任务id 返回任务的基本信息
     *
     * @param taskId
     * @param userDto
     * @return
     */
    ProjectTaskDetailVO detail(String taskId, UserDto userDto);

    /**
     * 根据任务父id 查询未删除的子任务
     *
     * @param taskPId
     * @return
     */
    List<ProjectTask> listChildTask(String taskPId);

    /**
     * 改变隐私模式状态
     *
     * @param dto
     * @param user
     * @return
     */
    Boolean setPrivate(ProjectTask dto, UserDto user);

    /**
     * 任务执行者指派
     *
     * @param dto
     * @param user
     * @return
     */
    Boolean assignExecutor(ProjectTaskAssignExecutorDTO dto, UserDto user);

    /**
     * 任务完成
     *
     * @param dto
     * @param user
     * @return
     */
    Boolean taskDone(ProjectTaskDoneDTO dto, UserDto user);

    /**
     * 添加任务关联文件
     *
     * @param fileInfo
     * @param task
     * @param user
     * @return
     */
    ProjectFileInfo taskRelationFile(ProjectFileInfo fileInfo, ProjectTask task, UserDto user);

    /**
     * 根据任务的id,查询关联文件信息
     *
     * @param taskId
     * @return
     */
    List<ProjectFileInfo> listTaskFile(String taskId);

    /**
     * 根据任务id查询任务动态
     *
     * @param page
     * @param dto
     * @return
     */
    Page<ProjectLog> listProjectLog(Page<ProjectLog> page, ProjectLog dto);

    /**
     * 任务评论区评论
     *
     * @param dto
     * @param user
     * @return
     */
    ProjectTaskCommentDTO taskComment(ProjectTaskCommentDTO dto, UserDto user);

    /**
     * 任务收藏
     *
     * @param dto
     * @param user
     * @return
     */
    Boolean taskCollect(TaskCollectDTO dto, UserDto user);

    /**
     * 任务点赞
     *
     * @param dto
     * @param user
     * @return
     */
    Boolean taskLike(TaskLikeDTO dto, UserDto user);

    /**
     * 将任务移到回收站
     *
     * @param taskId
     * @param user
     * @return
     */
    Boolean taskRecycle(String taskId, UserDto user);

    /**
     * 恢复任务
     *
     * @param taskId
     * @param user
     * @return
     */
    Boolean taskRecovery(String taskId, UserDto user);

    /**
     * 彻底删除任务
     *
     * @param taskId
     * @param user
     * @return
     */
    Boolean del(String taskId, UserDto user);

    /**
     * 查询任务下面回收站内的任务
     *
     * @param page
     * @param projectId
     * @return
     */
    Page<ProjectTask> listRecycle(Page<ProjectTask> page, String projectId);

    /**
     * 任务列表任务排序和移动
     *
     * @param dto
     * @param user
     * @return
     */
    Boolean sort(ProjectTaskSortDTO dto, UserDto user);

    /**
     * 任务执行者指派 批量
     *
     * @param dto
     * @param user
     * @return
     */
    Boolean assignExecutorBatch(ProjectTaskAssignExecutorBatchDTO dto, UserDto user);

    /**
     * 根据任务列表id 删除任务
     *
     * @param mouldId
     * @param user
     * @return
     */
    Boolean taskRecycleBatch(String mouldId, UserDto user);

    /**
     * 任务的 表格视图
     *
     * @param req
     * @param user
     * @return
     */
    List<ProjectTaskTreeMouldVO> taskTree(ProjectTaskReq req, UserDto user);

    /**
     * 根据标签查询该标签的任务
     *
     * @param dto
     * @param user
     * @return
     */
    List<ProjectTaskDetailVO> getListByTag(ProjectTaskTag dto, UserDto user);

    /**
     * 保存导入的任务
     *
     * @param taskList
     * @param login
     * @param projectId
     */
    void saveImportList(List<ProjectTask> taskList, UserDto login, String projectId);

    /**
     * 任务复制
     *
     * @param dto
     * @param currentUser
     * @return
     */
    ProjectTask taskCopy(TaskCopyDTO dto, UserDto currentUser);

    /**
     * 任务移动
     *
     * @param dto
     * @param currentUser
     * @return
     */
    ProjectTask taskMove(TaskMoveDTO dto, UserDto currentUser);

    /**
     * 根据时间 查询任务的新增趋势图
     *
     * @param dto
     * @return
     */
    ProjectTaskCountForDateVO taskDateCount(TaskDateCountReq dto);

    /**
     * 根据项目id 查询项目下任务的状态和对应的条数
     *
     * @param dto
     * @return
     */
    Map<String, Integer> getProjectStatus(ProjectTask dto);

    /**
     * 取消关联文件
     *
     * @param fileInfo
     * @param currentUser
     * @return
     */
    Boolean cancelRelationFile(ProjectFileInfo fileInfo, UserDto currentUser);

    /**
     * 填充任务列表查询条件
     *
     * @param wrapper
     * @param req
     * @return
     */
    LambdaQueryWrapper<ProjectTask> fillTaskTreeWrapper(LambdaQueryWrapper<ProjectTask> wrapper, ProjectTaskReq req);
}
