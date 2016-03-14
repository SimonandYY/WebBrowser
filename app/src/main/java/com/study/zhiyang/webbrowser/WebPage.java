package com.study.zhiyang.webbrowser;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.DownloadListener;
import android.webkit.GeolocationPermissions;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.PermissionRequest;
import android.webkit.URLUtil;
import android.webkit.ValueCallback;
import android.webkit.WebBackForwardList;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.study.zhiyang.Constants;
import com.study.zhiyang.adblock.AdBlock;
import com.study.zhiyang.database.MyDataBaseOpenHelper;
import com.study.zhiyang.download.DownloadTools;
import com.study.zhiyang.download.MyDownloadManager;
import com.study.zhiyang.utils.DensityUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

/**
 * Created by zhiyang on 2015/12/27.
 */
public class WebPage extends FrameLayout {


    private boolean isInNightMode;
    private MyDataBaseOpenHelper dataBaseOpenHelper;
    //    private SQLiteDatabase db;
    private long currentHistoryDatabaseIndex;
    private boolean fistTimeLaunch = true;
    private Bitmap currentIcon;
    private WebBackForwardList webBackForwardList;
    private String formerEndListSite;
    private String currentTitle;
    private MyWebView mWebView;
    private ImageView deletePage;
    private FrameLayout container;
    private LinearLayout topBar,topBarAddressBackGround;

    public LinearLayout webviewContainer;
    private WebViewClient webViewClient;
    private boolean isTopBarShowing = true;
    public static int topBarHeight;
    private Context mContext;
    private ImageView iconView, addTofavorite;
    private ProgressBar loadingprogress;

    public ProgressBar getLoadingprogress() {
        return loadingprogress;
    }

    public TextView address;
    private String s = "";
    private boolean isHorizental = false;
    private boolean isVertical = false;
    float startY, tempY, startX, tempX;
    private View customView;
    private WebChromeClient.CustomViewCallback mCallback;
    public MyWebChromeClient myWebChromeClient;
    private SharedPreferences sharedPreferences;
    private String[] defaultEngineAddress;
    private AdBlock adBlock;

    public void resetTranslation() {
        webviewContainer.setTranslationY(-topBarHeight);
        topBar.setTranslationY(-topBarHeight);
        topBar.setVisibility(INVISIBLE);
        isTopBarShowing = false;
    }

    public static final String DELETE_CURRENT_PAGE = "com.mybrowser.DELETE_BUTTON_CLICKED";
    public static final String CURRENT_PAGE_NEW_LOADING = "com.mybrowser.CURRENT_PAGE_NEW_LOADING";
    public static final String ORITATION_CHANGE_REQUEST = "com.mybrowser.ORITATION_CHANGE_REQUEST";

    final class InJavaScriptLocalObj {
        public void showSource(String html) {
            Log.d("HTML", html);
        }
    }

    public WebPage(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.webpage, this, true);
        mWebView = (MyWebView) findViewById(R.id.pagewebview);
        topBar = (LinearLayout) findViewById(R.id.topBar);
        deletePage = (ImageView) findViewById(R.id.deletecurrentPage);
        loadingprogress = (ProgressBar) findViewById(R.id.webLoadProgress);
        Log.d("ViewPage Created", "true");
    }

    public WebPage(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private boolean checkLongPress(long a, long b) {
        return Math.abs(b - a) > 500;
    }

    final class SetLayerType {
        @JavascriptInterface
        public void setLayerTypeHardware() {
            Message msg = new Message();
            msg.what = 5;
            handler.sendMessage(msg);
            Log.d("LayerType,", "setLayerTypeHardware");

        }

        @JavascriptInterface
        public void setLayerTypeSoftware() {
            Message msg = new Message();
            msg.what = 6;
            handler.sendMessage(msg);
            Log.d("LayerType,", "setLayerTypeSoftware");

        }
    }

    public WebPage(final Context context) {
        super(context);

        dataBaseOpenHelper = new MyDataBaseOpenHelper(context, Constants.DB_NAME, null, 1);
//        db = dataBaseOpenHelper.getWritableDatabase();
        mContext = context;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        isInNightMode = !sharedPreferences.getBoolean("dayornight", true);
        defaultEngineAddress = mContext.getResources().getStringArray(R.array.searchEngineAddressHttp);
//        defaultEngine = Integer.valueOf(sharedPreferences.getString(mContext.getString(R.string.preference_screen_setSearchEngine), "0"));
        LayoutInflater.from(context).inflate(R.layout.webpage, this, true);
        adBlock = new AdBlock(context);
        mWebView = (MyWebView) findViewById(R.id.pagewebview);
        mWebView.setLayerType(LAYER_TYPE_HARDWARE, null);
//        mWebView.bringToFront();
        deletePage = (ImageView) findViewById(R.id.deletecurrentPage);
        container = (FrameLayout) findViewById(R.id.linear);
        loadingprogress = (ProgressBar) findViewById(R.id.webLoadProgress);
        topBar = (LinearLayout) findViewById(R.id.topBar);
        topBarHeight = DensityUtils.dp2px(context, 45.0f);
        topBarAddressBackGround = (LinearLayout) findViewById(R.id.topBar_address_back);
        iconView = (ImageView) findViewById(R.id.searchEngine);
        topBar.setVisibility(VISIBLE);
        if (isInNightMode){
            topBar.setBackgroundColor(Color.parseColor("#303040"));
            topBarAddressBackGround.setBackgroundColor(Color.parseColor("#505060"));
            mWebView.setBackgroundColor(Color.parseColor("#202030"));
        }
        addTofavorite = (ImageView) findViewById(R.id.webpage_add_to_favorite);
        addTofavorite.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ContentValues values = new ContentValues();
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                try {
                    currentIcon.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                } catch (Exception e) {

                }
                byte[] iconByte = byteArrayOutputStream.toByteArray();
                values.put(Constants.FAVORITE_TABLE_TITLE, mWebView.getTitle());
                values.put(Constants.FAVORITE_TABLE_TIME, System.currentTimeMillis());
                values.put(Constants.FAVORITE_TABLE_URL, mWebView.getUrl());
                values.put(Constants.FAVORITE_TABLE_ICON, iconByte);
                dataBaseOpenHelper.addFavorite(values);
                addTofavorite.setImageResource(R.drawable.favorite_added);
            }
        });
