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

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.devjn.filemanager.utils.MimeTypeUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by @author Jahongir on 24-Apr-17
 * devjn@jn-arts.com
 * FileListAdapter
 */

public class FileListAdapter extends RecyclerView.Adapter<FileListAdapter.RecyclerViewHolders> {

    private Context context;
    private boolean isPortrait;
    private List<? extends FileData> itemList;
    private FilesClickListener clickListener;
    private SparseBooleanArray selectedItems;

    private boolean showCount;

    public FileListAdapter(Context context, FilesClickListener clickListener, List<? extends FileData> itemList, boolean isPortrait) {
        this.context = context;
        this.itemList = itemList;
        this.isPortrait = isPortrait;
        this.clickListener = clickListener;
        selectedItems = new SparseBooleanArray();
        showCount = FileManager.getConfig().isShowFolderCount();
    }

    @Override
    public RecyclerViewHolders onCreateViewHolder(ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(isPortrait ? R.layout.list_item : R.layout.grid_item, parent, false);
        return new RecyclerViewHolders(layoutView);
    }

    @Override
    public void onViewRecycled(RecyclerViewHolders holder) {
        super.onViewRecycled(holder);
        FileManager.getConfig().getImageLoader().clear(holder.picture);
    }

    @Override
    public void onBindViewHolder(RecyclerViewHolders holder, int position) {
        FileData fileData = itemList.get(position);
        String name = fileData.getName();
        holder.name.setText(name);
        holder.name.setTextSize(14);
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

        RecyclerViewHolders(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
            name = itemView.findViewById(R.id.name);
            size = itemView.findViewById(R.id.size);
            picture = itemView.findViewById(R.id.imageView);
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