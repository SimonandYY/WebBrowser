package com.study.zhiyang.webbrowser;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

import com.study.zhiyang.utils.DensityUtils;

/**
 * Created by zhiyang on 2015/12/30.
 */
public class MyHorizontalScrollView extends HorizontalScrollView {
    public int currentPage;
    public int pageNum = 1;
    public static int width, height;
    private LinearLayout wapper;
    private LinearLayout.LayoutParams paramLeft, paramNoLeft;
    private Context mContext;
    public static final String CURRENT_PAGE_DELETED = "CURRENT_PAGE_DELETED";
    public static final String PAGE_CLICKED = "PAGE_CLICKED";
    public boolean scrollable = false;

    public MyHorizontalScrollView(Context context) {
        super(context);
        currentPage = 0;
        paramNoLeft = new LinearLayout.LayoutParams(width, height - DensityUtils.dp2px(context, 50) - MainActivity.statusBarHeight);
        paramNoLeft.gravity = Gravity.CENTER_VERTICAL;
        paramNoLeft.leftMargin = (int) (-width * 3.0f / 8.0f);
        paramLeft = new LinearLayout.LayoutParams(width, height - DensityUtils.dp2px(context, 50) - MainActivity.statusBarHeight);
        paramLeft.gravity = Gravity.CENTER_VERTICAL;
        paramLeft.leftMargin = 0;
        wapper = (LinearLayout) getChildAt(0);
        mContext = context;
//        scrollToPage(0);
    }

