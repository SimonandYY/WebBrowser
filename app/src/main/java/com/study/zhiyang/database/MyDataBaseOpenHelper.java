package com.study.zhiyang.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.study.zhiyang.Constants;

/**
 * Created by zhiyang on 2016/1/10.
 */
public class MyDataBaseOpenHelper extends SQLiteOpenHelper {
    private SQLiteDatabase db;
    private static final String CREATE_DOWNLOAD_TABLE = "create table if not exists "
            + Constants.DOWNLOAD_TABLE_NAME
            + " ("
            + Constants.DOWNLOAD_TABLE_KEY_ID + " integer primary key autoincrement, "
            + Constants.DOWNLOAD_TABLE_ITEM_PATH + " text not null,"
            + Constants.DOWNLOAD_TABLE_ITEM_NAME + " text not null, "
            + Constants.DOWNLOAD_TABLE_ITEM_URL + " text not null, "
            + Constants.DOWNLOAD_TABLE_ITEM_MIMETYPE + " text, "
            + Constants.DOWNLOAD_TABLE_ITEM_USER_AGENT + " text, "
            + Constants.DOWNLOAD_TABLE_ITEM_CONTENT_DISPOSITION + " text, "
            + Constants.DOWNLOAD_TABLE_ITEM_CONTENT_SIZE + " integer, "
            + Constants.DOWNLOAD_TABLE_ITEM_FINISHED_SIZE + " integer, "
            + Constants.DOWNLOAD_TABLE_ITEM_STATE_PAUSE + " integer, "
            + Constants.DOWNLOAD_TABLE_ITEM_FINISHED + " integer);";

    private static final String CREATE_HISTORY_TABLE = "create table if not exists "
            + Constants.HISTORY_TABLE_NAME
            + " ("
            + Constants.HISTORY_TABLE_KEY_ID + " integer primary key autoincrement, "
            + Constants.HISTORY_TABLE_URL + " text not null,"
            + Constants.HISTORY_TABLE_TITLE + " text not null, "
            + Constants.HISTORY_TABLE_TIME + " integer, "
            + Constants.HISTORY_TABLE_ICON + " BLOB);";
    private static final String CREATE_FAVORITES_TABLE = "create table if not exists "
            + Constants.FAVORITE_TABLE_NAME
            + " ("
            + Constants.FAVORITE_TABLE_KEY_ID + " integer primary key autoincrement, "
            + Constants.FAVORITE_TABLE_URL + " text not null,"
            + Constants.FAVORITE_TABLE_TITLE + " text not null, "
            + Constants.FAVORITE_TABLE_TIME + " integer, "
            + Constants.FAVORITE_TABLE_ICON + " BLOB);";
    private static final String CREATE_SEARCH_HISTORY_TABLE = "create table if not exists "
            +Constants.SEARCH_TABLE_NAME
            + " ("
            +Constants.SEARCH_TABLE_KEY_ID +" integer primary key autoincrement, "
            +Constants.SEARCH_TABLE_CONTENT + " text not null,"
            +Constants.SEARCH_TABLE_TIME + " integer);";

    public MyDataBaseOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        db = getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_DOWNLOAD_TABLE);
        db.execSQL(CREATE_HISTORY_TABLE);
        db.execSQL(CREATE_FAVORITES_TABLE);
        db.execSQL(CREATE_SEARCH_HISTORY_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public long addHistory(ContentValues values) {
        Log.i("HISTORY TABLE","ADDED");
        return db.insert(Constants.HISTORY_TABLE_NAME, null, values);
    }

    public void updateHisrory(long id, ContentValues values) {
        Log.i("HISTORY TABLE", "UPDATED");
        db.update(Constants.HISTORY_TABLE_NAME, values, Constants.HISTORY_TABLE_KEY_ID + "=" + id, null);
    }

    public void deleteHistoryAtCertainTime(long time) {
        Log.i("HISTORY TABLE", "ITEM DELETED");

        String[] wheres = {String.valueOf(time)};
        db.delete(Constants.HISTORY_TABLE_NAME, Constants.HISTORY_TABLE_TIME + "=?", wheres);
    }
    public void clearAllHistory(){
        //String deleteHistory=" DROP TABLE IF EXISTS "+Constants.HISTORY_TABLE_NAME;
        Log.i("HISTORY TABLE","TABLE CLEARED");

        db.delete(Constants.HISTORY_TABLE_NAME,null,null);
    }

    public long addFavorite(ContentValues values) {
        Log.i("FAVORITE TABLE", "ADDED");
        return db.insert(Constants.FAVORITE_TABLE_NAME, null, values);
    }

    public void updateFavorite(long id, ContentValues values) {
        Log.i("FAVORITE TABLE","UPDATED");
        db.update(Constants.FAVORITE_TABLE_NAME, values, Constants.FAVORITE_TABLE_KEY_ID + "=" + id, null);
    }

    public void deleteFavoriteAtCertainTime(long time) {
        Log.i("FAVORITE TABLE","ITEM DELETED");

        String[] wheres = {String.valueOf(time)};
        db.delete(Constants.FAVORITE_TABLE_NAME, Constants.FAVORITE_TABLE_TIME + "=?", wheres);
    }
    public void clearAllFavorites(){
        //String deleteHistory=" DROP TABLE IF EXISTS "+Constants.HISTORY_TABLE_NAME;
        Log.i("FAVORITE TABLE","TABLE CLEARED");

        db.delete(Constants.FAVORITE_TABLE_NAME,null,null);
    }
    public long addSearchHistory(ContentValues values){
        return db.insert(Constants.SEARCH_TABLE_NAME,null,values);
    }
    public void deleteSearchHistory(String value){
        String[] wheres = {value};
        db.delete(Constants.SEARCH_TABLE_NAME,Constants.SEARCH_TABLE_CONTENT + "=?",wheres);
    }
    public void clearSearchHistory(){
        db.delete(Constants.SEARCH_TABLE_NAME,null,null);
    }
}
