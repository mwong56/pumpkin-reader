package io.pumpkinz.pumpkinreader.etc;

import android.app.Activity;
import android.content.res.Configuration;
import android.net.Uri;

import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.content.ContextCompat;

import io.pumpkinz.pumpkinreader.R;
import io.pumpkinz.pumpkinreader.model.News;


public class PumpkinCustomTab {

    private Activity activity;
    private News news;
    private CustomTabsIntent customTabsIntent;

    public PumpkinCustomTab(Activity activity, News news) {
        this.activity = activity;
        this.news = news;
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
