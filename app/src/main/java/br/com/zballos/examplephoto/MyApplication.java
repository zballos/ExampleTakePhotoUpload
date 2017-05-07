package br.com.zballos.examplephoto;

import android.app.Application;

import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by zballos on 06/05/17.
 */

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Realm.init(this);
        RealmConfiguration config = new RealmConfiguration.Builder()
                .name("ExamplePhoto.realm")
                .build();
        Realm.setDefaultConfiguration(config);
    }
}
