package cn.bctools.teamwork.common.enums;

/**
 * 文件关联的类型
 * @author admin
 */
public enum FileRelationTypeEnum {
    TASK_TYPE("task", "任务关联的文件"),
    PROJECT_UPLOAD("project", "项目上传的文件");

    String code;
    String remark;

    FileRelationTypeEnum(String code, String remark) {
        this.code = code;
        this.remark = remark;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return remark;
    }
}
