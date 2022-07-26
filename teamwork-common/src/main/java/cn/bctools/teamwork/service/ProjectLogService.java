package cn.bctools.teamwork.service;

import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.teamwork.dto.ProjectLogAddBO;
import com.baomidou.mybatisplus.extension.service.IService;
import cn.bctools.teamwork.entity.ProjectLog;

/**
 *
 * @author Auto Generator
 */
public interface ProjectLogService extends IService<ProjectLog> {
    /**
     * 保存项目操作的日志
     * @param log
     */
   void runLog(ProjectLog log);

    /**
     * 保存动态
     * @param logParam  动态参数
     * @param loginUser 当前用户
     */
    void runLog(ProjectLogAddBO logParam, UserDto loginUser);

    /**
     * 保存动态的方法
     * @param logParam  动态参数
     * @param loginUser 当前用户
     * @param returnFlag 是否返回信息，若为 false 直接保存
     * @return 返回需要动态信息
     */
    ProjectLog runLog(ProjectLogAddBO logParam, UserDto loginUser,boolean returnFlag);
}
