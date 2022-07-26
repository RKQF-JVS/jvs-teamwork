package cn.bctools.teamwork.common.enums;

/**
 * 收藏类型
 * @author admin
 */
public enum CollectionTypeEnum {
    TASK("task","任务收藏"),
    PROJECT("project","项目收藏");
    String code;
    String remark;
    CollectionTypeEnum(String code, String remark){
        this.code=code;
        this.remark=remark;
    }
    public String getCode() {
        return code;
    }

    public String getName() {
        return remark;
    }
}