//        clearAddressContainer = (FrameLayout) findViewById(R.id.delete_autocomplete_TextEdit_Content_container);
//        clearAddress = (ImageView) findViewById(R.id.delete_autocomplete_TextEdit_Content);
//        clearAddress.clearAnimation();
//        clearAddress.setVisibility(INVISIBLE);
        address = (TextView) findViewById(R.id.editAddress);
        address.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) mContext).showSearchWindow();
            }
        });
//        if (!MainActivity.dayNightMode) {
//            address.setBackgroundColor(Color.parseColor("#473C8B"));
//            address.setTextColor(Color.WHITE);
//        } else {
//            address.setBackgroundColor(Color.WHITE);
//            address.setTextColor(Color.BLACK);
//        }
//        address.setDropDownAnchor(R.id.topBar);
//        address.setDropDownWidth(MyHorizontalScrollView.width);
//        address.setDropDownVerticalOffset(topBarHeight/4);
//        clearAddress.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                address.setText("");
//            }
//        });
        topBar.setTranslationY(-topBarHeight);
//        goTo = (Button) findViewById(R.id.goTo);
//        goTo.setFocusable(true);
//        goTo.setFocusableInTouchMode(true);
        webviewContainer = (LinearLayout) findViewById(R.id.pagewebview1);
        webviewContainer.setTranslationY(-topBarHeight);
//        goTo.setOnClickListener(new GoToClickListener());
//        goTo.setOnTouchListener(new GoToClickListener());
//        searchHints = new ArrayList<>();
//        adapter = new SearchHintAdapter(mContext, R.layout.search_hint_item, searchHints);
//        address.setAdapter(adapter);
//
//        watcher = new TextWatcher() {
//
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//
//                searchHints.clear();
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//                if (address.getText().toString().equals("")) {
////                    clearAddress.clearAnimation();
//                    clearAddressContainer.setVisibility(INVISIBLE);
//                } else {
////                    clearAddressContainer.setVisibility(VISIBLE);
//                }
//                if (!fistTimeLaunch) {
//                    String name = address.getText().toString();
////                    if (searchHints != null) {
////                        for (int i = 0; i < searchHints.size(); i++) {
////                            Log.i("BaiduSearchHint", "DATA " + searchHints.get(i));
////                        }
////                    }
//                    showHIS(name);
//                } else {
//                    address.clearFocus();
//                    address.dismissDropDown();
//                    fistTimeLaunch = false;
//                }
//            }

//            private void showHIS(final String name) {
//                new AsyncTask<Void, Void, List<String>>() {
//                    @Override
//                    protected void onPostExecute(final List<String> result) {
//                        super.onPostExecute(result);
//                        adapter = new SearchHintAdapter(mContext, R.layout.search_hint_item, result);
//                        address.setAdapter(adapter);
//                        adapter.notifyDataSetChanged();
//                    }
//
//                    @Override
//                    protected List<String> doInBackground(Void... params) {
//                        Log.i("showHIS", "CALLED");
//                        String result = BaiduSearchHint.loginByHttpGet(
//                                mContext, name);
//                        Log.i("BaiduSearchHint", result);
////
//                        String[] from = null;
//                        if (result.indexOf("s:[\"") > 0) {
//                            result = result.substring(result.indexOf("s:[\""));
//                            result = result.replace("\"]})", "").replace(
//                                    "s:[\"", "");
//                            from = result.split("\",\"");
//                        }
//                        if (from != null) {
//                            for (int i = 0; i < from.length; i++) {
//                                searchHints.add(from[i]);
//                                Log.i("BaiduSearchHint", from[i]);
//                            }
//                        }
////                searchCount = searchs.size();// 搜索记录的数量
////                if (searchs.size() < 10 && from != null)
////                    for (int i = 0; i < from.length; i++) {
////                        if (searchs.contains(from[i]))
////                            continue;
////                        searchs.add(from[i]);
////                        if (searchs.size() >= 10)
////                            break;
////                    }
////                if (mSearchTextView.getText().length() <= 0) {
////                    List<String> searchs = browserDao.getSearch(10,
////                            null);
////                    searchCount = searchs.size();
////                    if (searchs.size() > 0) {
////                        searchs.add("删除搜索记录");
////                    }
////                }
//                        return searchHints;
//                    }
//                }.execute();
//            }
//        };
//        address.addTextChangedListener(watcher);
//        address.setOnKeyListener(new View.OnKeyListener() {
//
//            @Override
//            public boolean onKey(View v, int keyCode, KeyEvent event) {
//                if (keyCode == KeyEvent.KEYCODE_ENTER) {
////					navigateToUrl();
//                    InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
//                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
//                    return true;
//                }
//
//                return false;
//            }
//        });
//        address.setOnFocusChangeListener(new OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View v, boolean hasFocus) {
//                if (!hasFocus) {
//                    s = address.getText().toString();
//                    clearAddressContainer.setVisibility(INVISIBLE);
//                    goTo.setVisibility(GONE);
//                    address.setText(mWebView.getTitle());
//                    address.invalidate();
//
//                } else {
//                    address.setText(mWebView.getUrl());
//                    clearAddressContainer.setVisibility(VISIBLE);
//                    goTo.setVisibility(VISIBLE);
//                    address.invalidate();
//                }
//            }
//        });
//        address.setFocusable(true);

