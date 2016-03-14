package com.study.zhiyang.webbrowser;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.PaintDrawable;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.webkit.CookieManager;
import android.webkit.URLUtil;
import android.webkit.WebChromeClient;
import android.webkit.WebIconDatabase;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.study.zhiyang.Constants;
import com.study.zhiyang.Search.BaiduSearchHint;
import com.study.zhiyang.database.MyDataBaseOpenHelper;
import com.study.zhiyang.download.DownloadListActivity;
import com.study.zhiyang.download.DownloadTools;
import com.study.zhiyang.download.MyDownloadManager;
import com.study.zhiyang.history.HistoryActivity;
import com.study.zhiyang.utils.DensityUtils;

import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity
        implements View.OnClickListener {
    //true 白天 false 夜间
    public static boolean dayNightMode;
    public static LinearLayout.LayoutParams paramNoLeft, paramLeft;


    public static WebViewClient webViewClient;
    //状态栏高度
    public static int statusBarHeight;
    private final String mUrl = "https://www.baidu.com";
    private String currentSearchEngine;
    private String[] searchEngines;
    SharedPreferences sp;
    public WebChromeClient.CustomViewCallback mCallback;
    public MyHorizontalScrollView horizontalScrollView;
    ImageView settingButton, homeButton, quit, reload;
    ArrayList<Integer> isScaledList;
    FrameLayout topParent;
    private View.OnLongClickListener onLongClickListener;
    private FrameLayout customWebView;
    private ImageView backButton, forwardButton;
    private ImageView downLoadActivityButton;
    public Button downloadingIndicator;
    private ImageView newPage;
    private ArrayList<WebPage> webPages;
    private Button pagesButton;
    private ImageView newPageIndicator;
    private ImageView day2night, history, settingFragment;
    private ImageView backEnlarge;
    private RelativeLayout bottomBar;
    private LinearLayout newPageLayout;
    //private WebView webView;
    private LinearLayout waper;
    //searchWindow UIs
    private TextView autoCompleteTextView;
    private ImageView searchEngine, deleteInput;
    private Button goTo;
    private SearchHintAdapter adapter;
    private List<String> searchHints;
    private ListView searchList;
    private Button clearSearchHistory;
    //    private View blankBackground;
    private MyDataBaseOpenHelper helper;
    private SQLiteDatabase database;

    private String[] searchHistoryCol = {Constants.SEARCH_TABLE_CONTENT, Constants.SEARCH_TABLE_TIME};

    class SearchHintAdapter extends ArrayAdapter {
        private List<String> data;

        public SearchHintAdapter(Context context, int resource, List<String> data) {
            super(context, resource, data);
            this.data = data;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final String s = data.get(position);
            Log.i("BaiduSearchHint", "GETVIEW" + s);
            View v = getLayoutInflater().inflate(R.layout.search_hint_item, null);
            TextView tv = (TextView) v.findViewById(R.id.search_hint_textview);
//            if (!MainActivity.dayNightMode) {
//                v.setBackgroundColor(Color.parseColor("#473C8B"));
//                tv.setTextColor(Color.WHITE);
//            } else {
//                v.setBackgroundColor(Color.WHITE);
//                tv.setTextColor(Color.BLACK);
//            }
            tv.setText(s);
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ContentValues values = new ContentValues();
                    values.put(Constants.SEARCH_TABLE_CONTENT, s);
                    values.put(Constants.SEARCH_TABLE_TIME, System.currentTimeMillis());
                    helper.addSearchHistory(values);
                    dismissSearchWindow();
                    openUrl(currentSearchEngine + s);
                }
            });
            return v;
        }
    }

    //    private Handler handler = new Handler() {
//        @Override
//        public void handleMessage(Message msg) {
//            switch (msg.what) {
//                case HITTYPE_IMAGE: {
//                    final PopupWindow popupWindow = new PopupWindow();
//                    popupWindow.setContentView(getLayoutInflater().
//                            inflate(R.layout.pic_source_popupwindow, null));
//                    popupWindow.setWidth(FrameLayout.LayoutParams.WRAP_CONTENT);
//                    popupWindow.setHeight(FrameLayout.LayoutParams.WRAP_CONTENT);
//                    // save = (TextView) findViewById(R.id.save_pic);
//                    popupWindow.showAtLocation(bottomBar, Gravity.CENTER, 0, 0);
//                    popupWindow.setFocusable(true);
//                    popupWindow.setOutsideTouchable(false);
//                    popupWindow.setTouchInterceptor(new View.OnTouchListener() {
//                        @Override
//                        public boolean onTouch(View v, MotionEvent event) {
//                            if (event.getY() < 240) {  //这里处理，当点击gridview以外区域的时候，菜单关闭
//                                if (popupWindow.isShowing())
//                                    popupWindow.dismiss();
//                            }
//                            Log.d("Demo", "popupWindow::onTouch >>> view: "
//                                    + v + ", event: " + event);
//                            return false;
//                        }
//                    });
//                    break;
//                }
//            }
//            super.handleMessage(msg);
//        }
//    };
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case WebPage.DELETE_CURRENT_PAGE:
                    horizontalScrollView.deletePageWithAnimators();
                    break;
                case MyHorizontalScrollView.CURRENT_PAGE_DELETED:
                    pagesButton.setText(horizontalScrollView.pageNum + "");
                    pagesButton.invalidate();
                    break;
                case MyHorizontalScrollView.PAGE_CLICKED:
                    Log.d("BROADCAST", "PAGE_CLICKED");
                    newPageLayout.setVisibility(View.GONE);
