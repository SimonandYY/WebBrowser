package com.study.zhiyang.webbrowser;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.widget.Button;
import android.widget.CheckBox;

import com.study.zhiyang.Constants;
import com.study.zhiyang.database.MyDataBaseOpenHelper;


/**
 * Created by zhiyang on 2016/1/25.
 */
public class MyPreferenceFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    private ListPreference setSearchEngine;
    private MyDataBaseOpenHelper helper;
    //    private CheckBoxPreference loadPic;
//    private CheckBoxPreference locPermission;
//    private CheckBoxPreference setDefault;
//    private Preference deleteInfo;
//    private Preference checkUpdate;
    private String[] searchEngineTitle;
    private String[] getSearchEngineValue;
    //    private boolean sharedPreferenceChanged = false;
//    private int RESULT_CODE = 1;
    private SharedPreferences sharedPreferences;
    private Context mContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.sharefreference_list);
        setSearchEngine = (ListPreference) findPreference("searchEnginePreference");
//        loadPic = (CheckBoxPreference) findPreference("loadWidthPic");
//        locPermission = (CheckBoxPreference) findPreference("locationPermission");
//        setDefault = (CheckBoxPreference) findPreference("setAsDefault");
//        deleteInfo = findPreference("deleteViewedHistory");
//        checkUpdate = findPreference("checkUpdate");
        searchEngineTitle = getResources().getStringArray(R.array.searchEngineName);
        getSearchEngineValue = getResources().getStringArray(R.array.searchEngineAddress);
        sharedPreferences = getPreferenceScreen().getSharedPreferences();
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        setSearchEngine.setSummary(searchEngineTitle[Integer
                .valueOf(sharedPreferences.getString(getString(R.string.preference_screen_setSearchEngine), "0"))]);
    }

    @Override
    public void onStart() {
        Log.i("PREFERENCESCREEN", "onStart");
        helper = new MyDataBaseOpenHelper(getActivity(), Constants.DB_NAME, null, 1);
        super.onStart();
        Log.i("PREFERENCESCREEN", "Started");

    }

    @Override
    public void onDestroy() {
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
        super.onDestroy();
    }

    @Override
    public void onAttach(Context context) {
        mContext = context;
        super.onAttach(context);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {

        switch (preference.getKey()) {
            case "searchEnginePreference":
                break;
            case "loadWidthPic":
//                loadPic.setChecked(!loadPic.isChecked());
                break;
            case "deleteViewedHistory":
                final AlertDialog dialog = new AlertDialog.Builder(getActivity()).create();
                dialog.show();
                dialog.getWindow().setContentView(R.layout.delete_history_dialog);
                final CheckBox[] checkBoxes = new CheckBox[4];
                checkBoxes[0] = (CheckBox) dialog.getWindow().findViewById(R.id.delete_history_dialog_clear_cookie);
                checkBoxes[1] = (CheckBox) dialog.getWindow().findViewById(R.id.delete_history_dialog_clear_history);
                checkBoxes[2] = (CheckBox) dialog.getWindow().findViewById(R.id.delete_history_dialog_clear_search_history);
                checkBoxes[3] = (CheckBox) dialog.getWindow().findViewById(R.id.delete_history_dialog_clear_cache);
                Button ok = (Button) dialog.getWindow().findViewById(R.id.delete_history_dialog_ok);
                ok.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (checkBoxes[0].isChecked()) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                CookieManager manager = CookieManager.getInstance();
                                manager.removeAllCookies(null);
                            } else {
                                CookieManager manager = CookieManager.getInstance();
                                manager.removeAllCookie();
                            }
                        }
                        if (checkBoxes[1].isChecked()) {
                            helper.clearAllHistory();
                        }
                        if (checkBoxes[2].isChecked()) {
                        }
                        if (checkBoxes[3].isChecked()) {
                        }
                        dialog.dismiss();
                    }
                });
                Log.i("PREFERENCESCREEN", "DELETE CLICKED");

//                Toast.makeText(getActivity(),"DELETE CLICKED",Toast.LENGTH_SHORT).show();
                break;
            case "setAsDefault":
                Log.i("PREFERENCESCREEN", "SETDEFAULT CLICKED");
//                Toast.makeText(getActivity().getApplicationContext(),"SETDEFAULT CLICKED",Toast.LENGTH_SHORT).show();
                break;
            case "locationPermission":
//                locPermission.setChecked(!locPermission.isChecked());
                break;
            case "checkUpdate":
//                Toast.makeText(getActivity(),"checkUpdate CLICKED",Toast.LENGTH_LONG).show();
                break;
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        ((SettingActivity) getActivity()).spChanged = true;
        if (key.equals("searchEnginePreference")) {
            int i = Integer.valueOf(sharedPreferences.getString(key, "0"));
            Log.i("PREFERENCESCREEN", i + "");
            setSearchEngine.setSummary(searchEngineTitle[i]);
        }

//        Intent i = new Intent();
//        i.putExtra("changed",sharedPreferenceChanged);
    }

//    private class DialogOnClick extends View.OnClickListener{
//
//        @Override
//        public void onClick(View v) {
//            if (v.getId()==R.id.delete_history_dialog_ok)
//        }
//    }
}

