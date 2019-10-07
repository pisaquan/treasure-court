package com.sancai.oa.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 日期工具类
 *
 * @author fanjing
 * @date 2019/8/23
 */
public class DateCalUtil {
    /**
     * 根据考勤查询月份计算一共有多少天
     *
     * @param time 查询月份
     * @return 返回该月份对应的天数
     */
    public static Integer getDaysByMonth(String time) {
        //计算给定月份有多少天
        String year = time.split("-")[0];
        String month = time.split("-")[1];
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, Integer.parseInt(year));
        cal.set(Calendar.MONTH, Integer.parseInt(month) - 1);
        Integer days = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        return days;
    }

    /**
     * 日期转星期
     *
     * @param datetime 传入”2019-8-23"格式的时间
     * @return 返回 一 或 二 ……
     */
    public static String dateToWeek(String datetime) {
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd");
        String[] weekDays = {"日", "一", "二", "三", "四", "五", "六"};
        // 获得一个日历
        Calendar cal = Calendar.getInstance();
        Date datet = null;
        try {
            datet = f.parse(datetime);
            cal.setTime(datet);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        // 指示一个星期中的某天。
        int w = cal.get(Calendar.DAY_OF_WEEK) - 1;
        if (w < 0) {
            w = 0;
        }
        return weekDays[w];
    }

    /**
     * 计算某月第一天和最后一天的毫秒值
     * @param year 例：2019
     * @param month 例：9
     * @return 返回map，firstDayLongTime和lastDayLongTime
     */
    public static Map calFirstLastLongTime(int year,int month){
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR,year);
        calendar.set(Calendar.MONTH,month-1);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        //将小时至0
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        //将分钟至0
        calendar.set(Calendar.MINUTE, 0);
        //将秒至0
        calendar.set(Calendar.SECOND,0);
        //将毫秒至0
        calendar.set(Calendar.MILLISECOND, 0);
        //获得当前月第一天
        long firstLongTime = calendar.getTimeInMillis();
        //将当前月加1；
        calendar.add(Calendar.MONTH, 1);
        //在当前月的下一月基础上减去1毫秒
        calendar.add(Calendar.MILLISECOND, -1);
        //获得当前月最后一天
        long lastLongTime = calendar.getTimeInMillis();
        Map<String, Long> map = new HashMap<>();
        map.put("firstDayLongTime",firstLongTime);
        map.put("lastDayLongTime",lastLongTime);
        return map;
    }

}
