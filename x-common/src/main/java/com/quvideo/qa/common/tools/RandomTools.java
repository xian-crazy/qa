package com.quvideo.qa.common.tools;

import java.util.Random;

public class RandomTools {
    private static  RandomTools randomTools=null;
    private static Random r1 = new Random();
    private RandomTools(){}
    public static  RandomTools getInstance(){
        synchronized (RandomTools.class){
            if(null == randomTools){
                randomTools=new RandomTools();
            }
        }
        return randomTools;
    }

    //to do 增加随机工具

    public    int  getRandomDuid(){
        int  n2 = Math.abs(r1.nextInt() );
        return  n2;
    }
    public  static   int  getRandomInt(int min,int max){
        int  n2 = Math.abs(r1.nextInt(max-min)+max );
        return  n2;
    }
    public static int getNum(int start,int end) {
        return (int)(Math.random()*(end-start+1)+start);
    }
    public static String getRandomString(int length){
        String str="abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random=new Random();
        StringBuffer sb=new StringBuffer();
        for(int i=0;i<length;i++){
            int number=random.nextInt(62);
            sb.append(str.charAt(number));
        }
        return sb.toString();
    }

    /**
     * 随机生成手机号码
     * @return   手机号
     */
    public static String getTelephone(){
        String[] telFirst="134,135,136,137,138,139,150,151,152,157,158,159,130,131,132,155,156,133,153".split(",");
        int index=getNum(0,telFirst.length-1);
        String first=telFirst[index];
        String second=String.valueOf(getNum(1,888)+10000).substring(1);
        String thrid=String.valueOf(getNum(1,9100)+10000).substring(1);
        return first+second+thrid;

    }



}
