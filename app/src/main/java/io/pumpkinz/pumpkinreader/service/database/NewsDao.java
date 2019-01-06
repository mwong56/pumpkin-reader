package io.pumpkinz.pumpkinreader.service.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import io.pumpkinz.pumpkinreader.service.database.entity.JsonNews;
import io.reactivex.Observable;
import io.reactivex.Single;

@Dao
public interface NewsDao {

    @Query("SELECT * from news where newsId = :newsId limit 1")
    Single<JsonNews> loadNews(int newsId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertNews(JsonNews news);
}