package io.pumpkinz.pumpkinreader.service.database.entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.TypeConverters;

import io.pumpkinz.pumpkinreader.model.News;

@Entity(tableName = "news")
@TypeConverters(Converters.class)
public class JsonNews {
    @PrimaryKey
    @ColumnInfo(name = "newsId")
    public int newsId;
    @ColumnInfo(name = "news")
    public News news;

    public JsonNews(int newsId, News news) {
        this.newsId = newsId;
        this.news = news;
    }
}
