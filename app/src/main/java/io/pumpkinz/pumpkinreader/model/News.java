package io.pumpkinz.pumpkinreader.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.util.Log;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import org.parceler.Parcel;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import io.pumpkinz.pumpkinreader.etc.Constants;

@Entity(tableName = "news")
@Parcel
public class News extends Item {

    @ColumnInfo(name="kids")
    List<Integer> kids;
    @ColumnInfo(name="url")
    String url;
    @ColumnInfo(name="score")
    int score;
    @ColumnInfo(name="title")
    String title;
    @ColumnInfo(name="descendants")
    int descendants;

    public News() {
        this.kids = new ArrayList<>();
        this.descendants = 0;
    }

    public News(int id) {
        this.id = id;
        this.kids = new ArrayList<>();
        this.descendants = 0;
    }

    public News(int id, boolean deleted, String type, String by, long time, String text,
                boolean dead, List<Integer> kids, String url, int score, String title) {
        super(id, deleted, type, by, time, text, dead);
        this.kids = kids;
        this.url = url;
        this.score = score;
        this.title = title;
        this.kids = new ArrayList<>();
        this.descendants = 0;
    }

    public List<Integer> getCommentIds() {
        return kids;
    }

    public String getUrl() {
        return url;
    }

    public int getScore() {
        return score;
    }

    public String getTitle() {
        return title;
    }

    public int getTotalComments() {
        return descendants;
    }

    @Override
    public String toString() {
        String parent = super.toString();

        StringBuilder sb = new StringBuilder();
        sb.append(parent)
                .append("URL=" + getUrl())
                .append("; Score=" + getScore())
                .append("; Title=" + getTitle())
                .append("; Descendants=" + getTotalComments())
                .append("; Kids=" + getCommentIds().toString())
                .append("\n");

        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (!(o instanceof News)) {
            return false;
        }

        News news = (News) o;
        return news.getId() == this.getId();
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + this.getId();

        return result;
    }

    public static class NewsDeserializer implements JsonDeserializer<News> {

        @Override
        public News deserialize(JsonElement json, java.lang.reflect.Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jo = json.getAsJsonObject();
            try {
                Item.Type type = Item.Type.fromString(jo.get("type").getAsString());

                switch (type) {
                    case Story:
                        return context.deserialize(json, Story.class);
                    case Job:
                        return context.deserialize(json, Job.class);
                    case Poll:
                        return context.deserialize(json, Poll.class);
                    default:
                        throw new AssertionError("Unknown News type: " + type);
                }
            } catch (AssertionError | JsonParseException ex) {
                Log.d(Constants.APP, ex.getMessage());
            }

            return null;
        }
    }
}
