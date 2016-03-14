package com.study.zhiyang.download;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.study.zhiyang.adapter.MyDownLoadListAdapter;
import com.study.zhiyang.webbrowser.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

/**
 * Created by zhiyang on 2016/1/8.
 */
public class DownloadListActivity extends Activity implements View.OnClickListener {
    private ListView listView;
    private SQLiteDatabase db;
    private Cursor cursor;
    private Handler handler;
    private Timer timer;
    private List<DownloadInfo> downloadInfos;
    private MyDownLoadListAdapter adapter;
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case "MyBrowser.DownloadList.DATASETCHANGED":
                    Log.d("RECEIVER", "MyBrowser.DownloadList.DATASETCHANGED");

                    downloadInfos = DownloadTools.getMyDownloadManager().getDownloadInfos();
                    adapter.notifyDataSetChanged();
                    break;
            }
        }
    };
    //    final String[] column = {Constants.DOWNLOAD_TABLE_ITEM_NAME
//            , Constants.DOWNLOAD_TABLE_ITEM_FINISHED_PERCENTEAGE
//            , Constants.DOWNLOAD_TABLE_ITEM_FINISHED
//            , Constants.DOWNLOAD_TABLE_ITEM_SIZE
//            , Constants.DOWNLOAD_TABLE_ITEM_PATH
//            , Constants.DOWNLOAD_TABLE_ITEM_TYPE
//            , Constants.DOWNLOAD_TABLE_KEY_ID
//    };
//    int[] ids = {R.id.download_item_icon, R.id.download_item_name, R.id.download_item_progress, R.id.download_item_size};
    public static final boolean SELECT_MODE = true;
    public static final boolean NO_SELECT_MODE = false;
    private RelativeLayout controlBar;
    private Button cancleSelection, selectALL, deleteSelectedItems;
    private boolean listviewDisplayMode = NO_SELECT_MODE;
    private ImageView downloadBack;
    private ImageView back;
    LinearLayout view;
    private FrameLayout parent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        view = (LinearLayout) getLayoutInflater().inflate(R.layout.download_list_activity, null);
        setContentView(view);
        downloadBack = (ImageView) findViewById(R.id.download_activity_back);
        downloadBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ObjectAnimator animator = ObjectAnimator.ofFloat(parent, "translationX",
                        parent.getTranslationX(), parent.getWidth());
                animator.setDuration(300);
                animator.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        DownloadListActivity.this.finish();

                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });
                animator.start();
            }
        });
        listView = (ListView) view.findViewById(R.id.downloadListview);
        controlBar = (RelativeLayout) view.findViewById(R.id.download_activity_controlbar);
        selectALL = (Button) view.findViewById(R.id.download_activity_select_all);
        cancleSelection = (Button) view.findViewById(R.id.download_activity_cancle_selection);
        deleteSelectedItems = (Button) view.findViewById(R.id.download_activity_delete_selection);
        back = (ImageView) view.findViewById(R.id.download_activity_back);
        cancleSelection.setOnClickListener(this);
        parent = (FrameLayout) view.getParent();
        //  ((FrameLayout)(view.getParent())).setBackgroundColor(Color.parseColor("#00ff0000"));
        listView.setOnTouchListener(new View.OnTouchListener() {
            float startX, tempX;
            float startRawX, tempRawX;
            long startTime, stopTime;

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startX = event.getX();
                        startRawX = event.getRawX();
                        startTime = System.currentTimeMillis();
                    case MotionEvent.ACTION_MOVE:
                        tempX = event.getX();
                        tempRawX = event.getRawX();
                        if (parent.getTranslationX() + tempX - startX >= 0)
                            parent.setTranslationX(parent.getTranslationX() + tempX - startX);
//                        Log.d("view x",x+"");
                        break;
                    case MotionEvent.ACTION_UP:
                        stopTime = System.currentTimeMillis();
                        if (stopTime - startTime < 300 && parent.getTranslationX() > parent.getWidth()/4) {
                            ObjectAnimator animator = ObjectAnimator.ofFloat(parent, "translationX",
                                    parent.getTranslationX(), parent.getWidth());
                            animator.setDuration(300);
                            animator.addListener(new Animator.AnimatorListener() {
                                @Override
                                public void onAnimationStart(Animator animation) {

                                }

                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    DownloadListActivity.this.finish();

                                }

                                @Override
                                public void onAnimationCancel(Animator animation) {

                                }

                                @Override
                                public void onAnimationRepeat(Animator animation) {

                                }
                            });
                            animator.start();
                        } else {
                            if (parent.getTranslationX() > parent.getWidth() / 2) {
                                ObjectAnimator animator = ObjectAnimator.ofFloat(parent, "translationX",
                                        parent.getTranslationX(), parent.getWidth());
                                animator.setDuration(300);
                                animator.addListener(new Animator.AnimatorListener() {
                                    @Override
                                    public void onAnimationStart(Animator animation) {

                                    }

                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        DownloadListActivity.this.finish();

                                    }

                                    @Override
                                    public void onAnimationCancel(Animator animation) {

                                    }

                                    @Override
                                    public void onAnimationRepeat(Animator animation) {

                                    }
                                });
                                animator.start();
                            } else {
                                ObjectAnimator animator = ObjectAnimator.ofFloat(parent, "translationX",
                                        parent.getTranslationX(), 0);
                                animator.setDuration(300);
                                animator.start();
//                            parent.setTranslationX(0);
                            }
                            break;
                        }
                }
                return listView.onTouchEvent(event);
            }
        });
        selectALL.setOnClickListener(this);
        deleteSelectedItems.setOnClickListener(this);
        IntentFilter filter = new IntentFilter();
        filter.addAction("MyBrowser.DownloadList.DATASETCHANGED");
        registerReceiver(receiver, filter);
        myInitDownloadList();
