package com.github.devjn.simplefilemanager.common;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

/**
 * Created by @author Jahongir on 24-Apr-17
 * devjn@jn-arts.com
 * FileData
 */

public class FileData implements Comparable, Serializable {

    private boolean isFolder;
    private String name;
    private String path;
    private long size;

    public FileData(String name, String path, boolean isFolder) {
        this.isFolder = isFolder;
        this.path = path;
        this.name = name;
    }

    public FileData(String name, String path, boolean isFolder, long size) {
        this.isFolder = isFolder;
        this.path = path;
        this.name = name;
        this.size = size;
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

    public long getSize() {
        return size;
    }

    @Override
    public int compareTo(@NotNull Object obj) {
        if(!(obj instanceof FileData)) return 0;
        FileData data = (FileData) obj;
        if(data.isFolder && !isFolder)
            return 1;
        else if(!data.isFolder && isFolder)
            return -1;
        return name.compareTo(data.name);
    }
}
