package com.tts;

/**
 * 作者: mashuangwei
 * 日期: 2017/8/22
 */

public class CustomSleep {
    public static void sleep(double seconds){
        try {
            Thread.sleep((long) (1000*seconds));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
