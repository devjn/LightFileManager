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

import android.app.Dialog;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Toast;

import java.io.File;
import java.util.List;


/**
 * Created by @author Jahongir on 30-Sep-17
 * devjn@jn-arts.com
 * FileManagerDialog
 */
public class FileManagerDialog extends DialogFragment implements DataLoader.DataListener, FileListAdapter.FilesClickListener {

    private View mRootView;
    private Toolbar mToolbar;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private FileListAdapter mAdapter;

    private List<FileData> mData;
    private String mPath;
    private String mName;
    private String parentPath;

    private String mimeType;
    private boolean showHidden;

    public static FileManagerDialog newInstance(String mimeType, boolean showHidden) {
        Bundle args = new Bundle();
        args.putString("mimeType", mimeType);
        args.putBoolean("showHidden", showHidden);

        FileManagerDialog fragment = new FileManagerDialog();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        Bundle args = getArguments();
        if (args != null) {
            this.mimeType = args.getString("mimeType", null);
            this.showHidden = args.getBoolean("showHidden", false);
        }
    }

/*    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.dialog_filemanager_fragment, container);

        return mRootView;
    }*/

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mRootView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_filemanager_fragment, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(mRootView);

        builder.setPositiveButton(android.R.string.ok, (dialog, which) -> dismiss());

        builder.setNegativeButton(android.R.string.cancel, (dialog, which) -> dismiss());

        builder.getContext().getTheme().applyStyle(R.style.Theme_Window_NoMinWidth, true);

        initView();

        Dialog dialog = builder.create();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
//        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        return dialog;
    }

    private void initView() {
        mToolbar = mRootView.findViewById(R.id.toolbar);
        mRecyclerView = mRootView.findViewById(R.id.list);
        mLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mToolbar.setNavigationOnClickListener(v -> {
            if (mPath != null && mPath.length() > 1 && !parentPath.equals(mPath)) {
                int index = mPath.lastIndexOf("/");
                if (index < 1) return;
                mPath = mPath.substring(0, index);
                File file = new File(mPath);
                if (file.isDirectory())
                    loadData(file);
            } else dismiss();
        });


        mAdapter = new FileListAdapter(getContext(), this, mData,
                getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT);
        mRecyclerView.setAdapter(mAdapter);

        parentPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        File folder = new File(Manager.getDefaultFolder());
        if (!folder.exists() || !folder.isDirectory())
            folder = Environment.getExternalStorageDirectory();

        DataLoader.getInstance().setListener(this);
        loadData(folder);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }


    @Override
    public void onDataLoad(@NonNull List<FileData> list) {
        this.mData = list;
        mAdapter.setData(mData);
    }

    @Override
    public void onClick(int position) {
        FileData fileData = mData.get(position);
        File file = new File(fileData.getPath());
        if (file.isDirectory())
            loadData(file);
        else {
            FileManager.deliverResult(file.getAbsolutePath());
            dismiss();
        }
    }

    @Override
    public boolean onLongClick(int position) {
        return false;
    }

    private void loadData(File folder) {
        mPath = folder.getPath();
        mName = folder.getName();
        mToolbar.setTitle(mName);
        DataLoader.getInstance().loadData(folder, mimeType, showHidden);
    }


}
