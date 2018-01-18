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
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.github.devjn.filemanager.utils.MimeTypeUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
        showCount = Manager.getShowFolderCount();
    }

    @Override
    public RecyclerViewHolders onCreateViewHolder(ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(isPortrait ? R.layout.list_item : R.layout.grid_item, parent, false);
        return new RecyclerViewHolders(layoutView);
    }

    @Override
    public void onViewRecycled(RecyclerViewHolders holder) {
        super.onViewRecycled(holder);
        Glide.clear(holder.picture);
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
        setAppropriateIcon(fileData, fileData.getExtension(), holder.picture);
        holder.itemView.setActivated(selectedItems.get(position, false));
    }

    private void setAppropriateIcon(FileData fileData, String ext, ImageView imageView) {
        final int drawable;
        if (fileData.isFolder())
            drawable = R.drawable.ic_folder;
        else if (fileData.hasExtension()) {
            if (MimeTypeUtils.isImage(ext) || MimeTypeUtils.isVideo(ext)) {
                Glide.with(context).load(fileData.getPath()).into(imageView);
                return;
            } else if (MimeTypeUtils.isAudio(ext))
                drawable = R.drawable.ic_file_audio;
            else drawable = MapCompat.getOrDefault(MimeTypeUtils.icons, ext, R.drawable.ic_file);
        } else drawable = R.drawable.ic_file;

        imageView.setImageResource(drawable);
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

        public RecyclerViewHolders(View itemView) {
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

    public static class MapCompat {

        public static <K, V> V getOrDefault(@NonNull Map<K, V> map, K key, V defaultValue) {
            V v;
            return (((v = map.get(key)) != null) || map.containsKey(key))
                    ? v
                    : defaultValue;
        }
    }

}