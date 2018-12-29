package io.pumpkinz.pumpkinreader;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import io.pumpkinz.pumpkinreader.etc.Constants;
import io.pumpkinz.pumpkinreader.util.NightModeUtil;

import static android.support.v7.app.AppCompatDelegate.*;
import static io.pumpkinz.pumpkinreader.util.Util.isDayTime;


public abstract class PumpkinReaderActivity extends AppCompatActivity {

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
