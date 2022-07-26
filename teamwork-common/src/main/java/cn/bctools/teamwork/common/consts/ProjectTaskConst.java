package cn.bctools.teamwork.common.consts;

import cn.bctools.common.exception.BusinessException;
import lombok.Data;

/**
 * 项目任务中的常量
 *
 * @author admin
 */
@Data
public class ProjectTaskConst {
    /**
     * 任务列表显示任务等级
     */
    public final static String TOP_TASK_PID = "-1";

    /**
     * 任务优先级--非常紧急
     */
    public static final String VERY_URGENT_STR = "非常紧急";
    public static final String VERY_URGENT = "2";

    /**
     * 任务优先级--紧急
     */
    public static final String URGENT_STR = "紧急";
    public static final String URGENT = "1";

    /**
     * 任务优先级--普通
     */
    public static final String GENERAL_STR = "普通";
    public static final String GENERAL = "0";

    /**
     * 任务执行状态-未开始
     */
    public static final String NOT_STARTED_STR = "未开始";
    public static final String NOT_STARTED = "0";
    /**
     * 任务执行状态-已完成
     */
    public static final String COMPLETED_STR = "已完成";
    public static final String COMPLETED = "1";
    /**
     * 任务执行状态-进行中
     */
    public static final String IN_PROGRESS_STR = "进行中";
    public static final String IN_PROGRESS = "2";
    /**
     * 任务执行状态-挂起
     */
    public static final String HANG_UP_STR = "挂起";
    public static final String HANG_UP = "3";


    /**
     * 任务执行状态-测试中
     */
    public static final String UNDER_TEST_STR = "测试中";
    public static final String UNDER_TEST = "4";


    public static String transPriNumToStr(String priorityLevel) {
        switch (priorityLevel) {
            case GENERAL:
                return GENERAL_STR;
            case URGENT:
                return URGENT_STR;
            case VERY_URGENT:
                return VERY_URGENT_STR;
            default:
                throw new BusinessException("发现未知等级！");
        }
    }
    public static String transPriStrToNum(String priorityLevel) {
        switch (priorityLevel) {
            case GENERAL_STR:
                return GENERAL;
            case URGENT_STR:
                return URGENT;
            case VERY_URGENT_STR:
                return VERY_URGENT;
            default:
                throw new BusinessException("发现未知等级！");
        }
    }

    public static String transExecuteNumToStr(String executeStatus) {
        switch (executeStatus) {
            case NOT_STARTED:
                return NOT_STARTED_STR;
            case COMPLETED:
                return COMPLETED_STR;
            case IN_PROGRESS:
                return IN_PROGRESS_STR;
            case HANG_UP:
                return HANG_UP_STR;
            case UNDER_TEST:
                return URGENT_STR;
            default:
                throw new BusinessException("发现未知状态！");
        }
    }

    public static String transExecuteStrToNum(String executeStatusStr) {
        switch (executeStatusStr) {
            case NOT_STARTED_STR:
                return NOT_STARTED;
            case COMPLETED_STR:
                return COMPLETED;
            case IN_PROGRESS_STR:
                return IN_PROGRESS;
            case HANG_UP_STR:
                return HANG_UP;
            case URGENT_STR:
                return URGENT;
            default:
                throw new BusinessException("发现未知状态！");
        }
    }
}
