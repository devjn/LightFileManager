package com.github.devjn.simplefilemanager;


import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.DialogPreference;
import android.support.v7.preference.PreferenceDialogFragmentCompat;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.github.devjn.simplefilemanager.utils.DirectoryPrefernces;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;


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
            } else {
                super.onDisplayPreferenceDialog(preference);
            }
        }

    }


    public static class DirectoryPreferenceFragment extends PreferenceDialogFragmentCompat
            implements DataLoader.DataListener, FileListAdapter.FilesClickListener {

        public static DirectoryPreferenceFragment newInstance(String key) {
            final DirectoryPreferenceFragment
                    fragment = new DirectoryPreferenceFragment();
            final Bundle b = new Bundle(1);
            b.putString(ARG_KEY, key);
            fragment.setArguments(b);
            return fragment;
        }

        private Toolbar mToolbar;
        private RecyclerView mRecyclerView;
        private LinearLayoutManager mLayoutManager;
        private FileListAdapter mAdapter;

        private List<FileData> mData;
        private String mPath;
        private String mName;
        private String parentPath;

        @Override
        protected void onBindDialogView(View view) {
            super.onBindDialogView(view);
            mToolbar = (Toolbar) view.findViewById(R.id.toolbar);
            mRecyclerView = (RecyclerView) view.findViewById(R.id.list);
            mLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
            mRecyclerView.setHasFixedSize(true);
            mRecyclerView.setLayoutManager(mLayoutManager);

            mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    if (mPath != null && mPath.length() > 1 && !parentPath.equals(mPath)) {
                        int index = mPath.lastIndexOf("/");
                        if (index < 1) return;
                        mPath = mPath.substring(0, index);
                        File file = new File(mPath);
                        if (file.isDirectory())
                            loadData(file);
                    } else dismiss();
                }
            });


            mAdapter = new FileListAdapter(getContext(), this, mData,
                    getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT);
            mRecyclerView.setAdapter(mAdapter);

            parentPath = Environment.getExternalStorageDirectory().getAbsolutePath();
            File folder = new File(App.getDefaultFolder());
            if (!folder.exists() || !folder.isDirectory())
                folder = Environment.getExternalStorageDirectory();

            DataLoader.INSTANCE.setListener(this);
            loadData(folder);
        }

        @Override
        public void onDialogClosed(boolean positiveResult) {
            if (positiveResult) {
                // Save the value
                DialogPreference preference = getPreference();
                if (preference instanceof DirectoryPrefernces) {
                    DirectoryPrefernces dirPreference = ((DirectoryPrefernces) preference);
                    if (dirPreference.callChangeListener(mPath)) {
                        // Save the value
                        dirPreference.setDirectory(mPath);
                    }
                }
            }
        }

        @Override
        public void onDataLoad(@NotNull List<? extends FileData> list) {
            this.mData = (List<FileData>) list;
            mAdapter.setData(mData);
        }

        @Override
        public void onClick(int position) {
            FileData fileData = mData.get(position);
            File file = new File(fileData.getPath());
            if (file.isDirectory())
                loadData(file);
            else
                Toast.makeText(getActivity(), "Not a directory", Toast.LENGTH_SHORT).show();
        }

        @Override
        public boolean onLongClick(int position) {
            return false;
        }

        private void loadData(File folder) {
            mPath = folder.getPath();
            mName = folder.getName();
            mToolbar.setTitle(mName);
            DataLoader.INSTANCE.loadData(folder);
        }

    }

}