//                    enlargeCurrentPage();
                    break;
                case Constants.HISTORY_ITEM_CLICKED_INTENT:
                    ((WebPage) waper.getChildAt(horizontalScrollView.currentPage)).getWebView().
                            loadUrl(intent.getStringExtra("url"));
                    break;
                case WebPage.ORITATION_CHANGE_REQUEST:
                    if (customWebView.getVisibility() == View.VISIBLE) {
                        horizontalScrollView.setVisibility(View.GONE);
                        bottomBar.setVisibility(View.INVISIBLE);

                        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                                WindowManager.LayoutParams.FLAG_FULLSCREEN);
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    } else {
                        horizontalScrollView.setVisibility(View.VISIBLE);
                        bottomBar.setVisibility(View.VISIBLE);
                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    }
                    break;
                case MyDownloadManager.ONEDownloadingFinished:
                    downloadingIndicator.setText((Integer.parseInt(downloadingIndicator.getText().toString()) - 1) + "");
                    break;
                case MyDownloadManager.ALLDownloadingFinished:
                    downloadingIndicator.setText(0 + "");
                    downloadingIndicator.setVisibility(View.GONE);
                    break;
                case MyDownloadManager.NEWDOWNLOADTASKADDED:
                    if (downloadingIndicator.getVisibility() == View.GONE)
                        downloadingIndicator.setVisibility(View.VISIBLE);
                    String s = downloadingIndicator.getText().toString();
                    Log.d("indicator", s);
                    downloadingIndicator.setText(Integer.parseInt(s) + 1 + "");
                    break;

            }
        }
    };
    private WindowManager wm;
    private int width, height;
    private boolean isSettingWindowShowing = false;
    private boolean isWebViewScalled = false;
    private View settingView;
    private FrameLayout parentSettingView;
    private FrameLayout.LayoutParams params;
    private LinearLayout searchVindow;
    private TextWatcher watcher;
    private View.OnTouchListener listener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (v instanceof MyWebView) {
                if (((MyWebView) v).isWindowScaled)
                    return true;
                else return v.onTouchEvent(event);
            }
            return false;
        }
    };
    private String picUrl;
    private PopupWindow popupWindow;
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Log.d("searchWindow", msg.what + "");
            switch (msg.what) {
                case 0:
                    dismissSearchWindow();
                    ((WebPage) waper.getChildAt(horizontalScrollView.currentPage)).getWebView().loadUrl(currentSearchEngine + autoCompleteTextView.getText().toString());
                    break;
                case 1: {
                    dismissSearchWindow();
                    String url = autoCompleteTextView.getText().toString();
                    if (url.startsWith("https://") || url.startsWith("http://")) {
                        ((WebPage) waper.getChildAt(horizontalScrollView.currentPage)).getWebView().loadUrl(url);
                    } else {
                        ((WebPage) waper.getChildAt(horizontalScrollView.currentPage)).getWebView().loadUrl("http://" + url);
                    }
                    break;
                }
                case 3: {
                    final String url = (String) msg.getData().get("url");
                    if (url != null) {
                        popupWindow = new PopupWindow();
                        View view = getLayoutInflater().
                                inflate(R.layout.pic_source_popupwindow, null);
                        TextView openInBackGround = (TextView) view.findViewById(R.id.pic_source_open_in_background);
                        TextView save = (TextView) view.findViewById(R.id.pic_source_save);
                        TextView share = (TextView) view.findViewById(R.id.pic_source_share);
                        TextView openNewWindow = (TextView) view.findViewById(R.id.pic_source_new_window);
                        save.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                popupWindow.dismiss();
                                DownloadTools.getMyDownloadManager().addNewTask(getApplicationContext(), url, null, null, null, 0, MyDownloadManager.DOWNLOAD_TYPE_IMAGE);
                            }
                        });
                        share.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                            }
                        });
                        openNewWindow.setOnClickListener(

                                new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        popupWindow.dismiss();
                                        WebPage page = new WebPage(MainActivity.this);

                                        page.getWebView().loadUrl(url);
                                        shrinkCurrentPage();
                                        addNewWebPage(page);

                                        horizontalScrollView.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                horizontalScrollView.scrollToPage(horizontalScrollView.currentPage + 1);
                                                enlargeCurrentPage();
                                            }
                                        });
                                        page.getWebView().setOnLongClickListener(onLongClickListener);


                                    }
                                });
                        openInBackGround.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                popupWindow.dismiss();
                                WebPage page = new WebPage(MainActivity.this);
                                page.getWebView().loadUrl(url);
                                addNewWebPage(page);
                                page.getWebView().setLayerType(View.LAYER_TYPE_SOFTWARE, null);
                                page.getWebView().setOnLongClickListener(onLongClickListener);
                                page.setTranslationX(width * 3.0f / 8.0f);
                                horizontalScrollView.resetTranslationX();
                            }
                        });
                        popupWindow.setContentView(view);

                        popupWindow.setWidth(LinearLayout.LayoutParams.WRAP_CONTENT);
                        popupWindow.setHeight(LinearLayout.LayoutParams.WRAP_CONTENT);

                        // save = (TextView) findViewById(R.id.save_pic);
                        popupWindow.setFocusable(true);
                        popupWindow.setBackgroundDrawable(new PaintDrawable(Color.parseColor("#dd123456")));
                        popupWindow.setOutsideTouchable(true);