//        address.setOnTouchListener(new OnTouchListener() {
//            float touchX, tempX;
//            long startTime, temTime;
//            boolean poped = false;
//            boolean isClick = true;
//            boolean allSelected = false;
//
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                PopupWindow clipBoardPopUpWindow = new PopupWindow();
//                switch (event.getAction()) {
//
//                    case MotionEvent.ACTION_DOWN:
//                        touchX = event.getX();
//                        startTime = System.currentTimeMillis();
//
//                        break;
//                    case MotionEvent.ACTION_MOVE:
//                        tempX = event.getX();
//                        temTime = System.currentTimeMillis();
//                        if (checkLongPress(temTime, startTime) && !poped) {
//                            clipBoardPopUpWindow = new PopupWindow();
//                            ClipboardManager clipboardManager = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
//                            ClipData data = clipboardManager.getPrimaryClip();
//                            View popupview = LayoutInflater.from(mContext).inflate(R.layout.clipboard_pop_window, null);
//                            TextView paste = (TextView) popupview.findViewById(R.id.clipboard_paste);
//                            TextView pasteAndGo = (TextView) popupview.findViewById(R.id.clipboard_paste_and_go);
//                            TextView copy = (TextView) popupview.findViewById(R.id.clipboard_copy);
//                            clipBoardPopUpWindow.setFocusable(true);
//                            clipBoardPopUpWindow.setOutsideTouchable(true);
//                            clipBoardPopUpWindow.setContentView(popupview);
//                            clipBoardPopUpWindow.setHeight(LinearLayout.LayoutParams.WRAP_CONTENT);
//                            clipBoardPopUpWindow.setWidth(LinearLayout.LayoutParams.WRAP_CONTENT);
//                            clipBoardPopUpWindow.setBackgroundDrawable(new ColorDrawable());
//                            clipBoardPopUpWindow.showAsDropDown(address, (int) touchX, 0);
//                            clipBoardPopUpWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
//                                @Override
//                                public void onDismiss() {
//                                    poped = false;
//                                    isClick = true;
//                                }
//                            });
//                            poped = true;
//                            isClick = false;
//                        }
//                        break;
//                    case MotionEvent.ACTION_UP:
//                        if (isClick) {
//                            address.requestFocus();
//                            int size = address.getText().length();
//                            if (allSelected) {
//                                address.setSelection(size);
//                                allSelected = false;
//                            } else {
//
//                                address.setSelection(0, size);
//                                allSelected = true;
//                            }
//                            InputMethodManager inputManager =
//                                    (InputMethodManager) address.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
////                            address.performClick();
//                            inputManager.showSoftInput(address, 0);
//                        }
//                        break;
//
//                }
//                return true;
//            }
//        });
//        address.clearFocus();


//        Looper.prepare();
//        handler = new Handler(){
//            @Override
//            public void handleMessage(Message msg) {
//                super.handleMessage(msg);
//            }
//        };
        deletePage.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(DELETE_CURRENT_PAGE);
                context.sendBroadcast(intent);
                // container.invalidate();
                Log.d("Button Clicked", "deletePage");
            }
        });
        webViewClient = new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                Log.d("CHECK OPERATION", "onPageFinished");

//                if(url.contains("qq.com"))view.loadUrl("javascript: var meta = document.getElementsByTagName('meta');if(meta.length){for(var i = 0;i<meta.length;i++){var viewport = meta[i].getAttribute('name');if(viewport == 'viewport'){meta[i].setAttribute('content','width=device-width,initial-scale=1,minimum-scale=1,maximum-scale=1,user-scalable=no');}}}");


//                if (url.contains("qq.com/") || url.contains("youku.com/") || url.contains("iqiyi.com/") || url.contains("tudou.com") || url.contains("bilibili.com")) {
//                    mWebView.setLayerType(LAYER_TYPE_HARDWARE, null);
//                    mWebView.containsVideo = true;
//                    Log.d("LayerType,", mWebView.getLayerType() +url+ "url____________________________________________");
//                } else if (!url.contains("baidu.com")){
//                    view.addJavascriptInterface(new SetLayerType(), "ss");
//                    view.loadUrl("javascript: var v=document.getElementsByTagName('video'); "
//                            + "if(v.length){window.ss.setLayerTypeHardware();} else {window.ss.setLayerTypeSoftware();}");
//                }
                Log.d("LayerType,", mWebView.getLayerType() + "");
//                view.loadUrl("javascript: var video=document.getElementsByTagName('video');if(video.length){video.style.position = 'absolute';video.style.top = 0;}");

                Log.d("LayerType,", mWebView.getLayerType() + "");

//                 js="javascript: var v=document.getElementsByTagName('video')[0]; "+"v.webkitEnterFullscreen(); ";
//                view.loadUrl(js);
//                mWebView.requestFocus();


//                address.setText(mWebView.getTitle());

                ((MainActivity) mContext).setBackForwardIcon();
                super.onPageFinished(view, url);
                Log.d("CHECK OPERATION", "onPageFinished");
            }


            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
