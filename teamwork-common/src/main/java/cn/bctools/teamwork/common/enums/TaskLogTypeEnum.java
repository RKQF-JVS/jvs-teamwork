package cn.bctools.teamwork.common.enums;

/**
 * 这是项目任务的日志类型
 * @author admin
 */

public enum TaskLogTypeEnum {
    CREATE("create","创建了任务"),
    NAME("edit","更新了内容"),
    MOVE("move","将任务从 %s 移动到 %s"),
    CONTENT("content","更新了备注"),
    CLEAR_CONTENT("clearContent","清空了备注"),
    /**
     * 完成任务，版本库改变
     */
    DONE("done","完成了任务"),
    /**
     * 重做任务，版本库改变
     */
    REDO("redo","重做了任务"),
    CREATE_CHILD("createChild","添加了子任务 %s"),
    DONE_CHILD("doneChild","完成了子任务 %s"),
    REDO_CHILD("redoChild","重做了子任务 %s"),
    CLAIM("claim","认领了任务"),
    ASSIGN("assign","指派给了 %s"),
    PRI("pri","更新任务优先级为 %s"),
    STATUS("status","修改执行状态为 %s"),
    REMOVE_EXECUTOR("removeExecutor","移除了执行者"),
    CHANGE_STATE("changeState","将任务移动到 %s"),
    INVITE_MEMBER("inviteMember","添加了参与者 %s"),
    REMOVE_MEMBER("removeMember","移除了参与者 %s"),
    SET_BEGIN_TIME("setBeginTime","更新开始时间为 %s"),
    CLEAR_BEGIN_TIME("clearBeginTime","清除了开始时间 "),
    SET_END_TIME("setEndTime","更新截止时间为 %s"),
    CLEAR_END_TIME("clearEndTime","清除了截止时间"),
    RECYCLE("recycle","把任务移到了回收站"),
    RECOVERY("recovery","恢复了任务"),
    SET_WORK_TIME("setWorkTime","更新预估工时为 %s"),
    LINK_FILE("linkFile","关联了文件"),
    UNLINK_FILE("unlinkFile","取消关联文件"),
    /**
     * 评论
     */
    COMMENT("comment","");




    String code;
    String remark;

    TaskLogTypeEnum(String code, String remark) {
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
