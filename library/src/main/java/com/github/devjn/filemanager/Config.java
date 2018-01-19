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

import android.os.Environment;
import android.widget.ImageView;

import com.github.devjn.filemanager.utils.Utils;

/**
 * Created by @author Jahongir on 30-Sep-17
 * devjn@jn-arts.com
 * Config
 */
@SuppressWarnings("UnusedReturnValue")
public class Config {

    public static final String TAG = "FileManager";

    public interface ImageManager {
        void load(ImageView imageView, FileData fileData);

        void clear(ImageView imageView);
    }

    public interface ImageLoader extends ImageManager {
        @Override
        default void clear(ImageView imageView) {
        }
    }

    private String defaultFolder;
    private boolean showFolderCount;
    private ImageManager imageLoader;

    public Config() {
        defaultFolder = Environment.getExternalStorageDirectory().getAbsolutePath();
        imageLoader = (ImageLoader) (imageView, fileData) -> {
            try {
                imageView.setImageBitmap(Utils.decodeSampledBitmap(fileData.getPath(), imageView.getWidth(), imageView.getHeight()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
    }

    public Config setDefaultFolder(String folder) {
        defaultFolder = folder;
        return this;
    }

    public Config setShowFolderCount(boolean show) {
        this.showFolderCount = show;
        return this;
    }

    public Config setCustomImageLoader(ImageLoader loader) {
        this.imageLoader = loader;
        return this;
    }


    public String getDefaultFolder() {
        return defaultFolder;
    }

    public boolean isShowFolderCount() {
        return showFolderCount;
    }

    public ImageManager getImageLoader() {
        return imageLoader;
    }

}