//        view.setFocusable(false);
//        view.onInterceptTouchEvent(new )
//        view.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                switch (event.getAction()){
//                    case MotionEvent.ACTION_MOVE:
//                        float x = event.getX();
//                        view.setTranslationX(x);
//                        Log.d("view x",x+"");
//                }
//                return true;
//            }
//        });
//        initDownloadList();
//        handler = new Handler()
//        {
//            @Override
//            public void handleMessage(Message msg) {
//                switch (msg.what){
//                    case 0:
//                        adapter.changeCursor(db.query(Constants.DOWNLOAD_TABLE_NAME, column, null, null, null, null, null));
//                        break;
//                    case 1:
//                        adapter.changeCursor(db.query(Constants.DOWNLOAD_TABLE_NAME, column, null, null, null, null, null));
//                        break;
//                }
//                super.handleMessage(msg);
//            }
//        };
    }

    private void myInitDownloadList() {
        downloadInfos = new ArrayList<>();
        downloadInfos = DownloadTools.getMyDownloadManager().getDownloadInfos();
        adapter = new MyDownLoadListAdapter(this, R.layout.download_list_item, downloadInfos);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (!listviewDisplayMode) {
                    DownloadTools.getMyDownloadManager().handleItemClickAction(position);
                } else {

//                    CheckBox checkBox = (CheckBox) ((LinearLayout)((LinearLayout)view).getChildAt(0)).getChildAt(0);
                    CheckBox checkBox = (CheckBox) view.findViewById(R.id.download_item_checkbox_delete);

                    checkBox.setChecked(!checkBox.isChecked());
                }
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                controlBar.setVisibility(View.VISIBLE);
                adapter.setDisplayMode(SELECT_MODE);
                listView.invalidateViews();
                adapter.checkBoxStates.clear();
                adapter.iconMap.clear();
//                listView.setClickable(true);
                listviewDisplayMode = SELECT_MODE;//
                //listView.setLongClickable(false);
                return true;
            }
        });

    }

//    private void initDownloadList() {
//        MyDataBaseOpenHelper helper = new MyDataBaseOpenHelper(getApplicationContext(), Constants.DB_NAME, null, 1);
//        db = helper.getReadableDatabase();
//
//        cursor = db.query(Constants.DOWNLOAD_TABLE_NAME, column, null, null, null, null, null);
//        adapter = new DownLoadListAdapter(this, R.layout.download_list_item, cursor, column, ids, 0);
//        listView.setAdapter(adapter);
//        timer = new Timer();
//        TimerTask task = new TimerTask() {
//            @Override
//            public void run() {
//                cursor = db.query(Constants.DOWNLOAD_TABLE_NAME, column, null, null, null, null, null);
//                int allFinished = 1;
//                while (cursor.moveToNext()) {
//                    int itemFinished = cursor.getInt(cursor.getColumnIndex(Constants.DOWNLOAD_TABLE_ITEM_FINISHED));
//                    if (itemFinished == 0) {
//                        allFinished = 0;
//                        break;
//                    }
//                }
//                cursor.moveToFirst();
//                if (allFinished == 1) {
//                    Message msg = new Message();
//                    msg.what =allFinished;
//                    handler.sendMessage(msg);
//                    cancel();
//                } else {
//                    Message msg = new Message();
//                    msg.what =allFinished;
//                    handler.sendMessage(msg);
//                }
//            }
//        };
//        timer.schedule(task, 0, 200);
//    }

    @Override
    protected void onDestroy() {
//        timer.cancel();
        unregisterReceiver(receiver);
        super.onDestroy();
    }



    @Override
    public void onClick(View v) {
        int num = adapter.getCount();
        switch (v.getId()) {
            case R.id.download_activity_cancle_selection:
                adapter.setDisplayMode(NO_SELECT_MODE);
                listView.invalidateViews();
                listviewDisplayMode = NO_SELECT_MODE;
                controlBar.setVisibility(View.GONE);
                break;
            case R.id.download_activity_select_all:
                for (int i = 0; i < num; i++) {
                    adapter.checkBoxStates.put(i, true);
                }
                listView.invalidateViews();
                break;
            case R.id.download_activity_delete_selection:
                for (int i = 0; i < num; i++) {
                    if (adapter.checkBoxStates.get(i)) {
                        DownloadTools.getMyDownloadManager().removeDownloadedFile(i);
                    }
                }
                DownloadTools.getMyDownloadManager().deleteNullInfo();
                adapter.checkBoxStates.clear();
                adapter.iconMap.clear();
                DownloadTools.getMyDownloadManager().getDownloadInfos().clear();
                DownloadTools.getMyDownloadManager().initDownloadInfos();
                downloadInfos = DownloadTools.getMyDownloadManager().getDownloadInfos();

                adapter.notifyDataSetChanged();
                listView.invalidateViews();
        }
    }
}