//                            int windowHeight = view.getHeight();
//                            int windowWidth = view.getWidth();
                        popupWindow.showAtLocation(bottomBar, Gravity.NO_GRAVITY,
                                (int) ((WebPage) waper.getChildAt(horizontalScrollView.currentPage)).getWebView().touchX,
                                (int) ((WebPage) waper.getChildAt(horizontalScrollView.currentPage)).getWebView().touchY);
                    }
                    break;
                }
            }
            super.handleMessage(msg);
        }

    };

    private int getStatusBarHeight() {
        if (statusBarHeight == 0) {
            try {
                Class<?> c = Class.forName("com.android.internal.R$dimen");
                Object o = c.newInstance();
                Field field = c.getField("status_bar_height");
                int x = (Integer) field.get(o);
                statusBarHeight = getResources().getDimensionPixelSize(x);
                Log.d("statusBarHeight", "~~~~~~~~~~~~~~~~~~" + statusBarHeight);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return statusBarHeight;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        topParent = (FrameLayout) getLayoutInflater().inflate(R.layout.activity_main, null);
        setContentView(topParent);
//        requestVisibleBehind(true);

        sp = PreferenceManager.getDefaultSharedPreferences(this);
        searchEngines = getResources().getStringArray(R.array.searchEngineAddressHttp);

        dayNightMode = sp.getBoolean("dayornight", true);
        if (true) WebIconDatabase.getInstance().open(getDir("icons", MODE_PRIVATE).getPath());
        getStatusBarHeight();
//        LinearLayout la = new LinearLayout(this);
        initScreenValue();
        IntentFilter filter = new IntentFilter();
        helper = new MyDataBaseOpenHelper(this, Constants.DB_NAME, null, 1);
        database = helper.getWritableDatabase();
        filter.addAction(WebPage.DELETE_CURRENT_PAGE);
        filter.addAction(WebPage.CURRENT_PAGE_NEW_LOADING);
        filter.addAction(MyHorizontalScrollView.PAGE_CLICKED);
        filter.addAction(MyHorizontalScrollView.CURRENT_PAGE_DELETED);
        filter.addAction(Constants.HISTORY_ITEM_CLICKED_INTENT);
        filter.addAction(WebPage.ORITATION_CHANGE_REQUEST);
        filter.addAction(MyDownloadManager.NEWDOWNLOADTASKADDED);
        filter.addAction(MyDownloadManager.ONEDownloadingFinished);
        filter.addAction(MyDownloadManager.ALLDownloadingFinished);
        registerReceiver(receiver, filter);
        paramNoLeft = new LinearLayout.LayoutParams(width, LinearLayout.LayoutParams.MATCH_PARENT);
//        paramNoLeft.gravity = Gravity.CENTER_VERTICAL;
        paramNoLeft.leftMargin = (int) (-width * 3.0f / 8.0f);
        paramLeft = new LinearLayout.LayoutParams(width, LinearLayout.LayoutParams.MATCH_PARENT);
//        paramLeft.gravity = Gravity.CENTER_VERTICAL;
        paramLeft.leftMargin = 0;
        initSettingView();
        initButtons();
        waper = (LinearLayout) horizontalScrollView.getChildAt(0);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setVisibility(View.VISIBLE);
        fab.setOnClickListener(this);
//        webViewClient = new WebViewClient() {
//            @Override
//            public void onPageFinished(WebView view, String url) {
//                //页面加载完成后加载下面的javascript，修改页面中所有用target="_blank"标记的url（在url前加标记为“newtab”）
//                view.loadUrl("javascript: var allLinks = document.getElementsByTagName('a'); if (allLinks) {var i;for (i=0; i<allLinks.length; i++) {var link = allLinks[i];var target = link.getAttribute('target'); if (target && target == '_blank') {link.setAttribute('target','_self');}}}");
//            }
//
//            @Override
//            public boolean shouldOverrideUrlLoading(WebView view, String url) {
////                view.loadUrl(url);
//                return false;
//            }
//
//            @Override
//            public void onPageStarted(WebView view, String url, Bitmap favicon) {
//                super.onPageStarted(view, url, favicon);
//            }
//        };

        onLongClickListener = new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (v instanceof MyWebView) {
                    final WebView.HitTestResult result = ((MyWebView) v).getHitTestResult();
                    int resultType = result.getType();
                    switch (resultType) {
                        case WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE: {
                            Toast.makeText(MainActivity.this, "SRC_IMAGE_ANCHOR_TYPE " + result.getExtra(), Toast.LENGTH_LONG).show();
                            picUrl = result.getExtra();

                            Message message = handler.obtainMessage();
                            message.what = 3;
                            ((MyWebView) v).requestFocusNodeHref(message);
                            break;
                        }
                        case WebView.HitTestResult.IMAGE_TYPE: {


                            Toast.makeText(MainActivity.this, "IMAGE_TYPE", Toast.LENGTH_SHORT).show();
                            picUrl = result.getExtra();
                            final PopupWindow imagePop = new PopupWindow();
                            View imagePopUpView = getLayoutInflater().inflate(R.layout.pic_popup, null);
                            imagePop.setContentView(imagePopUpView);
//                            Log.d("popupWindow",imagePopUpView.getParent().getClass().toString());
                            TextView imagePopSave = (TextView) imagePopUpView.findViewById(R.id.pic_pop_save);
                            TextView imagePopShare = (TextView) imagePopUpView.findViewById(R.id.pic_pop_share);
                            imagePopSave.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    imagePop.dismiss();
                                    DownloadTools.getMyDownloadManager().addNewTask(getApplicationContext(), picUrl, null, null, null, 0, MyDownloadManager.DOWNLOAD_TYPE_IMAGE);
                                }
                            });
                            imagePopShare.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    imagePop.dismiss();
                                }
                            });
                            imagePop.setHeight(LinearLayout.LayoutParams.WRAP_CONTENT);
                            imagePop.setHeight(LinearLayout.LayoutParams.WRAP_CONTENT);
                            imagePop.setWidth(LinearLayout.LayoutParams.WRAP_CONTENT);

                            imagePop.setBackgroundDrawable(new PaintDrawable(Color.parseColor("#dd123456")));
                            imagePop.setOutsideTouchable(true);
                            imagePop.setFocusable(true);
                            imagePop.showAtLocation(bottomBar, Gravity.NO_GRAVITY, (int) ((MyWebView) v).touchX, (int) ((MyWebView) v).touchY);
                            break;
                        }
                        case WebView.HitTestResult.UNKNOWN_TYPE:
                            Toast.makeText(MainActivity.this, "UNKNOWN_TYPE", Toast.LENGTH_SHORT).show();
                            break;

//
                        case WebView.HitTestResult.EDIT_TEXT_TYPE:
                            Toast.makeText(MainActivity.this, "EDIT_TEXT_TYPE", Toast.LENGTH_SHORT).show();

                            break;
                        case WebView.HitTestResult.EMAIL_TYPE:
                            Log.i("HitTestResult", "EMAIL_TYPE");
                            Toast.makeText(MainActivity.this, "EMAIL_TYPE", Toast.LENGTH_SHORT).show();
                            break;
                        case WebView.HitTestResult.SRC_ANCHOR_TYPE:
                            Log.i("HitTestResult", "SRC_ANCHOR_TYPE");
                            Toast.makeText(MainActivity.this, "SRC_ANCHOR_TYPE", Toast.LENGTH_SHORT).show();
                            popupWindow = new PopupWindow();
                            View view = getLayoutInflater().
                                    inflate(R.layout.link_source_popup, null);
                            popupWindow.setContentView(view);
                            popupWindow.setWidth(LinearLayout.LayoutParams.WRAP_CONTENT);
                            popupWindow.setHeight(LinearLayout.LayoutParams.WRAP_CONTENT);
                            // save = (TextView) findViewById(R.id.save_pic);
                            popupWindow.setFocusable(true);
                            popupWindow.setBackgroundDrawable(new PaintDrawable(Color.parseColor("#dd123456")));
                            popupWindow.setOutsideTouchable(true);
                            popupWindow.showAtLocation(bottomBar, Gravity.CENTER, 0, 0);
                            break;
                        case WebView.HitTestResult.GEO_TYPE:
                            Toast.makeText(MainActivity.this, "GEO_TYPE", Toast.LENGTH_SHORT).show();
                            break;
                        case WebView.HitTestResult.PHONE_TYPE:
                            Toast.makeText(MainActivity.this, "PHONE_TYPE", Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
                return false;
            }
        };
        initSearchWindow();
        webPages = new ArrayList<WebPage>();
        isScaledList = new ArrayList<Integer>();
        WebPage webPage = new WebPage(this);
        webPage.getWebView().loadUrl(mUrl);
        //webPage.getWebView().getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
