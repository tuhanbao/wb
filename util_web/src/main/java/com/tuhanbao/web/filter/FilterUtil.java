package com.tuhanbao.web.filter;

import com.tuhanbao.io.base.Constants;
import com.tuhanbao.io.objutil.StringUtil;
import com.tuhanbao.util.db.table.Column;
import com.tuhanbao.web.filter.operator.Operator;

public class FilterUtil {

    /**
     * 获取传入sql语句中的like语句中的关键词
     * @param word
     * @return
     */
    public static String getLikeWord(String word) {
        StringBuilder sb = new StringBuilder();
        sb.append(Constants.PERCENT_SIGN).append(word).append(Constants.PERCENT_SIGN);
        return sb.toString();
    }

    /**
     * 获取传入sql语句中的like语句中的关键词并且加入到Filter中
     * 
     * 自动默认两侧加上 %
     * @param column
     * @param filter
     * @param word
     */
    public static void addLikeWordToFilter(Column column, Filter filter, String word) {
        if (!StringUtil.isEmpty(word)) {
            StringBuilder sb = new StringBuilder();
            sb.append(Constants.PERCENT_SIGN).append(word).append(Constants.PERCENT_SIGN);
            filter.andFilter(Operator.LIKE, column, sb.toString());
        }
    }
    
    /**
     * 获取传入sql语句中的like语句中的关键词并且加入到Filter中
     * 
     * 自动默认两侧加上 %
     * @param column
     * @param filter
     * @param word
     */
    public static void addLikeWordToFilterDIY(Column column, Filter filter, String word) {
        if (!StringUtil.isEmpty(word)) {
            filter.andFilter(Operator.LIKE, column, word);
        }
    }
}
