package io.pumpkinz.pumpkinreader;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;

import io.pumpkinz.pumpkinreader.etc.Constants;
import io.pumpkinz.pumpkinreader.util.NightModeUtil;


public class SettingsActivityFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    private SharedPreferences sharedPreferences;

    public SettingsActivityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getBaseContext());
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);

        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        configureDarkThemeButton(sharedPreferences);
    }

    public void configureDarkThemeButton(final SharedPreferences sharedPreferences) {
        boolean autoEnabled = sharedPreferences.getBoolean(Constants.CONFIG_AUTO_DARK_THEME, false);
        final Preference configDarkPreference = getPreferenceManager().findPreference(Constants.CONFIG_DARK_THEME);

        if (configDarkPreference != null) {
            configDarkPreference.setEnabled(!autoEnabled);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(Constants.CONFIG_AUTO_DARK_THEME)) {
            configureDarkThemeButton(sharedPreferences);
        }

        NightModeUtil.changeTheme(sharedPreferences);
        final AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            activity.getDelegate().applyDayNight();
        }
    }
}
