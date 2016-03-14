package com.study.zhiyang.download;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.study.zhiyang.Constants;
import com.study.zhiyang.database.MyDataBaseOpenHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by zhiyang on 2016/1/13.
 */
public class MyDownloadManager {
    private Context mContext;
    private MyDataBaseOpenHelper dataBaseOpenHelper;
    private SQLiteDatabase db;
    int taskCount = 0;
    private int finishedTaskCount = 0;
    private List<AsyncTask> downloadingTasks;
    private List<DownloadInfo> downloadInfos;
    public static final int TASK_FINISHED_SUCCESSFULLY = 1;
    public static final int TASK_FINISHED_UNSUCCESSFULLY = 2;
    public static final int TASK_STILL_RUNNING = 3;
    public static final int IMAGE_TASK_FINISHED_SUCCESSFULLY = 4;
    public static final int IMAGE_TASK_FINISHED_UNSUCCESSFULLY = 5;
    public static final int IMAGE_TASK_STILL_RUNNING = 6;
    public static final int DOWNLOAD_TYPE_IMAGE = 1;
    public static final int DOWNLOAD_TYPE_NO_IMAGE = 2;
    public static final String ONEDownloadingFinished = "com.ONE_DOWNLOAD_FINISHED";
    public static final String ALLDownloadingFinished = "com.ALL_DOWNLOAD_FINISHED";
    public static final String NEWDOWNLOADTASKADDED = "com.NEW_DOWNLOAD_TASK_ADDED";
    public List<DownloadInfo> getDownloadInfos() {
        return downloadInfos;
    }
    private String[] column =
            {Constants.DOWNLOAD_TABLE_ITEM_NAME
                    , Constants.DOWNLOAD_TABLE_ITEM_FINISHED
                    , Constants.DOWNLOAD_TABLE_ITEM_PATH
                    , Constants.DOWNLOAD_TABLE_KEY_ID
                    , Constants.DOWNLOAD_TABLE_ITEM_URL
                    , Constants.DOWNLOAD_TABLE_ITEM_STATE_PAUSE
                    , Constants.DOWNLOAD_TABLE_ITEM_FINISHED_SIZE
                    , Constants.DOWNLOAD_TABLE_ITEM_CONTENT_SIZE
                    , Constants.DOWNLOAD_TABLE_ITEM_MIMETYPE
                    , Constants.DOWNLOAD_TABLE_ITEM_CONTENT_DISPOSITION
                    , Constants.DOWNLOAD_TABLE_ITEM_USER_AGENT
            };
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Log.d("MEssage", "MessageReceived");
            int i = msg.what;
            int currentSize = msg.arg1;
            int runState = msg.arg2;
            switch (runState) {
                case TASK_FINISHED_SUCCESSFULLY:
                    Intent intent_s = new Intent(ONEDownloadingFinished);
                    mContext.sendBroadcast(intent_s);
                    finishedTaskCount++;
                    RingtoneManager.getRingtone(mContext,RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)).play();
                    downloadInfos.get(taskCount - i - 1).finishedPercentage = 1;
                    downloadInfos.get(taskCount - i - 1).finished = 1;
                    downloadInfos.get(taskCount - i - 1).finishedSize = downloadInfos.get(taskCount - i - 1).contentSize;
                    break;
                case TASK_FINISHED_UNSUCCESSFULLY:
                    finishedTaskCount++;
                    RingtoneManager.getRingtone(mContext,RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)).play();

                    Intent intent_u = new Intent(ONEDownloadingFinished);
                    mContext.sendBroadcast(intent_u);
                    downloadInfos.get(taskCount - i - 1).pause = 1;
                    downloadInfos.get(taskCount - i - 1).finishedPercentage = ((double) currentSize) / (double) downloadInfos.get(taskCount - i - 1).contentSize;
                    downloadInfos.get(taskCount - i - 1).finishedSize = currentSize;
                    break;
                case TASK_STILL_RUNNING:
