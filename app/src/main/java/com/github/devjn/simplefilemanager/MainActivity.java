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
        App.updateFileManagerPrefs();
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
        final int id = item.getItemId();
        switch (id) {
            case R.id.action_new:
                String[] options = new String[]{"File", "Folder"};
                Utils.showListDialog(this, options, (dialog, which) -> DialogNewItem.Companion.show(getSupportFragmentManager(), which, getCurrentPath()));
                return true;
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_views:
                DialogViewSettingsFragment.Companion.show(getSupportFragmentManager(), this::recreate);
                return true;
            case android.R.id.home:
                if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                    getSupportFragmentManager().popBackStack();
                    return true;
                } else return false;
            case R.id.action_dialog:
                FileManager.with(this).showHidden(false).setContentType("image/*").
                        showDialogFileManager(path -> Toast.makeText(MainActivity.this, "onResult: " + path, Toast.LENGTH_SHORT).show());
                return true;
            case R.id.action_activity:
                FileManager.with(this).showHidden(false).setContentType("image/*").
                        startFileManager(path -> Toast.makeText(MainActivity.this, "onResult: " + path, Toast.LENGTH_SHORT).show());
                return true;
        }

        return false;
    }

    @Override
    protected void onStart() {
        App.updateFileManagerPrefs();
        super.onStart();
    }

    @Override
    protected void onResume() {
        App.updateFileManagerPrefs();
        super.onResume();
    }
}
