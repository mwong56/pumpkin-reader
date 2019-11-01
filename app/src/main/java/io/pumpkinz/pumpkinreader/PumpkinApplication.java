package io.pumpkinz.pumpkinreader;

import android.app.Application;
import android.arch.persistence.room.Room;

import io.pumpkinz.pumpkinreader.service.AppSharedPreferences;
import io.pumpkinz.pumpkinreader.service.HackerNewsApi;
import io.pumpkinz.pumpkinreader.service.HackerNewsRepository;
import io.pumpkinz.pumpkinreader.service.database.AppDatabase;

public class PumpkinApplication extends Application {

    public static PumpkinApplication instance;
    public AppDatabase database;
    public HackerNewsApi hackerNewsApi;
    public HackerNewsRepository hackerNewsRepository;
    public AppSharedPreferences sharedPreferences;


    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        hackerNewsApi = HackerNewsApi.RestClient.getApi();
        sharedPreferences = new AppSharedPreferences(getApplicationContext());
        database = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "pumpkin-reader-db").build();
        hackerNewsRepository = new HackerNewsRepository(getApplicationContext(), hackerNewsApi, database, sharedPreferences);
    }


}
