package io.pumpkinz.pumpkinreader.service;

import java.util.List;

import io.pumpkinz.pumpkinreader.model.Comment;
import io.pumpkinz.pumpkinreader.model.News;
import io.reactivex.Single;
import retrofit.http.GET;
import retrofit.http.Path;


public interface ApiService {

    @GET("/newstories.json")
    Single<List<Integer>> getHNNewIds();

    @GET("/topstories.json")
    Single<List<Integer>> getHNTopIds();

    @GET("/askstories.json")
    Single<List<Integer>> getHNAskIds();

    @GET("/showstories.json")
    Single<List<Integer>> getHNShowIds();

    @GET("/jobstories.json")
    Single<List<Integer>> getHNJobIds();

    @GET("/item/{news}.json")
    Single<News> getNews(@Path("news") int newsId);

    @GET("/item/{comment}.json")
    Single<Comment> getComment(@Path("comment") int commentId);
}
