package com.github.devjn.simplefilemanager;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.github.devjn.simplefilemanager.utils.MimeTypeUtils;
import com.github.devjn.simplefilemanager.utils.Utils;

import java.util.ArrayList;
import java.util.List;

import static com.github.devjn.simplefilemanager.R.id.imageView;

/**
 * Created by @author Jahongir on 24-Apr-17
 * devjn@jn-arts.com
 * FileListAdapter
 */

public class FileListAdapter extends RecyclerView.Adapter<FileListAdapter.RecyclerViewHolders> {

    private Context context;
    private boolean isPortrait;
    private List<FileData> itemList;
    private FilesClickListener clickListener;
    private SparseBooleanArray selectedItems;

    public FileListAdapter(Context context, FilesClickListener clickListener, List<FileData> itemList, boolean isPortrait) {
        this.context = context;
        this.itemList = itemList;
        this.isPortrait = isPortrait;
        this.clickListener = clickListener;
        selectedItems = new SparseBooleanArray();
    }

    @Override
    public RecyclerViewHolders onCreateViewHolder(ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(isPortrait ? R.layout.list_item : R.layout.grid_item, parent, false);
        return new RecyclerViewHolders(layoutView);
    }

    @Override
    public void onBindViewHolder(RecyclerViewHolders holder, int position) {
        FileData fileData = itemList.get(position);
        String name = fileData.getName();
        String ext = Utils.fileExt(name);
        holder.name.setText(name);
        if(name.startsWith("."))
            holder.picture.setAlpha(0.5f);
        else holder.picture.setAlpha(1.0f);
        holder.size.setText("");
        if (fileData.isFolder()) {
            holder.picture.setImageResource(R.drawable.ic_folder);
            holder.size.setText(String.valueOf(fileData.getSize()));
        } else if(ext != null && !ext.isEmpty()) {
            if (MimeTypeUtils.isImage(ext) || MimeTypeUtils.isVideo(ext))
                Glide.with(context).load(fileData.getPath()).asBitmap().into(holder.picture);
            else setAppropriateIcon(ext, holder.picture);
        } else holder.picture.setImageResource(R.drawable.ic_file);
        holder.itemView.setActivated(selectedItems.get(position, false));
    }

    private void setAppropriateIcon(String ext, ImageView imageView) {
        switch (ext) {
            case "doc":
            case "docx":
                imageView.setImageResource(R.drawable.ic_file_doc); break;
            case "pdf":
                imageView.setImageResource(R.drawable.ic_file_pdf); break;
            case "txt":
                imageView.setImageResource(R.drawable.ic_file_txt); break;
            case "html":
                imageView.setImageResource(R.drawable.ic_file_html); break;
            case "zip":
                imageView.setImageResource(R.drawable.ic_file_zip); break;
            default: imageView.setImageResource(R.drawable.ic_file);
        }
    }

    @Override
    public int getItemCount() {
        if (this.itemList == null) return 0;
        return this.itemList.size();
    }

    public void setData(List<FileData> data) {
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
        List<Integer> items = new ArrayList<Integer>(selectedItems.size());
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
            name = (TextView) itemView.findViewById(R.id.name);
            size = (TextView) itemView.findViewById(R.id.size);
            picture = (ImageView) itemView.findViewById(imageView);
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

    interface FilesClickListener {
        void onClick(int position);

        boolean onLongClick(int position);
    }

}