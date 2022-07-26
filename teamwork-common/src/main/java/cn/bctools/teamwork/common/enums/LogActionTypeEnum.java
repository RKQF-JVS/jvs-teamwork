package cn.bctools.teamwork.common.enums;

/**
 * 日志的场景类型
 *
 * @author admin
 */
public enum LogActionTypeEnum {
    /**
     * 项目日志
     */
    PROJECT("project", "项目日志"),
    /**
     * 任务日志
     */
    TASK("task", "任务日志");
    String code;
    String remark;

    LogActionTypeEnum(String code, String remark) {
        this.code = code;
        this.remark = remark;
    }

    public String getCode() {
        return code;
    }

    public String getRemark() {
        return remark;
    }
}
