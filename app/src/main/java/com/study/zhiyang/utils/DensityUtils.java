package com.study.zhiyang.utils;

import android.content.Context;

/**
 * Created by zhiyang on 2015/12/24.
 */
public class DensityUtils {
    public static int dp2px(Context context, float dpValue){
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
    public static int px2dp(Context context,float pxValue){
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }
}
