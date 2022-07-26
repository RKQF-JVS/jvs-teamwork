package cn.bctools.teamwork.common.util;

import java.util.ArrayList;
import java.util.List;

/**
 * 分页工具
 * @author admin
 */
public class PageUtil {
    public PageUtil() {
    }

    /**
     * 这是一个集合分页的处理方法
     * @param list 原来的集合
     * @param pageNum 页码
     * @param pageSize 一页的数量
     * @param <T>
     * @return 分页后的数据
     */
    public static <T> List<T> startPage(List<T> list, Long pageNum, Long pageSize) {
        if (list == null) {
            return new ArrayList<>();
        } else if (list.size() == 0) {
            return new ArrayList<>();
        } else {
            Long count = (long)list.size();
            Long pageCount = 0L;
            if (count % pageSize == 0L) {
                pageCount = count / pageSize;
            } else {
                pageCount = count / pageSize + 1L;
            }

            Long fromIndex = 0L;
            Long toIndex = 0L;
            if (pageNum == null || pageNum < 1L) {
                pageNum = 1L;
            }

            if (pageNum > pageCount) {
                pageNum = pageCount;
            }

            if (!pageNum.equals(pageCount)) {
                fromIndex = (pageNum - 1L) * pageSize;
                toIndex = fromIndex + pageSize;
            } else {
                fromIndex = (pageNum - 1L) * pageSize;
                toIndex = count;
            }

            List pageList = list.subList(fromIndex.intValue(), toIndex.intValue());
            return pageList;
        }
    }
}
