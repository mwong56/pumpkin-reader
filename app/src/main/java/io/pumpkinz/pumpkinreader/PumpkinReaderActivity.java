package io.pumpkinz.pumpkinreader;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import com.google.android.material.appbar.AppBarLayout;

import com.trello.rxlifecycle3.components.support.RxAppCompatActivity;

import io.pumpkinz.pumpkinreader.etc.Constants;
import io.pumpkinz.pumpkinreader.util.NightModeUtil;


public abstract class PumpkinReaderActivity extends RxAppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        NightModeUtil.changeTheme(pref);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    protected void setScrollFlag(AppBarLayout.LayoutParams layoutParams) {
        final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean shouldHideToolbar = pref.getBoolean(Constants.CONFIG_HIDE_TOOLBAR, false);

        if (shouldHideToolbar) {
            layoutParams.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL
                    | AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS);
        } else {
            layoutParams.setScrollFlags(0);
        }
    }

}
