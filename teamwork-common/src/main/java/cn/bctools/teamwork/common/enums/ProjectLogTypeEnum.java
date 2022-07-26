package cn.bctools.teamwork.common.enums;

/**
 * 项目任务动态日志
 *
 * @author admin
 */

public enum ProjectLogTypeEnum {
    CREATE("create", "创建了项目"),
    EDIT("edit", "编辑了项目"),
    NAME("name", "修改了项目名称"),
    CONTENT("content", "更新了备注"),
    CLEAR_CONTENT("clearContent", "清空了备注"),
    INVITE_MEMBER("inviteMember", "邀请 %s 加入了项目"),
    REMOVE_MEMBER("removeMember", "移除了成员 %s"),
    RECYCLE("recycle", "把项目移到了回收站"),
    RECOVERY("recovery", "恢复了项目"),
    ARCHIVE("archive", "归档了项目"),
    RECOVERY_ARCHIVE("recoveryArchive", "恢复了项目"),
    UPLOAD_FILE("uploadFile", "上传了文件"),
    DELETE_FILE("deleteFile", "删除了文件");
    String code;
    String remark;

    ProjectLogTypeEnum(String code, String remark) {
        this.code=code;
        this.remark=remark;
    }

    public String getCode() {
        return code;
    }

    public String getRemark() {
        return remark;
    }
}