//        webPage.getWebView().getSettings().setLoadWithOverviewMode(true);
        webPage.setOnTouchListener(listener);
//        webPage.resetTranslation();
        webPage.getWebView().setScaleX(1);
        webPage.getWebView().setScaleY(1);
        webPage.getButton().setTranslationY(1500);
        webPage.getWebView().isWindowScaled = false;
        webPage.getWebView().setOnLongClickListener(onLongClickListener);
//        webPage.getWebView().setLayerType(View.LAYER_TYPE_HARDWARE, null);
        Intent intent = getIntent();
        if (intent.getAction().equals("android.intent.action.VIEW")) {
            webPage.getWebView().loadUrl(getIntent().toUri(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT));
        }
//        webPage.resetTranslation();
        waper.addView(webPage, paramLeft);
        horizontalScrollView.currentPage = 0;
        horizontalScrollView.pageNum = 1;
        horizontalScrollView.post(new Runnable() {
            @Override
            public void run() {
                horizontalScrollView.scrollToPage(0);
            }
        });
        setSearchEngine();
        setCookie();
    }

    private void setSearchEngine() {
        int engineNum = Integer.valueOf(sp.getString(getString(R.string.preference_screen_setSearchEngine), "0"));
        currentSearchEngine = searchEngines[engineNum];
        if (engineNum == 0) {
            searchEngine.setImageResource(R.drawable.duniang);
        } else if (engineNum == 1) {
            searchEngine.setImageResource(R.drawable.sougou);
        } else searchEngine.setImageResource(R.drawable.bing);
    }

    private void initSearchWindow() {

        searchVindow = (LinearLayout) findViewById(R.id.search_window);
//        searchVindow.setOnClickListener(this);
        autoCompleteTextView = (EditText) findViewById(R.id.search_window_editAddress);
//        searchVindow.setOnClickListener(this);
//        autoCompleteTextView.setDropDownAnchor(R.id.search_window_topBar);

        searchEngine = (ImageView) findViewById(R.id.search_window_searchEngineIcon);
//        searchVindow.setOnClickListener(this);
        deleteInput = (ImageView) findViewById(R.id.search_window_delete_autocomplete_TextEdit_Content);
        deleteInput.setOnClickListener(this);
        goTo = (Button) findViewById(R.id.search_window_goTo);
        goTo.setOnClickListener(this);
        clearSearchHistory = (Button) findViewById(R.id.search_window_clearsearchHistory);
        clearSearchHistory.setOnClickListener(this);
        searchList = (ListView) findViewById(R.id.search_window_background);
//        blankBackground.setOnClickListener(this);
        autoCompleteTextView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    deleteInput.setVisibility(View.VISIBLE);
                }
            }
        });
        searchHints = new ArrayList<>();
        adapter = new SearchHintAdapter(this, R.layout.search_hint_item, searchHints);
        searchList.setAdapter(adapter);
