package com.sancai.oa.utils;

import com.sancai.oa.report.entity.enums.ReportRecordTypeEnum;
import javafx.util.Pair;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.expression.spel.ast.NullLiteral;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.*;


/**
 *  @author fans
 * @Date 2019/7/24
 * @Description LocalDateTimeUtils is used to Java8中的时间类
 */
public class LocalDateTimeUtils {


    /**获取当前时间的LocalDateTime对象**/
    /**LocalDateTime.now();**/

    /**根据年月日构建LocalDateTime**/
    /**LocalDateTime.of();

     /**比较日期先后**/
    /**
     * LocalDateTime.now().isBefore(),
     * /**LocalDateTime.now().isAfter(),
     * <p>
     * /**Date转换为LocalDateTime
     **/
    public static LocalDateTime convertDateToLDT(Date date) {
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }

    /**
     * LocalDateTime转换为Date
     **/
    public static Date convertLDTToDate(LocalDateTime time) {
        return Date.from(time.atZone(ZoneId.systemDefault()).toInstant());
    }


    /**
     * 获取指定日期的毫秒
     **/
    public static Long getMilliByTime(LocalDateTime time) {
        return time.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    /**
     * 获取指定日期的秒
     **/
    public static Long getSecondsByTime(LocalDateTime time) {
        return time.atZone(ZoneId.systemDefault()).toInstant().getEpochSecond();
    }

    /**
     * 获取指定时间的指定格式
     **/
    public static String formatTime(LocalDateTime time, String pattern) {
        return time.format(DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * 获取当前时间的指定格式
     **/
    public static String formatNow(String pattern) {
        return formatTime(LocalDateTime.now(), pattern);
    }

    /**
     * 日期加上一个数,根据field不同加不同值,field为ChronoUnit.*
     **/
    public static LocalDateTime plus(LocalDateTime time, long number, TemporalUnit field) {
        return time.plus(number, field);
    }

    /**
     * 日期减去一个数,根据field不同减不同值,field参数为ChronoUnit.*
     **/
    public static LocalDateTime minu(LocalDateTime time, long number, TemporalUnit field) {
        return time.minus(number, field);
    }

    /**
     * 获取两个日期的差  field参数为ChronoUnit.*
     *
     * @param startTime
     * @param endTime
     * @param field     单位(年月日时分秒)
     * @return fans
     */
    public static long betweenTwoTime(LocalDateTime startTime, LocalDateTime endTime, ChronoUnit field) {
        Period period = Period.between(LocalDate.from(startTime), LocalDate.from(endTime));
        if (field == ChronoUnit.YEARS) {
            return period.getYears();
        }
        if (field == ChronoUnit.MONTHS) {
            return period.getYears() * 12 + period.getMonths();
        }
        return field.between(startTime, endTime);
    }

    /**
     * 获取一天的开始时间，2017,7,22 00:00
     **/
    public static LocalDateTime getDayStart(LocalDateTime time) {
        return time.withHour(0)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);
    }

    /**
     * 获取一天的结束时间，2017,7,22 23:59:59.999999999
     **/
    public static LocalDateTime getDayEnd(LocalDateTime time) {
        return time.withHour(23)
                .withMinute(59)
                .withSecond(59)
                .withNano(999999999);
    }

    /**
     * 将long类型的timestamp转为LocalDateTime
     **/
    public static LocalDateTime getDateTimeOfTimestamp(long timestamp) {
        Instant instant = Instant.ofEpochMilli(timestamp);
        ZoneId zone = ZoneId.systemDefault();
        return LocalDateTime.ofInstant(instant, zone);
    }

    /**
     * 获取一天的开始时间(long类型时间戳)，2017,7,22 00:00
     **/
    public static long getDayStart(long time) {
        LocalDateTime timeLocal = getDateTimeOfTimestamp(time);
        return Timestamp.valueOf(getDayStart(timeLocal)).getTime();
    }

    /**
     * 获取一天的结束时间（long类型时间戳），2017,7,22 23:59:59.999999999
     **/
    public static long getDayEnd(long time) {
        LocalDateTime timeLocal = getDateTimeOfTimestamp(time);
        return Timestamp.valueOf(getDayEnd(timeLocal)).getTime();
    }

    /**
     * 将String类型的时间戳timestamp转为LocalDateTime
     **/
    public static LocalDateTime getDateTimeOfStringTimestamp(String times) {
        Instant instant = Instant.ofEpochMilli(Long.valueOf(times));
        ZoneId zone = ZoneId.systemDefault();
        return LocalDateTime.ofInstant(instant, zone);
    }

    /**
     * 获取当天的开始时间
     **/
    public static Date getDayBegin() {
        Calendar cal = new GregorianCalendar();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }


    /**
     * 获取当天的结束时间
     **/
    public static Date getDayEnd() {
        Calendar cal = new GregorianCalendar();
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        return cal.getTime();
    }

    /**
     * 系统时间前一天开始时间
     **/
    public static long getBeforeDayStart() {
        Calendar cal = new GregorianCalendar();
        cal.setTime(getDayBegin());
        cal.add(Calendar.DAY_OF_MONTH, -1);
        return cal.getTime().getTime();
    }

    /**
     * 系统时间前一天结束时间
     **/
    public static long getBeforeDayeEnd() {
        Calendar cal = new GregorianCalendar();
        cal.setTime(getDayEnd());
        cal.add(Calendar.DAY_OF_MONTH, -1);
        return cal.getTime().getTime();
    }

    /**
     * 判断当前日期是否在某个区间
     *
     * @param nowTimes
     * @param startTimes
     * @param endTimes
     * @return boolean
     */
    public static boolean isEffectiveDate(long nowTimes, long startTimes, long endTimes) {
        Date nowTime = new Date(nowTimes);
        Date startTime = new Date(startTimes);
        Date endTime = new Date(endTimes);
        if (nowTime.getTime() == startTime.getTime() || nowTime.getTime() == endTime.getTime()) {
            return true;
        }
        Calendar date = Calendar.getInstance();
        date.setTime(nowTime);

        Calendar begin = Calendar.getInstance();
        begin.setTime(startTime);

        Calendar end = Calendar.getInstance();
        end.setTime(endTime);

        if (date.after(begin) && date.before(end)) {
            return true;
        } else {
            return false;
        }
    }
    /**
     * 判断当前日期是否在某个区间并且返回具体之前还是之后
     * @param nowTimes
     * @param startTimes
     * @param endTimes
     * @return String = null 在区间内
     * String = AFTER 之前
     * String = BEFORE 之后
     */
    public static String isAfterOrBeforeDate(long nowTimes, long startTimes, long endTimes) {
        Date nowTime = new Date(nowTimes);
        Date startTime = new Date(startTimes);
        Date endTime = new Date(endTimes);
        if (nowTime.getTime() == startTime.getTime() || nowTime.getTime() == endTime.getTime()) {
            return null;
        }
        Calendar date = Calendar.getInstance();
        date.setTime(nowTime);

        Calendar begin = Calendar.getInstance();
        begin.setTime(startTime);

        Calendar end = Calendar.getInstance();
        end.setTime(endTime);

        if (date.after(begin) && date.before(end)) {
            return null;
        } else if(date.after(begin)) {
            return ReportRecordTypeEnum.AFTER.getKey();
        } else {
            return ReportRecordTypeEnum.BEFORE.getKey();
        }
    }
    /**
     * 转换为时间（年-月-日 时:分:秒）
     *
     * @param timeMillis
     * @return String
     */
    public static String formatDateTime(long timeMillis) {
        return formatDateTime(timeMillis, "yyyy-MM-dd HH:mm:ss");
    }

    /**
     * 转换为时间（年-月-日）
     *
     * @param timeMillis
     * @return String
     */
    public static String formatDateTimeByYmd(long timeMillis) {
        return new SimpleDateFormat("yyyy-MM-dd").format(timeMillis);
    }
    /**
     * 得到日期字符串 默认格式（yyyy-MM-dd） pattern可以为："yyyy-MM-dd" "HH:mm:ss" "E"
     */
    public static String formatDate(Date date, Object... pattern) {
        String formatDate = null;
        if (pattern != null && pattern.length > 0) {
            formatDate = DateFormatUtils.format(date, pattern[0].toString());
        } else {
            formatDate = DateFormatUtils.format(date, "yyyy-MM-dd");
        }
        return formatDate;
    }

    /**
     * 得到日期时间字符串，转换格式（yyyy-MM-dd HH:mm:ss）
     */
    public static String formatDateTime(Date date) {
        return formatDate(date, "yyyy-MM-dd HH:mm:ss");
    }

    /**
     * 得到当前时间字符串 格式（HH:mm:ss）
     */
    public static String getTimeHms(long timeMillis) {
        return new SimpleDateFormat( "HH:mm:ss").format(timeMillis);
    }
    /**
     * 得到当前时间字符串 格式（yyyy-MM-dd  HH:mm:00）
     */
    public static String getTimeYmdHm(long timeMillis) {
        return new SimpleDateFormat( "yyyy-MM-dd HH:mm:00").format(timeMillis);
    }
    /**
     * 得到当前时间字符串 格式（HH:mm:ss）
     */
    public static String getTime() {
        return formatDate(new Date(), "yyyy-MM-dd");
    }
    /**
     * 将字符串转日期成Long类型的时间戳，格式为：yyyy-MM-dd HH:mm:ss
     */
    public static Long convertTimeToLong(String time) {
        DateTimeFormatter ftf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime parse = LocalDateTime.parse(time, ftf);
        return LocalDateTime.from(parse).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    public static void main(String[] args) {
        /*long getDayStart = 1564454989000L;
        long getDayEnd = 1564559102000L;
        System.out.println("之前" + formatDateTime(getDayStart));
        System.out.println("之后" + formatDateTime(LocalDateTimeUtils.getDayStart(getDayStart)));
        System.out.println("之前" + formatDateTime(getDayEnd));
        System.out.println("之后" + formatDateTime(LocalDateTimeUtils.getDayEnd(getDayEnd)));
        System.out.println(getTime()+" "+getTimeHms(1564559102000L));
        System.out.println(convertTimeToLong("2019-07-31 15:45:02"));
        System.out.println(formatDateTimeByYmd(LocalDateTimeUtils.getBeforeDayStart()));*/

        String begins = "2019-07-20 18:00:00";
        String finishs = "2019-07-20 20:00:00";
        String reportTimes = "2019-07-20 18:00:00";

        Long begin =  convertTimeToLong(begins);
        Long finish =  convertTimeToLong(finishs);
        Long reportTime =  convertTimeToLong(reportTimes);
        String timeIsNotValid = LocalDateTimeUtils.isAfterOrBeforeDate(reportTime, begin,finish);

        String times = "";
        if(ReportRecordTypeEnum.BEFORE.getKey().equals(timeIsNotValid)){
            times = ReportRecordTypeEnum.BEFORE.getValue()+LocalDateTimeUtils.getDistanceTime(begins,reportTimes);
        }
        if(ReportRecordTypeEnum.AFTER.getKey().equals(timeIsNotValid)){
            times = ReportRecordTypeEnum.AFTER.getValue()+LocalDateTimeUtils.getDistanceTime(finishs,reportTimes);
        }
        if(timeIsNotValid == null){
            times = "正常";
        }
        System.out.println(times);
    }

    /**
     * 转换为时间（自定义pattern）
     *
     * @param timeMillis
     * @return String
     */
    public static String formatDateTime(long timeMillis, String pattern) {
        return new SimpleDateFormat(pattern).format(timeMillis);
    }


    /**
     * 按指定间隔，把一段时间拆成N段，Pair的key和value分别是每段的开始和结束
     *
     * @param start
     * @param end
     * @param day
     * @return
     */
    public static List<Pair> getIntervalTimes(long start, long end, int day) {

        Instant startTimeInstant = Instant.ofEpochMilli(start);
        Instant endTimeInstant = Instant.ofEpochMilli(end);

        List<Pair> list = new ArrayList<>();

        while (startTimeInstant.isBefore(endTimeInstant)) {

            LocalDateTime startTime = LocalDateTime.ofInstant(startTimeInstant, ZoneId.systemDefault());
            LocalDateTime endTime = startTime.plusDays(day);
            Instant endInstant = endTime.toInstant(ZoneOffset.ofHours(8));
            startTimeInstant = endInstant;

            if (endInstant.isAfter(endTimeInstant)) {
                Pair p = new Pair(startTime.toInstant(ZoneOffset.ofHours(8)).toEpochMilli(), endTimeInstant.toEpochMilli());
                list.add(p);
            } else {
                Pair p = new Pair(startTime.toInstant(ZoneOffset.ofHours(8)).toEpochMilli(), endInstant.toEpochMilli() - 1);
                list.add(p);
            }
        }
        return list;

    }
    /**
     * 两个时间相差距离多少天多少小时多少分多少秒
     * @param str1 时间参数 1 格式：1990-01-01 12:00:00
     * @param str2 时间参数 2 格式：2009-01-01 12:00:00
     * @return String 返回值为：xx天xx小时xx分xx秒
     */
    public static String getDistanceTime(String str1, String str2){
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date one;
        Date two;
        long day = 0;
        long hour = 0;
        long min = 0;
        long sec = 0;
        try {
            one = df.parse(str1);
            two = df.parse(str2);
            long time1 = one.getTime();
            long time2 = two.getTime();
            long diff ;
            if(time1<time2) {
                diff = time2 - time1;
            } else {
                diff = time1 - time2;
            }
            day = diff / (24 * 60 * 60 * 1000);
            hour = (diff / (60 * 60 * 1000) - day * 24);
            min = ((diff / (60 * 1000)) - day * 24 * 60 - hour * 60);
            sec = (diff/1000-day*24*60*60-hour*60*60-min*60);
        } catch (ParseException e) {
            return "";
        }
        String days = "";
        String hours = "";
        String mins = "";
        String secs = "";
        if(day != 0){
            days = day + "天";
        }
        if(hour != 0){
            hours = hour + "小时";
        }
        if(min != 0){
            mins =  min + "分";
        }
        if(sec != 0){
            secs =  sec + "秒";
        }
        return days +  hours +  mins + secs;
    }


    /**
     * 根据时间获取当前月的所有天数
     * @param localDateTime
     * @return
     */
    public static List<LocalDate> getAllMonthDays(LocalDateTime localDateTime) {
        LocalDate localDate = localDateTime.toLocalDate();
        int days = localDate.lengthOfMonth();
        int year = localDate.getYear();
        int month = localDate.getMonthValue();

        String currentMonth = month < 10 ? ("0"+ month):String.valueOf(month);

        List<LocalDate> resultList = new ArrayList<>();
        for(int i=1;i<=days;i++) {
            String day;
            if(i<10) {
                day = String.valueOf(year)+"-"+currentMonth+"-0"+String.valueOf(i);
            }else {
                day = String.valueOf(year)+"-"+currentMonth+"-"+String.valueOf(i);
            }
            LocalDate MonthLocalDate = LocalDate.parse(day, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            resultList.add(MonthLocalDate);
        }
        return resultList;
    }

    /**
     * 根据开始结束时间计算天数
     * @param begin 开始时间参数 1 格式：2019-07-20 13:00:00
     * @param finish 完成时间参数 2 格式：2019-07-21 12:00:00
     * @return float 返回值为：1.0  天
     */
    public static float getDifferDateTime(String begin, String finish){
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date one;
        Date two;
        float day;
        float hour;
        try {
            one = df.parse(begin);
            two = df.parse(finish);
            long time1 = one.getTime();
            long time2 = two.getTime();
            long diff ;
            if(time1<time2) {
                diff = time2 - time1;
            } else {
                return 0f ;
            }
            day = diff / (24 * 60 * 60 * 1000);
            hour = (diff / (60 * 60 * 1000) - day * 24);
        } catch (ParseException e) {
            return 0f;
        }
        float days = 0f;
        if(day != 0){
            days = day;
        }
        if( hour < 9){
            days = days + 0.5f;
        } else {
            days = days + 1f;
        }
        return days ;
    }

}