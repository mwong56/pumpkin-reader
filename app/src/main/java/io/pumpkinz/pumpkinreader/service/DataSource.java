package io.pumpkinz.pumpkinreader.service;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.pumpkinz.pumpkinreader.etc.Constants;
import io.pumpkinz.pumpkinreader.exception.EndOfListException;
import io.pumpkinz.pumpkinreader.model.Comment;
import io.pumpkinz.pumpkinreader.model.News;
import io.pumpkinz.pumpkinreader.util.Util;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.SingleTransformer;
import io.reactivex.functions.Consumer;


public class DataSource {

    private Context ctx;

    public DataSource(Context ctx) {
        this.ctx = ctx;
    }

    public Single<List<News>> getHNSaved(final int from, final int count) {
        return getHNSavedIds()
                .compose(new NewsTransformer(from, count));
    }

    public Single<List<News>> getHNNew(final int from, final int count, boolean isRefresh) {
        return getHNNewIds(isRefresh)
                .compose(new NewsTransformer(from, count));
    }

    public Single<List<News>> getHNTop(final int from, final int count, boolean isRefresh) {
        return getHNTopIds(isRefresh)
                .compose(new NewsTransformer(from, count));
    }

    public Single<List<News>> getHNAsk(final int from, final int count, boolean isRefresh) {
        return getHNAskIds(isRefresh)
                .compose(new NewsTransformer(from, count));
    }

    public Single<List<News>> getHNShow(final int from, final int count, boolean isRefresh) {
        return getHNShowIds(isRefresh)
                .compose(new NewsTransformer(from, count));
    }

    public Single<List<News>> getHNJob(final int from, final int count, boolean isRefresh) {
        return getHNJobIds(isRefresh)
                .compose(new NewsTransformer(from, count));
    }

    public Single<News> getNews(int id) {
        return RestClient.getService().getNews(id)
                .timeout(Constants.CONN_TIMEOUT_SEC, TimeUnit.SECONDS);
    }

