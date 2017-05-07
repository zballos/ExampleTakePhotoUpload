package br.com.zballos.examplephoto.model;

import io.realm.RealmObject;

/**
 * Created by zballos on 06/05/17.
 */

public class MyImage extends RealmObject {
    private String title;
    private String pathName;
    private boolean syncronized;

    public MyImage(){}

    public MyImage(String title, String pathName, boolean syncronized) {
        this.title = title;
        this.pathName = pathName;
        this.syncronized = syncronized;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPathName() {
        return pathName;
    }

    public void setPathName(String pathName) {
        this.pathName = pathName;
    }

    public boolean isSyncronized() {
        return syncronized;
    }

    public void setSyncronized(boolean syncronized) {
        this.syncronized = syncronized;
    }
}
