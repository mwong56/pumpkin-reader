package io.pumpkinz.pumpkinreader.util;

import android.app.Activity;
import android.content.Context;
import android.text.util.Linkify;
import android.view.ViewGroup;
import android.widget.TextView;

import me.saket.bettermovementmethod.BetterLinkMovementMethod;

public class LinkUtil {

    public static void linkify(final Activity activity) {
        BetterLinkMovementMethod instance = BetterLinkMovementMethod.linkify(Linkify.WEB_URLS, activity);

        instance.setOnLinkClickListener(new BetterLinkMovementMethod.OnLinkClickListener() {
            @Override
            public boolean onClick(TextView textView, String url) {
                ActionUtil.open(activity, url);

                return true;
            }
        });
    }

    public static void linkify(ViewGroup viewGroup) {
        BetterLinkMovementMethod instance = BetterLinkMovementMethod.linkify(Linkify.WEB_URLS, viewGroup);

        final Context context = viewGroup.getContext();

        instance.setOnLinkClickListener(new BetterLinkMovementMethod.OnLinkClickListener() {
            @Override
            public boolean onClick(TextView textView, String url) {
                ActionUtil.open(context, url);

                return true;
            }
        });
    }
}
