package cn.bctools.teamwork.common.enums;

/**
 * 版本操作动态
 *
 * @author admin
 */
public enum ProjectVersionLogTypeEnum {
    CREATE("create","创建了版本"),
    STATUS("status","更新了状态为 %s"),
    ADD_TASK("addTask","添加了 % 项发布内容"),
    REMOVE_TASK("removeTask","移除了发布内容"),
    NAME("name","更新了版本名"),
    COMPLETE("complete","完成版本时间为 %s"),
    CONTENT("content","更新了备注"),
    CLEAR_CONTENT("clearContent","清空了备注"),
    SET_START_TIME("setStartTime","更新开始时间为 %s"),
    CLEAR_START_TIME("clearStartTime","清除了开始时间"),
    SET_PLAN_PUBLISH_TIME("setPlanPublishTime","更新计划发布时间为 %s"),
    CLEAR_PLAN_PUBLISH_TIME("clearPlanPublishTime","清除了计划发布时间"),
    REMOVE_VERSION("removeVersion","删除了版本 %s");



    String code;
    String name;

    ProjectVersionLogTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }
}
