package com.github.devjn.simplefilemanager;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.TextView;
import android.widget.Toast;

import com.github.devjn.simplefilemanager.utils.IntentUtils;
import com.github.devjn.simplefilemanager.utils.Utils;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static com.github.devjn.simplefilemanager.App.FILES_AUTHORITY;


public class ListFilesFragment extends Fragment implements DataLoader.DataListener, FileListAdapter.FilesClickListener, ActionMode.Callback {

    public static String TAG = ListFilesFragment.class.getSimpleName();
    private static String LOAD = "LOAD";
    private static String NAME_KEY = "NAME";
    private static String FOLDER_KEY = "FOLDER";

    public static ListFilesFragment newInstance(String name, String folderPath, boolean load) {
        ListFilesFragment fragment = new ListFilesFragment();
        Bundle args = new Bundle();
        args.putBoolean(LOAD, load);
        args.putString(NAME_KEY, name);
        args.putString(FOLDER_KEY, folderPath);
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

    private List<FileData> mData;
    private String mName;
    private String mPath;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
        Bundle args = getArguments();
        DataLoader.INSTANCE.setListener(this);
        mSelectedActionItems = new ArrayList<Integer>();
        if (args != null && args.containsKey(FOLDER_KEY)) {
            mPath = args.getString(FOLDER_KEY);
            mName = args.getString(NAME_KEY, "");
            if (!args.getBoolean(LOAD)) return;
            if (mPath != null && !mPath.isEmpty())
                DataLoader.INSTANCE.loadData(new File(mPath));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_main, container, false);

        mRecyclerView = (RecyclerView) mRootView.findViewById(R.id.list);
        mLayoutManager = new GridLayoutManager(getContext(), getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT ? 1
                : Utils.calculateNoOfColumns(getContext()), GridLayoutManager.VERTICAL, false);
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

        mAdapter = new FileListAdapter(getContext(), this, mData,
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
        DataLoader.INSTANCE.setListener(this);
        if (mPath != null && !mPath.isEmpty())
            DataLoader.INSTANCE.loadData(new File(mPath));
    }

    @Override
    public void onPause() {
        super.onPause();
        DataLoader.INSTANCE.removeListener(this);
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
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
        switch (item.getItemId()) {
            case R.id.action_refresh:
                if (actionMode != null) return false;
                refreshItem = item;
                setRefreshing(true);
                DataLoader.INSTANCE.loadData(new File(mPath));
                return true;
            case android.R.id.home:
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
    public void onDataLoad(@NotNull List<? extends FileData> list) {
        this.mData = (List<FileData>) list;
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
            Fragment fragment = ListFilesFragment.newInstance(fileData.getName(), fileData.getPath(), true);
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            transaction.replace(R.id.container, fragment, "child")
                    .addToBackStack(null)
                    .commit();
        } else {
            File folder = new File(fileData.getPath());
            MimeTypeMap myMime = MimeTypeMap.getSingleton();
            Intent newIntent = new Intent(Intent.ACTION_VIEW);
            String mimeType = myMime.getMimeTypeFromExtension(Utils.fileExt(fileData.getPath().substring(1)));
            newIntent.setDataAndType(FileProvider.getUriForFile(getActivity(), FILES_AUTHORITY, folder), mimeType);
            newIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try {
                startActivity(newIntent);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(getContext(), "No handler for this type of file.", Toast.LENGTH_LONG).show();
            }
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
        switch (item.getItemId()) {
            case R.id.action_delete:
                final List<Integer> selectedItemPositions = mAdapter.getSelectedItems();
                final int size = selectedItemPositions.size();
                final int items = countItems(selectedItemPositions);

                AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                alert.setTitle(getString(R.string.action_delete));
                alert.setMessage(items > 0 ? getResources().getQuantityString(R.plurals.delete_content_confirmation, size, size, items)
                        : getResources().getQuantityString(R.plurals.delete_confirmation, size, size));
                alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DataLoader.INSTANCE.deleteSelectedFiles(getActivity(), selectedItemPositions, mData, mPath);
                        dialog.dismiss();
                    }
                });
                alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                alert.show();
                actionMode.finish();
                return true;
            case R.id.action_share:
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
            case R.id.action_details:
                DetailsDialogFragment.newInstance(getSelectedData(mAdapter.getSelectedItems()))
                        .show(getActivity().getSupportFragmentManager(), "details");
                actionMode.finish();
                return true;
            default:
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
                    .setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dismiss();
                        }
                    });
            return alertDialog.create();
        }

        private void initViews() {
            Bundle args = getArguments();
            if (args == null || !args.containsKey("list")) dismiss();
            List<FileData> dataList = (List<FileData>) args.getSerializable("list");
            boolean isMultiple = dataList.size() > 1;
            FileData data = dataList.get(0);

            TextView name = (TextView) mRootView.findViewById(R.id.name);
            TextView type = (TextView) mRootView.findViewById(R.id.type);
            TextView path = (TextView) mRootView.findViewById(R.id.path);
            TextView size = (TextView) mRootView.findViewById(R.id.size);

            path.setText(data.getPath());

            if (isMultiple) {
                name.setText(getString(R.string.multiple_files));
                type.setText(getString(R.string.multiple_files));
                long totalSize = 0;
                for (FileData fileData : dataList) {
                    totalSize += Utils.getFileSize(new File(fileData.getPath()));
                }
                size.setText(Utils.humanReadableByteCount(totalSize, true));
            } else {
                name.setText(data.getName());
                type.setText(getString(data.isFolder() ? R.string.folder : R.string.file));
                size.setText(Utils.humanReadableByteCount(Utils.getFileSize(new File(data.getPath())), true));
            }
        }

    }

}
