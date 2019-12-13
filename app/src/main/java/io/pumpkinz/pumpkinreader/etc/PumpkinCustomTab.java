package io.pumpkinz.pumpkinreader.etc;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.content.ContextCompat;

import org.parceler.Parcels;

import io.pumpkinz.pumpkinreader.R;
import io.pumpkinz.pumpkinreader.model.News;


public class PumpkinCustomTab {

    private Activity activity;
    private CustomTabsIntent customTabsIntent;

    public PumpkinCustomTab(Activity activity) {
        this.activity = activity;
        this.customTabsIntent = buildCustomTabsIntent();
        this.customTabsIntent.intent.putExtra(CustomTabsIntent.EXTRA_DEFAULT_SHARE_MENU_ITEM, true);
    }

    public void openPage(Uri uri) {
        customTabsIntent.launchUrl(activity, uri);
    }

    private CustomTabsIntent buildCustomTabsIntent() {
        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
        builder.setShowTitle(true);

        int color = ContextCompat.getColor(activity, R.color.pumpkin_primary);

        int currentNightMode = activity.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
            color = ContextCompat.getColor(activity, R.color.primary_material_dark);
        }

        builder.setToolbarColor(color);
        return builder.build();
    }

}
