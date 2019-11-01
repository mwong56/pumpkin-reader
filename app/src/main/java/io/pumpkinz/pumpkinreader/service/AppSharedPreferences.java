package io.pumpkinz.pumpkinreader.service;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.List;

import io.pumpkinz.pumpkinreader.util.Util;
import rx.Observable;

public class AppSharedPreferences {
    private final Context context;

    public AppSharedPreferences(final Context context) {
        this.context = context;
    }

    public Observable<List<Integer>> getNewsIdsObservable(String newsFileKey, String newsValKey) {
        return Observable.just(getNewsIds(newsFileKey, newsValKey));
    }

    public List<Integer> getNewsIds(String newsFileKey, String newsValKey) {
        List<Integer> retval = new ArrayList<>();

        SharedPreferences topStoriesSp = context.getSharedPreferences(
                newsFileKey, Context.MODE_PRIVATE);
        String topStories = topStoriesSp.getString(newsValKey, "");

        if (!topStories.isEmpty()) {
            retval = Util.splitNews(topStories);
        }

        return retval;
    }
}
