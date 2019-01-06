package io.pumpkinz.pumpkinreader.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.TimeUnit;

import hu.akarnokd.rxjava.interop.RxJavaInterop;
import io.pumpkinz.pumpkinreader.etc.Constants;
import io.pumpkinz.pumpkinreader.exception.EndOfListException;
import io.pumpkinz.pumpkinreader.model.Comment;
import io.pumpkinz.pumpkinreader.model.News;
import io.pumpkinz.pumpkinreader.service.database.AppDatabase;
import io.pumpkinz.pumpkinreader.service.database.entity.JsonComment;
import io.pumpkinz.pumpkinreader.service.database.entity.JsonNews;
import io.pumpkinz.pumpkinreader.util.Util;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;


public class HackerNewsRepository {
    private static final String TAG = "HackerNewsRepository";

    private Context ctx;
    private HackerNewsApi hackerNewsApi;
    private AppDatabase appDatabase;
    private AppSharedPreferences appSharedPreferences;

    public HackerNewsRepository(Context ctx, HackerNewsApi api, AppDatabase appDatabase, AppSharedPreferences appSharedPreferences) {
        this.ctx = ctx;
        this.hackerNewsApi = api;
        this.appDatabase = appDatabase;
        this.appSharedPreferences = appSharedPreferences;
    }

