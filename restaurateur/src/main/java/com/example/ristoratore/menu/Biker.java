package com.example.ristoratore.menu;

import android.widget.ImageView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.GregorianCalendar;

public class Biker implements Serializable, Comparable<Biker>{
    private String name;
    private String work_hours;
    private String work_area;
    private transient ImageView photo;
    private String photoUri;
    private String uid;

    public Double getDist() {
        return dist;
    }

    public void setDist(Double dist) {
        this.dist = dist;
    }

    private Double dist;

    public Biker(String name, String work_hours, String work_area, ImageView photo, String photoUri){
        this.name=name;
        this.work_hours=work_hours;
        this.work_area=work_area;
        this.photo=photo;
        this.photoUri=photoUri;
    }

    public Biker(){}

    public String getWork_hours() { return work_hours; }

    public void setWork_hours(String work_hours) { this.work_hours = work_hours; }

    public String getWork_area() { return work_area; }

    public void setWork_area(String work_area) { this.work_area = work_area; }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public String getPhotoUri() { return photoUri; }

    public void setUid(String uid) { this.uid = uid; }

    public String getUid() { return uid; }

    public void setPhotoUri(String photoUri) { this.photoUri = photoUri; }

    public ImageView getPhoto() { return photo; }

    public void setPhoto(ImageView photo) { this.photo = photo; }

    @Override
    public int compareTo(Biker o) {
        return getDist().compareTo(o.getDist());
    }
}
