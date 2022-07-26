package cn.bctools.teamwork.service.impl;

import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.teamwork.common.enums.LogActionTypeEnum;
import cn.bctools.teamwork.common.enums.ProjectLogTypeEnum;
import cn.bctools.teamwork.common.enums.TaskLogTypeEnum;
import cn.bctools.teamwork.dto.ProjectLogAddBO;
import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.bctools.teamwork.entity.ProjectLog;
import cn.bctools.teamwork.mapper.ProjectLogMapper;
import cn.bctools.teamwork.service.ProjectLogService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Auto Generator
 */
@Service
public class ProjectLogServiceImpl extends ServiceImpl<ProjectLogMapper, ProjectLog> implements ProjectLogService {
    public static final String FORMAT_MARK = "%s";

    @Override
    @Transactional(rollbackFor = Exception.class)
    @Deprecated
    public void runLog(ProjectLog log) {
        this.save(log);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void runLog(ProjectLogAddBO logParam, UserDto loginUser) {
        runLog(logParam,loginUser,false);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProjectLog runLog(ProjectLogAddBO logParam, UserDto loginUser,boolean returnFlag) {
        LogActionTypeEnum actionTypeEnum = logParam.getLogActionTypeEnum();
        String[] formatContent = logParam.getFormatContent();
        ProjectLog projectLog = new ProjectLog();
        BeanUtil.copyProperties(logParam, projectLog);
        projectLog.initSave(loginUser);
        projectLog.setMemberId(loginUser.getId());
        //操作内容
        String enumRemark = "";
        String type = "";
        String actionType = actionTypeEnum.getCode();
        projectLog.setActionType(actionType);
        //任务动态和项目动态区分
        switch (actionTypeEnum) {
            case TASK:
                TaskLogTypeEnum taskLogTypeEnum = logParam.getTaskLogTypeEnum();
                enumRemark = taskLogTypeEnum.getRemark();
                type = taskLogTypeEnum.getCode();
                break;
            case PROJECT:
                ProjectLogTypeEnum projectLogTypeEnum = logParam.getProjectLogTypeEnum();
                enumRemark = projectLogTypeEnum.getRemark();
                type = projectLogTypeEnum.getCode();
                break;
            default:
        }
        String remark = formatRemark(enumRemark, formatContent);
        projectLog.setRemark(remark);
        projectLog.setType(type);
        if(returnFlag){
            return projectLog;
        }
        this.save(projectLog);
        return null;
    }

    public static String formatRemark(String enumRemark, String[] args) {
        //是否需要格式化
        if (enumRemark.contains(FORMAT_MARK)) {
            return String.format(enumRemark, args);
        }
        return enumRemark;
    }

}
