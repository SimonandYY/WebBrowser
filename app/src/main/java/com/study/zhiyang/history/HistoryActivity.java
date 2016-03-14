package com.study.zhiyang.history;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.PaintDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.study.zhiyang.Constants;
import com.study.zhiyang.database.MyDataBaseOpenHelper;
import com.study.zhiyang.webbrowser.R;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhiyang on 2016/1/15.
 */
public class HistoryActivity extends Activity {
    private View parentView;
    private HorizontalScrollView horizontalScrollView;
    private ListView historyListView, favoriteListView;
    private TextView historyClear, favoriteClear;
    private TextView historyTitle, favoriteTitle;
    private int windowWidth;
    HistoryAdapter historyadapter, favoriteAdapter;
    private MyDataBaseOpenHelper helper;
    private String[] querys = {
            Constants.HISTORY_TABLE_TITLE,
            Constants.HISTORY_TABLE_URL,
            Constants.HISTORY_TABLE_TIME,
            Constants.HISTORY_TABLE_ICON,
            Constants.HISTORY_TABLE_KEY_ID
    };
    private String[] querys1 = {
            Constants.FAVORITE_TABLE_TITLE,
            Constants.FAVORITE_TABLE_URL,
            Constants.FAVORITE_TABLE_TIME,
            Constants.FAVORITE_TABLE_ICON,
            Constants.FAVORITE_TABLE_KEY_ID
    };
    private List<HistoryInfo> historys;
    private List<HistoryInfo> favorites;
    private boolean isToMoveParent = false;
    private boolean isFirstMove = true;
    private float startX, startScrollX, moveX;
    long startTime, stopTime;
    float stopX;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        View view = getLayoutInflater().inflate(R.layout.history_activity, null);
        setContentView(view);
//        ColorDrawable drawable = new ColorDrawable(0x000f0000);
//        drawable.setAlpha(0);
//        getWindow().getAttributes().alpha = 0.5f;
        parentView = (View) view.getParent();
        parentView = (LinearLayout) parentView.getParent();
        Log.d("PARENTVIEW", parentView.getClass().getSimpleName() + " " + parentView.getParent().getClass().getName());
//        ((View)parentView.getParent()).setBackgroundDrawable(new PaintDrawable(Color.parseColor("#dd000000")));
//        (Window)(parentView.getParent().getParent()).
        horizontalScrollView = (HorizontalScrollView) findViewById(R.id.history_and_favorite_scrollview);
        horizontalScrollView.setOnTouchListener(new View.OnTouchListener() {


            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()) {
//                    case MotionEvent.ACTION_DOWN:
////                        startX = event.getX();
//                        Log.d("HISTORYActivity",startTime+ " stateTime");
//                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (isFirstMove) {
                            startTime = System.currentTimeMillis();
                            startX = event.getX();
                            startScrollX = horizontalScrollView.getScrollX();
                            isFirstMove = false;
                        } else {
                            moveX = event.getX();

                            if (!isToMoveParent) {
                                Log.i("isToMoveParent", moveX + ">" + startX + "=" + isToMoveParent + "");
                                if (moveX > startX && horizontalScrollView.getScrollX() == 0)
                                    isToMoveParent = true;
                            }
//                            Log.i("isToMoveParent", isToMoveParent + "");
                            if (isToMoveParent) {
                                Log.i("isToMoveParent", moveX + ">" + startX + "=" + isToMoveParent + "");
                                if (parentView.getTranslationX() + moveX - startX >= 0) {
                                    parentView.setTranslationX(parentView.getTranslationX() + moveX - startX);
                                }
                                return true;
                            } else break;
                        }
                        break;


                    case MotionEvent.ACTION_UP:
                        stopX = horizontalScrollView.getScrollX();
                        stopTime = System.currentTimeMillis();
                        Log.d("HISTORYActivity", stopTime + " stopTime");

                        long duration = stopTime - startTime;
                        Log.d("HISTORYActivity", duration + "");
                        if (!isToMoveParent) {
                            if (duration < 300) {
                                if (stopX > startScrollX) {
                                    historyTitle.setTextColor(Color.GRAY);
                                    favoriteTitle.setTextColor(Color.WHITE);
                                    horizontalScrollView.smoothScrollTo(windowWidth, 0);
                                } else{
                                    historyTitle.setTextColor(Color.WHITE);
                                    favoriteTitle.setTextColor(Color.GRAY);
                                    horizontalScrollView.smoothScrollTo(0, 0);
                                }
                            } else {
                                if (horizontalScrollView.getScrollX() < windowWidth / 2) {
                                    historyTitle.setTextColor(Color.WHITE);
                                    favoriteTitle.setTextColor(Color.GRAY);
                                    horizontalScrollView.smoothScrollTo(0, 0);
                                } else if (horizontalScrollView.getScaleX() <= windowWidth) {
                                    historyTitle.setTextColor(Color.GRAY);
                                    favoriteTitle.setTextColor(Color.WHITE);
                                    horizontalScrollView.smoothScrollTo(windowWidth, 0);
                                }
                            }
                        } else {
                            if (duration < 300) {
                                if (parentView.getTranslationX() > 150) {
                                    ObjectAnimator translationAnimator =
                                            ObjectAnimator.ofFloat(parentView, "translationX", parentView.getTranslationX(), windowWidth);
                                    translationAnimator.setDuration((int) ((1 - 1.0f * parentView.getTranslationX() / windowWidth) * 600));
                                    translationAnimator.addListener(new Animator.AnimatorListener() {
                                        @Override
                                        public void onAnimationStart(Animator animation) {

                                        }

                                        @Override
                                        public void onAnimationEnd(Animator animation) {
                                            finish();
                                        }

                                        @Override
                                        public void onAnimationCancel(Animator animation) {

                                        }

                                        @Override
                                        public void onAnimationRepeat(Animator animation) {

                                        }
                                    });
                                    translationAnimator.start();
                                } else {
                                    ObjectAnimator translationAnimator =
                                            ObjectAnimator.ofFloat(parentView, "translationX", parentView.getTranslationX(), 0);
                                    translationAnimator.setDuration((int) ((1.0f * Math.abs(parentView.getTranslationX() / windowWidth)) * 600));
                                    translationAnimator.start();
                                }
                            } else {
                                if (parentView.getTranslationX() < windowWidth / 2) {
                                    ObjectAnimator translationAnimator =
                                            ObjectAnimator.ofFloat(parentView, "translationX", parentView.getTranslationX(), 0);
                                    translationAnimator.setDuration((int) ((1.0f * Math.abs(parentView.getTranslationX() / windowWidth)) * 600));
                                    translationAnimator.start();

                                } else {
                                    ObjectAnimator translationAnimator =
                                            ObjectAnimator.ofFloat(parentView, "translationX", parentView.getTranslationX(), windowWidth);
                                    translationAnimator.setDuration((int) ((1 - 1.0f * parentView.getTranslationX() / windowWidth) * 600));
                                    translationAnimator.addListener(new Animator.AnimatorListener() {
                                        @Override
                                        public void onAnimationStart(Animator animation) {

                                        }

                                        @Override
                                        public void onAnimationEnd(Animator animation) {
                                            finish();
                                        }

                                        @Override
                                        public void onAnimationCancel(Animator animation) {

                                        }

                                        @Override
                                        public void onAnimationRepeat(Animator animation) {

                                        }
                                    });
                                    translationAnimator.start();

                                }
                            }
                        }
                        isToMoveParent = false;
                        isFirstMove = true;
                        return true;
                }
                return horizontalScrollView.onTouchEvent(event);
            }
        });
        historys = new ArrayList<>();
        favorites = new ArrayList<>();
        helper = new MyDataBaseOpenHelper(this, Constants.DB_NAME, null, 1);
        WindowManager wm = getWindowManager();
        DisplayMetrics m = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(m);
        windowWidth = m.widthPixels;
        historyListView = (ListView) findViewById(R.id.horizontalScrollView);
        historyClear = (TextView) findViewById(R.id.history_listview_history_clear_all);

        favoriteClear = (TextView) findViewById(R.id.history_listview_favorites_clear_all);
        historyListView = (ListView) findViewById(R.id.history_listview);
        favoriteListView = (ListView) findViewById(R.id.history_tag_listview);
        historyListView.getLayoutParams().width = windowWidth;
        favoriteListView.getLayoutParams().width = windowWidth;
        //query Database Data
        reachForHistoryData();
        reachForFavoriteData();
        //Adapter
        historyadapter = new HistoryAdapter(this, R.layout.history_activity_history_item, historys);
        favoriteAdapter = new HistoryAdapter(this, R.layout.history_activity_history_item, favorites);

        //Set ListViews
        historyListView.setAdapter(historyadapter);
        historyClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                helper.clearAllHistory();
                reachForHistoryData();
                historyadapter.notifyDataSetChanged();

            }
        });
        historyListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                HistoryInfo info = historys.get(position);
                Intent historyItemClickIntent = new Intent();
                historyItemClickIntent.setAction(Constants.HISTORY_ITEM_CLICKED_INTENT);
                historyItemClickIntent.putExtra("url", info.url);
                sendBroadcast(historyItemClickIntent);
                finish();
            }
        });
        historyListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                final PopupWindow historyItemPopup = new PopupWindow();
                View view1 = getLayoutInflater().inflate(R.layout.history_listview_long_click_popupwindow, null);
                TextView deleteThis = (TextView) view1.findViewById(R.id.history_listview_popup_delete);
                TextView addToFavorite = (TextView) view1.findViewById(R.id.history_listview_popup_add_to_favorite);
                historyItemPopup.setContentView(view1);
                historyItemPopup.setHeight(LinearLayout.LayoutParams.WRAP_CONTENT);
                historyItemPopup.setWidth(LinearLayout.LayoutParams.WRAP_CONTENT);
                historyItemPopup.setBackgroundDrawable(new PaintDrawable(Color.parseColor("#dd123456")));
                deleteThis.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        historyItemPopup.dismiss();
                        helper.deleteHistoryAtCertainTime(historys.get(position).time);
                        reachForHistoryData();
                        historyadapter.notifyDataSetChanged();
                    }
                });
                addToFavorite.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        historyItemPopup.dismiss();

                    }
                });
                historyItemPopup.setOutsideTouchable(true);
                historyItemPopup.setFocusable(true);
                historyItemPopup.showAsDropDown(view, windowWidth / 2, -50);
