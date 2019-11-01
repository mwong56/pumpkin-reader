package io.pumpkinz.pumpkinreader.service.database.entity;

import android.arch.persistence.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

import io.pumpkinz.pumpkinreader.model.Comment;
import io.pumpkinz.pumpkinreader.model.News;

public class Converters {
    @TypeConverter
    public static Comment fromStringToComment(String value) {
        Type commentType = new TypeToken<Comment>() {}.getType();
        return new Gson().fromJson(value, commentType);
    }

    @TypeConverter
    public static String fromCommentToString(Comment comment) {
        Gson gson = new Gson();
        return gson.toJson(comment);
    }

    @TypeConverter
    public static News fromStringToNews(String value) {
        Type type = new TypeToken<News>() {}.getType();
        return new Gson().fromJson(value, type);
    }

    @TypeConverter
    public static String fromNewsToString(News news) {
        Gson gson = new Gson();
        return gson.toJson(news);
    }
}
