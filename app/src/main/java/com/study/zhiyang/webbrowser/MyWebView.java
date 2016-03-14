package com.study.zhiyang.webbrowser;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.webkit.DownloadListener;
import android.webkit.WebView;

/**
 * Created by zhiyang on 2015/12/28.
 */
public class MyWebView extends WebView {

    public boolean isWindowScaled = true;
    private boolean touchable = true;
    public boolean containsVideo = false;
    public boolean isInNightMode = false;
    SharedPreferences sp;
    public MyWebView(Context context) {
        super(context);
        sp = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public MyWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        sp = PreferenceManager.getDefaultSharedPreferences(context);


    }

    @Override
    public void onResume() {

        if (containsVideo) {
            setLayerType(LAYER_TYPE_HARDWARE, null);

        }else setLayerType(LAYER_TYPE_SOFTWARE, null);

        super.onResume();
    }

    public float touchX, touchY, tempX;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isWindowScaled) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                touchX = event.getX();
                touchY = event.getY();
                onScrollChanged(getScrollX(), getScrollY(), getScrollX(), getScrollY());
                getParent().requestDisallowInterceptTouchEvent(true);
            }
            if (event.getAction() == MotionEvent.ACTION_UP) {
                getParent().requestDisallowInterceptTouchEvent(false);
                tempX = event.getX();
                if (touchX < getWidth() / 16 && tempX - touchX > getWidth() / 3) {
                    if (canGoBack())
                        goBack();
                    //return super.onTouchEvent(event);

                } else if (touchX > getWidth() * 15.0f / 16 && tempX - touchX < -getWidth() / 3) {
                    if (canGoForward()) {
                        goForward();
                        //return super.onTouchEvent(event);
                    }
                }
                //return super.onTouchEvent(event);

            }
            return super.onTouchEvent(event);

        } else {

            onScrollChanged(getScrollX(), getScrollY(), getScrollX(), getScrollY());
            return false;
        }
    }


    public void setWindowScaled(boolean isWindowScaled) {
        this.isWindowScaled = isWindowScaled;
    }

}