//                ((FrameLayout)view1.getParent()).setBackgroundColor(Color.parseColor("#00000000"));
                Log.d("PARENT TYPE", view1.getParent().getClass().getSimpleName());
                return true;
            }
        });
        favoriteListView.setAdapter(favoriteAdapter);
        favoriteListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                HistoryInfo info = favorites.get(position);
                Intent i = new Intent(Constants.HISTORY_ITEM_CLICKED_INTENT);
                i.putExtra("url", info.url);
                sendBroadcast(i);
                finish();
            }
        });
        favoriteListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                final PopupWindow favoriteItemPopup = new PopupWindow();
                View view1 = getLayoutInflater().inflate(R.layout.favorite_listview_long_click_popup, null);
                TextView deleteThis = (TextView) view1.findViewById(R.id.favorite_listview_popup_delete);
                favoriteItemPopup.setContentView(view1);
                favoriteItemPopup.setHeight(LinearLayout.LayoutParams.WRAP_CONTENT);
                favoriteItemPopup.setWidth(LinearLayout.LayoutParams.WRAP_CONTENT);
                favoriteItemPopup.setBackgroundDrawable(new PaintDrawable(Color.parseColor("#dd123456")));
                favoriteItemPopup.setOutsideTouchable(true);
                favoriteItemPopup.setFocusable(true);
                deleteThis.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        favoriteItemPopup.dismiss();
                        helper.deleteFavoriteAtCertainTime(favorites.get(position).time);
                        reachForFavoriteData();
                        favoriteAdapter.notifyDataSetChanged();
                    }
                });
                favoriteItemPopup.showAsDropDown(view, windowWidth / 2 - 100, -50);
                return true;
            }
        });

        favoriteClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                helper.clearAllFavorites();
                reachForFavoriteData();
                favoriteAdapter.notifyDataSetChanged();
            }
        });
        historyTitle = (TextView) findViewById(R.id.history_listview_historiy_title);
        favoriteTitle = (TextView) findViewById(R.id.history_listview_favorite_title);
        historyTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                historyTitle.setTextColor(Color.WHITE);
                favoriteTitle.setTextColor(Color.GRAY);
                horizontalScrollView.smoothScrollTo(0,0);
            }
        });
        favoriteTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                historyTitle.setTextColor(Color.GRAY);
                favoriteTitle.setTextColor(Color.WHITE);
                horizontalScrollView.smoothScrollTo(windowWidth,0);
            }
        });
    }

    private void reachForHistoryData() {
        Cursor c = helper.getReadableDatabase().query(Constants.HISTORY_TABLE_NAME, querys, null, null
                , null, null, null);
        historys.clear();
        if (c == null) {
            return;
        }
        while (c.moveToNext()) {
            HistoryInfo info = new HistoryInfo();
            info.time = c.getLong(c.getColumnIndex(Constants.HISTORY_TABLE_TIME));
            info.title = c.getString(c.getColumnIndex(Constants.HISTORY_TABLE_TITLE));
            info.url = c.getString(c.getColumnIndex(Constants.HISTORY_TABLE_URL));
            Log.i("HISTORY", info.url);
            byte[] b = c.getBlob(c.getColumnIndex(Constants.HISTORY_TABLE_ICON));
            try {
                info.icon = BitmapFactory.decodeByteArray(b, 0, b.length);
            } catch (Exception e) {

            }
            historys.add(0, info);
        }
        c.close();
    }

    private void reachForFavoriteData() {
        Cursor c = helper.getReadableDatabase().query(Constants.FAVORITE_TABLE_NAME, querys1, null, null
                , null, null, null);
        favorites.clear();
        if (c == null) {
            return;
        }
        while (c.moveToNext()) {
            HistoryInfo info = new HistoryInfo();
            info.time = c.getLong(c.getColumnIndex(Constants.FAVORITE_TABLE_TIME));
            info.title = c.getString(c.getColumnIndex(Constants.FAVORITE_TABLE_TITLE));
            info.url = c.getString(c.getColumnIndex(Constants.FAVORITE_TABLE_URL));
            Log.i("HISTORY", info.url);
            byte[] b = c.getBlob(c.getColumnIndex(Constants.FAVORITE_TABLE_ICON));
            try {
                info.icon = BitmapFactory.decodeByteArray(b, 0, b.length);
            } catch (Exception e) {

            }
            favorites.add(0, info);
        }
        c.close();
    }

    class HistoryAdapter extends ArrayAdapter {

        //    private long currentTime;
        public HistoryAdapter(Context context, int resource, List<HistoryInfo> textViewResourceId) {
            super(context, resource, textViewResourceId);
//        currentTime = System.currentTimeMillis();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            HistoryInfo info = (HistoryInfo) getItem(position);
            View v = getLayoutInflater().inflate(R.layout.history_activity_history_item, null);
//        ImageView iconView = v.findViewById(R.id.)
            TextView titleTextView = (TextView) v.findViewById(R.id.history_listview_list_item_webtitle);
            TextView urlTextView = (TextView) v.findViewById(R.id.history_listview_list_item_weburl);
            ImageView iconView = (ImageView) v.findViewById(R.id.history_listview_historiy_icon);
            TextView timeView = (TextView) v.findViewById(R.id.history_listview_historiy_item_time);
            timeView.setText(historyItemTime2String(info.time));
            titleTextView.setText(info.title);
            urlTextView.setText(info.url);
            iconView.setImageBitmap(info.icon);
            return v;
        }
    }