    public MyHorizontalScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        Log.i("CONSTRUCTER", "CALLED");
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(metrics);
        width = metrics.widthPixels;
        height = metrics.heightPixels;
        currentPage = 0;
        paramNoLeft = new LinearLayout.LayoutParams(width, LinearLayout.LayoutParams.MATCH_PARENT);
        paramNoLeft.gravity = Gravity.CENTER_VERTICAL;
        paramNoLeft.leftMargin = (int) (-width * 3.0f / 8.0f);
        paramLeft = new LinearLayout.LayoutParams(width, LinearLayout.LayoutParams.MATCH_PARENT);
        paramLeft.gravity = Gravity.CENTER_VERTICAL;
        paramLeft.leftMargin = 0;
//        scrollToPage(0);
        wapper = (LinearLayout) getChildAt(0);
        mContext = context;
    }

    float startX, stopX;

    float startY, stopY, startScrollPossition;
    float tempX, tempY;
    private boolean isVerticalScroll = false;
    private boolean isToScroll = false;
    private long startTime, stopTime;

    @Override
    public boolean onTouchEvent(MotionEvent ev) {


        if (!isVerticalScroll) {
            int action = ev.getAction();
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    startScrollPossition = getScrollX();
                    startX = ev.getX();
                    startY = ev.getY();
                    startTime = System.currentTimeMillis();
                    //判断触摸位置
                    if (startY > height / 4.0f && startY < height * 3.0f / 4.0f) {
                        isToScroll = true;
                        Log.d("Touch  Start", startY + "");
                        break;
                    } else return true;
                case MotionEvent.ACTION_MOVE:
                    if (isToScroll) {
                        if (isToScroll) {
                            tempX = ev.getX();
                            tempY = ev.getY();
                            if (Math.abs(tempY - startY) > Math.abs(tempX - startX) && Math.abs(tempY - startY) > 100) {
                                isVerticalScroll = true;
                                break;
                            } else return super.onTouchEvent(ev);
                        }
                    }

                    break;
                case MotionEvent.ACTION_UP:
                    if (isToScroll) {
                        stopX = getScrollX();
                        stopY = ev.getY();
                        float clickX = ev.getX();
                        stopTime = System.currentTimeMillis();

                        if (Math.abs(clickX - startX) < 20 && Math.abs(stopY - startY) < 20 && stopY > height / 4.0f && stopY < height * 3.0f / 4.0f) {
                            if (clickX < getWidth() / 8.0f && currentPage > 0) {
                                scrollToPageAndEnlarge(currentPage - 1);

                            } else if (clickX > 7.0f * getWidth() / 8.0f && currentPage < pageNum - 1) {
                                scrollToPageAndEnlarge(currentPage + 1);

                            } else if (clickX > getWidth() / 4.0f && clickX < 3.0f * getWidth() / 4.0f) {
                                scrollToPageAndEnlarge(currentPage);

                            }
                            Log.d("Scroll", "Click");


                            break;
                        } else if (stopTime - startTime < 500 && Math.abs(clickX - startX) >= 20) {
                            Log.d("Scroll", "fast Scroll");
                            if (clickX < startX && currentPage < pageNum - 1)
                                scrollToPage(currentPage + 1);
                            else if (clickX > startX && currentPage > 0)
                                scrollToPage(currentPage - 1);
                            break;
                        } else {
                            Log.d("Scroll", "Scroll");

                            if (stopX > (currentPage + 0.5f) * 5.0f / 8.0f * width) {
                                if (currentPage < pageNum - 1) {
                                    currentPage++;
                                    scrollToPage(currentPage);
                                }
                            } else if (stopX < (currentPage - 0.5f) * 5.0f / 8.0f * width) {
                                if (currentPage > 0) {
                                    currentPage--;
                                    scrollToPage(currentPage);
                                }
                            } else {
                                scrollToPage(currentPage);
                            }
                        }
                        isToScroll = false;

                        startX = stopX = startY = stopY = startScrollPossition =
                                tempX = tempY = 0;
                        return true;

                    }
                    // return super.onTouchEvent(ev);
            }
            return true;

        } else {
            int action = ev.getAction();
            switch (action) {
                case MotionEvent.ACTION_MOVE:
                    if (isToScroll) {

                        tempY = ev.getY();
                        (wapper.getChildAt(currentPage)).setTranslationY(tempY - startY);
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    if (isToScroll) {
                        stopTime = System.currentTimeMillis();
                        stopY = ev.getY();
                        if (Math.abs(startY - stopY) > height / 5) {
                            deletePageWithAnimators();

                        } else {
                            ObjectAnimator returnAnimator = ObjectAnimator.ofFloat(wapper.getChildAt(currentPage),
                                    "translationY", tempY - startY, 0);
                            returnAnimator.setDuration(500).start();
                        }
                        // scrollToPage(currentPage);
                        isToScroll = false;
                        isVerticalScroll = false;
                        startX = stopX = startY = stopY = startScrollPossition =
                                tempX = tempY = 0;
                        return true;

                    }

                    return true;
            }
            return true;
        }
    }

    public void deletePageWithAnimators() {
        //如果躲雨一页向上移除该页
        if (pageNum != 1) {
            ObjectAnimator upTrans = ObjectAnimator.ofFloat(wapper.getChildAt(currentPage), "translationY", ((WebPage) wapper.getChildAt(currentPage)).getTranslationY(), -height);

            upTrans.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    deleteCurrentPage();
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            upTrans.setDuration(200).start();
        } else {
            ObjectAnimator downTrans = ObjectAnimator.ofFloat(wapper.getChildAt(currentPage), "translationY", tempY - startY, 0);

//            downTrans.addListener(new Animator.AnimatorListener() {
//                @Override
//                public void onAnimationStart(Animator animation) {
//
//                }
//
//                @Override
//                public void onAnimationEnd(Animator animation) {
//                    deleteCurrentPage();
//                }
//
//                @Override
//                public void onAnimationCancel(Animator animation) {
//
//                }
//
//                @Override
//                public void onAnimationRepeat(Animator animation) {
//
//                }
//            });
            downTrans.setDuration(200).start();
        }
    }


