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

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.FileObserver;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.Formatter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.github.devjn.filemanager.utils.IntentUtils;
import com.github.devjn.filemanager.utils.Utils;
import com.github.devjn.filemanager.utils.ViewUtils;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class ListFilesFragment extends Fragment implements DataLoader.DataListener, FileListAdapter.FilesClickListener, ActionMode.Callback {

    public static String TAG = ListFilesFragment.class.getSimpleName();
    private static String LOAD = "LOAD";
    private static String NAME_KEY = "NAME";
    private static String FOLDER_KEY = "FOLDER";

    public static ListFilesFragment newInstance(String name, String folderPath, boolean load, int requestId) {
        ListFilesFragment fragment = new ListFilesFragment();
        Bundle args = new Bundle();
        args.putBoolean(LOAD, load);
        args.putString(NAME_KEY, name);
        args.putString(FOLDER_KEY, folderPath);
        args.putInt(Config.EXTRA_ID, requestId);
        fragment.setArguments(args);
        return fragment;
    }

    public ListFilesFragment() {
    }

    private View mRootView;
    private RecyclerView mRecyclerView;
    private GridLayoutManager mLayoutManager;
    private FileListAdapter mAdapter;

    private ActionMode actionMode;
    private List<Integer> mSelectedActionItems;
    private FileManager.RequestHolder requestHolder;
    private MyFileObserver mFileObserver;

    private List<? extends FileData> mData;
    private String mName;
    private String mPath;

    private boolean isPortrait;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
        Bundle args = getArguments();
        DataLoader.getInstance().setListener(this);
        mSelectedActionItems = new ArrayList<>();
        if (args != null && args.containsKey(FOLDER_KEY)) {
            this.requestHolder = FileManager.getRequestHolder(args.getInt(Config.EXTRA_ID));
            mPath = args.getString(FOLDER_KEY);
            mName = args.getString(NAME_KEY, "");
            if (!args.getBoolean(LOAD)) return;
            if (mPath != null && !mPath.isEmpty()) {
                mFileObserver = new MyFileObserver(mPath);
                loadData();
            }
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_main, container, false);

        isPortrait = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;

        mRecyclerView = mRootView.findViewById(R.id.list);
        mLayoutManager = ViewUtils.getLayoutManagerForStyle(getContext(), isPortrait);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(mLayoutManager);
        return mRootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        android.support.v7.app.ActionBar ab = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (ab != null) {
            ab.setTitle(mName);
            if (getActivity().getSupportFragmentManager().getBackStackEntryCount() > 0)
                ab.setDisplayHomeAsUpEnabled(true);
        }

        mAdapter = new FileListAdapter(requestHolder, this, mData,
                getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT);
        mRecyclerView.setAdapter(mAdapter);

        if (savedInstanceState != null) {
            try {
                mSelectedActionItems = savedInstanceState.getIntegerArrayList("SelectedActionItems");
                if (mSelectedActionItems != null && !mSelectedActionItems.isEmpty()) {
                    actionMode = ((AppCompatActivity) getActivity()).startSupportActionMode(ListFilesFragment.this);
                    restoreActionMode();
                }
            } catch (ClassCastException e) {
                mSelectedActionItems.clear();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() instanceof FragmentStateListener)
            ((FragmentStateListener) getActivity()).onFragmentResume(mPath);
        DataLoader.getInstance().setListener(this);
        mFileObserver.startWatching();
        if (mPath != null && !mPath.isEmpty())
            loadData();
    }

    @Override
    public void onPause() {
        super.onPause();
        DataLoader.getInstance().removeListener(this);
        mFileObserver.stopWatching();
    }

    @Override
    public void onSaveInstanceState(@NonNull final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putIntegerArrayList("SelectedActionItems", (ArrayList<Integer>) mSelectedActionItems);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Do something that differs the Activity's menu here
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == R.id.action_refresh) {
            if (actionMode != null) return false;
            refreshItem = item;
            setRefreshing(true);
            loadData();
            return true;
        } else if (i == android.R.id.home) {
            if (actionMode != null) {
                actionMode.finish();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private MenuItem refreshItem;

    public void setRefreshing(final boolean refreshing) {
        if (refreshItem != null) {
            if (refreshing) {
                refreshItem.setActionView(R.layout.actionbar_progress);
            } else {
                refreshItem.setActionView(null);
                Toast.makeText(getActivity(), "Content updated", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onDataLoad(@NonNull List<FileData> list) {
        this.mData = list;
        mAdapter.setData(mData);
        setRefreshing(false);
    }

    @Override
    public void onClick(int position) {
        if (actionMode != null) {
            toggleSelection(position);
            return;
        }

        FileData fileData = mData.get(position);
        if (fileData.isFolder()) {
            Fragment fragment = ListFilesFragment.newInstance(fileData.getName(), fileData.getPath(), true, requestHolder.getId());
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            transaction.replace(R.id.container, fragment, "child")
                    .addToBackStack(null)
                    .commit();
        } else {
            if (requestHolder != null) {
                if (requestHolder.callback != null) {
                    FileManager.deliverResult(requestHolder.options.getId(), fileData.getPath());
                    getActivity().finish();
                }
                //TODO: There is a bug, which results in non null calling activity after coming back from another activity
                else if (getActivity().getCallingActivity() != null && requestHolder.getId() >= 0) {
                    Intent data = new Intent();
                    data.setData(Uri.parse(fileData.getPath()));
                    getActivity().setResult(Activity.RESULT_OK, data);
                } else open(fileData);
            } else open(fileData);
        }
    }

    private void open(FileData fileData) {
        if (!IntentUtils.openFile(getContext(), fileData)) {
            Toast.makeText(getContext(), "No handler for this type of file.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onLongClick(int position) {
        if (actionMode != null)
            return false;
        // Start the CAB using the ActionMode.Callback defined above
        actionMode = ((AppCompatActivity) getActivity()).startSupportActionMode(ListFilesFragment.this);
        toggleSelection(position);
        return true;
    }

    private void toggleSelection(int position) {
        mSelectedActionItems.add(position);
        mAdapter.toggleSelection(position);
        int count = mAdapter.getSelectedItemCount();
        String title = getString(R.string.selected_count, count);
        actionMode.setTitle(title);
        if (count <= 0) actionMode.finish();
    }


    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        // Inflate a menu resource providing context menu items
        getActivity().getMenuInflater().inflate(R.menu.contextual_menu, menu);
        return true;
    }

    public void restoreActionMode() {
        for (int i = 0; i < mSelectedActionItems.size(); i++) {
            mAdapter.toggleSelection(mSelectedActionItems.get(i));
        }
        String title = getString(R.string.selected_count, mAdapter.getSelectedItemCount());
        actionMode.setTitle(title);
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        int i = item.getItemId();
        if (i == R.id.action_delete) {
            final List<Integer> selectedItemPositions = mAdapter.getSelectedItems();
            final int size = selectedItemPositions.size();
            final int items = countItems(selectedItemPositions);

            AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
            alert.setTitle(getString(R.string.action_delete));
            alert.setMessage(items > 0 ? getResources().getQuantityString(R.plurals.delete_content_confirmation, size, size, items)
                    : getResources().getQuantityString(R.plurals.delete_confirmation, size, size));
            alert.setPositiveButton("Yes", (dialog, which) -> {
                DataLoader.getInstance().deleteSelectedFiles(getActivity(), selectedItemPositions, mData, mPath);
                dialog.dismiss();
            });
            alert.setNegativeButton("No", (dialog, which) -> dialog.dismiss());
            alert.show();
            actionMode.finish();
            return true;
        } else if (i == R.id.action_share) {
            final List<Integer> selectedSharePositions = mAdapter.getSelectedItems();
            if (selectedSharePositions.isEmpty()) return false;
            if (selectedSharePositions.size() == 1) {
                FileData fileData = mData.get(selectedSharePositions.get(0));
                IntentUtils.shareFile(getActivity(), fileData);
            } else {
                IntentUtils.shareFiles(getActivity(), getSelectedData(selectedSharePositions));
            }
            actionMode.finish();
            return true;
        } else if (i == R.id.action_details) {
            DetailsDialogFragment.newInstance(getSelectedData(mAdapter.getSelectedItems()))
                    .show(getActivity().getSupportFragmentManager(), "details");
            actionMode.finish();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        this.actionMode = null;
        mAdapter.clearSelections();
        mSelectedActionItems.clear();
    }

    private List<FileData> getSelectedData(List<Integer> positions) {
        List<FileData> list = new ArrayList<>(positions.size());
        for (Integer pos : positions) {
            list.add(mData.get(pos));
        }
        return list;
    }

    private int countItems(List<Integer> selectedItemPositions) {
        int deleteCount = 0;
        for (Integer pos : selectedItemPositions) {
            FileData data = mData.get(pos);
            if (data.isFolder())
                deleteCount += data.getSize();
        }
        return deleteCount;
    }

    private void loadData() {
        FileManager.Options options = getOptions();
        if (options != null)
            DataLoader.getInstance().loadData(new File(mPath), options.getMimeType(), options.isShowHidden());
        else
            DataLoader.getInstance().loadData(new File(mPath));
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


    public static class DetailsDialogFragment extends DialogFragment {

        public static DetailsDialogFragment newInstance(List<FileData> datas) {
            DetailsDialogFragment fragment = new DetailsDialogFragment();
            Bundle args = new Bundle();
            args.putSerializable("list", (Serializable) datas);
            fragment.setArguments(args);
            return fragment;
        }

        private View mRootView;

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            mRootView = getActivity().getLayoutInflater().inflate(R.layout.fragment_file_details, null);

            try {
                initViews();
            } catch (Exception e) {
                Log.e(TAG, "Error while initializing dialog: " + e);
                dismiss();
            }

            AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity(), R.style.AppTheme_Dialog_CenterTitle)
                    .setView(mRootView)
                    .setTitle(getString(R.string.action_details))
                    .setNeutralButton(R.string.ok, (dialog, which) -> dismiss());
            return alertDialog.create();
        }

        private void initViews() {
            Bundle args = getArguments();
            if (args == null || !args.containsKey("list")) {
                dismiss();
                return;
            }
            List<FileData> dataList = (List<FileData>) args.getSerializable("list");
            if (dataList == null) return;
            boolean isMultiple = dataList.size() > 1;
            FileData data = dataList.get(0);

            TextView name = mRootView.findViewById(R.id.name);
            TextView type = mRootView.findViewById(R.id.type);
            TextView path = mRootView.findViewById(R.id.path);
            TextView size = mRootView.findViewById(R.id.size);

            path.setText(data.getPath());

            if (isMultiple) {
                name.setText(getString(R.string.multiple_files));
                type.setText(getString(R.string.multiple_files));
                long totalSize = 0;
                for (FileData fileData : dataList) {
                    totalSize += Utils.getFileSize(new File(fileData.getPath()));
                }
                size.setText(Formatter.formatShortFileSize(getContext(), totalSize));
            } else {
                name.setText(data.getName());
                type.setText(getString(data.isFolder() ? R.string.folder : R.string.file));
                size.setText(Formatter.formatShortFileSize(getContext(), Utils.getFileSize(new File(data.getPath()))));
            }
        }

    }


    class MyFileObserver extends FileObserver {
        static final int mask = (FileObserver.CREATE |
                FileObserver.DELETE |
                FileObserver.DELETE_SELF |
                FileObserver.MODIFY |
                FileObserver.MOVED_FROM |
                FileObserver.MOVED_TO |
                FileObserver.MOVE_SELF);

        MyFileObserver(String path) {
            super(path, mask);
        }

        @Override
        public void onEvent(int event, String path) {
            if (path == null) {
                return;
            }
            loadData();
        }
    }


    interface FragmentStateListener {
        void onFragmentResume(String path);
    }

}