//                view.loadUrl(url);
//                view.stopLoading();
//                if (url.startsWith("aa")) {
//                    url = url.substring(2, url.length());
//                    view.loadUrl(url);
//                    return true;
//                } else
                return false;
            }


            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    if (adBlock.isAd(request.getUrl().toString()))
                        return new WebResourceResponse("text/plain", "UTF-8", new ByteArrayInputStream("".getBytes()));
                    return super.shouldInterceptRequest(view, request.getUrl().toString());
                }
                return super.shouldInterceptRequest(view, request);
            }

            public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
                if (adBlock.isAd(url))
                    return new WebResourceResponse("text/plain", "UTF-8", new ByteArrayInputStream("".getBytes()));
                return super.shouldInterceptRequest(view, url);
            }

            @Override
            public void doUpdateVisitedHistory(WebView view, String url, boolean isReload) {
                Log.d("CHECK OPERATION", "doUpdateVisitedHistory 0");

                addTofavorite.setImageResource(R.drawable.favorite_not_added);
                webBackForwardList = mWebView.copyBackForwardList();
                ContentValues values = new ContentValues();
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                try {
                    currentIcon.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                } catch (Exception e) {

                }
                byte[] iconByte = byteArrayOutputStream.toByteArray();
                values.put(Constants.HISTORY_TABLE_TITLE, "");
                values.put(Constants.HISTORY_TABLE_TIME, System.currentTimeMillis());
                values.put(Constants.HISTORY_TABLE_URL, webBackForwardList.getCurrentItem().getUrl());
                values.put(Constants.HISTORY_TABLE_ICON, iconByte);
                currentHistoryDatabaseIndex = dataBaseOpenHelper.addHistory(values);
                ((MainActivity) mContext).setBackForwardIcon();
                super.doUpdateVisitedHistory(view, url, isReload);
                Log.d("CHECK OPERATION", "doUpdateVisitedHistory 1");
                Log.d("LayerType,", mWebView.getLayerType() + "");

            }


            @Override
            public void onLoadResource(WebView view, String url) {
                Log.d("CHECK OPERATION", "onLoadResource");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    Log.d("LogName0,", Build.VERSION.SDK_INT + "");
                    view.evaluateJavascript("var allLinks = document.getElementsByTagName('a'); if (allLinks)" +
                            "{var i;for (i=0; i<allLinks.length; i++) {var link = allLinks[i];var target =" +
                            "link.getAttribute('target'); if (target && target == '_blank')" +
                            "{link.setAttribute('target','_self');link.href = 'aa' + link.href;}" +
                            "if (target == 'video'){link.setAttribute('target','_self');}}}", new ValueCallback<String>() {
                        @Override
                        public void onReceiveValue(String value) {
                            Log.d("LogName0", s);
                        }
                    });

                    if (isInNightMode) {
                        view.evaluateJavascript("document.documentElement.style.backgroundColor = 'rgba(0,0,0,0)';", new ValueCallback<String>() {
                            @Override
                            public void onReceiveValue(String value) {
                                Log.d("LogName1", s);
                            }
                        });
                        view.evaluateJavascript("var all = document.getElementsByTagName('*');if(all){var i;for(i=0;i<all.length;i++){var div = all[i];if(div.style.color!=null)div.style.color='#7AB800';if(div.style.backgroundColor!=null)div.style.backgroundColor = 'rgba(0,0,0,0)';}}", new ValueCallback<String>() {
                            @Override
                            public void onReceiveValue(String value) {
                                Log.d("LogName2", s);
                            }
                        });

//                view.loadUrl("javascript: var title = document.getElementsById('title');if(title){for(int i = 0;i<title.length;i++){title[i].style.color = '#eeeeee'}");
//                view.loadUrl("javascript: var title = document.getElementsById('articleTitle');if(title){for(int i = 0;i<title.length;i++){title[i].style.color = '#eeeeee'}");
                    }
                } else {


                    view.loadUrl("javascript: var allLinks = document.getElementsByTagName('a'); if (allLinks) " +
                            "{var i;for (i=0; i<allLinks.length; i++) {var link = allLinks[i];var target = " +
                            "link.getAttribute('target'); " +
                            "{link.setAttribute('target','_self');}}}");

//                    String js = "javascript: var v=document.getElementsByTagName('video'); "
//                            + "if(v) window.ss.setLayerTypeHardware();else window.ss.setLayerTypeSoftware();";


                    Log.i("ISINNIGHTMODE", isInNightMode + "");
                    if (isInNightMode) {
                        view.loadUrl("javascript: document.documentElement.style.backgroundColor = '#202030';");
                        view.setBackgroundColor(Color.parseColor("#202030"));
                        view.loadUrl("javascript:  var all = document.getElementsByTagName('*');if(all){var i;for(i=0;i<all.length;i++){var div = all[i];if(div.style.color!=null)div.style.color='#aaaaaa';if(div.style.backgroundColor!=null)div.style.backgroundColor = 'RGBA(20,20,20,0.0)';}}");
                    } else {
                        view.setBackgroundColor(Color.parseColor("#ffffff"));
                    }
                }
//                if(url.contains("qq.com"))view.loadUrl("javascript: var meta = document.getElementsByTagName('meta');if(meta.length){for(var i = 0;i<meta.length;i++){var viewport = meta[i].getAttribute('name');if(viewport == 'viewport'){meta[i].setAttribute('content','width=device-width,initial-scale=1,minimum-scale=1,maximum-scale=1,user-scalable=no');}}}");

//                if (!loadingFinished)
//                    mWebView.stopLoading();
//                mWebView.getSettings().setJavaScriptEnabled(false);
//                view.loadUrl("javascript: var title = document.getElementsByTagName('video');if(title){for(var i;i<title.length;i++){ title[i].style.webkit-playsinline = 'false';}}");


                super.onLoadResource(view, url);
                Log.d("CHECK OPERATION", "onLoadResource");
                Log.d("LayerType,", mWebView.getLayerType() + "");
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                Log.d("CHECK OPERATION", "onPageStarted");

                if (url.contains("qq.com"))
                    view.loadUrl("javascript: var meta = document.getElementsByTagName('meta');if(meta.length){for(var i = 0;i<meta.length;i++){var viewport = meta[i].getAttribute('name');if(viewport == 'viewport'){meta[i].setAttribute('content','width=device-width,initial-scale=1,minimum-scale=1,maximum-scale=1,user-scalable=no');}}}");

                if (favicon != null) {
                    iconView.setImageBitmap(favicon);
                    currentIcon = favicon;
                }

