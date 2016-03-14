package com.study.zhiyang.utils;

import android.app.Application;

/**
 * Created by zhiyang on 2016/1/12.
 */
public class ContextUtils extends Application {
    private static ContextUtils instance;

    public static ContextUtils getInstance() {
        return instance;
    }
    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        instance = this;
    }
}