    public Single<List<Comment>> getComments(final News news) {
        return Single.just(news.getCommentIds())
                .toObservable()
                .flatMap(Observable::fromIterable)
                .flatMap(commentId -> RestClient.getService().getComment(commentId).toObservable().doOnError(error -> {}))
                .flatMap(comments -> getInnerComments(comments).toObservable())
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

    private Single<List<Integer>> getHNSavedIds() {
        List<Integer> retval = getNewsIdsFromSp(Constants.SAVED_FILE_SP, Constants.SAVED_VAL_SP, ctx);
        return Single.just(retval);
    }

    private Single<List<Integer>> getHNNewIds(boolean isRefresh) {
        List<Integer> retval = getNewsIdsFromSp(Constants.NEW_FILE_SP, Constants.NEW_VAL_SP, ctx);

        if (retval.isEmpty() || isRefresh) {
            return RestClient.getService().getHNNewIds()
                    .doOnSuccess(new putToSpAction(ctx, Constants.NEW_FILE_SP, Constants.NEW_VAL_SP));
        } else {
            return Single.just(retval);
        }
    }

    private Single<List<Integer>> getHNTopIds(boolean isRefresh) {
        List<Integer> retval = getNewsIdsFromSp(Constants.TOP_FILE_SP, Constants.TOP_VAL_SP, ctx);

        if (retval.isEmpty() || isRefresh) {
            return RestClient.getService().getHNTopIds()
                    .doOnSuccess(new putToSpAction(ctx, Constants.TOP_FILE_SP, Constants.TOP_VAL_SP));
        } else {
            return Single.just(retval);
        }
    }

    private Single<List<Integer>> getHNAskIds(boolean isRefresh) {
        List<Integer> retval = getNewsIdsFromSp(Constants.ASK_FILE_SP, Constants.ASK_VAL_SP, ctx);

        if (retval.isEmpty() || isRefresh) {
            return RestClient.getService().getHNAskIds()
                    .doOnSuccess(new putToSpAction(ctx, Constants.ASK_FILE_SP, Constants.ASK_VAL_SP));
        } else {
            return Single.just(retval);
        }
    }

    private Single<List<Integer>> getHNShowIds(boolean isRefresh) {
        List<Integer> retval = getNewsIdsFromSp(Constants.SHOW_FILE_SP, Constants.SHOW_VAL_SP, ctx);

        if (retval.isEmpty() || isRefresh) {
            return RestClient.getService().getHNShowIds()
                    .doOnSuccess(new putToSpAction(ctx, Constants.SHOW_FILE_SP, Constants.SHOW_VAL_SP));
        } else {
            return Single.just(retval);
        }
    }

    private Single<List<Integer>> getHNJobIds(boolean isRefresh) {
        List<Integer> retval = getNewsIdsFromSp(Constants.JOB_FILE_SP, Constants.JOB_VAL_SP, ctx);

        if (retval.isEmpty() || isRefresh) {
            return RestClient.getService().getHNJobIds()
                    .doOnSuccess(new putToSpAction(ctx, Constants.JOB_FILE_SP, Constants.JOB_VAL_SP));
        } else {
            return Single.just(retval);
        }
    }

    private List<Integer> getNewsIdsFromSp(String newsFileKey, String newsValKey, Context context) {
        List<Integer> retval = new ArrayList<>();

        SharedPreferences topStoriesSp = context.getSharedPreferences(
                newsFileKey, Context.MODE_PRIVATE);
        String topStories = topStoriesSp.getString(newsValKey, "");

        if (!topStories.isEmpty()) {
            retval = Util.splitNews(topStories);
        }

        return retval;
    }

    private Flowable<Comment> getInnerComments(Comment comment) {
        if (comment != null && comment.getCommentIds().size() > 0) {
            return Single.merge(
                    Single.just(comment),
                    Single.fromObservable(Single.just(comment.getCommentIds())
                            .toObservable()
                            .flatMapIterable(list -> list)
                            .flatMap(commentId -> RestClient.getService().getComment(commentId).toObservable().doOnError(error -> {}))
                            .flatMap(updatedComment -> getInnerComments(updatedComment).toObservable()))
            );
        }

        return Flowable.empty();
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

    private class putToSpAction implements Consumer<List<Integer>> {
        private Context context;
        private String SP_FILE_KEY;
        private String SP_VAL_KEY;

        public putToSpAction(Context context, String SP_FILE_KEY, String SP_VAL_KEY) {
            this.context = context;
            this.SP_FILE_KEY = SP_FILE_KEY;
            this.SP_VAL_KEY = SP_VAL_KEY;
        }

        @Override
        public void accept(List<Integer> integers) {
            String input = Util.joinNews(integers);
            SharedPreferences topStoriesSp = context.getSharedPreferences(
                    SP_FILE_KEY, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = topStoriesSp.edit();

            editor.putString(SP_VAL_KEY, input).apply();
        }
    }

    private class NewsTransformer implements SingleTransformer<List<Integer>, List<News>> {

        final List<Integer> subNewsIds = new ArrayList<>();
        private int from;
        private int to;

        NewsTransformer(int from, int count) {
            this.from = from;
            this.to = from + count;
        }

        @Override
        public SingleSource<List<News>> apply(Single<List<Integer>> newsIds) {
            return newsIds
                    .map(list -> {
                        if (from >= list.size()) {
                            throw new EndOfListException();
                        }

                        if (to > list.size()) {
                            to = list.size();
                        }

                        subNewsIds.addAll(list.subList(from, to));
                        return subNewsIds;
                    })
                    .flatMap(Single::just)
                    .toObservable()
                    .flatMapIterable(list -> list)
                    .flatMap(newsId -> RestClient.getService().getNews(newsId).toObservable().doOnError(error -> {}))

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
