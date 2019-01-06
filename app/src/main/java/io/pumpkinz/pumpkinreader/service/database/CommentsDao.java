package io.pumpkinz.pumpkinreader.service.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import io.pumpkinz.pumpkinreader.service.database.entity.JsonComment;


@Dao
interface CommentsDao {
    @Query("SELECT * from comments where commentId = :commentId limit 1")
    JsonComment loadComment(int commentId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertComment(JsonComment comment);
}