//        autoCompleteTextView.setAdapter(adapter);
        watcher = new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                searchHints.clear();
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (autoCompleteTextView.getText().toString().equals("")) {
//                    clearAddress.clearAnimation();
                    deleteInput.setVisibility(View.INVISIBLE);
                } else {
                    deleteInput.setVisibility(View.VISIBLE);
//                    clearAddressContainer.setVisibility(VISIBLE);
                }
                String name = autoCompleteTextView.getText().toString();
                showHIS(name);
            }

            private void showHIS(final String name) {
                new AsyncTask<Void, Void, List<String>>() {
                    @Override
                    protected void onPostExecute(final List<String> result) {
                        super.onPostExecute(result);
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    protected List<String> doInBackground(Void... params) {
                        Log.i("showHIS", "CALLED");
                        String result = BaiduSearchHint.loginByHttpGet(
                                MainActivity.this, name);
                        Log.i("BaiduSearchHint", result);
//
                        Cursor c = database.query(Constants.SEARCH_TABLE_NAME, searchHistoryCol, null, null, null, null, null);
                        if (c != null) {
                            while (c.moveToNext()) {
                                searchHints.add(0, c.getString(c.getColumnIndex(Constants.SEARCH_TABLE_CONTENT)));
                            }
                        }
                        c.close();
                        String[] from = null;
                        if (result.indexOf("s:[\"") > 0) {
                            result = result.substring(result.indexOf("s:[\""));
                            result = result.replace("\"]})", "").replace(
                                    "s:[\"", "");
                            from = result.split("\",\"");
                        }
                        if (from != null) {
                            for (int i = from.length - 1; i >= 0; i--) {
                                searchHints.add(0, from[i]);
                                Log.i("BaiduSearchHint", from[i]);
                            }
                        }
                        return searchHints;
                    }
                }.execute();
            }
        };
        autoCompleteTextView.addTextChangedListener(watcher);
    }



    private void initSettingView() {
        params = new FrameLayout.LayoutParams(width, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.BOTTOM;
        parentSettingView = new FrameLayout(this);
        parentSettingView.setBackgroundColor(Color.parseColor("#992B2B2B"));
        parentSettingView.setClickable(true);
        parentSettingView.setOnClickListener(this);

        FrameLayout.LayoutParams contentParams = new FrameLayout.LayoutParams(Constants.screenWidth, Constants.screenHeight - DensityUtils.dp2px(this, 49.0f) - 1 - statusBarHeight);
        contentParams.gravity = Gravity.TOP;
        LayoutInflater inflater = getLayoutInflater();
        settingView = inflater.inflate(R.layout.setting_layout, null);
        settingView.setBackgroundColor(Color.WHITE);
        getWindow().addContentView(parentSettingView, contentParams);
        parentSettingView.addView(settingView, params);
        parentSettingView.setVisibility(View.INVISIBLE);


    }

    private void initScreenValue() {
        wm = getWindowManager();
        Display defaultDisplay = wm.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        defaultDisplay.getMetrics(metrics);
        width = metrics.widthPixels;
        height = metrics.heightPixels;
        Constants.screenWidth = width;
        Constants.screenHeight = height;
    }

    private void initButtons() {
        settingFragment = (ImageView) findViewById(R.id.settingtag);
        settingFragment.setOnClickListener(this);
        history = (ImageView) findViewById(R.id.tagandsaving);
        history.setOnClickListener(this);
        reload = (ImageView) findViewById(R.id.refresh);
        reload.setOnClickListener(this);
        quit = (ImageView) findViewById(R.id.quit);
        quit.setOnClickListener(this);
        newPageLayout = (LinearLayout) findViewById(R.id.newPageLayout);
        newPageLayout.setVisibility(View.GONE);
        backEnlarge = (ImageView) findViewById(R.id.backEnlarge);
        backEnlarge.setOnClickListener(this);
        newPage = (ImageView) findViewById(R.id.newPage);
        bottomBar = (RelativeLayout) findViewById(R.id.bottomBar);
//        bottomBar.setTranslationY(DensityUtils.dp2px(this,45));

        bottomBar.setVisibility(View.VISIBLE);
        horizontalScrollView = (MyHorizontalScrollView) findViewById(R.id.horizontalScrollView);
        horizontalScrollView.setEnabled(true);
//        pager = (MyViewPager) findViewById(R.id.viewPager);
        backButton = (ImageView) findViewById(R.id.back);
        forwardButton = (ImageView) findViewById(R.id.forward);
        settingButton = (ImageView) findViewById(R.id.setting);
        downloadingIndicator = (Button) findViewById(R.id.downloading_indicator);
        pagesButton = (Button) findViewById(R.id.pages);
        newPageIndicator = (ImageView) findViewById(R.id.indicator);
        homeButton = (ImageView) findViewById(R.id.home);
        backButton.setOnClickListener(this);
        forwardButton.setOnClickListener(this);
        settingButton.setOnClickListener(this);
        pagesButton.setOnClickListener(this);
        homeButton.setOnClickListener(this);
        day2night = (ImageView) findViewById(R.id.nightday);
        day2night.setOnClickListener(this);
        newPage.setOnClickListener(this);
        downLoadActivityButton = (ImageView) findViewById(R.id.download);
        downLoadActivityButton.setOnClickListener(this);
        customWebView = (FrameLayout) findViewById(R.id.customWebView);
        setDayNightUI();
    }

    public FrameLayout getCustomWebView() {
        return customWebView;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.back:
                if (((WebPage) waper
                        .getChildAt(horizontalScrollView.currentPage)).getWebView().canGoBack()) {
                    ((WebPage) waper
                            .getChildAt(horizontalScrollView.currentPage)).getWebView().goBack();
                }
                ((WebPage) waper.getChildAt(horizontalScrollView.currentPage)).getWebView().requestFocus();

                break;
            case R.id.forward:
                if (((WebPage) waper
                        .getChildAt(horizontalScrollView.currentPage)).getWebView().canGoForward()) {
                    ((WebPage) waper
                            .getChildAt(horizontalScrollView.currentPage)).getWebView().goForward();
                }
                ((WebPage) waper.getChildAt(horizontalScrollView.currentPage)).getWebView().requestFocus();

                break;
            case R.id.nightday: {
                dayNightMode = !dayNightMode;
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean("dayornight", dayNightMode);
                editor.commit();
                ((WebPage) waper.getChildAt(horizontalScrollView.currentPage)).onPause();
                ((WebPage) waper.getChildAt(horizontalScrollView.currentPage)).onResume();
                setDayNightUI();
                dismissSettingWindow();
//                Toast.makeText(this, "Hello", Toast.LENGTH_SHORT).show();
                Log.d("Button", "Tag Clicked");
//                ((WebPage)waper.getChildAt(horizontalScrollView.currentPage)).getWebView().requestFocus();

                break;
            }

            case R.id.settingtag:
                dismissSettingWindow();
                ((WebPage) waper.getChildAt(horizontalScrollView.currentPage)).getWebView().requestFocus();

                Intent intent = new Intent(MainActivity.this, SettingActivity.class);
                startActivityForResult(intent, 0);
                break;
            case R.id.setting:
                ((WebPage) waper.getChildAt(horizontalScrollView.currentPage)).getWebView().requestFocus();

                if (!isSettingWindowShowing) {
                    backButton.setVisibility(View.INVISIBLE);
                    forwardButton.setVisibility(View.INVISIBLE);
                    homeButton.setVisibility(View.INVISIBLE);
                    pagesButton.setVisibility(View.INVISIBLE);
                    settingButton.setImageResource(R.drawable.down_arrow);
                    showSettingWindow();

                } else {

                    backButton.setVisibility(View.VISIBLE);
                    forwardButton.setVisibility(View.VISIBLE);
                    homeButton.setVisibility(View.VISIBLE);
                    pagesButton.setVisibility(View.VISIBLE);
                    settingButton.setImageResource(R.drawable.more);
                    dismissSettingWindow();
//                    webView.setBackgroundColor(Color.parseColor("#000000"));
                    //parentSettingView.setVisibility(View.INVISIBLE);
                }
                break;
            case R.id.backEnlarge:
                newPageLayout.setVisibility(View.GONE);

                enlargeCurrentPage();
                break;
            case R.id.pages:
                newPageLayout.setVisibility(View.VISIBLE);
                shrinkCurrentPage();
                break;
            case R.id.newPage:
                newPageLayout.setVisibility(View.GONE);
                ((WebPage) waper.getChildAt(horizontalScrollView.currentPage)).getWebView().requestFocus();

                WebPage webPage = new WebPage(this);
                webPage.getWebView().setOnLongClickListener(onLongClickListener);
                webPage.getWebView().loadUrl("https://www.baidu.com");
                webPage.resetTranslation();
                addNewWebPage(webPage);
                horizontalScrollView.post(new Runnable() {
                    public void run() {
                        horizontalScrollView.scrollToPageAndEnlarge(horizontalScrollView.currentPage + 1);
                    }
                });

                break;
            case R.id.download: {
                dismissSettingWindow();
                ((WebPage) waper.getChildAt(horizontalScrollView.currentPage)).getWebView().requestFocus();

                Intent i = new Intent(MainActivity.this, DownloadListActivity.class);
                startActivity(i);
            }
            break;
            case R.id.refresh:
                dismissSettingWindow();
                ((WebPage) waper.getChildAt(horizontalScrollView.currentPage)).getWebView().requestFocus();

                ((WebPage) waper.getChildAt(horizontalScrollView.currentPage)).getWebView().reload();
                break;
            case R.id.quit:
                dismissSettingWindow();
                ((WebPage) waper.getChildAt(horizontalScrollView.currentPage)).getWebView().requestFocus();
                if (DownloadTools.getMyDownloadManager().checkDownloadingTaskEmpty()) {
                    showTips();
                } else {
                    AlertDialog alertDialog = new AlertDialog.Builder(this).setTitle("提醒")
                            .setMessage("正在下载文件是否确定退出？")
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface dialog, int which) {
                                    DownloadTools.getMyDownloadManager().stopAllDownLoadingTasks();
                                    Intent intent = new Intent(Intent.ACTION_MAIN);
                                    intent.addCategory(Intent.CATEGORY_HOME);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(intent);
                                    android.os.Process.killProcess(android.os.Process.myPid());
                                }

                            }).setNegativeButton("取消",

                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            return;
                                        }
                                    }).create(); // 创建对话框
                    alertDialog.show();
                }
                break;
            case R.id.tagandsaving: {
                ((WebPage) waper.getChildAt(horizontalScrollView.currentPage)).getWebView().requestFocus();
                dismissSettingWindow();
                Intent intent2 = new Intent(this, HistoryActivity.class);
                startActivity(intent2);
                break;
            }

            case R.id.search_window_background:
                dismissSearchWindow();
                break;
            case R.id.search_window_goTo: {
                InputMethodManager inputManager =
                        (InputMethodManager) autoCompleteTextView.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromInputMethod(autoCompleteTextView.getWindowToken(), 0);
                ContentValues values = new ContentValues();
                values.put(Constants.SEARCH_TABLE_CONTENT, autoCompleteTextView.getText().toString());
                values.put(Constants.SEARCH_TABLE_TIME, System.currentTimeMillis());
                helper.addSearchHistory(values);
                final String s = autoCompleteTextView.getText().toString();
                Log.d("searchWindow", s + " " +
                        "current Text");
                Runnable r = new Runnable() {
                    boolean isInetDomain = false;

                    @Override
                    public void run() {
                        String ss;
                        String url;
                        if ((s.startsWith("https://") || s.startsWith("http://"))) {
                            ss = s;
                        } else ss = "http://" + s;
                        url = ss;
                        if (URLUtil.isNetworkUrl(ss) && URLUtil.isValidUrl(ss)) {
                            try {
//
                                URL url1 = new URL(url);
                                HttpURLConnection connection = (HttpURLConnection) url1.openConnection();
//                                connection.setRequestMethod("GET");
                                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK)
                                    isInetDomain = true;
                                connection.disconnect();
                            } catch (Exception e) {

                            }

                        }
                        Message message = new Message();
                        if (isInetDomain) message.what = 1;
                        else message.what = 0;
                        handler.sendMessage(message);
                        isInetDomain = false;
                    }
                };
                new Thread(r).start();
            }
            break;
            case R.id.search_window_delete_autocomplete_TextEdit_Content:
                autoCompleteTextView.setText("");
                break;
