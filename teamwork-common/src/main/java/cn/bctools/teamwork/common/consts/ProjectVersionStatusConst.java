package cn.bctools.teamwork.common.consts;

import cn.bctools.common.exception.BusinessException;

/**
 * 版本库版本
 *
 * @author admin
 */
public class ProjectVersionStatusConst {
    public final static int NOT_STARTED = 0;
    public final static String NOT_STARTED_STR = "未开始";
    public final static int IN_PROGRESS = 1;
    public final static String IN_PROGRESS_STR = "进行中";
    public final static int DELAYED_RELEASE = 2;
    public final static String DELAYED_RELEASE_STR = "延期发布";
    public final static int RELEASE = 3;
    public final static String RELEASE_STR = "已发布";

    /**
     * 执行状态值 转为执行状态字符串
     * @param code
     * @return
     */
    public static String transNumToStr(int code){
        switch (code){
            case NOT_STARTED:
                return  NOT_STARTED_STR;
            case IN_PROGRESS:
                return IN_PROGRESS_STR;
            case DELAYED_RELEASE:
                return DELAYED_RELEASE_STR;
            case RELEASE:
                return  RELEASE_STR;
            default:
                throw new BusinessException("转换版本状态出错！");
        }
    }
    /**
     * 执行状态字符串 转为执行状态值
     * @param str
     * @return
     */
    public static Integer transStrToNum(String str){
        switch (str){
            case NOT_STARTED_STR:
                return NOT_STARTED;
            case IN_PROGRESS_STR:
                return IN_PROGRESS;
            case DELAYED_RELEASE_STR:
                return  DELAYED_RELEASE;
            case RELEASE_STR:
                return RELEASE;
            default:
                throw new BusinessException("转换版本状态出错！");
        }
    }
}
