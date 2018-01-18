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
import android.support.annotation.NonNull;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.github.devjn.filemanager.utils.MimeTypeUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by @author Jahongir on 30-Sep-17
 * devjn@jn-arts.com
 * DataLoader
 */
public class DataLoader {

    public interface DataListener {
        void onDataLoad(@NonNull List<FileData> list);
    }

    private static volatile DataLoader Instance = null;

    public static DataLoader getInstance() {
        DataLoader localInstance = Instance;
        if (localInstance == null) {
            synchronized (DataLoader.class) {
                localInstance = Instance;
                if (localInstance == null) {
                    Instance = localInstance = new DataLoader();
                }
            }
        }
        return localInstance;
    }

    private DataListener listener = null;

    public void setListener(DataListener listener) {
        this.listener = listener;
    }

    public void removeListener(DataListener listener) {
        if (this.listener == listener)
            this.listener = null;
    }

    public void loadData(File folder) {
        Observable<List<FileData>> observe = Observable.fromCallable(() -> fill(folder));
        observe.subscribeOn(Schedulers.io())
                .flatMap(Observable::fromIterable)
                .toSortedList()
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(list -> {
                            if (listener != null)
                                listener.onDataLoad(list);
                        }
                );
    }

    public void loadData(File folder, String mime, boolean showHidden) {
        Observable<List<FileData>> observe = Observable.fromCallable(() -> fill(folder, mime, showHidden));
        observe.subscribeOn(Schedulers.io())
                .flatMap(Observable::fromIterable)
                .toSortedList()
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(list -> {
                            if (listener != null)
                                listener.onDataLoad(list);
                        }
                );
    }

    private List<FileData> fill(File folder) {
        File[] files = folder.listFiles();
        List<FileData> dataFiles = new ArrayList<>();
        for (File file : files) {
            if (file.isDirectory()) {
                File[] buf = file.listFiles();
                int num_item;
                if (buf != null) {
                    num_item = buf.length;
                } else
                    num_item = 0;

                dataFiles.add(new FileData(file.getName(), file.getAbsolutePath(), true, num_item));
            } else {
                dataFiles.add(new FileData(file.getName(), file.getAbsolutePath(), false, file.length()));
            }
        }
        return dataFiles;
    }

    private List<FileData> fill(File folder, String mimeType, boolean showHidden) {
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        boolean filter = mimeType != null && !mimeType.isEmpty();
        String baseType = (filter && mimeType.endsWith("*")) ? mimeType.substring(0, mimeType.lastIndexOf("/")) : null;
        String ext = mimeTypeMap.getExtensionFromMimeType(mimeType);

        File[] files = folder.listFiles();
        List<FileData> dataFiles = new ArrayList<>();
        for (File file : files) {
            if (file.isDirectory()) {
                File[] buf = file.listFiles();
                int num_item;
                if (buf != null) {
                    num_item = buf.length;
                } else
                    num_item = 0;

                FileData data = new FileData(file.getName(), file.getAbsolutePath(), true, num_item);
                if (!showHidden && data.isHidden()) continue;
                dataFiles.add(data);
            } else {
                FileData data = new FileData(file.getName(), file.getAbsolutePath(), false, file.length());
                if (filter && !suit(data, mimeType, ext, baseType, showHidden)) continue;
                dataFiles.add(data);
            }
        }
        return dataFiles;
    }

    private boolean suit(FileData data, String mimeType, String baseExt, String baseType, boolean showHidden) {
        if (!showHidden && data.isHidden()) return false;
        if (data.isFolder() || mimeType.equals(data.getMimeType()))
            return true;

        String type = MimeTypeUtils.getType(data.getExtension());
        if (baseType != null) {
            if (type == null || !type.startsWith(baseType)) return false;
        } else {
            if (!baseExt.equals(type)) return false;
        }

        return true;
    }

    public void deleteSelectedFiles(Activity activity, List<Integer> selectedItems, List<? extends FileData> data, String path) {
        final AtomicInteger deletedCount = new AtomicInteger(0);
        Observable.fromIterable(selectedItems)
                .subscribeOn(Schedulers.io())
                .subscribe(i -> {
                    try {
                        File file = new File(data.get(i).getPath());
                        deletedCount.addAndGet(deleteRecursive(file));
                    } catch (Exception e) {
                        Log.e(Manager.TAG, "Failed to delete file" + e);
                    }
                }, e -> Log.e(Manager.TAG, "Failed to delete file" + e), () ->
                        activity.runOnUiThread(() -> {
                            Toast.makeText(activity, deletedCount.get() + "files deleted", Toast.LENGTH_SHORT).show();
                            loadData(new File(path));
                        }));
    }

    public int deleteRecursive(File fileOrDirectory) {
        int deletedCount = 0;
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                deletedCount += deleteRecursive(child);

        if (fileOrDirectory.delete())
            return ++deletedCount;
        return deletedCount;
    }

}
