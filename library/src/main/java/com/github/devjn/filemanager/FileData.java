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

import android.support.annotation.NonNull;
import android.webkit.MimeTypeMap;

import com.github.devjn.filemanager.utils.Utils;

import java.io.Serializable;

/**
 * Created by @author Jahongir on 24-Apr-17
 * devjn@jn-arts.com
 * FileData
 */

public class FileData implements Comparable, Serializable {

    private boolean isFolder;
    private boolean isHidden;
    private String name;
    private String path;
    private String mimeType;
    private String extension;
    private long size;


    public FileData(String name, String path, boolean isFolder, long size) {
        this.isFolder = isFolder;
        this.path = path;
        this.name = name;
        this.size = size;
        this.isHidden = Utils.isHidden(name);
        this.extension = Utils.fileExt(name);
        if(extension != null && !extension.isEmpty()) {
            this.mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
    }

    public String getPath() {
        return path;
    }

    public boolean isFolder() {
        return isFolder;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMimeType() {
        return mimeType;
    }

    public String getExtension() {
        return extension;
    }

    public boolean hasExtension() {
        return extension != null && !extension.isEmpty();
    }

    public boolean isHidden() {
        return isHidden;
    }

    public long getSize() {
        return size;
    }

    @Override
    public int compareTo(@NonNull Object obj) {
        if(!(obj instanceof FileData)) return 0;
        FileData data = (FileData) obj;
        if(data.isFolder && !isFolder)
            return 1;
        else if(!data.isFolder && isFolder)
            return -1;
        return name.compareTo(data.name);
    }
}
