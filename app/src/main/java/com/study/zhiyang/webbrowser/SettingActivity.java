package com.study.zhiyang.webbrowser;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.view.KeyEvent;

public class SettingActivity extends Activity {
    public boolean spChanged = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.setting_activity);
        MyPreferenceFragment preferenceFragment = new MyPreferenceFragment();
        getFragmentManager().beginTransaction().add(R.id.settingFragmentContainer, preferenceFragment).commit();
//        CheckBoxPreference
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Intent i = new Intent();
            i.putExtra("changed", spChanged);
            setResult(0, i);
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }
}
