package com.zfx.commonlib.util;

import android.content.Context;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * 作者:ht
 * 日期:2017/3/15.
 * 备注:
 */
public class DateUtils {

    /**
     * 当前时间 -> yyyy.MM.dd HH:mm
     */
    public static String getCurrentTime(String pattern) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        return sdf.format(new Date());
    }

    /**
     * 当前时间 -> 时间戳
     */
    public static String getCurrentTimeStamp() {
        String time = String.valueOf(System.currentTimeMillis() / 1000);
        return time;
    }

    /**
     * yyyy.MM.dd HH:mm->时间戳
     */
    public static String timeToStamp(String time, String pattern) {
        SimpleDateFormat sdr = new SimpleDateFormat(pattern, Locale.CHINA);
        Date date;
        String times = null;
        try {
            date = sdr.parse(time);
            long l = date.getTime();
            times = String.valueOf(l / 1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return times;
    }

    /**
     * yyyy.MM.dd HH:mm->时间戳
     */
    public static Long timeToStampLong(String time, String pattern) {
        SimpleDateFormat sdr = new SimpleDateFormat(pattern, Locale.CHINA);
        Date date;
        Long times = null;
        try {
            date = sdr.parse(time);
            long l = date.getTime();
            times = l / 1000;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return times;
    }

    /**
     * 时间戳->yyyy.MM.dd HH:mm
     */
    public static String stampToTime(String stamp, String pattern) {
        SimpleDateFormat sdr = new SimpleDateFormat(pattern);
        @SuppressWarnings("unused")
        long lcc = Long.valueOf(stamp);
        int i = Integer.parseInt(stamp);
        String times = sdr.format(new Date(i * 1000L));
        return times;
    }

    /**
     * 当前时间 +i 天
     * 返回 pattern
     */
    public static String getDays(int i) {
        Date date = new Date();//取时间
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        calendar.add(calendar.DATE, i);//把日期往后增加一天.整数往后推,负数往前移动
        date = calendar.getTime();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy.MM.dd");
        String data = formatter.format(date);
        return data;
    }

    /**
     * 指定时间 +i 天
     * 返回 pattern
     */
    public static String getDaysTime(String time, int i, String pattern) {
        long t = Long.parseLong(time);
        Date date = new Date(t * 1000L);
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        calendar.add(calendar.DATE, i);//把日期往后增加一天.整数往后推,负数往前移动
        date = calendar.getTime();
        SimpleDateFormat formatter = new SimpleDateFormat(pattern);
        String data = formatter.format(date);
        return data;
    }

    /**
     * 指定时间 +i 天
     * 返回 时间戳
     */
    public static String getDaysStamp(String time, int i) {
        long t = Long.parseLong(time);
        Date date = new Date(t * 1000L);
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        calendar.add(calendar.DATE, i);//把日期往后增加一天.整数往后推,负数往前移动
        date = calendar.getTime();
        long l = date.getTime();
        return String.valueOf(l / 1000);
    }

    /**
     * 获取当前-年
     */
    public static String getCurrentTimeYear() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
        return sdf.format(new Date());
    }

    /**
     * 获取当前-月
     */
    public static String getCurrentTimeMonth() {
        SimpleDateFormat sdf = new SimpleDateFormat("MM");
        return sdf.format(new Date());
    }

    /**
     * 获取当前-日
     */
    public static String getCurrentTimeDays() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd");
        return sdf.format(new Date());
    }

    /**
     * 根据提供的年月日获取该月份的第一天
     */
    public static String getSupportBeginDayofMonth(int year, int monthOfYear) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, monthOfYear);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.add(Calendar.DAY_OF_MONTH, -1);
        Date lastDate = cal.getTime();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        Date firstDate = cal.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm");
        return sdf.format(firstDate);
    }

    /**
     * 根据提供的年月获取该月份的最后一天
     */
    public static String getSupportEndDayofMonth(int year, int monthOfYear) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, monthOfYear);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.add(Calendar.DAY_OF_MONTH, -1);
        Date lastDate = cal.getTime();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        Date firstDate = cal.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm");
        return sdf.format(lastDate);
    }

    /**
     * 时间戳->日
     */
    public static String getDay(String stamp) {
        Calendar cd = Calendar.getInstance();
        cd.setTime(new Date(Long.parseLong(stamp) * 1000));
        int day = cd.get(Calendar.DAY_OF_MONTH);
        return String.valueOf(day);
    }
    /**
     * 时间戳->月
     */
    public static String getMonth(String stamp) {
        Calendar cd = Calendar.getInstance();
        cd.setTime(new Date(Long.parseLong(stamp) * 1000));
        int day = cd.get(Calendar.MONTH);
        return String.valueOf(day);
    }
    /**
     * 时间戳->年
     */
    public static String getYear(String stamp) {
        Calendar cd = Calendar.getInstance();
        cd.setTime(new Date(Long.parseLong(stamp) * 1000));
        int day = cd.get(Calendar.YEAR);
        return String.valueOf(day);
    }

    /**
     * 得到指定月的天数
     * */
    public static int getMonthLastDay(int year, int month) {
        Calendar a = Calendar.getInstance();
        a.set(Calendar.YEAR, year);
        a.set(Calendar.MONTH, month - 1);
        a.set(Calendar.DATE, 1);//把日期设置为当月第一天
        a.roll(Calendar.DATE, -1);//日期回滚一天，也就是最后一天
        int maxDate = a.get(Calendar.DATE);
        return maxDate;
    }

    /**
     * 创建时间格式化
     */
    public static String getNoteDate(String stamp) {
        // 今天的开始时间、结束时间
        String todayTime = getCurrentTime("yyyy.MM.dd");
        String todayStartStamp = timeToStamp(todayTime + " 00:00:00", "yyyy.MM.dd HH:mm:ss");
        String todayEndStap = timeToStamp(todayTime + " 23:59:59", "yyyy.MM.dd HH:mm:ss");
        // 昨天的开始时间、结束时间
        String yesterdayStartStamp = getDaysStamp(todayStartStamp, -1);
        String yesterdayEndStamp = getDaysStamp(todayEndStap, -1);
        // 今年的开始时间
        String year = getCurrentTimeYear();
        String yearStartStame = timeToStamp(year + ".01.01 00:00:00", "yyyy.MM.dd HH:mm:ss");
        String yearEndStame = timeToStamp(year + ".12.31 23:59:59", "yyyy.MM.dd HH:mm:ss");

        String week = getWeekStamp(stamp);
        if(Long.parseLong(stamp) >= Long.parseLong(todayStartStamp) && Long.parseLong(stamp) <= Long.parseLong(todayEndStap)) {
            // 今天
            String s = stampToTime(stamp, "HH:mm") + " " + week;
            return s;
        }
        if(Long.parseLong(stamp) >= Long.parseLong(yesterdayStartStamp) && Long.parseLong(stamp) <= Long.parseLong(yesterdayEndStamp)) {
            // 昨天
            String s = "昨天 " + stampToTime(stamp, "HH:mm") + " " + week;
            return s;
        }
        if(Long.parseLong(stamp) >= Long.parseLong(yearStartStame) && Long.parseLong(stamp) <= Long.parseLong(yearEndStame)) {
            // 今年
            String s = stampToTime(stamp, "MM-dd HH:mm") + " " + week;
            return s;
        }
        String s = stampToTime(stamp, "yyyy-MM-dd HH:mm") + " " + week;
        return s;
    }

    /**
     * 时间戳->yyyy-MM-dd week
     *
     * pattern 如:yyyy-MM-dd
     */
    public static String stampToTimeWeek(String stamp, String pattern) {
        SimpleDateFormat sdr = new SimpleDateFormat(pattern);
        @SuppressWarnings("unused")
        long lcc = Long.valueOf(stamp);
        int i = Integer.parseInt(stamp);
        String times = sdr.format(new Date(i * 1000L));
        int mydate = 0;
        String week = null;
        Calendar cd = Calendar.getInstance();
        cd.setTime(new Date(i * 1000L));
        mydate = cd.get(Calendar.DAY_OF_WEEK);
        // 获取指定日期转换成星期几
        if (mydate == 1) {
            week = "周日";
        } else if (mydate == 2) {
            week = "周一";
        } else if (mydate == 3) {
            week = "周二";
        } else if (mydate == 4) {
            week = "周三";
        } else if (mydate == 5) {
            week = "周四";
        } else if (mydate == 6) {
            week = "周五";
        } else if (mydate == 7) {
            week = "周六";
        }
        return times + " (" + week + ")";
    }

    /**
     * 时间戳->周几
     */
    public static String getWeekStamp(String stamp) {
        int mydate = 0;
        String week = null;
        Calendar cd = Calendar.getInstance();
        cd.setTime(new Date(Long.parseLong(stamp) * 1000));
        mydate = cd.get(Calendar.DAY_OF_WEEK);
        // 获取指定日期转换成星期几
        if (mydate == 1) {
            week = "周日";
        } else if (mydate == 2) {
            week = "周一";
        } else if (mydate == 3) {
            week = "周二";
        } else if (mydate == 4) {
            week = "周三";
        } else if (mydate == 5) {
            week = "周四";
        } else if (mydate == 6) {
            week = "周五";
        } else if (mydate == 7) {
            week = "周六";
        }
        return week;
    }

    /**
     * yyyy.MM.dd HH:mm->周几
     */
    public static String getWeekTime(String time) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Calendar c = Calendar.getInstance();
        try {
            c.setTime(format.parse(time));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        int wek=c.get(Calendar.DAY_OF_WEEK);
        if (wek == 1) {
            return "周日";
        }
        if (wek == 2) {
            return "周一";
        }
        if (wek == 3) {
            return "周二";
        }
        if (wek == 4) {
            return "周三";
        }
        if (wek == 5) {
            return "周四";
        }
        if (wek == 6) {
            return "周五";
        }
        if (wek == 7) {
            return "周六";
        } else {
            return "周日";
        }
    }

    public static String switchCreateTime(String createTime) {
        String formatStr2 = null;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");//注意格式化的表达式
        try {
            Date time = format.parse(createTime );
            String date = time.toString();
            //将西方形式的日期字符串转换成java.util.Date对象
            SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US);
            Date datetime = (Date) sdf.parse(date);
            //再转换成自己想要显示的格式
            formatStr2 = new SimpleDateFormat("yyyy-MM-dd").format(datetime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return formatStr2;
    }

    /**
     * 仿微信消息提醒--时间
     *
     * @param value 需要处理的时间
     *              1分钟之内 显示：         “刚刚”
     *              1分钟到一小时 显示：  “mm分钟前”
     *              今天的 显示：                   “HH:mm”
     *              今年的 显示：                   “MM-dd HH:mm”
     *              历史的 显示：                   “yyyy-MM-dd HH:mm”
     * @return
     */
    public static String dateFormatLikeWX(Context context, long value) {
        //获取当前时间
        long currentTime = System.currentTimeMillis();
        //与消息的时间进行比较(单位是毫秒)
        long timeDiff = currentTime - value;
        if (timeDiff < 60000) {
            //一个钟之内
            return "刚刚";
        } else if (timeDiff < 3600000) {
            //一分钟~一小时
            return (int) ((timeDiff / 60000)) + "分钟前";
        } else {
            Date createDate = new Date(value);
            Date currentData = new Date();
            SimpleDateFormat fmtDay = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat fmtYear = new SimpleDateFormat("yyyy");
            if (fmtDay.format(createDate).equals(fmtDay.format(currentData))) {
                //今天
                SimpleDateFormat fmt = new SimpleDateFormat("HH:mm");
                return fmt.format(createDate);
            } else if (fmtYear.format(createDate).equals(fmtYear.format(currentData))) {
                //今年
                return changeMonthDateMinuteSecond2(value);
            } else {
                //历史
                return changeYearMonthDate(value);
            }
        }
    }

    /**
     * MM/dd HH:mm
     *
     * @param value
     * @return
     */
    public static String changeMonthDateMinuteSecond2(long value) {
        String dateFormat = "MM-dd HH:mm";
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        Date date = new Date(value);
        return sdf.format(date);
    }

    /**
     * yyyy-MM-dd
     *
     * @param value
     * @return
     */
    public static String changeYearMonthDate(long value) {
        String dateFormat = "yyyy-MM-dd";
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        Date date = new Date(value);
        return sdf.format(date);
    }

    /**
     * yyyy年MM月dd日
     *
     * @param value
     * @return
     */
    public static String changeYearMonthDateCN(long value) {
        String dateFormat = "yyyy年MM月dd日";
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        Date date = new Date(value);
        return sdf.format(date);
    }

    /**
     * MM-dd
     *
     * @param value
     * @return
     */
    public static String changeMonthDate(long value) {
        String dateFormat = "MM-dd";
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        Date date = new Date(value);
        return sdf.format(date);
    }

    /**
     * yyyy-MM-dd HH:mm:SS
     *
     * @param value
     * @return
     */
    public static String changeYearMonthDateMinSec(long value) {
        String dateFormat = "yyyy-MM-dd HH:mm:SS";
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        Date date = new Date(value);
        return sdf.format(date);
    }

    /**
     * yyyy-MM-dd HH:mm
     *
     * @param value
     * @return
     */
    public static String changeYearMonthDateMin(long value) {
        String dateFormat = "yyyy-MM-dd HH:mm";
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        Date date = new Date(value);
        return sdf.format(date);
    }

    /**
     * 获取两个日期之间的间隔天数
     * @return
     */
    public static int getGapCount(Date startDate, Date endDate) {
        Calendar fromCalendar = Calendar.getInstance();
        fromCalendar.setTime(startDate);
        fromCalendar.set(Calendar.HOUR_OF_DAY, 0);
        fromCalendar.set(Calendar.MINUTE, 0);
        fromCalendar.set(Calendar.SECOND, 0);
        fromCalendar.set(Calendar.MILLISECOND, 0);

        Calendar toCalendar = Calendar.getInstance();
        toCalendar.setTime(endDate);
        toCalendar.set(Calendar.HOUR_OF_DAY, 0);
        toCalendar.set(Calendar.MINUTE, 0);
        toCalendar.set(Calendar.SECOND, 0);
        toCalendar.set(Calendar.MILLISECOND, 0);

        return (int) ((toCalendar.getTime().getTime() - fromCalendar.getTime().getTime()) / (1000 * 60 * 60 * 24));
    }



    /**
     * 获取当前时间
     *
     * @return
     */
    public static Date getDateFromStr(String str) {
        Date date = new Date();
        try {
            SimpleDateFormat formatter = new SimpleDateFormat(
                    "yyyy-MM-dd HH:mm:ss");
            date = formatter.parse(str);
            return date;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;


    }

}
