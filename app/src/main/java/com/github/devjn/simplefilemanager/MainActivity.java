package com.github.devjn.simplefilemanager;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.github.devjn.simplefilemanager.utils.PermissionUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class MainActivity extends AppCompatActivity {

    private static int REQUEST_WRITE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        checkPermissions();

        if (savedInstanceState == null && PermissionUtils.isWriteGranted(this)) {
            addFragments();
        }

        getSupportFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                ActionBar actionBar = getSupportActionBar();
                if (actionBar == null) return;
                if (getSupportFragmentManager().getBackStackEntryCount() > 0)
                    actionBar.setDisplayHomeAsUpEnabled(true);
                else actionBar.setDisplayHomeAsUpEnabled(false);
            }
        });
    }

    private void addFragments() {
        File folder = new File(App.getDefaultFolder());
        if (!folder.exists() || !folder.isDirectory()) folder = Environment.getExternalStorageDirectory();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        if (!folder.equals(Environment.getExternalStorageDirectory())) {
            final String parentPath = Environment.getExternalStorageDirectory().getAbsolutePath();
            String path = folder.getAbsolutePath();
            Fragment fragment = ListFilesFragment.newInstance(Environment.getExternalStorageDirectory().getName(), parentPath, false);
            transaction.add(R.id.container, fragment, "parent").commit();
            ArrayList<File> list = new ArrayList<>();
            while (path.length() > 1 && !parentPath.equals(path)) {
                path = path.substring(0, path.lastIndexOf("/"));
                File folderChild = new File(path);
                if(!parentPath.equals(path))
                list.add(folderChild);
            }
            Log.i("Main", "list: " + list.size() + " ,content: "+list);
            Collections.reverse(list);
            for (File child : list) {
                fragment = ListFilesFragment.newInstance(child.getName(), child.getAbsolutePath(), false);
                transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.container, fragment, "child")
                        .addToBackStack(null)
                        .commit();
            }
            fragment = ListFilesFragment.newInstance(folder.getName(), folder.getAbsolutePath(), true);
            transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.container, fragment, "main")
                    .addToBackStack(null).commit();
        } else {
            Fragment fragment = ListFilesFragment.newInstance(folder.getName(), folder.getPath(), true);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, fragment, "main")
                    .commit();
        }
    }

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

        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        } else if(id == android.R.id.home && getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
            return true;
        }

        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_WRITE) {
            if (!PermissionUtils.isPermissionGranted(permissions, grantResults,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) && ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) !=
                    PackageManager.PERMISSION_GRANTED) {
                PermissionUtils.requestPermission(MainActivity.this, 0, Manifest.permission.READ_EXTERNAL_STORAGE, true);
            } else {
                triggerRebirth(this);
            }
        }
    }

    public static void triggerRebirth(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
        if (context instanceof Activity) {
            ((Activity) context).finish();
        }

        Runtime.getRuntime().exit(0);
    }

}