//                view.setBackgroundColor(Color.parseColor("#00ff00"));

//                webBackForwardList = mWebView.copyBackForwardList();
//                if (fistTimeLaunch){
//                     formerEndListSite="";
//                    fistTimeLaunch = false;
//                }
//                else formerEndListSite = webBackForwardList.getItemAtIndex(webBackForwardList.getSize()-1).getUrl();
//                Log.d("CHECK OPERATION", "onPageStarted");
//                loadingFinished = false;
                super.onPageStarted(view, url, favicon);
                Log.d("CHECK OPERATION", "onPageStarted");
                Log.d("LayerType,", mWebView.getLayerType() + "");
            }

        };
        mWebView.setLongClickable(true);
        mWebView.getSettings().setSupportMultipleWindows(false);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setLoadsImagesAutomatically(true);
        mWebView.getSettings().setUseWideViewPort(true);
        mWebView.getSettings().setLoadWithOverviewMode(true);
        mWebView.getSettings().setSaveFormData(true);
        mWebView.getSettings().setSupportMultipleWindows(true);
        mWebView.getSettings().setDomStorageEnabled(true);
        mWebView.getSettings().setBuiltInZoomControls(true);
        mWebView.getSettings().setDisplayZoomControls(false);
        mWebView.getSettings().setSupportZoom(true);
        mWebView.getSettings().setAppCacheEnabled(true);
//        mWebView.getSettings().setAppCacheMaxSize();
        mWebView.getSettings().setDatabaseEnabled(true);
        mWebView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        mWebView.getSettings().setGeolocationEnabled(true);
        mWebView.getSettings().getUserAgentString();
        mWebView.getSettings().setUserAgentString("Mozilla/5.0 (Linux; Android 4.4.4; Nexus 5 Build/KTU84P) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/38.0.2125.114 Mobile Safari/537.36");
        Log.i("USERAGENT", mWebView.getSettings().getUserAgentString());
        mWebView.getSettings().setGeolocationDatabasePath("data/data/com.study.zhiyang.webbrowser/database");
