package inha.inti.mobile_midterm;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;

public class Post implements Serializable {
    private String title;
    private String snippet;
    private transient LatLng latlng;
    private String imagePath;
    private int id;

    public Post(String _title, String _snippet, LatLng _latlng, String _imagePath, int _id) {
        title = _title;
        snippet = _snippet;
        latlng = _latlng;
        imagePath = _imagePath;
        id = _id;
    }

    public String getTitle() {
        return title;
    }

    public String getSnippet() {
        return snippet;
    }

    public LatLng getLatlng() {
        return latlng;
    }

    public int getId() {
        return id;
    }

    public String getImagePath() {
        return imagePath;
    }
}
