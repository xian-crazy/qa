package com.quvideo.qa.common.tools;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by guoli on 2019/2/13.
 */
public class DateUtils {
    /**
     * 获取
     *
     * @param cur 初始时间
     * @param n  增加的时间
     * @return  Date对象
     */

    public Date getNewDate(Date cur, int n) {
        Calendar c = Calendar.getInstance();
        c.setTime(cur);   //设置时间
        c.add(Calendar.HOUR_OF_DAY, n); //日期分钟加1,Calendar.DATE(天),Calendar.HOUR(小时)
        Date date = c.getTime(); //结果
        return date;
    }

    public static String nowTime() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");//设置日期格式
        return (df.format(new Date()));// new Date()为获取当前系统时间
    }

    public static String date2String(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(date);
    }

    public static Date string2Date(String dateString) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            return sdf.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Date dateAddDay(Date date, int day) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.DAY_OF_MONTH, day);
        return c.getTime();
    }

    /**
     * 通过时间秒毫秒数判断两个时间的间隔
     *
     * @param date1  时间1
     * @param date2   时间2
     * @return   间隔时间
     */
    public static int differentDaysByMillisecond(Date date1, Date date2) {
        int days = (int) ((date2.getTime() - date1.getTime()) / (1000 * 3600 * 24));
        return days;
    }

    public static String time(int year, int month, int day, int h, int m, int s) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
        Calendar c = Calendar.getInstance();
        c.set(year, month, day, h, m, s);
        return (df.format(c.getTime()));
    }

    /**
     * @param c      = Calendar.getInstance();
     * @param format "yyyy-MM-dd HH:mm:ss"
     * @return    时间字符串
     */
    public static String time(Calendar c, String format) {//
        SimpleDateFormat df = new SimpleDateFormat(format);//设置日期格式
        return (df.format(c.getTime()));
    }

    /**
     * 将指日期转为固定格式的字符串日期，日期类型为String
     *
     * @param date  data对象实例
     * @param dateFormat  格式化字符串
     * @return   时间字符串
     */
    public static String formatDateToString(Date date, DateformatEnum dateFormat) {
        if (null == date) {
            return "";
        }
        String strDate = null;
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat.getFormat());
        strDate = sdf.format(date);
        return strDate;
    }

    /**
     * 将指日期转为固定格式的字符串日期，日期类型为String
     *
     * @param date  date实例
     * @param dateFormat   格式化字符串
     * @return   tolong值
     */
    public static long formatDateTolong(Date date, DateformatEnum dateFormat) {
        if (null == date) {
            return 0;
        }
        String strDate = null;
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat.getFormat());
        strDate = sdf.format(date);
        return Long.parseLong(strDate);
    }

    /**
     * 将字符串类型日期转为日期格式
     *
     * @param strDate  data字符串
     * @param dateFormat   格式
     * @return  Date对象
     */
    public static Date formatStringToDate(String strDate, DateformatEnum dateFormat) {
        Date d = null;
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat.getFormat());
        try {
            d = sdf.parse(strDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return d;
    }

    public static long parsetimeToMills(String strDate) {
        long d = 0;
        try {
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            d = sdf.parse(strDate).getTime();

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return d;
    }

}
