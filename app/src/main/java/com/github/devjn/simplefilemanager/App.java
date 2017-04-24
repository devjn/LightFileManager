package com.github.devjn.simplefilemanager;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;

/**
 * Created by @author Jahongir on 24-Apr-17
 * devjn@jn-arts.com
 * App.java
 */

public class App extends Application {

    private static final String TAG = "Common global application";
    public static final String PROFILE_ID = "profile_id";
    public static volatile Context applicationContext = null;

    private static SharedPreferences prefs;

    private static String DEF_FOLDER = "DEF_FOLDER";

    @Override
    public void onCreate() {
        super.onCreate();
        applicationContext = getApplicationContext();
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
    }

    public static void setDefaultFolder(String folder) {
        prefs.edit().putString(DEF_FOLDER, folder).apply();
    }

    public static String getDefaultFolder() {
        return prefs.getString(DEF_FOLDER, Environment.getExternalStorageDirectory().getAbsolutePath());
    }

}
