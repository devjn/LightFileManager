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

import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.devjn.filemanager.components.FileViewListItem;
import com.github.devjn.filemanager.utils.MimeTypeUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by @author Jahongir on 24-Apr-17
 * devjn@jn-arts.com
 * FileListAdapter
 */

public class FileListAdapter extends RecyclerView.Adapter<FileListAdapter.RecyclerViewHolders> {

    private FileManager.RequestHolder requestHolder;
    private boolean isPortrait;
    private List<? extends FileData> itemList;
    private FilesClickListener clickListener;
    private SparseBooleanArray selectedItems;

    private boolean showCount;

    public FileListAdapter(@Nullable FileManager.RequestHolder requestHolder, FilesClickListener clickListener, List<? extends FileData> itemList, boolean isPortrait) {
        this.itemList = itemList;
        this.isPortrait = isPortrait;
        this.clickListener = clickListener;
        this.requestHolder = requestHolder;
        this.selectedItems = new SparseBooleanArray();
        showCount = FileManager.getInstance().getConfig().isShowFolderCount();
    }

    @Override
    public RecyclerViewHolders onCreateViewHolder(ViewGroup parent, int viewType) {
//        View layoutView = LayoutInflater.from(parent.getContext()).inflate(ViewUtils.getViewForStyle(isPortrait), parent, false);
        return new RecyclerViewHolders(new FileViewListItem(parent.getContext()));
    }

    @Override
    public void onViewRecycled(RecyclerViewHolders holder) {
        super.onViewRecycled(holder);
        FileManager.getInstance().getConfig().getImageLoader().clear(holder.picture);
    }

    @Override
    public void onBindViewHolder(RecyclerViewHolders holder, int position) {
        FileData fileData = itemList.get(position);
        String name = fileData.getName();
        holder.name.setText(name);
        if (name.startsWith("."))
            holder.picture.setAlpha(0.5f);
        else holder.picture.setAlpha(1.0f);
        holder.size.setText("");
        if (fileData.isFolder()) {
            if (showCount) holder.size.setText(String.valueOf(fileData.getSize()));
        }
        MimeTypeUtils.setIconForFile(holder.picture, fileData);
        holder.itemView.setActivated(selectedItems.get(position, false));
    }


    @Override
    public int getItemCount() {
        if (this.itemList == null) return 0;
        return this.itemList.size();
    }

    public void setData(List<? extends FileData> data) {
        this.itemList = data;
        this.notifyDataSetChanged();
    }

    public void toggleSelection(int pos) {
        if (selectedItems.get(pos, false)) {
            selectedItems.delete(pos);
        } else {
            selectedItems.put(pos, true);
        }
        notifyItemChanged(pos);
    }

    public void clearSelections() {
        selectedItems.clear();
        notifyDataSetChanged();
    }

    public int getSelectedItemCount() {
        return selectedItems.size();
    }

    public List<Integer> getSelectedItems() {
        List<Integer> items = new ArrayList<>(selectedItems.size());
        for (int i = 0; i < selectedItems.size(); i++) {
            items.add(selectedItems.keyAt(i));
        }
        return items;
    }


    class RecyclerViewHolders extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        TextView name;
        TextView size;
        ImageView picture;

        RecyclerViewHolders(FileViewListItem itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
            name = itemView.getTextName();
            size = itemView.getTextCount();
            picture = itemView.getImageCover();
        }

        @Override
        public void onClick(View view) {
            clickListener.onClick(getAdapterPosition());
        }

        @Override
        public boolean onLongClick(View v) {
            return clickListener.onLongClick(getAdapterPosition());
        }
    }

    public interface FilesClickListener {
        void onClick(int position);

        boolean onLongClick(int position);
    }

}