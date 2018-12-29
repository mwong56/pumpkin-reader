package io.pumpkinz.pumpkinreader.util;

import android.content.res.Resources;
import android.util.Log;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Pattern;

import io.pumpkinz.pumpkinreader.etc.Constants;


public class Util {

    public static String getDomainName(String url) {
        try {
            URL uri = new URL(url);
            String domain = uri.getHost();

            return domain.startsWith("www.") ? domain.substring(4) : domain;
        } catch (MalformedURLException ex) {
            Log.e("URL ERROR", ex.toString());
            return "";
        }
    }

    public static String join(List<Integer> list, String separator) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0, n = list.size(); i < n; ++i) {
            sb.append(list.get(i).toString());
            if (i < n) {
                sb.append(separator);
            }
        }

        return sb.toString();
    }

    public static List<Integer> split(String value, String separator) {
        List<Integer> retval = new ArrayList<>();
        if (value.isEmpty()) {
            return retval;
        }

        String[] parsedValues = value.split(Pattern.quote(separator));
        for (String parsedValue : parsedValues) {
            retval.add(Integer.valueOf(parsedValue));
        }

        return retval;
    }

    public static String joinNews(List<Integer> newsList) {
        return join(newsList, Constants.NEWS_SEPARATOR);
    }

    public static List<Integer> splitNews(String news) {
        return split(news, Constants.NEWS_SEPARATOR);
    }

    public static CharSequence trim(CharSequence source) {

        if (source == null)
            return "";

        int i = source.length();

        // loop back to the first non-whitespace character
        while (--i >= 0 && Character.isWhitespace(source.charAt(i))) {
        }

        return source.subSequence(0, i + 1);
    }

    public static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    public static int pxToDp(int px) {
        return (int) (px / Resources.getSystem().getDisplayMetrics().density);
    }

    public static boolean isDayTime(final Calendar current) {
        final Calendar start = Calendar.getInstance();
        start.set(Calendar.HOUR_OF_DAY, 7);
        start.set(Calendar.MINUTE, 0);
        final Calendar end = Calendar.getInstance();
        end.set(Calendar.HOUR_OF_DAY, 19);
        end.set(Calendar.MINUTE, 0);

        return (current.after(start) && current.before(end));
    }

}
