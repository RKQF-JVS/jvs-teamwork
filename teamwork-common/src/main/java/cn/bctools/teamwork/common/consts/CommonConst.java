package cn.bctools.teamwork.common.consts;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 这是一个公共的静态常量
 *
 * @author admin
 */
public class CommonConst {
    public final static String DELETE_STATUS = "1";
    public final static String NO_DELETE_STATUS = "0";
    public final static String ALL = "all";
    public final static String COLLECT = "collect";
    public final static String CANCEL = "cancel";
    public final static String DATE_TIME = "yyyy-MM-dd HH:mm:ss";
    public final static String DATE = "yyyy-MM-dd";
    public final static String DATE_TIME_MINUTE = "yyyy-MM-dd HH:mm";
    public final static String PROJECT_RULE = "projectRule";
    public final static String TIME_BEFORE = " 00:00:00";
    public final static String TIME_AFTER = " 23:59:59";

    /**
     * LocalDateTime 转为 yyyy-MM-dd HH:mm:ss
     */
    public static String dateTimeToStr(LocalDateTime localDateTime, String pattern) {
        DateTimeFormatter fm = DateTimeFormatter.ofPattern(pattern);
        String format = localDateTime.format(fm);
        return format;
    }

}
