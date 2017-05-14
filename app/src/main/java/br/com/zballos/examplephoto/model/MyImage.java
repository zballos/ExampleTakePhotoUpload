package br.com.zballos.examplephoto.model;

import android.util.Log;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.annotations.PrimaryKey;

/**
 * Created by zballos on 06/05/17.
 */

public class MyImage extends RealmObject {
    @PrimaryKey
    private String UUID;
    private String title;
    private String pathName;
    private boolean syncronized;

    public MyImage(){}

    public MyImage(String UUID, String title, String pathName, boolean syncronized) {
        this.UUID = UUID;
        this.title = title;
        this.pathName = pathName;
        this.syncronized = syncronized;
    }

    public String getUUID() {
        return UUID;
    }

    public void setUUID(String UUID) {
        this.UUID = UUID;
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

    public static void checkInvalidAndDelete() {
        Realm realm = Realm.getDefaultInstance();

        final RealmResults<MyImage> results = realm.where(MyImage.class)
                .beginGroup()
                    .equalTo("pathName", "")
                    .or()
                    .equalTo("UUID", "")
                .endGroup()
                .findAll();

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                results.deleteAllFromRealm();
            }
        });
    }
}
