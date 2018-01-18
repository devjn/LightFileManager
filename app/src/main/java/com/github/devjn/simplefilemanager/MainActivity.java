package com.github.devjn.simplefilemanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.github.devjn.filemanager.FileManager;
import com.github.devjn.filemanager.FileManagerActivity;

public class MainActivity extends FileManagerActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        FileManager.getConfig().setDefaultFolder(App.getDefaultFolder()).setShowFolderCount(App.getShowFolderCount());
        super.onCreate(savedInstanceState);
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
        } else if (id == android.R.id.home && getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
            return true;
        } else if (id == R.id.action_dialog) {
            FileManager.with(this).showHidden(false).setContentType("image/*").
                    showDialogFileManager(path -> Toast.makeText(MainActivity.this, "onResult: " + path, Toast.LENGTH_SHORT).show());
            return true;
        } else if (id == R.id.action_activity) {
            FileManager.with(this).showHidden(false).setContentType("image/*").
                    startFileManager(path -> Toast.makeText(MainActivity.this, "onResult: " + path, Toast.LENGTH_SHORT).show());
            return true;
        }

        return false;
    }

    @Override
    protected void onResume() {
        FileManager.getConfig().setDefaultFolder(App.getDefaultFolder()).setShowFolderCount(App.getShowFolderCount());
        super.onResume();
    }
}
