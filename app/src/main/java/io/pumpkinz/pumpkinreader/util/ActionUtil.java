package io.pumpkinz.pumpkinreader.util;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.customtabs.CustomTabsClient;
import android.support.customtabs.CustomTabsServiceConnection;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import io.pumpkinz.pumpkinreader.R;
import io.pumpkinz.pumpkinreader.WebViewActivity;
import io.pumpkinz.pumpkinreader.etc.Constants;
import io.pumpkinz.pumpkinreader.etc.PumpkinCustomTab;
import io.pumpkinz.pumpkinreader.model.News;


public class ActionUtil {
    static String OUTLINE_URL = "https://outline.com/";

    public static void open_in_outline(Context ctx, String url) {
        if (url != null) {
            Uri uri = Uri.parse(url);
            open_uri(ctx, Uri.parse(String.format("%s%s%s", OUTLINE_URL, uri.getHost(), uri.getPath())));
        }
    }

    public static void open(Context ctx, String url) {
        if (url != null) {
            open_uri(ctx, Uri.parse(url));
        }
    }

    private static void open_uri(Context ctx, Uri uri) {
        if (uri != null) {
            final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(ctx);
            boolean useChromeCustomTabs = pref.getBoolean(Constants.CONFIG_CUSTOM_TABS, true);

            if (useChromeCustomTabs && !getChromePackages(ctx).isEmpty()) {
                PumpkinCustomTab customTab = new PumpkinCustomTab((Activity) ctx);
                customTab.openPage(uri);
            } else {
                Intent intent = new Intent(ctx, WebViewActivity.class);
                ctx.startActivity(intent);
            }
        }
    }

    public static void save(final Context ctx, final Menu menu, final News news) {
        final boolean isNewsSaved = PreferencesUtil.isNewsSaved(ctx, news);

        if (isNewsSaved) {
            PreferencesUtil.removeNews(ctx, news);
        } else {
            PreferencesUtil.saveNews(ctx, news);
        }

        CoordinatorLayout layout = (CoordinatorLayout) ((Activity) ctx).findViewById(R.id.news_detail_layout);

        Snackbar sb = Snackbar.make(layout, isNewsSaved ? R.string.unsaved_news : R.string.saved_news, Snackbar.LENGTH_LONG)
                .setAction(R.string.undo, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (isNewsSaved) {
                            PreferencesUtil.saveNews(ctx, news);
                        } else {
                            PreferencesUtil.removeNews(ctx, news);
                        }

                        toggleSaveAction(ctx, menu, news);
                    }
                })
                .setActionTextColor(ctx.getResources().getColor(R.color.yellow_500));

        View sbView = sb.getView();
        sbView.setBackgroundColor(ctx.getResources().getColor(R.color.grey_800));

        sb.show();

        toggleSaveAction(ctx, menu, news);
    }

    public static void share(Context ctx, News news, boolean shareComments) {
        ctx.startActivity(Intent.createChooser(getPumpkinShareIntent(news, shareComments), ctx.getResources().getString(R.string.share)));
    }

    public static void toggleSaveAction(Context ctx, Menu menu, News news) {
        MenuItem item = menu.findItem(R.id.action_save);

        if (PreferencesUtil.isNewsSaved(ctx, news)) {
            item.setIcon(R.drawable.ic_bookmark_white_24dp);
            item.setTitle(R.string.unsave);
        } else {
            item.setIcon(R.drawable.ic_bookmark_border_white_24dp);
            item.setTitle(R.string.save);
        }
    }

    public static Intent getPumpkinShareIntent(News news, boolean shareComments) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, news.getTitle());

        String text;

        if (news.getUrl() != null && !news.getUrl().isEmpty() && !shareComments) {
            text = news.getUrl();
        } else {
            text = Constants.HN_BASE_URL + news.getId();
        }

        shareIntent.putExtra(Intent.EXTRA_TEXT, text);
        shareIntent.setType(Constants.MIME_TEXT_PLAIN);

        return shareIntent;
    }

    private static List<String> getChromePackages(Context ctx) {
        CustomTabsServiceConnection conn = new CustomTabsServiceConnection() {
            @Override
            public void onCustomTabsServiceConnected(ComponentName componentName, CustomTabsClient customTabsClient) {
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
            }
        };

        String[] packages = {"com.android.chrome", "com.chrome.beta", "com.chrome.dev"};
        List<String> availablePkgs = new ArrayList<>();

        for (String pkg : packages) {
            if (CustomTabsClient.bindCustomTabsService(ctx, pkg, conn)) {
                availablePkgs.add(pkg);
            }
        }

        return availablePkgs;
    }

}
