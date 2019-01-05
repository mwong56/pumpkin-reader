package io.pumpkinz.pumpkinreader;

import android.Manifest;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import java.util.List;

import io.pumpkinz.pumpkinreader.etc.Constants;
import io.pumpkinz.pumpkinreader.util.NightModeUtil;
import pub.devrel.easypermissions.EasyPermissions;
import pub.devrel.easypermissions.PermissionRequest;


public class SettingsActivityFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener,
        EasyPermissions.PermissionCallbacks, EasyPermissions.RationaleCallbacks {
    private static final String TAG = "SettingsActivityFragmen";
    private static final int RC_LOCATION_PERM = 123;

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

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(Constants.CONFIG_AUTO_DARK_THEME)) {
            if (sharedPreferences.getBoolean(Constants.CONFIG_AUTO_DARK_THEME, true)) {
                askLocationPerm();
            }
            configureDarkThemeButton(sharedPreferences);
        }

        NightModeUtil.changeTheme(sharedPreferences);
        final AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            activity.getDelegate().applyDayNight();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        Log.d(TAG, "Permissions granted: " + requestCode + perms);
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        Log.d(TAG, "Permissions denied: " + requestCode + perms);
        Toast.makeText(getActivity(), "Will use default sunrise/sunset values.", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRationaleAccepted(int requestCode) {
        Log.d(TAG, "Rationale accepted: " + requestCode);
    }

    @Override
    public void onRationaleDenied(int requestCode) {
        Log.d(TAG, "Rationale denied: " + requestCode);
        Toast.makeText(getActivity(), "Will use default sunrise/sunset values.", Toast.LENGTH_LONG).show();
    }

    private void askLocationPerm() {
        if (!EasyPermissions.hasPermissions(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)) {
            EasyPermissions.requestPermissions(getActivity(), getString(R.string.rationale_location),
                    RC_LOCATION_PERM, Manifest.permission.ACCESS_COARSE_LOCATION);
        }

    }

    private void configureDarkThemeButton(final SharedPreferences sharedPreferences) {
        boolean autoEnabled = sharedPreferences.getBoolean(Constants.CONFIG_AUTO_DARK_THEME, false);
        final Preference configDarkPreference = getPreferenceManager().findPreference(Constants.CONFIG_DARK_THEME);

        if (configDarkPreference != null) {
            configDarkPreference.setEnabled(!autoEnabled);
        }
    }
}