//}

    public void scrollToPage(int page) {
        this.currentPage = page;
        ObjectAnimator objectAnimator = ObjectAnimator.ofInt(this, "scrollX", getScrollX(), (int) (page * 5.0f / 8.0f * width));
        objectAnimator.setDuration(200);
        objectAnimator.start();
//        smoothScrollTo((int) (((float) page) * 5.0f / 8.0f * width), 0);
//        Log.d("ScrollTo Value", (int) (((float) page) * 5.0f / 8.0f * width) + " current Page " + currentPage);
    }

    public void scrollToPageAndEnlarge(int page) {
        this.currentPage = page;
        ObjectAnimator objectAnimator = ObjectAnimator.ofInt(this, "scrollX", getScrollX(), (int) (page * 5.0f / 8.0f * width));
        objectAnimator.setDuration(200);
        objectAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                Intent intent = new Intent(PAGE_CLICKED);
                mContext.sendBroadcast(intent);
                ((MainActivity) mContext).enlargeCurrentPage();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        objectAnimator.start();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        wapper = (LinearLayout) getChildAt(0);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }


    public void setCurrentPage(int page) {
        scrollToPage(page);
        this.currentPage = page;
    }

    //    ObjectAnimator leftTrans = ObjectAnimator.ofFloat(wapper.getChildAt(currentPage-1),"translationX",0,5.0f / 8.0f * width);
//    ObjectAnimator rightTrans = ObjectAnimator.ofFloat(wapper.getChildAt(currentPage+1),"translationX",0,-5.0f / 8.0f * width);
    public void deleteCurrentPage() {
        Log.d("Remove", "deleteCurrentPage");
        if (currentPage == pageNum - 1 && pageNum != 1 && currentPage != 0) {
            ObjectAnimator leftTrans = ObjectAnimator.ofFloat(wapper.getChildAt(currentPage - 1), "translationX", 0, 5.0f / 8.0f * width);
            leftTrans.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    ((LinearLayout) getChildAt(0)).removeViewAt(currentPage);
                    pageNum--;
                    scrollToPage(currentPage - 1);
                    wapper.getChildAt(currentPage).setTranslationX(0);
                    Intent i = new Intent(CURRENT_PAGE_DELETED);
                    mContext.sendBroadcast(i);

                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            leftTrans.setDuration(200).start();

//
        } else if (pageNum != 1 && currentPage == 0) {
            ObjectAnimator rightTrans = ObjectAnimator.ofFloat(wapper.getChildAt(currentPage + 1), "translationX", 0, -5.0f / 8.0f * width);
            rightTrans.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    ((LinearLayout) getChildAt(0)).getChildAt(1).setLayoutParams(MainActivity.paramLeft);
                    ((LinearLayout) getChildAt(0)).removeViewAt(currentPage);
                    pageNum--;
                    scrollToPage(currentPage);
                    wapper.getChildAt(currentPage).setTranslationX(0);
                    Intent i = new Intent(CURRENT_PAGE_DELETED);
                    mContext.sendBroadcast(i);
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            rightTrans.setDuration(500).start();

        } else if (pageNum == 1) {

        } else {
            ObjectAnimator rightTrans = ObjectAnimator.ofFloat(wapper.getChildAt(currentPage + 1), "translationX", 0, -5.0f / 8.0f * width);
            rightTrans.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    ((LinearLayout) getChildAt(0)).removeViewAt(currentPage);
//            ((LinearLayout) getChildAt(0)).removeView(((LinearLayout) getChildAt(0)).getChildAt(currentPage));
                    pageNum--;

                    setCurrentPage(currentPage);
                    wapper.getChildAt(currentPage).setTranslationX(0);
                    Intent i = new Intent(CURRENT_PAGE_DELETED);
                    mContext.sendBroadcast(i);
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            rightTrans.setDuration(200).start();

        }

        ((LinearLayout) getChildAt(0)).requestLayout();
        ((LinearLayout) getChildAt(0)).invalidate();
    }

    public void resetTranslationX(){
        for (int i = currentPage+2;i<pageNum;i++){
            wapper.getChildAt(i).setTranslationX(0);
            Log.d("resetTranslationX",wapper.getChildAt(i).getTranslationX()+"");
        }
    }
}