//        mWebView.getSettings().setMediaPlaybackRequiresUserGesture();
        mWebView.setWebViewClient(webViewClient);
        myWebChromeClient = new MyWebChromeClient();
        mWebView.setWebChromeClient(myWebChromeClient);
        mWebView.getSettings().setBlockNetworkImage(sharedPreferences.getBoolean(mContext.getString(R.string.preference_screen_setLoadPic), true));
        //mWebView.loadUrl("http://3g.163.com");
        mWebView.setScaleX(0.5f);
        mWebView.setScaleY(0.5f);

        mWebView.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(context, "Long Click", Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        mWebView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(final String url, final String userAgent, final String contentDisposition, final String mimetype, final long contentLength) {
                View view = LayoutInflater.from(mContext).inflate(R.layout.download_confirm_window, null);
                final PopupWindow downloadConfirmPopupWindow = new PopupWindow();
                downloadConfirmPopupWindow.setContentView(view);
                downloadConfirmPopupWindow.setWidth(LinearLayout.LayoutParams.MATCH_PARENT);
                downloadConfirmPopupWindow.setHeight(LinearLayout.LayoutParams.MATCH_PARENT);
                downloadConfirmPopupWindow.setOutsideTouchable(true);
                downloadConfirmPopupWindow.setFocusable(true);
                downloadConfirmPopupWindow.showAtLocation(mWebView, Gravity.NO_GRAVITY, 0, 0);
                Log.d("Download", "Started-----------");
                Button confirm = (Button) view.findViewById(R.id.downloadConfirmation_yes);
                Button cancle = (Button) view.findViewById(R.id.downloadConfirmation_no);
                TextView fileName = (TextView) view.findViewById(R.id.downloadConfirmation_filename);
                fileName.setText("即将从下载" + url.substring(url.lastIndexOf("/") + 1));
                confirm.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        downloadConfirmPopupWindow.dismiss();
//                        new DownloadAsyncTask(mContext.getApplicationContext(),url, userAgent, contentDisposition, mimetype, contentLength).execute();
                        DownloadTools.getMyDownloadManager().
                                addNewTask(mContext.getApplicationContext(), url, userAgent, contentDisposition, mimetype, contentLength, MyDownloadManager.DOWNLOAD_TYPE_NO_IMAGE);

                    }
                });
                cancle.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        downloadConfirmPopupWindow.dismiss();
                    }
                });

            }

        });


        mWebView.setOnTouchListener(new OnTouchListener() {


            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (mWebView.getScaleX() == 1) {
//                            topBar.setVisibility(VISIBLE);
                        }
                        startY = event.getY();
                        startX = event.getX();

                        return mWebView.onTouchEvent(event);
                    case MotionEvent.ACTION_MOVE:
                        tempY = event.getY();
                        tempX = event.getX();
                        if (!isHorizental && !isVertical) {
                            if (Math.abs(tempX - startX) > Math.abs(tempY - startY) && Math.abs(tempX - startX) > 5)
                                isHorizental = true;
                            else
                                isVertical = true;
                        }
                        if (isHorizental) {
//                            event.setLocation(tempX, startY);
                            return mWebView.onTouchEvent(event);
                        }
//                        if (isTopBarShowing) {
//                            if (tempY - startY < 0 && tempY - startY > -2.0f * topBarHeight) {
//                                setTranslationY((tempY - startY) / 2.0f);
//                                return mWebView.onTouchEvent(event);
//                            } else if (tempY - startY <= -2.0f * topBarHeight) {
//                                setTranslationY(-topBarHeight);
//                                isTopBarShowing = false;
//                                return mWebView.onTouchEvent(event);
//                            } else if (tempY - startY > 0)
//                                return mWebView.onTouchEvent(event);
//                        } else {
//                            if (tempY - startY > 0 && tempY - startY < 2.0f * topBarHeight) {
//                                setTranslationY((tempY - startY) / 2.0f - topBarHeight);
//                                return mWebView.onTouchEvent(event);
//                            } else if (tempY - startY >= 2.0f * topBarHeight) {
//                                setTranslationY(0);
//                                isTopBarShowing = true;
//                                return mWebView.onTouchEvent(event);
//                            } else if (tempY - startY < 0)
//                                return mWebView.onTouchEvent(event);
//                        }
                        else {
//                            event.setLocation(startX, tempY);
                            if (isTopBarShowing) {
                                if (tempY - startY <= 0 && tempY - startY > -2.0f * topBarHeight) {
                                    topBar.setTranslationY((tempY - startY) / 2.0f);
                                    webviewContainer.setTranslationY((tempY - startY) / 2.0f);
//                                    topBar.setTranslationY((tempY - startY));
//                                    webviewContainer.setTranslationY(tempY - startY);
//                                    mWebView.invalidate();
                                    break;
//                                    break;
                                } else if (tempY - startY <= -2.0f * topBarHeight) {
//                                    topBar.setTranslationY(-topBarHeight);
//                                    topBar.setVisibility(INVISIBLE);
                                    topBar.setTranslationY(-topBarHeight);
                                    webviewContainer.setTranslationY(-topBarHeight);
//                                    webviewContainer.setTranslationY(-topBarHeight);
                                    isTopBarShowing = false;
//                                    mWebView.invalidate();
                                    break;
//                                    return mWebView.onTouchEvent(event);
                                } else if (tempY - startY > 0)
                                    break;
                                //return mWebView.onTouchEvent(event);
                            } else {
                                if (tempY - startY >= 0 && tempY - startY < 2.0f * topBarHeight) {
//                                    topBar.setTranslationY((tempY - startY) / 2.0f - topBarHeight);
//                                    topBar.setVisibility(VISIBLE);
                                    topBar.setTranslationY(-topBarHeight + (tempY - startY) / 2.0f);
                                    webviewContainer.setTranslationY(-topBarHeight + (tempY - startY) / 2.0f);
//                                    webviewContainer.setTranslationY(-topBarHeight + (tempY - startY));
//                                    mWebView.invalidate();
                                    // return mWebView.onTouchEvent(event);
//                                    return true;
                                    break;
                                } else if (tempY - startY >= topBarHeight) {
                                    topBar.setTranslationY(0);
                                    webviewContainer.setTranslationY(0);
                                    isTopBarShowing = true;
//                                    mWebView.invalidate();
//                                    return mWebView.onTouchEvent(event);
                                    break;
                                } else if (tempY - startY < 0)
                                    break;

//                                    return mWebView.onTouchEvent(event);
                            }
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        if (topBar.getTranslationY() < -topBarHeight / 2) {
                            ObjectAnimator topBarAnimator = ObjectAnimator.ofFloat(topBar, "translationY", topBar.getTranslationY(), -topBarHeight);
                            topBarAnimator.setDuration(200);
                            ObjectAnimator webContainerAnimator = ObjectAnimator.ofFloat(webviewContainer, "translationY", webviewContainer.getTranslationY(), -topBarHeight);
                            webContainerAnimator.setDuration(200);
//                            webContainerAnimator.start();
                            AnimatorSet set = new AnimatorSet();
                            set.play(topBarAnimator).with(webContainerAnimator);
                            set.start();
                            webviewContainer.setTranslationY(0);
                            topBar.setTranslationY(-topBarHeight);
                            isTopBarShowing = false;
                            // topBar.setVisibility(GONE);
                        } else {
                            ObjectAnimator topBarAnimator = ObjectAnimator.ofFloat(topBar, "translationY", topBar.getTranslationY(), 0);
                            topBarAnimator.setDuration(200);
                            ObjectAnimator webContainerAnimator = ObjectAnimator.ofFloat(webviewContainer, "translationY", webviewContainer.getTranslationY(), 0);
                            webContainerAnimator.setDuration(200);
//                            webContainerAnimator.start();
                            AnimatorSet set = new AnimatorSet();
                            set.play(topBarAnimator).with(webContainerAnimator);
                            set.start();
                            isTopBarShowing = true;
//                            webviewContainer.setTranslationY(topBarHeight);
//                            topBar.setTranslationY(0);
                        }
                        isHorizental = false;
                        isVertical = false;
                        break;
//                        ?return mWebView.onTouchEvent(event);
                }
                return mWebView.onTouchEvent(event);
//                return false;
            }
        });
        Log.d("ViewPage Created", "true");

    }


    @Override
    public void setScaleX(float scaleX) {
        Log.d("X size", "Width " + container.getWidth() + " Height:  " +
                container.getHeight() + " Scale : " + container.getScaleX() + " " +
                container.getScaleY());
        super.setScaleX(scaleX);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    public MyWebView getWebView() {
        return mWebView;
    }

    public ImageView getButton() {
        return deletePage;
    }

    public LinearLayout getTopBar() {
        return topBar;
    }

    class MyWebChromeClient extends WebChromeClient {
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            Log.d("CHECK OPERATION", "onProgressChanged");
            if (newProgress == 100) {
                loadingprogress.setVisibility(GONE);
            } else {
                if (loadingprogress.getVisibility() == GONE)
                    loadingprogress.setVisibility(VISIBLE);
                loadingprogress.setProgress(newProgress);
            }
            super.onProgressChanged(view, newProgress);
            Log.d("CHECK OPERATION", "onProgressChanged");

        }

        @Override
        public void onReceivedIcon(WebView view, Bitmap icon) {
            iconView.setImageBitmap(icon);
            Log.d("CHECK OPERATION", "onReceivedIcon");
            currentIcon = icon;
            ContentValues updateIconValues = new ContentValues();
            ByteArrayOutputStream iconOutputStream = new ByteArrayOutputStream();
            icon.compress(Bitmap.CompressFormat.PNG, 100, iconOutputStream);
            byte[] bytes = iconOutputStream.toByteArray();
            updateIconValues.put(Constants.HISTORY_TABLE_ICON, bytes);
            dataBaseOpenHelper.updateHisrory(currentHistoryDatabaseIndex, updateIconValues);
            super.onReceivedIcon(view, icon);
            Log.d("CHECK OPERATION", "onReceivedIcon");

        }

        @Override
        public void onReceivedTitle(WebView view, String title) {
            Log.d("CHECK OPERATION", "onReceivedTitle");
            address.setText(title);
            currentTitle = title;
//            if (webBackForwardList.getCurrentIndex()!=webBackForwardList.getSize()-1){
            ContentValues updateTitleValues = new ContentValues();
            updateTitleValues.put(Constants.HISTORY_TABLE_TITLE, title);
            dataBaseOpenHelper.updateHisrory(currentHistoryDatabaseIndex, updateTitleValues);
//            }

            super.onReceivedTitle(view, title);
            Log.d("CHECK OPERATION", "onReceivedTitle");
        }

        @Override
        public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
            return super.onJsAlert(view, url, message, result);
        }


        @Override
        public void onGeolocationPermissionsShowPrompt(final String origin, final GeolocationPermissions.Callback callback) {
            final boolean remember = false;
            boolean locPermit = sharedPreferences.getBoolean(mContext.getString(R.string.preference_screen_setLocPermit), false);
            if (locPermit) {
                callback.invoke(origin, true, remember);
            } else {
                callback.invoke(origin, false, remember);
            }
//            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
//            builder.setTitle("Locations");
//            builder.setMessage(origin + " Would like to use your Current Location").setCancelable(true).setPositiveButton("Allow",
//                    new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog,
//                                            int id) {
//                            // origin, allow, remember
//                            callback.invoke(origin, true, remember);
//                        }
//                    })
//                    .setNegativeButton("Don't Allow",
//                            new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialog,
//                                                    int id) {
//                                    // origin, allow, remember
//                                    callback.invoke(origin, false, remember);
//                                }
//                            });
//            AlertDialog alert = builder.create();
//            alert.show();
        }

        @Override
        public void onPermissionRequest(PermissionRequest request) {
            super.onPermissionRequest(request);
        }

        @Override
        public void onShowCustomView(View view, CustomViewCallback callback) {

            if (view != null) {
                customView = view;
//                customView.setRotation(90);
                VideoView videoView;
                mWebView.setLayerType(LAYER_TYPE_SOFTWARE, null);
                if (view instanceof FrameLayout) {
                    if (((FrameLayout) view).getFocusedChild() instanceof VideoView) {
                        videoView = (VideoView) ((FrameLayout) view).getFocusedChild();

                        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mp) {
                                Log.d("VideoView", "Completed");
                                onHideCustomView();
                            }
                        });
                        videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                            @Override
                            public boolean onError(MediaPlayer mp, int what, int extra) {
                                onHideCustomView();
                                return true;
                            }
                        });
                    }
                }
