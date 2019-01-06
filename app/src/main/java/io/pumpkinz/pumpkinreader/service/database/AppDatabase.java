package io.pumpkinz.pumpkinreader.service.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

import io.pumpkinz.pumpkinreader.service.database.entity.JsonComment;
import io.pumpkinz.pumpkinreader.service.database.entity.JsonNews;

@Database(entities = { JsonComment.class, JsonNews.class }, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    abstract CommentsDao commentsDao();
    abstract NewsDao newsDao();
}
