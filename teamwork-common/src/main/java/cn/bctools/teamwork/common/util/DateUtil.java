package cn.bctools.teamwork.common.util;


import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 这是一个时间操作的工具类
 *
 * @author admin
 */
public class DateUtil {

    public static final String PATTERN_DATE = "yyyy-MM-dd";
    public static final String PATTERN_TIME = "HH:mm:ss";
    public static final String PATTERN_HOUR_MINUTE = "HH:mm";
    public static final String PATTERN_DATETIME = "yyyy-MM-dd HH:mm:ss";
    public static final String PATTERN_DATETIME_MIN = "yyyy-MM-dd HH:mm";
    public static final String PATTERN_MONTH = "yyyy-MM";
    public static final String PATTERN_YEAR = "yyyy";
    public static final String PATTERN_MONTH_DAY = "MM-dd";
    private static final String PATTERN_FULL = "yyyy-MM-dd HH:mm:ss.SSS";

    /**
     * 获取两个 yyyy-MM-dd 之间所有的日期
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return
     */
    public static List<String> getDateStr(String startTime, String endTime) {
        DateTimeFormatter ofPattern = DateTimeFormatter.ofPattern(PATTERN_DATE);
        List<String> str = new ArrayList<>();
        LocalDate st = LocalDate.parse(startTime, ofPattern);
        while (true) {
            String s1 = st.format(ofPattern);
            str.add(s1);
            if (s1.equals(endTime)) {
                break;
            }
            st = st.plusDays(1);
        }
        return str;
    }
}
