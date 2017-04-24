package com.github.devjn.simplefilemanager;

import android.app.ActionBar;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.github.devjn.simplefilemanager.utils.Utils;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;


public class MainActivityFragment extends Fragment implements DataLoader.DataListener, FileListAdapter.FilesClickListener {

    public static String TAG = MainActivityFragment.class.getSimpleName();
    private static String NAME_KEY = "NAME";
    private static String FOLDER_KEY = "FOLDER";

    public static MainActivityFragment newInstance(String name, String folderPath) {
        MainActivityFragment fragment = new MainActivityFragment();
        Bundle args = new Bundle();
        args.putString(NAME_KEY, name);
        args.putString(FOLDER_KEY, folderPath);
        fragment.setArguments(args);
        return fragment;
    }

    public MainActivityFragment() {
    }

    private View mRootView;
    private RecyclerView mRecyclerView;
    private GridLayoutManager mLayoutManager;
    private FileListAdapter mAdapter;

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
        if (args != null && args.containsKey(FOLDER_KEY)) {
            mPath = args.getString(FOLDER_KEY);
            mName = args.getString(NAME_KEY, "");
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
        ActionBar ab = getActivity().getActionBar();
        if (ab != null)
            ab.setTitle(mName);

        mAdapter = new FileListAdapter(getContext(), this, mData,
                getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT);
        mRecyclerView.setAdapter(mAdapter);
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
                refreshItem = item;
                setRefreshing(true);
                DataLoader.INSTANCE.loadData(new File(mPath));
                return true;
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
                Toast.makeText(getContext(), "Content updated", Toast.LENGTH_SHORT).show();
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
        FileData fileData = mData.get(position);
        if (fileData.isFolder()) {
            Fragment fragment = MainActivityFragment.newInstance(fileData.getName(), fileData.getPath());
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            transaction.replace(R.id.container, fragment, "fragment")
                    .addToBackStack(null)
                    .commit();
        } else {
            File folder = new File(fileData.getPath());
            MimeTypeMap myMime = MimeTypeMap.getSingleton();
            Intent newIntent = new Intent(Intent.ACTION_VIEW);
            String mimeType = myMime.getMimeTypeFromExtension(Utils.fileExt(fileData.getPath().substring(1)));
            newIntent.setDataAndType(Uri.fromFile(folder), mimeType);
            newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try {
                getContext().startActivity(newIntent);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(getContext(), "No handler for this type of file.", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public boolean onLongClick(int position) {
        FileData fileData = mData.get(position);
        return false;
    }
}
