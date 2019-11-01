package io.pumpkinz.pumpkinreader.service.database.entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.TypeConverters;

import io.pumpkinz.pumpkinreader.model.Comment;

@Entity(tableName = "comments")
@TypeConverters(Converters.class)
public class JsonComment {
    @PrimaryKey
    @ColumnInfo(name = "commentId")
    public int commentId;
    @ColumnInfo(name = "comment")
    public Comment comment;

    public JsonComment(int commentId, Comment comment) {
        this.commentId = commentId;
        this.comment = comment;
    }
}
