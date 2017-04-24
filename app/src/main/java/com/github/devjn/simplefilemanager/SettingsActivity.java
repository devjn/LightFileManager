package com.github.devjn.simplefilemanager;


import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceDialogFragmentCompat;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.view.MenuItem;
import android.view.View;

import com.github.devjn.simplefilemanager.utils.DirectoryPrefernces;

import java.io.File;


public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setupActionBar();
        if (savedInstanceState == null) {
            Fragment preferenceFragment = new GeneralPreferenceFragment();
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.add(R.id.pref_container, preferenceFragment);
            ft.commit();
        }
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            if (!super.onOptionsItemSelected(item)) {
                NavUtils.navigateUpFromSameTask(this);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }



    public static class GeneralPreferenceFragment extends PreferenceFragmentCompat {

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            // Load the Preferences from the XML file
            addPreferencesFromResource(R.xml.app_preferences);
        }

        @Override
        public void onDisplayPreferenceDialog(android.support.v7.preference.Preference preference) {
            DialogFragment dialogFragment = null;
            if (preference instanceof DirectoryPrefernces) {
                dialogFragment = DirectoryPreferenceFragment.newInstance(preference.getKey());
            }

            if (dialogFragment != null) {
                dialogFragment.setTargetFragment(this, 0);
                dialogFragment.show(this.getFragmentManager(),
                        "android.support.v7.preference" +
                                ".PreferenceFragment.DIALOG");
            }
            else {
                super.onDisplayPreferenceDialog(preference);
            }
        }

    }


    public static class DirectoryPreferenceFragment extends PreferenceDialogFragmentCompat {

        public static DirectoryPreferenceFragment newInstance(String key) {
            final DirectoryPreferenceFragment
                    fragment = new DirectoryPreferenceFragment();
            final Bundle b = new Bundle(1);
            b.putString(ARG_KEY, key);
            fragment.setArguments(b);
            return fragment;
        }

        @Override
        protected void onBindDialogView(View view) {
            super.onBindDialogView(view);

            File folder = new File(App.getDefaultFolder());
            if (!folder.exists()) folder = Environment.getExternalStorageDirectory();
            Fragment fragment = MainActivityFragment.newInstance(folder.getName(), folder.getAbsolutePath());
            getChildFragmentManager().beginTransaction().
                    add(R.id.pref_container, fragment, "settings")
                    .commit();
        }

        @Override
        public void onDialogClosed(boolean positiveResult) {

        }

    }

}
