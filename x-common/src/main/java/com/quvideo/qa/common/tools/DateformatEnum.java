package com.quvideo.qa.common.tools;

/**
 * Created by guoli on 2019/3/14.
 */
public enum DateformatEnum {
    /***/
    yyyy("yyyy"),
    /***/
    yyyyMM("yyyyMM"),
    /***/
    yyyyMMddSplit("yyyy-MM-dd"),
    /***/
    yyyyMMddHHSplit("yyyy-MM-dd HH"),
    /***/
    yyyyMMdd("yyyyMMdd"),
    /***/
    yyyyMMddHHmmss("yyyyMMddHHmmss"),
    /***/
    yyyyMMddHHmm("yyyyMMddHHmm"),
    /***/
    yyyyMMddHHmmssSSS("yyyyMMddHHmmssSSS"),
    /***/
    yyyyMMddHHmmssSplit("yyyy-MM-dd HH:mm:ss"),
    /***/
    yyyyMMSplit("yyyy-MM"),
    /***/
    yyyyMMddHHmmssSSSplit("yyyy-MM-dd HH:mm:ss.SSS"),
    /***/
    HHmmss("HHmmss"),
    /***/
    HHmmssSSS("HHmmssSSS"),
    /***/
    HH("HH"),
    /***/
    yyyyMMddHH("yyyyMMddHH"),

    /***/
    yyyy_MM("yyyy_MM"),
    ddMMMyyyyHHmmssSSSSSplit("dd/MMM/yyyy:HH:mm:ss +SSSS"),
    MMddyyyy("MM/dd/yyyy");

    public String format;

    DateformatEnum(String format) {
        this.format = format;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

}
