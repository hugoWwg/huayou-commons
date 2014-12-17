package com.huayou.commons.util;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.util.Date;

/**
 * @author wangweiguang133
 */
public class DateUtils {

    public static final String yyyyMMdd2 = "yyyy-MM-dd";
    public static final String yyyyMMdd3 = "yyyy/MM/dd";
    public static final String yyyyMMdd = "yyyyMMdd";
    public static final String MMdd = "MM-dd";
    public static final String yyyy_MM_dd_HH_mm_ss = "yyyy-MM-dd HH:mm:ss";
    public static final String yyyy_MM_dd_HH_mm_ss_SSS = "yyyy-MM-dd HH:mm:ss.SSS";

    public static DateTime parse(String date, String formatter) {
        return DateTime.parse(date, DateTimeFormat.forPattern(formatter));
    }

    public static String format(Date date, String formatter) {
        return new DateTime(date.getTime()).toString(formatter);
    }

    public static Date startOf(Date day) {
        return new DateTime(day).withTimeAtStartOfDay().toDate();
    }

    public static Date endOf(Date day) {
        return new DateTime(day).millisOfDay().withMaximumValue().toDate();
    }

    public static DateTime startOf(DateTime day) {
        return day.millisOfDay().withMinimumValue();
    }

    public static DateTime endOf(DateTime day) {
        return day.millisOfDay().withMaximumValue();
    }

    public static Date startOf2(DateTime day) {
        return day.millisOfDay().withMinimumValue().toDate();
    }

    public static Date endOf2(DateTime day) {
        return day.millisOfDay().withMaximumValue().toDate();
    }

    public static DateTime startOfYesterday() {
        return startOfNDaysBefore(1);
    }

    public static DateTime endOfYesterday() {
        return endOfNDaysBefore(1);
    }

    public static DateTime startOfNDaysBefore(int n) {
        return DateTime.now().minusDays(n).withTimeAtStartOfDay();
    }

    public static DateTime endOfNDaysBefore(int n) {
        return DateTime.now().minusDays(n).secondOfDay().withMaximumValue();
    }

    public static Date startOfYesterday2() {
        return startOfNDaysBefore(1).toDate();
    }

    public static Date endOfYesterday2() {
        return endOfNDaysBefore(1).toDate();
    }

    public static Date startOfNDaysBefore2(int n) {
        return DateTime.now().minusDays(n).withTimeAtStartOfDay().toDate();
    }
    //几天以前的最后时刻
    public static Date endOfNDaysBefore2(int n) {
        return DateTime.now().minusDays(n).secondOfDay().withMaximumValue().toDate();
    }

    //几天以后的最后时刻
    public static Date endOfNDaysAfter2(int n) {
        return DateTime.now().plusDays(n).secondOfDay().withMaximumValue().toDate();
    }

    //几天以后的时间
    public static Date daysAfter(int n) {
        return DateTime.now().plusDays(n).secondOfDay().getDateTime().toDate();
    }
}