//                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(Constants.screenHeight,Constants.screenWidth);
//                Log.i("VideoView", view.getClass().toString());
//                Log.i("VideoView", ((FrameLayout) view).getChildAt(0).getClass().toString());
                ((MainActivity) mContext).getCustomWebView().setVisibility(VISIBLE);
                ((MainActivity) mContext).mCallback = callback;
                ((MainActivity) mContext).getCustomWebView().addView(customView);
                mCallback = callback;
                Intent intent = new Intent(ORITATION_CHANGE_REQUEST);
                mContext.sendBroadcast(intent);
//                ((MainActivity) mContext).horizontalScrollView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);

                super.onShowCustomView(view, callback);
            }
        }

        @Override
        public void onHideCustomView() {
            if (((MainActivity) mContext).mCallback != null) {
                mWebView.setLayerType(LAYER_TYPE_HARDWARE, null);
                ((MainActivity) mContext).getCustomWebView().removeView(customView);
                ((MainActivity) mContext).getCustomWebView().setVisibility(GONE);

                mWebView.setVisibility(VISIBLE);
                ((LinearLayout) getParent()).invalidate();
                Intent intent = new Intent(ORITATION_CHANGE_REQUEST);
                mContext.sendBroadcast(intent);
                ((MainActivity) mContext).mCallback.onCustomViewHidden();
                Log.d("VideoView", "onHIdeCustomView called");
                ((MainActivity) mContext).mCallback = null;
//                Log.d("VideoView", ((MainActivity) mContext).horizontalScrollView.getTranslationX() + "");

                super.onHideCustomView();
                ((MainActivity) mContext).horizontalScrollView.scrollToPage(((MainActivity) mContext).horizontalScrollView.currentPage);
            }
        }
    }

    boolean checkFinished = false;


    class GoToClickListener implements View.OnTouchListener {
//        @Override
//        public void onClick(View v) {
////            goTo.requestFocus();
//            Log.d("ADDRESS",s);
//            address.clearFocus();
//            InputMethodManager inputManager =
//                    (InputMethodManager) address.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
////            if (inputManager.isActive()) {
//            inputManager.hideSoftInputFromInputMethod(address.getWindowToken(), 0);
////            }
//            //s = address.getText().toString();
//
//            Runnable r = new Runnable() {
//                boolean isInetDomain = false;
//
//                @Override
//                public void run() {
//                    Log.d(s, "------------" );
//                    try {
//
//                        InetAddress a = InetAddress.getByName(s);
//                        if (a.isReachable(500)) {
//                            isInetDomain = true;
//                        }
//
//                    } catch (UnknownHostException e) {
//                        e.printStackTrace();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                    // checkFinished = true;
//                    Message message = new Message();
//                    if (isInetDomain) message.what = 1;
//                    else message.what = 0;
//                    handler.sendMessage(message);
//                    Log.d(s, "------------" + isInetDomain);
//                }
//            };
//            new Thread(r).start();
//        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_UP:
                    InputMethodManager inputManager =
                            (InputMethodManager) address.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputManager.hideSoftInputFromInputMethod(address.getWindowToken(), 0);
//                    goTo.requestFocus();
//                    address.clearFocus();
//                    ( (MainActivity)mContext).horizontalScrollView.scrollToPage(((MainActivity) mContext).horizontalScrollView.currentPage);
//                    goTo.clearFocus();
//                    mWebView.requestFocus();
                    Runnable r = new Runnable() {
                        boolean isInetDomain = false;

                        @Override
                        public void run() {
//                            Log.d(s, "------------");
//                            String ss = "";
//                            if (s.startsWith("https://")) {
//                                ss = s.substring(8, s.length());
//
//                            } else if (s.startsWith("http://")) {
//                                ss = s.substring(7, s.length());
//                            } else {
//                                ss = s;
//                            }
//                            while (ss.endsWith("/")){
//                                ss=ss.substring(0,ss.length()-1);
//                            }
//                            try {
//
//                                InetAddress a = InetAddress.getByName(ss);
//                                if (a.isReachable(500)) {
//                                    isInetDomain = true;
//                                }
//
//                            } catch (UnknownHostException e) {
//                                e.printStackTrace();
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                            }
                            // checkFinished = true;

//                            try {
//                                String ss;
//                                URL url;
//                                if ((s.startsWith("https://") || s.startsWith("http://"))) {
//                                    ss = s;
//                                } else ss = "http://" + s;
//                                url = new URL(ss);
//
//                                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
////                                connection.setRequestMethod("GET");
//                                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK)
//                                    isInetDomain = true;
//                                connection.disconnect();
//                            } catch (Exception e) {
//
//                            }
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
                            Log.d(s, "------------" + isInetDomain);
                        }
                    };
                    new Thread(r).start();
                    break;
            }
            return false;
        }
    }


    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Log.d("Message Received", msg.what + "");