//            case R.id.
            case R.id.search_window_clearsearchHistory:
                helper.clearSearchHistory();
                searchHints.clear();
                adapter.notifyDataSetChanged();
                break;
            default:
                if (isSettingWindowShowing) {
                    dismissSettingWindow();
                    isSettingWindowShowing = false;
                }
        }
    }

    public void showSearchWindow() {
//        ((WebPage) waper.getChildAt(horizontalScrollView.currentPage)).onPause();
        autoCompleteTextView.setText(((WebPage) waper.getChildAt(horizontalScrollView.currentPage)).getWebView().getUrl());
        autoCompleteTextView.requestFocus();
        searchVindow.setVisibility(View.VISIBLE);
        searchVindow.bringToFront();
        Log.d("SearchWindow", "showed");
    }

    private void dismissSearchWindow() {
//        ((WebPage) waper.getChildAt(horizontalScrollView.currentPage)).onResume();

        (waper.getChildAt(horizontalScrollView.currentPage)).requestFocus();
        searchVindow.setVisibility(View.GONE);
    }

    private void setDayNightUI() {
        if (!dayNightMode) {

            day2night.setImageResource(R.drawable.moon);
            settingView.setBackgroundColor(Color.parseColor("#303040"));
            bottomBar.setBackgroundColor(Color.parseColor("#303040"));
        } else {
            day2night.setImageResource(R.drawable.sun);
            settingView.setBackgroundColor(Color.parseColor("#fdfdfd"));
            bottomBar.setBackgroundColor(Color.parseColor("#fdfdfd"));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        boolean settingChanged = data.getBooleanExtra("changed", false);
        if (settingChanged) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            boolean picEnable = sharedPreferences.getBoolean(getString(R.string.preference_screen_setLoadPic), false);
            int n = horizontalScrollView.pageNum;
            for (int i = 0; i < n; i++) {
                ((WebPage) waper.getChildAt(i)).getWebView().getSettings().setBlockNetworkImage(picEnable);
            }
            setSearchEngine();
            setCookie();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void setCookie() {
        CookieManager cm = CookieManager.getInstance();
        cm.setAcceptCookie(sp.getBoolean(getString(R.string.preference_screen_allow_cookie), true));
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(receiver);
        super.onDestroy();
    }

    private void shrinkCurrentPage() {
        setBackForwardIcon();
        ((WebPage) waper.getChildAt(horizontalScrollView.currentPage)).getButton().setVisibility(View.VISIBLE);
        ((WebPage) waper.getChildAt(horizontalScrollView.currentPage)).getLoadingprogress().setVisibility(View.GONE);
        ((WebPage) waper.getChildAt(horizontalScrollView.currentPage)).resetTranslation();
        ((WebPage) waper.getChildAt(horizontalScrollView.currentPage)).getWebView().requestFocus();
        ((WebPage) waper.getChildAt(horizontalScrollView.currentPage)).getWebView().setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        ((WebPage) waper.getChildAt(horizontalScrollView.currentPage)).onPause();
        topParent.setMotionEventSplittingEnabled(false);
//        ((WebPage) waper
//                .getChildAt(horizontalScrollView.currentPage)).webviewContainer.setTranslationY(0);
        ((WebPage) waper
                .getChildAt(horizontalScrollView.currentPage)).getTopBar().setVisibility(View.INVISIBLE);
        bottomBar.setVisibility(View.INVISIBLE);
        ObjectAnimator buttonTranslationY = ObjectAnimator.ofFloat(((WebPage) waper
                .getChildAt(horizontalScrollView.currentPage)).getButton(), "translationY", DensityUtils.dp2px(getApplicationContext(), 210), 0);
        buttonTranslationY.setDuration(300);
        ObjectAnimator buttonScaleX = ObjectAnimator.ofFloat(((WebPage) waper
                .getChildAt(horizontalScrollView.currentPage)).getButton(), "scaleX", 1, 0.5f);
        buttonScaleX.setDuration(300);
        ObjectAnimator buttonScaleY = ObjectAnimator.ofFloat(((WebPage) waper
                .getChildAt(horizontalScrollView.currentPage)).getButton(), "scaleY", 1, 0.5f);
        buttonScaleY.setDuration(300);
        ObjectAnimator scaleXAnimator = ObjectAnimator.ofFloat(((WebPage) waper.getChildAt(horizontalScrollView.currentPage)).getWebView(), "scaleX", 1, 0.5f);
        scaleXAnimator.setDuration(300);

        ObjectAnimator scaleYAnimator = ObjectAnimator.ofFloat(((WebPage) waper.getChildAt(horizontalScrollView.currentPage)).getWebView(), "scaleY", 1, 0.5f);
        scaleYAnimator.setDuration(300);
        if (horizontalScrollView.currentPage == 0 && horizontalScrollView.pageNum != 1) {
            ObjectAnimator marginRightAnimator = ObjectAnimator.ofFloat(waper.getChildAt(horizontalScrollView.currentPage + 1), "translationX", width * 3.0f / 8.0f, 0);
            marginRightAnimator.setDuration(300);
            AnimatorSet set = new AnimatorSet();
            set.play(scaleXAnimator).with(scaleYAnimator);
            set.play(scaleXAnimator).with(marginRightAnimator);
            set.play(buttonTranslationY).with(buttonScaleX);
            set.play(scaleYAnimator).with(buttonScaleY);
            set.start();
        } else if (horizontalScrollView.currentPage == horizontalScrollView.pageNum - 1 && horizontalScrollView.pageNum != 1) {
            ObjectAnimator marginLeftAnimator = ObjectAnimator.ofFloat(waper.getChildAt(horizontalScrollView.currentPage - 1), "translationX", -width * 3.0f / 8.0f, 0);
            marginLeftAnimator.setDuration(300);
            AnimatorSet set = new AnimatorSet();
            set.play(scaleXAnimator).with(scaleYAnimator);
            set.play(marginLeftAnimator).with(scaleXAnimator);
            set.play(buttonTranslationY).with(buttonScaleX);
            set.play(scaleYAnimator).with(buttonScaleY);
            set.start();
        } else if (horizontalScrollView.pageNum == 1) {
            AnimatorSet set = new AnimatorSet();
            set.play(scaleXAnimator).with(scaleYAnimator);
            set.play(buttonTranslationY).with(buttonScaleX);
            set.play(scaleYAnimator).with(buttonScaleY);
            set.start();
        } else {
            ObjectAnimator marginLeftAnimator = ObjectAnimator.ofFloat(waper.getChildAt(horizontalScrollView.currentPage - 1), "translationX", -width * 3.0f / 8.0f, 0);
            marginLeftAnimator.setDuration(300);
            ObjectAnimator marginRightAnimator = ObjectAnimator.ofFloat(waper.getChildAt(horizontalScrollView.currentPage + 1), "translationX", width * 3.0f / 8.0f, 0);
            marginRightAnimator.setDuration(300);
            AnimatorSet set = new AnimatorSet();
            set.play(scaleXAnimator).with(scaleYAnimator);
            set.play(marginLeftAnimator).with(marginRightAnimator);
            set.play(buttonTranslationY).with(buttonScaleX);
            set.play(scaleYAnimator).with(buttonScaleY);
            set.start();
        }
        ((WebPage) waper.getChildAt(horizontalScrollView.currentPage)).getWebView().isWindowScaled = true;
    }

    public void enlargeCurrentPage() {
        setBackForwardIcon();
        ((WebPage) waper.getChildAt(horizontalScrollView.currentPage)).onResume();

        ((WebPage) waper.getChildAt(horizontalScrollView.currentPage)).getWebView().setLayerType(View.LAYER_TYPE_HARDWARE, null);
        ((WebPage) waper.getChildAt(horizontalScrollView.currentPage)).getWebView().requestFocus();
        ObjectAnimator scaleXAnimator = ObjectAnimator.ofFloat(((WebPage) waper
                .getChildAt(horizontalScrollView.currentPage)).getWebView(), "scaleX", 0.5f, 1);
        scaleXAnimator.setDuration(200);
        ObjectAnimator buttonTranslationY = ObjectAnimator.ofFloat(((WebPage) waper
                .getChildAt(horizontalScrollView.currentPage)).getButton(), "translationY", 0, DensityUtils.dp2px(getApplicationContext(), 210));
        buttonTranslationY.setDuration(200);
        ObjectAnimator buttonScaleX = ObjectAnimator.ofFloat(((WebPage) waper
                .getChildAt(horizontalScrollView.currentPage)).getButton(), "scaleX", 0.5f, 1);
        buttonScaleX.setDuration(200);
        ObjectAnimator buttonScaleY = ObjectAnimator.ofFloat(((WebPage) waper
                .getChildAt(horizontalScrollView.currentPage)).getButton(), "scaleY", 0.5f, 1);
        buttonScaleY.setDuration(200);
        ObjectAnimator scaleYAnimator = ObjectAnimator.ofFloat(((WebPage) waper
                .getChildAt(horizontalScrollView.currentPage)).getWebView(), "scaleY", 0.5f, 1);
        scaleYAnimator.setDuration(200);
        if (horizontalScrollView.currentPage == 0 && horizontalScrollView.pageNum != 1) {
            ObjectAnimator marginRightAnimator = ObjectAnimator.ofFloat(waper.getChildAt(horizontalScrollView.currentPage + 1), "translationX", 0, width * 3.0f / 8.0f);


            marginRightAnimator.setDuration(200);
            AnimatorSet set = new AnimatorSet();
            set.play(scaleXAnimator).with(scaleYAnimator);
            set.play(buttonTranslationY).with(buttonScaleX);
            set.play(scaleYAnimator).with(buttonScaleY);
            set.play(scaleXAnimator).with(marginRightAnimator);
            set.start();
        } else if (horizontalScrollView.currentPage == horizontalScrollView.pageNum - 1 && horizontalScrollView.pageNum != 1) {
            ObjectAnimator marginLeftAnimator = ObjectAnimator.ofFloat(waper.getChildAt(horizontalScrollView.currentPage - 1), "translationX", 0, -width * 3.0f / 8.0f);

            marginLeftAnimator.setDuration(200);
            AnimatorSet set = new AnimatorSet();
            set.play(scaleXAnimator).with(scaleYAnimator);
            set.play(marginLeftAnimator).with(scaleXAnimator);
            set.play(buttonTranslationY).with(buttonScaleX);
            set.play(scaleYAnimator).with(buttonScaleY);
            set.start();
        } else if (horizontalScrollView.pageNum == 1) {
            AnimatorSet set = new AnimatorSet();
            set.play(scaleXAnimator).with(scaleYAnimator);
            set.play(buttonTranslationY).with(buttonScaleX);
            set.play(scaleYAnimator).with(buttonScaleY);
            set.start();
        } else {
            ObjectAnimator marginLeftAnimator = ObjectAnimator.ofFloat(waper.getChildAt(horizontalScrollView.currentPage - 1), "translationX", 0, -width * 3.0f / 8.0f);
            marginLeftAnimator.setDuration(200);
            ObjectAnimator marginRightAnimator = ObjectAnimator.ofFloat(waper.getChildAt(horizontalScrollView.currentPage + 1), "translationX", 0, width * 3.0f / 8.0f);
            marginRightAnimator.setDuration(200);
            AnimatorSet set = new AnimatorSet();
            set.play(scaleXAnimator).with(scaleYAnimator);
            set.play(buttonTranslationY).with(buttonScaleX);
            set.play(scaleYAnimator).with(buttonScaleY);
            set.play(marginLeftAnimator).with(marginRightAnimator);
            set.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    ((WebPage) waper
                            .getChildAt(horizontalScrollView.currentPage)).getButton().setVisibility(View.GONE);
                    ((WebPage) waper.getChildAt(horizontalScrollView.currentPage)).getWebView().setLayerType(View.LAYER_TYPE_HARDWARE, null);

                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            set.start();
        }
        ((WebPage) waper.getChildAt(horizontalScrollView.currentPage)).getTopBar().setVisibility(View.VISIBLE);
        bottomBar.setVisibility(View.VISIBLE);

        ((WebPage) waper.getChildAt(horizontalScrollView.currentPage)).getWebView().isWindowScaled = false;
    }

    private void addNewWebPage(WebPage webPage) {
        ObjectAnimator animatorScaleX = ObjectAnimator.ofFloat(newPageIndicator, "scaleX", 0.3f, 1.0f);
        ObjectAnimator animatorScaleY = ObjectAnimator.ofFloat(newPageIndicator, "scaleY", 0.3f, 1.0f);
        ObjectAnimator animatorAlpha = ObjectAnimator.ofFloat(newPageIndicator, "alpha", 1.0f, 0.0f);
        animatorAlpha.setDuration(500);
        animatorScaleX.setDuration(500);
        animatorScaleY.setDuration(500);
        AnimatorSet set = new AnimatorSet();
        set.play(animatorScaleX).with(animatorScaleY);
        set.play(animatorScaleX).with(animatorAlpha);
        set.start();
        webPage.setOnTouchListener(listener);
        ((WebPage) waper.getChildAt(horizontalScrollView.currentPage)).resetTranslation();
        waper.addView(webPage, horizontalScrollView.currentPage + 1, paramNoLeft);
        horizontalScrollView.pageNum++;
        pagesButton.setText(horizontalScrollView.pageNum + "");
    }

    private void showSettingWindow() {
        parentSettingView.setVisibility(View.VISIBLE);
        Animation popup = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.popshow_anim);
        popup.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        settingView.startAnimation(popup);
        isSettingWindowShowing = true;
    }

    private void dismissSettingWindow() {
        backButton.setVisibility(View.VISIBLE);
        forwardButton.setVisibility(View.VISIBLE);
        homeButton.setVisibility(View.VISIBLE);
        pagesButton.setVisibility(View.VISIBLE);
        settingButton.setImageResource(R.drawable.more);
        Animation remove = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.removefrombottom_anim);
        remove.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                Log.d("Animation Started", "Child Animation");
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                parentSettingView.setVisibility(View.INVISIBLE);
                Log.d("Remove", "Child removed");
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        settingView.startAnimation(remove);
        isSettingWindowShowing = false;

    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (intent.getAction().equals("android.intent.action.VIEW")) {
            ((WebPage) waper.getChildAt(horizontalScrollView.currentPage)).getWebView().loadUrl(intent.toUri(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT));
        }
        super.onNewIntent(intent);
    }

    private void showTips() {

        AlertDialog alertDialog = new AlertDialog.Builder(this).setTitle("提醒")
                .setMessage("是否退出程序")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Intent.ACTION_MAIN);
                        intent.addCategory(Intent.CATEGORY_HOME);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        android.os.Process.killProcess(android.os.Process.myPid());
                    }

                }).setNegativeButton("取消",

                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                return;
                            }
                        }).create(); // 创建对话框
        alertDialog.show(); // 显示对话框
    }

    public void setBackForwardIcon() {
        if (((WebPage) waper.getChildAt(horizontalScrollView.currentPage)).getWebView().canGoBack()) {
            backButton.setImageResource(R.drawable.ic_action_backword_active);
        } else {
            backButton.setImageResource(R.drawable.ic_action_backword_default);
        }
        if (((WebPage) waper.getChildAt(horizontalScrollView.currentPage)).getWebView().canGoForward()) {
            forwardButton.setImageResource(R.drawable.ic_action_forward_active);
        } else {
            forwardButton.setImageResource(R.drawable.ic_action_forward_default);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            if (searchVindow.getVisibility() == View.VISIBLE) {
                dismissSearchWindow();
                return true;
            } else {
                if (customWebView.getVisibility() == View.VISIBLE) {
                    ((WebPage) waper.getChildAt(horizontalScrollView.currentPage)).myWebChromeClient.onHideCustomView();
                } else {
                    if (((WebPage) waper.getChildAt(horizontalScrollView.currentPage)).getWebView().canGoBack()) {
                        ((WebPage) waper.getChildAt(horizontalScrollView.currentPage)).getWebView().goBack();
                    } else {
                        showTips();
                    }
                }
                return false;
            }
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            AudioManager am = (AudioManager) getSystemService(AUDIO_SERVICE);
            am.adjustVolume(AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            AudioManager am = (AudioManager) getSystemService(AUDIO_SERVICE);
            am.adjustVolume(AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_MUTE) {
            AudioManager am = (AudioManager) getSystemService(AUDIO_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (am.isStreamMute(AudioManager.STREAM_MUSIC))
                    am.adjustVolume(AudioManager.ADJUST_MUTE, AudioManager.FLAG_SHOW_UI);
                else am.adjustVolume(AudioManager.ADJUST_UNMUTE, AudioManager.FLAG_SHOW_UI);
            }
        }
        return true;
    }


    public void openUrl(String s) {
        ((WebPage) waper.getChildAt(horizontalScrollView.currentPage)).getWebView().loadUrl(s);
    }

}
