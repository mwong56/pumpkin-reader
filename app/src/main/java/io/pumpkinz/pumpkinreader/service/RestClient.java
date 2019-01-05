package io.pumpkinz.pumpkinreader.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.List;

import io.pumpkinz.pumpkinreader.model.Comment;
import io.pumpkinz.pumpkinreader.model.News;
import io.reactivex.Single;
import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;


public class RestClient {

    private static final String HN_API_ENDPOINT = "https://hacker-news.firebaseio.com/v0";
    private static RestAdapter restAdapter;
    private static ApiService apiService;

    private RestClient() {
    }

    private static RestAdapter getAdapter() {
        if (restAdapter == null) {
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(News.class, new ItemTypeAdapter())
                    .create();

            restAdapter = new RestAdapter.Builder()
                    .setEndpoint(HN_API_ENDPOINT)
                    .setConverter(new GsonConverter(gson))
                    .build();
        }

        return restAdapter;
    }

    public static ApiService getService() {
        if (apiService == null) {
            apiService = getAdapter().create(ApiService.class);
        }
        return apiService;
    }

}