    public Observable<List<News>> getHNSaved(final int from, final int count) {
        return getHNSavedIds()
                .compose(new NewsTransformer(from, count));
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

    public Observable<News> getNews(int id) {
        return Observable.concat(
                RxJavaInterop.toV1Observable(appDatabase.newsDao().loadNews(id).toFlowable().map(jNews -> {
                    Log.e(TAG, "Loaded news from db");
                    return jNews.news;
                })),
                hackerNewsApi.getNews(id)
                        .doOnNext(news -> {
                            long result = appDatabase.newsDao().insertNews(new JsonNews(news.getId(), news));
                            Log.e(TAG, String.format("Inserted %d row into db", result));
                        })
        )
//                .debounce(Constants.DEBOUNCE_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)
                .timeout(Constants.CONN_TIMEOUT_SEC, TimeUnit.SECONDS);
    }

    public Observable<Comment> getComment(int id) {
        return Observable.concat(
                RxJavaInterop.toV1Observable(appDatabase.commentsDao().loadComment(id).toFlowable().map(jComments -> {
                    Log.e(TAG, "Loaded comments from db");
                    return jComments.comment;
                })),
                hackerNewsApi.getComment(id)
                        .doOnNext(comment -> {
                            long result = appDatabase.commentsDao().insertComment(new JsonComment(comment.getId(), comment));
                            Log.e(TAG, String.format("Inserted %d row into db", result));
                        })
        )
//                .debounce(Constants.DEBOUNCE_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)
                .timeout(Constants.CONN_TIMEOUT_SEC, TimeUnit.SECONDS);
    }

    public Observable<List<Comment>> getAllComments(final News news) {
        return Observable.from(news.getCommentIds())
                .flatMap(commentId -> getComment(commentId).onErrorReturn(throwable -> null))
                .flatMap(comment -> getInnerComments(comment))
                .timeout(Constants.CONN_TIMEOUT_SEC, TimeUnit.SECONDS)
                .filter(comment -> (comment != null) && !comment.isDeleted() && !comment.isDead())
                .toList()
                .map(comments -> {
                    Dictionary<Integer, Comment> commentDict = new Hashtable<>();
                    List<Comment> retval = new ArrayList<>();

                    for (Comment comment : comments) {
                        commentDict.put(comment.getId(), comment);
                    }

                    for (Integer commentId : news.getCommentIds()) {
                        Comment comment = commentDict.get(commentId);
                        if (comment != null) {
                            retval.add(getCommentWithChild(0, comment, commentDict));
                        }
                    }

                    return flattenComments(retval);
                })
                .map(comments -> {
                    for (Comment comment : comments) {
                        comment.setAllChildCount(getAllChildCount(comment));
                    }

                    return comments;
                });
    }

    private Observable<List<Integer>> getHNSavedIds() {
        List<Integer> retval = appSharedPreferences.getNewsIds(Constants.SAVED_FILE_SP, Constants.SAVED_VAL_SP);
        return Observable.just(retval);
    }

    private Observable<List<Integer>> getHNNewIds(boolean isRefresh) {
        List<Integer> retval = appSharedPreferences.getNewsIds(Constants.NEW_FILE_SP, Constants.NEW_VAL_SP);

        if (retval.isEmpty() || isRefresh) {
            return hackerNewsApi.getHNNewIds()
                    .doOnNext(new putToSpAction(ctx, Constants.NEW_FILE_SP, Constants.NEW_VAL_SP));
        } else {
            return Observable.just(retval);
        }
    }

    private Observable<List<Integer>> getHNTopIds(boolean isRefresh) {
        List<Integer> retval = appSharedPreferences.getNewsIds(Constants.TOP_FILE_SP, Constants.TOP_VAL_SP);

        if (retval.isEmpty() || isRefresh) {
            return hackerNewsApi.getHNTopIds()
                    .doOnNext(new putToSpAction(ctx, Constants.TOP_FILE_SP, Constants.TOP_VAL_SP));
        } else {
            return Observable.just(retval);
        }
    }

    private Observable<List<Integer>> getHNAskIds(boolean isRefresh) {
        List<Integer> retval = appSharedPreferences.getNewsIds(Constants.ASK_FILE_SP, Constants.ASK_VAL_SP);

        if (retval.isEmpty() || isRefresh) {
            return hackerNewsApi.getHNAskIds()
                    .doOnNext(new putToSpAction(ctx, Constants.ASK_FILE_SP, Constants.ASK_VAL_SP));
        } else {
            return Observable.just(retval);
        }
    }

    private Observable<List<Integer>> getHNShowIds(boolean isRefresh) {
        List<Integer> retval = appSharedPreferences.getNewsIds(Constants.SHOW_FILE_SP, Constants.SHOW_VAL_SP);

        if (retval.isEmpty() || isRefresh) {
            return hackerNewsApi.getHNShowIds()
                    .doOnNext(new putToSpAction(ctx, Constants.SHOW_FILE_SP, Constants.SHOW_VAL_SP));
        } else {
            return Observable.just(retval);
        }
    }

    private Observable<List<Integer>> getHNJobIds(boolean isRefresh) {
        List<Integer> retval = appSharedPreferences.getNewsIds(Constants.JOB_FILE_SP, Constants.JOB_VAL_SP);

        if (retval.isEmpty() || isRefresh) {
            return hackerNewsApi.getHNJobIds()
                    .doOnNext(new putToSpAction(ctx, Constants.JOB_FILE_SP, Constants.JOB_VAL_SP));
        } else {
            return Observable.just(retval);
        }
    }

    private Observable<Comment> getInnerComments(Comment comment) {
        if (comment != null && comment.getCommentIds().size() > 0) {
            return Observable.merge(
                    Observable.just(comment),
                    Observable.from(comment.getCommentIds())
                            .flatMap(commentId -> hackerNewsApi.getComment(commentId)
                                    .onErrorReturn(throwable -> null))
                            .flatMap(comment1 -> getInnerComments(comment1))
            );
        }

        return Observable.just(comment);
    }

    private Comment getCommentWithChild(int level, Comment comment, Dictionary<Integer, Comment> commentDict) {
        if (comment.getCommentIds().size() == 0) {
            comment.setLevel(level);
            return comment;
        }

        for (Integer commentId : comment.getCommentIds()) {
            Comment childComment = commentDict.get(commentId);
            if (childComment != null) {
                childComment.setParentComment(comment);
                comment.addChildComment(getCommentWithChild(level + 1, childComment, commentDict));
            }
        }

        comment.setLevel(level);
        return comment;
    }

    private List<Comment> flattenComments(List<Comment> comments) {
        List<Comment> retval = new ArrayList<>();

        for (Comment comment : comments) {
            retval.add(comment);
            if (comment.getCommentIds().size() > 0) {
                retval.addAll(flattenComments(comment.getChildComments()));
            }
        }

        return retval;
    }

    private int getAllChildCount(Comment comment) {
        int size = comment.getChildComments().size();

        if (size > 0) {
            for (Comment childComment : comment.getChildComments()) {
                size += getAllChildCount(childComment);
            }
        }

        return size;
    }

    private class putToSpAction implements Action1<List<Integer>> {
        private Context context;
        private String SP_FILE_KEY;
        private String SP_VAL_KEY;

        putToSpAction(Context context, String SP_FILE_KEY, String SP_VAL_KEY) {
            this.context = context;
            this.SP_FILE_KEY = SP_FILE_KEY;
            this.SP_VAL_KEY = SP_VAL_KEY;
        }

        @Override
        public void call(List<Integer> integers) {
            String input = Util.joinNews(integers);
            SharedPreferences topStoriesSp = context.getSharedPreferences(
                    SP_FILE_KEY, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = topStoriesSp.edit();

            editor.putString(SP_VAL_KEY, input).apply();
        }
    }

    private class NewsTransformer implements Observable.Transformer<List<Integer>, List<News>> {

        final List<Integer> subNewsIds = new ArrayList<>();
        private int from;
        private int to;

        public NewsTransformer(int from, int count) {
            this.from = from;
            this.to = from + count;
        }

        @Override
        public Observable<List<News>> call(Observable<List<Integer>> newsIds) {
            // Get a subset from Top News IDs and save it for later lookup
            // Emit Top News IDs one at a time
            // Get News body
            //If API returns error, return null News
            //Filter out the NULL News (from any parse error)
            return newsIds
                    .map(newsIds1 -> {
                        if (from >= newsIds1.size()) {
                            throw new EndOfListException();
                        }

                        if (to > newsIds1.size()) {
                            to = newsIds1.size();
                        }

                        subNewsIds.addAll(newsIds1.subList(from, to));
                        return subNewsIds;
                    })
                    .flatMap(Observable::from)
                    .flatMap(integer -> getNews(integer)
                            .onErrorReturn(throwable -> null))
                    .timeout(Constants.CONN_TIMEOUT_SEC, TimeUnit.SECONDS)
                    .filter(news -> (news != null) && !news.isDeleted() && !news.isDead())
                    .toList()
                    .map(newses -> {
                        List<News> retval = new ArrayList<>();
                        Dictionary<Integer, News> dict = new Hashtable<>();

                        for (News news : newses) {
                            dict.put(news.getId(), news);
                        }

                        for (Integer topStory : subNewsIds) {
                            retval.add(dict.get(topStory));
                        }

                        return retval;
                    });
        }
    }

}
