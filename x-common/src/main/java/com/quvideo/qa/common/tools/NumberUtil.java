package com.quvideo.qa.common.tools;

/**
 * Created by guoli on 2019/2/16.
 */
public class NumberUtil {

    public static long parseLong(String str, long defValue) {
        try {
            return Long.parseLong(str);

        } catch (Exception e) {
            return defValue;
        }

    }
    public static int parseInt(String str, int defValue) {
        try {
            return Integer.parseInt(str);

        } catch (Exception e) {
            return defValue;
        }
    }

    public static int[] parseStringArr(String... strarr){
        int len=strarr.length;
        int [] p=new int[len];
        for (int i = 0; i < len; i++) {
            p[i]=Integer.parseInt(strarr[i]);
        }
        return p;
    }
}
