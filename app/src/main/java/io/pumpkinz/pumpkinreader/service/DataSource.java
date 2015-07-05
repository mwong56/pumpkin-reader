package io.pumpkinz.pumpkinreader.service;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

import io.pumpkinz.pumpkinreader.etc.Constants;
import io.pumpkinz.pumpkinreader.model.News;
import io.pumpkinz.pumpkinreader.util.Util;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;


public class DataSource {

    private Context ctx;

    public DataSource(Context ctx) {
        this.ctx = ctx;
    }

    public Observable<List<News>> getHNNew(final int from, final int count, boolean isRefresh) {
        return getHNNewIds(isRefresh)
                .compose(new NewsTransformer(from, count));
    }

    public Observable<List<News>> getHNTop(final int from, final int count, boolean isRefresh) {
        return getHNTopIds(isRefresh)
                .compose(new NewsTransformer(from, count));
    }

    public Observable<List<News>> getHNAsk(final int from, final int count, boolean isRefresh) {
        return getHNAskIds(isRefresh)
                .compose(new NewsTransformer(from, count));
    }

    public Observable<List<News>> getHNShow(final int from, final int count, boolean isRefresh) {
        return getHNShowIds(isRefresh)
                .compose(new NewsTransformer(from, count));
    }

    public Observable<List<News>> getHNJob(final int from, final int count, boolean isRefresh) {
        return getHNJobIds(isRefresh)
                .compose(new NewsTransformer(from, count));
    }

    private Observable<List<Integer>> getHNNewIds(boolean isRefresh) {
        List<Integer> retval = getNewsIdsFromSp(Constants.NEW_FILE_SP, Constants.NEW_VAL_SP, ctx);

        if (retval.isEmpty() || isRefresh) {
            return RestClient.service().getHNNewIds()
                    .doOnNext(new putToSpAction(ctx, Constants.NEW_FILE_SP, Constants.NEW_VAL_SP));
        } else {
            return Observable.just(retval);
        }
    }

    private Observable<List<Integer>> getHNTopIds(boolean isRefresh) {
        List<Integer> retval = getNewsIdsFromSp(Constants.TOP_FILE_SP, Constants.TOP_VAL_SP, ctx);

        if (retval.isEmpty() || isRefresh) {
            return RestClient.service().getHNTopIds()
                    .doOnNext(new putToSpAction(ctx, Constants.TOP_FILE_SP, Constants.TOP_VAL_SP));
        } else {
            return Observable.just(retval);
        }
    }

    private Observable<List<Integer>> getHNAskIds(boolean isRefresh) {
        List<Integer> retval = getNewsIdsFromSp(Constants.ASK_FILE_SP, Constants.ASK_VAL_SP, ctx);

        if (retval.isEmpty() || isRefresh) {
            return RestClient.service().getHNAskIds()
                    .doOnNext(new putToSpAction(ctx, Constants.ASK_FILE_SP, Constants.ASK_VAL_SP));
        } else {
            return Observable.just(retval);
        }
    }

    private Observable<List<Integer>> getHNShowIds(boolean isRefresh) {
        List<Integer> retval = getNewsIdsFromSp(Constants.SHOW_FILE_SP, Constants.SHOW_VAL_SP, ctx);

        if (retval.isEmpty() || isRefresh) {
            return RestClient.service().getHNShowIds()
                    .doOnNext(new putToSpAction(ctx, Constants.SHOW_FILE_SP, Constants.SHOW_VAL_SP));
        } else {
            return Observable.just(retval);
        }
    }

    private Observable<List<Integer>> getHNJobIds(boolean isRefresh) {
        List<Integer> retval = getNewsIdsFromSp(Constants.JOB_FILE_SP, Constants.JOB_VAL_SP, ctx);

        if (retval.isEmpty() || isRefresh) {
            return RestClient.service().getHNJobIds()
                    .doOnNext(new putToSpAction(ctx, Constants.JOB_FILE_SP, Constants.JOB_VAL_SP));
        } else {
            return Observable.just(retval);
        }
    }

    private List<Integer> getNewsIdsFromSp(String newsFileKey, String newsValKey, Context context) {
        List<Integer> retval = new ArrayList<>();

        SharedPreferences topStoriesSp = context.getSharedPreferences(
                newsFileKey, Context.MODE_PRIVATE);
        String topStories = topStoriesSp.getString(newsValKey, "");

        if (!topStories.isEmpty()) {
            retval = Util.split(topStories, "|");
        }

        return retval;
    }

    private class putToSpAction implements Action1<List<Integer>> {

        private Context context;
        private String SP_FILE_KEY;
        private String SP_VAL_KEY;

        public putToSpAction(Context context, String SP_FILE_KEY, String SP_VAL_KEY) {
            this.context = context;
            this.SP_FILE_KEY = SP_FILE_KEY;
            this.SP_VAL_KEY = SP_VAL_KEY;
        }

        @Override
        public void call(List<Integer> integers) {
            String input = Util.join(integers, '|');
            SharedPreferences topStoriesSp = context.getSharedPreferences(
                    SP_FILE_KEY, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = topStoriesSp.edit();

            editor.putString(SP_VAL_KEY, input);
            editor.commit();
        }

    }

    private class NewsTransformer implements Observable.Transformer<List<Integer>, List<News>> {

        final List<Integer> subNewsIds = new ArrayList<>();
        final Dictionary<Integer, News> dict = new Hashtable<>();
        private int from;
        private int count;

        public NewsTransformer(int from, int count) {
            this.from = from;
            this.count = count;
        }

        @Override
        public Observable<List<News>> call(Observable<List<Integer>> newsIds) {
            return newsIds
                    .map(new Func1<List<Integer>, List<Integer>>() {
                        @Override // Get a subset from Top News IDs and save it for later lookup
                        public List<Integer> call(List<Integer> ids) {
                            subNewsIds.addAll(ids.subList(from, from + count));
                            return subNewsIds;
                        }
                    })
                    .flatMap(new Func1<List<Integer>, Observable<Integer>>() {
                        @Override // Emit Top News IDs one at a time
                        public Observable<Integer> call(List<Integer> integers) {
                            return Observable.from(integers);
                        }
                    })
                    .flatMap(new Func1<Integer, Observable<News>>() {
                        @Override // Get News body
                        public Observable<News> call(Integer integer) {
                            return RestClient.service().getNews(integer)
                                    .onErrorReturn(new Func1<Throwable, News>() {
                                        @Override //If API returns error, return null News
                                        public News call(Throwable throwable) {
                                            return null;
                                        }
                                    });
                        }
                    })
                    .filter(new Func1<News, Boolean>() {
                        @Override //Filter out the NULL News (from any parse error)
                        public Boolean call(News news) {
                            return (news != null);
                        }
                    })
                    .doOnNext(new Action1<News>() {
                        @Override // Put the News into dictionary for faster lookup
                        public void call(News news) {
                            dict.put(news.getId(), news);
                        }
                    })
                    .toList()
                    .map(new Func1<List<News>, List<News>>() {
                        @Override
                        // Discard the formed List<News> (what?!) and use the ones on dictionary instead
                        public List<News> call(List<News> newses) {
                            List<News> retval = new ArrayList<>();

                            for (Integer topStory : subNewsIds) {
                                retval.add(dict.get(topStory));
                            }

                            return retval;
                        }
                    });
        }
    }
    
}