package com.github.devjn.simplefilemanager;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;

import com.bumptech.glide.Glide;
import com.github.devjn.filemanager.FileManager;

/**
 * Created by @author Jahongir on 24-Apr-17
 * devjn@jn-arts.com
 * App.java
 */

public class App extends Application {

    public static final String TAG = "SimpleFileManager";
    public static volatile Context applicationContext = null;

    public static String FILES_AUTHORITY = BuildConfig.APPLICATION_ID + ".fileprovider";

    private static SharedPreferences prefs;

    private static String DEF_FOLDER = "DEF_FOLDER";

    @Override
    public void onCreate() {
        super.onCreate();
        applicationContext = getApplicationContext();
        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        FileManager.init(applicationContext).setDefaultFolder(App.getDefaultFolder()).setShowFolderCount(App.getShowFolderCount())
                .setCustomImageLoader((imageView, fileData) -> Glide.with(imageView.getContext()).load(fileData.getPath()).into(imageView));

        FileManager.setIconForExtension("apk", R.drawable.ic_apk);
    }

    public static void setDefaultFolder(String folder) {
        prefs.edit().putString(DEF_FOLDER, folder).apply();
    }

    public static String getDefaultFolder() {
        return prefs.getString(DEF_FOLDER, Environment.getExternalStorageDirectory().getAbsolutePath());
    }

    public static boolean getShowFolderCount() {
        return prefs.getBoolean(applicationContext.getString(R.string.pref_folder_count), true);
    }

}
