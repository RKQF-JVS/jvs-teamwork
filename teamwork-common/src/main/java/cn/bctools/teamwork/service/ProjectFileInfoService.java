package cn.bctools.teamwork.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.teamwork.entity.ProjectFileInfo;

/**
 * @author Auto Generator
 */
public interface ProjectFileInfoService extends IService<ProjectFileInfo> {

    /**
     * 文件移入回收站
     * @param fileId
     * @param user
     * @return
     */
    Boolean fileRecycle(String fileId, UserDto user);

    /**
     * 查询回收站内的文件
     * @param page
     * @param projectId
     * @return
     */
    Page<ProjectFileInfo> listRecycle(Page<ProjectFileInfo> page, String projectId);

    /**
     * 恢复回收站的文件
     * @param fileId
     * @param user
     * @return
     */
    Boolean fileRecovery(String fileId, UserDto user);

    /**
     * 彻底删除回收站内的文件
     * @param fileId
     * @param user
     * @return
     */
    Boolean del(String fileId, UserDto user);
}