//            mWebView.requestFocus();
//            ( (MainActivity)mContext).horizontalScrollView.scrollToPage(((MainActivity) mContext).horizontalScrollView.currentPage);

            switch (msg.what) {

                case 0:
                    int engine = Integer.valueOf(sharedPreferences.getString(mContext.getString(R.string.preference_screen_setSearchEngine), "0"));
                    mWebView.loadUrl(defaultEngineAddress[engine] + s);

                    break;
                case 1:
                    if (s.startsWith("http://") || s.startsWith("https://")) {
                        mWebView.loadUrl(s);
                    } else {
                        mWebView.loadUrl("http://" + s);
                    }
                    break;
                case 5:
                    mWebView.containsVideo = true;
                    if (mWebView.getLayerType() == LAYER_TYPE_SOFTWARE)
                        try {
                            mWebView.setLayerType(LAYER_TYPE_HARDWARE, null);
                        } catch (Exception e) {
                        }
                    break;
                case 6:
                    mWebView.containsVideo = false;
                    if (mWebView.getLayerType() == LAYER_TYPE_HARDWARE)
                        try {
                            mWebView.setLayerType(LAYER_TYPE_SOFTWARE, null);
                        } catch (Exception e) {

                        }
                    Log.d("LayerType,", mWebView.getLayerType() + "");

                    break;
            }
        }
    };


    private void updateAdapter(final List<String> result) {
    }

    public void onPause() {
        mWebView.onPause();
    }

    public void onResume() {
        boolean daynight = sharedPreferences.getBoolean("dayornight", true);
        Log.d("ISINNIGHTMODE", daynight + " onResume");
        if (daynight) {
            if (isInNightMode) {
                isInNightMode = false;
                Log.d("ISINNIGHTMODE", daynight + "" + isInNightMode + " onResume");
                mWebView.clearCache(true);
                mWebView.loadUrl(mWebView.getUrl());
                topBar.setBackgroundColor(Color.parseColor("#3F51B5"));
                topBarAddressBackGround.setBackgroundColor(Color.WHITE);
            }
        } else {
            if (!isInNightMode) {
                isInNightMode = true;
                Log.d("ISINNIGHTMODE", daynight + "" + isInNightMode + " onResume");
                mWebView.clearCache(true);
                mWebView.loadUrl(mWebView.getUrl());
                topBar.setBackgroundColor(Color.parseColor("#303040"));
                topBarAddressBackGround.setBackgroundColor(Color.parseColor("#505060"));
            }
        }
        mWebView.onResume();
    }
    public void setTopBarBackground(){
        topBar.setBackgroundColor(Color.parseColor("#303040"));
        topBarAddressBackGround.setBackgroundColor(Color.parseColor("#505060"));
    }

}


