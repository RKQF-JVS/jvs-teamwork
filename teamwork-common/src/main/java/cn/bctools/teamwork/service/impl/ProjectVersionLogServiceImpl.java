package cn.bctools.teamwork.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.bctools.teamwork.entity.ProjectVersionLog;
import cn.bctools.teamwork.mapper.ProjectVersionLogMapper;
import cn.bctools.teamwork.service.ProjectVersionLogService;
import org.springframework.stereotype.Service;

/**
 * @author Auto Generator
 */
@Service
public class ProjectVersionLogServiceImpl extends ServiceImpl<ProjectVersionLogMapper, ProjectVersionLog> implements ProjectVersionLogService {

    @Override
    public Page<ProjectVersionLog> pageLog(Page<ProjectVersionLog> page, ProjectVersionLog dto) {
        lambdaQuery().eq(ProjectVersionLog::getSourceId, dto.getSourceId())
                .orderByAsc(ProjectVersionLog::getCreateTime)
                .page(page);
        return page;
    }
}
