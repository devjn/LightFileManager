/*
 * Copyright 2017 devjn
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.devjn.filemanager;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.github.devjn.filemanager.utils.PermissionUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class FileManagerActivity extends AppCompatActivity implements FragmentManager.OnBackStackChangedListener, ListFilesFragment.FragmentStateListener {

    private static int REQUEST_WRITE = 100;

    FileManager.RequestHolder requestHolder;

    private String currentPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getContentView());
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        checkPermissions();

        requestHolder = FileManager.getRequestHolder(getIntent().getIntExtra(Config.EXTRA_ID, -1));

        if (savedInstanceState == null && PermissionUtils.isWriteGranted(this)) {
            addFragments();
        }
    }

    @LayoutRes
    protected int getContentView() {
        return R.layout.activity_file_manager;
    }

    @Override
    protected void onResume() {
        super.onResume();
        getSupportFragmentManager().addOnBackStackChangedListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        getSupportFragmentManager().removeOnBackStackChangedListener(this);
    }

    private void addFragments() {
        File folder = new File(getConfig().getDefaultFolder());
        if (!folder.exists() || !folder.isDirectory())
            folder = Environment.getExternalStorageDirectory();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        if (!folder.equals(Environment.getExternalStorageDirectory())) {
            final String parentPath = Environment.getExternalStorageDirectory().getAbsolutePath();
            String path = folder.getAbsolutePath();
            Fragment fragment = ListFilesFragment.newInstance(Environment.getExternalStorageDirectory().getName(), parentPath, false, requestHolder.getId());
            transaction.add(R.id.container, fragment, "parent").commit();
            ArrayList<File> list = new ArrayList<>();
            while (path.length() > 1 && !parentPath.equals(path)) {
                path = path.substring(0, path.lastIndexOf("/"));
                File folderChild = new File(path);
                if (!parentPath.equals(path))
                    list.add(folderChild);
            }
            Collections.reverse(list);
            for (File child : list) {
                fragment = ListFilesFragment.newInstance(child.getName(), child.getAbsolutePath(), false, requestHolder.getId());
                transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.container, fragment, "child")
                        .addToBackStack(null)
                        .commit();
            }
            fragment = ListFilesFragment.newInstance(folder.getName(), folder.getAbsolutePath(), true, requestHolder.getId());
            transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.container, fragment, "main")
                    .addToBackStack(null).commit();
        } else {
            Fragment fragment = ListFilesFragment.newInstance(folder.getName(), folder.getPath(), true, requestHolder.getId());
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, fragment, "main")
                    .commit();
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void checkPermissions() {
        if (!PermissionUtils.isWriteGranted(this))
            PermissionUtils.requestPermission(this, REQUEST_WRITE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home && getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
            return true;
        }

        return false;
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_WRITE) {
            if (!PermissionUtils.isPermissionGranted(permissions, grantResults,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) && ContextCompat.checkSelfPermission(FileManagerActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) !=
                    PackageManager.PERMISSION_GRANTED) {
                PermissionUtils.requestPermission(FileManagerActivity.this, 0, Manifest.permission.READ_EXTERNAL_STORAGE, true);
            } else {
                triggerRebirth(this);
            }
        }
    }

    public static void triggerRebirth(Activity activity) {
        Intent intent = new Intent(activity, FileManagerActivity.class);
        intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
        intent.putExtras(activity.getIntent());
        activity.startActivity(intent);
        activity.finish();

        Runtime.getRuntime().exit(0);
    }

    @Override
    public void onBackStackChanged() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar == null) return;
        if (getSupportFragmentManager().getBackStackEntryCount() > 0)
            actionBar.setDisplayHomeAsUpEnabled(true);
        else actionBar.setDisplayHomeAsUpEnabled(false);
    }


    protected final FileManager.Options getOptions() {
        if (requestHolder == null) {
            return FileManager.getInstance().getOptions();
        } else return requestHolder.options;
    }

    protected final Config getConfig() {
        if (requestHolder == null || requestHolder.options.getConfig() == null) {
            return FileManager.getInstance().getConfig();
        } else return requestHolder.options.getConfig();
    }

    public String getCurrentPath() {
        return currentPath;
    }

    @Override
    public void onFragmentResume(String path) {
        this.currentPath = path;
    }


}
