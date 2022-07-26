package cn.bctools.teamwork.common.enums;

/**
 * 这是一个项目查询类型的枚举
 *
 * @author admin
 */

public enum SelectTypeEnum {

    MY("1", "我的项目"),
    COLLECT("2", "我的收藏"),
    ARCHIVE("3", "已归档"),
    DELETE("4", "回收站");

    String code;
    String name;

     SelectTypeEnum(String code, String name) {
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
