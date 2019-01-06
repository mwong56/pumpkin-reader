package io.pumpkinz.pumpkinreader.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.List;

import io.pumpkinz.pumpkinreader.model.Comment;
import io.pumpkinz.pumpkinreader.model.News;
import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;
import retrofit.http.GET;
import retrofit.http.Path;
import rx.Observable;

public interface HackerNewsApi {

    @GET("/newstories.json")
    Observable<List<Integer>> getHNNewIds();

    @GET("/topstories.json")
    Observable<List<Integer>> getHNTopIds();

    @GET("/askstories.json")
    Observable<List<Integer>> getHNAskIds();

    @GET("/showstories.json")
    Observable<List<Integer>> getHNShowIds();

    @GET("/jobstories.json")
    Observable<List<Integer>> getHNJobIds();

    @GET("/item/{news}.json")
    Observable<News> getNews(@Path("news") int newsId);

    @GET("/item/{comment}.json")
    Observable<Comment> getComment(@Path("comment") int commentId);

    class RestClient {
        private static final String HN_API_ENDPOINT = "https://hacker-news.firebaseio.com/v0";
        private static RestAdapter restAdapter;
        private static HackerNewsApi apiService;

        private RestClient() {
        }

        private static RestAdapter getAdapter() {
            if (restAdapter == null) {
                Gson gson = new GsonBuilder()
                        .registerTypeAdapter(News.class, new News.NewsDeserializer())
                        .create();

                restAdapter = new RestAdapter.Builder()
                        .setEndpoint(HN_API_ENDPOINT)
                        .setConverter(new GsonConverter(gson))
                        .build();
            }

            return restAdapter;
        }

        public static HackerNewsApi getApi() {
            if (apiService == null) {
                apiService = getAdapter().create(HackerNewsApi.class);
            }
            return apiService;
        }

    }
}