//    class FavoriteAdapter extends ArrayAdapter {
//
//        //    private long currentTime;
//        public FavoriteAdapter(Context context, int resource, List<HistoryInfo> textViewResourceId) {
//            super(context, resource, textViewResourceId);
////        currentTime = System.currentTimeMillis();
//        }
//
//        @Override
//        public View getView(int position, View convertView, ViewGroup parent) {
//
//            HistoryInfo info = (HistoryInfo) getItem(position);
//            View v = getLayoutInflater().inflate(R.layout.history_activity_history_item, null);
////        ImageView iconView = v.findViewById(R.id.)
//            TextView titleTextView = (TextView) v.findViewById(R.id.history_listview_list_item_webtitle);
//            TextView urlTextView = (TextView) v.findViewById(R.id.history_listview_list_item_weburl);
//            ImageView iconView = (ImageView) v.findViewById(R.id.history_listview_historiy_icon);
//            TextView timeView = (TextView) v.findViewById(R.id.history_listview_historiy_item_time);
//            timeView.setText(historyItemTime2String(info.time));
//            titleTextView.setText(info.title);
//            urlTextView.setText(info.url);
//            iconView.setImageBitmap(info.icon);
//            return v;
//        }
//    }

    class HistoryInfo {
        public String title, url;
        long time;
        public Bitmap icon;
    }


    public String historyItemTime2String(long time) {
        String result = "";
        long timeMillis = System.currentTimeMillis() - time;
        if (timeMillis < 60000)
            result = timeMillis / 1000 + " 秒钟之前";
        else if (timeMillis < 3600000)
            result = timeMillis / 60000 + " 分钟之前";
        else if (timeMillis < 3600000 * 24)
            result = timeMillis / 3600000 + " 小时之前";
        else if (timeMillis < 3600000 * 24 * 7)
            result = timeMillis / (3600000 * 24) + " 天之前";
        else result = " 一周之前";
        return result;
    }


}
