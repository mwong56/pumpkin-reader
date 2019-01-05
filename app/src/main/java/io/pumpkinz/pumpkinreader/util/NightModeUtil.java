package io.pumpkinz.pumpkinreader.util;

import android.content.SharedPreferences;

import io.pumpkinz.pumpkinreader.etc.Constants;

import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_AUTO;
import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO;
import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES;
import static androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode;

public class NightModeUtil {
    public static int changeTheme(SharedPreferences preferences) {
        boolean isDarkTheme = preferences.getBoolean(Constants.CONFIG_DARK_THEME, false);
        boolean isAutoTheme = preferences.getBoolean(Constants.CONFIG_AUTO_DARK_THEME, false);

        if (isAutoTheme) {
            setDefaultNightMode(MODE_NIGHT_AUTO);
            return MODE_NIGHT_AUTO;
        }
        else {
            final int toReturn = isDarkTheme ? MODE_NIGHT_YES : MODE_NIGHT_NO;
            setDefaultNightMode(toReturn);
            return toReturn;
        }
    }
}
