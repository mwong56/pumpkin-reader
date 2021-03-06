package io.pumpkinz.pumpkinreader.model;

import org.parceler.Parcel;

import java.io.Serializable;
import java.util.ArrayList;


@Parcel
public class Job extends News implements Serializable {

    public Job() {
    }

    public Job(int id, boolean deleted, String type, String by, long time, String text,
               boolean dead, String url, int score, String title) {
        super(id, deleted, type, by, time, text, dead, new ArrayList<Integer>(), url, score,
                title);
    }

}