//                    Log.d("finished", downloadInfos.get(taskCount - i - 1).finishedPercentage + "");
                    downloadInfos.get(taskCount - i - 1).finishedPercentage = ((double) currentSize) / (double) downloadInfos.get(taskCount - i - 1).contentSize;
                    downloadInfos.get(taskCount - i - 1).finishedSize = currentSize;
                    break;
                case IMAGE_TASK_FINISHED_SUCCESSFULLY:
                    finishedTaskCount++;
                    RingtoneManager.getRingtone(mContext,RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)).play();

                    Intent intent_is = new Intent(ONEDownloadingFinished);
                    mContext.sendBroadcast(intent_is);
                    downloadInfos.get(taskCount - i - 1).finishedPercentage = 1;
                    downloadInfos.get(taskCount - i - 1).finished = 1;
                    downloadInfos.get(taskCount - i - 1).finishedSize = currentSize;
                    downloadInfos.get(taskCount - i - 1).contentSize= currentSize;

                case IMAGE_TASK_STILL_RUNNING:
//                    Log.d("finished", downloadInfos.get(taskCount - i - 1).finishedPercentage + "");
                    downloadInfos.get(taskCount - i - 1).finishedPercentage = 1;
                    downloadInfos.get(taskCount - i - 1).finishedSize = currentSize;
                    downloadInfos.get(taskCount - i - 1).contentSize= currentSize;
                    break;
                case IMAGE_TASK_FINISHED_UNSUCCESSFULLY:
                    finishedTaskCount++;
                    RingtoneManager.getRingtone(mContext,RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)).play();
                    Intent intent_iu = new Intent(ONEDownloadingFinished);
                    mContext.sendBroadcast(intent_iu);
                    downloadInfos.get(taskCount - i - 1).pause = 1;
                    downloadInfos.get(taskCount - i - 1).finishedPercentage = 1;
                    downloadInfos.get(taskCount - i - 1).finishedSize = currentSize;
                    break;
            }
            if (finishedTaskCount == taskCount) {
                Intent intent_all = new Intent(ALLDownloadingFinished);
                mContext.sendBroadcast(intent_all);
                downloadingTasks.clear();
                taskCount = 0;
                finishedTaskCount = 0;
            }
            Intent intent = new Intent();
            intent.setAction("MyBrowser.DownloadList.DATASETCHANGED");
            mContext.sendBroadcast(intent);
            super.handleMessage(msg);
        }
    };

    public MyDownloadManager(Context context) {
        mContext = context;
        downloadingTasks = new ArrayList<>();
        downloadInfos = new ArrayList<>();
        dataBaseOpenHelper = new MyDataBaseOpenHelper(context, Constants.DB_NAME, null, 1);
        db = dataBaseOpenHelper.getWritableDatabase();
        initDownloadInfos();
    }

    public boolean checkDownloadingTaskEmpty() {
        if (downloadingTasks.isEmpty())
            return true;
        else return false;
    }

    public void initDownloadInfos() {
        Cursor c = dataBaseOpenHelper.getReadableDatabase().query(Constants.DOWNLOAD_TABLE_NAME, column, null, null, null, null, Constants.DOWNLOAD_TABLE_KEY_ID);
        while (c.moveToNext()) {
            String name = c.getString(c.getColumnIndex(Constants.DOWNLOAD_TABLE_ITEM_NAME));
            String path = c.getString(c.getColumnIndex(Constants.DOWNLOAD_TABLE_ITEM_PATH));
            Log.d("Path", path);
            String url = c.getString(c.getColumnIndex(Constants.DOWNLOAD_TABLE_ITEM_URL));
            long finishedSize = c.getLong(c.getColumnIndex(Constants.DOWNLOAD_TABLE_ITEM_FINISHED_SIZE));
            long contentSize = c.getLong(c.getColumnIndex(Constants.DOWNLOAD_TABLE_ITEM_CONTENT_SIZE));
            int finished = c.getInt(c.getColumnIndex(Constants.DOWNLOAD_TABLE_ITEM_FINISHED));
            int pause = c.getInt(c.getColumnIndex(Constants.DOWNLOAD_TABLE_ITEM_STATE_PAUSE));
            String mUserAgent = c.getString(c.getColumnIndex(Constants.DOWNLOAD_TABLE_ITEM_USER_AGENT));
            String mContentDisposition = c.getString(c.getColumnIndex(Constants.DOWNLOAD_TABLE_ITEM_CONTENT_DISPOSITION));
            String mMimetype = c.getString(c.getColumnIndex(Constants.DOWNLOAD_TABLE_ITEM_MIMETYPE));
            DownloadInfo info = new DownloadInfo(name, path, url, finished, contentSize, finishedSize, pause, mUserAgent, mContentDisposition, mMimetype);
            downloadInfos.add(0, info);
        }
        c.close();
    }

    public void addNewTask(Context context, String url, String userAgent,
                           String contentDisposition, String mimetype, long contentLength, int type) {
        taskCount++;
        DownloadTask task = new DownloadTask(mContext, url, userAgent, contentDisposition, mimetype, contentLength, type);
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        downloadingTasks.add(task);
        Intent newIntent = new Intent(NEWDOWNLOADTASKADDED);
        mContext.sendBroadcast(newIntent);
    }

    public void addNewTaskAtPosition(Context context, String url, String userAgent,
                                     String contentDisposition, String mimetype, long contentLength, int position) {
        taskCount++;
        DownloadTask task = new DownloadTask(mContext, url, userAgent, contentDisposition, mimetype, contentLength, DOWNLOAD_TYPE_NO_IMAGE);
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        downloadingTasks.add(task);
    }


    public void handleItemClickAction(int position) {
        DownloadInfo clickedInfo = downloadInfos.get(position);
        if (clickedInfo.finished == 1) {
            openDownloadedFile(clickedInfo.path);
        } else {
            if (clickedInfo.pause == 1) {
                continuteDownload(clickedInfo, position);
            } else stopCurrentDownload(position);
        }
    }

    public void stopAllDownLoadingTasks() {
        int downloading = downloadingTasks.size();
        for (int i = 0; i < downloading; i++) {
            try {
                stopCurrentDownload(i);
            } catch (Exception e) {

            }
        }
    }

    private void stopCurrentDownload(int position) {
        try {
            ((DownloadTask) downloadingTasks.get(position)).exitTask();
        } catch (Exception e) {

        }
    }

    private void openDownloadedFile(String path) {
        File targetFile = new File(path);
        if (targetFile == null)
            return;

        Intent intent = new Intent();

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //设置intent的Action属性
        intent.setAction(Intent.ACTION_VIEW);
        //获取文件file的MIME类型
        String type = getMIMEType(targetFile);
        //设置intent的data和Type属性。
        intent.setDataAndType(/*uri*/Uri.fromFile(targetFile), type);
        //跳转
        mContext.startActivity(intent);
    }

    private void continuteDownload(DownloadInfo info, int position) {
        String url = info.url;
        String useragent = info.useragent;
        String contentDisposition = info.contentDisposition;
        String mimetype = info.mimetype;
        long contentSize = info.contentSize;
        try {
            new File(info.path).delete();
        } catch (Exception e) {

        }
        downloadInfos.remove(position);
        try {
            String[] names = {String.valueOf(info.name)};
            db.delete(Constants.DOWNLOAD_TABLE_NAME, Constants.DOWNLOAD_TABLE_ITEM_NAME + "=?", names);
        } catch (Exception e) {

        }
        addNewTask(mContext, url, useragent,
                info.contentDisposition, mimetype, contentSize, 0);
    }

    //删除下载选择的
    // 列表内容
    public void removeDownloadedFile(int position) {
        DownloadInfo info = downloadInfos.get(position);
        try {

            Log.d("DATABASE", "_________________");
            String[] args = {String.valueOf(info.name)};
            db.delete(Constants.DOWNLOAD_TABLE_NAME, Constants.DOWNLOAD_TABLE_ITEM_NAME + "=?", args);
        } catch (Exception e) {

        }
        try {
            new File(info.path).delete();
        } catch (Exception e) {

        }

//        info=null;

    }

    public void deleteNullInfo() {
        for (DownloadInfo info : downloadInfos) {
            if (info == null) {
                downloadInfos.remove(info);
            }
        }
    }

    private String getMIMEType(File file) {

        String type = "*/*";
        String fName = file.getName();
        //获取后缀名前的分隔符"."在fName中的位置。
        int dotIndex = fName.lastIndexOf(".");
        if (dotIndex < 0) {
            return type;
        }
    /* 获取文件的后缀名 */
        String end = fName.substring(dotIndex, fName.length()).toLowerCase();
        if (end == "") return type;
        //在MIME和文件类型的匹配表中找到对应的MIME类型。
        for (int i = 0; i < MIME_MapTable.length; i++) { //MIME_MapTable??在这里你一定有疑问，这个MIME_MapTable是什么？
            if (end.equals(MIME_MapTable[i][0]))
                type = MIME_MapTable[i][1];
        }
        return type;
    }


    //    }
    private class DownloadTask extends AsyncTask<String, Integer, String> {

        private int taskTag;
        private String mUrl, mUserAgent, mContentDisposition, mMimetype;
        private long mContentLength;
        private Context mContext;
        private long currentDatabaseId;
        private HttpURLConnection connection;
        private int currentFinished = 0;
        private int mType;


        // 使用时需传入context以打开数据库
        public DownloadTask(Context context, String url, String userAgent,
                            String contentDisposition, String mimetype, long contentLength, int type) {
            this.mContext = context;
            this.mUrl = url;
            this.mContentDisposition = contentDisposition;
            this.mUserAgent = userAgent;
            this.mContentLength = contentLength;
            this.taskTag = downloadingTasks.size();
            this.mMimetype = mimetype;
            this.mType = type;

        }

        @Override
        protected String doInBackground(String... params) {
            String result = "";
            Log.d("DOWNLOADTYPE",mType+"");
            if (mType == DOWNLOAD_TYPE_NO_IMAGE) {
                try {
                    String sdcard = Environment.getExternalStorageDirectory().toString();

                    File file = new File(sdcard + "/MyBrowser/DownLoad/");
                    if (!file.exists()) {
                        file.mkdirs();
                    }
                    InputStream inputStream = null;
                    URL url = new URL(mUrl);
//                Log.i("PIC URL", mUrl);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(2000);
                    if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        Log.d("openConnection", connection.getClass().toString());
                        inputStream = connection.getInputStream();
                        Log.i(" Failure INfo", "HTTP_OK");
                    }
                    String fileName = DownloadTools.getFileName(connection);
                    String fileType = fileName.substring(fileName.lastIndexOf(".") + 1);
                    String filePath = sdcard + "/MyBrowser/DownLoad/" + fileName;
                    file = new File(sdcard + "/MyBrowser/DownLoad/" + fileName);
                    ContentValues values = new ContentValues();

                    //存储初始值，下载名称，存储路径，大小，类型，完成度（初始为0），是否完成（1/0）
                    Log.i(" Failure INfo", "FILE_OK" + fileName + " " + filePath + " ");

                    values.put(Constants.DOWNLOAD_TABLE_ITEM_NAME, fileName);
                    values.put(Constants.DOWNLOAD_TABLE_ITEM_FINISHED, 0);
                    values.put(Constants.DOWNLOAD_TABLE_ITEM_PATH, filePath);
                    values.put(Constants.DOWNLOAD_TABLE_ITEM_URL, mUrl);
                    values.put(Constants.DOWNLOAD_TABLE_ITEM_FINISHED_SIZE, 0);
                    values.put(Constants.DOWNLOAD_TABLE_ITEM_STATE_PAUSE, 0);
                    values.put(Constants.DOWNLOAD_TABLE_ITEM_CONTENT_SIZE, mContentLength);

                    values.put(Constants.DOWNLOAD_TABLE_ITEM_MIMETYPE, mMimetype);
                    values.put(Constants.DOWNLOAD_TABLE_ITEM_USER_AGENT, mUserAgent);
                    values.put(Constants.DOWNLOAD_TABLE_ITEM_CONTENT_DISPOSITION, mContentDisposition);

                    downloadInfos.add(0, new DownloadInfo(fileName, filePath, mUrl, 0, mContentLength,
                            0, 0, mUserAgent, mContentDisposition, mMimetype));
                    currentDatabaseId = db.insert(Constants.DOWNLOAD_TABLE_NAME, null, values);
                    // initDownloadInfos();
                    Log.i(" Failure INfo", "CONTENT VALUES OK");
                    byte[] buffer = new byte[4096];
                    int len = 0;
                    currentFinished = 0;
                    double percentage = 0;
                    FileOutputStream outputStream = new FileOutputStream(file);
                    while ((len = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, len);
                        currentFinished += len;
                        double temp = (double) currentFinished / mContentLength;
                        if (temp - percentage >= 0.01) {
                            percentage = temp;
                            Message msg = new Message();
                            msg.what = taskTag;
                            msg.arg1 = currentFinished;
                            msg.arg2 = TASK_STILL_RUNNING;
                            handler.sendMessage(msg);
                        }

                    }
                    if (currentFinished == mContentLength) {
                        Log.i(" Failure INfo", "OUTPUT_OK");
                        //更新完成度和完成状态
                        ContentValues finshUpdateValues = new ContentValues();
                        finshUpdateValues.put(Constants.DOWNLOAD_TABLE_ITEM_FINISHED, 1);
                        finshUpdateValues.put(Constants.DOWNLOAD_TABLE_ITEM_FINISHED_SIZE, mContentLength);
                        db.update(Constants.DOWNLOAD_TABLE_NAME, finshUpdateValues, Constants.DOWNLOAD_TABLE_KEY_ID + "=" + currentDatabaseId, null);
                        //db.close();
                        Message msg = new Message();
                        msg.what = taskTag;
                        msg.arg1 = currentFinished;
                        msg.arg2 = TASK_FINISHED_SUCCESSFULLY;
                        handler.sendMessage(msg);

                        outputStream.close();

                        result = "保存成功";
                    } else {
                        ContentValues finshUpdateValues = new ContentValues();
                        finshUpdateValues.put(Constants.DOWNLOAD_TABLE_ITEM_FINISHED_SIZE, currentFinished);
                        db.update(Constants.DOWNLOAD_TABLE_NAME, finshUpdateValues, Constants.DOWNLOAD_TABLE_KEY_ID + "=" + currentDatabaseId, null);
                        Message msg = new Message();
                        msg.what = taskTag;
                        msg.arg1 = currentFinished;
                        msg.arg2 = TASK_FINISHED_UNSUCCESSFULLY;
                        handler.sendMessage(msg);
                        outputStream.close();
                        result = "保存失败";
                    }
                } catch (Exception e) {
                    handleException();
                    return result;
                }

                Log.i("result", result);
                return result;
            } else if (mType == DOWNLOAD_TYPE_IMAGE) {
                try {
                    String sdcard = Environment.getExternalStorageDirectory().toString();

                    File file = new File(sdcard + "/MyBrowser/DownLoad/");
                    if (!file.exists()) {
                        file.mkdirs();
                    }
                    InputStream inputStream = null;
                    URL url = new URL(mUrl);
//                Log.i("PIC URL", mUrl);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(2000);
                    if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        Log.d("openConnection", connection.getClass().toString());
                        inputStream = connection.getInputStream();
                        Log.i(" Failure INfo", "HTTP_OK");
                    }
                    String fileName =  new Date().getTime()+".jpg";
                    String filePath = sdcard + "/MyBrowser/DownLoad/" + fileName;
                    file = new File(sdcard + "/MyBrowser/DownLoad/" + fileName);
                    ContentValues values = new ContentValues();

                    //存储初始值，下载名称，存储路径，大小，类型，完成度（初始为0），是否完成（1/0）
                    Log.i(" Failure INfo", "FILE_OK" + fileName + " " + filePath + " ");

                    values.put(Constants.DOWNLOAD_TABLE_ITEM_NAME, fileName);
                    values.put(Constants.DOWNLOAD_TABLE_ITEM_FINISHED, 0);
                    values.put(Constants.DOWNLOAD_TABLE_ITEM_PATH, filePath);
                    values.put(Constants.DOWNLOAD_TABLE_ITEM_URL, mUrl);
                    values.put(Constants.DOWNLOAD_TABLE_ITEM_FINISHED_SIZE, 0);
                    values.put(Constants.DOWNLOAD_TABLE_ITEM_STATE_PAUSE, 0);
                    values.put(Constants.DOWNLOAD_TABLE_ITEM_CONTENT_SIZE, mContentLength);

                    values.put(Constants.DOWNLOAD_TABLE_ITEM_MIMETYPE, ".");
                    values.put(Constants.DOWNLOAD_TABLE_ITEM_USER_AGENT, ".");
                    values.put(Constants.DOWNLOAD_TABLE_ITEM_CONTENT_DISPOSITION, ".");

                    downloadInfos.add(0, new DownloadInfo(fileName, filePath, mUrl, 0, mContentLength,
                            0, 0, mUserAgent, mContentDisposition, mMimetype));
                    currentDatabaseId = db.insert(Constants.DOWNLOAD_TABLE_NAME, null, values);
                    // initDownloadInfos();
                    Log.i(" Failure INfo", "CONTENT VALUES OK");
                    byte[] buffer = new byte[4096];
                    int len = 0;
                    currentFinished = 0;
                    double percentage = 0;
                    FileOutputStream outputStream = new FileOutputStream(file);
                    while ((len = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, len);
                        int temp = currentFinished;
                        currentFinished += len;
                        mContentLength = currentFinished;
                        if (currentFinished - temp >= 4096) {
                            Message msg = new Message();
                            msg.what = taskTag;
                            msg.arg1 = currentFinished;
                            msg.arg2 = IMAGE_TASK_STILL_RUNNING;
                            handler.sendMessage(msg);
                        }

                    }
                    if (currentFinished == mContentLength) {
                        Log.i(" Failure INfo", "OUTPUT_OK");
                        //更新完成度和完成状态
                        ContentValues finshUpdateValues = new ContentValues();
                        finshUpdateValues.put(Constants.DOWNLOAD_TABLE_ITEM_CONTENT_SIZE,currentFinished);
                        finshUpdateValues.put(Constants.DOWNLOAD_TABLE_ITEM_FINISHED, 1);
                        finshUpdateValues.put(Constants.DOWNLOAD_TABLE_ITEM_FINISHED_SIZE, currentFinished);
                        db.update(Constants.DOWNLOAD_TABLE_NAME, finshUpdateValues, Constants.DOWNLOAD_TABLE_KEY_ID + "=" + currentDatabaseId, null);
                        //db.close();
                        Message msg = new Message();
                        msg.what = taskTag;
                        msg.arg1 = currentFinished;
                        msg.arg2 = IMAGE_TASK_FINISHED_SUCCESSFULLY;
                        handler.sendMessage(msg);

                        outputStream.close();

                        result = "保存成功";
                    } else {
                        ContentValues finshUpdateValues = new ContentValues();
                        finshUpdateValues.put(Constants.DOWNLOAD_TABLE_ITEM_CONTENT_SIZE,currentFinished);
                        finshUpdateValues.put(Constants.DOWNLOAD_TABLE_ITEM_FINISHED_SIZE, currentFinished);
                        db.update(Constants.DOWNLOAD_TABLE_NAME, finshUpdateValues, Constants.DOWNLOAD_TABLE_KEY_ID + "=" + currentDatabaseId, null);
                        Message msg = new Message();
                        msg.what = taskTag;
                        msg.arg1 = currentFinished;
                        msg.arg2 = IMAGE_TASK_FINISHED_UNSUCCESSFULLY;
                        handler.sendMessage(msg);
                        outputStream.close();
                        result = "保存失败";
                    }
                } catch (Exception e) {
                    handleException();
                    return result;
                }

                Log.i("result", result);
                return result;
            }
            return result;
        }

        public void handleException() {
            String result;
            ContentValues exceptionUpdateValues = new ContentValues();
            exceptionUpdateValues.put(Constants.DOWNLOAD_TABLE_ITEM_STATE_PAUSE, 1);
            exceptionUpdateValues.put(Constants.DOWNLOAD_TABLE_ITEM_FINISHED_SIZE, currentFinished);
            db.update(Constants.DOWNLOAD_TABLE_NAME, exceptionUpdateValues, Constants.DOWNLOAD_TABLE_KEY_ID + "=" + currentDatabaseId, null);
            Message msg = new Message();
            msg.what = taskTag;
            msg.arg1 = currentFinished;
            msg.arg2 = TASK_FINISHED_UNSUCCESSFULLY;
            handler.sendMessage(msg);
            result = "保存失败";

        }

        public void exitTask() {
            connection.disconnect();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }
    }

    private final String[][] MIME_MapTable = {
            //{后缀名， MIME类型}
            {".3gp", "video/3gpp"},
            {".apk", "application/vnd.android.package-archive"},
            {".asf", "video/x-ms-asf"},
            {".avi", "video/x-msvideo"},
            {".bin", "application/octet-stream"},
            {".bmp", "image/bmp"},
            {".c", "text/plain"},
            {".class", "application/octet-stream"},
            {".conf", "text/plain"},
            {".cpp", "text/plain"},
            {".doc", "application/msword"},
            {".docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"},
            {".xls", "application/vnd.ms-excel"},
            {".xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"},
            {".exe", "application/octet-stream"},
            {".gif", "image/gif"},
            {".gtar", "application/x-gtar"},
            {".gz", "application/x-gzip"},
            {".h", "text/plain"},
            {".htm", "text/html"},
            {".html", "text/html"},
            {".jar", "application/java-archive"},
            {".java", "text/plain"},
            {".jpeg", "image/jpeg"},
            {".jpg", "image/jpeg"},
            {".js", "application/x-javascript"},
            {".log", "text/plain"},
            {".m3u", "audio/x-mpegurl"},
            {".m4a", "audio/mp4a-latm"},
            {".m4b", "audio/mp4a-latm"},
            {".m4p", "audio/mp4a-latm"},
            {".m4u", "video/vnd.mpegurl"},
            {".m4v", "video/x-m4v"},
            {".mov", "video/quicktime"},
            {".mp2", "audio/x-mpeg"},
            {".mp3", "audio/x-mpeg"},
            {".mp4", "video/mp4"},
            {".mpc", "application/vnd.mpohun.certificate"},
            {".mpe", "video/mpeg"},
            {".mpeg", "video/mpeg"},
            {".mpg", "video/mpeg"},
            {".mpg4", "video/mp4"},
            {".mpga", "audio/mpeg"},
            {".msg", "application/vnd.ms-outlook"},
            {".ogg", "audio/ogg"},
            {".pdf", "application/pdf"},
            {".png", "image/png"},
            {".pps", "application/vnd.ms-powerpoint"},
            {".ppt", "application/vnd.ms-powerpoint"},
            {".pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation"},
            {".prop", "text/plain"},
            {".rc", "text/plain"},
            {".rmvb", "audio/x-pn-realaudio"},
            {".rtf", "application/rtf"},
            {".sh", "text/plain"},
            {".tar", "application/x-tar"},
            {".tgz", "application/x-compressed"},
            {".txt", "text/plain"},
            {".wav", "audio/x-wav"},
            {".wma", "audio/x-ms-wma"},
            {".wmv", "audio/x-ms-wmv"},
            {".wps", "application/vnd.ms-works"},
            {".xml", "text/plain"},
            {".z", "application/x-compress"},
            {".zip", "application/x-zip-compressed"},
            {"", "*/*"}
    };
}
