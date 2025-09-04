package com.wenjia.common.constant;

/**
 * 使用final和私有化构造函数使得类不可被继承和构造
 */
public final class DateTimeFormat {
    private DateTimeFormat(){}
    public static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    public static final String TIME_PATTERN = "HH:mm:ss";
}
